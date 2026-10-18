package com.multistorage_upload.backend.Features.DB_Storage.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.multistorage_upload.backend.Features.DB_Storage.Entity.PhotoEntity;
import java.util.List;
import com.multistorage_upload.backend.Features.DB_Storage.Dtos.PhotoResponse;


@Repository 
public interface  PhotoRepository extends  JpaRepository<PhotoEntity, Long>{

}
