package com.example.securitewebback.incident.entity;

public enum VoteStatus {
    ONGOING,    // Le vote est en cours
    CLOSED,     // La date de fin est passée, en attente de dépouillement (optionnel)
    PASSED,     // Le vote a été accepté (majorité atteinte)
    REJECTED    // Le vote a été refusé
}