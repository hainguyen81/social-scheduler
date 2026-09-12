# User Service Documentation
*File Path:* `./sources/docs/user-service-documentation.md`

## Overview
The **User Service** is a core microservice within the `social-scheduler` ecosystem, responsible for managing system user identities, roles, and authentication metadata. It provides CRUD operations, search capabilities, and integrates with OAuth2/JWT authentication flows. All interactions are exposed via RESTful endpoints and are instrumented with comprehensive logging and exception handling.

## Architecture
- **Package:** `org.nlh4j.socialscheduler.userservice`
- **Technology Stack:** Spring Boot, Spring Data JPA, PostgreSQL, Redis (caching), Apache Kafka (event publishing), Spring Security.
- **Data Flow:** User events (create, update, delete) are published to Kafka topic `user-events` for downstream services (e.g., scheduling, recommendations).
- **Multi‑tenancy:** Each user belongs to a tenant context; the service respects tenant isolation via the `tenant_id` column (see schema).

## Data Model

### User Entity