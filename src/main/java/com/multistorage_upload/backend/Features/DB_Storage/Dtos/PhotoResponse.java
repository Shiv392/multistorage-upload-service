package com.multistorage_upload.backend.Features.DB_Storage.Dtos;

import java.time.LocalDateTime;

import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;

public record PhotoResponse(
        Long id,
        String name,
        String contentType,
        byte[] data,
        long size,
        String description,
        LocalDateTime createdAt
) {
    public static PhotoResponse from(PhotoEntity e) {
        return new PhotoResponse(e.getId(), e.getName(), e.getContentType(),
                e.getData(),
                e.getSize(), e.getDescription(), e.getCreatedAt());
    }
}