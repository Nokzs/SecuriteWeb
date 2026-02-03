package com.example.securitewebback.incident.controller;

import com.example.securitewebback.incident.dto.CreateVoteDto;
import com.example.securitewebback.incident.dto.IncidentSyndicDto;
import com.example.securitewebback.incident.entity.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.securitewebback.user.repository.ProprietaireRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.incident.dto.CreateIncidentDto;
import com.example.securitewebback.incident.dto.IncidentDto;
import com.example.securitewebback.incident.service.IncidentService;
import com.example.securitewebback.security.CustomUserDetails;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final ProprietaireRepository proprietaireRepository;

    public IncidentController(IncidentService incidentService, ProprietaireRepository proprietaireRepository) {
        this.incidentService = incidentService;
        this.proprietaireRepository = proprietaireRepository;
    }

    @PostMapping
    public ResponseEntity<IncidentDto> createIncident(
            @Valid @RequestBody CreateIncidentDto dto,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Récupérer l'utilisateur Proprietaire depuis la base
        Proprietaire reporter = proprietaireRepository.findById(userDetails.getUuid())
                .orElseThrow(() -> new RuntimeException("Propriétaire non trouvé"));

        // Créer l'incident via le service
        IncidentDto result = incidentService.createIncident(dto, reporter);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<Page<IncidentSyndicDto>> getIncidentsForSyndic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy, // "urgent", "recent", "building", "owner"
            @RequestParam(required = false) IncidentStatus status, // PENDING, IGNORED, VOTED
            Authentication auth) {

        // Récupérer l'ID du syndic connecté
        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();

        // Créer la pagination
        Pageable pageable = PageRequest.of(page, limit);

        // Appel au service (qui devra gérer le tri complexe via Specification ou Query)
        Page<IncidentSyndicDto> incidents = incidentService.getIncidentsForSyndic(
                syndicId,
                pageable,
                sortBy,
                status
        );

        return ResponseEntity.ok(incidents);
    }

    // 3. ACTION : IGNORER (Pour le Syndic)
    @PatchMapping("/{id}/ignore")
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<Void> ignoreIncident(
            @PathVariable UUID id,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        incidentService.ignoreIncident(id, syndicId);

        return ResponseEntity.ok().build();
    }

    // 4. ACTION : VOTER (Pour le Syndic)
    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<Void> createVoteForIncident(
            @PathVariable UUID id,
            @Valid @RequestBody CreateVoteDto voteDto,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        incidentService.createVote(id, voteDto, syndicId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
