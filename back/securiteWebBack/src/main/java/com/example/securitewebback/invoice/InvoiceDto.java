package com.example.securitewebback.invoice;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.securitewebback.building.dto.BuildingDto;

public record InvoiceDto(
                                                                UUID id,
                                                                String label,
                                                                BigDecimal amount,
                                                                BuildingDto buildingId,
                                                                String statut, String createdAt) {
}
