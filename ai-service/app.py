import json
import math
import os
import re
from typing import List

from fastapi import FastAPI
from openai import OpenAI
from pydantic import BaseModel, Field

app = FastAPI(title="AI Incident Triage Service", version="2.0.0")

RUNBOOKS = [
    {
        "id": "db-pool",
        "title": "Database connection pool exhaustion",
        "text": "When database connections are exhausted, inspect long-running queries and pool usage. Check connection pool size, transaction duration, idle connections, database CPU, and lock contention. Mitigate with safe pool tuning and query remediation.",
        "keywords": ["database", "postgres", "connection", "pool", "timeout", "sql", "query"]
    },
    {
        "id": "kafka-lag",
        "title": "Kafka consumer lag",
        "text": "For Kafka lag, inspect consumer group lag, partition distribution, processing latency, broker health, and rebalance activity. Scale consumers only after confirming partitions and downstream capacity.",
        "keywords": ["kafka", "consumer", "lag", "partition", "broker", "rebalance"]
    },
    {
        "id": "api-latency",
        "title": "High API latency",
        "text": "For high API latency, inspect p95 and p99 latency, downstream dependencies, database query time, thread pools, CPU, memory, and recent deployments. Compare traces before changing capacity.",
        "keywords": ["latency", "api", "slow", "timeout", "response", "p95", "p99"]
    },
    {
        "id": "auth-failure",
        "title": "Authentication failure spike",
        "text": "For authentication failures, inspect error rates, token validation, identity provider health, clock skew, certificate expiry, and recent authentication configuration changes. Avoid disabling security controls as a first response.",
        "keywords": ["auth", "authentication", "jwt", "token", "login", "certificate", "401", "403"]
    },
]

class IncidentRequest(BaseModel):
    incidentId: str
    serviceName: str
    errorCode: str
    message: str = Field(min_length=1, max_length=4000)

class AnalysisResponse(BaseModel):
    severity: str
    rootCause: str
    recommendation: str
    confidence: float
    retrievedRunbook: str


def tokens(text: str) -> set[str]:
    return set(re.findall(r"[a-z0-9_:-]+", text.lower()))


def retrieve_runbook(query: str) -> dict:
    q = tokens(query)
    scored = []
    for book in RUNBOOKS:
        book_tokens = tokens(book["text"] + " " + " ".join(book["keywords"]))
        overlap = len(q & book_tokens)
        keyword_bonus = sum(2 for k in book["keywords"] if k in q)
        scored.append((overlap + keyword_bonus, book))
    scored.sort(key=lambda x: x[0], reverse=True)
    return scored[0][1]


def fallback_analysis(req: IncidentRequest, book: dict) -> AnalysisResponse:
    text = f"{req.errorCode} {req.message}".lower()
    if any(x in text for x in ["database", "connection", "timeout", "payment"]):
        severity = "HIGH"
    elif any(x in text for x in ["auth", "401", "403", "latency"]):
        severity = "MEDIUM"
    else:
        severity = "LOW"

    return AnalysisResponse(
        severity=severity,
        rootCause=f"Likely related to the '{book['title']}' operational pattern based on the incident signal.",
        recommendation=book["text"],
        confidence=0.72,
        retrievedRunbook=book["id"],
    )


def llm_analysis(req: IncidentRequest, book: dict) -> AnalysisResponse | None:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        return None

    client = OpenAI(api_key=api_key)
    model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    prompt = f"""You are a production incident triage assistant.
Use ONLY the incident and retrieved runbook context below. Do not invent infrastructure facts.
Return JSON with exactly: severity (LOW|MEDIUM|HIGH|CRITICAL), rootCause, recommendation, confidence (0-1).

Incident:
service={req.serviceName}
errorCode={req.errorCode}
message={req.message}

Retrieved runbook:
{book['title']}: {book['text']}
"""
    response = client.chat.completions.create(
        model=model,
        temperature=0.1,
        response_format={"type": "json_object"},
        messages=[
            {"role": "system", "content": "You produce concise, operationally safe incident triage."},
            {"role": "user", "content": prompt},
        ],
    )
    data = json.loads(response.choices[0].message.content)
    confidence = max(0.0, min(1.0, float(data.get("confidence", 0.7))))
    return AnalysisResponse(
        severity=str(data.get("severity", "MEDIUM")).upper(),
        rootCause=str(data.get("rootCause", "Insufficient evidence for a precise root cause.")),
        recommendation=str(data.get("recommendation", book["text"])),
        confidence=confidence,
        retrievedRunbook=book["id"],
    )


@app.get("/health")
def health():
    return {"status": "ok", "llmEnabled": bool(os.getenv("OPENAI_API_KEY"))}


@app.post("/analyze", response_model=AnalysisResponse)
def analyze(req: IncidentRequest):
    query = f"{req.serviceName} {req.errorCode} {req.message}"
    book = retrieve_runbook(query)
    result = llm_analysis(req, book)
    return result if result else fallback_analysis(req, book)
