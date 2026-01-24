package com.example.securitewebback.auth.service;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.dto.CreateUserDTO;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.user.repository.UserRepository;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(CreateUserDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User newUser = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }
}
