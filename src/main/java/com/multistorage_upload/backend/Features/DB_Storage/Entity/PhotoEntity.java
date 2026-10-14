package com.multistorage_upload.backend.Features.DB_Storage.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class PhotoEntity {

    public static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message="Invalid image")
    @NotEmpty(message="Invalid image")
    @Column(
        name = "name",
        nullable = false
    )
    private String name;

    @NotBlank(message = "Invalid image")
    @NotEmpty(message = "Invalid image")
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


    public long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public byte[] getData(){
        return data;
    }
}
