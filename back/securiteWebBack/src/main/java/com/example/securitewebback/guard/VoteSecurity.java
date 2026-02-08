package com.example.securitewebback.guard;

import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.incident.entity.Vote;
import com.example.securitewebback.incident.repository.VoteRepository;
import com.example.securitewebback.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("voteSecurity")
public class VoteSecurity {

    @Autowired
    private VoteRepository voteRepository;
    @Autowired private ApartmentRepository apartmentRepository; // Pour vérifier si le votant habite là

    // Pour PROPRIETAIRE (Cast Vote) : Vérifie qu'il a un appart dans l'immeuble du vote
    public boolean canVote(UUID voteId, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Vote vote = voteRepository.findById(voteId).orElse(null);
        if (vote == null) return false;

        UUID buildingIdDuVote = vote.getIncident().getApartment().getBuilding().getId();

        // On vérifie si l'utilisateur possède AU MOINS un appartement dans cet immeuble
        return apartmentRepository.existsByOwnerIdAndBuildingId(user.getUuid(), buildingIdDuVote);
    }

    // Pour SYNDIC (Close Vote) : Vérifie qu'il a créé le vote
    public boolean canManageVote(UUID voteId, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return voteRepository.findById(voteId)
                .map(v -> v.getCreatedBy().getId().equals(user.getUuid()))
                .orElse(false);
    }
}
