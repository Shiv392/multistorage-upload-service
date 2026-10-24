package com.multistorage_upload.backend.Features.Object_Storage.Services;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.Object_Storage.Entity.BucketPhotosEntity;
import com.multistorage_upload.backend.Features.Object_Storage.Repository.BucketPhotosRepository;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service 
public class ObjectStorageService {

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private static final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int HEADER_SIZE = 12;
    private static final int MAX_FILENAME_LENGTH = 255;   // zyadatar filesystems ki limit
    private static final String SEP = ".";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BucketPhotosRepository bucketPhotoRepo;
    private final S3Client s3Client;
    private final String bucketName;

    public ObjectStorageService(
        BucketPhotosRepository _bucketPhotoRepo,
        S3Client _s3Client, 
        @Value("${app.aws.s3.bucket-name}") String _bucketName
    ){
        bucketPhotoRepo = _bucketPhotoRepo;
        bucketName = _bucketName;
        s3Client = _s3Client;
    }

    public List<PhotoResponse> getAllImages(){
        List<BucketPhotosEntity>bucketPhotosEntityList = this.bucketPhotoRepo.findAll();
        if(bucketPhotosEntityList == null || bucketPhotosEntityList.size() == 0) return new ArrayList<>();

        List<PhotoResponse>ans = new ArrayList<>();

        for(BucketPhotosEntity bucketPhotoEntity : bucketPhotosEntityList){
            //photo key 
            String photokey = bucketPhotoEntity.getPhotoKey();

            //create request to get s3 object 
            GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(photokey)
            .build();

            //3. fetch actual bytes from the s3
            ResponseBytes<GetObjectResponse>objectBytes = s3Client.getObject(request, 
                ResponseTransformer.toBytes()
            );

            byte[]data = objectBytes.asByteArray();

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
}
