package com.multistorage_upload.backend.Utils.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration 
public class AWSConfigs {

    //s3 client bean to get s3client builder object
    @Bean 
    public S3Client s3Client(){
        return S3Client.builder().region(Region.AP_SOUTH_2)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
    }

    @Bean 
    public S3Presigner s3Presigner(){
        return S3Presigner.builder()
        .region(Region.AP_SOUTH_2)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
    }
}
