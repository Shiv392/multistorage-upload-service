package com.multistorage_upload.backend.Features.DB_Storage.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import com.multistorage_upload.backend.Features.DB_Storage.Service.DbStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;

@RestController 
@RequestMapping("/v1/db-storage")
public class DbStorageController {

    private DbStorageService dbStorageService;
    private final Logger logger = LoggerFactory.getLogger(DbStorageController.class);

    public DbStorageController(
        DbStorageService _dbStorageService
    ){
        dbStorageService = _dbStorageService;
    }

    @GetMapping("/photos")
    public ResponseEntity<GlobalApiResponse> getPhotos(){
        List<PhotoResponse> photos = this.dbStorageService.getPhotos();
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Photos fetched successfully", photos)
        );
    }

    @PostMapping(value="/photos", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalApiResponse> addPhotos(@Valid @ModelAttribute  PhotoUploadRequest request) throws IOException{
        PhotoResponse newPhoto = this.dbStorageService.addPhoto(request);
        logger.info("File name--------------------->"+ request.getFile().getName());

        return ResponseEntity.status(201)
        .body(
            new GlobalApiResponse(true, "Photo added successfully", newPhoto)
        );
    }

}
