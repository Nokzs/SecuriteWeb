package com.example.securitewebback.invoice.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.invoice.InvoiceDto;
import com.example.securitewebback.invoice.entity.Invoice;
import com.example.securitewebback.invoice.service.InvoicesService;
import com.example.securitewebback.payement.PaymentService;
import com.example.securitewebback.security.CustomUserDetails;

import jakarta.transaction.Transactional;

@RequestMapping("/api/invoices")
@RestController
public class InvoiceController {
    private final InvoicesService invoicesService;
    private final PaymentService payementService;

    public InvoiceController(InvoicesService invoicesService, PaymentService payementService) {
        this.invoicesService = invoicesService;
        this.payementService = payementService;
    }

    @GetMapping
    public Page<InvoiceDto> getInvoices(Authentication authentication, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUuid();
        Pageable pageable = Pageable.ofSize(limit).withPage(page);

        Page<Invoice> invoices = this.invoicesService.getInvoices(userId, pageable);
        return invoices.map(invoice -> {
            return new InvoiceDto(invoice.getId(), invoice.getLabel(), invoice.getAmount(),
                    BuildingDto.fromEntity(invoice.getExpense().getBuilding()), invoice.getStatut().toString(),
                    invoice.getCreatedAt().toString());
        });
    }

    @PostMapping("/pay/{invoiceId}")
    public void pay(@PathVariable("invoiceId") UUID invoiceId, Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        Invoice invoice = invoicesService.getInvoiceForPayment(invoiceId);
        String tokenValue = user.getToken();
        this.payementService.transfertRequest(
                invoice.getExpense().getSyndic().getEmail(),
                invoice.getAmount().doubleValue(),
                "Payement de la facture " + invoice.getLabel(),
                tokenValue);

        invoicesService.markAsPaid(invoiceId);
    }
}
