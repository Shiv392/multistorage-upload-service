package com.multistorage_upload.backend.Utils.Validations;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;
import java.util.Set;

@Component 
public class PhotoRequestValidation {
    public boolean validatePhoto(MultipartFile file){
        if(file.isEmpty()){
            throw new BadRequestException("File can't be empty");
        }
        
        if (!Set.of("image/png", "image/jpeg", "image/webp").contains(file.getContentType())) {
        throw new IllegalArgumentException("Only PNG, JPEG or WEBP images are allowed");
    }

        return true;
    }
}
