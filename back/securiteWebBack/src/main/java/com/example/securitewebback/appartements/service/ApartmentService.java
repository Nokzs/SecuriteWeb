package com.example.securitewebback.appartements.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.storage.MinioService;
import com.example.securitewebback.user.repository.ProprietaireRepository;
import com.example.securitewebback.appartements.dto.ApartementAndBuildingDto;
import com.example.securitewebback.appartements.dto.ApartementDto;
import com.example.securitewebback.appartements.dto.CreateAppartementDto;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final ProprietaireRepository proprietaireRepository;
    private final MinioService minioService;

    public ApartmentService(ApartmentRepository apartmentRepository, BuildingRepository buildingRepository,
            ProprietaireRepository proprietaireRepository, MinioService minioService) {
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
        this.proprietaireRepository = proprietaireRepository;
        this.minioService = minioService;
    }

    @Transactional
    public Apartment createApartment(CreateAppartementDto dto) {
        Building building = buildingRepository.findById(dto.buildingId())
                .orElseThrow(() -> new EntityNotFoundException("Bâtiment non trouvé"));
        Proprietaire owner = null;

        if (dto.ownerEmail() != null && !dto.ownerEmail().isBlank()) {
            owner = proprietaireRepository.findByEmail(dto.ownerEmail())
                    .orElse(null);

        }
        if (building.getCurrentTantieme() + dto.tantiemes() > building.getTotalTantieme()) {
            throw new IllegalStateException("Quota dépassé");
        }
        Apartment apt = dto.toEntity(building, owner);

        building.setCurrentTantieme(building.getCurrentTantieme() + dto.tantiemes());

        return apartmentRepository.save(apt);
    }

    public ApartementAndBuildingDto getApartmentsBySyndicId(UUID buildingId, Pageable pageable, String search) {
        Page<Apartment> ApartmentPage = this.apartmentRepository.findByBuildingIdAndNumeroContaining(buildingId,
                search, pageable);
        Page<ApartementDto> apartmentPageDto = ApartmentPage.map((apartment) -> {
            String downloadUrl = null;
            if (apartment.getPhotoFilename() != null) {
                try {
                    String objectName = apartment.getBuilding().getId().toString() + "/" + apartment.getId().toString()
                            + "/"
                            + apartment.getPhotoFilename();
                    downloadUrl = minioService.generatePresignedUrl(objectName);
                } catch (Exception e) {
                    log.error("Erreur génération lien photo", e);
                }
            }
            return ApartementDto.fromEntity(apartment, downloadUrl);
        });
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Bâtiment non trouvé"));

        return ApartementAndBuildingDto.of(BuildingDto.fromEntity(building), apartmentPageDto);
    }
}
