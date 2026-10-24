package com.multistorage_upload.backend.Features.Object_Storage.Dtos;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PhotoUploadRequest {

    @NotNull(message="File is required")
    private final MultipartFile file;

    @Size(max = 250, message="Max 200 characters allowed in description")
    private String description;

    public PhotoUploadRequest(MultipartFile file, String description){
        this.file = file;
        this.description = description;
    }

    public MultipartFile getFile(){
        return file;
    }
    public String getDescription(){
        return description;
    }
}
