package com.example.securitewebback.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.auth.entity.Proprietaire;

@Repository
public interface ProprietaireRepository extends JpaRepository<Proprietaire, UUID> {

    Optional<Proprietaire> findByEmail(String email);
}
