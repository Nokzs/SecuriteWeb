package com.example.securitewebback.appartements.dto;

import org.springframework.data.domain.Page;

import com.example.securitewebback.building.dto.BuildingDto;

public record ApartementAndBuildingDto(BuildingDto building, Page<ApartementDto> appartement) {
                public static ApartementAndBuildingDto of(BuildingDto building, Page<ApartementDto> appartement) {
                                return new ApartementAndBuildingDto(building, appartement);
                }
}
