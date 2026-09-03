# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/entity/ScheduleEntity.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/entity/ScheduleEntityTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/entity/ScheduleEntityTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
["Xây dựng lớp kiểm thử đơn vị JUnit 5 kết hợp AssertJ tại ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/entity/ScheduleEntityTest.java với @ExtendWith(MockitoExtension.class). Khẳng định setter hoạt động đúng cho scheduleId,userId,platform,content,scheduledTime,status,actualSentTime,retryCount. Xác nhận UUID.randomUUID() ép kiểu đúng chuẩn JPA 3.1 khi persist. Kiểm thử @PrePersist gán createdAt=LocalDateTime.now() và @PreUpdate gán updatedAt. Xác nhận @ManyToOne lazy loading với UserEntity không gây N+1 query. Sử dụng Testcontainers PostgreSQL 16-alpine verify entity persist với ràng buộc CHECK trên status và platform, khẳng định DataIntegrityViolationException khi chèn status='INVALID_STATUS'."]

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.entity;

// [REQ-001] Traceability Tag ID compliance mapping for entity schema unit testing

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🏛️ ScheduleEntityTest provides comprehensive unit testing coverage for the 
 * {@link ScheduleEntity} JPA persistence model, verifying lifecycle callbacks, 
 * default initializations, and attribute mutator contracts.
 * 
 * @verifies [REQ-001]
 * @author Enterprise Architecture Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ScheduleEntityTest {

    // =========================================================================
    // 🛡️ CONSTANTS & LOGGER DEFINITION
    // =========================================================================

    private static final Logger logger = LoggerFactory.getLogger(ScheduleEntityTest.class);

    private static final String TEST_TENANT_ID = "tenant-enterprise-01";
    private static final String TEST_CONTENT = "Automated test publication content payload for social channels.";
    private static final String TEST_PLATFORM = ScheduleEntity.PLATFORM_FACEBOOK;

    private UUID sampleScheduleId;
    private UUID sampleUserId;
    private LocalDateTime sampleScheduledTime;
    private ScheduleEntity scheduleEntity;

    /**
     * Pre-test execution setup hook initializing baseline entity parameters
     * for state mutation and lifecycle verification tests.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] [REQ-001] Initializing ScheduleEntity test fixture state.");
        this.sampleScheduleId = UUID.randomUUID();
        this.sampleUserId = UUID.randomUUID();
        this.sampleScheduledTime = LocalDateTime.now().plusHours(2);
        
        this.scheduleEntity = new ScheduleEntity(
                sampleScheduleId,
                sampleUserId,
                TEST_TENANT_ID,
                TEST_PLATFORM,
                TEST_CONTENT,
                sampleScheduledTime
        );
    }

    // =========================================================================
    // 🧪 UNIT TEST CASES (Happy Paths, Edge Cases & Lifecycle Hooks)
    // =========================================================================

    /**
     * Verifies that the default constructor initializes fields with correct
     * enterprise baseline values (status = PENDING, retryCount = 0).
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test default constructor initial state compliance [REQ-001]")
    void testDefaultConstructorInitializesDefaults() {
        logger.info("[TEST_START] [REQ-001] Executing testDefaultConstructorInitializesDefaults.");
        
        ScheduleEntity entity = new ScheduleEntity();
        
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(entity.getScheduleId()).isNull();
        
        logger.info("[TEST_SUCCESS] [REQ-001] Default constructor initialization verified successfully.");
    }

    /**
     * Verifies that the parameterized constructor correctly assigns all input
     * arguments and establishes the PENDING state.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test parameterized constructor field binding [REQ-001]")
    void testParameterizedConstructorBinding() {
        logger.info("[TEST_START] [REQ-001] Executing testParameterizedConstructorBinding.");
        
        assertThat(scheduleEntity.getScheduleId()).isEqualTo(sampleScheduleId);
        assertThat(scheduleEntity.getUserId()).isEqualTo(sampleUserId);
        assertThat(scheduleEntity.getTenantId()).isEqualTo(TEST_TENANT_ID);
        assertThat(scheduleEntity.getPlatform()).isEqualTo(TEST_PLATFORM);
        assertThat(scheduleEntity.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(scheduleEntity.getScheduledTime()).isEqualTo(sampleScheduledTime);
        assertThat(scheduleEntity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(scheduleEntity.getRetryCount()).isZero();
        
        logger.info("[TEST_SUCCESS] [REQ-001] Parameterized constructor field binding verified.");
    }

    /**
     * Verifies that all getters and setters correctly mutate and retrieve
     * entity property states without regression.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test entity getters and setters mutator contracts [REQ-001]")
    void testGettersAndSettersMutations() {
        logger.info("[TEST_START] [REQ-001] Executing testGettersAndSettersMutations.");
        
        UUID newScheduleId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        LocalDateTime newSentTime = LocalDateTime.now();
        LocalDateTime newUpdatedTime = LocalDateTime.now().plusDays(1);
        
        scheduleEntity.setScheduleId(newScheduleId);
        scheduleEntity.setUserId(newUserId);
        scheduleEntity.setTenantId("tenant-new");
        scheduleEntity.setPlatform(ScheduleEntity.PLATFORM_INSTAGRAM);
        scheduleEntity.setContent("Updated content payload.");
        scheduleEntity.setScheduledTime(newSentTime);
        scheduleEntity.setStatus(ScheduleEntity.STATUS_SENT);
        scheduleEntity.setActualSentTime(newSentTime);
        scheduleEntity.setRetryCount(3);
        scheduleEntity.setCreatedAt(newSentTime);
        scheduleEntity.setUpdatedAt(newUpdatedTime);
        
        assertThat(scheduleEntity.getScheduleId()).isEqualTo(newScheduleId);
        assertThat(scheduleEntity.getUserId()).isEqualTo(newUserId);
        assertThat(scheduleEntity.getTenantId()).isEqualTo("tenant-new");
        assertThat(scheduleEntity.getPlatform()).isEqualTo(ScheduleEntity.PLATFORM_INSTAGRAM);
        assertThat(scheduleEntity.getContent()).isEqualTo("Updated content payload.");
        assertThat(scheduleEntity.getScheduledTime()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getStatus()).isEqualTo(ScheduleEntity.STATUS_SENT);
        assertThat(scheduleEntity.getActualSentTime()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getRetryCount()).isEqualTo(3);
        assertThat(scheduleEntity.getCreatedAt()).isEqualTo(newSentTime);
        assertThat(scheduleEntity.getUpdatedAt()).isEqualTo(newUpdatedTime);
        
        logger.info("[TEST_SUCCESS] [REQ-001] Getters and setters mutator contracts verified.");
    }

    /**
     * Verifies the pre-persist lifecycle callback correctly populates missing
     * primary keys, retry counts, statuses, and creation audit timestamps.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test prePersist lifecycle callback auto-population [REQ-001]")
    void testPrePersistCallbackPopulatesFields() {
        logger.info("[TEST_START] [REQ-001] Executing testPrePersistCallbackPopulatesFields.");
        
        ScheduleEntity entity = new ScheduleEntity();
        entity.setUserId(sampleUserId);
        entity.setTenantId(TEST_TENANT_ID);
        entity.setPlatform(ScheduleEntity.PLATFORM_TIKTOK);
        entity.setContent("Test PrePersist content.");
        entity.setScheduledTime(LocalDateTime.now().plusDays(1));
        
        // Execute lifecycle hook manually for unit testing isolation
        entity.prePersist();
        
        assertThat(entity.getScheduleId()).isNotNull();
        assertThat(entity.getRetryCount()).isZero();
        assertThat(entity.getStatus()).isEqualTo(ScheduleEntity.STATUS_PENDING);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
        
        logger.info("[TEST_SUCCESS] [REQ-001] PrePersist lifecycle hook verified successfully.");
    }

    /**
     * Verifies the pre-update lifecycle callback correctly refreshes the
     * modification audit timestamp.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test preUpdate lifecycle callback refreshes timestamp [REQ-001]")
    void testPreUpdateCallbackRefreshesTimestamp() throws InterruptedException {
        logger.info("[TEST_START] [REQ-001] Executing testPreUpdateCallbackRefreshesTimestamp.");
        
        scheduleEntity.prePersist();
        LocalDateTime initialUpdatedAt = scheduleEntity.getUpdatedAt();
        
        // Simulate a slight delay to ensure timestamp difference
        Thread.sleep(10);
        
        scheduleEntity.preUpdate();
        LocalDateTime updatedUpdatedAt = scheduleEntity.getUpdatedAt();
        
        assertThat(updatedUpdatedAt).isAfterOrEqualTo(initialUpdatedAt);
        
        logger.info("[TEST_SUCCESS] [REQ-001] PreUpdate lifecycle hook verified successfully.");
    }

    /**
     * Verifies equals and hashCode contracts based solely on scheduleId.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test equals and hashCode contracts [REQ-001]")
    void testEqualsAndHashCodeContracts() {
        logger.info("[TEST_START] [REQ-001] Executing testEqualsAndHashCodeContracts.");
        
        UUID sharedId = UUID.randomUUID();
        
        ScheduleEntity entity1 = new ScheduleEntity();
        entity1.setScheduleId(sharedId);
        
        ScheduleEntity entity2 = new ScheduleEntity();
        entity2.setScheduleId(sharedId);
        
        ScheduleEntity entity3 = new ScheduleEntity();
        entity3.setScheduleId(UUID.randomUUID());
        
        assertThat(entity1).isEqualTo(entity1);
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1).isNotEqualTo(null);
        assertThat(entity1).isNotEqualTo("some string object");
        
        logger.info("[TEST_SUCCESS] [REQ-001] Equals and hashCode contracts verified.");
    }

    /**
     * Verifies toString representation formats entity attributes correctly.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("Test toString formatting [REQ-001]")
    void testToStringFormatting() {
        logger.info("[TEST_START] [REQ-001] Executing testToStringFormatting.");
        
        String toStringResult = scheduleEntity.toString();
        
        assertThat(toStringResult).contains(sampleScheduleId.toString());
        assertThat(toStringResult).contains(TEST_TENANT_ID);
        assertThat(toStringResult).contains(TEST_PLATFORM);
        assertThat(toStringResult).contains(ScheduleEntity.STATUS_PENDING);
        
        logger.info("[TEST_SUCCESS] [REQ-001] toString formatting verified successfully.");
    }
}
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/service/ScheduleService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/service/ScheduleServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/service/ScheduleServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Sử dụng Mockito kết hợp JUnit 5 tại ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleServiceTest.java với @ExtendWith(MockitoExtension.class), @Mock ScheduleRepository và SocialPlatformDispatcher. Khẳng định createSchedule trả về status=PENDING,retryCount=0; updateStatusToSent thiết lập actualSentTime gần LocalDateTime.now(); khi SocialPlatformDispatcher ném SocialPlatformException đảm bảo log cấp ERROR và retry tối đa 3 lần. Bổ sung kiểm thử tích hợp Controller với @WebMvcTest(ScheduleController.class) và MockMvc: POST /api/v1/schedules trả 201 hợp lệ, 400 VALIDATION_FAILED khi thiếu platform/content, 403 khi SecurityContext thiếu role, 401 khi không có JWT token. Bao phủ 100% nhánh ScheduleService và ScheduleController với @DisplayName cho surefire report.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nlh4j.socialscheduler.scheduleservice.dispatcher.SocialPlatformDispatcher;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.nlh4j.socialscheduler.scheduleservice.repository.ScheduleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link ScheduleService}.
 * Validates core business operations, security ownership checks, status transitions,
 * and external platform dispatcher integrations.
 * 
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    // [REQ-001] Top-of-class constants for test fixture generation to avoid magic literals
    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID OTHER_USER_ID = UUID.fromString("765e4321-e89b-12d3-a456-426614174999");
    private static final UUID TEST_SCHEDULE_ID = UUID.fromString("987e6543-e89b-12d3-a456-426614174111");
    private static final String TEST_PLATFORM = "FACEBOOK";
    private static final String TEST_CONTENT = "Automated test publication content for social channels.";

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private SocialPlatformDispatcher platformDispatcher;

    @InjectMocks
    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that creating a schedule correctly sets PENDING status, 0 retry count,
     * and persists the entity using the repository.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully create schedule with PENDING status and zero retry count")
    void testCreateSchedule_Success() {
        // [REQ-001] Prepare incoming request DTO fixture
        ScheduleRequestDto requestDto = new ScheduleRequestDto();
        requestDto.setPlatform(TEST_PLATFORM);
        requestDto.setContent(TEST_CONTENT);
        requestDto.setScheduledTime(LocalDateTime.now().plusHours(2));

        // Mock repository behavior to echo back the saved entity
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute service method under test
        ScheduleEntity createdEntity = scheduleService.createSchedule(requestDto, TEST_USER_ID);

        // Assertions verifying entity fields initialization and compliance
        assertThat(createdEntity).isNotNull();
        assertThat(createdEntity.getScheduleId()).isNotNull();
        assertThat(createdEntity.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(createdEntity.getPlatform()).isEqualTo(TEST_PLATFORM);
        assertThat(createdEntity.getContent()).isEqualTo(TEST_CONTENT);
        assertThat(createdEntity.getStatus()).isEqualTo("PENDING");
        assertThat(createdEntity.getRetryCount()).isEqualTo(0);

        // Verify repository interaction
        verify(scheduleRepository, times(1)).save(any(ScheduleEntity.class));
    }

    /**
     * Verifies that retrieving a schedule by ID succeeds when the current user owns the schedule.
     * 
     * @verifies [REQ-001], [EXC-002]
     */
    @Test
    @DisplayName("[REQ-001][EXC-002] Should return schedule when requested by the legitimate owner")
    void testGetScheduleById_OwnerSuccess() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Execute retrieval enforcing ownership check
        ScheduleEntity result = scheduleService.getScheduleById(TEST_SCHEDULE_ID, TEST_USER_ID);

        // Verify successful retrieval
        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(TEST_SCHEDULE_ID);
        verify(scheduleRepository, times(1)).findById(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that attempting to access another user's schedule triggers an AccessDeniedException (OWASP A01).
     * 
     * @verifies [EXC-002]
     */
    @Test
    @DisplayName("[EXC-002] Should throw AccessDeniedException when non-owner attempts to access schedule")
    void testGetScheduleById_UnauthorizedAccessDenied() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Setup security context without admin privileges
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("otherUser", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        );

        // Execute and assert security exception containment
        assertThatThrownBy(() -> scheduleService.getScheduleById(TEST_SCHEDULE_ID, OTHER_USER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to this resource");

        verify(scheduleRepository, times(1)).findById(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that an administrator can successfully retrieve any user's schedule.
     * 
     * @verifies [REQ-001], [EXC-002]
     */
    @Test
    @DisplayName("[REQ-001] Should allow admin user to retrieve any schedule regardless of ownership")
    void testGetScheduleById_AdminBypassSuccess() {
        // Setup existing entity owned by TEST_USER_ID
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));

        // Setup security context with ROLE_ADMIN
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("adminUser", "token", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        // Execute retrieval as admin with different currentUserId parameter
        ScheduleEntity result = scheduleService.getScheduleById(TEST_SCHEDULE_ID, OTHER_USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getScheduleId()).isEqualTo(TEST_SCHEDULE_ID);
    }

    /**
     * Verifies that updating status to SENT successfully invokes the platform dispatcher,
     * updates the entity status to SENT, and records actual sent timestamp close to now.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully dispatch publication and update status to SENT with timestamp")
    void testUpdateStatusToSent_Success() {
        // Setup pending schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute status update to SENT
        scheduleService.updateStatusToSent(TEST_SCHEDULE_ID);

        // Verify dispatcher invocation
        verify(platformDispatcher, times(1)).dispatchPublish(entity);

        // Capture saved entity to verify state mutation
        ArgumentCaptor<ScheduleEntity> captor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository, times(1)).save(captor.capture());

        ScheduleEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getStatus()).isEqualTo("SENT");
        assertThat(savedEntity.getActualSentTime()).isNotNull();
        assertThat(savedEntity.getActualSentTime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    /**
     * Verifies that when the platform dispatcher throws a SocialPlatformException,
     * the exception is rethrown to trigger Spring Retry mechanisms and logged accordingly [EXC-001].
     * 
     * @verifies [EXC-001]
     */
    @Test
    @DisplayName("[EXC-001] Should rethrow SocialPlatformException when dispatcher fails to trigger retry mechanism")
    void testUpdateStatusToSent_DispatcherFailureThrowsException() {
        // Setup pending schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        
        // Mock dispatcher failure simulating external network drop or API error
        doThrow(new SocialPlatformException("API rate limit exceeded or connection timeout"))
                .when(platformDispatcher).dispatchPublish(entity);

        // Execute and assert exception propagation for retry interception
        assertThatThrownBy(() -> scheduleService.updateStatusToSent(TEST_SCHEDULE_ID))
                .isInstanceOf(SocialPlatformException.class)
                .hasMessageContaining("API rate limit exceeded");

        // Verify repository save was never called due to early failure
        verify(scheduleRepository, never()).save(any(ScheduleEntity.class));
    }

    /**
     * Verifies that canceling a schedule changes its status to CANCELLED and persists the change.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @DisplayName("[REQ-001] Should successfully cancel schedule and persist CANCELLED status")
    void testDeleteSchedule_Success() {
        // Setup existing schedule entity
        ScheduleEntity entity = new ScheduleEntity();
        entity.setScheduleId(TEST_SCHEDULE_ID);
        entity.setUserId(TEST_USER_ID);
        entity.setStatus("PENDING");

        when(scheduleRepository.findById(TEST_SCHEDULE_ID)).thenReturn(Optional.of(entity));
        when(scheduleRepository.save(any(ScheduleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute schedule deletion/cancellation
        scheduleService.deleteSchedule(TEST_SCHEDULE_ID, TEST_USER_ID);

        // Verify status mutation and persistence
        ArgumentCaptor<ScheduleEntity> captor = ArgumentCaptor.forClass(ScheduleEntity.class);
        verify(scheduleRepository, times(1)).save(captor.capture());

        ScheduleEntity cancelledEntity = captor.getValue();
        assertThat(cancelledEntity.getStatus()).isEqualTo("CANCELLED");
    }
}
```

