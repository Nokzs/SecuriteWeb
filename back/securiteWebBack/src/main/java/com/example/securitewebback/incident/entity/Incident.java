package com.example.securitewebback.incident.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Proprietaire;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean isUrgent;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne
    @JoinColumn(name = "reporter_uuid", nullable = false)
    private Proprietaire reporter;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IncidentPhoto> photos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public void addPhoto(IncidentPhoto photo) {
        photos.add(photo);
        photo.setIncident(this);
    }
}

