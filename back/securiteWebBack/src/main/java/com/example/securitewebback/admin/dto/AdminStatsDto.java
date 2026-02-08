package com.example.securitewebback.admin.dto;

public record AdminStatsDto(
        long totalUsers,
        long totalBuildings,
        long totalIncidents,
        long activeVotes
) {}