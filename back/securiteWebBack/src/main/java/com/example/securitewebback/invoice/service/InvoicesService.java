package com.example.securitewebback.invoice.service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.securitewebback.auth.entity.Proprietaire;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.expense.entity.Expense;
import com.example.securitewebback.invoice.entity.Invoice;
import com.example.securitewebback.invoice.invoiceEnum.InvoiceStatut;
import com.example.securitewebback.invoice.repository.InvoicesRepository;
import com.example.securitewebback.payement.PaymentService;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;

@Service
public class InvoicesService {
    private final BuildingRepository buildingRepository;
    private final InvoicesRepository invoiceRepository;
    private final PaymentService payementService;

    public InvoicesService(BuildingRepository buildingRepository, InvoicesRepository invoiceRepository,
            PaymentService payementService) {
        this.buildingRepository = buildingRepository;
        this.invoiceRepository = invoiceRepository;
        this.payementService = payementService;
    }

    public void generateInvoicesForBuilding(UUID buildingId, Expense expense) {
        Map<Proprietaire, Integer> tantiemesParProprietaire = getTantiemesParProprietaire(buildingId);
        int totalTantiemes = tantiemesParProprietaire.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<Proprietaire, Integer> entry : tantiemesParProprietaire.entrySet()) {
            Proprietaire proprietaire = entry.getKey();
            int tantiemes = entry.getValue();
            double montantFacture = (tantiemes / (double) totalTantiemes) * expense.getTotalAmount().doubleValue();
            Invoice invoice = new Invoice();
            invoice.setDestinataire(proprietaire);
            invoice.setExpense(expense);
            invoice.setAmount(BigDecimal.valueOf(montantFacture));
            invoice.setLabel(expense.getLabel());
            invoiceRepository.save(invoice);
        }
    }

    public Page<Invoice> getInvoices(UUID ownerId, Pageable pageable) {
        return invoiceRepository.findByProprietaire(ownerId, pageable);
    }

    private Map<Proprietaire, Integer> getTantiemesParProprietaire(UUID buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Bâtiment non trouvé"));

        return building.getApartment().stream()
                .filter(apartment -> apartment.getOwner() != null)
                .collect(Collectors.toMap(
                        apartment -> apartment.getOwner(),
                        apartment -> apartment.getTantiemes(),
                        Integer::sum));
    }

    public void payInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
        invoice.setStatut(InvoiceStatut.PAID);
        this.payementService.transfertRequest(invoice.getDestinataire().getId(), invoice.getAmount().doubleValue());
        invoiceRepository.save(invoice);
    }
}
