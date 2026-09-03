```markdown
# 🏛️ Enterprise Microservices Architecture Blueprint: social-scheduler
* **Blueprint Document ID:** ARCH-20260831151355-CENTRAL-SPEC
* **Target Destination Path:** `./sources/docs/architecture/CENTRAL_ENDPOINT_API_CONTRACT_SPECS.md`
* **Enforced Package Namespace:** `org.nlh4j.socialscheduler`
* **Traceability Tag ID Reference:** `[ARC-000]`

---

## 📑 TABLE OF CONTENTS
1. [Executive Summary & System Overview](#1-executive-summary--system-overview)
2. [Core Technology Stack & Ecosystem Dependencies](#2-core-technology-stack--ecosystem-dependencies)
3. [Microservices Bounded Contexts & Package Naming Conventions](#3-microservices-bounded-contexts--package-naming-conventions)
4. [Event-Driven Architecture & Kafka Pipeline Flow](#4-event-driven-architecture--kafka-pipeline-flow)
5. [Database Multi-Tenancy & Schema-per-Tenant Topology](#5-database-multi-tenancy--schema-per-tenant-topology)
6. [Service Responsibility Matrix & Traceability Audit (`[ARC-000]`)](#6-service-responsibility-matrix--traceability-audit-arc-000)
7. [System Integration & Sequence Reference](#7-system-integration--sequence-reference)

---

## 1. Executive Summary & System Overview

The **social-scheduler** platform is architected as a highly scalable, event-driven microservices ecosystem designed to handle high-throughput social media publishing, AI-assisted content generation, and strict rate-limiting governance across distributed enterprise environments. 

The system isolates discrete business capabilities into independent, containerized services fronted by a centralized Spring Cloud API Gateway. All inter-service communication is decoupled via Apache Kafka event streams, ensuring non-blocking execution, fault isolation, and resilient asynchronous processing. Multi-tenancy is enforced at the database persistence boundary via a strict schema-per-tenant architectural pattern hosted on PostgreSQL 16.x.

---

## 2. Core Technology Stack & Ecosystem Dependencies

The architecture relies on a robust enterprise-grade technology stack bound by strict version controls and dependency management rules:

| Technology Layer | Component / Framework | Version | Traceability Tag |
| :--- | :--- | :--- | :--- |
| **Language Runtime** | Eclipse Temurin JDK (LTS) | 21.0.x | `[ARC-000]` |
| **Backend Framework** | Spring Boot / Spring Cloud | 3.3.x / 2023.x | `[ARC-000]` |
| **Messaging Broker** | Apache Kafka (Client / Broker) | 3.7.x | `[ARC-000]` |
| **Caching & Rate Limiting** | Redis (Lettuce Client) | 7.x | `[ARC-000]` |
| **Database Engine** | PostgreSQL | 16.x | `[ARC-000]` |
| **Migration Tooling** | Flyway Core | 10.x | `[ARC-000]` |
| **Container & Orchestration** | Docker / Kubernetes (GKE) | Latest | `[ARC-000]` |

---

## 3. Microservices Bounded Contexts & Package Naming Conventions

All Java source code across the multi-module Maven project strictly adheres to the mandated root package prefix: `org.nlh4j.socialscheduler`. Each microservice resides within its dedicated Maven module under `./sources/backend/`, enforcing strict encapsulation of domain logic, persistence layers, and integration adapters.

### Package Namespace Mapping:
* **API Gateway:** `org.nlh4j.socialscheduler.gateway` (`./sources/backend/api-gateway/`)
* **User Service:** `org.nlh4j.socialscheduler.userservice` (`./sources/backend/user-service/`)
* **Schedule Service:** `org.nlh4j.socialscheduler.scheduleservice` (`./sources/backend/schedule-service/`)
* **AI Service:** `org.nlh4j.socialscheduler.aiservice` (`./sources/backend/ai-service/`)
* **Rate Limit Service:** `org.nlh4j.socialscheduler.ratelimitservice` (`./sources/backend/rate-limit-service/`)

---

## 4. Event-Driven Architecture & Kafka Pipeline Flow

The system utilizes Apache Kafka as the central backbone for event-driven choreography. The primary event topic `social.scheduler.events` facilitates fan-out distribution from producers (`schedule-service`, `ai-service`) to reactive consumers across downstream bounded contexts.

```mermaid
flowchart TD
    Client([Client / Frontend App]) -->|HTTPS / JWT| Gateway[API Gateway
`org.nlh4j.socialscheduler.gateway`]
    
    subgraph Microservices Cluster
        Gateway -->|Route /api/v1/users| UserSvc[user-service
`org.nlh4j.socialscheduler.userservice`]
        Gateway -->|Route /api/v1/schedules| ScheduleSvc[schedule-service
`org.nlh4j.socialscheduler.scheduleservice`]
        Gateway -->|Route /api/v1/ai| AISvc[ai-service
`org.nlh4j.socialscheduler.aiservice`]
        Gateway -->|Route /api/v1/rate-limits| RateSvc[rate-limit-service
`org.nlh4j.socialscheduler.ratelimitservice`]
    end

    subgraph Messaging & Caching Infrastructure
        ScheduleSvc -->|Publish / Consume| Kafka{Apache Kafka 3.7.x
Topic: `social.scheduler.events`}
        AISvc -->|Publish / Consume| Kafka
        RateSvc -->|Token Bucket State| Redis[(Redis 7.x Cache)]
    end

    subgraph Persistence Layer (PostgreSQL 16.x)
        UserSvc --> DB[(PostgreSQL Cloud SQL)]
        ScheduleSvc --> DB
        AISvc --> DB
        RateSvc --> DB
    end

    style Gateway fill:#f9f,stroke:#333,stroke-width:2px
    style Kafka fill:#ff9,stroke:#333,stroke-width:2px
    style Redis fill:#9f9,stroke:#333,stroke-width:2px
    style DB fill:#99f,stroke:#333,stroke-width:2px
```

---

## 5. Database Multi-Tenancy & Schema-per-Tenant Topology

To guarantee strict data isolation between enterprise tenants, PostgreSQL 16.x is configured using a **Schema-per-Tenant** isolation model managed via Flyway 10.x migration scripts. Each microservice owns a dedicated schema within the cluster, ensuring foreign key integrity, localized indexing, and tenant-scoped query execution.

### Schema Catalog & Domain Mapping:
1. **`user_schema`**: Managed by `user-service`. Houses enterprise user identities, credential hashes, and RBAC role assignments (`ADMIN`, `USER`, `SCHEDULER`, `ANALYST`).
2. **`schedule_schema`**: Managed by `schedule-service`. Stores publishing schedules, target social platforms (`FACEBOOK`, `INSTAGRAM`, `TIKTOK`), execution timestamps, and lifecycle states (`PENDING`, `SENT`, `FAILED`, `CANCELLED`).
3. **`ai_schema`**: Managed by `ai-service`. Records historical performance metrics (`performance_metrics`) referenced by AI recommendation models to optimize future content generation.
4. **`rate_limit_schema`**: Managed by `rate-limit-service`. Tracks sliding window request counts and token bucket allocations per endpoint and user tenant.

---

## 6. Service Responsibility Matrix & Traceability Audit (`[ARC-000]`)

The following traceability matrix maps architectural microservices components to their structural design parameters, package roots, and governing Tag IDs.

| Module Name | Physical Source Path | Core Responsibility & Architectural Scope | Governing Tag ID |
| :--- | :--- | :--- | :--- |
| **API Gateway** | `./sources/backend/api-gateway/` | JWT token validation, rate-limiting filter interception, SSL termination, and dynamic request routing. | `[ARC-000]` |
| **User Service** | `./sources/backend/user-service/` | Tenant management, user authentication, profile provisioning, and RBAC authorization governance. | `[ARC-000]` |
| **Schedule Service** | `./sources/backend/schedule-service/` | Multi-platform publishing schedule CRUD operations, state machine management, and SDK integration dispatchers. | `[ARC-000]` |
| **AI Service** | `./sources/backend/ai-service/` | OpenAI Completion API integration, historical performance analysis, and fallback content orchestration. | `[ARC-000]` |
| **Rate Limit Service** | `./sources/backend/rate-limit-service/` | Redis Token Bucket rate limiting enforcement, sliding window calculations, and quota breach logging. | `[ARC-000]` |

---

## 7. System Integration & Sequence Reference

Cross-service coordination during an incoming scheduling request follows a strictly synchronized asynchronous flow:
1. **Inbound Ingestion:** Client transmits an authenticated `POST /api/v1/schedules` payload carrying a valid Bearer JWT.
2. **Gateway Interception:** API Gateway (`api-gateway`) executes `JwtAuthFilter` and invokes `RateLimitGatewayFilter` to evaluate Redis Token Bucket availability.
3. **Service Dispatch:** Request is routed to `schedule-service` (`org.nlh4j.socialscheduler.scheduleservice`).
4. **Persistence & Event Emission:** `ScheduleService` validates the payload, commits the entity to `schedule_schema.schedules` via Flyway-managed JPA repositories, and publishes a `schedule.created` event to Apache Kafka topic `social.scheduler.events`.
5. **Asynchronous Processing:** Downstream workers (`ai-service`, analytics adapters) consume the event asynchronously without blocking the primary client HTTP thread, ensuring response latencies remain strictly under the 200ms threshold.

---
*End of Central Architecture & Microservices Blueprint (`[ARC-000]`).*
```