```markdown
# Physical ERD & Relational Mapping Catalog: Social Scheduler Enterprise System

## Document Control & Metadata
- **Document ID:** `DOC-DAT-ERD-MAPPING-001`
- **Target Destination Path:** `./sources/docs/database/PHYSICAL_ERD_RELATIONAL_MAPPING.md`
- **Project Identity:** `social-scheduler`
- **Java Package Prefix Base:** `org.nlh4j.socialscheduler`
- **Architectural Scope:** Multi-tenant PostgreSQL Relational Database Schema Catalog
- **Associated Traceability Tag IDs:** `[DAT-001]`, `[DAT-002]`, `[DAT-003]`, `[DAT-ALL (1 to 3)]`

---

## Table of Contents
1. [Introduction](#1-introduction)
2. [Multi-Tenancy & Schema-per-Tenant Strategy](#2-multi-tenancy--schema-per-tenant-strategy)
3. [Physical Entity-Relationship Diagram (ERD)](#3-physical-entity-relationship-diagram-erd)
4. [Database Schema Specifications & Column Dictionaries](#4-database-schema-specifications--column-dictionaries)
   - 4.1. `user_schema.users` (`[DAT-001]`)
   - 4.2. `schedule_schema.schedules` (`[DAT-001]`)
   - 4.3. `ai_schema.performance_metrics` (`[DAT-002]`)
   - 4.4. `rate_limit_schema.rate_limits` (`[DAT-003]`)
5. [Flyway Migration Automation & Lifecycle Execution](#5-flyway-migration-automation--lifecycle-execution)
6. [Traceability Matrix Reference](#6-traceability-matrix-reference)

---

## 1. Introduction

The `social-scheduler` enterprise platform is engineered upon a distributed, event-driven microservices architecture where data isolation, transactional integrity, and horizontal scalability are paramount. This document defines the physical Entity-Relationship (ERD) relational mapping, schema distribution strategies, and column-level data dictionaries governing the persistence layer across all backend services (`user-service`, `schedule-service`, `ai-service`, and `rate-limit-service`).

By enforcing strict relational constraints, foreign key referential integrity across bounded contexts, and dedicated schema segregation, this persistence model guarantees absolute compliance with enterprise multi-tenancy requirements, auditability standards, and high-throughput transactional performance.

---

## 2. Multi-Tenancy & Schema-per-Tenant Strategy

To prevent cross-tenant data leakage and satisfy stringent enterprise security compliance, the persistence layer implements a **Schema-per-Tenant** isolation pattern built on top of a centralized PostgreSQL cluster. 

- **Bounded Context Segregation:** Each microservice owns an exclusive PostgreSQL schema within its assigned persistence boundary:
  - `user_schema`: Governed by `user-service` (`[DAT-001]`).
  - `schedule_schema`: Governed by `schedule-service` (`[DAT-001]`).
  - `ai_schema`: Governed by `ai-service` (`[DAT-002]`).
  - `rate_limit_schema`: Governed by `rate-limit-service` (`[DAT-003]`).
- **Tenant Context Propagation:** Every table across all schemas incorporates a mandatory `tenant_id VARCHAR(64)` column. During runtime execution, the API Gateway extracts the tenant identifier from incoming JWT claims or HTTP headers (`X-Tenant-Id`), populating the thread-local tenant context. Hibernate interceptors and Flyway connection hooks dynamically prepend or set the PostgreSQL search path to guarantee that queries execute strictly within the designated schema context.
- **Index Optimization:** Every `tenant_id` column is indexed alongside primary operational query fields (e.g., `idx_users_tenant`, `idx_schedules_tenant_time`), ensuring that multi-tenant queries execute with $O(\log n)$ complexity and zero table-scan overhead.

---

## 3. Physical Entity-Relationship Diagram (ERD)

The following Mermaid.js entity-relationship diagram illustrates the structural topology, foreign key dependencies, and schema boundaries across the four core persistence domains:

```mermaid
erDiagram
    user_schema_users {
        UUID user_id PK
        VARCHAR(64) tenant_id "UK, Indexed"
        VARCHAR(255) email "UK"
        VARCHAR(255) password_hash
        VARCHAR(32) role "CHECK (ADMIN, USER, SCHEDULER, ANALYST)"
        BOOLEAN enabled
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    schedule_schema_schedules {
        UUID schedule_id PK, FK
        UUID user_id PK, FK
        VARCHAR(64) tenant_id "Indexed"
        VARCHAR(32) platform PK "CHECK (FACEBOOK, INSTAGRAM, TIKTOK)"
        TEXT content
        TIMESTAMP scheduled_time PK, Indexed
        VARCHAR(16) status "CHECK (PENDING, SENT, FAILED, CANCELLED)"
        TIMESTAMP actual_sent_time
        INTEGER retry_count
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ai_schema_performance_metrics {
        UUID performance_id PK
        UUID post_id PK, FK
        VARCHAR(64) tenant_id "Indexed"
        INTEGER likes "CHECK (>= 0)"
        INTEGER comments "CHECK (>= 0)"
        INTEGER shares "CHECK (>= 0)"
        TIMESTAMP collected_at PK, Indexed
    }

    rate_limit_schema_rate_limits {
        UUID rate_limit_id PK
        UUID user_id FK
        VARCHAR(64) tenant_id "Indexed"
        VARCHAR(255) endpoint PK "CHECK (/api/v1/...)"
        INTEGER request_count "CHECK (>= 0)"
        TIMESTAMP window_start PK, Indexed
        TIMESTAMP window_end
    }

    user_schema_users ||--o{ schedule_schema_schedules : "owns (user_id)"
    schedule_schema_schedules ||--o{ ai_schema_performance_metrics : "measures (schedule_id = post_id)"
    user_schema_users ||--o{ rate_limit_schema_rate_limits : "throttles (user_id)"
```

---

## 4. Database Schema Specifications & Column Dictionaries

### 4.1. `user_schema.users` (`[DAT-001]`, `[DAT-ALL (1 to 3)]`)
- **Microservice Owner:** `user-service`
- **Physical Path:** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql`
- **Description:** Stores enterprise user profiles, authentication credentials, RBAC roles, and tenant associations.

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description | Traceability Tag |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `user_id` | `UUID` | NOT NULL | - | PRIMARY KEY (`pk_users`) | Unique identifier for the user account. | `[DAT-001]` |
| `tenant_id` | `VARCHAR(64)` | NOT NULL | - | PART OF `uk_users_tenant_email`, INDEX (`idx_users_tenant`) | Enterprise tenant discriminator. | `[DAT-001]` |
| `email` | `VARCHAR(255)` | NOT NULL | - | PART OF `uk_users_tenant_email` | User login email address. | `[DAT-001]` |
| `password_hash` | `VARCHAR(255)` | NOT NULL | - | - | Bcrypt hashed password credential. | `[DAT-001]` |
| `role` | `VARCHAR(32)` | NOT NULL | - | CHECK (`ck_users_role`: `ADMIN`, `USER`, `SCHEDULER`, `ANALYST`) | Assigned RBAC system role. | `[DAT-001]` |
| `enabled` | `BOOLEAN` | NOT NULL | `TRUE` | - | Account active status flag. | `[DAT-001]` |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | - | Record creation timestamp. | `[DAT-001]` |
| `updated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | - | Record last modification timestamp. | `[DAT-001]` |

---

### 4.2. `schedule_schema.schedules` (`[DAT-001]`, `[DAT-ALL (1 to 3)]`)
- **Microservice Owner:** `schedule-service`
- **Physical Path:** `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql`
- **Description:** Manages multi-platform social media publishing schedules, content payloads, execution lifecycle states, and retry counters.

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description | Traceability Tag |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `schedule_id` | `UUID` | NOT NULL | - | PART OF `pk_schedules`, INDEX (`idx_schedules_user_status`) | Unique schedule task identifier. | `[DAT-001]` |
| `user_id` | `UUID` | NOT NULL | - | PART OF `pk_schedules`, FOREIGN KEY (`fk_schedules_user` -> `user_schema.users`) | Owner user reference ID. | `[DAT-001]` |
| `tenant_id` | `VARCHAR(64)` | NOT NULL | - | INDEX (`idx_schedules_tenant_time`) | Enterprise tenant discriminator. | `[DAT-001]` |
| `platform` | `VARCHAR(32)` | NOT NULL | - | PART OF `pk_schedules`, CHECK (`ck_schedules_platform`: `FACEBOOK`, `INSTAGRAM`, `TIKTOK`) | Target social media platform. | `[DAT-001]` |
| `content` | `TEXT` | NOT NULL | - | - | Sanitized publishing text content. | `[DAT-001]` |
| `scheduled_time` | `TIMESTAMP` | NOT NULL | - | PART OF `pk_schedules`, INDEX (`idx_schedules_tenant_time`) | Target execution timestamp. | `[DAT-001]` |
| `status` | `VARCHAR(16)` | NOT NULL | - | CHECK (`ck_schedules_status`: `PENDING`, `SENT`, `FAILED`, `CANCELLED`) | Current publication lifecycle state. | `[DAT-001]` |
| `actual_sent_time`| `TIMESTAMP` | TRUE | `NULL` | - | Timestamp when successfully dispatched. | `[DAT-001]` |
| `retry_count` | `INTEGER` | NOT NULL | `0` | - | Number of upstream dispatch retries. | `[DAT-001]` |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | - | Record creation timestamp. | `[DAT-001]` |
| `updated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | - | Record last modification timestamp. | `[DAT-001]` |

---

### 4.3. `ai_schema.performance_metrics` (`[DAT-002]`, `[DAT-ALL (1 to 3)]`)
- **Microservice Owner:** `ai-service`
- **Physical Path:** `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql`
- **Description:** Aggregates historical engagement metrics (likes, comments, shares) linked to published schedules to power AI prompt engineering and content recommendations.

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description | Traceability Tag |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `performance_id`| `UUID` | NOT NULL | - | PART OF `pk_performance` | Unique performance record identifier. | `[DAT-002]` |
| `post_id` | `UUID` | NOT NULL | - | PART OF `pk_performance`, FOREIGN KEY (`fk_performance_schedule` -> `schedule_schema.schedules`), INDEX (`idx_performance_post`) | Referenced schedule/post ID. | `[DAT-002]` |
| `tenant_id` | `VARCHAR(64)` | NOT NULL | - | - | Enterprise tenant discriminator. | `[DAT-002]` |
| `likes` | `INTEGER` | NOT NULL | `0` | CHECK (`ck_performance_likes` >= 0) | Total accumulated likes count. | `[DAT-002]` |
| `comments` | `INTEGER` | NOT NULL | `0` | CHECK (`ck_performance_comments` >= 0) | Total accumulated comments count. | `[DAT-002]` |
| `shares` | `INTEGER` | NOT NULL | `0` | CHECK (`ck_performance_shares` >= 0) | Total accumulated shares count. | `[DAT-002]` |
| `collected_at` | `TIMESTAMP` | NOT NULL | - | PART OF `pk_performance` | Timestamp when metrics were harvested. | `[DAT-002]` |

---

### 4.4. `rate_limit_schema.rate_limits` (`[DAT-003]`, `[DAT-ALL (1 to 3)]`)
- **Microservice Owner:** `rate-limit-service`
- **Physical Path:** `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql`
- **Description:** Tracks API request counts within sliding time windows to enforce rate limiting policies and prevent API abuse.

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description | Traceability Tag |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `rate_limit_id` | `UUID` | NOT NULL | - | PART OF `pk_rate_limits` | Unique rate limit tracking identifier. | `[DAT-003]` |
| `user_id` | `UUID` | NOT NULL | - | FOREIGN KEY (`fk_rate_limits_user` -> `user_schema.users`), INDEX (`idx_rate_limits_window`) | Target user reference ID. | `[DAT-003]` |
| `tenant_id` | `VARCHAR(64)` | NOT NULL | - | - | Enterprise tenant discriminator. | `[DAT-003]` |
| `endpoint` | `VARCHAR(255)`| NOT NULL | - | PART OF `pk_rate_limits`, CHECK (`ck_rate_limits_endpoint` IN API whitelist) | Target restricted API endpoint path. | `[DAT-003]` |
| `request_count` | `INTEGER` | NOT NULL | - | CHECK (`ck_rate_limits_count` >= 0) | Cumulative request count in window. | `[DAT-003]` |
| `window_start` | `TIMESTAMP` | NOT NULL | - | PART OF `pk_rate_limits`, INDEX (`idx_rate_limits_window`) | Rate limit sliding window start time. | `[DAT-003]` |
| `window_end` | `TIMESTAMP` | NOT NULL | - | - | Rate limit sliding window expiration time. | `[DAT-003]` |

---

## 5. Flyway Migration Automation & Lifecycle Execution

Database schema provisioning and evolution across all microservices are fully automated using **Flyway 10.x** integrated directly into the Spring Boot startup lifecycle.

1. **Initialization Sequence:** When any microservice (`user-service`, `schedule-service`, `ai-service`, `rate-limit-service`) boots up, the Flyway migration engine initializes against the configured PostgreSQL datasource.
2. **Schema & Versioning Convention:**
   - Migration scripts are located at `src/main/resources/db/migration/V1__init_<table_name>.sql`.
   - Flyway inspects the `flyway_schema_history` tracking table within each respective schema (`user_schema`, `schedule_schema`, `ai_schema`, `rate_limit_schema`).
3. **Execution Guarantee:** If no migration history exists, Flyway executes the SQL DDL statements atomically within a database transaction, establishing schemas, tables, primary keys, foreign key constraints, check constraints, and performance indexes automatically before the application context completes initialization. This ensures zero manual DB intervention required during enterprise deployments.

---

## 6. Traceability Matrix Reference

The persistence architecture defined in this catalog maps directly to the system requirements and technical specification identifiers established in the master architecture blueprint:

| Traceability Tag ID | Architecture Domain / Component | Target Physical Artifact Path | Compliance Summary |
| :--- | :--- | :--- | :--- |
| `[DAT-001]` | User & Schedule Persistence | `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql`<br>`./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql` | Establishes multi-tenant user authentication and multi-platform publishing schedules with state machine constraints. |
| `[DAT-002]` | AI Analytics Persistence | `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql` | Implements engagement tracking tables referenced by AI recommendation engines. |
| `[DAT-003]` | Rate Limiting Persistence | `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql` | Provides persistent backing for sliding window rate limiting and abuse prevention. |
| `[DAT-ALL (1 to 3)]` | Global Relational Integrity | `./sources/docs/database/PHYSICAL_ERD_RELATIONAL_MAPPING.md` | Enforces schema-per-tenant isolation, foreign key referential integrity, and automated Flyway migrations across all bounded contexts. |
```