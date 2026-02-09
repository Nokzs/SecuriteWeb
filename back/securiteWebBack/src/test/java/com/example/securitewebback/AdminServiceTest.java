package com.example.securitewebback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.securitewebback.admin.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.securitewebback.admin.dto.AdminStatsDto;
import com.example.securitewebback.admin.dto.UserSummaryDto;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.incident.entity.Vote;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.incident.repository.VoteRepository;
import com.example.securitewebback.incident.repository.VoteSubmissionRepository;
import com.example.securitewebback.user.repository.ContactMessageRepository;
import com.example.securitewebback.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BuildingRepository buildingRepository;
    @Mock private ApartmentRepository apartmentRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private VoteSubmissionRepository voteSubmissionRepository;
    @Mock private IncidentRepository incidentRepository;
    @Mock private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("getStats : Retourne les comptes corrects")
    void getStats() {
        when(userRepository.count()).thenReturn(10L);
        when(buildingRepository.count()).thenReturn(5L);

        AdminStatsDto stats = adminService.getStats();

        assertThat(stats.totalUsers()).isEqualTo(10L);
        assertThat(stats.totalBuildings()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getAllUsers : Retourne la liste transformée en DTO")
    void getAllUsers() {
        Proprietaire p = new Proprietaire();
        p.setId(UUID.randomUUID());
        p.setEmail("p@test.com");
        p.setRole(Role.PROPRIETAIRE);
        p.setNom("Doe");
        p.setPrenom("John");

        Syndic s = new Syndic();
        s.setId(UUID.randomUUID());
        s.setEmail("s@test.com");
        s.setRole(Role.SYNDIC);
        s.setNomAgence("Agence Immo");

        when(userRepository.findAll()).thenReturn(List.of(p, s));

        List<UserSummaryDto> results = adminService.getAllUsers();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).details()).contains("Doe");
        assertThat(results.get(1).details()).contains("Agence Immo");
    }

    @Test
    @DisplayName("deleteUser (Syndic) : Détache immeubles, supprime votes créés et messages")
    void deleteUser_Syndic() {
        UUID syndicId = UUID.randomUUID();
        Syndic syndic = new Syndic();
        syndic.setId(syndicId);

        Building building = new Building();
        building.setSyndic(syndic);

        Vote vote = new Vote();
        vote.setId(UUID.randomUUID());

        // Mocks
        when(userRepository.findById(syndicId)).thenReturn(Optional.of(syndic));
        when(buildingRepository.findBySyndicId(syndicId)).thenReturn(List.of(building));
        when(voteRepository.findByCreatedById(syndicId)).thenReturn(List.of(vote));

        // Act
        adminService.deleteUser(syndicId);

        // Assert
        // 1. Vérifie le détachement des immeubles
        assertThat(building.getSyndic()).isNull();
        verify(buildingRepository).save(building);

        // 2. Vérifie la suppression des votes
        verify(voteSubmissionRepository).deleteByVoteId(vote.getId());
        verify(voteRepository).delete(vote);

        // 3. Vérifie suppression messages et user
        verify(contactMessageRepository).deleteBySyndicId(syndicId);
        verify(userRepository).delete(syndic);
    }

    @Test
    @DisplayName("deleteUser (Proprietaire) : Détache apparts, supprime votes soumis et incidents")
    void deleteUser_Proprietaire() {
        UUID ownerId = UUID.randomUUID();
        Proprietaire prop = new Proprietaire();
        prop.setId(ownerId);

        Apartment apt = new Apartment();
        apt.setOwner(prop);

        Incident incident = new Incident();
        incident.setId(UUID.randomUUID());

        Vote voteLieInc = new Vote();
        voteLieInc.setId(UUID.randomUUID());

        // Mocks
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(prop));
        when(apartmentRepository.findByOwnerId(ownerId)).thenReturn(List.of(apt));

        // Incident signalé par le proprio
        when(incidentRepository.findByReporterId(ownerId)).thenReturn(List.of(incident));
        // Vote lié à cet incident
        when(voteRepository.findByIncidentId(incident.getId())).thenReturn(List.of(voteLieInc));

        // Act
        adminService.deleteUser(ownerId);

        // Assert
        // 1. Détachement appartement
        assertThat(apt.getOwner()).isNull();
        verify(apartmentRepository).save(apt);
        verify(userRepository).deleteProprioAppartRelation(ownerId);

        // 2. Suppression des votes soumis par lui
        verify(voteSubmissionRepository).deleteByVoterId(ownerId);

        // 3. Suppression Incidents et ses dépendances
        verify(voteSubmissionRepository).deleteByVoteId(voteLieInc.getId()); // Bulletins du vote lié à l'incident
        verify(voteRepository).delete(voteLieInc); // Le vote lié à l'incident
        verify(incidentRepository).delete(incident); // L'incident lui-même

        // 4. Suppression User
        verify(userRepository).delete(prop);
    }
}