package com.example.securitewebback.admin.dto;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.entity.User;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String email,
        String role,     // "SYNDIC", "PROPRIETAIRE", "ADMIN"
        String telephone,
        String details   // Ex: "Agence Dupond" ou "Jean Michel"
) {
    public static UserSummaryDto convertToDto(User user) {
        String details = "-";

        if (user instanceof Syndic syndic) {
            details = (syndic.getNomAgence() != null) ? syndic.getNomAgence() : "Agence sans nom";
        } else if (user instanceof Proprietaire prop) {
            details = prop.getNom() + " " + prop.getPrenom();
        } else if ("ADMIN".equals(user.getRole().name())) {
            details = "Administrateur";
        }

        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getTelephone(),
                details
        );
    }
}
