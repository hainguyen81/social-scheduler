# Rate Limiting Service - Technical Documentation

## 1. Overview

The **Rate Limiting Service** is a critical microservice within the `social-scheduler` enterprise ecosystem, designed to enforce input validation and rate limiting policies for individual users. It ensures system stability, prevents abuse, and maintains fair resource allocation across all API consumers.

This service operates under the package namespace `org.nlh4j.socialscheduler.ratelimitingservice` and is deployed as an independent container on port `8084`.

---

## 2. Core Responsibilities

- Enforce per-user rate limits on API endpoints.
- Track and manage request counts within configurable time windows.
- Validate incoming requests against defined thresholds.
- Emit structured exceptions when limits are exceeded.
- Persist rate limit state in a relational database for auditability and recovery.

---

## 3. Domain Model: `RateLimit` Entity

### 3.1 Class Definition

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimit.java`

**Package:**  
`org.nlh4j.socialscheduler.ratelimitingservice`

### 3.2 Attributes

| Field Name       | Type      | Description                                      | Constraints                          |
|------------------|-----------|--------------------------------------------------|--------------------------------------|
| `rateLimitId`    | `UUID`    | Unique identifier for the rate limit record      | Primary Key, Not Null                |
| `userId`         | `UUID`    | Identifier of the user subject to rate limiting  | Foreign Key → `users.user_id`, Not Null |
| `endpoint`       | `String`  | API endpoint path being rate-limited             | Max Length 255, Not Null             |
| `requestCount`   | `Integer` | Number of requests made in the current window    | Not Null, Default 0                  |
| `windowStart`    | `Timestamp` | Start timestamp of the rate limit window       | Not Null                             |
| `windowEnd`      | `Timestamp` | End timestamp of the rate limit window         | Not Null                             |

### 3.3 Relationships

- **ManyToOne** relationship with the `User` entity via `userId`.
- Maps to the `rate_limits` table in PostgreSQL.

---

## 4. Repository Layer

### 4.1 Interface Definition

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java`

**Extends:**  
`JpaRepository<RateLimit, UUID>`

### 4.2 Key Methods

| Method Signature                                                                 | Return Type           | Description                                                                 | Targeted Tag IDs |
|----------------------------------------------------------------------------------|-----------------------|-----------------------------------------------------------------------------|------------------|
| `findByUserIdAndEndpoint(UUID userId, String endpoint)`                          | `Optional<RateLimit>` | Retrieves the active rate limit record for a given user and endpoint.       | [REQ-003], [EXC-002], [EXC-003] |
| `save(RateLimit rateLimit)`                                                      | `RateLimit`           | Persists or updates a rate limit record using parameterized queries.        | [REQ-003], [EXC-002], [EXC-003] |
| `delete(RateLimit rateLimit)`                                                    | `void`                | Deletes a rate limit record from the database.                              | [REQ-003], [EXC-002], [EXC-003] |
| `findAll()`                                                                      | `List<RateLimit>`     | Returns all rate limit records.                                             | [REQ-003], [EXC-002], [EXC-003] |
| `findByUserId(UUID userId)`                                                      | `List<RateLimit>`     | Retrieves all rate limit records for a specific user across all endpoints.  | [REQ-003], [EXC-002], [EXC-003] |
| `deleteByUserIdAndEndpoint(UUID userId, String endpoint)`                        | `void`                | Deletes rate limit record for specific user and endpoint combination.       | [REQ-003], [EXC-002], [EXC-003] |
| `existsByUserIdAndEndpoint(UUID userId, String endpoint)`                        | `boolean`             | Checks if a rate limit record exists for the user and endpoint.             | [REQ-003], [EXC-002], [EXC-003] |

> **Security Note:** All database interactions are performed via Spring Data JPA repositories using named parameters to prevent SQL injection attacks [NFR-001].

### 4.3 Custom Query Annotations