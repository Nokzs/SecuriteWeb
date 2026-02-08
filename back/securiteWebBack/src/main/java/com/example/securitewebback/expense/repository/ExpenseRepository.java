package com.example.securitewebback.expense.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securitewebback.expense.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findByBuildingId(UUID buildingId, Pageable pageable);
}
