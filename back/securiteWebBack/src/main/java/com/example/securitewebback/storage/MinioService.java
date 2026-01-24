
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!found) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Bucket '{}' créé avec succès.", bucketName);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation de MinIO : {}", e.getMessage());
        }
    }

    /**
     * Upload un fichier (ex: facture, contrat de syndic)
     * @param pathName Le nom/chemin souhaité dans MinIO
     * @param file Le fichier venant du Frontend
     */
    public void upload(String pathName, MultipartFile file) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(pathName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            log.info("Fichier '{}' uploadé avec succès.", pathName);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload : " + e.getMessage());
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
                    .expiry(10, TimeUnit.MINUTES) // URL valide 10 min
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Impossible de générer l'URL : " + e.getMessage());
        }
    }

    /**
     * Récupère le flux de données d'un fichier
     */
    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
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
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
