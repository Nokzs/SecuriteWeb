package com.example.walletback.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users") // "user" est souvent un mot réservé en SQL, "users" est plus sûr
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private UUID ssoId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private BigDecimal balance;

}
