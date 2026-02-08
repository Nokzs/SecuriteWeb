package com.example.securitewebback.incident.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.incident.entity.Incident;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {
    // On cherche tous les incidents liés à une liste d'immeubles (au cas où le proprio a plusieurs apparts)
    @Query("SELECT i FROM Incident i " +
            "JOIN i.apartment a " +
            "JOIN a.building b " +
            "WHERE b.id IN :buildingIds " +
            "ORDER BY i.createdAt DESC")
    List<Incident> findAllByBuildingIds(@Param("buildingIds") List<UUID> buildingIds);


    List<Incident> findByReporterId(UUID id);
}


