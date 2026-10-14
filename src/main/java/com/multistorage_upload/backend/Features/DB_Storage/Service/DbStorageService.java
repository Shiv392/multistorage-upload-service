package com.multistorage_upload.backend.Features.DB_Storage.Service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import com.multistorage_upload.backend.Features.DB_Storage.Repository.PhotoRepository;
import java.util.List;

@Service 
public class DbStorageService {

    private final PhotoRepository photoRepository;

    public DbStorageService(
        PhotoRepository _photoRepository
    ){
        photoRepository = _photoRepository;
    }
    
    public List<PhotoEntity> getPhotos(){
       List<PhotoEntity>photos = this.photoRepository.findAll();
       return photos;
    }
}
