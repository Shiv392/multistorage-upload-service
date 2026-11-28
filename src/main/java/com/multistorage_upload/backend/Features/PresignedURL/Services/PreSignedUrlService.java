package com.multistorage_upload.backend.Features.PresignedURL.Services;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.multistorage_upload.backend.Features.PresignedURL.Entities.PresignedUrls;
import com.multistorage_upload.backend.Features.PresignedURL.Repository.PreSignedUrlsRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.multistorage_upload.backend.Features.PresignedURL.Classes.UploadStatus;
import com.multistorage_upload.backend.Features.PresignedURL.Dtos.FileResponseDtos;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Service
public class PreSignedUrlService {

    private final PreSignedUrlsRepo preSignedUrlsRepository;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    private final Logger logger = LoggerFactory.getLogger(PreSignedUrlService.class);

    public PreSignedUrlService(
            PreSignedUrlsRepo _PreSignedUrlsRepo,
            S3Presigner _s3Presigner,
            @Value("${app.aws.s3.bucket-name-presigned-urls}") String _bucketName,
            S3Client _s3Client
    ) {
        preSignedUrlsRepository = _PreSignedUrlsRepo;
        bucketName = _bucketName;
        s3Presigner = _s3Presigner;
        s3Client = _s3Client;
    }

    public List<FileResponseDtos> getAllFiles() {
        List<PresignedUrls> list = this.preSignedUrlsRepository.findAllByUploadStatusOrderByUpdatedAtDesc(UploadStatus.UPLOADED);
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }

        List<FileResponseDtos> files = new ArrayList<>();

        for (PresignedUrls entity : list) {
            String file_bucket_key = entity.getFileBucketKey();

            //create object request to fetch files
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName.trim())
                    .key(file_bucket_key)
                    .build();

            //get object bytes for the key
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(getObjectRequest,
                    ResponseTransformer.toBytes());

            // get bytes data from response 
            byte[] data = objectBytes.asByteArray();

            long fileSize = objectBytes.response().contentLength();

            //update the file size;
            if(entity.getSize() == null){
            entity.setSize(fileSize);
            preSignedUrlsRepository.save(entity);
            }

            //create file response and push into the complete response
            FileResponseDtos object = new FileResponseDtos(
                    entity.getId(),
                    entity.getFileName(),
                    entity.getfileType(),
                    data,
                    fileSize,
                     entity.getDescription(),
                    entity.getCreatedAt()
            );

            files.add(object);
        }

        return files;
    }

    public String addFiles(String file) {
        if (file == null || file.isBlank()) {
            throw new BadRequestException("File is required");
        }
        int lastDot = file.lastIndexOf(".");

        if (file == null || lastDot <= 0 || lastDot == file.length() - 1) {
            throw new BadRequestException("Invalid file format");
        }

        String fileName = file.substring(0, lastDot);
        String extension = file.substring(lastDot);

        String bucketFileKey = "file/" + fileName + UUID.randomUUID() + extension;

        String preSignedURL = generatedPreSignedURL(bucketFileKey);

        //update schema for the request 
        PresignedUrls newEntity = new PresignedUrls();
        newEntity.setBucketKey(bucketFileKey);
        newEntity.setFileName(fileName);
        newEntity.setFileType(extension);
        newEntity.setUploadStatus(UploadStatus.PENDING);

        PresignedUrls res = this.preSignedUrlsRepository.saveAndFlush(newEntity);
        return preSignedURL;
    }

    public String generatedPreSignedURL(String objectKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName.trim())
                .key(objectKey)
                .build();

        PutObjectPresignRequest presignRequest
                = PutObjectPresignRequest.builder() //create PutObjectPreSignedRequest
                        .signatureDuration(Duration.ofMinutes(10)) //add time duration for 
                        .putObjectRequest(putObjectRequest)
                        .build();

        String preSignedURL = s3Presigner.presignPutObject(presignRequest)
                .url()
                .toString();

        // logger.info("PreSigned url created........."+preSignedURL);
        return preSignedURL;
    }

    public Boolean deleteFileById(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid request");
        }

        //check if id exists in the table or not
        PresignedUrls fileData = this.preSignedUrlsRepository.findById(id).
                orElseThrow(() -> new BadRequestException("File not exists"));

        String fileBucketKey = fileData.getFileBucketKey();

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName.trim())
                .key(fileBucketKey)
                .build();

        s3Client.deleteObject(deleteRequest);

        //soft delete by marking deleted status
        fileData.markDeleted();

        return true;
    }
}
