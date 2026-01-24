package com.example.securitewebback.user.dto;

import com.example.securitewebback.auth.entity.Role;

public record ProprietaireDTO(
                String email,
                String telephone,
                Role role,
                String nom,
                String prenom) implements UserDto {
}
