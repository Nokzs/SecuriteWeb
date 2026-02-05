package com.example.securitewebback.guard;

import org.springframework.stereotype.Component;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.securitewebback.security.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.building.repository.BuildingRepository;

@Component("apartmentSecurity")
@Slf4j
public class ApartmentSecurity {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    public boolean canAccess(UUID apartmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return false;
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElse(null);
        if (apartment == null)
            return false;
        log.info("User Role: " + user.getRole());
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
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return false;
        }

        UUID connectedUserId = userDetails.getUuid();
        Role role = userDetails.getUser().getRole();

        return buildingRepository.findById(buildingId).map(building -> {
            if (role != Role.SYNDIC) {
                return false;
            }

            if (building.getSyndic() == null)
                return false;
            return building.getSyndic().getId().equals(connectedUserId);
        }).orElse(false);
    }
}
