package com.example.securitewebback.incident.entity;

public enum IncidentStatus {
    PENDING,      // En attente de lecture par le syndic
    IGNORED,      // Ignoré par le syndic (spam ou non pertinent)
    VOTED,        // Un vote est en cours
    IN_PROGRESS,  // Vote accepté : Travaux en cours / Commandés
    RESOLVED      // Terminé (Réparé ou Vote refusé -> Dossier clos)
}