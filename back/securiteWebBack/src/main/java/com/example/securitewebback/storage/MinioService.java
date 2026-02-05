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

    public MinioService(
            @Qualifier("minioInternalClient") MinioClient minioInternalClient,
            @Qualifier("minioExternalClient") MinioClient minioExternalClient) {
        this.minioInternalClient = minioInternalClient;
        this.minioExternalClient = minioExternalClient;
    }

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * Initialisation : Vérifie ou crée le bucket au lancement du Back
     */
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
        }
    }

    /**
     * Génère une URL temporaire sécurisée (idéal pour SSO)
     * L'utilisateur n'a pas besoin d'être authentifié sur MinIO,
     * seulement sur ton Back.
     */
    public String generatePresignedUrl(String objectName) {
        try {
            String signedUrl = minioExternalClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());

            return signedUrl;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de générer l'URL : " + e.getMessage());
        }
    }

    /**
     * Récupère le flux de données d'un fichier
     */
    public String presignedDownloadUrl(String objectName) {
        try {

            return minioExternalClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(5, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du téléchargement : " + e.getMessage());
        }
    }

    /**
     * Supprime un document
     */
    public void remove(String objectName) {
        try {
            minioInternalClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de laundefined suppression : " + e.getMessage());
        }
    }
}
