package com.example.securitewebback.user.service;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.user.dto.SyndicPublicDto;
import com.example.securitewebback.user.dto.ContactSyndicDto;
import com.example.securitewebback.user.dto.ContactMessageDto;
import com.example.securitewebback.user.entity.ContactMessage;
import com.example.securitewebback.user.repository.ContactMessageRepository;
import com.example.securitewebback.user.repository.SyndicRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SyndicService {

    private final SyndicRepository syndicRepository;
    private final ContactMessageRepository contactMessageRepository;

    public SyndicService(SyndicRepository syndicRepository, ContactMessageRepository contactMessageRepository) {
        this.syndicRepository = syndicRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    // Nouvelle méthode paginée utilisée par le front
    public Page<SyndicPublicDto> getSyndics(Pageable pageable, String search) {
        String q = (search == null) ? "" : search;
        Page<Syndic> page = syndicRepository.findByNomAgenceContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContaining(q, q, q, pageable);
        return page.map(this::toPublicDto);
    }

    // Récupère les messages du syndic (depuis son UUID) en page de DTOs
    public Page<ContactMessageDto> getMessagesForSyndic(UUID syndicId, Pageable pageable, String type) {
        Page<ContactMessage> page;
        String t = type == null ? "all" : type;
        switch (t) {
            case "unread":
                page = contactMessageRepository.findBySyndicIdAndReadFalse(syndicId, pageable);
                break;
            case "archived":
                page = contactMessageRepository.findBySyndicIdAndArchivedTrue(syndicId, pageable);
                break;
            default:
                page = contactMessageRepository.findBySyndicId(syndicId, pageable);
                break;
        }
        return page.map(ContactMessageDto::fromEntity);
    }

    private SyndicPublicDto toPublicDto(Syndic s) {
        // Syndic entity doesn't have firstName/lastName: expose agence name in lastName
        String firstName = "";
        String lastName = s.getNomAgence() != null ? s.getNomAgence() : "";
        Integer buildingsCount = (s.getBuildings() != null) ? s.getBuildings().size() : 0;
        return new SyndicPublicDto(
                s.getId() != null ? s.getId().toString() : null,
                firstName,
                lastName,
                s.getEmail(),
                s.getTelephone(),
                s.getAdresse(),
                buildingsCount);
    }

    public ContactMessage contactSyndic(UUID syndicId, ContactSyndicDto dto) {
        Syndic syndic = syndicRepository.findById(syndicId)
                .orElseThrow(() -> new RuntimeException("Syndic non trouvé"));

        ContactMessage msg = new ContactMessage();
        msg.setSyndic(syndic);
        msg.setSenderFirstName(dto.firstName());
        msg.setSenderLastName(dto.lastName());
        msg.setSenderPhone(dto.phone());
        msg.setSenderEmail(dto.email());
        msg.setMessageContent(dto.message());

        return contactMessageRepository.save(msg);
    }

    @Transactional
    public void markMessageAsRead(UUID syndicId, UUID messageId) {
        ContactMessage msg = contactMessageRepository.findByIdAndSyndicId(messageId, syndicId)
                .orElseThrow(() -> new EntityNotFoundException("Message non trouvé ou non autorisé"));
        if (!msg.isRead()) {
            msg.setRead(true);
            contactMessageRepository.save(msg);
        }
    }

    @Transactional
    public void archiveMessage(UUID syndicId, UUID messageId) {
        ContactMessage msg = contactMessageRepository.findByIdAndSyndicId(messageId, syndicId)
                .orElseThrow(() -> new EntityNotFoundException("Message non trouvé ou non autorisé"));
        if (!msg.isArchived()) {
            msg.setArchived(true);
            contactMessageRepository.save(msg);
        }
    }

}
