package com.multistorage_upload.backend.Features.DB_Storage.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.multistorage_upload.backend.Features.DB_Storage.Service.DbStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;

@RestController 
@RequestMapping("/v1/db-storage/photos")
@CrossOrigin(origins = "http://localhost:4200")
public class DbStorageController {

    private DbStorageService dbStorageService;

    public DbStorageController(
        DbStorageService _dbStorageService
    ){
        dbStorageService = _dbStorageService;
    }

    @GetMapping("")
    public ResponseEntity<GlobalApiResponse> getPhotos(){
        List<PhotoResponse> photos = this.dbStorageService.getPhotos();
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Photos fetched successfully", photos)
        );
    }

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalApiResponse> addPhotos(@Valid @ModelAttribute  PhotoUploadRequest request) throws IOException{
        PhotoResponse newPhoto = this.dbStorageService.addPhoto(request);

        return ResponseEntity.status(201)
        .body(
            new GlobalApiResponse(true, "Photo added successfully", newPhoto)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalApiResponse> deletePhotos(@PathVariable long id){
        boolean deleted = this.dbStorageService.removePhoto(id);

        if(deleted){
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Photo has been deleted", null)
        );
        }

        return ResponseEntity.status(500)
        .body(
            new GlobalApiResponse(true, "Server error", null)
        );
    }

}
