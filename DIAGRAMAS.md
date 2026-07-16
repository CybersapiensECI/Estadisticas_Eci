# Diagramas PlantUML — Estadisticas_Eci

Este documento contiene el código PlantUML de los 4 diagramas de arquitectura del microservicio `Estadisticas_Eci`. Basado en el detalle documentado en [`ARQUITECTURA.md`](./ARQUITECTURA.md). Puedes renderizar cada bloque en [plantuml.com/plantuml](https://www.plantuml.com/plantuml) o con la extensión PlantUML de tu IDE.

---

## 1. Diagrama de Clases

Modelo de dominio (`domain/model`), servicios de dominio (`domain/service`) y ports (`domain/port`).

```plantuml
@startuml EstadisticasEci_Clases
title Estadisticas_Eci - Diagrama de Clases (Dominio)

skinparam classAttributeIconSize 0

class AcademicProgram <<record>> {
  +code: String
}

class DateRange <<record>> {
  +from: LocalDate
  +to: LocalDate
}

class NewConnectionRate <<record>> {
  +percentage: double
  +connectedCount: long
  +totalCount: long
}

class InactiveFirstSemester <<record>> {
  +percentage: double
  +inactiveCount: long
  +totalCount: long
}

class MentorshipByProgram <<record>> {
  +programCode: String
  +programName: String
  +mentorshipCount: long
}

class WeeklyWelfare <<record>> {
  +weekStart: LocalDate
  +checkinCount: long
  +interventionCount: long
  +uniqueStudents: long
}

class IntegrationMetrics <<record>> {
  +newConnectionRate: NewConnectionRate
  +inactiveFirstSemester: InactiveFirstSemester
  +mentorshipsByProgram: List<MentorshipByProgram>
  +weeklyWelfare: List<WeeklyWelfare>
}

IntegrationMetrics *-- NewConnectionRate
IntegrationMetrics *-- InactiveFirstSemester
IntegrationMetrics *-- "0..*" MentorshipByProgram
IntegrationMetrics *-- "0..*" WeeklyWelfare

class UserPersonalStats <<record>> {
  +userId: String
  +gamification: GamificationStats
  +event: EventStats
  +parche: ParcheStats
  +profile: ProfileStats
}

class GamificationStats <<record>> {
  +totalXp: int
  +totalMonasUnlocked: int
  +monasInProgress: int
  +monasLocked: int
  +completionPercentage: double
  +monas: List<MonaEntry>
}

class MonaEntry <<record>> {
  +code: String
  +name: String
  +rarity: String
  +status: String
  +unlockedAt: String
}

class EventStats <<record>> {
  +totalAttended: int
  +upcomingEvents: int
  +totalEvents: int
  +eventIds: List<String>
}

class ParcheStats <<record>> {
  +totalJoined: int
  +activeParches: int
}

class ProfileStats <<record>> {
  +xp: int
  +level: int
  +isActive: boolean
  +career: String
  +semester: Integer
}

UserPersonalStats *-- GamificationStats
UserPersonalStats *-- EventStats
UserPersonalStats *-- ParcheStats
UserPersonalStats *-- ProfileStats
GamificationStats *-- "0..*" MonaEntry

class MetricsCalculationService <<domain.service>> {
  +calculate(range: DateRange, program: AcademicProgram): IntegrationMetrics
}

class AnonymizationService <<domain.service>> {
  +ensureNoPii(metrics: IntegrationMetrics): IntegrationMetrics
  -collectStringFields(metrics): List<String>
}

interface ActivityDataPort <<port.out>> {
  +queryNewConnectionRate(range, program): NewConnectionRate
  +queryInactiveFirstSemester(range, program): InactiveFirstSemester
}
interface MentorshipDataPort <<port.out>> {
  +getMentorshipsByProgram(range, program): List<MentorshipByProgram>
}
interface WelfareDataPort <<port.out>> {
  +getWeeklyIndicators(range, program): List<WeeklyWelfare>
}
interface ExternalGamificationPort <<port.out>>
interface ExternalEventPort <<port.out>>
interface ExternalParchePort <<port.out>>
interface ExternalProfilePort <<port.out>>

MetricsCalculationService --> ActivityDataPort
MetricsCalculationService --> MentorshipDataPort
MetricsCalculationService --> WelfareDataPort
MetricsCalculationService ..> IntegrationMetrics : construye

interface GetIntegrationMetricsUseCase <<port.in>> {
  +execute(range, program): IntegrationMetrics
}
interface GetUserPersonalStatsUseCase <<port.in>> {
  +execute(userId): UserPersonalStats
}

class GetIntegrationMetricsHandler <<application>> implements GetIntegrationMetricsUseCase {
  +execute(range, program): IntegrationMetrics
}
class GetUserPersonalStatsHandler <<application>> implements GetUserPersonalStatsUseCase {
  +execute(userId): UserPersonalStats
}

GetIntegrationMetricsHandler --> MetricsCalculationService
GetIntegrationMetricsHandler --> AnonymizationService
GetUserPersonalStatsHandler --> ExternalGamificationPort
GetUserPersonalStatsHandler --> ExternalEventPort
GetUserPersonalStatsHandler --> ExternalParchePort
GetUserPersonalStatsHandler --> ExternalProfilePort
GetUserPersonalStatsHandler ..> UserPersonalStats : ensambla (CompletableFuture, fan-out paralelo)

@enduml
```

---

## 2. Diagrama de Componentes Específicos

Vista de componentes de la arquitectura hexagonal: adaptador REST, seguridad JWT, casos de uso, dominio, adaptadores JPA y clientes HTTP salientes.

```plantuml
@startuml EstadisticasEci_Componentes
title Estadisticas_Eci - Diagrama de Componentes

skinparam componentStyle rectangle

package "Seguridad" {
  [JwtAuthFilter] as JwtFilter
  [JwtValidator] as JwtValidator
}

package "Adaptador de Entrada (Inbound)" {
  [MetricsController] as Controller
  [GlobalExceptionHandler] as ExHandler
  [MetricsWebMapper] as WebMapper
  [MetricsCsvSerializer] as CsvSerializer
}

package "Aplicación (Casos de Uso)" {
  [GetIntegrationMetricsHandler] as MetricsHandler
  [GetUserPersonalStatsHandler] as StatsHandler
}

package "Dominio" {
  [MetricsCalculationService] as CalcService
  [AnonymizationService] as AnonService
}

package "Adaptadores de Salida (Outbound)" {
  [ActivityDataJpaAdapter] as ActivityAdapter
  [MentorshipDataJpaAdapter] as MentorshipAdapter
  [WelfareDataJpaAdapter] as WelfareAdapter
  [GamificationServiceClient] as GamifClient
  [EventServiceClient] as EventClient
  [ParcheServiceClient] as ParcheClient
  [ProfileServiceClient] as ProfileClient
}

database "PostgreSQL / H2\n(activity_records,\nmentorships,\nwelfare_checkins)" as DB

node "GamificationService" as GamifSvc
node "EventService" as EventSvc
node "Parches-Service" as ParcheSvc
node "profile-service" as ProfileSvc

JwtFilter --> JwtValidator : valida Bearer token
JwtFilter --> Controller : autentica request

Controller --> MetricsHandler : GET /integration
Controller --> StatsHandler : GET /user/{userId}
Controller --> WebMapper
Controller --> CsvSerializer
Controller ..> ExHandler : maneja excepciones

MetricsHandler --> CalcService
MetricsHandler --> AnonService
StatsHandler --> GamifClient
StatsHandler --> EventClient
StatsHandler --> ParcheClient
StatsHandler --> ProfileClient

CalcService --> ActivityAdapter
CalcService --> MentorshipAdapter
CalcService --> WelfareAdapter

ActivityAdapter --> DB
MentorshipAdapter --> DB
WelfareAdapter --> DB

GamifClient --> GamifSvc : GET /api/v1/gamification/users/{id}/monas
EventClient --> EventSvc : GET /events/agenda?userId=
ParcheClient --> ParcheSvc : GET /api/parches/user/{id}
ProfileClient --> ProfileSvc : GET /api/v1/users/{id}

@enduml
```

---

## 3. Diagrama de Despliegue

Infraestructura desplegada en Azure Container Apps (según `Dockerfile` y `deploy-env.sh`).

```plantuml
@startuml EstadisticasEci_Despliegue
title Estadisticas_Eci - Diagrama de Despliegue (Azure Container Apps)

skinparam nodeStyle rectangle

cloud "Azure" {

  node "Azure Container Registry (ACR)" as ACR

  node "Container Apps Environment" as Env {

    node "Container App: estadisticas-eci" as EstApp {
      artifact "estadisticas-eci:latest\n(eclipse-temurin:21-jre)" as EstImage
      component "Spring Boot App\n(puerto 8082)" as EstRuntime
      EstImage --> EstRuntime
    }

    node "Container App: cybersapiens-pg\n(postgres:15-alpine, compartido)" as PgApp {
      database "estadisticas_db (prod)" as DBProd
      database "estadisticas_db_qa (qa)" as DBQa
    }
  }

  node "Container App: profile-service\n(alphaeci-profile-service-prod)" as ProfileApp
  node "Container App: gamification-service" as GamifApp
}

node "GitHub Actions" as CI {
  component "ci.yml\n(mvn clean test)" as CIBuild
  component "cd-qa.yml / cd-prod.yml\n(deploy-env.sh)" as CIDeploy
}

node "EventService" as EventSvc
node "Parches-Service" as ParcheSvc

CI --> ACR : build & push imagen Docker
ACR --> EstApp : pull de imagen
CIDeploy --> Env : despliega/actualiza container app

EstRuntime --> DBProd : JDBC (5432, TCP ingress)
EstRuntime --> DBQa : JDBC (5432, TCP ingress, QA)

EstRuntime --> GamifApp : HTTPS GET /users/{id}/monas
EstRuntime --> EventSvc : HTTPS GET /events/agenda
EstRuntime --> ParcheSvc : HTTPS GET /api/parches/user/{id}
EstRuntime --> ProfileApp : HTTPS GET /api/v1/users/{id}

note right of EstApp
  min-replicas: 0
  max-replicas: 2
  (scale-to-zero)
  Ingress externo, puerto 8082
end note

@enduml
```

---

## 4. Diagrama de Datos

Modelo entidad-relación de las tablas persistidas (`activity_records`, `mentorships`, `welfare_checkins`). Es un esquema plano de reporting: no existen claves foráneas formales entre las tablas; el vínculo con usuarios/programas es por strings (`user_id`, `program_code`), no por relaciones JPA.

```plantuml
@startuml EstadisticasEci_Datos
title Estadisticas_Eci - Diagrama de Datos (Modelo Entidad-Relación)

entity "activity_records" as activity {
  * id : BIGINT <<PK>>
  --
  * user_id : VARCHAR
  * activity_type : VARCHAR
  .. ("CONNECTION", "PATCH", "EVENT") ..
  academic_program : VARCHAR (nullable)
  * created_at : TIMESTAMP
}

entity "mentorships" as mentorships {
  * id : BIGINT <<PK>>
  --
  * mentor_id : VARCHAR
  * mentee_id : VARCHAR
  program_code : VARCHAR (nullable)
  program_name : VARCHAR (nullable)
  * started_at : DATE
}

entity "welfare_checkins" as welfare {
  * id : BIGINT <<PK>>
  --
  * user_id : VARCHAR
  * checkin_date : DATE
  intervention_type : VARCHAR (nullable)
  .. ("COUNSELING", "WORKSHOP", null) ..
  academic_program : VARCHAR (nullable)
}

activity .[hidden]. mentorships
mentorships .[hidden]. welfare

note bottom of activity
  Sin FK formales entre tablas.
  Relación lógica por "user_id" /
  "academic_program" (strings),
  no por @ManyToOne / @JoinColumn.
  Esquema plano orientado a reporting,
  no OLTP relacional.
end note

note top of mentorships
  Fuente de "Mentorships by Program":
  agrupado por (program_code, program_name)
  en el rango de fechas solicitado.
end note

note top of welfare
  Fuente de "Weekly Welfare":
  agregado en memoria por semana ISO
  (lunes a domingo), no en SQL.
end note

@enduml
```
