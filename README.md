# Estadísticas ECI — Institutional Integration Metrics

A Spring Boot REST API that serves aggregated, anonymized metrics for an institutional
integration dashboard. Built with hexagonal (ports & adapters) architecture.

## Overview

This service provides authorized administrators and wellbeing personnel with quantitative
insights into four key areas:

- **Early Activity** — percentage of students who performed a connection or patch
  operation within a given period (new connection rate), plus those who remained inactive
  during their first semester.
- **Mentorships** — distribution of mentorship relationships grouped by academic program.
- **Wellbeing** — weekly check-in and intervention counts, along with unique student
  participation, giving a pulse on welfare engagement.

All responses are strictly aggregated; the service includes a PII detection layer that
scans output for email addresses and user identifiers, preventing personal data from
leaking into reports. Data can be retrieved as JSON or CSV.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.5 (Web, Data JPA, Security, Validation)
- **Database:** H2 in-memory (development) / PostgreSQL (production)
- **Authentication:** JWT (HMAC-SHA256) with role-based access control
- **CSV Export:** OpenCSV 5.9
- **Build:** Maven

## Architecture

The project follows a hexagonal (ports & adapters) structure:

```
src/main/java/com/cybersapiens/estadisticaseci/
├── domain/          # Core business logic (models, services, ports)
│   ├── model/       # NewConnectionRate, InactiveFirstSemester, MentorshipByProgram, WeeklyWelfare
│   ├── port/in/     # Use case interfaces
│   ├── port/out/    # Repository / data source interfaces
│   └── service/     # MetricsCalculationService, AnonymizationService
├── application/     # Use case handlers
│   └── handler/     # GetIntegrationMetricsHandler
├── infrastructure/  # Adapters (web, persistence, security, config)
│   ├── web/         # REST controller, DTOs, mappers, CSV serializer, exception handler
│   ├── persistence/ # JPA entities, repositories, adapters
│   └── security/    # JWT filter, validator, payload model
└── shared/          # Cross-cutting exceptions
```

## Prerequisites

- JDK 21
- Maven 3.9+

## Getting Started

### Development (H2 in-memory)

```bash
mvn spring-boot:run
```

The application starts on port `8082` with an H2 in-memory database.
Seed data is loaded automatically from `data.sql`, providing sample activity records,
mentorships, and welfare check-ins across four academic programs.

### Production (PostgreSQL)

```bash
mvn spring-boot:run -Dspring.profiles.active=postgres
```

Requires a PostgreSQL instance. Connection details are configured via environment
variables `DB_USER` (default: `postgres`) and `DB_PASSWORD` (default: `postgres`).

## API Reference

### GET /api/v1/metrics/integration

Returns aggregated integration metrics within a date range, optionally filtered by
academic program.

**Authentication:** Bearer token in the `Authorization` header. The JWT payload must
contain a `roles` array with at least one of `ADMIN` or `WELLBEING`.

**Required Roles:** `ADMIN`, `WELLBEING`

#### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `dateFrom` | `LocalDate` (ISO 8601) | Yes | Start of the analysis period |
| `dateTo` | `LocalDate` (ISO 8601) | Yes | End of the analysis period |
| `academicProgram` | `String` | No | Program code to filter by (e.g., `ING-COMP`) |
| `format` | `String` | No | Response format: `json` (default) or `csv` |

#### Response (JSON)

```json
{
  "newConnectionRate": {
    "percentage": 75.0,
    "connectedCount": 3,
    "totalCount": 4
  },
  "inactiveFirstSemester": {
    "percentage": 25.0,
    "inactiveCount": 1,
    "totalCount": 4
  },
  "mentorshipsByProgram": [
    {
      "programCode": "ING-COMP",
      "programName": "Ingeniería en Computación",
      "mentorshipCount": 2
    }
  ],
  "weeklyWelfare": [
    {
      "weekStart": "2026-06-01",
      "checkinCount": 2,
      "interventionCount": 1,
      "uniqueStudents": 2
    }
  ]
}
```

#### Response (CSV)

When `format=csv` is specified, the response is a `text/csv` file with four sections:
new connection rate, inactive first semester, mentorships by program, and weekly welfare.

#### Error Responses

| Status | Scenario |
|---|---|
| `403 Forbidden` | Missing or invalid role (not ADMIN or WELLBEING) |
| `400 Bad Request` | Missing required parameters or validation errors |
| `200 OK` (zeroed metrics) | No data found for the given filters |

## Authentication

The API uses self-contained HMAC-SHA256 JWTs. The expected payload structure is:

```json
{
  "sub": "user-id",
  "roles": ["ADMIN"]
}
```

The JWT secret is configured via the `jwt.secret` property (default for development:
`dev-secret-key-for-local-testing-only`).

## Profiles

| Profile | Database | DDL Mode | Seed Data |
|---|---|---|---|
| default (dev) | H2 in-memory | `create-drop` | Loaded from `data.sql` |
| `postgres` | PostgreSQL | `validate` | None |

## Testing

```bash
mvn test
```

The test suite covers:

- **Controller layer:** JSON and CSV responses, role-based access (ADMIN and WELLBEING),
  empty-data fallback.
- **Domain services:** Anonymization (clean data passes, emails and user IDs are rejected),
  metrics calculation using mocked data ports.
