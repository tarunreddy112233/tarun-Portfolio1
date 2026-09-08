package com.tarun.incidenttriage.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findTop50ByOrderByOccurredAtDesc();
    boolean existsByIncidentId(UUID incidentId);
}
