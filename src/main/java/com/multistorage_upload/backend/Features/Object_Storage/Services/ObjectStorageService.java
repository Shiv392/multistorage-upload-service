package com.multistorage_upload.backend.Features.Object_Storage.Services;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;
import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import com.multistorage_upload.backend.Features.Object_Storage.Entity.BucketPhotosEntity;
import com.multistorage_upload.backend.Features.Object_Storage.Repository.BucketPhotosRepository;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;
import com.multistorage_upload.backend.Utils.Validations.PhotoRequestValidation;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ObjectStorageService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_TYPES
            = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int HEADER_SIZE = 12;
    private static final int MAX_FILENAME_LENGTH = 255;   // zyadatar filesystems ki limit
    private static final String SEP = ".";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BucketPhotosRepository bucketPhotoRepo;
    private final S3Client s3Client;
    private final String bucketName;
    private final PhotoRequestValidation photoRequestValidation;

    public ObjectStorageService(
            BucketPhotosRepository _bucketPhotoRepo,
            S3Client _s3Client,
            @Value("${app.aws.s3.bucket-name}") String _bucketName,
            PhotoRequestValidation _PhotoRequestValidation
    ) {
        bucketPhotoRepo = _bucketPhotoRepo;
        bucketName = _bucketName;
        s3Client = _s3Client;
        photoRequestValidation = _PhotoRequestValidation;
    }

    public List<PhotoResponse> getAllImages() {
        List<BucketPhotosEntity> bucketPhotosEntityList = this.bucketPhotoRepo.findAllByOrderByCreatedAtDesc();
        if (bucketPhotosEntityList == null || bucketPhotosEntityList.size() == 0) {
            return new ArrayList<>();
        }

        List<PhotoResponse> ans = new ArrayList<>();

        for (BucketPhotosEntity bucketPhotoEntity : bucketPhotosEntityList) {
            //photo key 
            String photokey = bucketPhotoEntity.getPhotoKey();

            //create request to get s3 object 
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(photokey)
                    .build();

            //3. fetch actual bytes from the s3
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(request,
                    ResponseTransformer.toBytes()
            );

            byte[] data = objectBytes.asByteArray();

            //4. create actual DTO from meta data
            PhotoResponse response = new PhotoResponse(
                    bucketPhotoEntity.getId(),
                    bucketPhotoEntity.getName(),
                    bucketPhotoEntity.getContentType(),
                    data,
                    bucketPhotoEntity.getSize(),
                    bucketPhotoEntity.getDescription(),
                    bucketPhotoEntity.getCreatedAt()
            );

            ans.add(response);
        }

        return ans;
    }

    public PhotoResponse uploadImages(PhotoUploadRequest request) throws IOException {
        MultipartFile file = request.getFile();
        String description = request.getDescription();

        boolean isValidPhoto = photoRequestValidation.validatePhoto(file);
        if (!isValidPhoto) {
            throw new BadRequestException("Image not supported");
        }

        if (file.getSize() > PhotoEntity.MAX_SIZE_BYTES) {
            throw new BadRequestException("Image size should be maximum of 2MB");
        }

        String fileName = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "unnamed";
        long size = file.getSize();
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();

        // 1. Generate a unique S3 object key
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex).toLowerCase();
        }

        String photoKey = "photos/" + UUID.randomUUID() + extension;

        //2. build s3 upload request
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(photoKey)
                .contentType(contentType)
                .contentLength(size)
                .build();

        //3. upload actual file bytes to s3
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        //4. save photo meta data into the db
        BucketPhotosEntity newEntity = new BucketPhotosEntity(
                fileName,
                size,
                contentType,
                photoKey,
                description
        );
        BucketPhotosEntity saved = this.bucketPhotoRepo.save(newEntity);

        return new PhotoResponse(
                saved.getId(),
                saved.getName(),
                saved.getContentType(),
                bytes,
                saved.getSize(),
                saved.getDescription(),
                saved.getCreatedAt()
        );
    }

    public boolean deleteServerBucketImage(long id) {
        Optional<BucketPhotosEntity> entityOptional = this.bucketPhotoRepo.findById(id);
        if (entityOptional.isEmpty()) {
            throw new BadRequestException("Photo not exists");
        }

        BucketPhotosEntity entity = entityOptional.get();
        String photoKey = entity.getPhotoKey();

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(photoKey)
                .build();

        DeleteObjectResponse response = s3Client.deleteObject(deleteRequest);
        if (response != null && response.sdkHttpResponse().isSuccessful()) {
            bucketPhotoRepo.delete(entity);
            return true;
        }

        throw new RuntimeException("Failed to delete photo from S3");
    }
}
