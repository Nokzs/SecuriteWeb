package com.example.securitewebback.guard;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.securitewebback.auth.entity.User;
import com.example.securitewebback.security.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

@Component("InvoiceSecurity")
@Slf4j
public class InvoiceSecurity {
    public boolean isOwner(Authentication authentication, Long invoiceId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        // Log the user and invoiceId for debugging
        log.info("Checking ownership for user: {} on invoiceId: {}", user.getEmail(), invoiceId);
        // Implement logic to check if the user owns the invoice with the given ID
        // This is a placeholder implementation; replace with actual logic
        boolean isOwner = checkInvoiceOwnership(user, invoiceId);
        // Log the result of the ownership check
        log.info("Is user {} owner of invoice {}: {}", user.getEmail(), invoiceId, isOwner);
        return isOwner;
    }

    private boolean checkInvoiceOwnership(User user, Long invoiceId) {
        // Placeholder logic; replace with actual database check
        // For example, query the database to see if the invoice belongs to the user's
        // apartments
        return true; // or false based on actual ownership
    }
}
