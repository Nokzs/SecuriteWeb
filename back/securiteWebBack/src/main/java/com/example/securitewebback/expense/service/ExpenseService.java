package com.example.securitewebback.expense.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.securitewebback.building.repository.BuildingRepository;
import com.example.securitewebback.expense.dto.CreateExpenseDto;
import com.example.securitewebback.expense.entity.Expense;
import com.example.securitewebback.expense.repository.ExpenseRepository;
import com.example.securitewebback.user.repository.SyndicRepository;
import com.example.securitewebback.building.entity.Building;
import com.example.securitewebback.auth.entity.Syndic;
import com.example.securitewebback.invoice.entity.Invoice;
import com.example.securitewebback.invoice.invoiceEnum.InvoiceStatut;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BuildingRepository buildingRepository;
    private final SyndicRepository syndicRepository;
    @Value("${app.paiement.url}")
    private String paiementAppUrl;
    private final PaymentService paymentService;
    public ExpenseService(ExpenseRepository expenseRepository, BuildingRepository buildingRepository,
            SyndicRepository syndicRepository,PaymentService paymentService) {
        this.expenseRepository = expenseRepository;
        this.buildingRepository = buildingRepository;
        this.syndicRepository = syndicRepository;
    }

    public Page<Expense> getExpensesByBuildingId(String buildingId, Pageable pageable) {
        return expenseRepository.findByBuildingId(UUID.fromString(buildingId), pageable);
    }

    @Transactional
    public Expense createExpense(UUID buildingId, CreateExpenseDto expense) {
        Expense newExpense = new Expense();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Syndic syndic = building.getSyndic();
        newExpense.setBuilding(building);
        newExpense.setSyndic(syndic);
        newExpense.setTotalAmount(expense.amount());
        newExpense.setLabel(expense.description());
        return expenseRepository.save(newExpense);
    }

    @Transactional
    public void cancelExpense(UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        List<Invoice> invoices = expense.getInvoices();
        List<Invoice> toRepay = new List<Invoice>();
        for (Invoice invoice : invoices) {
            if(invoice.getStatut() == InvoiceStatut.PAID) {
               this.payementService.transfertRequest(invoice.getDestinataire().getId(),invoice.getAmount().doubleValue());
            }
            invoice.setStatut(InvoiceStatut.CANCELLED);
        } 
        expense.setStatut(ExpenseStatut.CANCELLED);
    }

   }
