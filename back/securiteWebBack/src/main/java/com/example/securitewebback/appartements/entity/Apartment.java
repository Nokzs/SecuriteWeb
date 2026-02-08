package com.example.securitewebback.appartements.entity;

import java.util.UUID;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = true)
    private Integer etage;

    @Column(nullable = true)
    private Double surface;

    @Column(nullable = true)
    private Integer nombrePieces;

    @Column(nullable = true)
    private String photoFilename;

    @Column(nullable = false)
    private Integer tantiemes;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private Proprietaire owner;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;
}
