package com.example.securitewebback.appartements.service;

import java.util.Optional;
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
import com.example.securitewebback.appartements.dto.UpdateApartmentDto;
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
                    downloadUrl = minioService.presignedDownloadUrl(objectName);
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

    public Page<ApartementDto> getApartmentsByOwnerId(UUID ownerId, Pageable pageable) {
        Page<Apartment> apartments = apartmentRepository.findByOwnerId(ownerId, pageable);
        return apartments.map(apartment -> {
            String downloadUrl = null;
            if (apartment.getPhotoFilename() != null) {
                try {
                    String objectName = apartment.getBuilding().getId().toString() + "/"
                            + apartment.getId().toString() + "/" + apartment.getPhotoFilename();
                    downloadUrl = minioService.presignedDownloadUrl(objectName);
                } catch (Exception e) {
                    log.error("Erreur génération lien photo", e);
                }
            }
            return ApartementDto.fromEntity(apartment, downloadUrl);
        });
    }

    private void validateTantiemes(Apartment apartment, int newTantiemes) {
        Building building = apartment.getBuilding();
        int maxBuilding = building.getTotalTantieme();

        int currentSum = building.getCurrentTantieme();
        int oldTantiemes = apartment.getTantiemes();
        int projectedSum = currentSum - oldTantiemes + newTantiemes;

        if (projectedSum > maxBuilding) {
            throw new IllegalArgumentException(
                    String.format("Dépassement : Le total serait de %d pour un max de %d", projectedSum, maxBuilding));
        }

        apartment.setTantiemes(newTantiemes);
        building.setCurrentTantieme(projectedSum);
    }

    @Transactional
    public Apartment update(UUID id, UpdateApartmentDto dto) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appartement non trouvé"));

        validateTantiemes(apartment, dto.tantiemes());

        handleProprietaireChange(apartment, dto.ownerEmail());

        handlePhotoUpdate(apartment, dto.photoFilename(), dto.deletePhoto());

        apartment.setNumero(dto.numero());
        apartment.setSurface(dto.surface());
        apartment.setNombrePieces(dto.nombrePieces());

        return apartmentRepository.save(apartment);
    }

    private void handleProprietaireChange(Apartment apartment, String newEmail) {
        Proprietaire currentProprio = apartment.getOwner();

        if (newEmail == null || newEmail.isBlank()) {
            if (currentProprio != null) {
                currentProprio.removeApartement(apartment);
            }
            return;
        }

        if (currentProprio != null && newEmail.equalsIgnoreCase(currentProprio.getEmail())) {
            return;
        }

        if (currentProprio != null) {
            currentProprio.removeApartement(apartment);
        }

        Optional<Proprietaire> nextProprio = proprietaireRepository.findByEmail(newEmail);

        nextProprio.get().addApartement(apartment);
    }

    private void handlePhotoUpdate(Apartment apartment, String newPhotoKey, boolean deleteRequested) {
        if (deleteRequested) {
            if (apartment.getPhotoFilename() != null) {
                minioService.remove(apartment.getPhotoFilename());
            }
            apartment.setPhotoFilename(null);
            return;
        }

        if (newPhotoKey != null && !newPhotoKey.isBlank()) {
            if (apartment.getPhotoFilename() != null) {
                minioService.remove(apartment.getBuilding().getId().toString() + "/"
                        + apartment.getId().toString()
                        + "/"
                        + newPhotoKey);
            }

            String link = apartment.getBuilding().getId().toString() + "/"
                    + apartment.getId().toString()
                    + "/"
                    + newPhotoKey;
            apartment.setPhotoFilename(link);
        }
    }
}
