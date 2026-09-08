package com.tarun.incidenttriage.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID incidentId;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String errorCode;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private Instant occurredAt;

    protected Incident() {
    }

    public Incident(UUID incidentId, String serviceName, String errorCode, String message, Instant occurredAt) {
        this.incidentId = incidentId;
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.message = message;
        this.status = IncidentStatus.OPEN;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public UUID getIncidentId() { return incidentId; }
    public String getServiceName() { return serviceName; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public IncidentStatus getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setStatus(IncidentStatus status) { this.status = status; }
}
