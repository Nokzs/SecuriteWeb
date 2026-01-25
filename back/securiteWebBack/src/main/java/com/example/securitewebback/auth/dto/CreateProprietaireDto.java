package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProprietaireDto(
        @NotBlank @Size(min = 10, max = 50) @Pattern(regexp = "^(?=.*[0-10])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Le mot de passe doit contenir au moins un chiffre, une minuscule, une majuscule et un caractère spécial") String password,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Le format de l'email est invalide") String email,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "le prenom est obligatoire") String prenom,
        @NotBlank(message = "Le téléphone est obligatoire") @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide") String telephone) {
    public Proprietaire toEntity() {
        return new Proprietaire(email, password, nom, prenom, telephone);
    }

}
