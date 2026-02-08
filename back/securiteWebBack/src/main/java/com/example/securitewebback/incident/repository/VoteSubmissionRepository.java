package com.example.securitewebback.incident.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.incident.entity.VoteSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VoteSubmissionRepository extends JpaRepository<VoteSubmission, UUID> {
    // Pour vérifier si un user a déjà voté
    boolean existsByVoteIdAndVoterId(UUID voteId, UUID voterId);

    List<VoteSubmission> findByVoteId(UUID voteId);

    void deleteByVoterId(UUID voterId);
    void deleteByVoteId(UUID voteId);
}