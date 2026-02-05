package com.example.securitewebback.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseDto(
                String label,
                BigDecimal amount,
                LocalDateTime createdAt,
                int numberPaid,
                int invoiceCount) {

}
