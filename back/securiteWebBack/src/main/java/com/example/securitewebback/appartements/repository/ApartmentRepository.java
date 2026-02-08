package com.example.securitewebback.appartements.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securitewebback.appartements.entity.Apartment;
import org.springframework.data.jpa.repository.Query;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    Page<Apartment> findByBuildingId(UUID buildingId, Pageable page);

    Page<Apartment> findByBuildingIdAndNumeroContaining(
            UUID buildingId,
            String numero,
            Pageable pageable);

    Page<Apartment> findByOwnerId(UUID ownerId, Pageable pageable);

    List<Apartment> findByOwnerId(UUID ownerId);

    @Query("SELECT SUM(a.tantiemes) FROM Apartment a WHERE a.owner.id = :ownerId AND a.building.id = :buildingId")
    Integer sumTantiemesByOwnerAndBuilding(UUID ownerId, UUID buildingId);

    boolean existsByOwnerIdAndBuildingId(UUID ownerId, UUID buildingId);
}
