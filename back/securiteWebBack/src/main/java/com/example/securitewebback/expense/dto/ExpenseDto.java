package com.example.securitewebback.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseDto(
                                String id,
                                String label,
                                BigDecimal amount,
                                String status,
                                LocalDateTime createdAt,
                                int numberPaid,
                                int invoiceCount) {

}
