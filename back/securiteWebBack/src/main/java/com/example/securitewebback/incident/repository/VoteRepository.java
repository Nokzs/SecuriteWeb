package com.example.securitewebback.incident.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.incident.entity.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    // List<Vote> findByIncidentId(UUID incidentId);
    // List<Vote> findByStatusAndEndDateBefore(VoteStatus status, LocalDateTime date);
}