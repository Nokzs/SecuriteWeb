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

    @PreAuthorize("@apartmentSecurity.canAccessToBuildingBuilding(#buildingId, authentication)")
    @GetMapping("/{buildingId}")
    public ResponseEntity<ApartementAndBuildingDto> getApartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String search,
            @PathVariable("buildingId") UUID buildingId,
            Authentication auth) {

        Pageable pageable = PageRequest.of(page, limit);
        ApartementAndBuildingDto buildingPage = apartmentService.getApartmentsBySyndicId(buildingId, pageable, search);

        return ResponseEntity.ok(buildingPage);
    }

    @PreAuthorize("@apartmentSecurity.canAccessToBuildingBuilding(#createAppartementDto.buildingId, authentication)")
    @PostMapping
    public ResponseEntity<ApartementDto> createApartement(@RequestBody CreateAppartementDto createAppartementDto,
            Authentication auth) {

        Apartment createdApartment = apartmentService.createApartment(createAppartementDto);
        String signedLink = null;
        if (createdApartment.getPhotoFilename() != null) {

            String objectName = createdApartment.getBuilding().getId().toString() + "/"
                    + createdApartment.getId().toString()
                    + "/"
                    + createdApartment.getPhotoFilename();

            try {
                signedLink = this.minioService.generatePresignedUrl(objectName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
