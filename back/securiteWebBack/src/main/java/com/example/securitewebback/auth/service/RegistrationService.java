package com.example.securitewebback.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.dto.CreateProprietaireDto;
import com.example.securitewebback.auth.dto.CreateSyndicDto;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.error.UserAlreadyExistsException;
import com.example.securitewebback.user.repository.SyndicRepository;
import com.example.securitewebback.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final SyndicRepository syndicRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            SyndicRepository syndicRepository) {
        this.userRepository = userRepository;
        this.syndicRepository = syndicRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Syndic registerSyndic(CreateSyndicDto dto) {
        if (syndicRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        Syndic newUser = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        return syndicRepository.save(newUser);
    }

    @Transactional
    public Proprietaire registerProprietaire(CreateProprietaireDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        Proprietaire newOwner = new Proprietaire(
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.nom(),
                dto.prenom(),
                dto.telephone());

        return (Proprietaire) userRepository.save(newOwner);
    }
}
