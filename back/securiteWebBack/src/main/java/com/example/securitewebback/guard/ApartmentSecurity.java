package com.example.securitewebback.guard;

import com.example.securitewebback.auth.entity.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.example.securitewebback.security.CustomUserDetails;
import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;

@Component("apartmentSecurity")
public class ApartmentSecurity {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    public boolean canAccess(UUID apartmentId, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElse(null);
        if (apartment == null)
            return false;

        if (user.getRole().equals("ROLE_SYNDIC")) {
            return apartment.getBuilding().getSyndic().getId().equals(user.getUuid());
        }

        if (user.getRole().equals("ROLE_PROPRIETAIRE")) {
            return apartment.getOwner() != null &&
                    apartment.getOwner().getId().equals(user.getUuid());
        }

        return false;
    }

    public boolean canAccessToBuilding(UUID buildingId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            return false;
        }
        Building building = buildingRepository.findById(buildingId).orElse(null);

        if (building == null)
            return false;

        System.out.println("User ID: " + user.getUuid());
        System.out.println("User Role: " + user.getRole());
        System.out.println("Building Syndic ID: " + building.getSyndic().getId());

        // Vérification propre avec l'Enum (si getRole renvoie un Enum)
        String userRole = user.getRole();
        if (userRole.equals("SYNDIC") || userRole.equals("ROLE_SYNDIC")) {
            // Logique pour le Syndic...
            return building.getSyndic().getId().equals(user.getUuid());
        }

        if (userRole.equals("PROPRIETAIRE") || userRole.equals("ROLE_PROPRIETAIRE")) {
            // Les proprios n'ont pas accès à la vue globale de l'immeuble pour l'instant
            return false;
        }

        return false;
    }

    public boolean canUpdateApartment(UUID apartmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails user)) {
            return false;
        }
        Optional<Apartment> apartment = apartmentRepository.findById(apartmentId);
        Building building = buildingRepository.findById(apartment.get().getBuilding().getId()).orElse(null);

        if (building == null)
            return false;

        String userRole = user.getRole();
        if (userRole.equals("SYNDIC") || userRole.equals("ROLE_SYNDIC")) {
            return building.getSyndic().getId().equals(user.getUuid());
        }

        if (userRole.equals("PROPRIETAIRE") || userRole.equals("ROLE_PROPRIETAIRE")) {
            return false;
        }

        return false;
    }
}
