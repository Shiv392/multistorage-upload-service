package com.multistorage_upload.backend.Features.Server_Storage.Dtos;

// GET response (metadata + image data)
public record ServerPhotoDataResponse(String fileName, String contentType, long size, String base64Data) {}