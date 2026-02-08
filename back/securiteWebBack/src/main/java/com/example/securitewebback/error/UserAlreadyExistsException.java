package com.example.securitewebback.error;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("L'utilisateur avec l'email " + email + " existe déjà.");
    }
}
