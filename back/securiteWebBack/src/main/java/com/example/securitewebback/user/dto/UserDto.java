package com.example.securitewebback.user.dto;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.Syndic;

public interface UserDto {

    String email();

    String telephone();

    Role role();

    static ProprietaireDTO build(Proprietaire proprietaire) {
        return new ProprietaireDTO(
                proprietaire.getEmail(),
                proprietaire.getTelephone(),
                proprietaire.getRole(),
                proprietaire.getNom(),
                proprietaire.getPrenom());
    }

    static SyndicDto buildSyndic(Syndic sy) {
        return new SyndicDto(
                sy.getEmail(),
                sy.getTelephone(),
                sy.getNomAgence(),
                sy.getRole(),
                sy.getAdresse());
    }
}
