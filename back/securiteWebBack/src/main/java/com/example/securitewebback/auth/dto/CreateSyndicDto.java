package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.Syndic;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSyndicDto(
        @NotBlank @Size(min = 10, max = 50) @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).*$",
                message = "Le mot de passe doit contenir au moins un chiffre, une minuscule, une majuscule et un caractère spécial"
        ) String password,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Le format de l'email est invalide") String email,
        @NotBlank(message = "Le téléphone est obligatoire") @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide") String telephone,
        @NotBlank(message = "Le nom de l'agence est obligatoire") String nomAgence,
        @NotBlank(message = "L'adresse est obligatoire") String adresse) {

    public Syndic toEntity() {
        return new Syndic(email, password, nomAgence, adresse, telephone);
    }

}
