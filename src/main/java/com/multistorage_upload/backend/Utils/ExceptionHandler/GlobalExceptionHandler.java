package com.multistorage_upload.backend.Utils.ExceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse> handleGlobalException(Exception e){
        return ResponseEntity.status(500)
        .body(
            new GlobalApiResponse(false, e.getMessage(), null)
        );
    }
}
