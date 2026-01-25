package com.example.securitewebback.building.dto;

import com.example.securitewebback.building.entity.Building;

public record BuildingDto(
        String id,
        String name,
        String adresse,
        String photoFilename,
        String syndicId) {

    public static BuildingDto fromEntity(Building building, String link) {
        if (building == null)
            return null;

        return new BuildingDto(
                building.getId().toString(),
                building.getName(),
                building.getAdresse(),
                link,
                building.getSyndic().getId().toString());
    }

}
