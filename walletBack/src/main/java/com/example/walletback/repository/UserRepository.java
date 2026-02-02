
package com.example.walletback.repository;

import com.example.walletback.entities.User; // Vérifie que c'est bien le bon package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySsoId(UUID ssoId);

    Optional<User> findByEmail(String email);
}
