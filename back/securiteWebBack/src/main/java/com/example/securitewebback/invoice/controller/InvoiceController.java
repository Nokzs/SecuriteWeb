package com.example.securitewebback.invoice.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.securitewebback.building.dto.BuildingDto;
import com.example.securitewebback.invoice.InvoiceDto;
import com.example.securitewebback.invoice.entity.Invoice;
import com.example.securitewebback.invoice.service.InvoicesService;

@RequestMapping("/api/invoices")
@RestController
public class InvoiceController {
    private final InvoicesService invoicesService;

    public InvoiceController(InvoicesService invoicesService) {
        this.invoicesService = invoicesService;
    }

    @GetMapping
    public Page<InvoiceDto> getInvoices(Authentication authentication, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        String syndicUuid = authentication.getName();
        Pageable pageable = Pageable.ofSize(limit).withPage(page);

        Page<Invoice> invoices = this.invoicesService.getInvoices(UUID.fromString(syndicUuid), pageable);
        return invoices.map(invoice -> {
            return new InvoiceDto(invoice.getLabel(), invoice.getAmount(),
                    BuildingDto.fromEntity(invoice.getExpense().getBuilding()), invoice.getStatut().toString(),
                    invoice.getCreatedAt().toString());
        });
    }

    @PostMapping("/pay/{invoiceId}")
    public void pay(@PathVariable("invoiceId") UUID invoiceId) {
    }

}
