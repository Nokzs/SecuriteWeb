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

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{buildingId}")
    public Page<ExpenseDto> getExpense(@RequestParam String buildingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(page, limit);
        Page<Expense> expenses = expenseService.getExpensesByBuildingId(buildingId, pageable);
        return expenses.map(
                expense -> {
                    int numberPaid = (int) expense.getInvoices().stream()
                            .filter(invoice -> invoice.getStatut() == InvoiceStatut.PAID).count();
                    return new ExpenseDto(expense.getLabel(), expense.getTotalAmount(), expense.getCreatedAt(),
                            expense.getInvoices().size(), numberPaid);
                });
    }

    @PostMapping("/{buildingId}")
    public Expense createExpense(@RequestParam UUID buildingId, CreateExpenseDto request) {
        return expenseService.createExpense(buildingId, request);
    }

}
