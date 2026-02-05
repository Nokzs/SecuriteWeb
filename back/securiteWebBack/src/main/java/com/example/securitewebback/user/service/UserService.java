package com.example.securitewebback.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.user.repository.ProprietaireRepository;
import com.example.securitewebback.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final ProprietaireRepository proprietaireRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(ProprietaireRepository proprietaireRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.proprietaireRepository = proprietaireRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Proprietaire findByEmail(String email) {
        return proprietaireRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsFirstLogin(false);
        return userRepository.save(user);
    }
}
