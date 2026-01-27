package com.example.securitewebback.auth.entity;

import java.util.List;

import com.example.securitewebback.appartements.entity.Apartment;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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

    @OneToMany
    List<Apartment> appartements;

    public Proprietaire() {
        super();
    }

    public Proprietaire(String email, String password, String nom, String prenom, String telephone) {
        super(email, password, Role.PROPRIETAIRE, telephone);
    }

    public void addApartement(Apartment apt) {
        this.appartements.add(apt);
        apt.setOwner(this);
    }

    public void removeApartement(Apartment apt) {
        this.appartements.remove(apt);
        apt.setOwner(null);
    }

}
