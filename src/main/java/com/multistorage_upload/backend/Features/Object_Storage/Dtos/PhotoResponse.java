package com.multistorage_upload.backend.Features.Object_Storage.Dtos;

import java.time.LocalDateTime;

public record PhotoResponse(
        Long id,
        String name,
        String contentType,
        byte[] data,
        long size,
        String description,
        LocalDateTime createdAt
) {
    public static PhotoResponse from(BucketPhotoResponse e) {
        return new PhotoResponse(e.id(), e.name(), e.contentType(),
                e.data(),
                e.size(), e.description(), e.createdAt());
    }
}
