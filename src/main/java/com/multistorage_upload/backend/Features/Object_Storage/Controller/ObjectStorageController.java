package com.multistorage_upload.backend.Features.Object_Storage.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.Object_Storage.Services.ObjectStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

@RestController 
@RequestMapping("/v1/bucket-storage")
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
    
}
