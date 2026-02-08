package com.example.securitewebback.appartements.dto;

import java.util.UUID;

import com.example.securitewebback.building.entity.Building;

public record BuildingSimpleDto(
        UUID id,
        String name,
        String adresse,
        String photoFilename
) {
    public static BuildingSimpleDto fromEntity(Building building) {
        return new BuildingSimpleDto(
                building.getId(),
                building.getName(),
                building.getAdresse(),
                building.getPhotoFilename()
        );
    }
}

