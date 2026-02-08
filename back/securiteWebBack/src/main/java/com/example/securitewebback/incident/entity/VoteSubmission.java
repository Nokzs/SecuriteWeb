package com.example.securitewebback.incident.entity;

import com.example.securitewebback.auth.entity.Proprietaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vote_proprietaire", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vote_id", "proprietaire_id"}) // Un seul vote par personne par scrutin !
})
@Getter
@Setter
public class VoteSubmission {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire voter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteChoice choice;

    // On stocke les tantièmes au moment du vote (snapshot)
    // car si le proprio vend son appart demain, le vote passé doit rester valide avec le poids d'avant.
    @Column(nullable = false)
    private Integer tantiemesWeight;

    @CreationTimestamp
    private LocalDateTime votedAt;
}
