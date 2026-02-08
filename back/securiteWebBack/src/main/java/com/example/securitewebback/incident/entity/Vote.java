package com.example.securitewebback.incident.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.incident.entity.VoteStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.example.securitewebback.auth.entity.Syndic; // Assure-toi que ce package est bon
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "votes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private Double amount; // Montant total des travaux proposés

    @Column(nullable = false)
    private LocalDateTime endDate; // Date limite pour voter

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Relation vers l'incident concerné
    // Un incident a généralement un seul vote actif, mais on met ManyToOne pour la flexibilité (historique)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    // Le syndic qui a créé ce vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_syndic_id", nullable = false)
    private Syndic createdBy;
}