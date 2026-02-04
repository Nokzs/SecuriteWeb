package com.example.securitewebback.incident.dto;

import com.example.securitewebback.incident.entity.Incident;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record IncidentSyndicDto(
        String id,
        String title,
        String description,
        boolean isUrgent,
        LocalDateTime createdAt,
        String status,         // "PENDING", "IGNORED", "VOTED"

        // Infos Propriétaire
        String ownerFirstName,
        String ownerLastName,
        String ownerPhone,

        // Infos Immeuble
        String buildingName,
        String buildingAddress,
        String apartmentNumber,

        // Infos Photos
        int photoCount
) {

    public static IncidentSyndicDto fromEntity(Incident incident) {
        return new IncidentSyndicDto(
                incident.getId().toString(),
                incident.getTitle(),
                incident.getDescription(),
                incident.isUrgent(),

                // CONVERSION AUTOMATIQUE ICI
                LocalDateTime.ofInstant(incident.getCreatedAt(), ZoneId.systemDefault()),

                incident.getStatus().name(),

                // Infos Propriétaire
                incident.getReporter().getPrenom(),
                incident.getReporter().getNom(),
                incident.getReporter().getTelephone(),

                // Infos Immeuble
                incident.getApartment().getBuilding().getName(),
                incident.getApartment().getBuilding().getAdresse(),
                incident.getApartment().getNumero(),

                // Photos
                incident.getPhotos() != null ? incident.getPhotos().size() : 0
        );
    }
}

