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
import com.example.securitewebback.building.entity.Building;
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
        if (authentication == null) return false;
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
    
        // 1. Check Auth
        if (auth == null) {
            System.out.println("❌ [SECURITY] Authentication is NULL");
            return false;
        }

        // 2. Check Principal
        if (!(auth.getPrincipal() instanceof CustomUserDetails user)) {
            System.out.println("❌ [SECURITY] Principal is NOT CustomUserDetails. Type is: " + auth.getPrincipal().getClass().getName());
            return false;
        }

        System.out.println("✅ [SECURITY] User: " + user.getUsername() + " | Role: " + user.getRole());

        // 3. Check Building & Logic
        return buildingRepository.findById(buildingId).map(building -> {
            UUID connectedUserId = user.getUuid();
            UUID buildingSyndicId = (building.getSyndic() != null) ? building.getSyndic().getId() : null;

            System.out.println("🔍 [SECURITY] Building Found: " + buildingId);
            System.out.println("🔍 [SECURITY] Connected User ID: " + connectedUserId);
            System.out.println("🔍 [SECURITY] Building Syndic ID: " + buildingSyndicId);

            if (!"ROLE_SYNDIC".equals(user.getRole())) {
                System.out.println("❌ [SECURITY] Access Denied: Role is not ROLE_SYNDIC");
                return false;
            }

            boolean hasAccess = connectedUserId.equals(buildingSyndicId);
            System.out.println(hasAccess ? "✅ [SECURITY] ACCESS GRANTED" : "❌ [SECURITY] ACCESS DENIED: ID Mismatch");

            return hasAccess;
        }).orElseGet(() -> {
            System.out.println("❌ [SECURITY] Building not found in DB: " + buildingId);
            return false;
        });
    }
}
