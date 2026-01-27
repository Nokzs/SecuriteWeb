package com.example.securitewebback.guard;

import org.springframework.stereotype.Component;

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

    public boolean canAccessToBuilding(UUID buildingId, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Building building = buildingRepository.findById(buildingId).orElse(null);
        if (building == null)
            return false;

        if (user.getRole().equals("ROLE_SYNDIC")) {
            return building.getSyndic().getId().equals(user.getUuid());
        }

        if (user.getRole().equals("ROLE_PROPRIETAIRE")) {
            return false;
        }
        return false;
    }
}
