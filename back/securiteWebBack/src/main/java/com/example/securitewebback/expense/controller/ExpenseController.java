package com.example.securitewebback.expense.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.securitewebback.expense.dto.CreateExpenseDto;
import com.example.securitewebback.expense.dto.ExpenseDto;
import com.example.securitewebback.expense.entity.Expense;
import com.example.securitewebback.expense.service.ExpenseService;
import com.example.securitewebback.invoice.invoiceEnum.InvoiceStatut;
import com.example.securitewebback.invoice.service.InvoicesService;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.transaction.annotation.Transactional;
import com.example.securitewebback.security.CustomUserDetails;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final InvoicesService invoiceService;

    public ExpenseController(ExpenseService expenseService, InvoicesService invoiceService) {
        this.invoiceService = invoiceService;
        this.expenseService = expenseService;
    }

    @PreAuthorize("hasRole('SYNDIC')")
    @GetMapping("/{buildingId}")
    public Page<ExpenseDto> getExpense(@PathVariable String buildingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);
        Page<Expense> expenses = expenseService.getExpensesByBuildingId(buildingId, pageable);
        return expenses.map(
                expense -> {
                    int numberPaid = (int) expense.getInvoices().stream()
                            .filter(invoice -> invoice.getStatut() == InvoiceStatut.PAID).count();
                    return new ExpenseDto(expense.getId().toString(), expense.getLabel(), expense.getTotalAmount(),
                            expense.getStatut().toString(),
                            expense.getCreatedAt(),
                            numberPaid, expense.getInvoices().size());
                });
    }

    @PreAuthorize("hasRole('SYNDIC')")
    @PostMapping("/{buildingId}")
    @Transactional
    public void createExpense(@PathVariable UUID buildingId, @RequestBody CreateExpenseDto request) {
        Expense expense = expenseService.createExpense(buildingId, request);
        this.invoiceService.generateInvoicesForBuilding(buildingId, expense);

    }

    @PreAuthorize("hasRole('SYNDIC')")
    @PutMapping("/{expenseId}")
    public void cancelExpense(@PathVariable UUID expenseId, Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        String tokenValue = user.getToken();
        this.expenseService.cancelExpense(expenseId, tokenValue);
    }

}
