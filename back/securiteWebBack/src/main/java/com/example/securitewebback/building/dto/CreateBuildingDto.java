package com.example.securitewebback.building.dto;

import java.util.UUID;

import com.example.securitewebback.building.entity.Building;

import jakarta.validation.constraints.NotBlank;

public record CreateBuildingDto(@NotBlank(message = "Le nom est obligatoire") String name,
                @NotBlank(message = "Le nom est obligatoire") String adresse,
                String photoFilename) {

        public Building ToEntity() {
                Building building = new Building();
                building.setName(this.name());
                building.setAdresse(this.adresse());
                if (this.photoFilename() != null && !this.photoFilename().isEmpty()) {
                        String id = UUID.randomUUID().toString();
                        building.setPhotoFilename(this.photoFilename() + "-" + id);
                }
                return building;
        }

}
