package com.example.securitewebback.building.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.example.securitewebback.auth.entity.Syndic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = true)
    private String photoFilename;

    @ManyToOne
    @JoinColumn(name = "syndic_id") // Crée la colonne de clé étrangère dans la table building
    private Syndic syndic;
}
