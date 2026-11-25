package com.multistorage_upload.backend.Features.PresignedURL.Entities;

import org.hibernate.validator.constraints.Length;

import com.multistorage_upload.backend.Features.PresignedURL.Classes.UploadStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "presigned_urls"
)
public class PresignedUrls {

    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message="File name is required")
    @NotBlank(message="File name is required")
    @Length(min=1, max=200, message="File name can't be more then 50 characters long")
    @Column(name="filename", nullable=false, length = 200)
    private String fileName;

    @Length(max = 200, message="Description can't be more then 200 characters long")
    @Column(name = "description", nullable=true)
    private String description;

    @NotNull(message="File type is required")
    @NotBlank(message="File type is required")
    @Length(min=1, max=50, message="Content type can't be more then 50 characters long")
    @Column (name = "content_type", nullable=false, length = 50)
    private String contentType;

    @NotNull(message="File size is required")
    @Max(value = MAX_SIZE_BYTES, message = "file size must not exceed 2MB")
    @Column(name = "size_in_bytes", nullable = false)
    private Long size_in_bytes;

    @NotNull(message="Upload status is required")
    @NotBlank(message="Upload status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable=false, length = 20)
    private UploadStatus uploadStatus = UploadStatus.PENDING;



    public PresignedUrls(){}

    public PresignedUrls(String _fileName, String _description, String _contentType, Long _byte, UploadStatus _uploadStatus){
        fileName = _fileName;
        description = _description;
        contentType = _contentType;
        size_in_bytes = _byte;
        uploadStatus = _uploadStatus;
    }

    public Long getId(){
        return id;
    }

    public String getDescription(){
        return description;
    }

    public String getFileName(){
        return fileName;
    }

    public Long getSize(){
        return size_in_bytes;
    }

    public UploadStatus getUploadStatus(){
        return uploadStatus;
    }
    
    public String getContentType(){
        return contentType;
    }
}
