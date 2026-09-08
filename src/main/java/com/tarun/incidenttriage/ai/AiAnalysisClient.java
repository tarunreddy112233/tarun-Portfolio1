package com.tarun.incidenttriage.ai;

import com.tarun.incidenttriage.incident.Incident;
import com.tarun.incidenttriage.incident.IncidentAnalysis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiAnalysisClient {
    private final RestClient restClient;

    public AiAnalysisClient(@Value("${ai.service-url}") String serviceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .build();
    }

    public IncidentAnalysis analyze(Incident incident) {
        AnalysisRequest request = new AnalysisRequest(
                incident.getIncidentId().toString(),
                incident.getServiceName(),
                incident.getErrorCode(),
                incident.getMessage()
        );

        return restClient.post()
                .uri("/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(IncidentAnalysis.class);
    }

    private record AnalysisRequest(
            String incidentId,
            String serviceName,
            String errorCode,
            String message
    ) {}
}
