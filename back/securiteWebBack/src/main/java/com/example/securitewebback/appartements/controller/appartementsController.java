package com.example.securitewebback.appartements.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.appartements.dto.ApartementAndBuildingDto;
import com.example.securitewebback.appartements.dto.ApartementDto;
import com.example.securitewebback.appartements.dto.CreateAppartementDto;
import com.example.securitewebback.appartements.dto.UpdateApartmentDto;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.service.ApartmentService;
import com.example.securitewebback.security.CustomUserDetails;
import com.example.securitewebback.storage.MinioService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController()
@RequestMapping("/api/apartment")
@Slf4j
public class appartementsController {
    private final ApartmentService apartmentService;
    private final MinioService minioService;

    public appartementsController(ApartmentService apartmentService, MinioService minioService) {
        this.apartmentService = apartmentService;
        this.minioService = minioService;
    }

    @PreAuthorize("@apartmentSecurity.canAccessToBuilding(#buildingId, authentication)")
    @GetMapping("/{buildingId}")
    public ResponseEntity<ApartementAndBuildingDto> getApartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String search,
            @PathVariable("buildingId") UUID buildingId,
            Authentication auth) {

        Pageable pageable = PageRequest.of(page, limit);

        // 1. On récupère la page d'appartements brute depuis le service
        ApartementAndBuildingDto initialData = apartmentService.getApartmentsBySyndicId(buildingId, pageable, search);

        // 2. On transforme CHAQUE appartement pour lui ajouter son lien MinIO signé
        Page<ApartementDto> mappedApartments = initialData.appartement().map(apt -> {
            String signedUrl = null;
            if (apt.photoFilename() != null && !apt.photoFilename().isEmpty()) {
                try {
                    // On construit le chemin : "ID_APPART/nom_fichier.jpg"
                    String objectPath = apt.id().toString() + "/" + apt.photoFilename();

                    // On génère le lien de lecture (GET) pour le bucket de l'immeuble
                    signedUrl = minioService.presignedDownloadUrl(buildingId.toString(), objectPath);
                } catch (Exception e) {
                    System.err.println("Erreur signature image pour appart " + apt.id() + ": " + e.getMessage());
                }
            }

            // On recrée le DTO avec le lien signé inclus
            return new ApartementDto(
                    apt.id(), apt.numero(), apt.etage(), apt.surface(),
                    apt.nombrePieces(), apt.tantiemes(), apt.photoFilename(),
                    signedUrl, // <--- C'est ici que le lien magique arrive au Front !
                    apt.building()
            );
        });

        // 3. On renvoie le DTO global avec les appartements mis à jour
        return ResponseEntity.ok(new ApartementAndBuildingDto(initialData.building(), mappedApartments));
    }

    @PreAuthorize("@apartmentSecurity.canAccessToBuilding(#createAppartementDto.buildingId, authentication)")
    @PostMapping
    public ResponseEntity<ApartementDto> createApartement(@RequestBody CreateAppartementDto createAppartementDto, Authentication auth) {

        Apartment createdApartment = apartmentService.createApartment(createAppartementDto);
        String signedLink = null;

        if (createdApartment.getPhotoFilename() != null) {
            // 1. On sépare bien le Bucket (Immeuble) et l'Objet (Appart/Fichier)
            String bucketName = createdApartment.getBuilding().getId().toString();
            String objectPath = createdApartment.getId().toString() + "/" + createdApartment.getPhotoFilename();

            try {
                // 2. On utilise la méthode avec les 2 paramètres (Bucket dynamique)
                signedLink = this.minioService.generatePresignedUploadUrl(bucketName, objectPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. On passe le signedLink au DTO
        ApartementDto dto = ApartementDto.fromEntity(createdApartment, signedLink);
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApartementDto> updateApartment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApartmentDto dto) {

        Apartment apartment = apartmentService.update(id, dto);

        String uploadUrl = null;

        if (dto.photoFilename() != null) {

            String objectName = apartment.getBuilding().getId().toString() + "/"
                    + apartment.getId().toString()
                    + "/"
                    + apartment.getPhotoFilename();
            uploadUrl = minioService.generatePresignedUrl(objectName);
        }

        // 3. Transformation en DTO de réponse
        return ResponseEntity.ok(ApartementDto.fromEntity(apartment, uploadUrl));
    }

    @GetMapping
    public ResponseEntity<Page<ApartementDto>> getOwnerProperties(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int limit) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID ownerUuid = userDetails.getUuid();

        Pageable pageable = PageRequest.of(page, limit);
        Page<ApartementDto> properties = apartmentService.getApartmentsByOwnerId(ownerUuid, pageable);

        return ResponseEntity.ok(properties);
    }
}
