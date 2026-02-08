package com.example.securitewebback.user.entity;

import java.time.Instant;
import java.util.UUID;

import com.example.securitewebback.auth.entity.Syndic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "syndic_id")
    @JsonIgnore // recursion infini avec building
    private Syndic syndic;

    private String senderFirstName;
    private String senderLastName;
    private String senderPhone;
    private String senderEmail;

    @Column(length = 2000)
    private String messageContent;

    // Flag indiquant si le syndic a lu le message
    @Column(name = "is_read")
    private boolean read;

    // Flag indiquant si le message est archivé
    @Column (name = "is_archived")
    private boolean archived;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        // par défaut non lu
        this.read = false;
        this.archived = false;
    }
}
