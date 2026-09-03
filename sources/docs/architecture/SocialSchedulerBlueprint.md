```markdown
# Social Scheduler - Architecture Blueprint (Auto-Generated)
**Traceability Anchors:** [DOC-001] | [ARC-001]–[ARC-006] | [NFR-001]–[NFR-003] | [REQ-001]–[REQ-003] | [DAT-001]–[DAT-003]

## 1. System Context
High-level context diagram illustrating user, admin, API gateway, microservices, Kafka broker, external APIs, and database layer.

```mermaid
flowchart LR
    User((User)) -->|HTTPS Request| Gateway[API Gateway]
    Admin((Admin)) -->|HTTPS Request| Gateway
    Gateway -->|Route| user-service[User Service]
    Gateway -->|Route| schedule-service[Schedule Service]
    Gateway -->|Route| ai-service[AI/ML Service]
    Gateway -->|Route| rate-limit-service[Rate Limit Service]
    schedule-service -->|Publish Events| kafka[Kafka Topic: schedule.events]
    ai-service -->|Consume Events| kafka
    rate-limit-service -->|Rate Limit Check| redis[(Redis Token Bucket)]
    schedule-service -->|Persist/Query| postgres[(Cloud SQL Postgres)]
    ai-service -->|Persist/Query| postgres
    schedule-service -->|Integrate| facebook[Facebook Graph API]
    schedule-service -->|Integrate| instagram[Instagram Graph API]
    schedule-service -->|Integrate| tiktok[TikTok Open API]
    ai-service -->|Generate Content| openai[OpenAI Completion API]
```

## 2. Container Diagram
Internal technical components and runtime dependencies for each microservice.

```mermaid
graph TD
    subgraph user-service[user-service]
        JPA[JPA / Hibernate] -->|SQL Queries| PostgresDB
        OAuth2[OAuth2 / JWT Auth] -->|Auth Tokens| UserSession
        RedisCache[Redis Client] -->|Session/RateLimit| AppLogic
    end
    subgraph schedule-service[schedule-service]
        WebClient[WebClient / RestClient] -->|External Calls| facebook & instagram & tiktok
        KafkaProd[Kafka Producer] -->|Publish| kafka
        JPA2[JPA / Hibernate] -->|SQL Queries| PostgresDB
        Actuator[Spring Actuator] -->|Metrics/Health| Monitoring
    end
    subgraph ai-service[ai-service]
        OpenAIClient[OpenAI Client] -->|Completion API| OpenAI
        AnalyticsClient[Analytics Client] -->|Query| postgres
        CaffeineCache[Caffeine Cache] -->|Prompt Data| ServiceLayer
    end
    subgraph rate-limit-service[rate-limit-service]
        RedisStrategy[Redis Token Bucket] -->|Rate Check| RedisDB
        ServiceLayer[Service Layer] -->|Check Limit| RedisStrategy
        Actuator[Spring Actuator] -->|Metrics| Monitoring
    end
    kafka -->|Broker| kafka-broker
    PostgresDB -->|Instance| cloudsql
    redis -->|Cluster| redis-cluster
```

## 3. Component Diagram (Schedule Service)
Internal component structure focusing on request flow, state management, and integration points.

```mermaid
graph LR
    Controller[ScheduleController] -->|Validate Payload| Validator[SchedulePayloadValidator]
    Controller -->|Create Schedule| Service[ScheduleService]
    Service -->|Persist| Repository[ScheduleRepository]
    Service -->|Dispatch Event| KafkaProducer[Kafka Producer]
    Service -->|Rate Limit Check| RateLimiter[RateLimiterService]
    Validator -->|Apply Constraints| Jakarta[Jakarta Validation]
    RateLimiter -->|Redis Check| Redis[Redis Token Bucket]
    KafkaProducer -->|Publish| kafka
    Repository -->|SQL Queries| Postgres
```

## 4. Sequence Diagrams

### 4.1 Publishing Schedule Flow
End-to-end flow from user request to social platform publication.

```mermaid
sequenceDiagram
    participant User
    participant Gateway as API-GW
    participant Sched as ScheduleSvc
    participant Kafka as Kafka
    participant Intg as IntegrationSvc
    participant FB as Facebook
    participant IG as Instagram
    participant TT as TikTok
    
    User->>Gateway: POST /api/v1/schedules
    Gateway->>Sched: Authenticate JWT [EXC-002]
    Sched->>Repository: Save Schedule [REQ-001]
    Sched->>Kafka: Publish schedule.event
    Kafka->>Intg: Route to Adapter
    Intg->>FB: POST Graph API
    Intg->>IG: POST Graph API
    Intg->>TT: POST OpenAPI
    FB-->>Intg: 200 OK
    IG-->>Intg: 200 OK
    TT-->>Intg: 200 OK
    Intg->>Kafka: Publish executed event
    Kafka->>Sched: Update Status SENT
    Sched->>Repository: Update status
    Sched-->>Gateway: 201 Created
    Gateway-->>User: Response
```

### 4.2 AI Recommendation Flow
Flow for AI-driven content generation with fallback orchestration.

```mermaid
sequenceDiagram
    participant User
    participant Gateway as API-GW
    participant AI as AISvc
    participant Open as OpenAI
    participant Cache as Caffeine Cache
    
    User->>Gateway: POST /api/v1/ai/recommendations
    Gateway->>AI: Authenticate & Route [ARC-005]
    AI->>Cache: Check Prompt History
    alt Cache Hit
        Cache-->>AI: Return Cached Prompt
    else Cache Miss
        AI->>Postgres: Query performance_metrics [DAT-002]
        Postgres-->>AI: Return metrics
        AI->>OpenAI: Generate Completion [EXC-003]
        Open-->>AI: Content Response
        AI->>Cache: Store Result [EXC-004]
    end
    AI-->>Gateway: Recommendation Response
    Gateway-->>User: 200 OK with content
```

## 5. RBAC Role Mapping
Matrix mapping the four defined roles [ARC-001] Admin, [ARC-002] User, [ARC-003] Scheduler, [ARC-004] Analyst to specific permission scopes and API access levels.

| Role ID | Role Name | Description | API Access Scope | Kafka Topic Access |
| :--- | :--- | :--- | :--- | :--- |
| [ARC-001] | Admin | Full system administration, tenant management, emergency reset | All endpoints (`/api/v1/**`), Admin-only actions (`/rate-limits/reset`) | All topics including `metrics.collected`, `auth.token.refreshed` |
| [ARC-002] | User | Standard user operations, create/my schedules, view recommendations | `/api/v1/schedules` (POST, GET own), `/api/v1/ai/recommendations` | `schedule.created`, `ai.recommendation.generated` |
| [ARC-003] | Scheduler | Schedule creation, status updates, platform integration | `/api/v1/schedules` (PUT status, DELETE cancel) | `schedule.executed`, `post.published` |
| [ARC-004] | Analyst | Read-only analytics, performance metrics, reporting | `/api/v1/analytics`, `/api/v1/metrics` (GET) | `metrics.collected`, `performance.metrics.collected` |

**Traceability:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [DOC-001]

## 6. Security Policy & Performance Compliance
Enterprise security standards and non-functional requirement adherence per OWASP Top 10 and project NFRs.

- **SQL Injection Prevention (NFR-003, DAT-001, DAT-002, DAT-003):** All database queries utilize `PreparedStatement`/JPA parameter binding. Dynamic sorting whitelist `ALLOWED_SORT_FIELDS` = [`scheduledTime`, `status`, `likes`, `comments`, `shares`]. Tenant isolation enforced via `tenant_id` in every transaction session.
- **XSS & CSP (ARC-006, NFR-002):** User-generated `content` sanitized via OWASP Java HTML Sanitizer. CSP header `default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'`. `X-Content-Type-Options: nosniff`, `Strict-Transport-Security` enforced at Ingress.
- **CORS Multi-Tenant (NFR-002, NFR-003):** Origin whitelist per tenant from `TENANT_ORIGINS` table. No `*` allowed. Preflight `OPTIONS` validated against tenant_id. `Access-Control-Allow-Credentials` only with specific origin.
- **Log Scrubbing & PII Masking (NFR-002, ARC-005):** `LogScrubbingInterceptor` regex patterns for email, UUID, JWT, IP. `@SensitiveData` annotation on DTO fields triggers `SensitiveFieldSerializer` for hash/truncation in logs and API responses.
- **Performance Targets (NFR-001):** Latency < 200ms for schedule creation/ recommendation. Throughput > 1000 req/min. HPA scales based on CPU > 70% or Kafka consumer lag > 1000 messages.
- **Secret Management (ARC-006):** All API keys, JWT secrets, DB credentials stored in GCP Secret Manager. Runtime fetches via dynamic environment variables. Never hardcoded.

### 7. Traceability Matrix
Mapping of architectural modules, Kafka event pipelines, and database schemas to their originating requirement tags.

| Module / Entity | Tag IDs | Description |
| :--- | :--- | :--- |
| `users` Table | [DAT-001], [DAT-ALL (1 to 3)] | User entity with tenant isolation, role constraints |
| `schedules` Table | [DAT-001], [REQ-001], [EXC-001], [EXC-002] | Publishing schedule with status lifecycle |
| `performance_metrics` Table | [DAT-002], [REQ-002] | AI/ML performance data collection |
| `rate_limits` Table | [DAT-003], [REQ-003], [EXC-005] | Redis Token Bucket rate limiting |
| API Endpoint `POST /api/v1/schedules` | [REQ-001], [EXC-001], [EXC-002], [ARC-001]-[ARC-006] | Schedule creation with validation & auth |
| API Endpoint `POST /api/v1/ai/recommendations` | [REQ-002], [EXC-003], [EXC-004], [ARC-005] | AI content generation with fallback |
| API Endpoint `POST /api/v1/rate-limits/check` | [REQ-003], [EXC-005] | Rate limit check with Token Bucket |
| Security Controls | [ARC-001]-[ARC-006], [NFR-002], [NFR-003] | RBAC, CORS, CSP, Secret Management |
| Non-Functional Requirements | [NFR-001], [NFR-002], [NFR-003] | Performance, Security, Multi-tenancy |

---
**End of Architecture Blueprint**
```