package com.tarun.incidenttriage.incident;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentTest {

    @Test
    void newIncidentStartsOpen() {
        Incident incident = new Incident(
                UUID.randomUUID(),
                "payment-service",
                "DATABASE_CONNECTION_FAILURE",
                "Connection pool exhausted",
                Instant.parse("2026-09-08T12:00:00Z")
        );

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(incident.getServiceName()).isEqualTo("payment-service");
    }
}
