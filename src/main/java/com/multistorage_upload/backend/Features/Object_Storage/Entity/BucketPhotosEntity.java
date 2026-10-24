package com.multistorage_upload.backend.Features.Object_Storage.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "bucket_photos")
public class BucketPhotosEntity {

    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message="Name is required")
    @Column(
        name = "name",
        nullable = false
    )
    private String name;

    @NotNull(message = "Content type is required")
    @Column(
        name = "content_type",
        nullable = false,
        length=100
    )
    private String contentType;

    @Max(value = MAX_SIZE_BYTES, message = "images size must not exceed 2MB")
    @Column(
        name = "size_in_bytes",
        nullable = false
    )
    private long size;


    @Column(
        name = "photo_key",
        nullable=false,
        unique=true,
        length = 512
    )
    private String photoKey;

    @Size(max=200, message="Description can't be more then 200 characters long")
    @Column(name = "description")
    private String description;

     @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BucketPhotosEntity(){};

    public BucketPhotosEntity(String _name, String _contentType, String _photoKey, String _description){
        name = _name;
        contentType = _contentType;
        photoKey = _photoKey;
        description = _description;
        this.createdAt = LocalDateTime.now();
    }

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }
    public String getContentType(){
        return contentType;
    }
    public long getSize(){
        return size;
    }
    
    public String getPhotoKey(){
        return photoKey;
    }
    public String getDescription(){
        return description;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
