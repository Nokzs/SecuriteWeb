package com.example.securitewebback.incident.dto;

import com.example.securitewebback.incident.entity.Vote;

import java.time.Instant;
import java.time.LocalDateTime;

public record VoteSummaryDto(
        String id,                // ID du vote
        String incidentTitle,     // Titre de l'incident lié
        String status,            // ONGOING, APPROVED, REJECTED...
        LocalDateTime endDate,

        // Statistiques
        int totalBuildingTantiemes, // Le total possible (ex: 1000)
        int participationTantiemes, // Le total des votants (ex: 650)
        int tantiemesFor,           // Combien de POUR
        int tantiemesAgainst,       // Combien de CONTRE
        int tantiemesAbstain        // Combien d'ABSTENTION
) {}
