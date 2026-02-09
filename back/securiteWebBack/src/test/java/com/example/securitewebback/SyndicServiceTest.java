package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.user.service.SyndicService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.user.dto.ContactMessageDto;
import com.example.securitewebback.user.dto.ContactSyndicDto;
import com.example.securitewebback.user.dto.SyndicPublicDto;
import com.example.securitewebback.user.entity.ContactMessage;
import com.example.securitewebback.user.repository.ContactMessageRepository;
import com.example.securitewebback.user.repository.SyndicRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class SyndicServiceTest {

    @Mock
    private SyndicRepository syndicRepository;

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private SyndicService syndicService;

    @Test
    @DisplayName("getSyndics : Retourne une page de DTOs publics")
    void getSyndics_Success() {
        // Arrange
        Syndic syndic = new Syndic();
        syndic.setId(UUID.randomUUID());
        syndic.setNomAgence("Agence Test");
        syndic.setEmail("test@agence.com");

        Page<Syndic> page = new PageImpl<>(List.of(syndic));
        Pageable pageable = Pageable.unpaged();

        when(syndicRepository.findByNomAgenceContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContaining(
                anyString(), anyString(), anyString(), eq(pageable))).thenReturn(page);

        // Act
        Page<SyndicPublicDto> result = syndicService.getSyndics(pageable, "test");

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).lastName()).isEqualTo("Agence Test");
    }

    @Test
    @DisplayName("getMessagesForSyndic : Filtre correctement (unread)")
    void getMessagesForSyndic_Unread() {
        UUID syndicId = UUID.randomUUID();
        ContactMessage msg = new ContactMessage();
        msg.setRead(false);
        Page<ContactMessage> page = new PageImpl<>(List.of(msg));
        Pageable pageable = Pageable.unpaged();

        when(contactMessageRepository.findBySyndicIdAndReadFalse(syndicId, pageable)).thenReturn(page);

        Page<ContactMessageDto> result = syndicService.getMessagesForSyndic(syndicId, pageable, "unread");

        assertThat(result.getContent()).hasSize(1);
        verify(contactMessageRepository).findBySyndicIdAndReadFalse(syndicId, pageable);
    }

    @Test
    @DisplayName("contactSyndic : Crée et sauvegarde le message")
    void contactSyndic_Success() {
        UUID syndicId = UUID.randomUUID();
        Syndic syndic = new Syndic();
        syndic.setId(syndicId);

        ContactSyndicDto dto = new ContactSyndicDto("Jean", "Dupont", "0606060606", "Bonjour", "jean@test.com");

        when(syndicRepository.findById(syndicId)).thenReturn(Optional.of(syndic));
        when(contactMessageRepository.save(any(ContactMessage.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        ContactMessage result = syndicService.contactSyndic(syndicId, dto);

        // Assert
        assertThat(result.getSyndic()).isEqualTo(syndic);
        assertThat(result.getSenderEmail()).isEqualTo("jean@test.com");
        assertThat(result.getMessageContent()).isEqualTo("Bonjour");
    }

    @Test
    @DisplayName("markMessageAsRead : Passe le message en lu")
    void markMessageAsRead_Success() {
        UUID syndicId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        ContactMessage msg = new ContactMessage();
        msg.setRead(false);

        when(contactMessageRepository.findByIdAndSyndicId(msgId, syndicId)).thenReturn(Optional.of(msg));

        syndicService.markMessageAsRead(syndicId, msgId);

        assertThat(msg.isRead()).isTrue();
        verify(contactMessageRepository).save(msg);
    }

    @Test
    @DisplayName("archiveMessage : Passe le message en archivé")
    void archiveMessage_Success() {
        UUID syndicId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();
        ContactMessage msg = new ContactMessage();
        msg.setArchived(false);

        when(contactMessageRepository.findByIdAndSyndicId(msgId, syndicId)).thenReturn(Optional.of(msg));

        syndicService.archiveMessage(syndicId, msgId);

        assertThat(msg.isArchived()).isTrue();
        verify(contactMessageRepository).save(msg);
    }

    @Test
    @DisplayName("markMessageAsRead : Exception si message non trouvé")
    void markMessageAsRead_NotFound() {
        UUID syndicId = UUID.randomUUID();
        UUID msgId = UUID.randomUUID();

        when(contactMessageRepository.findByIdAndSyndicId(msgId, syndicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> syndicService.markMessageAsRead(syndicId, msgId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}