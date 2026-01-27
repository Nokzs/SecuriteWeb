package com.example.securitewebback.user.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.user.dto.SyndicPublicDto;
import com.example.securitewebback.user.dto.ContactSyndicDto;
import com.example.securitewebback.user.entity.ContactMessage;
import com.example.securitewebback.user.service.SyndicService;

@RestController
@RequestMapping("/api/syndics")
public class SyndicController {

    private final SyndicService syndicService;

    public SyndicController(SyndicService syndicService) {
        this.syndicService = syndicService;
    }

    @GetMapping
    public ResponseEntity<List<SyndicPublicDto>> listSyndics() {
        List<SyndicPublicDto> list = syndicService.getAllSyndics();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/contact")
    public ResponseEntity<ContactMessage> contactSyndic(@PathVariable("id") UUID id,
            @Valid @RequestBody ContactSyndicDto dto) {
        ContactMessage saved = syndicService.contactSyndic(id, dto);
        return ResponseEntity.ok(saved);
    }
}
