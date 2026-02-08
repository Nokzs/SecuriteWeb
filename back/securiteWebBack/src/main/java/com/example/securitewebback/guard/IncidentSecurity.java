package com.example.securitewebback.guard;

import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.incident.repository.IncidentRepository;
import com.example.securitewebback.security.CustomUserDetails;
import org.springframework.security.core.Authentication;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("incidentSecurity")

public class IncidentSecurity {

    @Autowired
    private IncidentRepository incidentRepository;
    @Autowired private ApartmentRepository apartmentRepository;

    // Pour CREATE INCIDENT : Vérifie que le proprio possède l'appart
    public boolean canCreateIncident(UUID apartmentId, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return apartmentRepository.findById(apartmentId)
                .map(a -> a.getOwner() != null && a.getOwner().getId().equals(user.getUuid()))
                .orElse(false);
    }

    // Pour SYNDIC (Ignore, Create Vote) : Vérifie qu'il gère l'immeuble de l'incident
    public boolean canManageIncident(UUID incidentId, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return incidentRepository.findById(incidentId)
                .map(i -> i.getApartment().getBuilding().getSyndic().getId().equals(user.getUuid()))
                .orElse(false);
    }
}