package com.multistorage_upload.backend.Utils.ExceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.multistorage_upload.backend.Utils.ApiResponse.GlobalApiResponse;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;
import com.multistorage_upload.backend.Utils.Exceptions.NotFoundException;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GlobalApiResponse> handleBadRequest(Exception e){
        return ResponseEntity.status(400)
        .body(
            new GlobalApiResponse(false, e.getMessage(), null)
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<GlobalApiResponse> handleNotFoundRequest(Exception e){
        return ResponseEntity.status(404)
        .body(
            new GlobalApiResponse(false, e.getMessage(), null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse> handleGlobalException(Exception e){
        return ResponseEntity.status(500)
        .body(
            new GlobalApiResponse(false, e.getMessage(), null)
        );
    }
}
