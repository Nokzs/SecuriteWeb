package com.example.walletback.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * OperationDto
 */
public record OperationDto(
        String mail,
        BigDecimal amount,
        LocalDateTime date,
        String sign,
        String label,
        String receiverMail) {
}
