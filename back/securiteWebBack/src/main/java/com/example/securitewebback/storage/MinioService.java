package com.example.securitewebback.storage;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioInternalClient;
    private final MinioClient minioExternalClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public MinioService(
            @Qualifier("minioInternalClient") MinioClient minioInternalClient,
            @Qualifier("minioExternalClient") MinioClient minioExternalClient) {
        this.minioInternalClient = minioInternalClient;
        this.minioExternalClient = minioExternalClient;
    }

    @PostConstruct
    public void initializeBucket() {
        try {
            boolean found = minioInternalClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioInternalClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            System.err.println("Erreur init MinIO : " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTHODES POUR L'UPLOAD (PUT)
    // ==========================================

    /**
     * Upload vers le bucket par défaut (Ancien code)
     */
    public String generatePresignedUrl(String objectName) {
        return generatePresignedUploadUrl(this.bucketName, objectName);
    }

    /**
     * Upload vers un bucket spécifique (Immeuble)
     */
    public String generatePresignedUploadUrl(String targetBucketName, String objectName) {
        try {
            // Vérification/Création via client interne
            if (!minioInternalClient.bucketExists(BucketExistsArgs.builder().bucket(targetBucketName).build())) {
                minioInternalClient.makeBucket(MakeBucketArgs.builder().bucket(targetBucketName).build());
            }

            // Signature via client externe (PUT)
            return minioExternalClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(targetBucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur upload MinIO : " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTHODES POUR LA LECTURE (GET)
    // ==========================================

    /**
     * Lecture depuis le bucket par défaut (Pour BuildingService -> ça compile !)
     */
    public String presignedDownloadUrl(String objectName) {
        return presignedDownloadUrl(this.bucketName, objectName);
    }

    /**
     * Lecture depuis un bucket spécifique (Pour Appartements)
     */
    public String presignedDownloadUrl(String targetBucketName, String objectName) {
        try {
            return minioExternalClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(targetBucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture MinIO : " + e.getMessage());
        }
    }

    // ==========================================
    // SUPPRESSION
    // ==========================================

    public void remove(String objectName) {
        try {
            minioInternalClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur suppression MinIO : " + e.getMessage());
        }
    }
}