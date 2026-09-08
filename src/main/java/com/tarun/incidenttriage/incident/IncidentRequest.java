package com.tarun.incidenttriage.incident;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record IncidentRequest(
        @NotBlank String serviceName,
        @NotBlank String errorCode,
        @NotBlank String message,
        Instant occurredAt
) {
}
