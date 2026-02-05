package com.example.securitewebback.building.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.auth.entity.Syndic;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @Column(nullable = false)
    private Integer totalTantieme = 1000;

    @Column(nullable = false)
    private Integer currentTantieme = 0;

    @ManyToOne
    @JoinColumn(name = "syndic_id")
    private Syndic syndic;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL)
    private List<Apartment> apartment;
}
