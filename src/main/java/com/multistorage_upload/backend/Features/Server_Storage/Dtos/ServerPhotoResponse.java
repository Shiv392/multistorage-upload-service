package com.multistorage_upload.backend.Features.Server_Storage.Dtos;

// POST response (upload ke baad metadata)
public record ServerPhotoResponse(String fileName, String contentType, long size) {}