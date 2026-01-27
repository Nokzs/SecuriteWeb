package com.example.securitewebback.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MinioConfig {
    @Value("${minio.internalUrl}")
    private String internalUrl;

    @Value("${minio.externalUrl}")
    private String externalUrl;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String accessSecret;

    @Bean("minioInternalClient")
    public MinioClient minioInternalClient() {
        return MinioClient.builder()
                .endpoint(internalUrl)
                .credentials(accessKey, accessSecret)
                .region("us-east-1")
                .build();
    }

    @Bean("minioExternalClient")
    public MinioClient minioExternalClient() {
        return MinioClient.builder()
                .endpoint(externalUrl)
                .credentials(accessKey, accessSecret)
                .region("us-east-1")
                .build();
    }
}
