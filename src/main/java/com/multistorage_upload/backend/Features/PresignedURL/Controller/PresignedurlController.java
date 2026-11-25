package com.multistorage_upload.backend.Features.PresignedURL.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;


@RestController 
@RequestMapping("/v1/bucket-storage/presigned-urls")
public class PresignedurlController {
    
    @GetMapping("")
    public ResponseEntity<GlobalApiResponse> getMethodName() {
        return ResponseEntity.status(200)
        .body(
            new GlobalApiResponse(true, "Fetched all presigned urls images", null)
        );
    }
    
}
