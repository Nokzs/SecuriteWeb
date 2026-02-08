package com.example.securitewebback.invoice;

import java.util.UUID;

public record RefundInfo(String email, double amount, UUID invoiceId) {
}
