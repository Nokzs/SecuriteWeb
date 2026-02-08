package com.example.securitewebback.admin.service;

import com.example.securitewebback.admin.dto.AdminStatsDto;
import com.example.securitewebback.admin.dto.UserSummaryDto;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.incident.entity.Incident;
import com.example.securitewebback.incident.entity.Vote;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.incident.repository.VoteRepository;
import com.example.securitewebback.incident.repository.VoteSubmissionRepository;
import com.example.securitewebback.user.repository.ContactMessageRepository;
import com.example.securitewebback.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    private final BuildingRepository buildingRepository;

    private final ApartmentRepository apartmentRepository;

    private final VoteRepository voteRepository;

    private final VoteSubmissionRepository voteSubmissionRepository;

    private final IncidentRepository incidentRepository;

    private final ContactMessageRepository contactMessageRepository;

    public AdminStatsDto getStats() {
        long totalUsers = userRepository.count();
        long totalBuildings = buildingRepository.count();

        // Si tu as les repositories, utilise .count(), sinon mets 0L
        long totalIncidents = 0; // incidentRepository.count();
        long activeVotes = 0;    // voteRepository.countActiveVotes(); ou voteRepository.count();

        return new AdminStatsDto(
                totalUsers,
                totalBuildings,
                totalIncidents,
                activeVotes
        );
    }

    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserSummaryDto::convertToDto) // On délègue la transformation à une méthode privée
                .toList();
    }

    @Transactional // Transactionnel est vital ici !
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // =========================================================
        // 1. NETTOYAGE SYNDIC
        // =========================================================
        if (user instanceof Syndic syndic) {

            // A. DÉTACHER LES IMMEUBLES (On ne veut pas supprimer les immeubles !)
            List<Building> buildings = buildingRepository.findBySyndicId(syndic.getId());
            for (Building b : buildings) {
                b.setSyndic(null); // L'immeuble n'a plus de gestionnaire pour l'instant
                buildingRepository.save(b);
            }

            // B. SUPPRIMER LES VOTES CRÉÉS PAR CE SYNDIC
            // (Comme 'createdBy' est obligatoire dans Vote, on doit supprimer le Vote)
            List<Vote> votesCreated = voteRepository.findByCreatedById(syndic.getId());

            for (Vote vote : votesCreated) {
                // 1. D'abord, on supprime les bulletins de participation des proprios à ce vote
                // (Sinon contrainte FK sur vote_proprietaire)
                voteSubmissionRepository.deleteByVoteId(vote.getId());

                // 2. Ensuite, on peut supprimer le vote lui-même
                voteRepository.delete(vote);

            }

            // Cela supprime les lignes dans 'contact_message' qui bloquaient la suppression
            contactMessageRepository.deleteBySyndicId(syndic.getId());
        }

        // =========================================================
        // 2. NETTOYAGE PROPRIETAIRE
        // =========================================================
        if (user instanceof Proprietaire proprietaire) {

            // A. Détacher les appartements
            List<Apartment> apartments = apartmentRepository.findByOwnerId(proprietaire.getId());
            for (Apartment a : apartments) {
                a.setOwner(null);
                apartmentRepository.save(a);
            }

            userRepository.deleteProprioAppartRelation(proprietaire.getId());

            // B. Supprimer SES votes (bulletins qu'il a déposés)
            voteSubmissionRepository.deleteByVoterId(proprietaire.getId());

            // C. SUPPRIMER LES INCIDENTS QU'IL A SIGNALÉS (ET TOUT CE QUI EN DÉCOULE)
            List<Incident> incidents = incidentRepository.findByReporterId(proprietaire.getId());

            for (Incident incident : incidents) {
                // 1. D'abord, on cherche s'il y a un VOTE lié à cet incident
                List<Vote> votesLies = voteRepository.findByIncidentId(incident.getId());

                for (Vote vote : votesLies) {
                    // 2. Si oui, on supprime d'abord les bulletins de participation à ce vote
                    voteSubmissionRepository.deleteByVoteId(vote.getId());

                    // 3. Ensuite, on supprime le vote
                    voteRepository.delete(vote);
                }

                // 4. Enfin, l'incident est libre, on le supprime (les photos partiront toutes seules avec Cascade)
                incidentRepository.delete(incident);
            }
        }

        // =========================================================
        // 3. SUPPRESSION FINALE
        // =========================================================
        userRepository.delete(user);
    }

}