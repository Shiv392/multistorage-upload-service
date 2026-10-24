package com.multistorage_upload.backend.Features.Object_Storage.Controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.Object_Storage.Services.ObjectStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;


@RestController 
@RequestMapping("/v1/bucket-storage")
@CrossOrigin(originPatterns="http://localhost:4200")
public class ObjectStorageController {

    private ObjectStorageService objectStorageService;

    public ObjectStorageController(ObjectStorageService _ObjectStorageService){
        objectStorageService = _ObjectStorageService;
    }

    @GetMapping("/server-bucket/photo")
    public ResponseEntity<GlobalApiResponse> getServerBucketPhotos(){
        List<PhotoResponse>photos = this.objectStorageService.getAllImages();

        return ResponseEntity.ok()
        .body(
            new GlobalApiResponse(true, "Successfully fetched bucket images from server", photos)
        );
    }

    @PostMapping(value= "/server-bucket/photo",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalApiResponse> postMethodName(@Valid @ModelAttribute  PhotoUploadRequest request) throws Exception {
        PhotoResponse saveResponse = this.objectStorageService.uploadImages(request);
        return ResponseEntity.status(201)
        .body(
            new GlobalApiResponse(true, "New photo uploaded successfull", saveResponse)
        );
    }
    
    
}
