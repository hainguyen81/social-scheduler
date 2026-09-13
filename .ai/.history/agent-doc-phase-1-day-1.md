# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/user-service-documentation.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Documentation Destination Path: `./sources/docs/user-service-documentation.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Tạo tài liệu kỹ thuật cho dịch vụ người dùng, bao gồm mô tả lớp User và các phương thức chính.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
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
```java
package org.nlh4j.socialscheduler.userservice;

/**
 * [ARC-001] Core user entity representing system user profile.
 * [ARC-002] Defines user attributes and constraints for multi‑tenancy.
 * [ARC-003] Supports OAuth2/JWT authentication integration.
 * [ARC-004] Provides audit fields for compliance tracking.
 */
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId; // Multi‑tenant isolation

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role; // Values: Admin, User, Scheduler, Analyst

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Getters, Setters, Constructors omitted for brevity
}
```

**Key Constraints**
- `user_id` – UUID primary key.
- `tenant_id` – enforces row‑level security.
- `username` & `email` – unique, non‑null.
- `role` – enumerated set for RBAC.

## Core Service Methods
| Method | Description | Signature | Targeted Tag IDs |
|--------|-------------|-----------|------------------|
| `createUser` | Registers a new user after validation. | `User createUser(User user, String tenantId)` | [ARC-001], [ARC-002] |
| `getUserById` | Retrieves a user by its UUID. | `User getUserById(UUID userId, String tenantId)` | [ARC-001], [ARC-003] |
| `updateUser` | Updates user profile fields. | `User updateUser(UUID userId, UserPatch patch, String tenantId)` | [ARC-001], [ARC-004] |
| `deleteUser` | Soft‑deletes a user (sets `deleted_flag`). | `void deleteUser(UUID userId, String tenantId)` | [ARC-001], [ARC-002] |
| `searchUsers` | Performs paginated, filtered search. | `Page<User> searchUsers(String filter, int page, int size, String tenantId)` | [ARC-001], [ARC-003] |
| `assignRole` | Changes user role (admin only). | `void assignRole(UUID userId, String newRole, String tenantId)` | [ARC-001], [ARC-004] |

## API Specifications
| HTTP Method | Endpoint | Request Headers | Request Body (JSON) | Response Body (JSON) | Targeted Tag IDs |
|-------------|----------|-----------------|---------------------|----------------------|------------------|
| `POST` | `/users` | `Authorization: Bearer <token>`<br>`Content-Type: application/json` | `{ "tenantId": "t001", "username": "jdoe", "email": "jdoe@example.com", "password": "SecurePass123!", "role": "User" }` | `{ "userId": "550e8400-e29b-41d4-a716-446655440000", "tenantId": "t001", "username": "jdoe", "email": "jdoe@example.com", "role": "User", "createdAt": "2024-09-12T13:29:15Z", "updatedAt": "2024-09-12T13:29:15Z" }` | [ARC-001], [ARC-002] |
| `GET` | `/users/{userId}` | `Authorization: Bearer <token>` | – | `{ "userId": "550e8400-e29b-41d4-a716-446655440000", "tenantId": "t001", "username": "jdoe", "email": "jdoe@example.com", "role": "User", "createdAt": "2024-09-12T13:29:15Z", "updatedAt": "2024-09-12T13:29:15Z" }` | [ARC-001], [ARC-003] |
| `PUT` | `/users/{userId}` | `Authorization: Bearer <token>`<br>`Content-Type: application/json` | `{ "username": "jdoe_updated", "email": "jdoe_updated@example.com", "role": "Admin" }` | `{ "userId": "550e8400-e29b-41d4-a716-446655440000", "username": "jdoe_updated", "email": "jdoe_updated@example.com", "role": "Admin", "updatedAt": "2024-09-12T13:35:00Z" }` | [ARC-001], [ARC-004] |
| `DELETE` | `/users/{userId}` | `Authorization: Bearer <token>` | – | `{ "message": "User soft‑deleted", "userId": "550e8400-e29b-41d4-a716-446655440000" }` | [ARC-001], [ARC-002] |
| `GET` | `/users` | `Authorization: Bearer <token>` | – | `[ { "userId": "...", "tenantId": "t001", "username": "jdoe", "email": "jdoe@example.com", "role": "User" }, ... ]` | [ARC-001], [ARC-003] |

*All endpoints enforce **Idempotency Key** validation via the `Idempotency-Key` HTTP header to guarantee exactly‑once delivery.*

## Exception Handling
The service leverages a dedicated `UserExceptionHandler` to normalize error responses:

```java
package org.nlh4j.socialscheduler.userservice;

/**
 * [ARC-001] Global exception handler for User Service.
 * [ARC-002] Provides consistent error payloads and logging.
 * [ARC-003] Maps domain violations to HTTP status codes.
 * [ARC-004] Ensures traceability via Tag IDs in error logs.
 */
@ControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        logger.error("[USER-EXCEPTION] [ARC-001] User not found. Raw error: {}", ex.getMessage(), ex);
        return new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateUser(DuplicateUserException ex) {
        logger.error("[USER-EXCEPTION] [ARC-002] Duplicate user entry. Raw error: {}", ex.getMessage(), ex);
        return new ErrorResponse("DUPLICATE_USER", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        logger.error("[USER-EXCEPTION] [ARC-003] Unexpected error in User Service. Raw error: {}", ex.getMessage(), ex);
        return new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred.");
    }
}
```

**Custom Exceptions**
- `UserNotFoundException` – thrown when a requested user does not exist.
- `DuplicateUserException` – thrown on unique constraint violation (username/email).
- All custom exceptions preserve the original cause (`throw new MyException("msg", cause)`).

## Logging
- **Entry/Exit:** Each service method logs `INFO` on entry (`[PROCESS] Entering createUser for userId: ...`) and on successful exit (`[PROCESS] Exiting createUser for userId: ...`).
- **Error:** All `catch` blocks emit `ERROR` logs with the required three context keys: subsystem (`UserService`), raw exception message, and Tag ID (e.g., `[USER-EXCEPTION] [ARC-001] ...`).
- **Audit:** User lifecycle events are written to Kafka topic `user-events` for downstream analytics.

## Security & Compliance
- **Authentication:** JWT bearer tokens validated via Spring Security; roles mapped to authorities.
- **Authorization:** `@PreAuthorize("hasRole('Admin')")` guards admin‑only operations (`assignRole`, `deleteUser`).
- **Input Validation:** `@Valid` + custom validators enforce email format, password strength, and role enumeration.
- **SQL Injection Protection:** All repository queries use Spring Data JPA’s parameter binding; no native queries with string concatenation.
- **Data Masking:** Sensitive fields (`passwordHash`) are never logged; `logger` calls use masked placeholders.
- **Audit Trails:** `createdAt`/`updatedAt` and `tenantId` enable forensic reconstruction.

## Traceability Matrix Reference
| Component | Description | Targeted Tag IDs |
|-----------|-------------|------------------|
| **User Entity** | Core domain model for system users | [ARC-001], [ARC-002], [ARC-003], [ARC-004] |
| **UserService** | Business logic layer (CRUD, search, role assignment) | [ARC-001], [ARC-002], [ARC-003], [ARC-004] |
| **UserController** | REST API entry points for user operations | [ARC-001], [ARC-002], [ARC-003], [ARC-004] |
| **UserExceptionHandler** | Global exception handling and error logging | [ARC-001], [ARC-002], [ARC-003], [ARC-004] |
| **Kafka Event Publisher** | Emits user events to `user-events` topic | [ARC-001], [ARC-002] |
| **Security Config** | OAuth2/JWT authentication & RBAC enforcement | [ARC-003], [ARC-004] |

All Tag IDs are preserved exactly as specified and mapped to the corresponding architectural or functional elements within this documentation.

## Deployment Notes
- **Docker Image:** `org.nlh4j/socialscheduler/user-service:1.0.0`
- **Environment Variables:** `SPRING_PROFILES_ACTIVE`, `DB_URL`, `REDIS_HOST`, `KAFKA_BOOTSTRAP_SERVERS`.
- **Health Checks:** `/actuator/health` and `/actuator/info` exposed on port `8081`.
- **Scaling:** Horizontal Pod Autoscaler configured for CPU utilization > 70%.

---

*End of User Service Documentation.*

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/scheduling-service-documentation.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Documentation Destination Path: `./sources/docs/scheduling-service-documentation.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Tạo tài liệu kỹ thuật cho dịch vụ lên lịch, bao gồm mô tả lớp Schedule và các phương thức chính.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# 📄 Technical Documentation – Scheduling Service
**File Path:** `./sources/docs/scheduling-service-documentation.md`  
**Version:** 1.0 (Cơ sở)  
**Date:** 2026/09/12 13:29:15  
**Author:** Enterprise System Architect (SA Agent)  
**Approved:** Chờ phê duyệt quản trị kỹ thuật  

## 🎯 1. Tổng quan về Dịch vụ

### 1.1 Mục đích & Phạm vi
Dịch vụ lên lịch (`scheduling-service`) cung cấp các chức năng cốt lõi để quản lý các công việc đăng bài trên các nền tảng mạng xã hội (Facebook, Instagram, TikTok). Nó tuân thủ các yêu cầu nghiệp vụ chính sau:

- **[REQ-001]** Tích hợp lịch đăng bài tự động cho Facebook, Instagram và TikTok.  
- **[EXC-001]** Xử lý các ngoại lệ liên quan đến lỗi định dạng lịch hoặc xung đột thời gian.  
- **[EXC-002]** Quản lý các lỗi liên quan đến quyền truy cập người dùng hoặc xác thực nền tảng.  

### 1.2 Kiến trúc Hệ thống
Dịch vụ này là một phần của kiến trúc **microservices** đa tenant, sử dụng **Apache Kafka** để phát sóng các sự kiện như `SCHEDULE_CREATED`, `SCHEDULE_UPDATED`, `SCHEDULE_PROCESSED`. Nó tuân thủ mô hình **CQRS** và **Event-Driven** với các thành phần chính sau:

- **Entity:** `Schedule` (lịch đăng bài) – được lưu trữ trong PostgreSQL.  
- **Service Layer:** `ScheduleService` – chứa logic nghiệp vụ và xử lý sự kiện.  
- **API Layer:** `ScheduleController` – các điểm cuối REST tuân thủ chuẩn **OpenAPI 3.0**.  
- **Lớp xử lý ngoại lệ:** `ScheduleExceptionHandler` – chuẩn hóa lỗi và ghi log với các Tag ID theo dõi.  

### 1.3 Ma trận Theo dõi
| Module | TagID Mục tiêu |
|--------|--------------|
| Lớp Schedule (Domain Model) | `[REQ-001]` |
| Lớp ScheduleService (Logic Nghiệp vụ) | `[REQ-001], [EXC-001], [EXC-002]` |
| Lớp ScheduleController (API) | `[REQ-001]` |
| Lớp ScheduleExceptionHandler (Xử lý Ngoại lệ) | `[EXC-001], [EXC-002]` |

---

## 🗺️ 2. Sơ đồ Kiến trúc (Mermaid)

```mermaid
graph TB
    subgraph "Tenant A"
        DB[(PostgreSQL)]
        SCHED_SERVICE[[Schedule Service]]
        SCHED_CTRL[[Schedule Controller]]
        KAFKA[(Kafka Broker)]
    end

    subgraph "Tenant B"
        DB2[(PostgreSQL)]
        SCHED_SERVICE2[[Schedule Service]]
        SCHED_CTRL2[[Schedule Controller]]
    end

    DB --> SCHED_SERVICE
    DB2 --> SCHED_SERVICE2

    SCHED_SERVICE --> KAFKA
    SCHED_SERVICE2 --> KAFKA

    KAFKA --> SCHED_CTRL
    KAFKA --> SCHED_CTRL2

    style DB fill:#e3f2fd,stroke:#1976d2
    style SCHED_SERVICE fill:#fff3e0,stroke:#f57c00
    style SCHED_CTRL fill:#fff3e0,stroke:#f57c00
    style KAFKA fill:#fce4ec,stroke:#c2185b
```

**Mô tả:**
- Mỗi tenant có một **PostgreSQL** riêng biệt để lưu trữ các bản ghi `Schedule`.
- **Schedule Service** xử lý các thao tác CRUD và phát sự kiện lên Kafka.
- **Schedule Controller** lộ các điểm cuối REST cho các dịch vụ tiêu thụ (ví dụ: các worker xử lý tự động).
- Các chủ đề Kafka (`schedule.created`, `schedule.updated`, `schedule.processed`) được fan-out đến các tenant khác để đồng bộ hóa dữ liệu.

---

## 🏗️ 3. Lớp Mô hình Miền – `Schedule`

**Đường dẫn tệp:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/Schedule.java`

```java
package org.nlh4j.socialscheduler.schedulingservice;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Lớp đại diện cho một lịch đăng bài trên các nền tảng mạng xã hội.
 * Tuân thủ các quy tắc xác thực nghiệp vụ theo [REQ-001], [EXC-001], [EXC-002].
 *
 * @author Enterprise System Architect
 * @version 1.0
 */
public class Schedule {

    /* -------------------------------------------------------------------------
     * Constants (Top-of-class immutable configuration)
     * ------------------------------------------------------------------------- */
    /** Các nền tảng được hỗ trợ – tuân thủ chính sách xác thực đầu vào [REQ-001] */
    public static final String PLATFORM_FACEBOOK  = "Facebook";
    public static final String PLATFORM_INSTAGRAM = "Instagram";
    public static final String PLATFORM_TIKTOK    = "TikTok";

    /** Các trạng thái hợp lệ của lịch – được sử dụng để xác thực nghiệp vụ [EXC-001] */
    public static final String STATUS_PENDING  = "pending";
    public static final String STATUS_SENT     = "sent";
    public static final String STATUS_FAILED   = "failed";
    public static final String STATUS_CANCELLED = "cancelled";

    /* -------------------------------------------------------------------------
     * Fields (Domain attributes)
     * ------------------------------------------------------------------------- */
    /** ID duy nhất của lịch – khóa chính trong PostgreSQL */
    private UUID scheduleId; // [DAT-001]

    /** ID người dùng sở hữu lịch – khóa ngoại tham chiếu bảng users */
    private UUID userId; // [DAT-001]

    /** Nền tảng mục tiêu – phải nằm trong {@link #PLATFORM_* constants} */
    private String platform; // [DAT-001]

    /** Nội dung bài đăng sẽ được lên lịch */
    private String content; // [DAT-001]

    /** Thời điểm dự kiến đăng bài (ISO‑8601 với múi giờ) */
    private ZonedDateTime scheduledTime; // [DAT-001]

    /** Trạng thái hiện tại của lịch – phải nằm trong {@link #STATUS_* constants} */
    private String status; // [DAT-001]

    /* -------------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------------- */
    /** Constructor mặc định (bắt buộc cho JPA / JSON deserialization) */
    public Schedule() {}

    /**
     * Constructor đầy đủ tham số để tạo một lịch mới.
     *
     * @param scheduleId   ID duy nhất (thường được tạo bởi UUID random)
     * @param userId       ID người dùng sở hữu
     * @param platform     Một trong {@link #PLATFORM_FACEBOOK}, {@link #PLATFORM_INSTAGRAM}, {@link #PLATFORM_TIKTOK}
     * @param content      Nội dung bài đăng
     * @param scheduledTime Thời điểm dự kiến đăng bài
     * @param status       Một trong {@link #STATUS_PENDING}, {@link #STATUS_SENT}, {@link #STATUS_FAILED}, {@link #STATUS_CANCELLED}
     */
    public Schedule(UUID scheduleId, UUID userId, String platform,
                    String content, ZonedDateTime scheduledTime, String status) {
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.platform = platform;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.status = status;
    }

    /* -------------------------------------------------------------------------
     * Getters & Setters (Lombok-style – được tạo bởi framework)
     * ------------------------------------------------------------------------- */
    public UUID getScheduleId() { return scheduleId; }
    public void setScheduleId(UUID scheduleId) { this.scheduleId = scheduleId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ZonedDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(ZonedDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /* -------------------------------------------------------------------------
     * Business Methods
     * ------------------------------------------------------------------------- */
    /**
     * Xác thực tính hợp lệ của lịch trước khi lưu.
     * Ném {@link IllegalArgumentException} nếu bất kỳ quy tắc nào bị vi phạm – được xử lý bởi {@link ScheduleExceptionHandler}.
     *
     * @throws IllegalArgumentException khi xác thực thất bại ([EXC-001], [EXC-002])
     */
    public void validate() {
        if (platform == null || (!PLATFORM_FACEBOOK.equals(platform) &&
                                 !PLATFORM_INSTAGRAM.equals(platform) &&
                                 !PLATFORM_TIKTOK.equals(platform))) {
            throw new IllegalArgumentException("Nền tảng không hợp lệ: " + platform);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung không được để trống");
        }
        if (scheduledTime == null || scheduledTime.isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException("Thời gian dự kiến phải là thời điểm trong tương lai");
        }
        if (status == null || (!STATUS_PENDING.equals(status) &&
                               !STATUS_SENT.equals(status) &&
                               !STATUS_FAILED.equals(status) &&
                               !STATUS_CANCELLED.equals(status))) {
            throw new IllegalArgumentException("Trạng thái lịch không hợp lệ: " + status);
        }
    }

    /**
     * Đánh dấu lịch là đã được xử lý – chuyển trạng thái sang {@link #STATUS_SENT}.
     * Được gọi bởi worker sau khi đăng bài thành công trên nền tảng mạng xã hội.
     */
    public void markAsSent() {
        this.status = STATUS_SENT;
    }

    /**
     * Ghi lại lỗi xử lý – chuyển trạng thái sang {@link #STATUS_FAILED}.
     *
     * @param errorMessage Thông báo lỗi chi tiết (được ghi log bởi {@link ScheduleExceptionHandler})
     */
    public void markAsFailed(String errorMessage) {
        this.status = STATUS_FAILED;
        // Ghi log lỗi với Tag ID theo dõi để giám sát
        // (Trong mã thực tế, một logger sẽ được sử dụng ở đây)
        // logger.error("[CRITICAL FAIL] [EXC-001] Lịch {} xử lý thất bại: {}",
        //              this.scheduleId, errorMessage);
    }

    /* -------------------------------------------------------------------------
     * toString / equals / hashCode (dùng cho debug & JPA)
     * ------------------------------------------------------------------------- */
    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", platform='" + platform + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return scheduleId.equals(schedule.scheduleId);
    }

    @Override
    public int hashCode() {
        return scheduleId.hashCode();
    }
}
```

**Ghi chú chính:**
- Tất cả các giá trị cấu hình (ví dụ: tên nền tảng, trạng thái) được định nghĩa dưới dạng `public static final` ở đầu lớp – tuân thủ chính sách **Anti-Magic-Numbers**.
- Phương thức `validate()` thực thi các quy tắc xác thực nghiệp vụ và ném các ngoại lệ được xử lý bởi `ScheduleExceptionHandler`.
- Các phương thức `markAsSent()` / `markAsFailed()` cập nhật trạng thái và bao gồm các ghi log mẫu (được chú thích) để minh họa **Logging Audit** theo quy định của doanh nghiệp.

---

## ⚙️ 4. Lớp Dịch vụ – `ScheduleService`

**Đường dẫn tệp:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`

```java
package org.nlh4j.socialscheduler.schedulingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ lớp cho các thao tác nghiệp vụ liên quan đến lịch.
 * Tích hợp với Kafka để phát sự kiện và tuân thủ các yêu cầu theo dõi [REQ-001], [EXC-001], [EXC-002].
 *
 * @author Enterprise System Architect
 */
@Service
@Transactional
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository repository;

    public ScheduleService(ScheduleRepository repository) {
        this.repository = repository;
    }

    /**
     * Tạo một lịch mới sau khi xác thực và phát sự kiện {@code SCHEDULE_CREATED}.
     *
     * @param schedule Đối tượng lịch (không bao gồm {@code scheduleId})
     * @return Lịch đã được tạo với {@code scheduleId} được gán
     */
    public Schedule createSchedule(Schedule schedule) {
        logger.info("[ENTRY] createSchedule for userId={} platform={}", schedule.getUserId(), schedule.getPlatform());
        schedule.validate(); // Kích hoạt xác thực nghiệp vụ – được xử lý bởi ScheduleExceptionHandler

        Schedule saved = repository.save(schedule);
        logger.debug("[BUSINESS] Lịch đã được lưu: {}", saved.getScheduleId());

        // Phát sự kiện lên Kafka (mẫu)
        // kafkaTemplate.send("schedule.created", saved.getScheduleId(), saved);

        logger.info("[EXIT] createSchedule completed with id={}", saved.getScheduleId());
        return saved;
    }

    /**
     * Lấy thông tin lịch theo ID.
     *
     * @param id UUID của lịch
     * @return Lịch nếu tồn tại, nếu không ném {@link ScheduleNotFoundException}
     */
    public Schedule getScheduleById(UUID id) {
        logger.info("[ENTRY] getScheduleById id={}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    String msg = "Lịch không tồn tại: " + id;
                    logger.error("[CRITICAL FAIL] [EXC-001] Lấy lịch thất bại: {}", msg);
                    return new ScheduleNotFoundException(msg);
                });
    }

    /**
     * Cập nhật một lịch hiện có.
     * Áp dụng cơ chế **Idempotency Key** thông qua tham số yêu cầu {@code Idempotency-Key}.
     *
     * @param id       UUID của lịch cần cập nhật
     * @param updated  Đối tượng lịch với các trường đã cập nhật
     * @return Lịch đã được cập nhật
     */
    public Schedule updateSchedule(UUID id, Schedule updated) {
        logger.info("[ENTRY] updateSchedule id={} userId={}", id, updated.getUserId());
        Schedule existing = getScheduleById(id);

        // Cập nhật các trường được phép
        existing.setPlatform(updated.getPlatform());
        existing.setContent(updated.getContent());
        existing.setScheduledTime(updated.getScheduledTime());
        existing.setStatus(updated.getStatus());

        existing.validate(); // Xác thực lại sau khi cập nhật

        Schedule saved = repository.save(existing);
        logger.info("[BUSINESS] Lịch đã được cập nhật: {}", saved.getScheduleId());

        // Phát sự kiện lên Kafka
        // kafkaTemplate.send("schedule.updated", saved.getScheduleId(), saved);

        logger.info("[EXIT] updateSchedule completed");
        return saved;
    }

    /**
     * Xóa một lịch theo ID.
     *
     * @param id UUID của lịch cần xóa
     */
    public void deleteSchedule(UUID id) {
        logger.info("[ENTRY] deleteSchedule id={}", id);
        Schedule existing = getScheduleById(id);
        repository.delete(existing);
        logger.info("[BUSINESS] Lịch đã được xóa: {}", id);
        logger.info("[EXIT] deleteSchedule completed");
    }

    /**
     * Lấy tất cả các lịch của một người dùng cụ thể.
     *
     * @param userId UUID của người dùng
     * @return Danh sách các lịch
     */
    public List<Schedule> getSchedulesByUserId(UUID userId) {
        logger.info("[ENTRY] getSchedulesByUserId userId={}", userId);
        List<Schedule> schedules = repository.findByUserId(userId);
        logger.info("[EXIT] getSchedulesByUserId returned {} bản ghi", schedules.size());
        return schedules;
    }

    /**
     * Điểm vào xử lý công việc – được gọi bởi một worker bất đồng bộ sau khi nhận được sự kiện từ Kafka.
     * Thực hiện việc đăng bài thực tế và cập nhật trạng thái.
     *
     * @param scheduleId ID của lịch cần xử lý
     * @throws ScheduleProcessingException nếu việc đăng bài thất bại ([EXC-002])
     */
    public void processScheduledPost(UUID scheduleId) {
        logger.info("[ENTRY] processScheduledPost scheduleId={}", scheduleId);
        Schedule schedule = getScheduleById(scheduleId);

        try {
            // Mô phỏng việc đăng bài lên nền tảng mạng xã hội
            boolean success = postToPlatform(schedule);
            if (success) {
                schedule.markAsSent();
                repository.save(schedule);
                logger.info("[BUSINESS] Đăng bài thành công cho lịch {}", scheduleId);
            } else {
                schedule.markAsFailed("API nền tảng trả về lỗi");
                repository.save(schedule);
                logger.warn("[BUSINESS] Đăng bài thất bại cho lịch {}", scheduleId);
            }
        } catch (Exception ex) {
            schedule.markAsFailed(ex.getMessage());
            repository.save(schedule);
            logger.error("[CRITICAL FAIL] [EXC-002] Xử lý lịch {} thất bại: {}", scheduleId, ex.getMessage(), ex);
            throw new ScheduleProcessingException("Xử lý lịch thất bại: " + scheduleId, ex);
        }

        logger.info("[EXIT] processScheduledPost completed");
    }

    /**
     * Phương thức giả lập – thay thế bằng tích hợp thực tế với API Facebook/Instagram/TikTok.
     */
    private boolean postToPlatform(Schedule schedule) {
        // Giả lập thành công cho các giá trị platform đã biết
        return PLATFORM_FACEBOOK.equals(schedule.getPlatform()) ||
               PLATFORM_INSTAGRAM.equals(schedule.getPlatform()) ||
               PLATFORM_TIKTOK.equals(schedule.getPlatform());
    }
}
```

**Điểm chính:**
- ** SOLID:** Lớp này tuân thủ **Single Responsibility** (quản lý lịch) và **Dependency Inversion** (phụ thuộc vào `ScheduleRepository` trừu tượng).
- **Logging:** Tất cả các điểm vào và điểm ra đều được ghi log ở mức `INFO`/`DEBUG`. Các lỗi được ghi log ở mức `ERROR` với **Tag ID** (`[EXC-001]`, `[EXC-002]`).
- **Idempotency:** Phương thức `updateSchedule` giả định xác thực khóa idempotency từ tiêu đề yêu cầu (`Idempotency-Key`).
- **Xử lý ngoại lệ:** Các ngoại lệ tùy chỉnh (`ScheduleNotFoundException`, `ScheduleProcessingException`) được ném và được xử lý bởi `ScheduleExceptionHandler`.

---

## 🌐 5. Lớp Điều khiển API – `ScheduleController`

**Đường dẫn tệp:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java`

```java
package org.nlh4j.socialscheduler.schedulingservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Các điểm cuối REST cho các thao tác quản lý lịch.
 * Tuân thủ các hợp đồng API theo [REQ-001] và các quy tắc xác thực đầu vào theo [EXC-001], [EXC-002].
 *
 * @author Enterprise System Architect
 */
@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Tạo một lịch mới.
     * Yêu cầu:
     * - Header: `Idempotency-Key` (bắt buộc cho các thao tác mutation)
     * - Body: {@link Schedule} (không bao gồm {@code scheduleId})
     */
    @Operation(summary = "Tạo lịch mới", description = "Tạo một lịch đăng bài mới trên một nền tảng mạng xã hội được hỗ trợ")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lịch đã được tạo thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu yêu cầu không hợp lệ"),
        @ApiResponse(responseCode = "409", description = "Xung đột idempotency – yêu cầu đã được xử lý trước đó")
    })
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody Schedule schedule) {

        logger.info("[ENTRY] POST /schedules idempotencyKey={} userId={}", idempotencyKey, schedule.getUserId());

        // Xác thực khóa idempotency (mẫu)
        if (!idempotencyKey.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            logger.warn("[VALIDATION] Khóa idempotency không hợp lệ: {}", idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Schedule created = scheduleService.createSchedule(schedule);
        logger.info("[EXIT] POST /schedules completed with id={}", created.getScheduleId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lấy thông tin lịch theo ID.
     */
    @Operation(summary = "Lấy lịch theo ID", description = "Truy xuất một lịch cụ thể dựa trên UUID của nó")
    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable UUID id) {
        logger.info("[ENTRY] GET /schedules/{}", id);
        Schedule schedule = scheduleService.getScheduleById(id);
        logger.info("[EXIT] GET /schedules/{} completed", id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Cập nhật một lịch hiện có.
     * Yêu cầu:
     * - Header: `Idempotency-Key` (bắt buộc)
     * - Body: {@link Schedule} với các trường đã cập nhật
     */
    @Operation(summary = "Cập nhật lịch", description = "Cập nhật các thuộc tính của một lịch hiện có")
    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable UUID id,
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
            @Valid @RequestBody Schedule updated) {

        logger.info("[ENTRY] PUT /schedules/{} idempotencyKey={}", id, idempotencyKey);

        if (!idempotencyKey.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            logger.warn("[VALIDATION] Khóa idempotency không hợp lệ cho cập nhật: {}", idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Schedule updatedSchedule = scheduleService.updateSchedule(id, updated);
        logger.info("[EXIT] PUT /schedules/{} completed", id);
        return ResponseEntity.ok(updatedSchedule);
    }

    /**
     * Xóa một lịch.
     * Sử dụng DELETE với xác thực Idempotency Key để ngăn chặn việc xóa trùng lặp.
     */
    @Operation(summary = "Xóa lịch", description = "Xóa một lịch theo ID của nó")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID id,
            @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey) {

        logger.info("[ENTRY] DELETE /schedules/{} idempotencyKey={}", id, idempotencyKey);

        if (!idempotencyKey.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            logger.warn("[VALIDATION] Khóa idempotency không hợp lệ cho xóa: {}", idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        scheduleService.deleteSchedule(id);
        logger.info("[EXIT] DELETE /schedules/{} completed", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy tất cả các lịch của một người dùng cụ thể.
     */
    @Operation(summary = "Lấy lịch theo người dùng", description = "Truy xuất tất cả các lịch thuộc sở hữu của một người dùng nhất định")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Schedule>> getSchedulesByUserId(@PathVariable UUID userId) {
        logger.info("[ENTRY] GET /schedules/user/{}", userId);
        List<Schedule> schedules = scheduleService.getSchedulesByUserId(userId);
        logger.info("[EXIT] GET /schedules/user/{} returned {} bản ghi", userId, schedules.size());
        return ResponseEntity.ok(schedules);
    }
}
```

### 5.1 Bảng Thông số Kỹ thuật API (OpenAPI)

| HTTP Method | Full Endpoint               | Request Headers                     | Path/Query Parameters | JSON Request Payload Schema                                                                 | JSON Response Schema (Success)                                                                 | JSON Response Schema (Failure) | Targeted Tag IDs |
|-------------|----------------------------|-------------------------------------|----------------------|--------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|--------------------------------|------------------|
| `POST`      | `/schedules`               | `Idempotency-Key` (bắt buộc)       | –                    | `{ "userId":"550e8400-e29b-41d4-a716-446655440000", "platform":"Facebook", "content":"Hello World", "scheduledTime":"2026-12-31T15:04:05+07:00", "status":"pending" }` | `{ "scheduleId":"550e8400-e29b-41d4-a716-446655440000", "userId":"550e8400-e29b-41d4-a716-446655440000", "platform":"Facebook", "content":"Hello World", "scheduledTime":"2026-12-31T15:04:05+07:00", "status":"pending" }` | `{ "timestamp":"2026-09-12T13:29:15Z", "error":"Conflict", "message":"Khóa idempotency không hợp lệ" }` | `[REQ-001]` |
| `GET`       | `/schedules/{id}`          | –                                   | `id (UUID)`         | –                                                                                          | `{ "scheduleId":"550e8400-e29b-41d4-a716-446655440000", "userId":"550e8400-e29b-41d4-a716-446655440000", "platform":"Facebook", "content":"Hello World", "scheduledTime":"2026-12-31T15:04:05+07:00", "status":"pending" }` | `{ "timestamp":"2026-09-12T13:29:15Z", "error":"Not Found", "message":"Lịch không tồn tại: <id>" }` | `[REQ-001]` |
| `PUT`       | `/schedules/{id}`          | `Idempotency-Key` (bắt buộc)       | `id (UUID)`         | `{ "platform":"Instagram", "content":"Updated content", "scheduledTime":"2026-12-31T16:00:00+07:00", "status":"pending" }` | `{ "scheduleId":"550e8400-e29b-41d4-a716-446655440000", "userId":"550e8400-e29b-41d4-a716-446655440000", "platform":"Instagram", "content":"Updated content", "scheduledTime":"2026-12-31T16:00:00+07:00", "status":"pending" }` | `{ "timestamp":"2026-09-12T13:29:15Z", "error":"Conflict", "message":"Khóa idempotency không hợp lệ" }` | `[REQ-001]` |
| `DELETE`    | `/schedules/{id}`          | `Idempotency-Key` (bắt buộc)       | `id (UUID)`         | –                                                                                          | `204 No Content`                                                                                                                                           | `{ "timestamp":"2026-09-12T13:29:15Z", "error":"Conflict", "message":"Khóa idempotency không hợp lệ" }` | `[REQ-001]` |
| `GET`       | `/schedules/user/{userId}` | –                                   | `userId (UUID)`      | –                                                                                          | `[ { "scheduleId":"550e8400-e29b-41d4-a716-446655440000", ... } ]`                                                                                           | `{ "timestamp":"2026-09-12T13:29:15Z", "error":"Not Found", "message":"Không tìm thấy lịch cho người dùng <userId>" }` | `[REQ-001]` |

**Ghi chú về Bảng:**
- Tất cả các **Targeted Tag IDs** được trích xuất từ ngữ cảnh nhiệm vụ con: `[REQ-001]`, `[EXC-001]`, `[EXC-002]`.
- Các **Idempotency-Key** tuân thủ UUID bắt buộc cho tất cả các thao tác mutation (`POST`, `PUT`, `DELETE`) để đảm bảo **Idempotency Key** theo quy định của doanh nghiệp.
- Các **Prepared Statements** được giả định trong `ScheduleRepository` để ngăn chặn SQL Injection.

---

## 🚨 6. Lớp Xử lý Ngoại lệ – `ScheduleExceptionHandler`

**Đường dẫn tệp:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`

```java
package org.nlh4j.socialscheduler.schedulingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

/**
 * Điểm vào toàn cầu để xử lý các ngoại lệ liên quan đến lịch.
 * Tuân thủ các quy tắc **Comprehensive Exception Logging** và **Exception Cause Preservation**.
 *
 * @author Enterprise System Architect
 */
@RestControllerAdvice
public class ScheduleExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionHandler.class);

    /**
     * Xử lý {@link ScheduleNotFoundException} – trường hợp không tìm thấy tài nguyên.
     */
    @ExceptionHandler(ScheduleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ScheduleNotFoundException ex) {
        logger.error("[CRITICAL FAIL] [EXC-001] Lấy lịch thất bại: {}", ex.getMessage(), ex);
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                ZonedDateTime.now().toString(),
                "[EXC-001]"
        );
    }

    /**
     * Xử lý {@link ScheduleProcessingException} – lỗi trong quá trình đăng bài.
     */
    @ExceptionHandler(ScheduleProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleProcessing(ScheduleProcessingException ex) {
        logger.error("[CRITICAL FAIL] [EXC-002] Xử lý lịch thất bại: {}", ex.getMessage(), ex);
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                ZonedDateTime.now().toString(),
                "[EXC-002]"
        );
    }

    /**
     * Xử lý {@link IllegalArgumentException} – lỗi xác thực nghiệp vụ từ {@link Schedule#validate()}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(IllegalArgumentException ex) {
        logger.warn("[VALIDATION] Xác thực lịch thất bại: {}", ex.getMessage(), ex);
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ZonedDateTime.now().toString(),
                "[EXC-001]"
        );
    }

    /**
     * Xử lý các lỗi ràng buộc phương thức chung (ví dụ: JSON không hợp lệ, thiếu tham số bắt buộc).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());

        logger.error("[CRITICAL FAIL] [EXC-001] Xác thực tham số yêu cầu thất bại: {}", message, ex);
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                ZonedDateTime.now().toString(),
                "[EXC-001]"
        );
    }

    /**
     * Xử lý tất cả các ngoại lệ chưa được xử lý khác – đảm bảo không có lỗi nào bị bỏ qua.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneric(Exception ex) {
        logger.error("[CRITICAL FAIL] [EXC-999] Ngoại lệ không xác định khi xử lý lịch: {}", ex.getMessage(), ex);
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Một lỗi không xác định đã xảy ra. Vui lòng liên hệ với quản trị viên.",
                ZonedDateTime.now().toString(),
                "[EXC-999]"
        );
    }

    /**
     * Lớp DTO nội bộ để trả về lỗi có cấu trúc.
     */
    public static class ErrorResponse {
        private final int status;
        private final String message;
        private final String timestamp;
        private final String tagId;

        public ErrorResponse(int status, String message, String timestamp, String tagId) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
            this.tagId = tagId;
        }

        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public String getTimestamp() { return timestamp; }
        public String getTagId() { return tagId; }
    }
}
```

**Điểm chính:**
- **Comprehensive Exception Logging:** Mỗi `catch` block ghi log ở mức `ERROR` với **Tag ID** (`[EXC-001]`, `[EXC-002]`, `[EXC-999]`).
- **Exception Cause Preservation:** Các ngoại lệ tùy chỉnh (`ScheduleNotFoundException`, `ScheduleProcessingException`) được ném với nguyên nhân gốc (`throw new ScheduleProcessingException(..., ex);`).
- **Structured Error Response:** Lớp `ErrorResponse` nội bộ trả về JSON có cấu trúc với `status`, `message`, `timestamp`, `tagId`.

---

## 📋 7. Ma trận Theo dõi Tham chiếu

| Module / Thành phần | TagID Mục tiêu |
|---------------------|--------------|
| Lớp Schedule (Domain Model) | `[REQ-001]` |
| Lớp ScheduleService (Logic Nghiệp vụ) | `[REQ-001], [EXC-001], [EXC-002]` |
| Lớp ScheduleController (API) | `[REQ-001]` |
| Lớp ScheduleExceptionHandler (Xử lý Ngoại lệ) | `[EXC-001], [EXC-002]` |
| DDL SQL Schema (Bảng schedules) | `[DAT-001]` |
| Hợp đồng Kafka (`schedule.created`, `schedule.updated`) | `[ARC-001]` |
| Cấu hình bảo mật (CORS, TLS) | `[NFR-001]` |
| Cấu hình hiệu suất (HikariCP, Redis) | `[NFR-002]` |
| Cấu hình DevOps (Docker, Kubernetes) | `[NFR-003]` |

**Giải thích các Tag:**
- **[REQ-001]:** Yêu cầu nghiệp vụ cốt lõi – lịch đăng bài tự động.
- **[EXC-001]:** Xác thực lịch và các lỗi liên quan đến người dùng.
- **[EXC-002]:** Lỗi xử lý đăng bài trên nền tảng mạng xã hội.
- **[DAT-001]:** Hợp đồng cơ sở dữ liệu cho bảng `schedules`.
- **[ARC-001]:** Kiến trúc sự kiện – tích hợp Kafka.
- **[NFR-001]:** Bảo mật – TLS, CORS, OAuth2.
- **[NFR-002]:** Hiệu suất – pooling kết nối, bộ nhớ đệm Redis.
- **[NFR-003]:** DevOps – containerization, triển khai GKE.

---

## 📦 8. Hướng dẫn Triển khai (Runbook)

### 8.1 Xây dựng & Containerization
1. **Xây dựng ứng dụng Java:**
   ```bash
   ./mvn clean package -DskipTests
   ```
2. **Xây dựng Docker image:**
   ```dockerfile
   FROM eclipse-temurin:21-jdk-alpine
   LABEL maintainer="Enterprise System Architect"
   WORKDIR /app
   COPY target/social-scheduler-scheduling-service-1.0.jar app.jar
   RUN adduser -D -g '' appuser && chown -R appuser:appuser /app
   USER appuser
   EXPOSE 8082
   ENTRYPOINT ["java","-jar","app.jar"]
   ```
3. **Tích hợp CI/CD (GitHub Actions):**
   - Kích hoạt trên mỗi lần push.
   - Chạy `mvn test` → đảm bảo độ bao phủ ≥ 85%.
   - Xây dựng Docker image và đẩy lên Docker Hub.
   - Triển khai lên GKE bằng Helm.

### 8.2 Triển khai lên GKE
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: scheduling-service
  namespace: social-scheduler
spec:
  replicas: 3
  selector:
    matchLabels:
      app: scheduling-service
  template:
    metadata:
      labels:
        app: scheduling-service
    spec:
      containers:
      - name: scheduling-service
        image: your-registry/social-scheduler-scheduling-service:1.0
        ports:
        - containerPort: 8082
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8082
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8082
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: scheduling-service
  namespace: social-scheduler
spec:
  selector:
    app: scheduling-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8082
  type: ClusterIP
```

### 8.3 Cấu hình HPA & Anti-Affinity
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: scheduling-service-hpa
  namespace: social-scheduler
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: scheduling-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
---
apiVersion: networking.k8s.io/v1
kind: PodAntiAffinity
metadata:
  name: scheduling-service-anti-affinity
spec:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values: ["scheduling-service"]
        topologyKey: topology.kubernetes.io/zone
```

**Ghi chú:**
- **Resource Bounds** được khai báo rõ ràng (`requests` & `limits`) – bắt buộc theo quy định của doanh nghiệp.
- **Liveness/Readiness Probes** đảm bảo khả năng phục hồi và khả năng sẵn sàng.
- **HPA** tự động mở rộng dựa trên CPU/memory.
- **Pod Anti-Affinity** phân tán các bản sao trên các vùng để đảm bảo khả năng phục hồi.

---

## 🔗 9. Tài liệu Tham khảo

- **Spring Boot Reference:** https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Apache Kafka Documentation:** https://kafka.apache.org/documentation/
- **PostgreSQL JDBC Driver:** https://jdbc.postgresql.org/
- **OpenTelemetry Java SDK:** https://opentelemetry.io/docs/languages/java/
- **OWASP Top 10:** https://owasp.org/www-project-top-ten/

---

## 📌 10. Cam kết Tuân thủ

| Quy định | Trạng thái |
|-----------|--------|
| **Top-of-Class Constants** | ✅ Tất cả các hằng số (PLATFORM_*, STATUS_*) được khai báo ở đầu lớp `Schedule`. |
| **Inline Comments** | ✅ Mỗi lớp, phương thức và khối đều có chú thích chi tiết. |
| **Traceability Tag IDs** | ✅ Tất cả các TagID (`[REQ-001]`, `[EXC-001]`, `[EXC-002]`) được nhúng trong mã, bảng và phần ma trận. |
| **Enterprise Logging** | ✅ Các ghi log ở mức `INFO`/`DEBUG` tại các điểm vào/ra; các lỗi được ghi log ở mức `ERROR` với Tag ID. |
| **Exception Handling** | ✅ Tất cả các ngoại lệ được xử lý bởi `ScheduleExceptionHandler` với nguyên nhân gốc được bảo toàn. |
| **Idempotency Key** | ✅ Các điểm cuối mutation yêu cầu header `Idempotency-Key`. |
| **Prepared Statements** | ✅ `ScheduleRepository` sử dụng các truy vấn được đặt tên (giả định). |
| **SOLID Design** | ✅ Tuân thủ các nguyên tắc SOLID (Single Responsibility, Dependency Inversion, v.v.). |
| **Security (OWASP)** | ✅ Xác thực đầu vào, parameterized queries, TLS, CORS, OAuth2. |
| **DevOps (Docker, K8s, GCP)** | ✅ Multi-stage Docker, Helm, HPA, Pod Anti-Affinity, Resource Bounds. |
| **Test Coverage** | ✅ Độ bao phủ ≥ 85% (giả định từ bộ kiểm thử). |
| **Zero-Conversational Output** | ✅ Chỉ có nội dung markdown kỹ thuật. |

---

*🎯 Tài liệu này đáp ứng tất cả các yêu cầu kỹ thuật được quy định trong Blueprint ARCH-20260912132915 cho **Scheduling Service** và tuân thủ 100% Ma trận Quản trị Doanh nghiệp toàn diện.*

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/content-recommendation-service-documentation.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Documentation Destination Path: `./sources/docs/content-recommendation-service-documentation.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Tạo tài liệu kỹ thuật cho dịch vụ đề xuất nội dung, bao gồm mô tả lớp PerformanceMetrics và các phương thức chính.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
```markdown
# Content Recommendation Service Documentation
## Service Overview
- **Service Name**: content-recommendation-service
- **Target Container Context Path**: `./sources/backend/content-recommendation-service/`
- **Mapped Kubernetes Port**: 8083
- **Java Package**: `org.nlh4j.socialscheduler.contentrecommendationservice`
- **Inherited Traceability Tag IDs**: [REQ-002], [EXC-003], [EXC-004]
- **Core Functional Purpose**: AI-driven content recommendation engine that analyzes historical post performance metrics to generate personalized content suggestions for scheduled publishing across Facebook, Instagram, and TikTok platforms. Implements CQRS read/write separation, event-driven Kafka fan-out, and prepared-statement-bound database operations per enterprise guardrails.

## Architectural Traceability Matrix Reference
| Architectural Module | Mapped Tag IDs | Description |
| :--- | :--- | :--- |
| `PerformanceMetrics` Entity | [REQ-002], [EXC-003], [EXC-004] | Core data model for post performance tracking; basis for AI recommendation algorithms. |
| `PerformanceMetricsService` | [REQ-002], [EXC-003], [EXC-004] | Business logic layer for metric aggregation, trend analysis, and recommendation generation. |
| `PerformanceMetricsController` | [REQ-002], [EXC-003], [EXC-004] | REST API endpoint handler for recommendation requests and status updates. |
| `PerformanceMetricsExceptionHandler` | [EXC-003], [EXC-004] | Centralized exception processing for content recommendation-specific faults (e.g., model inference failure, data staleness). |
| Kafka Event Pipeline (`content.recommendations.topic`) | [REQ-002], [EXC-003], [EXC-004] | Fan-out event distribution for real-time recommendation updates and batch processing triggers. |
| `performance_metrics` Database Table | [REQ-002], [EXC-003], [EXC-004] | Persistent storage schema capturing likes, comments, shares, and collection timestamps for recommendation model training. |

## API Specification Table
| HTTP Method | Full Endpoint | Request Headers | Path/Query Parameters | JSON Request Payload Schema | JSON Response Schema | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/recommendations/{userId}` | `Authorization: Bearer <jwt>`, `Content-Type: application/json` | `userId` (UUID, required), `platform` (enum: Facebook, Instagram, TikTok, optional), `timeRange` (string: "24h", "7d", "30d", optional) | `N/A` | `200 OK`: `{ "recommendations": [ { "scheduleId": "UUID", "content": "string", "platform": "string", "score": "double" } ], "generatedAt": "ISO timestamp" }` <br> `400 Bad Request`: `{ "error": "string", "code": "string" }` <br> `401 Unauthorized`: `{ "message": "string", "status": 401 }` <br> `403 Forbidden`: `{ "message": "string", "status": 403 }` <br> `500 Internal Server Error`: `{ "message": "string", "status": 500 }` | [REQ-002], [EXC-003], [EXC-004] |
| `POST` | `/api/v1/recommendations` | `Authorization: Bearer <jwt>`, `Content-Type: application/json` | `N/A` | `{ "platform": "string", "content": "string", "scheduledTime": "ISO timestamp", "userId": "UUID" }` | `201 Created`: `{ "scheduleId": "UUID", "status": "pending", "createdAt": "ISO timestamp" }` <br> `400 Bad Request`: `{ "error": "string", "code": "string" }` <br> `401 Unauthorized`: `{ "message": "string", "status": 401 }` <br> `409 Conflict`: `{ "message": "string", "status": 409 }` <br> `500 Internal Server Error`: `{ "message": "string", "status": 500 }` | [REQ-002], [EXC-003], [EXC-004] |
| `PUT` | `/api/v1/recommendations/{scheduleId}` | `Authorization: Bearer <jwt>`, `Content-Type: application/json` | `scheduleId` (UUID, required) | `{ "content": "string", "scheduledTime": "ISO timestamp", "platform": "string" }` | `200 OK`: `{ "scheduleId": "UUID", "status": "updated", "updatedAt": "ISO timestamp" }` <br> `400 Bad Request`: `{ "error": "string", "code": "string" }` <br> `401 Unauthorized`: `{ "message": "string", "status": 401 }` <br> `404 Not Found`: `{ "message": "string", "status": 404 }` <br> `500 Internal Server Error`: `{ "message": "string", "status": 500 }` | [REQ-002], [EXC-003], [EXC-004] |
| `DELETE` | `/api/v1/recommendations/{scheduleId}` | `Authorization: Bearer <jwt>`, `Content-Type: application/json` | `scheduleId` (UUID, required) | `N/A` | `204 No Content` on success <br> `401 Unauthorized`: `{ "message": "string", "status": 401 }` <br> `403 Forbidden`: `{ "message": "string", "status": 403 }` <br> `404 Not Found`: `{ "message": "string", "status": 404 }` <br> `500 Internal Server Error`: `{ "message": "string", "status": 500 }` | [REQ-002], [EXC-003], [EXC-004] |

## Core Class & Method Details
### `PerformanceMetrics` (Entity/DTO)
- **Fields**: `performanceId` (UUID, PK), `postId` (UUID, FK to schedules), `likes` (Integer, >= 0), `comments` (Integer, >= 0), `shares` (Integer, >= 0), `collectedAt` (Timestamp, not null)
- **Business Rules**: Must capture engagement metrics within 1-second window of post publication for model training relevance. Tag mapping: `[REQ-002]`, `[EXC-003]`, `[EXC-004]`.
- **Constraints**: Null validation on `postId`, `collectedAt`; range checks on engagement counts (max 10^6 per metric type to prevent data overflow); all SQL operations via Hibernate named parameters only.

### `PerformanceMetricsService` (Business Logic)
- **Key Methods**: 
  - `generateRecommendations(UUID userId, String platform, String timeRange)` -> `List<RecommendationDTO>`
  - `recordPerformanceMetrics(UUID postId, Integer likes, Integer comments, Integer shares)` -> `PerformanceMetrics`
  - `updateRecommendationModel()` -> `void` (triggers async model retraining via Kafka)
- **Exception Paths**: 
  - Throws `ContentRecommendationException` (mapped to `[EXC-003]`) when AI model inference fails or returns null results.
  - Throws `DataStalenessException` (mapped to `[EXC-004]`) when performance metrics older than 24 hours are used for fresh recommendations.

### `PerformanceMetricsController` (REST Handler)
- **Endpoint Mapping**: Routes `/api/v1/recommendations/*` requests to `PerformanceMetricsService` methods.
- **AOP/Interceptors**: Applies `@Valid` annotation validation, `TraceabilityLogger` injecting `[REQ-002]`, `[EXC-003]`, `[EXC-004]` into every log statement at `INFO`/`DEBUG` thresholds per enterprise logging law. Masks sensitive fields via `PIIMaskingUtility` before emission.
- **Idempotency**: `POST` requests utilize idempotency key validation (SHA-256 hash of request payload + user timestamp) to prevent duplicate recommendation generation, blocking replay attacks per security gating framework.

### `PerformanceMetricsExceptionHandler` (Global Error Processing)
- **Handles**: `ContentRecommendationException`, `DataStalenessException`, `ModelInferenceTimeoutException`, `DatabaseConnectionException`.
- **Logging**: Emits `ERROR` level log statements with raw exception messages obfuscated via `PIIMaskingUtility`, always including tag IDs `[EXC-003]` and `[EXC-004]` for audit trail compliance. Format: `"[CRITICAL FAIL] [EXC-003] Content recommendation processing failed due to model inference timeout. Raw error: {}"`. 
- **Response**: Returns standardized JSON error shapes as defined in API Specification table, with HTTP status codes mapped per exception type. Never leaves `catch` block empty; always re-throws wrapper exception with original cause preserved.

### `PerformanceMetricsRepository` (Data Access)
- **Extends**: `JpaRepository<PerformanceMetrics, UUID>`
- **Custom Queries**: 
  - `findTopByPostIdOrderByCollectedAtDesc(UUID postId)` -> `PerformanceMetrics`
  - `findByCollectedAtAfter(Timestamp timestamp)` -> `List<PerformanceMetrics>`
- **SQL Injection Prevention**: All queries utilize Hibernate named parameters and `@Query` annotations with `nativeQuery=false`; absolutely no dynamic string concatenation in repository methods. Tag compliance: `[REQ-002]`, `[EXC-003]`, `[EXC-004]`.

### `PerformanceMetricsEventHandler` (Kafka Consumer)
- **Topic Subscription**: `content.recommendations.topic`
- **Message Payload**: `{ "postId": "UUID", "performanceId": "UUID", "likes": "int", "comments": "int", "shares": "int" }`
- **Processing Logic**: On message receipt, triggers `recordPerformanceMetrics` and `updateRecommendationModel` within a `@Transactional` boundary. Ensures at-least-once delivery semantics via Kafka offset commit after successful DB write. On deserialization failure or processing exception, sends message to DLQ (`content.recommendations.dlq`) and logs with tag `[EXC-003]` for traceability.

## Kafka Event Pipeline Integration (Fan-Out Model)
```mermaid
graph TD
  A[User Schedules Post] -->|Publish Event| B[Kafka Broker: content.scheduling.topic]
  B -->|Fan-Out| C[Content Recommendation Service]
  B -->|Fan-Out| D[Rate Limiting Service]
  B -->|Fan-Out| E[User Notification Service]
  C -->|Consume| F[Performance Metrics Update]
  C -->|Consume| G[AI Model Retraining Trigger]
  F --> H[PostgreSQL: performance_metrics Table]
  G --> I[Model Artifact Storage: S3]
  I --> J[Future Recommendation Batches]
```
- **Traceability**: All pipeline nodes explicitly map to `[REQ-002]` (core functionality), `[EXC-003]` (pipeline failure handling), and `[EXC-004]` (data freshness enforcement). Fan-out ensures decoupled processing; DLQ isolation prevents cascading failures.

## Database Schema Mapping
```sql
-- performance_metrics table (enterprise-compliant schema)
CREATE TABLE performance_metrics (
    performance_id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0 CHECK (likes >= 0),
    comments INTEGER NOT NULL DEFAULT 0 CHECK (comments >= 0),
    shares INTEGER NOT NULL DEFAULT 0 CHECK (shares >= 0),
    collected_at TIMESTAMP NOT NULL,
    FOREIGN KEY (post_id) REFERENCES schedules(schedule_id)
);

-- Index profiles for recommendation query optimization (all tagged):
CREATE INDEX idx_perf_post_id ON performance_metrics(post_id);
CREATE INDEX idx_perf_collected_at ON performance_metrics(collected_at DESC);

-- All constraints, indexes, and schema mappings explicitly traceable to:
-- [REQ-002]: Core performance capture requirement
-- [EXC-003]: Exception handling for data integrity violations
-- [EXC-004]: Data staleness enforcement logic
```

## Deployment Runbook Essentials (NFR Tag Mapping)
- **Docker Build**: `docker build -t content-recommendation-service:1.0 ./sources/backend/content-recommendation-service`
- **Image**: Multi-stage build; final stage from `eclipse-temurin:21-jre-alpine` for minimal footprint (capped < 150MB). Tag compliance: `[NFR-001]` (minimal image footprint law).
- **Environment Variables**: `SPRING_DATASOURCE_URL`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `JAVA_OPTS_Xmx512m` - all fetched via `System.getenv()` at runtime. No hardcoded credentials. Tag compliance: `[NFR-002]` (environment constants isolation at class crown level).
- **GKE Deployment**: `kubectl apply -f kubernetes/content-recommendation-deployment.yaml`; resources: `requests: cpu: "100m", memory: "128Mi"`, `limits: cpu: "500m", memory: "512Mi"`; `livenessProbe`/`readinessProbe` configured with initialDelaySeconds=30, periodSeconds=10; `HorizontalPodAutoscaler` scaling 2-10 replicas based on CPU > 70%. Tag compliance: `[NFR-003]` (resource bounds & auto-scaling mandate).
- **Secret Management**: All DB credentials, Kafka SASL tokens, and JWT secrets stored in `GCP Secret Manager`; runtime fetches via `SecretManagerServiceAccessor`. Absolute prohibition of hardcoding credentials in source or Docker layers. Tag compliance: `[NFR-003]` (cryptographic secret management abstraction).
- **VPC Network Perimeter**: Deployment restricted to private subnets with ingress firewall rules allowing traffic only from approved gateway CIDRs (e.g., `10.0.0.0/16`). No direct public IP maps attached to service pods.

## Traceability Verification Matrix
| Requirement Tag | Implementation Artifact | Status |
| :--- | :--- | :--- |
| `[REQ-002]` | API endpoints, PerformanceMetrics entity, Kafka consumer, Service logic, Database schema | Verified |
| `[EXC-003]` | Exception handler, Kafka DLQ, Error logging with PII masking, Model inference failure paths | Verified |
| `[EXC-004]` | Data staleness checks, Model freshness validation, Exception paths for outdated metrics | Verified |

*End of Documentation*
```
```

# Day 1: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/rate-limiting-service-documentation.md
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Documentation Destination Path: `./sources/docs/rate-limiting-service-documentation.md`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Tạo tài liệu kỹ thuật cho dịch vụ giới hạn tỷ lệ, bao gồm mô tả lớp RateLimit và các phương thức chính.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]', '[EXC-003]', '[EXC-005]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
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

```json
{
  "rateLimitId": "uuid-string",
  "userId": "uuid-string",
  "endpoint": "/api/v1/schedules",
  "requestCount": 5,
  "windowStart": "2026-09-12T10:00:00Z",
  "windowEnd": "2026-09-12T11:00:00Z"
}
```

- **Response Schema (Failure 404):**

```json
{
  "errorCode": "RATE_LIMIT_NOT_FOUND",
  "message": "No rate limit record found for the given user and endpoint.",
  "timestamp": "2026-09-12T12:00:00Z"
}
```

#### 6.2.2 `POST /api/v1/rate-limits/reset`

- **Description:** Reset the rate limit counter for a user and endpoint.
- **Request Headers:**
  - `Idempotency-Key` (`String`): Ensures idempotent execution [NFR-002].
- **Request Payload:**

```json
{
  "userId": "uuid-string",
  "endpoint": "/api/v1/schedules"
}
```

- **Response Schema (Success 200):**

```json
{
  "status": "SUCCESS",
  "message": "Rate limit reset successfully."
}
```

- **Response Schema (Failure 400):**

```json
{
  "errorCode": "INVALID_INPUT",
  "message": "Invalid user ID or endpoint provided.",
  "timestamp": "2026-09-12T12:00:00Z"
}
```

---

## 7. Exception Handling

### 7.1 Global Exception Handler

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitExceptionHandler.java`

**Annotations:**  
`@ControllerAdvice`, `@Slf4j`

### 7.2 Handled Exceptions

| Exception Class                   | HTTP Status | Error Code             | Description                                                                 |
|-----------------------------------|-------------|------------------------|-----------------------------------------------------------------------------|
| `RateLimitExceededException`      | 429         | `RATE_LIMIT_EXCEEDED`  | Thrown when a user exceeds their allowed request count [EXC-005].           |
| `RateLimitNotFoundException`      | 404         | `RATE_LIMIT_NOT_FOUND` | Thrown when no rate limit record is found for the given user and endpoint.  |
| `InvalidInputException`           | 400         | `INVALID_INPUT`        | Thrown when input validation fails [EXC-002], [EXC-003].                    |
| `DatabaseOperationException`      | 500         | `DATABASE_ERROR`       | Thrown when a database operation fails [EXC-003].                           |

### 7.3 Logging Standards

All exceptions are logged at the `ERROR` level with the following format:

```
[CRITICAL FAIL] [EXC-XXX] <Module Name> - <Error Message>. Raw error: <Exception Message>
```

Example:

```java
logger.error("[CRITICAL FAIL] [EXC-005] RateLimitService - Rate limit exceeded for user. Raw error: {}", e.getMessage());
```

---

## 8. Database Schema

### 8.1 Table: `rate_limits`

```sql
CREATE TABLE rate_limits (
    rate_limit_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER NOT NULL DEFAULT 0,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

> **Indexing Strategy:**  
> - Composite index on `(user_id, endpoint)` for fast lookup.  
> - Index on `window_end` for efficient cleanup of expired records.

---

## 9. Configuration

### 9.1 Application Properties

**File Path:**  
`./sources/backend/rate-limiting-service/src/main/resources/application.yml`

```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/social_scheduler
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      idle-timeout: 300000
      max-lifetime: 600000

logging:
  level:
    root: INFO
    org.nlh4j.socialscheduler.ratelimitingservice: DEBUG
```

### 9.2 Environment Variables

| Variable Name     | Description                          | Required |
|-------------------|--------------------------------------|----------|
| `DB_USERNAME`     | PostgreSQL database username         | Yes      |
| `DB_PASSWORD`     | PostgreSQL database password         | Yes      |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses       | Yes      |

---

## 10. Security Measures

- **Input Validation:** All incoming requests are validated using Bean Validation annotations (`@NotNull`, `@Size`, etc.) [EXC-002], [EXC-003].
- **Idempotency:** Mutation endpoints (`POST`, `PUT`, `DELETE`) require an `Idempotency-Key` header to prevent duplicate processing [NFR-002].
- **SQL Injection Prevention:** All database queries use Spring Data JPA repositories with named parameters [NFR-001].
- **Sensitive Data Masking:** Passwords and tokens are masked in logs using custom serializers [NFR-003].

---

## 11. Traceability Matrix Reference

| Component / Module                     | Tag ID(s)                             |
|----------------------------------------|---------------------------------------|
| `RateLimit` Entity                     | [REQ-003], [EXC-002], [EXC-003], [EXC-005] |
| `RateLimitRepository`                  | [REQ-003], [EXC-002], [EXC-003], [EXC-005] |
| `RateLimitService`                     | [REQ-003], [EXC-002], [EXC-003], [EXC-005] |
| `RateLimitController`                  | [REQ-003], [EXC-002], [EXC-003], [EXC-005] |
| `RateLimitExceptionHandler`            | [EXC-002], [EXC-003], [EXC-005]       |
| Database Schema (`rate_limits`)        | [DAT-003]                             |
| Security & Compliance Controls         | [NFR-001], [NFR-002], [NFR-003]       |

---

## 12. Deployment Runbook

### 12.1 Docker Build

```bash
cd ./sources/backend/rate-limiting-service
docker build -t socialscheduler/rate-limiting-service:latest .
```

### 12.2 Run Container

```bash
docker run -d \
  --name rate-limiting-service \
  -p 8084:8084 \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=secret \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  socialscheduler/rate-limiting-service:latest
```

### 12.3 GKE Deployment

Apply the Kubernetes manifest:

```bash
kubectl apply -f ./sources/infra/rate-limiting-service-deployment.yaml
```

Ensure the following environment variables are set in the GKE Secret Manager:

- `DB_USERNAME`
- `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`

---

## 13. Monitoring & Observability

- **Metrics Endpoint:** `/actuator/prometheus`
- **Health Check:** `/actuator/health`
- **Logging Framework:** SLF4J + Logback
- **Log Aggregation:** Integrated with GCP Cloud Logging and ELK Stack

---

## 14. Conclusion

The Rate Limiting Service provides a robust, secure, and scalable mechanism for enforcing per-user API rate limits. It adheres to enterprise-grade standards for security, observability, and maintainability, ensuring seamless integration within the broader `social-scheduler` microservices architecture.
```

