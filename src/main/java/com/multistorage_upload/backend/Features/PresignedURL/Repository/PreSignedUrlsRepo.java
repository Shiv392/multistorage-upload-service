package com.multistorage_upload.backend.Features.PresignedURL.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multistorage_upload.backend.Features.PresignedURL.Entities.PresignedUrls;

import java.util.List;

import com.multistorage_upload.backend.Features.PresignedURL.Classes.UploadStatus;


public interface  PreSignedUrlsRepo extends JpaRepository<PresignedUrls, Long> {
    
    List<PresignedUrls> findAllByUploadStatusOrderByUpdatedAtDesc(UploadStatus uploadStatus);
}
