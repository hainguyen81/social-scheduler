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

| Method Signature                                                                 | Return Type           | Description                                                                 |
|----------------------------------------------------------------------------------|-----------------------|-----------------------------------------------------------------------------|
| `findByUserIdAndEndpoint(UUID userId, String endpoint)`                          | `Optional<RateLimit>` | Retrieves the active rate limit record for a given user and endpoint.       |
| `save(RateLimit rateLimit)`                                                      | `RateLimit`           | Persists or updates a rate limit record using parameterized queries.        |
| `delete(RateLimit rateLimit)`                                                    | `void`                | Deletes a rate limit record from the database.                              |
| `findAll()`                                                                      | `List<RateLimit>`     | Returns all rate limit records.                                             |

> **Security Note:** All database interactions are performed via Spring Data JPA repositories using named parameters to prevent SQL injection attacks [NFR-001].

---

## 5. Service Layer

### 5.1 Class Definition

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitService.java`

**Annotations:**  
`@Service`, `@Slf4j`

### 5.2 Core Methods

#### 5.2.1 `checkAndIncrementRequest(UUID userId, String endpoint)`

- **Purpose:** Validates whether a user can make another request to the specified endpoint.
- **Logic:**
  1. Fetch the current `RateLimit` record for the user and endpoint.
  2. If no record exists, create a new one with `requestCount = 1`.
  3. If a record exists:
     - Check if the current time is within the `windowEnd`.
     - If yes, increment `requestCount`.
     - If no, reset the window and set `requestCount = 1`.
  4. Compare `requestCount` against the configured threshold.
  5. If exceeded, throw `RateLimitExceededException`.
- **Returns:** `boolean` indicating success.
- **Throws:** `RateLimitExceededException` [EXC-005]

#### 5.2.2 `getRateLimitByUserIdAndEndpoint(UUID userId, String endpoint)`

- **Purpose:** Retrieve the current rate limit status for a user and endpoint.
- **Returns:** `Optional<RateLimit>`

#### 5.2.3 `resetRateLimit(UUID userId, String endpoint)`

- **Purpose:** Manually reset the rate limit counter for a user and endpoint.
- **Use Case:** Administrative override or scheduled cleanup.

---

## 6. Controller Layer

### 6.1 Class Definition

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitController.java`

**Annotations:**  
`@RestController`, `@RequestMapping("/api/v1/rate-limits")`, `@Slf4j`

### 6.2 Endpoints

#### 6.2.1 `GET /api/v1/rate-limits/{userId}/{endpoint}`

- **Description:** Retrieve the current rate limit status for a specific user and endpoint.
- **Path Parameters:**
  - `userId` (`UUID`): The user's unique identifier.
  - `endpoint` (`String`): The API endpoint path.
- **Response Schema (Success 200):**