package com.example.securitewebback.appartements.dto;

import java.util.UUID;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;

import jakarta.validation.constraints.NotBlank;

public record CreateAppartementDto(@NotBlank(message = "le numero du lot doit être renseigné") String numero,
        Integer etage,
        Double surface,
        Integer nombrePieces,
        String photoFilename,
        @NotBlank(message = "le tantième du lot doit être renseigné") Integer tantiemes,
        UUID buildingId,
        String ownerEmail) {

    public Apartment toEntity(Building building, Proprietaire owner) {
        Apartment apt = new Apartment();
        apt.setNumero(this.numero());
        apt.setTantiemes(this.tantiemes());
        apt.setEtage(this.etage());
        apt.setSurface(this.surface());
        if (this.photoFilename() != null && !this.photoFilename().isEmpty()) {
            String id = UUID.randomUUID().toString();
            apt.setPhotoFilename(this.photoFilename() + "-" + id);
        }
        apt.setBuilding(building);
        if (owner != null) {
            owner.addApartement(apt);
        }
        return apt;
    }
}
