package com.tarun.incidenttriage.incident;

import java.time.Instant;
import java.util.UUID;

public record IncidentEvent(
        UUID incidentId,
        String serviceName,
        String errorCode,
        String message,
        Instant occurredAt
) {
}
