package com.example.securitewebback.incident.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateVoteDto(

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être supérieur à 0")
        Double amount,

        @NotNull(message = "La date de fin est obligatoire")
        @Future(message = "La date de fin du vote doit être dans le futur")
        LocalDateTime endDate
) {}