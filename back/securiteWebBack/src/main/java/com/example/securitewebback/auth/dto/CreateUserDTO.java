
package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface CreateUserDTO {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    String email();

    @NotBlank
    @Size(min = 10, max = 50)
    @Pattern(regexp = "^(?=.*[0-10])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Le mot de passe doit contenir au moins un chiffre, une minuscule, une majuscule et un caractère spécial")
    String password();

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format de téléphone invalide")
    String telephone();

    @NotBlank(message = "Le rôle est obligatoire")
    @Pattern(regexp = "^(SYNDIC|PROPRIETAIRE)$", message = "Le rôle doit être SYNDIC ou PROPRIETAIRE")
    String role();

    User toEntity();

}
