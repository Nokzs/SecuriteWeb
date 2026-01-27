package com.example.securitewebback.user.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.user.dto.SyndicPublicDto;
import com.example.securitewebback.user.dto.ContactSyndicDto;
import com.example.securitewebback.user.entity.ContactMessage;
import com.example.securitewebback.user.repository.ContactMessageRepository;
import com.example.securitewebback.user.repository.SyndicRepository;

@Service
public class SyndicService {

    private final SyndicRepository syndicRepository;
    private final ContactMessageRepository contactMessageRepository;

    public SyndicService(SyndicRepository syndicRepository, ContactMessageRepository contactMessageRepository) {
        this.syndicRepository = syndicRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    public List<SyndicPublicDto> getAllSyndics() {
        List<Syndic> syndics = syndicRepository.findAll();
        return syndics.stream().map(this::toPublicDto).collect(Collectors.toList());
    }

    private SyndicPublicDto toPublicDto(Syndic s) {
        // Syndic entity doesn't have firstName/lastName: expose agence name in lastName
        String firstName = "";
        String lastName = s.getNomAgence() != null ? s.getNomAgence() : "";
        return new SyndicPublicDto(
                s.getId() != null ? s.getId().toString() : null,
                firstName,
                lastName,
                s.getEmail(),
                s.getTelephone(),
                s.getNomAgence(),
                s.getAdresse());
    }

    public ContactMessage contactSyndic(UUID syndicId, ContactSyndicDto dto) {
        Syndic syndic = syndicRepository.findById(syndicId)
                .orElseThrow(() -> new RuntimeException("Syndic non trouvé"));

        ContactMessage msg = new ContactMessage();
        msg.setSyndic(syndic);
        msg.setSenderFirstName(dto.firstName());
        msg.setSenderLastName(dto.lastName());
        msg.setSenderPhone(dto.phone());
        msg.setMessageContent(dto.message());

        return contactMessageRepository.save(msg);
    }
}

