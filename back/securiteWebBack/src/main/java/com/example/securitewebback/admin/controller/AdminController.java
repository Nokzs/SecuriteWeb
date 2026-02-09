package com.example.securitewebback.admin.controller;

import com.example.securitewebback.admin.dto.AdminStatsDto;
import com.example.securitewebback.admin.dto.UserSummaryDto;
import com.example.securitewebback.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // 1. LES STATS (Pour les cartes du haut)
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // 2. LISTE DES UTILISATEURS
    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        // Le contrôleur ne sait pas comment on transforme les données,
        // il sait juste qu'il doit demander la liste au service.
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // 3. SUPPRIMER UN UTILISATEUR (Le Ban Hammer 🔨)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
