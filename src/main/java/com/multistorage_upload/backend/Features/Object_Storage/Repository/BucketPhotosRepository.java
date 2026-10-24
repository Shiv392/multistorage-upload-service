package com.multistorage_upload.backend.Features.Object_Storage.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.multistorage_upload.backend.Features.Object_Storage.Entity.BucketPhotosEntity;

public interface BucketPhotosRepository extends  JpaRepository<BucketPhotosEntity, Long> {
    List<BucketPhotosEntity> findAllByOrderByCreatedAtDesc();
}
