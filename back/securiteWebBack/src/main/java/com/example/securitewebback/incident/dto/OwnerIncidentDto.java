package com.example.securitewebback.incident.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OwnerIncidentDto(
        String id,
        String title,
        String status, // "PENDING", "IGNORED", "VOTED"
        LocalDateTime createdAt,

        // Champs optionnels (seulement si status == VOTED)
        String voteId,
        Double voteAmount,
        LocalDateTime voteEndDate,

        boolean hasVoted,
        List<String> photoUrl

) {}
