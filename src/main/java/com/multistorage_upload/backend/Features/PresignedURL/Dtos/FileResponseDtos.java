package com.multistorage_upload.backend.Features.PresignedURL.Dtos;

import java.time.LocalDateTime;

public record FileResponseDtos(
        Long id,
        String name,
        String contentType,
        byte[] data,
        long size,
        String description,
        LocalDateTime createdAt
) {
    
}
