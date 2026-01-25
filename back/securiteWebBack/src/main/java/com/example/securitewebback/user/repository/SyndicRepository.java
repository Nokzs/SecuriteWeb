
package com.example.securitewebback.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.auth.entity.Syndic;

@Repository
public interface SyndicRepository extends JpaRepository<Syndic, UUID> {

    Optional<SyndicRepository> findByEmail(String email);
}
