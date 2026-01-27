package com.example.securitewebback.appartements.dto;

import com.example.securitewebback.appartements.entity.Apartment;

/**
 * ApartementDto
 */
public record ApartementDto(
                                String id,

                                String numero,

                                Integer etage,

                                Double surface,

                                Integer nombrePieces,

                                String photoFilename,

                                Integer tantiemes,
                                String ownerId,

                                String buildingUuid) {

                public static ApartementDto fromEntity(Apartment apartment, String photoFilename) {
                                return new ApartementDto(
                                                                apartment.getId().toString(),
                                                                apartment.getNumero(),
                                                                apartment.getEtage(),
                                                                apartment.getSurface(),
                                                                apartment.getNombrePieces(),
                                                                photoFilename,
                                                                apartment.getTantiemes(),
                                                                apartment.getOwner() != null ? apartment.getOwner()
                                                                                                .getEmail() : null,
                                                                apartment.getBuilding().getId().toString());
                }

}
