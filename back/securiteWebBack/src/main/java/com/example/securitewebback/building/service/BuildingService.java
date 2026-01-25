package com.example.securitewebback.building.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.building.dto.CreateBuildingDto;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.storage.MinioService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final MinioService minioService;

    public BuildingService(BuildingRepository buildingRepository, MinioService MinioService) {
        this.minioService = MinioService;
        this.buildingRepository = buildingRepository;
    }

    public Page<BuildingDto> getBuildingsBySyndicId(UUID syndicId, Pageable pageable) {
        Page<Building> buildingPage = buildingRepository.findBySyndicId(syndicId, pageable);
        Page<BuildingDto> buildingDtoPage = buildingPage.map((building) -> {
            String downloadUrl = null;
            if (building.getPhotoFilename() != null) {
                try {
                    String objectName = building.getId().toString() + "/" + building.getPhotoFilename();
                    downloadUrl = minioService.generatePresignedUrl(objectName);
                } catch (Exception e) {
                    log.error("Erreur génération lien photo", e);
                }
            }
            return BuildingDto.fromEntity(building, downloadUrl);
        });
        return buildingDtoPage;
    }

    public Building createBuilding(CreateBuildingDto createBuildingDto, UUID syndicId) {

        Building building = createBuildingDto.ToEntity();
        building.setId(syndicId);
        Building savedBuilding = buildingRepository.save(building);
        return savedBuilding;
    }
}
