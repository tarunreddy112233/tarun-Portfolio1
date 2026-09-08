package com.tarun.incidenttriage.incident;

import com.tarun.incidenttriage.ai.AiAnalysisClient;
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
    private final AiAnalysisClient aiAnalysisClient;

    public IncidentService(KafkaTemplate<String, IncidentEvent> kafkaTemplate,
                           IncidentRepository incidentRepository,
                           AiAnalysisClient aiAnalysisClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.incidentRepository = incidentRepository;
        this.aiAnalysisClient = aiAnalysisClient;
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
        return incidentRepository.findByIncidentId(event.incidentId())
                .orElseGet(() -> incidentRepository.save(new Incident(
                        event.incidentId(),
                        event.serviceName(),
                        event.errorCode(),
                        event.message(),
                        event.occurredAt()
                )));
    }

    @Transactional
    public void analyzeAndStore(IncidentEvent event) {
        Incident incident = persist(event);
        IncidentAnalysis analysis = aiAnalysisClient.analyze(incident);
        incident.applyAnalysis(analysis);
        incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public List<Incident> latest() {
        return incidentRepository.findTop50ByOrderByOccurredAtDesc();
    }
}
