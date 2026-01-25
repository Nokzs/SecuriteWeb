package com.example.securitewebback.building.dto;

import com.example.securitewebback.building.entity.Building;

import jakarta.validation.constraints.NotBlank;

public record CreateBuildingDto(@NotBlank(message = "Le nom est obligatoire") String name,
                @NotBlank(message = "Le nom est obligatoire") String address,
                String filename) {

        public Building ToEntity() {
                Building building = new Building();
                building.setName(this.name());
                building.setAdresse(this.address());
                building.setPhotoFilename(this.filename());
                return building;
        }

}
