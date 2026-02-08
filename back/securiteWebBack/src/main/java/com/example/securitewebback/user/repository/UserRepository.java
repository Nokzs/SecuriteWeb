package com.example.securitewebback.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.securitewebback.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Modifying // Indique que c'est une requête qui modifie la base (DELETE)
    @Query(value = "DELETE FROM proprietaires_appartements WHERE proprietaire_id = :id", nativeQuery = true)
    void deleteProprioAppartRelation(@Param("id") UUID id);
}
