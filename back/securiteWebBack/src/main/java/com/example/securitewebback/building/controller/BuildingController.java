package com.example.securitewebback.building.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.building.dto.CreateBuildingDto;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.service.BuildingService;
import com.example.securitewebback.security.CustomUserDetails;
import com.example.securitewebback.storage.MinioService;

@RestController()
@RequestMapping("/api/building")
public class BuildingController {
    private final BuildingService buildingService;
    private final MinioService minioService;

    public BuildingController(BuildingService buildingService, MinioService minioService) {
        this.buildingService = buildingService;
        this.minioService = minioService;
    }

    @GetMapping
    public ResponseEntity<Page<BuildingDto>> getBuildings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication auth) {
        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();

        Pageable pageable = PageRequest.of(page, size);

        Page<BuildingDto> buildingPage = buildingService.getBuildingsBySyndicId(syndicId, pageable);

        return ResponseEntity.ok(buildingPage);
    }

    @PostMapping
    public ResponseEntity<BuildingDto> createBuilding(@RequestBody CreateBuildingDto createBuildingDto,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        Building createdBuilding = buildingService.createBuilding(createBuildingDto, syndicId);
        String signedLink = null;
        if (createBuildingDto.photoFilename() != null) {
            String objectName = createdBuilding.getId().toString() + "/"
                    + createdBuilding.getPhotoFilename();
            try {
                signedLink = this.minioService.generatePresignedUrl(objectName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BuildingDto dto = BuildingDto.fromEntity(createdBuilding, signedLink);
        return ResponseEntity.ok(dto);
    }
}
