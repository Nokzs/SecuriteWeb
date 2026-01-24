package com.example.securitewebback.user.dto;

import com.example.securitewebback.auth.entity.Role;

public record SyndicDto(
                                String email,
                                String telephone,
                                String nomAgence,
                                Role role,
                                String adresse) implements UserDto {

}
