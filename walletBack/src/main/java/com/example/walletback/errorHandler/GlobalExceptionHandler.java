package com.example.walletback.errorHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.walletback.error.InsufficientSoldException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientSoldException.class)
    public ResponseEntity<String> handleUserAlreadyExists(InsufficientSoldException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
