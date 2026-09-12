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
| Attribute | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `userId` | UUID | Unique identifier for the user | Primary Key, Auto-generated |
| `username` | VARCHAR(255) | User's chosen display name | Not Null, Unique |
| `email` | VARCHAR(255) | User's email address | Not Null, Unique |
| `passwordHash` | VARCHAR(255) | Hashed password for authentication | Not Null |
| `role` | VARCHAR(50) | User role within the system | Not Null, Check: [Admin, User, Scheduler, Analyst] |
| `tenantId` | UUID | Tenant context identifier | Not Null, Foreign Key to Tenants |
| `createdAt` | TIMESTAMP | Record creation timestamp | Not Null, Default: current_timestamp |
| `updatedAt` | TIMESTAMP | Last update timestamp | Not Null, Default: current_timestamp on update |

### User Repository
The `UserRepository` interface extends `JpaRepository<User, UUID>` and provides Spring Data JPA methods for database operations. It is configured to enforce tenant isolation and integrate with the system's authentication and event publishing pipelines.

**Core Methods:**
- `findById(UUID userId)` - Retrieve a user by unique identifier.
- `findByUsername(String username)` - Retrieve a user by username for authentication validation.
- `findByEmail(String email)` - Retrieve a user by email address.
- `save(User user)` - Persist a new user or update an existing user.
- `deleteById(UUID userId)` - Remove a user from the system by identifier.

**Traceability Mapping:**
- `[ARC-001]`: Core user entity management and CRUD operations foundation.
- `[ARC-002]`: Integration with OAuth2/JWT authentication flows via username/email lookup.
- `[ARC-003]`: Tenant isolation enforcement through `tenantId` column and query filtering.
- `[ARC-004]`: Event publishing to Kafka `user-events` topic on user create/update/delete lifecycle.

## Traceability Matrix Reference
| Module / Component | Targeted Tag IDs | Description |
| :--- | :--- | :--- |
| User Entity Attributes | `[ARC-001]`, `[ARC-003]`, `[ARC-004]` | Defines user identity, tenant isolation, and event lifecycle markers. |
| User Repository Methods | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]` | Enables data access, authentication integration, tenant filtering, and event triggering. |
| Architecture Overview | `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]` | Maps package structure, tech stack, data flow, and multi-tenancy to requirement codes. |