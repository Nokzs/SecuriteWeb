package com.example.securitewebback.appartements.dto;

import java.util.UUID;

import com.example.securitewebback.appartements.entity.Apartment;

/**
 * ApartementDto
 */
public record ApartementDto(
        UUID id,
        String numero,
        Integer etage,
        Double surface,
        Integer nombrePieces,
        Integer tantiemes,
        String photoFilename,
        String signedLink,
        BuildingSimpleDto building
) {
    public static ApartementDto fromEntity(Apartment apartment, String signedLink) {
        return new ApartementDto(
                apartment.getId(),
                apartment.getNumero(),
                apartment.getEtage(),
                apartment.getSurface(),
                apartment.getNombrePieces(),
                apartment.getTantiemes(), // Assure-toi que c'est bien un Integer dans ton Entité aussi
                apartment.getPhotoFilename(),
                signedLink,
                BuildingSimpleDto.fromEntity(apartment.getBuilding())
        );
    }
}