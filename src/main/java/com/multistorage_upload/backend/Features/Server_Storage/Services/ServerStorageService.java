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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import com.multistorage_upload.backend.Features.Server_Storage.Dtos.ServerPhotoDataResponse;
import com.multistorage_upload.backend.Features.Server_Storage.Dtos.ServerPhotoResponse;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;

@Service
public class ServerStorageService {

    private static final Logger log = LoggerFactory.getLogger(ServerStorageService.class);
    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
            
    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int HEADER_SIZE = 12;

    private final Path uploadDir;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public ServerStorageService(@Value("${app.storage.upload-dir}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory", e);
        }
    }

    // ---------- POST ----------
    public ServerPhotoResponse uploadImage(MultipartFile file) {
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

        // Original filename kabhi path mein use mat karo (path traversal risk)
        String savedName = UUID.randomUUID() + "." + extensionFor(detectedType);
        Path target = uploadDir.resolve(savedName).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        } catch (IOException e) {
            // adhoori file chhod ke mat jao
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                log.warn("Could not delete partial file {}", target);
            }
            throw new UncheckedIOException("Failed to store file", e);
        }

        return new ServerPhotoResponse(savedName, detectedType, file.getSize());
    }

    // ---------- GET ----------
    public List<ServerPhotoDataResponse> getAllImages() {
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
        List<CompletableFuture<ServerPhotoDataResponse>> futures = files.stream()
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
                .toList();
    }

    // ---------- helpers ----------
    private byte[] readHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(HEADER_SIZE);   // file chhoti ho toh kam bytes milenge, error nahi
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

    private ServerPhotoDataResponse readImage(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            String contentType = Files.probeContentType(path);
            return new ServerPhotoDataResponse(
                    path.getFileName().toString(),
                    contentType != null ? contentType : "application/octet-stream",
                    bytes.length,
                    Base64.getEncoder().encodeToString(bytes)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}