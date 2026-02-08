package com.example.securitewebback.appartements.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateApartmentDto(
                                @NotBlank String numero,
                                Double surface,
                                Integer nombrePieces,
                                int tantiemes,
                                @Email String ownerEmail,
                                boolean deletePhoto,
                                String photoFilename) {
}
