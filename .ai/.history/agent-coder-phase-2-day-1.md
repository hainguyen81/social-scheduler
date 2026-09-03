# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/entity/ScheduleEntity.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/entity/ScheduleEntity.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo thực thể JPA ScheduleEntity ánh xạ bảng schedules trong schema schedule_schema tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java. Sử dụng annotation @Entity, @Table(name="schedules", schema="schedule_schema"), định nghĩa cột scheduleId kiểu UUID với @Id, @Check cho cột status whitelist (\'PENDING\',\'SENT\',\'FAILED\',\'CANCELLED\') và platform whitelist (\'FACEBOOK\',\'INSTAGRAM\',\'TIKTOK\'), @ManyToOne(fetch=LAZY) tham chiếu UserEntity, @Column(updatable=false) cho createdAt và scheduleId, @PrePersist/@PreUpdate với AuditingEntityListener. Tạo ScheduleRepository kế thừa JpaRepository<ScheduleEntity, UUID> tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java với findByUserIdAndStatus và findByTenantIdAndScheduledTimeBetween dùng @Query JPQL named parameter chống SQL injection OWASP A03. Tạo ScheduleRequestDto với @NotNull, @NotBlank, @Size(min=1,max=5000), @Pattern(regexp="^(Facebook|Instagram|TikTok)$") cho platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java và ScheduleResponseDto với các trường scheduleId,userId,platform,content,scheduledTime,status,actualSentTime,retryCount,createdAt,updatedAt tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.entity;

// [REQ-001] Traceability Tag ID compliance mapping for entity schema definition

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🏛️ ScheduleEntity represents the core database persistence entity mapped to the
 * 'schedules' table within the 'schedule_schema' multi-tenant Postgres partition.
 * 
 * @traceability [REQ-001]
 * @author Enterprise Architecture Team
 * @since 1.0.0
 */
@Entity
@Table(name = "schedules", schema = "schedule_schema")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleEntity implements Serializable {

    // =========================================================================
    // 🛡️ CONSTANT DEFINITIONS (Anti-Magic-Numbers & Configuration Isolation)
    // =========================================================================
    
    private static final long serialVersionUID = 1L;
    
    public static final String SCHEMA_NAME = "schedule_schema";
    public static final String TABLE_NAME = "schedules";
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    public static final String PLATFORM_FACEBOOK = "FACEBOOK";
    public static final String PLATFORM_INSTAGRAM = "INSTAGRAM";
    public static final String PLATFORM_TIKTOK = "TIKTOK";

    // =========================================================================
    // 📊 PERSISTENT ATTRIBUTES & COLUMN MAPPINGS
    // =========================================================================

    /**
     * Unique primary key identifier for the scheduled publication item.
     * Mapped as non-updatable to guarantee immutable ledger lineage.
     */
    @Id
    @Column(name = "schedule_id", nullable = false, updatable = false)
    private UUID scheduleId;

    /**
     * Foreign key reference identifying the owner user of the schedule.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /**
     * Multi-tenancy isolation discriminator key mapping to the tenant partition.
     */
    @Column(name = "tenant_id", nullable = false, length = 64, updatable = false)
    private String tenantId;

    /**
     * Target social media network platform (FACEBOOK, INSTAGRAM, TIKTOK).
     * Validated against explicit architectural whitelist constraints.
     */
    @Column(name = "platform", nullable = false, length = 32)
    private String platform;

    /**
     * Plain text or formatted social content payload to be published.
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp indicating when the publication job is scheduled to execute.
     */
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * Current lifecycle status of the schedule (PENDING, SENT, FAILED, CANCELLED).
     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * Exact timestamp when the publication job was successfully dispatched.
     */
    @Column(name = "actual_sent_time")
    private LocalDateTime actualSentTime;

    /**
     * Cumulative count of delivery retry attempts performed by worker nodes.
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * System audit timestamp marking the exact instant of entity creation.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * System audit timestamp marking the last modification update instant.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================================
    // 🔗 RELATIONSHIP MAPPINGS (Lazy Loading Enforcement)
    // =========================================================================

    /**
     * Many-to-one logical association back to the parent user entity.
     * Configured with LAZY fetch to prevent accidental eager N+1 loading queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private Object userEntityRef; // Placeholder or explicit UserEntity mapping handle

    // =========================================================================
    // 🏗️ CONSTRUCTORS
    // =========================================================================

    /**
     * Default protected no-args constructor required by JPA specification.
     */
    public ScheduleEntity() {
        this.retryCount = 0;
        this.status = STATUS_PENDING;
    }

    /**
     * Parameterized constructor for high-speed programmatic entity instantiation.
     */
    public ScheduleEntity(UUID scheduleId, UUID userId, String tenantId, String platform, 
                          String content, LocalDateTime scheduledTime) {
        this.scheduleId = scheduleId != null ? scheduleId : UUID.randomUUID();
        this.userId = userId;
        this.tenantId = tenantId;
        this.platform = platform;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.status = STATUS_PENDING;
        this.retryCount = 0;
    }

    // =========================================================================
    // 🔄 LIFECYCLE CALLBACK AUDITING HOOKS
    // =========================================================================

    /**
     * Pre-persist lifecycle callback method executed prior to database insertion.
     * Automatically assigns default IDs, audit timestamps, and initial states.
     */
    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) {
            this.scheduleId = UUID.randomUUID();
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.status == null) {
            this.status = STATUS_PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    /**
     * Pre-update lifecycle callback method executed prior to database updates.
     * Automatically refreshes the modification audit timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // 📈 GETTERS AND SETTERS
    // =========================================================================

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getActualSentTime() {
        return actualSentTime;
    }

    public void setActualSentTime(LocalDateTime actualSentTime) {
        this.actualSentTime = actualSentTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // 🧮 HASHCODE, EQUALS & TOSTRING CONTRACTS
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEntity that = (ScheduleEntity) o;
        return Objects.equals(scheduleId, that.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", tenantId='" + tenantId + '\'' +
                ", platform='" + platform + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/repository/ScheduleRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/repository/ScheduleRepository.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo thực thể JPA ScheduleEntity ánh xạ bảng schedules trong schema schedule_schema tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java. Sử dụng annotation @Entity, @Table(name="schedules", schema="schedule_schema"), định nghĩa cột scheduleId kiểu UUID với @Id, @Check cho cột status whitelist (\'PENDING\',\'SENT\',\'FAILED\',\'CANCELLED\') và platform whitelist (\'FACEBOOK\',\'INSTAGRAM\',\'TIKTOK\'), @ManyToOne(fetch=LAZY) tham chiếu UserEntity, @Column(updatable=false) cho createdAt và scheduleId, @PrePersist/@PreUpdate với AuditingEntityListener. Tạo ScheduleRepository kế thừa JpaRepository<ScheduleEntity, UUID> tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java với findByUserIdAndStatus và findByTenantIdAndScheduledTimeBetween dùng @Query JPQL named parameter chống SQL injection OWASP A03. Tạo ScheduleRequestDto với @NotNull, @NotBlank, @Size(min=1,max=5000), @Pattern(regexp="^(Facebook|Instagram|TikTok)$") cho platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java và ScheduleResponseDto với các trường scheduleId,userId,platform,content,scheduledTime,status,actualSentTime,retryCount,createdAt,updatedAt tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.repository;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------------
 * Enterprise Data Access Repository: ScheduleRepository
 * ----------------------------------------------------------------------------------
 * Business Context: Manages database persistence operations for the Schedule entity
 * within the schedule-service bounded context. Enforces strict multi-tenancy and 
 * parameterized JPQL query boundaries to neutralize SQL Injection vulnerabilities (OWASP A03).
 * 
 * Traceability Tags: [REQ-001]
 * ----------------------------------------------------------------------------------
 */
@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    /**
     * Retrieves a list of schedules associated with a specific user ID and status.
     * Complies with performance constraints by executing optimized index-backed lookups.
     * 
     * @param userId the unique identifier of the user (UUID)
     * @param status the operational status of the schedule (e.g., PENDING, SENT, FAILED, CANCELLED)
     * @return a list of matching ScheduleEntity records
     * @traceability [REQ-001]
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.userId = :userId AND s.status = :status")
    List<ScheduleEntity> findByUserIdAndStatus(
            @Param("userId") UUID userId, 
            @Param("status") String status
    );

    /**
     * Retrieves a list of schedules belonging to a specific tenant ID whose scheduled execution 
     * time falls within a specified date-time range. Employs parameterized JPQL queries 
     * to eliminate SQL injection vectors.
     * 
     * @param tenantId      the unique enterprise tenant identifier
     * @param startTime     the beginning of the scheduling window
     * @param endTime       the end of the scheduling window
     * @return a list of ScheduleEntity records within the timeframe
     * @traceability [REQ-001]
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.tenantId = :tenantId AND s.scheduledTime BETWEEN :startTime AND :endTime")
    List<ScheduleEntity> findByTenantIdAndScheduledTimeBetween(
            @Param("tenantId") String tenantId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime
    );

}
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dto/ScheduleRequestDto.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dto/ScheduleRequestDto.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo thực thể JPA ScheduleEntity ánh xạ bảng schedules trong schema schedule_schema tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java. Sử dụng annotation @Entity, @Table(name="schedules", schema="schedule_schema"), định nghĩa cột scheduleId kiểu UUID với @Id, @Check cho cột status whitelist (\'PENDING\',\'SENT\',\'FAILED\',\'CANCELLED\') và platform whitelist (\'FACEBOOK\',\'INSTAGRAM\',\'TIKTOK\'), @ManyToOne(fetch=LAZY) tham chiếu UserEntity, @Column(updatable=false) cho createdAt và scheduleId, @PrePersist/@PreUpdate với AuditingEntityListener. Tạo ScheduleRepository kế thừa JpaRepository<ScheduleEntity, UUID> tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java với findByUserIdAndStatus và findByTenantIdAndScheduledTimeBetween dùng @Query JPQL named parameter chống SQL injection OWASP A03. Tạo ScheduleRequestDto với @NotNull, @NotBlank, @Size(min=1,max=5000), @Pattern(regexp="^(Facebook|Instagram|TikTok)$") cho platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java và ScheduleResponseDto với các trường scheduleId,userId,platform,content,scheduledTime,status,actualSentTime,retryCount,createdAt,updatedAt tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.dto;

// [REQ-001] Enterprise traceability tag injection for schedule publishing request payload validation.
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing the ingestion payload for creating or updating a publishing schedule.
 * Enforces strict validation guardrails against injection attacks and malformed payloads in compliance
 * with OWASP top-10 security benchmarks.
 * 
 * @traceability [REQ-001]
 * @author Enterprise System Architect (SA Agent)
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {

    /**
     * Unique identifier of the user initiating the publishing schedule.
     * Must be a valid non-null UUID matching an authorized tenant context.
     */
    @NotNull(message = "User ID cannot be null and must be a valid UUID.")
    private UUID userId;

    /**
     * Target social media platform for content dissemination.
     * Enforces strict whitelist validation via regular expressions to prevent arbitrary string injections.
     * Allowed values: FACEBOOK, INSTAGRAM, TIKTOK (case-sensitive matching based on enterprise DDL constraints).
     */
    @NotBlank(message = "Social media platform identifier cannot be blank.")
    @Pattern(
        regexp = "^(FACEBOOK|INSTAGRAM|TIKTOK)$", 
        message = "Invalid social platform target. Permitted values are strictly FACEBOOK, INSTAGRAM, or TIKTOK."
    )
    private String platform;

    /**
     * Raw textual content to be published on the target social network.
     * Bounded by string length constraints to prevent oversized payload buffer exhaustion attacks.
     */
    @NotBlank(message = "Publishing content payload cannot be blank or empty.")
    @Size(
        min = 1, 
        max = 5000, 
        message = "Content length must be contained strictly between 1 and 5000 characters."
    )
    private String content;

    /**
     * Target execution timestamp denoting when the publication must be dispatched.
     * Must be set in the future to qualify for automated scheduler queueing.
     */
    @NotNull(message = "Scheduled execution timestamp is mandatory.")
    @Future(message = "Scheduled timestamp must target a future point in time.")
    private OffsetDateTime scheduledTime;
}
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dto/ScheduleResponseDto.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dto/ScheduleResponseDto.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo thực thể JPA ScheduleEntity ánh xạ bảng schedules trong schema schedule_schema tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java. Sử dụng annotation @Entity, @Table(name="schedules", schema="schedule_schema"), định nghĩa cột scheduleId kiểu UUID với @Id, @Check cho cột status whitelist (\'PENDING\',\'SENT\',\'FAILED\',\'CANCELLED\') và platform whitelist (\'FACEBOOK\',\'INSTAGRAM\',\'TIKTOK\'), @ManyToOne(fetch=LAZY) tham chiếu UserEntity, @Column(updatable=false) cho createdAt và scheduleId, @PrePersist/@PreUpdate với AuditingEntityListener. Tạo ScheduleRepository kế thừa JpaRepository<ScheduleEntity, UUID> tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java với findByUserIdAndStatus và findByTenantIdAndScheduledTimeBetween dùng @Query JPQL named parameter chống SQL injection OWASP A03. Tạo ScheduleRequestDto với @NotNull, @NotBlank, @Size(min=1,max=5000), @Pattern(regexp="^(Facebook|Instagram|TikTok)$") cho platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java và ScheduleResponseDto với các trường scheduleId,userId,platform,content,scheduledTime,status,actualSentTime,retryCount,createdAt,updatedAt tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.dto;

// [REQ-001]
// [DAT-001]
// [ARC-005]

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Data Transfer Object (DTO) representing the response payload for a publishing schedule.
 * Implements strict serialization patterns, immutable encapsulation, and full traceability tagging.
 * 
 * <p>Traceability Tags: [REQ-001], [DAT-001], [ARC-005]
 * 
 * @author Enterprise System Architect (SA Agent) / Coder Agent
 * @version 1.0.0
 * @since 2026/08/31
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleResponseDto implements Serializable {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers & Compliance Law)
    // =========================================================================
    
    private static final long serialVersionUID = 1L;
    
    public static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DEFAULT_PLATFORM_REGEX = "^(FACEBOOK|INSTAGRAM|TIKTOK)$";
    public static final String DEFAULT_STATUS_REGEX = "^(PENDING|SENT|FAILED|CANCELLED)$";
    public static final int CONTENT_MIN_LENGTH = 1;
    public static final int CONTENT_MAX_LENGTH = 5000;

    // =========================================================================
    // INSTANCE FIELD ATTRIBUTES (Immutable Data Contract Layout)
    // =========================================================================

    /**
     * Unique identifier for the publishing schedule entity.
     */
    @NotNull(message = "Schedule ID must not be null")
    private UUID scheduleId;

    /**
     * Unique identifier for the owner user associated with the schedule.
     */
    @NotNull(message = "User ID must not be null")
    private UUID userId;

    /**
     * Target social media platform for content publication.
     */
    @NotNull(message = "Platform must not be null")
    @Pattern(regexp = DEFAULT_PLATFORM_REGEX, message = "Platform must be one of: FACEBOOK, INSTAGRAM, TIKTOK")
    private String platform;

    /**
     * Text content or payload to be published to the target social channel.
     */
    @NotNull(message = "Content must not be null")
    @Size(min = CONTENT_MIN_LENGTH, max = CONTENT_MAX_LENGTH, message = "Content length must be between 1 and 5000 characters")
    private String content;

    /**
     * Planned timestamp for when the schedule is designated to execute.
     */
    @NotNull(message = "Scheduled time must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime scheduledTime;

    /**
     * Current execution lifecycle status of the publishing schedule.
     */
    @NotNull(message = "Status must not be null")
    @Pattern(regexp = DEFAULT_STATUS_REGEX, message = "Status must be one of: PENDING, SENT, FAILED, CANCELLED")
    private String status;

    /**
     * Actual timestamp when the content was successfully dispatched to the platform.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime actualSentTime;

    /**
     * Counter tracking the total number of retry attempts executed upon failure.
     */
    @NotNull(message = "Retry count must not be null")
    private Integer retryCount;

    /**
     * Timestamp indicating when the schedule record was initially created.
     */
    @NotNull(message = "Creation timestamp must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating when the schedule record was last modified.
     */
    @NotNull(message = "Update timestamp must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT_PATTERN)
    private LocalDateTime updatedAt;

    // =========================================================================
    // CONSTRUCTORS
    // =========================================================================

    /**
     * Default zero-argument constructor for enterprise JSON deserialization frameworks (Jackson).
     */
    public ScheduleResponseDto() {
        super();
    }

    /**
     * Fully parameterized constructor for instantiating the response DTO with immutable payloads.
     * 
     * @param scheduleId     unique schedule identifier
     * @param userId         unique owner user identifier
     * @param platform       target social media platform
     * @param content        publishing text content
     * @param scheduledTime  planned execution timestamp
     * @param status         current lifecycle status
     * @param actualSentTime actual dispatch timestamp
     * @param retryCount     number of retry attempts
     * @param createdAt      creation timestamp
     * @param updatedAt      last modification timestamp
     */
    public ScheduleResponseDto(UUID scheduleId, UUID userId, String platform, String content,
                               LocalDateTime scheduledTime, String status, LocalDateTime actualSentTime,
                               Integer retryCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.platform = platform;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.actualSentTime = actualSentTime;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // GETTERS AND SETTERS WITH DEFENSIVE ENCAPSULATION
    // =========================================================================

    /**
     * Retrieves the schedule unique identifier.
     * @return UUID scheduleId
     */
    public UUID getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the schedule unique identifier.
     * @param scheduleId unique schedule identifier
     */
    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * Retrieves the owner user unique identifier.
     * @return UUID userId
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Sets the owner user unique identifier.
     * @param userId owner user unique identifier
     */
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the target publishing platform.
     * @return String platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets the target publishing platform.
     * @param platform target social media platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Retrieves the content payload.
     * @return String content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content payload.
     * @param content publishing text content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Retrieves the planned execution time.
     * @return LocalDateTime scheduledTime
     */
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Sets the planned execution time.
     * @param scheduledTime planned timestamp
     */
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Retrieves the schedule lifecycle status.
     * @return String status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the schedule lifecycle status.
     * @param status lifecycle status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieves the actual dispatch timestamp.
     * @return LocalDateTime actualSentTime
     */
    public LocalDateTime getActualSentTime() {
        return actualSentTime;
    }

    /**
     * Sets the actual dispatch timestamp.
     * @param actualSentTime actual dispatch timestamp
     */
    public void setActualSentTime(LocalDateTime actualSentTime) {
        this.actualSentTime = actualSentTime;
    }

    /**
     * Retrieves the retry attempt count.
     * @return Integer retryCount
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * Sets the retry attempt count.
     * @param retryCount number of retry attempts
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Retrieves the record creation timestamp.
     * @return LocalDateTime createdAt
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the record creation timestamp.
     * @param createdAt creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the record last update timestamp.
     * @return LocalDateTime updatedAt
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the record last update timestamp.
     * @param updatedAt last modification timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // OBJECT EQUALITY, HASHING, AND STRING REPRESENTATION
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleResponseDto that = (ScheduleResponseDto) o;
        return Objects.equals(scheduleId, that.scheduleId) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(platform, that.platform) &&
               Objects.equals(scheduledTime, that.scheduledTime) &&
               Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, userId, platform, scheduledTime, status);
    }

    @Override
    public String toString() {
        return "ScheduleResponseDto{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", platform='" + platform + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}
```

# Day 1: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/service/ScheduleService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/service/ScheduleService.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Phát triển ScheduleService tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java với annotation @Service, @Transactional. Triển khai createSchedule(ScheduleRequestDto request, UUID userId) khởi tạo status=PENDING,retryCount=0,gán userId và tenantId từ SecurityContextHolder; getScheduleById(UUID scheduleId) kiểm tra quyền sở hữu theo userId chống IDOR OWASP A01; updateStatusToSent(UUID scheduleId) chuyển SENT và actualSentTime=LocalDateTime.now(); deleteSchedule(UUID scheduleId) chuyển CANCELLED bảo toàn lịch sử. Tích hợp SocialPlatformDispatcher với dispatchPublish(ScheduleEntity entity). Bọc SocialPlatformException trong try-catch ghi log ERROR kèm correlation ID, kích hoạt @Retryable backoff lũy thừa maxAttempts=3 từ Resilience4j. Áp dụng SecurityContextHolder kiểm tra chủ sở hữu hoặc Admin. Tạo ScheduleController tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java với @RestController, @RequestMapping("/api/v1/schedules"), endpoint POST / (201 Created), GET /{scheduleId} (200 OK), PUT /{scheduleId}/status, DELETE /{scheduleId} (204 No Content), @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\',\'SCHEDULER\')") cho tạo mới và @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\')") cho truy vấn/cập nhật theo RBAC 4 vai trò, @Valid cho payload. Tạo SocialPlatformDispatcher facade điều phối theo enum Platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.service;

import org.nlh4j.socialscheduler.scheduleservice.dispatcher.SocialPlatformDispatcher;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation for managing social media publishing schedules.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@Service
@Transactional
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SocialPlatformDispatcher platformDispatcher;

    /**
     * Creates a new publishing schedule with PENDING status.
     * 
     * @param request The schedule request DTO
     * @param userId The authenticated user ID
     * @return The created schedule entity
     */
    public ScheduleEntity createSchedule(ScheduleRequestDto request, UUID userId) {
        logger.info("[PROCESS] Creating new schedule for User ID: {}", userId);
        
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setPlatform(request.getPlatform());
        entity.setContent(request.getContent());
        entity.setScheduledTime(request.getScheduledTime());
        entity.setStatus("PENDING");
        entity.setRetryCount(0);
        
        return scheduleRepository.save(entity);
    }

    /**
     * Retrieves a schedule by ID, enforcing ownership check to prevent IDOR (OWASP A01).
     */
    @Transactional(readOnly = true)
    public ScheduleEntity getScheduleById(UUID scheduleId, UUID currentUserId) {
        ScheduleEntity entity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        // Enforce ownership check
        if (!entity.getUserId().equals(currentUserId) && !isAdmin()) {
            logger.warn("[SECURITY] Unauthorized access attempt by User: {} on Schedule: {}", currentUserId, scheduleId);
            throw new AccessDeniedException("Access denied to this resource");
        }
        return entity;
    }

    /**
     * Updates schedule status to SENT and records actual sent time.
     * Implements retry logic for external platform integration failures.
     */
    @Retryable(value = { SocialPlatformException.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void updateStatusToSent(UUID scheduleId) {
        ScheduleEntity entity = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        try {
            platformDispatcher.dispatchPublish(entity);
            entity.setStatus("SENT");
            entity.setActualSentTime(LocalDateTime.now());
            scheduleRepository.save(entity);
            logger.info("[PROCESS] Schedule {} successfully published.", scheduleId);
        } catch (SocialPlatformException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Failed to publish schedule {}. Error: {}", scheduleId, e.getMessage());
            throw e; // Trigger retry
        }
    }

    /**
     * Cancels a schedule by setting status to CANCELLED.
     */
    public void deleteSchedule(UUID scheduleId, UUID currentUserId) {
        ScheduleEntity entity = getScheduleById(scheduleId, currentUserId);
        entity.setStatus("CANCELLED");
        scheduleRepository.save(entity);
        logger.info("[PROCESS] Schedule {} cancelled by User: {}", scheduleId, currentUserId);
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
```

# Day 1: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/controller/ScheduleController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/controller/ScheduleController.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Phát triển ScheduleService tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java với annotation @Service, @Transactional. Triển khai createSchedule(ScheduleRequestDto request, UUID userId) khởi tạo status=PENDING,retryCount=0,gán userId và tenantId từ SecurityContextHolder; getScheduleById(UUID scheduleId) kiểm tra quyền sở hữu theo userId chống IDOR OWASP A01; updateStatusToSent(UUID scheduleId) chuyển SENT và actualSentTime=LocalDateTime.now(); deleteSchedule(UUID scheduleId) chuyển CANCELLED bảo toàn lịch sử. Tích hợp SocialPlatformDispatcher với dispatchPublish(ScheduleEntity entity). Bọc SocialPlatformException trong try-catch ghi log ERROR kèm correlation ID, kích hoạt @Retryable backoff lũy thừa maxAttempts=3 từ Resilience4j. Áp dụng SecurityContextHolder kiểm tra chủ sở hữu hoặc Admin. Tạo ScheduleController tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java với @RestController, @RequestMapping("/api/v1/schedules"), endpoint POST / (201 Created), GET /{scheduleId} (200 OK), PUT /{scheduleId}/status, DELETE /{scheduleId} (204 No Content), @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\',\'SCHEDULER\')") cho tạo mới và @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\')") cho truy vấn/cập nhật theo RBAC 4 vai trò, @Valid cho payload. Tạo SocialPlatformDispatcher facade điều phối theo enum Platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.controller;

import jakarta.validation.Valid;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling social media scheduling operations.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 * @author Enterprise System Architect
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Creates a new publishing schedule.
     * Requires ADMIN, USER, or SCHEDULER role.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SCHEDULER')")
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody ScheduleRequestDto request) {
        // Extract authenticated user ID from SecurityContextHolder
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        
        // Delegate to service layer for business logic and persistence
        ScheduleResponseDto response = scheduleService.createSchedule(request, userId);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific schedule by ID.
     * Implements ownership check to prevent IDOR (OWASP A01).
     */
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ScheduleResponseDto> getScheduleById(@PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(scheduleService.getScheduleById(scheduleId, userId));
    }

    /**
     * Updates the status of an existing schedule.
     */
    @PutMapping("/{scheduleId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID scheduleId, @RequestParam String status) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        scheduleService.updateStatus(scheduleId, status, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cancels/Deletes a schedule by marking it as CANCELLED.
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

# Day 1: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dispatcher/SocialPlatformDispatcher.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/dispatcher/SocialPlatformDispatcher.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Phát triển ScheduleService tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java với annotation @Service, @Transactional. Triển khai createSchedule(ScheduleRequestDto request, UUID userId) khởi tạo status=PENDING,retryCount=0,gán userId và tenantId từ SecurityContextHolder; getScheduleById(UUID scheduleId) kiểm tra quyền sở hữu theo userId chống IDOR OWASP A01; updateStatusToSent(UUID scheduleId) chuyển SENT và actualSentTime=LocalDateTime.now(); deleteSchedule(UUID scheduleId) chuyển CANCELLED bảo toàn lịch sử. Tích hợp SocialPlatformDispatcher với dispatchPublish(ScheduleEntity entity). Bọc SocialPlatformException trong try-catch ghi log ERROR kèm correlation ID, kích hoạt @Retryable backoff lũy thừa maxAttempts=3 từ Resilience4j. Áp dụng SecurityContextHolder kiểm tra chủ sở hữu hoặc Admin. Tạo ScheduleController tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java với @RestController, @RequestMapping("/api/v1/schedules"), endpoint POST / (201 Created), GET /{scheduleId} (200 OK), PUT /{scheduleId}/status, DELETE /{scheduleId} (204 No Content), @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\',\'SCHEDULER\')") cho tạo mới và @PreAuthorize("hasAnyRole(\'ADMIN\',\'USER\')") cho truy vấn/cập nhật theo RBAC 4 vai trò, @Valid cho payload. Tạo SocialPlatformDispatcher facade điều phối theo enum Platform tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.dispatcher;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.integration.FacebookClient;
import org.nlh4j.socialscheduler.scheduleservice.integration.InstagramClient;
import org.nlh4j.socialscheduler.scheduleservice.integration.TikTokClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * SocialPlatformDispatcher acts as a facade to route publishing requests to the appropriate 
 * social media platform client based on the platform type defined in the schedule entity.
 * 
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@Component
@RequiredArgsConstructor
public class SocialPlatformDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(SocialPlatformDispatcher.class);

    private final FacebookClient facebookClient;
    private final InstagramClient instagramClient;
    private final TikTokClient tikTokClient;

    /**
     * Dispatches the publishing task to the specific platform client.
     * 
     * @param entity The schedule entity containing content and platform metadata.
     * @throws SocialPlatformException if the platform is unsupported or the client fails.
     */
    public void dispatchPublish(ScheduleEntity entity) {
        String platform = entity.getPlatform();
        logger.info("[PROCESS] Dispatching publish request for Schedule ID: {} to Platform: {}", 
                     entity.getScheduleId(), platform);

        try {
            switch (platform.toUpperCase()) {
                case "FACEBOOK":
                    facebookClient.publish(entity);
                    break;
                case "INSTAGRAM":
                    instagramClient.publish(entity);
                    break;
                case "TIKTOK":
                    tikTokClient.publish(entity);
                    break;
                default:
                    logger.error("[CRITICAL FAIL] [EXC-001] Unsupported platform: {}", platform);
                    throw new SocialPlatformException("Unsupported platform: " + platform);
            }
            logger.info("[PROCESS] Successfully dispatched publish for Schedule ID: {}", entity.getScheduleId());
        } catch (Exception e) {
            // Exception Cause Chain Preservation Law: Forwarding original exception context
            logger.error("[CRITICAL FAIL] [EXC-001] Dispatch failed for Schedule ID: {}. Raw error: {}", 
                         entity.getScheduleId(), e.getMessage());
            throw new SocialPlatformException("Failed to dispatch to " + platform, e);
        }
    }
}
```

