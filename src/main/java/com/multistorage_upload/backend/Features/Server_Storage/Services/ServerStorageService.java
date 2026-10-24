package com.multistorage_upload.backend.Features.Server_Storage.Services;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;

@Service
public class ServerStorageService {

    private static final Logger log = LoggerFactory.getLogger(ServerStorageService.class);
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int HEADER_SIZE = 12;
    private static final int MAX_FILENAME_LENGTH = 255;   // zyadatar filesystems ki limit
    private static final String SEP = ".";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final Path uploadDir;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public ServerStorageService(@Value("${app.storage.upload-dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        log.info("Upload directory: {}", this.uploadDir);
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory", e);
        }
    }

    // ---------- POST ----------
    public PhotoResponse uploadImage(PhotoUploadRequest request) {
        MultipartFile file = request.getFile();
        String description = request.getDescription();

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("File size must be 2MB or less");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException("Only jpeg, png, gif and webp images are allowed");
        }

        // Magic bytes check: header ka Content-Type client ka claim hai, asli type file ke bytes batate hain
        String detectedType = detectType(readHeader(file));
        if (detectedType == null || !detectedType.equals(contentType)) {
            throw new BadRequestException("File content does not match a valid image type");
        }

        if (description!=null && description.getBytes(StandardCharsets.UTF_8).length > 80) {
    throw new BadRequestException("Description must be at most 80 bytes for server storage");
}

        String originalName = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "unnamed" : file.getOriginalFilename();
        String desc = description == null ? "" : description;
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        // uuid.createdAt.name(b64).description(b64).ext
        String savedName = String.join(SEP,
                UUID.randomUUID().toString(),
                createdAt.format(TS),
                encode(originalName),
                encode(desc),
                extensionFor(detectedType));

        if (savedName.length() > MAX_FILENAME_LENGTH) {
            throw new BadRequestException("File name and description are too long");
        }

        Path target = uploadDir.resolve(savedName).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                log.warn("Could not delete partial file {}", target);
            }
            throw new UncheckedIOException("Failed to store file", e);
        }

        try {
            return new PhotoResponse(null, originalName, detectedType, file.getBytes(),
                    file.getSize(), desc.isEmpty() ? null : desc, createdAt);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded file", e);
        }
    }

    // ---------- GET ----------
    public List<PhotoResponse> getAllImages() {
        List<Path> files;
        try (Stream<Path> stream = Files.list(uploadDir)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(this::isImage)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list upload directory", e);
        }

        // Pehle saare tasks submit karo (parallel chalenge)
        List<CompletableFuture<PhotoResponse>> futures = files.stream()
                .map(path -> CompletableFuture
                        .supplyAsync(() -> readImage(path), executor)
                        .exceptionally(ex -> {
                            log.warn("Failed to read {}", path, ex);
                            return null;
                        }))
                .toList();

        // Phir results collect karo
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))   // newest first
                .toList();
    }

    // ---------- helpers ----------
    private PhotoResponse readImage(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            String fileName = path.getFileName().toString();
            String[] parts = fileName.split("\\.", -1);   // -1: khaali description wala segment bhi mile

            // Naya format: uuid.createdAt.name.description.ext
            if (parts.length == 5) {
                try {
                    LocalDateTime createdAt = LocalDateTime.parse(parts[1], TS);
                    String desc = decode(parts[3]);
                    return new PhotoResponse(
                            null,
                            decode(parts[2]),
                            typeFor(parts[4]),
                            bytes,
                            bytes.length,
                            desc.isEmpty() ? null : desc,
                            createdAt);
                } catch (RuntimeException e) {
                    log.warn("Filename metadata is malformed, using fallback for {}", fileName);
                }
            }

            // Fallback: purani file (sirf uuid.ext) ya kharab naam
            LocalDateTime modified = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
            return new PhotoResponse(null, fileName, typeFor(parts[parts.length - 1]),
                    bytes, bytes.length, null, modified);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(HEADER_SIZE);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file header", e);
        }
    }

    // Signature se asli MIME type batata hai, unknown ho toh null
    private String detectType(byte[] h) {
        if (h.length < HEADER_SIZE) {
            return null;
        }
        if (h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF) {
            return "image/jpeg";
        }
        if (h[0] == (byte) 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return "image/png";
        }
        if (h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') {
            return "image/gif";
        }
        if (h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot > 0 && IMAGE_EXT.contains(name.substring(dot + 1));
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> throw new BadRequestException("Unsupported type");
        };
    }

    private String typeFor(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}