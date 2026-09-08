package com.tarun.incidenttriage.incident;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {
    public static final String INCIDENT_TOPIC = "production-incidents";

    private final KafkaTemplate<String, IncidentEvent> kafkaTemplate;
    private final IncidentRepository incidentRepository;

    public IncidentService(KafkaTemplate<String, IncidentEvent> kafkaTemplate,
                           IncidentRepository incidentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.incidentRepository = incidentRepository;
    }

    public UUID publish(IncidentRequest request) {
        UUID incidentId = UUID.randomUUID();
        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();

        IncidentEvent event = new IncidentEvent(
                incidentId,
                request.serviceName(),
                request.errorCode(),
                request.message(),
                occurredAt
        );

        kafkaTemplate.send(INCIDENT_TOPIC, incidentId.toString(), event);
        return incidentId;
    }

    @Transactional
    public Incident persist(IncidentEvent event) {
        if (incidentRepository.existsByIncidentId(event.incidentId())) {
            return incidentRepository.findAll().stream()
                    .filter(existing -> event.incidentId().equals(existing.getIncidentId()))
                    .findFirst()
                    .orElseThrow();
        }

        return incidentRepository.save(new Incident(
                event.incidentId(),
                event.serviceName(),
                event.errorCode(),
                event.message(),
                event.occurredAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<Incident> latest() {
        return incidentRepository.findTop50ByOrderByOccurredAtDesc();
    }
}
