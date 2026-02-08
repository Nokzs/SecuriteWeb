package com.example.securitewebback.user.dto;

import java.time.Instant;

public record ContactMessageDto(
        String id,
        String senderFirstName,
        String senderLastName,
        String senderPhone,
        String senderEmail,
        String messageContent,
        boolean isRead,
        boolean isArchived,
        Instant createdAt) {

    public static ContactMessageDto fromEntity(com.example.securitewebback.user.entity.ContactMessage m) {
        return new ContactMessageDto(
                m.getId() != null ? m.getId().toString() : null,
                m.getSenderFirstName(),
                m.getSenderLastName(),
                m.getSenderPhone(),
                m.getSenderEmail(),
                m.getMessageContent(),
                m.isRead(),
                m.isArchived(),
                m.getCreatedAt());
    }
}
