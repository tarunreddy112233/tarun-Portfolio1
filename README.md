# AI-Powered Production Incident Triage Platform

An event-driven incident intake and persistence service built with Spring Boot, Apache Kafka, and PostgreSQL. Phase 1 establishes the core production-style backend foundation for a later AI/RAG incident analysis workflow.

## Phase 1 architecture

```text
Client / simulated service
          |
          | POST /api/incidents
          v
   Spring Boot REST API
          |
          | Kafka event
          v
   production-incidents
          |
          | async consumer
          v
      PostgreSQL
          |
          v
 GET /api/incidents
```

## Why this project exists

The platform models a common production workflow: application services emit incident events, Kafka decouples ingestion from persistence, and PostgreSQL provides durable incident history. Later phases will add anomaly detection, vector search/RAG, LLM-assisted root-cause analysis, and a React operations dashboard.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Apache Kafka
- PostgreSQL 17
- Docker Compose
- Spring Actuator
- JUnit / Testcontainers

Spring Boot 4.1.1 is the current stable release as of September 2026; Spring's documentation lists Java 17+ as required and recommends `spring-boot-starter-webmvc` for Spring MVC applications. citeturn889742search1turn889742search4

## Run locally

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` and Kafka on `localhost:9092`.

### 2. Start the application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### 3. Create an incident

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "payment-service",
    "errorCode": "DATABASE_CONNECTION_FAILURE",
    "message": "PostgreSQL connection pool exhausted"
  }'
```

Expected response:

```json
{
  "incidentId": "<uuid>",
  "status": "QUEUED",
  "message": "Incident accepted for asynchronous processing"
}
```

### 4. Query recent incidents

```bash
curl http://localhost:8080/api/incidents
```

## API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/incidents` | Accept an incident and publish it to Kafka |
| GET | `/api/incidents` | Return the latest persisted incidents |
| GET | `/actuator/health` | Service health check |
| GET | `/actuator/metrics` | Application metrics |

## Event contract

Kafka topic: `production-incidents`

```json
{
  "incidentId": "8bf3a6ce-4ddf-4fa0-b3b9-4fc6dc7f2c52",
  "serviceName": "payment-service",
  "errorCode": "DATABASE_CONNECTION_FAILURE",
  "message": "PostgreSQL connection pool exhausted",
  "occurredAt": "2026-09-08T16:00:00Z"
}
```

## Engineering decisions

- REST ingestion is intentionally asynchronous: the API returns `202 Accepted` after publishing to Kafka instead of waiting for database persistence.
- Kafka uses three partitions in the local topic definition to model partitioned event processing.
- PostgreSQL stores a durable incident record and protects against duplicate event IDs.
- Configuration is externalized through environment variables so local and cloud deployment can use the same application artifact.
- Actuator health and metrics endpoints are enabled for the observability work that will be expanded in later phases.

## Roadmap

### Phase 1 — Event-driven foundation
- Spring Boot API
- Kafka event ingestion
- PostgreSQL persistence
- Docker Compose
- Health/metrics endpoints

### Phase 2 — Incident intelligence
- Error-rate and latency anomaly detection
- Incident correlation
- Severity scoring
- Redis caching

### Phase 3 — AI/RAG
- Runbook knowledge base
- Embeddings and vector search
- LLM root-cause analysis
- Structured remediation recommendations

### Phase 4 — Operations UI
- React + TypeScript dashboard
- Live incident stream
- Incident details and AI recommendations

### Phase 5 — Productionization
- Docker image
- CI/CD
- AWS deployment
- Observability and load testing
