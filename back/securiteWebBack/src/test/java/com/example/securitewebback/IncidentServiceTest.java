package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.incident.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.incident.dto.CreateIncidentDto;
import com.example.securitewebback.incident.dto.CreateVoteDto;
import com.example.securitewebback.incident.dto.IncidentDto;
import com.example.securitewebback.incident.dto.OwnerIncidentDto;
import com.example.securitewebback.incident.dto.VoteSummaryDto;
import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.incident.entity.IncidentPhoto;
import com.example.securitewebback.incident.entity.IncidentStatus;
import com.example.securitewebback.incident.entity.Vote;
import com.example.securitewebback.incident.entity.VoteChoice;
import com.example.securitewebback.incident.entity.VoteStatus;
import com.example.securitewebback.incident.entity.VoteSubmission;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.incident.repository.VoteRepository;
import com.example.securitewebback.incident.repository.VoteSubmissionRepository;
import com.example.securitewebback.storage.MinioService;
import com.example.securitewebback.user.repository.ProprietaireRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock private IncidentRepository incidentRepository;
    @Mock private ApartmentRepository apartmentRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private VoteSubmissionRepository voteSubmissionRepository;
    @Mock private ProprietaireRepository proprietaireRepository;
    @Mock private MinioService minioService;

    @InjectMocks
    private IncidentService incidentService;

    private Building building;
    private Apartment apartment;
    private Proprietaire proprietaire;
    private Syndic syndic;

    @BeforeEach
    void setUp() {
        syndic = new Syndic();
        syndic.setId(UUID.randomUUID());

        building = new Building();
        building.setId(UUID.randomUUID());
        building.setSyndic(syndic);
        building.setTotalTantieme(1000);

        apartment = new Apartment();
        apartment.setId(UUID.randomUUID());
        apartment.setBuilding(building);

        proprietaire = new Proprietaire();
        proprietaire.setId(UUID.randomUUID());
        proprietaire.setEmail("proprio@test.com");
    }

    // --- Tests Create Incident ---

    @Test
    @DisplayName("CreateIncident : Succès avec photo")
    void createIncident_Success() throws Exception {
        CreateIncidentDto dto = new CreateIncidentDto(
                "Fuite", "Gros dégâts", true, apartment.getId(), List.of("photo.jpg")
        );

        when(apartmentRepository.findById(apartment.getId())).thenReturn(Optional.of(apartment));
        // Simulation de la sauvegarde : on retourne l'objet modifié (avec ID généré)
        when(incidentRepository.save(any(Incident.class))).thenAnswer(inv -> {
            Incident inc = inv.getArgument(0);
            if (inc.getId() == null) inc.setId(UUID.randomUUID());
            return inc;
        });

        when(minioService.generatePresignedUploadUrl(anyString(), anyString())).thenReturn("http://minio/upload");

        IncidentDto result = incidentService.createIncident(dto, proprietaire);

        assertThat(result.title).isEqualTo("Fuite");
        assertThat(result.photoUrls).hasSize(1);
        assertThat(result.photoUrls.get(0)).isEqualTo("http://minio/upload");

        verify(incidentRepository, times(2)).save(any(Incident.class)); // 1 fois init, 1 fois avec photos
    }

    // --- Tests Cast Vote ---

    @Test
    @DisplayName("CastVote : Succès si le propriétaire a des tantièmes")
    void castVote_Success() {
        UUID voteId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setApartment(apartment); // L'appart lie au building

        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setStatus(VoteStatus.ONGOING);
        vote.setEndDate(LocalDateTime.now().plusDays(1));
        vote.setIncident(incident);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
        when(voteSubmissionRepository.existsByVoteIdAndVoterId(voteId, proprietaire.getId())).thenReturn(false);
        // Le propriétaire possède 150 tantièmes dans cet immeuble
        when(apartmentRepository.sumTantiemesByOwnerAndBuilding(proprietaire.getId(), building.getId())).thenReturn(150);
        when(proprietaireRepository.getReferenceById(proprietaire.getId())).thenReturn(proprietaire);

        incidentService.castVote(voteId, VoteChoice.FOR, proprietaire.getId());

        verify(voteSubmissionRepository).save(any(VoteSubmission.class));
    }

    @Test
    @DisplayName("CastVote : Échec si le propriétaire n'a pas de lots dans l'immeuble")
    void castVote_NoTantiemes_ThrowsException() {
        UUID voteId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setApartment(apartment);

        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setStatus(VoteStatus.ONGOING);
        vote.setEndDate(LocalDateTime.now().plusDays(1));
        vote.setIncident(incident);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
        when(voteSubmissionRepository.existsByVoteIdAndVoterId(voteId, proprietaire.getId())).thenReturn(false);
        when(apartmentRepository.sumTantiemesByOwnerAndBuilding(proprietaire.getId(), building.getId())).thenReturn(0);

        assertThatThrownBy(() -> incidentService.castVote(voteId, VoteChoice.AGAINST, proprietaire.getId()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Vous ne possédez pas de lots");

        verify(voteSubmissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("CastVote : Échec si le vote est clos")
    void castVote_Closed_ThrowsException() {
        UUID voteId = UUID.randomUUID();
        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setStatus(VoteStatus.CLOSED); // Clos

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));

        assertThatThrownBy(() -> incidentService.castVote(voteId, VoteChoice.FOR, proprietaire.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ce vote est clos");
    }

    // --- Tests Get Incidents For Owner ---

    @Test
    @DisplayName("GetIncidentsForOwner : Retourne les incidents des immeubles du propriétaire")
    void getIncidentsForOwner_Success() throws Exception {
        // Le propriétaire a cet appartement
        when(apartmentRepository.findByOwnerId(proprietaire.getId())).thenReturn(List.of(apartment));

        Incident incident = new Incident();
        incident.setId(UUID.randomUUID());
        incident.setTitle("Panne");
        incident.setStatus(IncidentStatus.VOTED);
        incident.setApartment(apartment);
        incident.setReporter(proprietaire);
        incident.setCreatedAt(Instant.now());

        // Incident trouvé dans l'immeuble
        when(incidentRepository.findAllByBuildingIds(List.of(building.getId()))).thenReturn(List.of(incident));

        // Un vote est en cours
        Vote vote = new Vote();
        vote.setId(UUID.randomUUID());
        vote.setAmount(500.0);
        vote.setEndDate(LocalDateTime.now().plusDays(2));

        when(voteRepository.findFirstByIncidentIdAndStatusOrderByCreatedAtDesc(incident.getId(), VoteStatus.ONGOING))
                .thenReturn(Optional.of(vote));

        when(voteSubmissionRepository.existsByVoteIdAndVoterId(vote.getId(), proprietaire.getId())).thenReturn(true);

        List<OwnerIncidentDto> results = incidentService.getIncidentsForOwner(proprietaire.getId());

        assertThat(results).hasSize(1);
        OwnerIncidentDto dto = results.get(0);
        assertThat(dto.title()).isEqualTo("Panne");
        assertThat(dto.status()).isEqualTo("VOTED");
        assertThat(dto.hasVoted()).isTrue();
        assertThat(dto.voteAmount()).isEqualTo(500.0);
    }

    // --- Tests Create Vote (Syndic) ---

    @Test
    @DisplayName("CreateVote : Succès si le syndic est bien celui de l'immeuble")
    void createVote_Success() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setId(incidentId);
        incident.setApartment(apartment); // Linked to building -> syndic
        incident.setStatus(IncidentStatus.PENDING);

        CreateVoteDto voteDto = new CreateVoteDto(1000.0, LocalDateTime.now().plusDays(7));

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));

        incidentService.createVote(incidentId, voteDto, syndic.getId());

        verify(voteRepository).save(any(Vote.class));
        verify(incidentRepository).save(incident);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.VOTED);
    }

    @Test
    @DisplayName("CreateVote : Échec si un autre syndic essaie")
    void createVote_WrongSyndic_ThrowsSecurityException() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setApartment(apartment);

        UUID otherSyndicId = UUID.randomUUID();
        CreateVoteDto voteDto = new CreateVoteDto(1000.0, LocalDateTime.now().plusDays(7));

        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> incidentService.createVote(incidentId, voteDto, otherSyndicId))
                .isInstanceOf(SecurityException.class);
    }

    // --- Tests Close Vote Manually ---

    @Test
    @DisplayName("CloseVote : Vote accepté (PASSED) -> Incident IN_PROGRESS")
    void closeVote_Passed() {
        UUID voteId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.VOTED);

        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setStatus(VoteStatus.ONGOING);
        vote.setIncident(incident);

        // Simulation : 600 Pour, 400 Contre
        VoteSubmission sub1 = new VoteSubmission(); sub1.setChoice(VoteChoice.FOR); sub1.setTantiemesWeight(600);
        VoteSubmission sub2 = new VoteSubmission(); sub2.setChoice(VoteChoice.AGAINST); sub2.setTantiemesWeight(400);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
        when(voteSubmissionRepository.findByVoteId(voteId)).thenReturn(List.of(sub1, sub2));

        incidentService.closeVoteManually(voteId);

        assertThat(vote.getStatus()).isEqualTo(VoteStatus.PASSED);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.IN_PROGRESS);
        verify(voteRepository).save(vote);
    }

    @Test
    @DisplayName("CloseVote : Vote rejeté (REJECTED) -> Incident RESOLVED")
    void closeVote_Rejected() {
        UUID voteId = UUID.randomUUID();
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.VOTED);

        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setStatus(VoteStatus.ONGOING);
        vote.setIncident(incident);

        // Simulation : 200 Pour, 500 Contre
        VoteSubmission sub1 = new VoteSubmission(); sub1.setChoice(VoteChoice.FOR); sub1.setTantiemesWeight(200);
        VoteSubmission sub2 = new VoteSubmission(); sub2.setChoice(VoteChoice.AGAINST); sub2.setTantiemesWeight(500);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
        when(voteSubmissionRepository.findByVoteId(voteId)).thenReturn(List.of(sub1, sub2));

        incidentService.closeVoteManually(voteId);

        assertThat(vote.getStatus()).isEqualTo(VoteStatus.REJECTED);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
    }

    @Test
    @DisplayName("GetVotesForSyndic : Calcule correctement les statistiques")
    void getVotesForSyndic_StatsCheck() {
        Vote vote = new Vote();
        vote.setId(UUID.randomUUID());
        vote.setStatus(VoteStatus.ONGOING);

        Incident incident = new Incident();
        incident.setTitle("Toiture");
        incident.setApartment(apartment); // totalTantieme = 1000 dans setup
        vote.setIncident(incident);

        when(voteRepository.findAllBySyndicId(syndic.getId())).thenReturn(List.of(vote));

        VoteSubmission sub1 = new VoteSubmission(); sub1.setChoice(VoteChoice.FOR); sub1.setTantiemesWeight(300);
        VoteSubmission sub2 = new VoteSubmission(); sub2.setChoice(VoteChoice.ABSTAIN); sub2.setTantiemesWeight(100);

        when(voteSubmissionRepository.findByVoteId(vote.getId())).thenReturn(List.of(sub1, sub2));

        List<VoteSummaryDto> summaries = incidentService.getVotesForSyndic(syndic.getId());

        assertThat(summaries).hasSize(1);
        VoteSummaryDto dto = summaries.get(0);

        assertThat(dto.totalBuildingTantiemes()).isEqualTo(1000);
        assertThat(dto.participationTantiemes()).isEqualTo(400); // 300 + 100
        assertThat(dto.tantiemesFor()).isEqualTo(300);
        assertThat(dto.tantiemesAgainst()).isEqualTo(0);
        assertThat(dto.tantiemesAbstain()).isEqualTo(100);
    }
}