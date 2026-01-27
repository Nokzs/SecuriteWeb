package com.example.securitewebback.user.service;

import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.user.repository.ProprietaireRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final ProprietaireRepository proprietaireRepository;

    public UserService(ProprietaireRepository proprietaireRepository) {
        this.proprietaireRepository = proprietaireRepository;
    }

    public Proprietaire findByEmail(String email) {
        return proprietaireRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }
}
