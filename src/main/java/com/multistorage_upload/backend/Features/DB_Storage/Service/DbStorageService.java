package com.multistorage_upload.backend.Features.DB_Storage.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import com.multistorage_upload.backend.Features.DB_Storage.Repository.PhotoRepository;

import java.io.IOException;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoUploadRequest;
import com.multistorage_upload.backend.Utils.Exceptions.BadRequestException;
import com.multistorage_upload.backend.Utils.Validations.PhotoRequestValidation;

import jakarta.transaction.Transactional;

@Service 
public class DbStorageService {

    private final PhotoRepository photoRepository;
    private final PhotoRequestValidation photoRequestValidation;

    public DbStorageService(
        PhotoRepository _photoRepository,
        PhotoRequestValidation _photoRequestValidation
    ){
        photoRepository = _photoRepository;
        photoRequestValidation = _photoRequestValidation;
    }
    
    public List<PhotoResponse> getPhotos(){
       List<PhotoEntity>photos = this.photoRepository.findAllByOrderByCreatedAtDesc();
       return photos.stream().map(PhotoResponse :: from).toList();
    }

    @Transactional
    public PhotoResponse addPhoto(PhotoUploadRequest request) throws IOException {
        MultipartFile file = request.getFile();

        boolean isValidPhoto = photoRequestValidation.validatePhoto(file);
        if(!isValidPhoto){
            throw new BadRequestException("Image not supported");
        }

        if(file.getSize() > PhotoEntity.MAX_SIZE_BYTES){
            throw new BadRequestException("Image size should be maximum of 2MB");
        }

        String fileName = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "unnamed";
        String description = request.getDescription();
        long size = file.getSize();
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();


        PhotoEntity newPhoto = new PhotoEntity(fileName, contentType, size, bytes, description);

        PhotoEntity saved = this.photoRepository.save(newPhoto);
        return PhotoResponse.from(saved);
    }
}
