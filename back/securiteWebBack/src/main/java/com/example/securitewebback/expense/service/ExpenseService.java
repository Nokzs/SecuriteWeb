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

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final BuildingRepository buildingRepository;
    private final SyndicRepository syndicRepository;

    public ExpenseService(ExpenseRepository expenseRepository, BuildingRepository buildingRepository,
            SyndicRepository syndicRepository) {
        this.expenseRepository = expenseRepository;
        this.buildingRepository = buildingRepository;
        this.syndicRepository = syndicRepository;
    }

    public Page<Expense> getExpensesByBuildingId(String buildingId, Pageable pageable) {
        return expenseRepository.findByBuildingId(UUID.fromString(buildingId), pageable);
    }

    public Expense createExpense(UUID buildingId, CreateExpenseDto expense) {
        Expense newExpense = new Expense();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));
        Syndic syndic = syndicRepository.findById(building.getSyndic().getId())
                .orElseThrow(() -> new RuntimeException("Syndic not found for the building"));
        newExpense.setBuilding(building);
        newExpense.setSyndic(syndic);
        newExpense.setTotalAmount(expense.amount());
        newExpense.setLabel(expense.description());
        return expenseRepository.save(newExpense);
    }
}
