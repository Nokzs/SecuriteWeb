package com.example.securitewebback.invoice;

import java.math.BigDecimal;

import com.example.securitewebback.building.dto.BuildingDto;

public record InvoiceDto(
                                String label,
                                BigDecimal amount,
                                BuildingDto buildingId,
                                String statut, String createdAt) {
}
