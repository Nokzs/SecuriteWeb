package com.example.securitewebback.auth.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.auth.dto.CreateProprietaireDto;
import com.example.securitewebback.auth.dto.CreateSyndicDto;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.service.RegistrationService;
import com.example.securitewebback.security.CustomUserDetails;

import com.example.securitewebback.security.FormLoginSuccesHandler;
import com.example.securitewebback.user.dto.ProprietaireDTO;
import com.example.securitewebback.user.dto.UserDto;
import com.example.securitewebback.user.repository.ProprietaireRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final FormLoginSuccesHandler successHandler;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public RegistrationController(RegistrationService registrationService, FormLoginSuccesHandler successHandler) {
        this.registrationService = registrationService;
        this.successHandler = successHandler;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody CreateSyndicDto dto,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. Création de l'utilisateur en base de données
        Syndic user = registrationService.registerSyndic(dto);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        SecurityContextHolder.setContext(context);

        // 5. CRUCIAL : Sauvegarde explicite dans la session (via le repository)
        // Sans cette ligne, la session est oubliée à la fin de la requête
        securityContextRepository.saveContext(context, request, response);

        // 6. Déclenchement du succès (envoi de la réponse JSON / Cookie CSRF)
        try {
            successHandler.onAuthenticationSuccess(request, response, auth);
        } catch (IOException | ServletException e) {
            throw new RuntimeException("Erreur lors de la finalisation de l'inscription", e);
        }
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
    /*
     * @GetMapping("user")
     * public UserDto getProfil(Authentication authentication) {
     * CustomUserDetails userDetails = (CustomUserDetails)
     * authentication.getPrincipal();
     * 
     * User user = userRepository.findById(userDetails.getUuid())
     * .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
     * "Utilisateur non trouvé"));
     * 
     * return UserDto.fromEntity(user);
     * }
     */
}
