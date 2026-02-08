package com.example.securitewebback.config;

import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordFixer {

    @Bean
    CommandLineRunner resetPassword(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "wekosol277@codgal.com";

            // 1. Cherche l'utilisateur
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                // 2. Génère un hash VALIDE via l'encodeur de l'application
                String validHash = passwordEncoder.encode("123456");

                // 3. Met à jour et sauvegarde
                user.setPassword(validHash);
                userRepository.save(user);

                System.out.println("✅ MOT DE PASSE RÉPARÉ pour " + email);
                System.out.println("Nouveau Hash en base : " + validHash);
            } else {
                System.out.println("❌ Utilisateur introuvable pour le correctif : " + email);
            }
        };
    }
}