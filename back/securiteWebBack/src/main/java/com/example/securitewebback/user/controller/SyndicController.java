package com.example.securitewebback.user.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.security.CustomUserDetails;
import com.example.securitewebback.user.dto.SyndicPublicDto;
import com.example.securitewebback.user.dto.ContactSyndicDto;
import com.example.securitewebback.user.dto.ContactMessageDto;
import com.example.securitewebback.user.entity.ContactMessage;
import com.example.securitewebback.user.service.SyndicService;

@RestController
@RequestMapping({"/api/syndics", "/syndics"})
public class SyndicController {

    private final SyndicService syndicService;

    public SyndicController(SyndicService syndicService) {
        this.syndicService = syndicService;
    }

    @GetMapping
    public ResponseEntity<Page<SyndicPublicDto>> listSyndics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, limit);
        Page<SyndicPublicDto> result = syndicService.getSyndics(pageable, search);
        return ResponseEntity.ok(result);
    }

    // Endpoint sécurisé pour récupérer les messages du syndic connecté
    @GetMapping("/messages")
    public ResponseEntity<Page<ContactMessageDto>> getMyMessages(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String type) {

        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        Pageable pageable = PageRequest.of(page, limit);
        Page<ContactMessageDto> messages = syndicService.getMessagesForSyndic(syndicId, pageable, type);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<ContactMessageDto> contactSyndic(@PathVariable("id") UUID id,
            @Valid @RequestBody ContactSyndicDto dto) {
        ContactMessage saved = syndicService.contactSyndic(id, dto);
        ContactMessageDto dtoResponse = ContactMessageDto.fromEntity(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoResponse);
    }

    @PatchMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markAsRead(Authentication auth, @PathVariable("messageId") UUID messageId) {
        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        syndicService.markMessageAsRead(syndicId, messageId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/messages/{messageId}/archive")
    public ResponseEntity<Void> archiveMessage(Authentication auth, @PathVariable("messageId") UUID messageId) {
        UUID syndicId = ((CustomUserDetails) auth.getPrincipal()).getUuid();
        syndicService.archiveMessage(syndicId, messageId);
        return ResponseEntity.ok().build();
    }
}
