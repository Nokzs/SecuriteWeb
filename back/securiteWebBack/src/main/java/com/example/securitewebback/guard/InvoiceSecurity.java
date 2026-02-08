
package com.example.securitewebback.guard;

import org.springframework.stereotype.Component;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.securitewebback.security.CustomUserDetails;

import com.example.securitewebback.invoice.repository.InvoicesRepository;
import lombok.extern.slf4j.Slf4j;

import com.example.securitewebback.appartements.entity.Apartment;
import com.example.securitewebback.appartements.repository.ApartmentRepository;
import com.example.securitewebback.auth.entity.Role;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.invoice.entity.Invoice;
import java.util.Optional;

@Component("InvoiceSecurity")
@Slf4j
public class InvoiceSecurity {

    private final InvoicesRepository invoiceRepository;

    InvoiceSecurity(InvoicesRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public boolean canPay(UUID invoiceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return false;
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Optional<Invoice> invoice = this.invoiceRepository.findById(invoiceId);
        if (!invoice.isPresent()) {
            return false;
        }
        return invoice.get().getDestinataire().getId().equals(user.getUuid());
    }
}
