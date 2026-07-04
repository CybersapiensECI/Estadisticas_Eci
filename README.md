# Estadísticas ECI — Institutional Integration Metrics
...
A Spring Boot REST API that serves aggregated, anonymized metrics for an institutional
integration dashboard. Built with hexagonal (ports & adapters) architecture. Also acts as
a **BFF (Backend For Frontend)** aggregating personal statistics from multiple microservices.

## Overview

This service provides two types of metrics:

### Admin / Institutional Metrics

Authorized administrators and wellbeing personnel can access quantitative insights into
four key areas:

- **Early Activity** — percentage of students who performed a connection or patch
  operation within a given period (new connection rate), plus those who remained inactive
  during their first semester.
- **Mentorships** — distribution of mentorship relationships grouped by academic program.
- **Wellbeing** — weekly check-in and intervention counts, along with unique student
  participation, giving a pulse on welfare engagement.

All responses are strictly aggregated; the service includes a PII detection layer that
scans output for email addresses and user identifiers, preventing personal data from
leaking into reports. Data can be retrieved as JSON or CSV.

### Personal User Statistics

Authenticated users can view their own personal statistics aggregated in real time from
multiple microservices across the platform:

- **Gamification** — monas (achievements) obtained, XP earned, progress, completion rate.
- **Events** — events attended and upcoming agenda.
- **Parches** — university social groups joined and active memberships.
- **Profile** — career, semester, level, active status.

If any upstream service is unavailable, the corresponding section is omitted (graceful
degradation).

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.5 (Web, WebFlux, Data JPA, Security, Validation)
- **Database:** H2 in-memory (development) / PostgreSQL (production)
- **Authentication:** JWT (HMAC-SHA256) with role-based access control
- **Service-to-Service Communication:** WebClient (reactive HTTP client)
- **CSV Export:** OpenCSV 5.9
- **Build:** Maven

## Architecture

The project follows a hexagonal (ports & adapters) structure:

```
src/main/java/com/cybersapiens/estadisticaseci/
├── domain/          # Core business logic (models, services, ports)
│   ├── model/       # IntegrationMetrics, UserPersonalStats
│   ├── port/in/     # GetIntegrationMetricsUseCase, GetUserPersonalStatsUseCase
│   ├── port/out/    # ActivityDataPort, MentorshipDataPort, WelfareDataPort,
│   │                # ExternalGamificationPort, ExternalEventPort,
│   │                # ExternalParchePort, ExternalProfilePort
│   └── service/     # MetricsCalculationService, AnonymizationService
├── application/     # Use case handlers
│   └── handler/     # GetIntegrationMetricsHandler, GetUserPersonalStatsHandler
├── infrastructure/  # Adapters (web, persistence, security, config)
│   ├── web/         # REST controller, DTOs, mappers, CSV serializer,
│   │               # exception handler, HTTP client adapters
│   ├── persistence/ # JPA entities, repositories, adapters
│   ├── security/    # JWT filter, validator, payload model
│   └── config/      # DomainServiceConfig, WebClientConfig, SecurityConfig
└── shared/          # Cross-cutting exceptions
```

## Microservices Integration

This service consumes REST APIs from the following microservices:

| Service | Endpoints Used | Purpose |
|---|---|---|
| GamificationService | `GET /api/v1/gamification/users/{userId}/monas` | Monas, XP, progress |
| EventService | `GET /events/agenda?userId=` | Events attended |
| Parches-Service | `GET /api/parches/user/{userId}` | Parches joined |
| profile-service | `GET /api/v1/users/{userId}` | Career, level, active status |

Service URLs are configurable via environment variables (see Configuration section).

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

## Configuration

### Service URLs

Endpoints for external microservices are configured via environment variables
with localhost defaults:

| Variable | Default | Description |
|---|---|---|
| `GAMIFICATION_SERVICE_URL` | `http://localhost:8082` | GamificationService base URL |
| `EVENT_SERVICE_URL` | `http://localhost:8081` | EventService base URL |
| `PARCHES_SERVICE_URL` | `http://localhost:8080` | Parches-Service base URL |
| `PROFILE_SERVICE_URL` | `http://localhost:8080` | profile-service base URL |

Override for production (e.g., Azure App Settings):

```
GAMIFICATION_SERVICE_URL=https://gamification-api.azurewebsites.net
EVENT_SERVICE_URL=https://events-api.azurewebsites.net
PARCHES_SERVICE_URL=https://parches-api.azurewebsites.net
PROFILE_SERVICE_URL=https://profile-api.azurewebsites.net
```

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

### GET /api/v1/metrics/user/{userId}

Returns personal statistics for a specific user by aggregating data from multiple
microservices in real time.

**Authentication:** Bearer token in the `Authorization` header. The authenticated user
can only view their own data (`sub` claim matches `userId`) unless they have the
`ADMIN` or `WELLBEING` role.

**Required Roles:** Matching `sub` claim, `ADMIN`, or `WELLBEING`

#### Path Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `userId` | `String` | Yes | User identifier |

#### Response

```json
{
  "userId": "user-123",
  "gamification": {
    "totalXp": 1500,
    "totalMonasUnlocked": 5,
    "monasInProgress": 3,
    "monasLocked": 22,
    "completionPercentage": 16.67,
    "monas": [
      {
        "code": "PRIMER_CONTACTO",
        "name": "Primer Contacto",
        "rarity": "COMUN",
        "status": "UNLOCKED",
        "unlockedAt": "2026-07-03T10:30:00"
      }
    ]
  },
  "events": {
    "totalAttended": 3,
    "upcomingEvents": 1,
    "totalEvents": 4,
    "eventIds": ["evt-001", "evt-002", "evt-003"]
  },
  "parches": {
    "totalJoined": 2,
    "activeParches": 2
  },
  "profile": {
    "xp": 1500,
    "level": 5,
    "isActive": true,
    "career": "SYSTEMS_ENGINEERING",
    "semester": 5
  }
}
```

If any upstream microservice is unavailable, the corresponding section is returned
as `null` (graceful degradation).

#### Error Responses

| Status | Scenario |
|---|---|
| `403 Forbidden` | Authenticated user does not match `userId` and is not ADMIN or WELLBEING |
| `200 OK` (partial data) | One or more upstream services unavailable |

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

The test suite covers **33 tests**:

- **Controller layer:** JSON and CSV responses for integration metrics, role-based
  access (ADMIN and WELLBEING), empty-data fallback, personal user stats endpoint
  (own user, admin access, partial data).
- **Domain services:** Anonymization (clean data passes, emails and user IDs are
  rejected), metrics calculation using mocked data ports.
- **Application handlers:** Personal stats aggregation with parallel service calls,
  partial failure tolerance, graceful degradation.
- **HTTP clients:** GamificationService, EventService, Parches-Service, profile-service
  client adapters — each tested with MockWebServer against real HTTP responses.
- **Response mapping:** Domain-to-DTO conversion with null safety.
