# Arquitectura de Estadisticas_Eci

## 1. Resumen

`Estadisticas_Eci` es el microservicio de estadísticas e integración institucional de la plataforma CyberSapiens. Cumple una doble función:

1. **Métricas administrativas/institucionales**: expone indicadores agregados y anonimizados (tasa de nuevas conexiones, inactividad de primer semestre, mentorías por programa, bienestar semanal) para personal de administración y bienestar.
2. **BFF (Backend For Frontend) de estadísticas personales**: agrega en tiempo real datos de otros microservicios (Gamification, Event, Parches, Profile) para construir un perfil de estadísticas de un usuario específico.

Construido con **arquitectura hexagonal (Ports & Adapters)**, con una capa de dominio (`domain/`) completamente libre de dependencias de framework.

## 2. Stack Tecnológico

| Aspecto | Valor |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Build tool | Maven |
| Artefacto | `com.cybersapiens:estadisticas-eci:0.0.1-SNAPSHOT` |
| Capa web | `spring-boot-starter-web` (MVC/Servlet, controladores reales) + `spring-boot-starter-webflux` (usado únicamente para el `WebClient` reactivo de llamadas salientes, invocado en estilo bloqueante con `.block()`) |
| Validación | `spring-boot-starter-validation` (Jakarta) |
| Persistencia | `spring-boot-starter-data-jpa` |
| Base de datos producción | PostgreSQL (`org.postgresql:postgresql`, runtime) |
| Base de datos dev/test | H2 en memoria (runtime, modo compatibilidad PostgreSQL) |
| Seguridad | `spring-boot-starter-security` |
| Exportación CSV | `com.opencsv:opencsv:5.9` |
| Documentación API | `springdoc-openapi-starter-webmvc-ui:2.6.0` (Swagger UI) |
| Testing | `spring-boot-starter-test`, `spring-security-test`, `com.squareup.okhttp3:mockwebserver` |
| Lombok | Presente como dependencia opcional (excluido del jar final), pero **no se usa** en el código — el proyecto prefiere records/POJOs planos |

## 3. Estructura de Carpetas

Arquitectura hexagonal estricta bajo `src/main/java/com/cybersapiens/estadisticaseci/`:

```
Estadisticas_Eci/
├── .github/workflows/
│   ├── ci.yml                    # Build + test en cada push/PR a cualquier rama
│   ├── cd-qa.yml                 # Deploy a QA en push a `develop`
│   └── cd-prod.yml               # Deploy a Prod en push a `main`/`master`
├── Dockerfile                    # Multi-stage: Maven builder -> JRE runtime
├── deploy-env.sh                 # Script de despliegue a Azure Container Apps
├── pom.xml
├── README.md                     # Documentación existente detallada
└── src/
    ├── main/java/com/cybersapiens/estadisticaseci/
    │   ├── EstadisticasEciApplication.java        # Entry point Spring Boot
    │   ├── domain/                                 # NÚCLEO — sin dependencias de framework
    │   │   ├── model/                              # Entidades de negocio como Java records
    │   │   │   ├── AcademicProgram.java
    │   │   │   ├── DateRange.java
    │   │   │   ├── InactiveFirstSemester.java
    │   │   │   ├── IntegrationMetrics.java
    │   │   │   ├── MentorshipByProgram.java
    │   │   │   ├── NewConnectionRate.java
    │   │   │   ├── UserPersonalStats.java          # agregado con records anidados
    │   │   │   └── WeeklyWelfare.java
    │   │   ├── port/
    │   │   │   ├── in/                             # Interfaces de caso de uso
    │   │   │   │   ├── GetIntegrationMetricsUseCase.java
    │   │   │   │   └── GetUserPersonalStatsUseCase.java
    │   │   │   └── out/                            # Interfaces de repositorio/servicio externo
    │   │   │       ├── ActivityDataPort.java
    │   │   │       ├── ExternalEventPort.java
    │   │   │       ├── ExternalGamificationPort.java
    │   │   │       ├── ExternalParchePort.java
    │   │   │       ├── ExternalProfilePort.java
    │   │   │       ├── MentorshipDataPort.java
    │   │   │       └── WelfareDataPort.java
    │   │   └── service/                            # Lógica de negocio pura
    │   │       ├── AnonymizationService.java        # Guardia de detección de PII
    │   │       └── MetricsCalculationService.java   # Agrega métricas desde los ports
    │   ├── application/
    │   │   └── handler/                             # Implementaciones de caso de uso (orquestadores)
    │   │       ├── GetIntegrationMetricsHandler.java
    │   │       └── GetUserPersonalStatsHandler.java
    │   ├── infrastructure/                          # Adaptadores — código específico de framework
    │   │   ├── config/
    │   │   │   ├── DomainServiceConfig.java          # Cablea beans de dominio/aplicación (DI manual)
    │   │   │   ├── SecurityConfig.java                # Cadena de filtros de Spring Security
    │   │   │   └── WebClientConfig.java               # Beans de WebClient por microservicio externo
    │   │   ├── persistence/
    │   │   │   ├── entity/                            # Entidades JPA
    │   │   │   │   ├── ActivityRecordEntity.java
    │   │   │   │   ├── MentorshipEntity.java
    │   │   │   │   └── WelfareCheckinEntity.java
    │   │   │   └── adapter/                            # Repositorios JPA + adaptadores de ports
    │   │   │       ├── ActivityDataJpaAdapter.java
    │   │   │       ├── ActivityRecordJpaRepository.java
    │   │   │       ├── MentorshipDataJpaAdapter.java
    │   │   │       ├── MentorshipJpaRepository.java
    │   │   │       ├── WelfareCheckinJpaRepository.java
    │   │   │       └── WelfareDataJpaAdapter.java
    │   │   ├── security/
    │   │   │   ├── JwtAuthFilter.java                  # OncePerRequestFilter, construye el Authentication de Spring Security
    │   │   │   ├── JwtPayload.java                     # record(subject, roles)
    │   │   │   └── JwtValidator.java                   # Decodificación/verificación manual de JWT HMAC-SHA256 (sin librería externa)
    │   │   └── web/
    │   │       ├── MetricsController.java              # Único controlador REST
    │   │       ├── client/                             # Adaptadores HTTP salientes a otros microservicios
    │   │       │   ├── EventServiceClient.java
    │   │       │   ├── GamificationServiceClient.java
    │   │       │   ├── ParcheServiceClient.java
    │   │       │   ├── ProfileServiceClient.java
    │   │       │   └── dto/                            # DTOs de respuesta externa
    │   │       │       ├── GamificationUserMonasResponse.java
    │   │       │       ├── ProfileUserResponse.java
    │   │       │       └── UserPersonalStatsResponse.java  # También actúa como DTO de respuesta + mapper fromDomain()
    │   │       ├── dto/
    │   │       │   ├── request/MetricsFilterRequest.java
    │   │       │   └── response/IntegrationMetricsResponse.java
    │   │       ├── handler/GlobalExceptionHandler.java  # @RestControllerAdvice
    │   │       └── mapper/
    │   │           ├── MetricsCsvSerializer.java        # domain -> CSV (OpenCSV)
    │   │           └── MetricsWebMapper.java             # DTO <-> domain
    │   └── shared/
    │       └── exception/NoDataFoundException.java      # Excepción transversal
    ├── main/resources/
    │   ├── application.yml            # Config por defecto (H2) + perfil `postgres`
    │   └── data.sql                   # Datos semilla para perfil H2 de desarrollo
    └── test/java/com/cybersapiens/estadisticaseci/    # Espeja la estructura de main (ver sección 12)
```

## 4. Arranque de la Aplicación

- **Clase principal:** `EstadisticasEciApplication`, `@SpringBootApplication`, `SpringApplication.run(...)`. El component-scan estándar cubre `application`/`infrastructure`, pero **`domain` no está anotado** (se mantiene libre de framework); en su lugar, **`DomainServiceConfig`** (en `infrastructure/config`) instancia manualmente como `@Bean` los objetos de dominio/aplicación (`MetricsCalculationService`, `AnonymizationService`, `GetIntegrationMetricsHandler`, `GetUserPersonalStatsHandler`), cableándolos con los beans adaptadores gestionados por Spring (adaptadores JPA, clientes HTTP) que implementan los ports `port.out` del dominio.
- **Puerto del servidor:** 8082 (`application.yml`, también `EXPOSE 8082` en Dockerfile y `--target-port 8082` en `deploy-env.sh`).
- **Flujo de arranque:** Spring Boot autoconfigura el contenedor servlet embebido Tomcat (spring-web) más un bean `WebClient` de WebFlux (usado solo como cliente HTTP, sin controladores reactivos). Con el perfil por defecto se crea una BD H2 en memoria (`ddl-auto: create-drop`) sembrada vía `data.sql` (`defer-datasource-initialization: true`, `spring.sql.init.mode: always`).

## 5. Endpoints REST

Único controlador: `MetricsController` (`infrastructure/web/MetricsController.java`), base path `/api/v1/metrics`.

### `GET /api/v1/metrics/integration`
- **Autorización:** `@PreAuthorize("hasAnyRole('ADMIN', 'WELLBEING')")`
- **Query params** (`@Valid MetricsFilterRequest filter`):
  - `dateFrom` (LocalDate ISO-8601, `@NotNull`)
  - `dateTo` (LocalDate ISO-8601, `@NotNull`)
  - `academicProgram` (String, opcional)
  - `format` (String, default `json`) — `json` o `csv`
- **Propósito:** devuelve métricas institucionales agregadas y anonimizadas (tasa de nuevas conexiones, inactividad de primer semestre, mentorías por programa, bienestar semanal) para el rango de fechas/programa filtrado.
- **Respuesta:** `IntegrationMetricsResponse` (JSON) o `text/csv` (con `Content-Disposition: attachment; filename=integration-metrics.csv`) si `format=csv`.
- **Comportamiento de error:** `NoDataFoundException` → capturada globalmente, retorna HTTP 200 con métricas en cero/vacías (respuesta "sin datos" controlada, no un error); fallos de validación → 400; rol incorrecto/ausente → 403.

### `GET /api/v1/metrics/user/{userId}`
- **Autorización:** `@PreAuthorize("authentication.name == #userId or hasRole('ADMIN') or hasRole('WELLBEING')")` — control de auto-acceso: un usuario solo puede consultar sus propias estadísticas salvo que tenga rol privilegiado.
- **Path param:** `userId` (String)
- **Propósito:** endpoint tipo BFF que hace fan-out en paralelo a 4 microservicios externos (Gamification, Event, Parche, Profile) y devuelve un payload combinado de estadísticas personales. Ante fallo de un upstream, la sección correspondiente es `null` (degradación controlada), manteniendo HTTP 200.
- **Respuesta:** `UserPersonalStatsResponse`.

**Nota de seguridad en el matcher** (`SecurityConfig`): solo `GET /api/v1/metrics/**` se fuerza a `.authenticated()`; `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html` son `permitAll()`; `anyRequest().permitAll()` es el fallback general — es decir, cualquier ruta no-GET o distinta queda abierta por defecto (actualmente no existen otras rutas además de las dos GET y Swagger, pero es un default permisivo digno de señalar).

Swagger/OpenAPI habilitado en `/swagger-ui.html` y `/v3/api-docs`.

## 6. Modelo de Dominio y Esquema de Base de Datos

### Modelos de dominio (records Java puros, `domain/model/`)
- `AcademicProgram(String code)` — valida código no vacío.
- `DateRange(LocalDate from, LocalDate to)` — valida que `from` no sea posterior a `to`.
- `NewConnectionRate(double percentage, long connectedCount, long totalCount)` — valida 0–100%.
- `InactiveFirstSemester(double percentage, long inactiveCount, long totalCount)` — valida 0–100%.
- `MentorshipByProgram(String programCode, String programName, long mentorshipCount)`.
- `WeeklyWelfare(LocalDate weekStart, long checkinCount, long interventionCount, long uniqueStudents)`.
- `IntegrationMetrics(NewConnectionRate, InactiveFirstSemester, List<MentorshipByProgram>, List<WeeklyWelfare>)` — raíz del agregado de métricas administrativas.
- `UserPersonalStats(String userId, GamificationStats, EventStats, ParcheStats, ProfileStats)`, con records anidados:
  - `GamificationStats(int totalXp, int totalMonasUnlocked, int monasInProgress, int monasLocked, double completionPercentage, List<MonaEntry> monas)`
  - `MonaEntry(String code, String name, String rarity, String status, String unlockedAt)`
  - `EventStats(int totalAttended, int upcomingEvents, int totalEvents, List<String> eventIds)`
  - `ParcheStats(int totalJoined, int activeParches)`
  - `ProfileStats(int xp, int level, boolean isActive, String career, Integer semester)`

### Entidades JPA / Esquema de BD (`infrastructure/persistence/entity/`)

**Tabla `activity_records`** (`ActivityRecordEntity`)
| Columna | Tipo | Notas |
|---|---|---|
| id | Long (IDENTITY) | PK |
| user_id | String | not null |
| activity_type | String | not null; valores: `CONNECTION`, `PATCH`, `EVENT` |
| academic_program | String | nullable |
| created_at | LocalDateTime | not null |

**Tabla `mentorships`** (`MentorshipEntity`)
| Columna | Tipo | Notas |
|---|---|---|
| id | Long (IDENTITY) | PK |
| mentor_id | String | not null |
| mentee_id | String | not null |
| program_code | String | nullable |
| program_name | String | nullable |
| started_at | LocalDate | not null |

**Tabla `welfare_checkins`** (`WelfareCheckinEntity`)
| Columna | Tipo | Notas |
|---|---|---|
| id | Long (IDENTITY) | PK |
| user_id | String | not null |
| checkin_date | LocalDate | not null |
| intervention_type | String | nullable (ej. `COUNSELING`, `WORKSHOP`, o NULL = check-in simple) |
| academic_program | String | nullable |

No hay relaciones FK formales entre entidades (todo el vínculo es por strings débilmente tipados `user_id`/`program_code`, sin `@ManyToOne`/`@JoinColumn`) — es un esquema de reporting plano y denormalizado a propósito, no un esquema OLTP relacional completo.

**Datos semilla** (`src/main/resources/data.sql`): puebla las tres tablas con datos de ejemplo en 4 programas académicos (`ING-COMP`, `MED`, `DER`, `ADM`) — 10 registros de actividad, 4 mentorías, 8 check-ins de bienestar entre usuarios `user-001`..`user-007`.

## 7. Configuración de Base de Datos

Definida en `src/main/resources/application.yml`:
- **Perfil por defecto/dev:** H2 en memoria, `jdbc:h2:mem:estadisticas;DB_CLOSE_DELAY=-1;MODE=PostgreSQL` (modo compatibilidad PostgreSQL), usuario `sa` sin contraseña, `ddl-auto: create-drop`, `show-sql: true`, datos semilla siempre cargados.
- **Perfil `postgres`** (activado vía `SPRING_PROFILES_ACTIVE=postgres`): PostgreSQL, URL desde `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/estadisticas_eci`), usuario desde `DB_USER` (default `postgres`), contraseña desde `DB_PASSWORD` (default `postgres`), `ddl-auto: update`, `show-sql: false`, esquema `public`, sin datos semilla.
- En despliegue (`deploy-env.sh`), se crea una base de datos Postgres dedicada por ambiente dentro de un Container App Azure compartido (`cybersapiens-pg`): `estadisticas_db` para prod, `estadisticas_db_qa` para QA.

## 8. Capa de Servicios / Lógica de Negocio

- **`MetricsCalculationService`** (`domain/service/`) — servicio de dominio puro, sin anotaciones Spring. Orquesta 3 ports de salida (`ActivityDataPort`, `MentorshipDataPort`, `WelfareDataPort`) para ensamblar el agregado `IntegrationMetrics`. Composición síncrona sencilla (a diferencia del handler de estadísticas personales, aquí no hay asincronía).
- **`AnonymizationService`** (`domain/service/`) — guardia de detección de PII. Escanea los campos string de `mentorshipsByProgram` (`programCode`, `programName`) usando dos patrones regex: patrón de email (`[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}`) y patrón de ID de usuario (`user-\d+`, case-insensitive). Lanza `SecurityException` si alguno coincide, registrando un warning. Actualmente solo inspecciona los campos string de mentorías — no escanea todos los campos de las métricas (aunque los campos numéricos obviamente no pueden contener PII, si se añadieran nuevos campos string a `IntegrationMetrics` no quedarían cubiertos automáticamente).
- **`GetIntegrationMetricsHandler`** (`application/handler/`) — implementa `GetIntegrationMetricsUseCase`; llama a `MetricsCalculationService.calculate()`, lanza `NoDataFoundException` si el resultado es nulo, y pasa el resultado por `AnonymizationService.ensureNoPii()` antes de devolverlo.
- **`GetUserPersonalStatsHandler`** (`application/handler/`) — implementa `GetUserPersonalStatsUseCase`; hace fan-out a los 4 ports externos (gamification, event, parche, profile) usando `CompletableFuture.supplyAsync(...).exceptionally(ex -> null)` en paralelo sobre el ForkJoinPool común, luego `CompletableFuture.allOf(...).join()` antes de ensamblar el `UserPersonalStats` final. Este es el mecanismo central de "degradación controlada": cualquier fallo individual de un upstream produce `null` para esa sección sin fallar la petición completa.

## 9. Mensajería / Integración de Eventos

- **No hay uso de ningún broker de mensajes (Kafka/RabbitMQ/Azure Service Bus) en el código Java** — sin productores/consumidores, sin `@RabbitListener`/`@KafkaListener`, sin dependencias de cliente AMQP/Kafka en el `pom.xml`.
- Sin embargo, `deploy-env.sh` **sí aprovisiona un container app de RabbitMQ** (`rabbitmq`/`rabbitmq-$ENV`) en Azure con ingress TCP en el puerto 5672 — esto parece ser **infraestructura provisionada para la plataforma CyberSapiens en general** (probablemente usada por `GamificationService` u otros microservicios del ecosistema), no consumida directamente por este servicio. No hay variable de entorno ni cableado de cliente RabbitMQ en el código propio de `Estadisticas_Eci`.
- Toda la comunicación entre servicios en este repositorio es exclusivamente **REST síncrono sobre HTTP** vía Spring `WebClient` (ver sección 10).

## 10. Autenticación / Autorización

- **Mecanismo:** JWT autocontenido con firma **HMAC-SHA256**, implementado a mano (sin librerías como `jjwt`/`nimbus-jwt` — codificado en `JwtValidator.java`).
  - `JwtAuthFilter` (`infrastructure/security/`) — extiende `OncePerRequestFilter`, registrado con `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`. Extrae `Bearer <token>` del header `Authorization`, valida vía `JwtValidator.validate(token, jwtSecret)`, y en caso de éxito construye un `UsernamePasswordAuthenticationToken(payload.subject(), null, authorities)` con autoridades `ROLE_<role>` por cada rol en el claim `roles` del JWT. Ante cualquier excepción, limpia silenciosamente el contexto de seguridad (queda no autenticado) en vez de rechazar la petición directamente — la autorización falla después vía `@PreAuthorize`.
  - `JwtValidator` — separa el token en 3 partes por punto, recalcula HMAC-SHA256 sobre `header.payload` con el secreto configurado, compara contra la firma del token, y luego decodifica Base64URL y parsea con Jackson el JSON del payload para extraer los claims `sub` (subject) y `roles` (array) en un record `JwtPayload`. **No se valida el claim de expiración (`exp`)** en el código visible — la expiración del token actualmente no se verifica.
  - **Secreto:** propiedad `jwt.secret`, default `dev-secret-key-for-local-testing-only` — no se observa que `deploy-env.sh` configure `JWT_SECRET`/`jwt.secret` como variable de entorno para QA/prod, lo cual es una posible brecha si no se configura por otro medio (producción podría estar usando el default de desarrollo).
- **Autorización:** a nivel de método vía `@EnableMethodSecurity` + `@PreAuthorize`:
  - `/api/v1/metrics/integration`: `hasAnyRole('ADMIN', 'WELLBEING')`
  - `/api/v1/metrics/user/{userId}`: `authentication.name == #userId or hasRole('ADMIN') or hasRole('WELLBEING')` (patrón auto-acceso-o-rol-privilegiado)
- **Sesión:** stateless (`SessionCreationPolicy.STATELESS`), CSRF deshabilitado (apropiado para una API basada en tokens sin estado).
- **Manejo de errores:** `AccessDeniedException` → 403 con `{"error": "Forbidden", "message": "Rol no autorizado"}`.

## 11. Integraciones con Otros Microservicios

Consumidas vía `WebClient` reactivo (con llamadas `.block()` para integrarse al código imperativo del controlador — uso bloqueante de un cliente reactivo):

| Servicio | Clase Cliente | Endpoint Llamado | Propósito |
|---|---|---|---|
| GamificationService | `GamificationServiceClient` | `GET /api/v1/gamification/users/{userId}/monas` | Monas (logros), XP, progreso → mapeado desde `GamificationUserMonasResponse` |
| EventService | `EventServiceClient` | `GET /events/agenda?userId={userId}` | Lista de IDs de eventos asistidos, mapeado a `EventStats` |
| Parches-Service | `ParcheServiceClient` | `GET /api/parches/user/{userId}` | Lista de membresías de parches (`List<Map<String,Object>>` sin tipar), cuenta total y activos (`status == "ACTIVE"`) |
| profile-service | `ProfileServiceClient` | `GET /api/v1/users/{userId}` | Carrera, semestre, XP, nivel, estado activo, mapeado desde `ProfileUserResponse` |

URLs base configuradas en `WebClientConfig`, cada una inyectada desde propiedades de `application.yml` con fallback a variable de entorno:
- `SERVICES_GAMIFICATION_URL` (default `http://localhost:8082`)
- `SERVICES_EVENT_URL` (default `http://localhost:8081`)
- `SERVICES_PARCHES_URL` (default `http://localhost:8080`)
- `SERVICES_PROFILE_URL` (default apunta a un Azure Container App en vivo: `https://alphaeci-profile-service-prod.icycoast-fc5305af.eastus.azurecontainerapps.io`)

**Nota:** el `README.md` documenta nombres de variables de entorno ligeramente distintos (`GAMIFICATION_SERVICE_URL`, `EVENT_SERVICE_URL`, `PARCHES_SERVICE_URL`, `PROFILE_SERVICE_URL`) frente a los realmente usados en `application.yml`/`deploy-env.sh` (prefijo `SERVICES_*`) — vale la pena reconciliar esta discrepancia.

Cada cliente devuelve de forma controlada un objeto de estadísticas vacío/por defecto cuando la respuesta upstream es `null` (ej. `ProfileServiceClient` retorna `ProfileStats(0, 1, false, null, null)` como fallback), y los errores upstream (5xx, timeouts) se propagan como excepciones capturadas por `.exceptionally(ex -> null)` en `GetUserPersonalStatsHandler`.

## 12. Configuración

- **`pom.xml`** — descriptor de build Maven.
- **`src/main/resources/application.yml`** — puerto 8082, rutas springdoc/swagger, datasource (H2 default + perfil `postgres`), `ddl-auto` por perfil, URLs de servicios, `jwt.secret`.
- **`src/main/resources/data.sql`** — datos semilla solo de desarrollo (solo se carga con el perfil H2 por defecto).
- **`Dockerfile`** — build en 2 etapas: `maven:3.9-eclipse-temurin-21` (builder: `mvn dependency:go-offline`, `mvn clean package -DskipTests`) → `eclipse-temurin:21-jre` (runtime, copia el jar), `EXPOSE 8082`, `ENTRYPOINT ["java","-jar","app.jar"]`.
- **`deploy-env.sh`** — script de despliegue a Azure parametrizado por `qa`/`prod`. Aprovisiona/reutiliza: Resource Group, ACR, Container Apps Environment, un container app Postgres compartido (`cybersapiens-pg`, imagen `postgres:15-alpine`, ingress TCP 5432), BD dedicada por ambiente (`estadisticas_db` o `estadisticas_db_$ENV`), un container app RabbitMQ por ambiente, y despliega `estadisticas-eci`/`estadisticas-eci-$ENV` con variables de entorno: `SPRING_PROFILES_ACTIVE=postgres`, `SPRING_DATASOURCE_URL`, `DB_USER`, `DB_PASSWORD`, `SERVICES_GAMIFICATION_URL` (derivada del FQDN del gamification-service desplegado), `PROFILE_SERVICE_URL`. Ingress externo, puerto 8082, `min-replicas 0 max-replicas 2` (scale-to-zero).
- **`.github/workflows/ci.yml`** — en cada push/PR a cualquier rama; JDK 21 Temurin con caché Maven; `mvn -B clean test`.
- **`.github/workflows/cd-qa.yml`** — en push a `develop`; login Azure vía secreto `AZURE_CREDENTIALS`; ejecuta `deploy-env.sh qa` con secreto `PG_PASS_QA` y variables `ACR_NAME`/`AZURE_LOCATION`.
- **`.github/workflows/cd-prod.yml`** — en push a `main`/`master`; mismo patrón con `PG_PASS_PROD`.
- **Inventario de secretos/variables de entorno (solo nombres):** `AZURE_CREDENTIALS`, `PG_PASS_QA`, `PG_PASS_PROD`, `ACR_NAME`, `AZURE_LOCATION`, `AZURE_RESOURCE_GROUP`, `AZURE_ACA_ENV`, `AZURE_PG_NAME`, `PG_PASSWORD`, `DB_USER`, `DB_PASSWORD`, `SPRING_DATASOURCE_URL`, `SPRING_PROFILES_ACTIVE`, `SERVICES_GAMIFICATION_URL`, `SERVICES_EVENT_URL`, `SERVICES_PARCHES_URL`, `SERVICES_PROFILE_URL`, `PROFILE_SERVICE_URL`, `jwt.secret`.
- No existe archivo `.env`/`.env.example` en el repositorio.

## 13. Testing

Suite bajo `src/test/java/com/cybersapiens/estadisticaseci/`, reflejando la estructura de `main` (README menciona 33 tests en total):

- `EstadisticasEciApplicationTests` — smoke test `@SpringBootTest` (`contextLoads()`).
- `MetricsControllerTest` — `@WebMvcTest(MetricsController.class)` + `@Import({MetricsWebMapper.class, MetricsCsvSerializer.class})`, `@MockBean` para los casos de uso, `@WithMockUser` para simular roles (ADMIN/WELLBEING/USER). Cubre: respuesta JSON, respuesta CSV, fallback de datos vacíos, control de acceso por rol, endpoint de estadísticas personales (propio usuario / acceso admin / datos parciales por fallo upstream).
- `GetUserPersonalStatsHandlerTest` — basado en Mockito, prueba: todos los servicios disponibles, un servicio (gamification) falla, todos fallan, verifica invocación paralela de los 4 ports.
- `MetricsCalculationServiceTest` — Mockito, prueba ensamblaje del agregado con y sin filtro de programa académico.
- `AnonymizationServiceTest` — datos limpios pasan, email en nombre de programa lanza excepción, user-id en código de programa lanza excepción, lista de mentorías vacía pasa.
- `UserPersonalStatsResponseTest` — prueba la null-safety del mapper `fromDomain()`.
- Tests de adaptadores HTTP con `okhttp3.mockwebserver.MockWebServer` (ciclo real de request/response contra un servidor mock local): `EventServiceClientTest`, `GamificationServiceClientTest`, `ParcheServiceClientTest`, `ProfileServiceClientTest` — casos felices, listas vacías, error de servidor (500) → excepción.

**Frameworks:** JUnit 5, Mockito, AssertJ, `MockMvc` + `spring-security-test` (`@WithMockUser`), OkHttp `MockWebServer`. Ejecutado con `mvn test` (integrado en CI vía `mvn -B clean test`).

## 14. Documentación Existente

`README.md` es extenso y en gran parte autoritativo; ya cubre: overview del doble propósito, diagrama de arquitectura, tabla de integración de microservicios, ejemplos JSON completos de request/response para ambos endpoints, tabla de respuestas de error, comparación de perfiles (dev/H2 vs `postgres`), y resumen de testing (33 tests).

**Discrepancias a reconciliar:** (a) nombres de variables de entorno (`SERVICES_*` vs `*_SERVICE_URL`), (b) no menciona el aprovisionamiento de RabbitMQ en `deploy-env.sh` (que no se usa realmente en el código de este servicio, probablemente correcto de omitir salvo que se documente infraestructura).

## 15. Patrones de Diseño

- **Arquitectura Hexagonal / Ports & Adapters** — patrón dominante; `domain` sin dependencias de framework (verificado: ninguna anotación Spring en `domain/*`), dependiendo solo de clases del JDK.
- **Inyección de Dependencias con cableado manual** — `DomainServiceConfig` expone como `@Bean` los objetos de dominio en lugar de anotarlos con `@Component`/`@Service`, manteniendo el dominio puro/agnóstico de framework.
- **Repository Pattern** — repositorios Spring Data JPA detrás de adaptadores que implementan los ports de dominio.
- **Adapter Pattern** — adaptadores JPA (`*JpaAdapter`) y clientes HTTP (`*ServiceClient`) traducen entre representaciones externas (entidades DB / DTOs JSON) y modelos de dominio.
- **DTO / Mapper** — separación explícita entre records de dominio y DTOs web (`MetricsWebMapper`, `UserPersonalStatsResponse.fromDomain(...)`), evitando exponer objetos de dominio directamente por HTTP.
- **Use Case / Command Handler** — interfaces `*UseCase` + implementaciones de un solo método, estilo ligero "una clase por caso de uso" (sin CQRS completo, solo lado de lectura/consulta).
- **Degradación controlada / fan-out tipo bulkhead** — `CompletableFuture` + `.exceptionally(ex -> null)` aísla fallos por dependencia upstream en `GetUserPersonalStatsHandler`.
- **Global Exception Handling** — `@RestControllerAdvice` centraliza el mapeo de excepciones a estados HTTP.
- **Value objects inmutables** — casi todo el modelo de dominio son Java `record`s con validación en constructor compacto (`AcademicProgram`, `DateRange`, `NewConnectionRate`, `InactiveFirstSemester`), forzando invariantes en el momento de construcción.
- **Filtro de seguridad / Guard pattern** — `JwtAuthFilter` custom + expresiones SpEL declarativas `@PreAuthorize` para autorización fina consciente del propietario del recurso.
- **Guardia de anonimización de PII** — `AnonymizationService` actúa como capa defensiva de validación antes de que los datos salgan del sistema, una forma de sanitización de salida forzada en la capa de dominio ("seguridad como concern de dominio").

## 16. Conceptos de Dominio: Estadísticas y Analítica

Este servicio calcula dos categorías de estadísticas:

### A. Métricas Institucionales/Administrativas (agregadas, anonimizadas, históricas)

1. **Tasa de Nuevas Conexiones** (`NewConnectionRate`)
   - Definición: porcentaje de *todos los usuarios distintos activos en el rango de fechas* (cualquier actividad) que realizaron una actividad `CONNECTION` o `PATCH` dentro del rango.
   - Lógica SQL: `connected` = `COUNT(DISTINCT userId)` donde `activityType IN ('CONNECTION','PATCH')` y fecha en rango (más filtro opcional de programa); `total` = `COUNT(DISTINCT userId)` mismo filtro sin restricción de tipo de actividad (incluye `EVENT`); `percentage = connected / total * 100` (0 si `total == 0`).
   - Significado de negocio: mide el "engagement temprano" institucional — qué fracción de estudiantes activos están generando nuevas conexiones sociales/de networking (vs. solo asistir a eventos) — indicador de salud de integración/onboarding.

2. **Inactividad de Primer Semestre** (`InactiveFirstSemester`)
   - Definición: el complemento de la tasa de conexión — `inactive = total - connected`, `percentage = inactive/total*100`.
   - **Nota importante:** pese al nombre "Primer Semestre", la implementación actual **no filtra por semestre/cohorte de matrícula** — se calcula puramente desde la misma tabla de actividad filtrada por fecha/programa que la métrica anterior. Esto parece un desajuste entre el nombre/intención de negocio y la implementación real (probablemente pensada para estudiantes de primer semestre específicamente, pero tratando "primer semestre" como sinónimo del rango de fechas dado) — vale la pena señalarlo/validarlo con el equipo de producto.

3. **Mentorías por Programa** (`MentorshipByProgram`)
   - JPQL: `SELECT new MentorshipByProgram(m.programCode, m.programName, COUNT(m)) FROM MentorshipEntity m WHERE m.startedAt BETWEEN :from AND :to AND (:program IS NULL OR m.programCode = :program) GROUP BY m.programCode, m.programName ORDER BY COUNT(m) DESC`.
   - Significado de negocio: distribución del número de relaciones mentor-mentee por programa académico en el rango de fechas, ordenado descendente — muestra qué programas tienen más actividad de mentoría.

4. **Bienestar Semanal** (`WeeklyWelfare`)
   - Obtiene todos los `WelfareCheckinEntity` en el filtro fecha/programa, y los agrupa **en código de aplicación** (no en SQL) en semanas ISO que inician en lunes, usando un `TreeMap<LocalDate, WeeklyAccumulator>` ordenado y una clase interna `WeeklyAccumulator` que rastrea:
     - `checkinCount` — total de check-ins de la semana.
     - `interventionCount` — check-ins donde `interventionType` no es nulo/vacío (intervención activa como `COUNSELING` o `WORKSHOP`, vs. un check-in pasivo).
     - `uniqueStudents` — un `HashSet<String>` de `userId`s distintos esa semana.
   - Significado de negocio: da al personal de bienestar un pulso semana a semana del engagement de bienestar estudiantil — volumen total, carga de intervenciones y alcance de estudiantes únicos — útil para detectar tendencias/picos que requieran atención.

Las cuatro métricas se calculan **frescas en cada request** desde queries JPA (sin caché, sin tablas materializadas/pre-agregadas), acotadas por el `DateRange` y `AcademicProgram` opcional suministrados por el caller, ensambladas en un único agregado `IntegrationMetrics` por `MetricsCalculationService`, y finalmente pasadas por `AnonymizationService.ensureNoPii()` antes de devolverse — este chequeo de PII re-escanea específicamente solo los strings de programa/código de mentoría en busca de emails o patrones `user-\d+` embebidos, lanzando una `SecurityException` dura (no una respuesta silenciosamente redactada) si se detecta una fuga, para prevenir exposición accidental de datos personales vía un código o nombre de programa mal formado o suministrado por el usuario.

La salida se ofrece en dos formatos: JSON (default) o CSV (OpenCSV) — el formato CSV concatena 4 secciones etiquetadas (Tasa de Nuevas Conexiones, Inactividad de Primer Semestre, Mentorías por Programa, Bienestar Semanal) en un solo archivo descargable, cada una con fila de encabezado, con filas placeholder "Sin datos" cuando una sección está vacía.

### B. Estadísticas Personales de Usuario (en tiempo real, por usuario, desde datos upstream en vivo — sin persistencia local)

Agregadas por `GetUserPersonalStatsHandler`, calculadas **no** desde la base de datos propia de este servicio sino desde llamadas en tiempo real a microservicios hermanos:

1. **Estadísticas de Gamificación** (`GamificationServiceClient`): XP total, total de monas desbloqueadas, monas en progreso, monas bloqueadas, y un **porcentaje de completitud** derivado = `totalUnlocked / (totalUnlocked + inProgress.size() + locked.size()) * 100`, redondeado a 2 decimales. También retorna una lista aplanada de todas las monas (desbloqueadas/en progreso/bloqueadas) etiquetadas con un campo `status` ("UNLOCKED"/"IN_PROGRESS"/"LOCKED") y, para las desbloqueadas, un timestamp `unlockedAt`.
2. **Estadísticas de Eventos** (`EventServiceClient`): llama a `/events/agenda?userId=`, que retorna una lista plana de IDs de eventos; `totalAttended` y `totalEvents` se fijan ambos a `eventIds.size()` y `upcomingEvents` está **hardcodeado a `0`** — lo que sugiere que el endpoint upstream actualmente retorna solo eventos asistidos, y "próximos eventos" no está realmente distinguido/calculado en este cliente (una simplificación/limitación digna de documentar).
3. **Estadísticas de Parches** (`ParcheServiceClient`): llama a la lista JSON cruda de membresías de parches, calcula `totalJoined` (tamaño de la lista) y `activeParches` (conteo donde el campo `status` es `"ACTIVE"`, sin distinguir mayúsculas) — se hace parseando a `List<Map<String,Object>>` en lugar de un DTO tipado.
4. **Estadísticas de Perfil** (`ProfileServiceClient`): carrera, semestre, nivel, XP, flag activo, con defaults defensivos (`level` por defecto 1, `xp` 0, `isActive` false) cuando los campos son nulos en la respuesta upstream o cuando toda la respuesta es nula.

Las 4 llamadas se ejecutan **concurrentemente** vía `CompletableFuture.supplyAsync(...)` (ForkJoinPool común) y se combinan con `CompletableFuture.allOf(...).join()`, y cualquier fallo individual se absorbe a `null` vía `.exceptionally(ex -> null)` — es decir, el endpoint de estadísticas personales siempre retorna HTTP 200 con tantos datos como se hayan podido recolectar ("degradación controlada"/patrón BFF de resultado parcial), nunca propagando una caída parcial upstream como un 5xx general.

## 17. Limitaciones y Gaps Conocidos

- La métrica **"Inactividad de Primer Semestre"** no filtra realmente por semestre/cohorte — es funcionalmente el complemento de la tasa de nuevas conexiones sobre el mismo rango de fechas. Podría no reflejar la intención de negocio original.
- Discrepancia entre los nombres de variables de entorno documentados en el `README.md` (`*_SERVICE_URL`) y los realmente usados en `application.yml`/`deploy-env.sh` (`SERVICES_*`).
- El JWT no valida expiración (`exp`) — un token nunca "vence" desde la perspectiva de este servicio.
- El secreto JWT (`jwt.secret`) por defecto es un valor de desarrollo (`dev-secret-key-for-local-testing-only`); no se observa que se configure explícitamente vía variable de entorno en el script de despliegue a QA/prod.
- El fallback de seguridad `anyRequest().permitAll()` en `SecurityConfig` deja abierta por defecto cualquier ruta que no sea `GET /api/v1/metrics/**` o Swagger — actualmente inocuo porque no hay otras rutas, pero requiere atención si se agregan nuevos endpoints.
- El endpoint de eventos (`EventServiceClient`) no distingue eventos futuros de pasados; `upcomingEvents` está hardcodeado a 0.
