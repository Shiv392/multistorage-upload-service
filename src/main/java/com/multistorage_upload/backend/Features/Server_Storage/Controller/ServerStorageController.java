package com.multistorage_upload.backend.Features.Server_Storage.Controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;
import com.multistorage_upload.backend.Features.Server_Storage.Dtos.ServerPhotoDataResponse;
import com.multistorage_upload.backend.Features.Server_Storage.Dtos.ServerPhotoResponse;
import com.multistorage_upload.backend.Features.Server_Storage.Services.ServerStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/server-storage/photos")
@CrossOrigin(origins = "http://localhost:4200")
public class ServerStorageController{

    private final ServerStorageService serverStorageService;

    public ServerStorageController(ServerStorageService _serverStorageService){
        serverStorageService = _serverStorageService;
    }

    @GetMapping
    public ResponseEntity<GlobalApiResponse<List<PhotoResponse>>> getAllPhotos() {
        List<PhotoResponse> images = serverStorageService.getAllImages();
        return ResponseEntity.ok(
                new GlobalApiResponse(
                        true,
                        "Successfully fetched server images",
                        images
                )
        );
    }

    @PostMapping
public ResponseEntity<GlobalApiResponse<PhotoResponse>> addPhoto(
        @Valid @ModelAttribute PhotoUploadRequest request) {
    PhotoResponse saved = serverStorageService.uploadImage(request);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new GlobalApiResponse<>(true, "Photo added successfully", saved));
}
    
}
