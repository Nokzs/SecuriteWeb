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
import com.example.securitewebback.invoice.RefundInfo;
import com.example.securitewebback.invoice.service.InvoicesService;
import com.example.securitewebback.payement.PaymentService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Service
@Slf4j
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BuildingRepository buildingRepository;
    private final SyndicRepository syndicRepository;
    @Value("${app.paiement.url}")
    private String paiementAppUrl;
    private final PaymentService paymentService;
    private final InvoicesService invoicesService;

    public ExpenseService(ExpenseRepository expenseRepository, BuildingRepository buildingRepository,
            SyndicRepository syndicRepository, PaymentService paymentService, PaymentService paymentService2,
            InvoicesService invoicesService) {
        this.expenseRepository = expenseRepository;
        this.buildingRepository = buildingRepository;
        this.syndicRepository = syndicRepository;
        this.paymentService = paymentService2;
        this.invoicesService = invoicesService;
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
    public void cancelExpense(UUID expenseId, String token) {
        List<RefundInfo> refunds = this.invoicesService.getRefundInfos(expenseId);

        for (RefundInfo refund : refunds) {
            try {
                this.paymentService.transfertRequest(
                        refund.email(),
                        refund.amount(),
                        "Remboursement pour annulation de dépense",
                        token);
            } catch (Exception e) {
                log.error("ERREUR CRITIQUE : Impossible de rembourser {} pour la facture {}",
                        refund.email(), refund.invoiceId());
            }
        }

        invoicesService.finalizeCancellation(expenseId);
    }
}
