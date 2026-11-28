package com.multistorage_upload.backend.Features.PresignedURL.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import com.multistorage_upload.backend.Features.PresignedURL.Dtos.FileResponseDtos;
import com.multistorage_upload.backend.Features.PresignedURL.Services.PreSignedUrlService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController 
@RequestMapping("/v1/bucket-storage/presigned-urls")
@CrossOrigin(origins = "http://localhost:4200")
public class PresignedurlController {

    private final PreSignedUrlService preSignedUrlService;

    public PresignedurlController(
        PreSignedUrlService _preSignedUrlService
    ){
        preSignedUrlService = _preSignedUrlService;
    }
    
    @GetMapping("")
    public ResponseEntity<GlobalApiResponse> getAllFiles() {
        List<FileResponseDtos>files = preSignedUrlService.getAllFiles();

        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Successfully fetch files", files)
        );
    }
    
    @PostMapping("/{file}")
    public ResponseEntity<GlobalApiResponse> createPreSignedUrl(@PathVariable String file) {
        String preSignedURL = preSignedUrlService.addFiles(file);
        return ResponseEntity.status(201)
        .body(
            new GlobalApiResponse(true, "Presigned URL Created", preSignedURL)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalApiResponse> deleteFile(@PathVariable String id){
        Long fileId = Long.parseLong(id);
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(false, "File has been deleted", null)
        );
    }
}
