package com.example.securitewebback.auth.dto;

import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.entity.User;

public record CreateSyndicDto(String email, String password, String nomAgence, String adresse, String telephone)
        implements CreateUserDTO {
    @Override
    public Role role() {
        return Role.SYNDIC;
    }

    @Override
    public User toEntity() {
        return new Syndic(email, password, nomAgence, adresse, adresse);
    }

}
