package com.example.securitewebback.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactSyndicDto(
        @NotBlank(message = "Le prénom est obligatoire") String firstName,
        @NotBlank(message = "Le nom est obligatoire") String lastName,
        @NotBlank(message = "Le téléphone est obligatoire") @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide") String phone,
        @NotBlank(message = "Le message est obligatoire") String message) {
}

