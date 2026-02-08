package com.example.securitewebback.incident.controller;

import java.util.List;
import java.util.UUID;

import com.example.securitewebback.incident.dto.*;
import com.example.securitewebback.user.repository.ProprietaireRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.incident.entity.IncidentStatus;
import com.example.securitewebback.incident.service.IncidentService;
import com.example.securitewebback.security.CustomUserDetails;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final ProprietaireRepository proprietaireRepository;

    public IncidentController(IncidentService incidentService, ProprietaireRepository proprietaireRepository) {
        this.incidentService = incidentService;
        this.proprietaireRepository = proprietaireRepository;
    }

    // --- PROPRIETAIRE ---

    @PostMapping
    @PreAuthorize("hasRole('PROPRIETAIRE') and @incidentSecurity.canCreateIncident(#dto.apartmentId(), authentication)")
    public ResponseEntity<IncidentDto> createIncident(
            @Valid @RequestBody CreateIncidentDto dto,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Proprietaire reporter = proprietaireRepository.findById(userDetails.getUuid())
                .orElseThrow(() -> new RuntimeException("Propriétaire non trouvé"));

        IncidentDto result = incidentService.createIncident(dto, reporter);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/votes/{id}/cast")
    @PreAuthorize("hasRole('PROPRIETAIRE') and @voteSecurity.canVote(#id, authentication)")    public ResponseEntity<Void> castVote(
            @PathVariable UUID id, // L'ID du Vote (pas de l'incident)
            @Valid @RequestBody CastVoteDto castVoteDto,
            Authentication auth) {

        UUID voterId = ((CustomUserDetails) auth.getPrincipal()).getUuid();

        incidentService.castVote(id, castVoteDto.choice(), voterId);

        return ResponseEntity.ok().build();
    }

    // Dans IncidentController

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROPRIETAIRE')")
    public ResponseEntity<List<OwnerIncidentDto>> getMyIncidents(Authentication auth) {
        UUID ownerId = ((CustomUserDetails) auth.getPrincipal()).getUuid();

        List<OwnerIncidentDto> incidents = incidentService.getIncidentsForOwner(ownerId);

        return ResponseEntity.ok(incidents);
    }

    // --- SYNDIC ---

    @GetMapping
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<Page<IncidentSyndicDto>> getIncidentsForSyndic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) IncidentStatus status,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        Pageable pageable = PageRequest.of(page, limit);

        Page<IncidentSyndicDto> incidents = incidentService.getIncidentsForSyndic(
                syndicId,
                pageable,
                sortBy,
                status
        );

        return ResponseEntity.ok(incidents);
    }

    @PatchMapping("/{id}/ignore")
    @PreAuthorize("hasRole('SYNDIC') and @incidentSecurity.canManageIncident(#id, authentication)")    public ResponseEntity<Void> ignoreIncident(
            @PathVariable UUID id,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        incidentService.ignoreIncident(id, syndicId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/votes")
    @PreAuthorize("hasRole('SYNDIC')")
    public ResponseEntity<List<VoteSummaryDto>> getSyndicVotes(Authentication auth) {
        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();

        List<VoteSummaryDto> votes = incidentService.getVotesForSyndic(syndicId);

        return ResponseEntity.ok(votes);
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasRole('SYNDIC') and @incidentSecurity.canManageIncident(#id, authentication)")
    public ResponseEntity<Void> createVoteForIncident(
            @PathVariable UUID id,
            @Valid @RequestBody CreateVoteDto voteDto,
            Authentication auth) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        incidentService.createVote(id, voteDto, syndicId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/votes/{id}/close")
    @PreAuthorize("hasRole('SYNDIC') and @voteSecurity.canManageVote(#id, authentication)")    public ResponseEntity<Void> closeVote(@PathVariable UUID id) {
        incidentService.closeVoteManually(id);
        return ResponseEntity.ok().build();
    }
}