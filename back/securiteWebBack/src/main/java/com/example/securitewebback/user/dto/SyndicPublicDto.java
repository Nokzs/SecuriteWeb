package com.example.securitewebback.user.dto;

public record SyndicPublicDto(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        Integer buildingsCount) {
}
