package com.example.walletback.error;

public class InsufficientSoldException extends RuntimeException {
    public InsufficientSoldException(String message) {
        super(message);
    }
}
