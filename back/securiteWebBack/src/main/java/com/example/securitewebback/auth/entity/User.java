package com.example.securitewebback.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // Crée une table par enfant liée à celle-ci
@Getter
@Setter

public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String telephone;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {
    }

    public User(String email, String password, Role role, String telephone) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.telephone = telephone;
    }
}
