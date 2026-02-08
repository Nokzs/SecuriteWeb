package com.example.securitewebback.auth.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.auth.dto.ChangePasswordDto;
import com.example.securitewebback.auth.dto.CreateProprietaireDto;
import com.example.securitewebback.auth.dto.CreateSyndicDto;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.service.RegistrationService;
import com.example.securitewebback.security.CustomUserDetails;

import com.example.securitewebback.security.FormLoginSuccesHandler;
import com.example.securitewebback.user.dto.ProprietaireDTO;
import com.example.securitewebback.user.dto.UserDto;
import com.example.securitewebback.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FormLoginSuccesHandler successHandler;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public RegistrationController(RegistrationService registrationService, UserService userService,
            PasswordEncoder passwordEncoder,
            FormLoginSuccesHandler successHandler) {
        this.registrationService = registrationService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody CreateSyndicDto dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. Création de l'utilisateur en base de données
        Syndic user = registrationService.registerSyndic(dto);

        /*
         * CustomUserDetails userDetails = new CustomUserDetails(user);
         * Authentication auth = new UsernamePasswordAuthenticationToken(
         * userDetails,
         * null,
         * userDetails.getAuthorities());
         * 
         * SecurityContext context = SecurityContextHolder.createEmptyContext();
         * context.setAuthentication(auth);
         * 
         * SecurityContextHolder.setContext(context);
         * 
         * securityContextRepository.saveContext(context, request, response);
         * 
         * try {
         * successHandler.onAuthenticationSuccess(request, response, auth);
         * } catch (IOException e) {
         * throw new RuntimeException("Erreur lors de la finalisation de l'inscription",
         * e);
         * }
         */
    }

    @GetMapping("/csrf")
    public void initCsrf() {
        // Méthode vide pour initialiser une session et obtenir un token CSRF
    }

    @PreAuthorize("hasRole('SYNDIC')")
    @PostMapping("/owner")
    public ResponseEntity<ProprietaireDTO> createOwner(@RequestBody CreateProprietaireDto dto, Authentication auth) {
        Proprietaire proprietaire = registrationService.registerProprietaire(dto);
        ProprietaireDTO proprietaireDTO = (ProprietaireDTO) UserDto.fromEntity(proprietaire);
        return ResponseEntity.ok(proprietaireDTO);
    }

    @PreAuthorize("hasRole('PROPRIETAIRE')")
    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordDto dto, Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (passwordEncoder.matches(dto.newPassword(), userDetails.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent");
        }

        userService.changePassword(userDetails.getUser(), dto.newPassword());

    }
}
