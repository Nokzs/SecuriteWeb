package com.example.securitewebback.user.dto;

import java.util.UUID;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SyndicDto.class, name = "SYNDIC"),
        @JsonSubTypes.Type(value = ProprietaireDTO.class, name = "PROPRIETAIRE")
})
public abstract class UserDto {
    private UUID uuid;
    private String email;
    private String telephone;
    private Role role;

    // La méthode factory reste très propre
    public static UserDto fromEntity(User user) {
        if (user instanceof Syndic s) {
            return new SyndicDto(s);
        } else if (user instanceof Proprietaire p) {
            return new ProprietaireDTO(p);
        }
        throw new IllegalArgumentException("Type d'utilisateur non supporté");
    }
}
