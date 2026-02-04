package com.example.securitewebback.incident.dto;

import java.util.List;
import java.util.UUID;

import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Proprietaire;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateIncidentDto(
        @NotBlank(message = "le titre est obligatoire")
        String title,

        @NotBlank(message = "la description est obligatoire")
        String description,

        boolean isUrgent,

        @NotNull(message = "l'ID de la propriété est obligatoire")
        UUID apartmentId,

        List<String> photoFilenames
) {

    public Incident toEntity(Apartment apartment, Proprietaire reporter) {
        Incident incident = new Incident();
        incident.setTitle(this.title());
        incident.setDescription(this.description());
        incident.setUrgent(this.isUrgent());
        incident.setApartment(apartment);
        incident.setReporter(reporter);

        if (this.photoFilenames() != null && !this.photoFilenames().isEmpty()) {
            for (String photoFilename : this.photoFilenames()) {
                if (photoFilename != null && !photoFilename.isEmpty()) {
                    String id = UUID.randomUUID().toString();
                    String finalFilename = photoFilename + "-" + id;
                    // Les URLs seront générées par le service via MinIO
                }
            }
        }

        return incident;
    }
}
