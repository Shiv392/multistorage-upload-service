package com.multistorage_upload.backend.Features.PresignedURL.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import com.multistorage_upload.backend.Features.PresignedURL.Entities.PresignedUrls;
import com.multistorage_upload.backend.Features.PresignedURL.Services.PreSignedUrlService;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
        List<PresignedUrls>files = preSignedUrlService.getAllFiles();

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
    
}
