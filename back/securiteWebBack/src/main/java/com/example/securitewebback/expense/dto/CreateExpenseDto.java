package com.example.securitewebback.expense.dto;

import java.math.BigDecimal;

public record CreateExpenseDto(
                                String description,
                                BigDecimal amount) {
}
