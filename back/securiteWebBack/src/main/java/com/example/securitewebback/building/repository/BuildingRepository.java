package com.example.securitewebback.building.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.example.securitewebback.building.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    Page<Building> findByNameContainingIgnoreCaseAndSyndicId(String name, UUID syndicId, Pageable pageable);

    List<Building> findBySyndicId(UUID syndicId);
}
