package com.example.securitewebback.incident.dto;

import java.time.Instant;
import java.util.List;

public class IncidentDto {
    public String id;
    public String title;
    public String description;
    public boolean isUrgent;
    public String apartmentId;
    public String reporterUuid;
    public Instant createdAt;
    public List<String> photoUrls;

    public IncidentDto(String id, String title, String description, boolean isUrgent,
                      String apartmentId, String reporterUuid, Instant createdAt, List<String> photoUrls) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isUrgent = isUrgent;
        this.apartmentId = apartmentId;
        this.reporterUuid = reporterUuid;
        this.createdAt = createdAt;
        this.photoUrls = photoUrls;
    }
}
