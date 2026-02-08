package com.example.securitewebback.incident.dto;

import com.example.securitewebback.incident.entity.Incident;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public record IncidentSyndicDto(
        String id,
        String title,
        String description,
        boolean isUrgent,
        LocalDateTime createdAt,
        String status,
        String ownerFirstName,
        String ownerLastName,
        String ownerPhone,
        String buildingName,
        String buildingAddress,
        String apartmentNumber,
        List<String> photoUrls
) {
    public static IncidentSyndicDto fromEntity(Incident incident, List<String> photoUrls) {
        return new IncidentSyndicDto(
                incident.getId().toString(),
                incident.getTitle(),
                incident.getDescription(),
                incident.isUrgent(),
                LocalDateTime.ofInstant(incident.getCreatedAt(), ZoneId.systemDefault()),
                incident.getStatus().name(),
                incident.getReporter().getPrenom(),
                incident.getReporter().getNom(),
                incident.getReporter().getTelephone(),
                incident.getApartment().getBuilding().getName(),
                incident.getApartment().getBuilding().getAdresse(),
                incident.getApartment().getNumero(),
                photoUrls
        );
    }
}