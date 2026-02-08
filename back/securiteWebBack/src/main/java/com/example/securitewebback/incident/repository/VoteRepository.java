package com.example.securitewebback.incident.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.incident.entity.VoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.incident.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Optional<Vote> findFirstByIncidentIdAndStatusOrderByCreatedAtDesc(UUID incidentId, VoteStatus status);

    // On remonte la chaine : Vote -> Incident -> Appartement -> Immeuble -> Syndic
    @Query("SELECT v FROM Vote v " +
            "JOIN v.incident i " +
            "JOIN i.apartment a " +
            "JOIN a.building b " +
            "WHERE b.syndic.id = :syndicId " +
            "ORDER BY v.endDate DESC") // Les plus récents en premier
    List<Vote> findAllBySyndicId(UUID syndicId);

    List<Vote> findByIncidentId(UUID incidentId);

    List<Vote> findByCreatedById(UUID syndicId);
}