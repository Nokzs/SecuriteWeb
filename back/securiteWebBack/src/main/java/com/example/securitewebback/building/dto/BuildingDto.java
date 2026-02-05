package com.example.securitewebback.building.dto;

import com.example.securitewebback.building.entity.Building;

public record BuildingDto(
        String id,
        String name,
        String adresse,
        String photoFilename,
        String syndicId,
        Integer totalTantieme,
        Integer currentTantieme) {

    public static BuildingDto fromEntity(Building building, String link) {
        if (building == null)
            return null;

        return new BuildingDto(
                building.getId().toString(),
                building.getName(),
                building.getAdresse(),
                link,
                building.getSyndic().getId().toString(),
                building.getTotalTantieme(),
                building.getCurrentTantieme());
    }

    public static BuildingDto fromEntity(Building building) {
        if (building == null)
            return null;

        return new BuildingDto(
                building.getId().toString(),
                building.getName(),
                building.getAdresse(),
                building.getPhotoFilename(),
                building.getSyndic().getId().toString(),
                building.getTotalTantieme(),
                building.getCurrentTantieme());
    }
}
