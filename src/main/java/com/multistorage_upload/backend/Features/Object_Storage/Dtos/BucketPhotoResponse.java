package com.multistorage_upload.backend.Features.Object_Storage.Dtos;

import java.time.LocalDateTime;

public record BucketPhotoResponse(
        Long id,
        String name,
        String contentType,
        byte[] data,
        long size,
        String description,
        LocalDateTime createdAt
) {
    
}
