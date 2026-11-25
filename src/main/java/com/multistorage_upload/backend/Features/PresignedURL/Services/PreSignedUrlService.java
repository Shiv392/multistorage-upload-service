package com.multistorage_upload.backend.Features.PresignedURL.Services;

import org.springframework.stereotype.Service;

import com.multistorage_upload.backend.Features.PresignedURL.Entities.PresignedUrls;
import com.multistorage_upload.backend.Features.PresignedURL.Repository.PreSignedUrlsRepo;

import java.util.List;

import com.multistorage_upload.backend.Features.PresignedURL.Classes.UploadStatus;


@Service 
public class PreSignedUrlService {
    private final PreSignedUrlsRepo preSignedUrlsRepository;

    public PreSignedUrlService(
        PreSignedUrlsRepo _PreSignedUrlsRepo
    ){
        preSignedUrlsRepository = _PreSignedUrlsRepo;
    }

    public List<PresignedUrls> getAllFiles(){
        return this.preSignedUrlsRepository.findAllByUploadStatus(UploadStatus.UPLOADED);
    }
}
