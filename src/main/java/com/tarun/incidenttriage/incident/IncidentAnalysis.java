package com.tarun.incidenttriage.incident;

/** Structured output returned by the AI/RAG triage service. */
public record IncidentAnalysis(
        String severity,
        String rootCause,
        String recommendation,
        Double confidence,
        String retrievedRunbook
) {
}
