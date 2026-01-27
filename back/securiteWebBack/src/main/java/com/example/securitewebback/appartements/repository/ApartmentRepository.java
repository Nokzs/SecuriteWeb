package com.example.securitewebback.appartements.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securitewebback.appartements.entity.Apartment;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    Page<Apartment> findByBuildingId(UUID buildingId, Pageable page);

    Page<Apartment> findByBuildingIdAndNumeroContaining(
            UUID buildingId,
            String numero,
            Pageable pageable);
}
