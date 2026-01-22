package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;

public record CreateProprietaireDto(String email, String password, String nom, String prenom, String telephone)
        implements CreateUserDTO {

    public Proprietaire toEntity() {
        return new Proprietaire(email, password, Role.PROPRIETAIRE.toString(), nom, prenom);
    }

    public Role role() {
        return Role.PROPRIETAIRE;
    }
}
