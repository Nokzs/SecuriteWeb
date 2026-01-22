package com.example.securitewebback.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "proprietaires")
@Getter
@Setter
public class Proprietaire extends User {

    String nom;
    String prenom;

    public Proprietaire() {
        super();
    }

    public Proprietaire(String email, String password, String nom, String prenom, String telephone) {
        super(email, password, Role.PROPRIETAIRE, telephone);
    }
}
