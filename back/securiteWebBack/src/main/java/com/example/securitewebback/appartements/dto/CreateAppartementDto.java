package com.example.securitewebback.appartements.dto;

import java.util.UUID;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAppartementDto(
        @NotBlank(message = "le numero du lot doit être renseigné")
        String numero,

        Integer etage,
        Double surface,
        Integer nombrePieces,
        String photoFilename,

        @NotNull(message = "le tantième du lot doit être renseigné")
        @Positive(message = "le tantième doit être un nombre positif")
        Integer tantiemes,

        @NotNull(message = "l'ID de l'immeuble est obligatoire")
        UUID buildingId,

        String ownerEmail
) {

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
