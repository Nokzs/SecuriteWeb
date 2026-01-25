package com.example.securitewebback.storage;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * Initialisation : Vérifie ou crée le bucket au lancement du Back
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' créé avec succès.", bucketName);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation de MinIO : {}", e.getMessage());
        }
    }

    /**
     * Génère une URL temporaire sécurisée (idéal pour SSO)
     * L'utilisateur n'a pas besoin d'être authentifié sur MinIO,
     * seulement sur ton Back.
     */
    public String generatePresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(5, TimeUnit.MINUTES) // URL valide 10 min
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Impossible de générer l'URL : " + e.getMessage());
        }
    }

    /**
     * Récupère le flux de données d'un fichier
     */
    public String presignedDownloadUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
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
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
