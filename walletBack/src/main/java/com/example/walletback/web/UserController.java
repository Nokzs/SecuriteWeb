package com.example.walletback.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.walletback.entities.Operation.OperationSign;
import com.example.walletback.entities.User;
import com.example.walletback.repository.UserRepository;
import com.example.walletback.web.service.OperationService;
import com.example.walletback.web.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import com.example.walletback.dto.AddMoneyRequest;
import com.example.walletback.dto.TransfertMoneyRequest;
import com.example.walletback.dto.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.walletback.dto.UserDto;
import com.example.walletback.entities.User;
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    OperationService operationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @GetMapping
    public UserDto getUser(Authentication authentication) {
        UUID ssoId = UUID.fromString(authentication.getName());
        User user = userRepository.findBySsoId(ssoId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        return UserDto.fromEntity(user);
    }

    //@PreAuthorize("hasRole('PROPRIETAIRE')")
    @PostMapping("/addMoney")
    public ResponseEntity<?> addMoney(@RequestBody AddMoneyRequest request, Authentication authentication) {
        UUID ssoId = UUID.fromString(authentication.getName());
        boolean operationSuccess = this.userService.addMoney(ssoId, request);
        if (operationSuccess) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransfertMoneyRequest request, Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String tokenValue = jwt.getTokenValue();

        UUID ssoId = UUID.fromString(auth.getName());
        userService.transferMoney(ssoId, request, tokenValue);
    
        return ResponseEntity.ok().build();
    }}
