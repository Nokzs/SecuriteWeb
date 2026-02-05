package com.example.securitewebback.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank @Size(min = 10, max = 50) String newPassword) {
}
