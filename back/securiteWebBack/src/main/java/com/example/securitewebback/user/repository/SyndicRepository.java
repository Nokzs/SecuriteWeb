package com.example.securitewebback.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.auth.entity.Syndic;

@Repository
public interface SyndicRepository extends JpaRepository<Syndic, UUID> {

    Optional<Syndic> findByEmail(String email);

    // Recherche paginée par nom d'agence, email ou téléphone
    Page<Syndic> findByNomAgenceContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContaining(String nomAgence,
            String email, String telephone, Pageable pageable);
}
