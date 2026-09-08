package com.tarun.incidenttriage.incident;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentEventConsumer {
    private final IncidentService incidentService;

    public IncidentEventConsumer(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @KafkaListener(topics = IncidentService.INCIDENT_TOPIC, groupId = "incident-persistence")
    public void consume(IncidentEvent event) {
        incidentService.analyzeAndStore(event);
    }
}
