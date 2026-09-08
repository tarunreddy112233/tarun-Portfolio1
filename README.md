# AI-Powered Production Incident Triage Platform

A production-style incident intelligence platform that combines an event-driven Java backend with a Python RAG/LLM service. It ingests incidents asynchronously through Kafka, persists them in PostgreSQL, retrieves relevant operational runbooks, and generates structured triage guidance.

## Architecture

```text
Simulated service / client
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
          | incident context
          v
   Python AI Service
          |
          +--> runbook retrieval
          |       |
          |       +--> operational knowledge
          |
          +--> optional OpenAI LLM
          |
          v
 structured triage result
          |
          v
      PostgreSQL
          |
          v
 GET /api/incidents
```

## What Phase 2 adds

- Dedicated Python FastAPI AI service.
- Retrieval-augmented generation (RAG) using an operational runbook knowledge base.
- Safe fallback analysis when no LLM API key is configured.
- Optional OpenAI-powered structured incident reasoning.
- Severity, probable root cause, remediation recommendation, confidence, and retrieved runbook stored with each incident.
- Spring Boot AI client connecting the Kafka-driven incident pipeline to the AI service.
- Docker Compose support for PostgreSQL, Kafka, and the AI service.

## Stack

### Backend
- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Apache Kafka
- PostgreSQL 17
- Spring Actuator

### AI
- Python 3.12
- FastAPI
- OpenAI API (optional)
- Retrieval-augmented generation (RAG)
- Operational runbook knowledge base

### DevOps
- Docker Compose
- GitHub Actions
- JUnit

## Run locally

### 1. Prerequisites

Install:
- Java 21
- Maven 3.9+
- Docker Desktop
- Python is not required locally if you run the AI service through Docker Compose.

### 2. Configure the optional LLM

Create a local `.env` file in the repository root. Never commit it.

```env
OPENAI_API_KEY=your_api_key
OPENAI_MODEL=gpt-4o-mini
```

The AI service still works without the key by using the deterministic runbook-retrieval fallback.

### 3. Start infrastructure and AI service

```bash
docker compose up -d --build
```

This starts PostgreSQL on `localhost:5432`, Kafka on `localhost:9092`, and the AI service on `localhost:8000`.

Check the AI service:

```bash
curl http://localhost:8000/health
```

### 4. Start the Spring Boot application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080` and connects to the AI service at `http://localhost:8000` by default.

### 5. Create an incident

```bash
curl -X POST http://localhost:8080/api/incidents \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "payment-service",
    "errorCode": "DATABASE_CONNECTION_FAILURE",
    "message": "PostgreSQL connection pool exhausted"
  }'
```

The API responds immediately with a queued incident ID. Kafka then delivers the event to the consumer, which persists the incident and requests AI triage.

### 6. Query recent incidents

```bash
curl http://localhost:8080/api/incidents
```

A completed record can contain fields such as:

```json
{
  "severity": "HIGH",
  "rootCause": "Likely related to database connection pool exhaustion...",
  "recommendation": "Inspect long-running queries and pool usage...",
  "confidence": 0.72,
  "retrievedRunbook": "db-pool"
}
```

## AI service API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/health` | AI service health and LLM configuration status |
| POST | `/analyze` | Retrieve relevant runbook context and generate triage analysis |

Example:

```bash
curl -X POST http://localhost:8000/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "incidentId": "demo-123",
    "serviceName": "payment-service",
    "errorCode": "DATABASE_CONNECTION_FAILURE",
    "message": "PostgreSQL connection pool exhausted"
  }'
```

## Security

- API keys are supplied through environment variables and are excluded by `.gitignore`.
- The LLM prompt explicitly limits reasoning to the incident and retrieved runbook context.
- The AI service returns structured JSON instead of free-form output so the backend can validate and persist predictable fields.
- The fallback mode allows local development without exposing an API key.

## Current project roadmap

### Phase 1 — Event-driven foundation ✅
- Spring Boot API
- Kafka event ingestion
- PostgreSQL persistence
- Docker Compose
- Health/metrics endpoints

### Phase 2 — Incident intelligence ✅
- Runbook retrieval
- AI service integration
- Severity scoring
- Structured root-cause and remediation output
- AI results persisted with incidents

### Phase 3 — Advanced RAG
- Embedding generation
- PostgreSQL + pgvector vector search
- Hybrid retrieval
- Runbook ingestion pipeline
- Evaluation dataset and retrieval metrics

### Phase 4 — Operations UI
- React + TypeScript dashboard
- Live incident stream
- Incident details
- AI recommendations and confidence
- Search/filtering

### Phase 5 — Productionization
- Containerized full stack
- CI/CD quality gates
- AWS deployment
- Observability
- Load testing and resilience testing
