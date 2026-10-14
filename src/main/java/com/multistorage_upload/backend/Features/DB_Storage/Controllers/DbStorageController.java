package com.multistorage_upload.backend.Features.DB_Storage.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import com.multistorage_upload.backend.Features.DB_Storage.Service.DbStorageService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;
import java.util.List;

@RestController 
@RequestMapping("/v1/db-storage")
public class DbStorageController {

    private DbStorageService dbStorageService;

    public DbStorageController(
        DbStorageService _dbStorageService
    ){
        dbStorageService = _dbStorageService;
    }

    @GetMapping("/photos")
    public ResponseEntity<GlobalApiResponse> getPhotos(){
        List<PhotoEntity> photos = this.dbStorageService.getPhotos();
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Photos fetched successfully", photos)
        );
    }

    // @PostMapping("/photos")
    // public ResponseEntity<GlobalApiResponse> addPhotos(){
    //     List<PhotoEntity> photos = this.dbStorageService.getPhotos()();
    //     return ResponseEntity.status(200)
    //     .body(
    //         new GlobalApiResponse(true, "Photo addedd successfully", photos)
    //     );
    // }

}
