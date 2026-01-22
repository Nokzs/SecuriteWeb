package com.example.securitewebback.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.auth.dto.CreateUserDTO;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.auth.service.RegistrationService;

import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CreateUserDTO request) {
        try {
            User newUser = registrationService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Utilisateur créé avec succès",
                    "userUuid", newUser.getId()));
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "Cet email est déjà utilisé"));
        }
    }
}
