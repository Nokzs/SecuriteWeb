package com.example.securitewebback.incident.dto;

import com.example.securitewebback.incident.entity.VoteChoice;
import jakarta.validation.constraints.NotNull;

public record CastVoteDto(
        @NotNull(message = "Le choix est obligatoire (FOR, AGAINST, ABSTAIN)")
        VoteChoice choice
) {}