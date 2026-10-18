package com.multistorage_upload.backend.Features.DB_Storage.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="photos")
public class PhotoEntity {

    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @NotNull(message="Name is required")
    @Column(
        name = "name",
        nullable = false
    )
    private String name;

    @NotEmpty
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Max(value = MAX_SIZE_BYTES, message = "images size must not exceed 2MB")
    @Column(
        name = "size_in_bytes",
        nullable = false
    )
    private long size;

    @Column(
        name = "photo_data",
        nullable = false,
        columnDefinition = "bytea"
    )
    private byte[] data;

    @Size(max=200, message="Description can't be more then 200 characters long")
    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PhotoEntity() {}   // JPA ke liye zaroori

    public PhotoEntity(String name, String contentType, long size, byte[] data, String description) {
        this.name = name;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public long getId(){
        return id;
    }
    public long getSize(){
        return size;
    }
    public String getName(){
        return name;
    }
    public byte[] getData(){
        return data;
    }
    public String getDescription(){
        return description;
    }
    public String getContentType(){
        return contentType;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
}
