package com.tarun.incidenttriage.incident;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingest(@Valid @RequestBody IncidentRequest request) {
        UUID incidentId = incidentService.publish(request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/incidents/" + incidentId))
                .body(Map.of(
                        "incidentId", incidentId,
                        "status", "QUEUED",
                        "message", "Incident accepted for asynchronous processing"
                ));
    }

    @GetMapping
    public List<Incident> latest() {
        return incidentService.latest();
    }
}
