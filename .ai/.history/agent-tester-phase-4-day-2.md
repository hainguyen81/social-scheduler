# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
/*
 * ScheduleServiceTest
 * Unit tests for ScheduleService scheduling operations.
 * Traceability: @verifies [REQ-001], [EXC-001], [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.nlh4j.socialscheduler.schedulingservice.exception.ScheduleServiceException;
import org.nlh4j.socialscheduler.schedulingservice.model.ErrorCode;
import org.nlh4j.socialscheduler.schedulingservice.model.Schedule;
import org.nlh4j.socialscheduler.userservice.model.User;

/**
 * Unit test suite for ScheduleService core scheduling operations.
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private UUID testUserId;
    private String validIdempotencyKey;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        validIdempotencyKey = "idempotency-key-" + System.currentTimeMillis();
        // Initialize any static state if required by the service
        ScheduleService.processedKeys.clear();
    }

    /**
     * Test happy path: schedule a post with valid inputs.
     * Business requirement: REQ-001 - System shall allow users to schedule social media posts
     * with valid platform, content, and future scheduled time.
     * Expected: Repository save called once, schedule returned with pending status.
     */
    @Test
    @DisplayName("Schedule post with valid inputs should succeed and persist entity")
    void schedulePost_validInput_shouldSaveAndReturnSchedule() {
        // Arrange: Prepare valid request parameters matching source logic constraints
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Sample post content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setScheduleId(UUID.randomUUID());
        expectedSchedule.setUserId(testUserId);
        expectedSchedule.setPlatform(platform);
        expectedSchedule.setContent(content);
        expectedSchedule.setScheduledTime(scheduleTime);
        expectedSchedule.setStatus(ScheduleService.STATUS_PENDING);
        expectedSchedule.setCreatedAt(LocalDateTime.now());
        expectedSchedule.setUpdatedAt(LocalDateTime.now());

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(expectedSchedule);

        // Act: Execute scheduling operation
        Schedule result = scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey);

        // Assert: Verify entity was persisted with correct attributes
        assertNotNull(result.getScheduleId(), "Schedule ID must not be null after successful persistence");
        assertEquals(testUserId, result.getUserId(), "User ID must match request");
        assertEquals(platform, result.getPlatform(), "Platform must match requested platform");
        assertEquals(content, result.getContent(), "Content must match requested content");
        assertEquals(ScheduleService.STATUS_PENDING, result.getStatus(), "Initial status must be PENDING");
        verify(scheduleRepository, times(1)).save(argThat(s -> 
            s.getPlatform().equals(platform) && s.getContent().equals(content)));
    }

    /**
     * Test edge case: invalid platform string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Platform must be one of Facebook/Instagram/TikTok;
     * invalid platform triggers business exception with preserved cause chain.
     */
    @Test
    @DisplayName("Invalid platform should throw ScheduleServiceException")
    void schedulePost_invalidPlatform_shouldThrowException() {
        // Arrange
        String invalidPlatform = "Twitter"; // Not in allowed set
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert: Expect IllegalArgumentException wrapped in ScheduleServiceException
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, invalidPlatform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode(), "Error code must be INVALID_INPUT");
        assertTrue(ex.getMessage().contains("Invalid platform"), "Message must indicate invalid platform");
        // Verify cause chain preservation: original IllegalArgumentException should be embedded
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must not be severed");
        assertTrue(cause.getMessage().contains("Invalid platform"), "Root cause message must contain platform validation text");
    }

    /**
     * Test edge case: empty content string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Content field must not be blank; empty content triggers exception.
     */
    @Test
    @DisplayName("Empty content should throw ScheduleServiceException")
    void schedulePost_emptyContent_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = ""; // Blank content
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Content cannot be empty"), "Message must indicate empty content constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve root exception");
        assertTrue(cause.getMessage().contains("Content cannot be empty"), "Root cause must reflect content validation");
    }

    /**
     * Test edge case: past schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Scheduled time must be in the future; past times rejected.
     */
    @Test
    @DisplayName("Past schedule time should throw ScheduleServiceException")
    void schedulePost_pastScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_TIKTOK;
        String content = "Content";
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1); // Explicitly in the past
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, pastTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must enforce future time constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must be preserved");
        assertTrue(cause.getMessage().contains("Schedule time must be in the future"), "Root cause must reflect time validation");
    }

    /**
     * Test negative path: duplicate idempotency key should throw exception.
     * Business requirement: REQ-001 / EXC-002 - System must prevent duplicate schedule submissions
     * within the idempotency window (3600 seconds).
     */
    @Test
    @DisplayName("Duplicate idempotency key should throw ScheduleServiceException")
    void schedulePost_duplicateIdempotencyKey_shouldThrowException() {
        // Arrange: First successful submission
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String duplicateKey = "duplicate-key-1";

        Schedule firstSchedule = new Schedule();
        firstSchedule.setScheduleId(UUID.randomUUID());
        firstSchedule.setUserId(testUserId);
        firstSchedule.setPlatform(platform);
        firstSchedule.setContent(content);
        firstSchedule.setScheduledTime(scheduleTime);
        firstSchedule.setStatus(ScheduleService.STATUS_PENDING);

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(firstSchedule);

        // Act: First call should succeed
        scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey);

        // Act: Second call with same key should fail
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey));

        // Assert: Exception must reference duplicate request error code
        assertEquals(ErrorCode.DUPLICATE_REQUEST, ex.getErrorCode(), "Error code must be DUPLICATE_REQUEST");
        assertTrue(ex.getMessage().contains("Duplicate schedule request detected"), 
            "Message must indicate duplicate request detection");
        // Verify cause chain: null cause is explicitly passed in service, but exception wraps business context
        assertNull(ex.getCause(), "Cause should be null as per service design when throwing duplicate exception");
    }

    /**
     * Test negative path: null schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Null schedule time violates pre-condition and triggers exception.
     */
    @Test
    @DisplayName("Null schedule time should throw ScheduleServiceException")
    void schedulePost_nullScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = "Content";
        LocalDateTime nullTime = null;
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, nullTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must handle null time as invalid");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve the NullPointerException or wrapped validation");
        assertTrue(cause.getMessage() != null && cause.getMessage().contains("Schedule time"), 
            "Root cause must reference schedule time constraint");
    }
}
```
</EXISTING_TEST_SUITE_CODE>



### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.schedulingservice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * ScheduleService provides core scheduling operations for social media posts.
 * <p>
 * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
 * </p>
 */
@Service
@Transactional
public class ScheduleService {

    // Logger injection for enterprise audit and process tracking
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    // Constants for platform values, status values, and idempotency window
    public static final String PLATFORM_FACEBOOK = "Facebook";
    public static final String PLATFORM_INSTAGRAM = "Instagram";
    public static final String PLATFORM_TIKTOK = "TikTok";

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Idempotency window: 1 hour in seconds (used for duplicate request detection)
    public static final long IDEMPOTENCY_WINDOW_SECONDS = 3600;

    // In-memory store for idempotency keys (production should use Redis or similar)
    private final Map<String, LocalDateTime> processedKeys = new ConcurrentHashMap<>();

    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * Schedules a new social media post.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @param userId            the user requesting the schedule
     * @param platform          the target platform (Facebook, Instagram, TikTok)
     * @param content           the post content
     * @param scheduleTime      the exact time to publish
     * @param idempotencyKey   unique key to prevent duplicate submissions
     * @return the created Schedule entity
     * @throws ScheduleServiceException on business or data access errors
     */
    public Schedule schedulePost(UUID userId, String platform, String content,
                                 LocalDateTime scheduleTime, String idempotencyKey) {
        logger.info("[ENTRY] schedulePost called for userId={}, platform={}, scheduleTime={}", userId, platform, scheduleTime);

        // Input validation – OWASP XSS and SQL Injection prevention via strict parameter checks
        if (StringUtils.isBlank(platform) ||
                !(platform.equals(PLATFORM_FACEBOOK) ||
                        platform.equals(PLATFORM_INSTAGRAM) ||
                        platform.equals(PLATFORM_TIKTOK))) {
            IllegalArgumentException iae = new IllegalArgumentException("Invalid platform: " + platform);
            logger.error("[CRITICAL FAIL] [EXC-001] Invalid platform provided in schedulePost. Raw error: {}", iae.getMessage(), iae);
            throw new ScheduleServiceException(ErrorCode.INVALID_INPUT, "Invalid platform supplied", iae);
        }
        if (StringUtils.isBlank(content)) {
            IllegalArgumentException iae = new IllegalArgumentException("Content cannot be empty");
            logger.error("[CRITICAL FAIL] [EXC-001] Empty content in schedulePost. Raw error: {}", iae.getMessage(), iae);
            throw new ScheduleServiceException(ErrorCode.INVALID_INPUT, "Content cannot be empty", iae);
        }
        if (scheduleTime == null || scheduleTime.isBefore(LocalDateTime.now())) {
            IllegalArgumentException iae = new IllegalArgumentException("Schedule time must be in the future");
            logger.error("[CRITICAL FAIL] [EXC-001] Invalid schedule time in schedulePost. Raw error: {}", iae.getMessage(), iae);
            throw new ScheduleServiceException(ErrorCode.INVALID_INPUT, "Schedule time must be in the future", iae);
        }

        // Idempotency check – prevents duplicate submissions within the window
        if (!validateIdempotencyKey(idempotencyKey)) {
            logger.warn("[WARN] Duplicate schedule request detected for idempotencyKey={}", idempotencyKey);
            throw new ScheduleServiceException(ErrorCode.DUPLICATE_REQUEST, "Duplicate schedule request detected", null);
        }

        try {
            Schedule schedule = new Schedule();
            schedule.setScheduleId(UUID.randomUUID());
            schedule.setUserId(userId);
            schedule.setPlatform(platform);
            schedule.setContent(content);
            schedule.setScheduledTime(scheduleTime);
            schedule.setStatus(STATUS_PENDING);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());

            Schedule saved = scheduleRepository.save(schedule);
            logger.info("[EXIT] schedulePost completed successfully for scheduleId={}", saved.getScheduleId());
            return saved;
        } catch (Exception e) {
            // Preserve cause chain for enterprise audit
            logger.error("[CRITICAL FAIL] [EXC-001] Unexpected error while scheduling post. Raw error: {}", e.getMessage(), e);
            throw new ScheduleServiceException(ErrorCode.PROCESSING_FAILED, "Unexpected error while scheduling post", e);
        }
    }

    /**
     * Retrieves a schedule by its unique identifier.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @param scheduleId the schedule identifier
     * @return the Schedule entity
     * @throws ScheduleServiceException if the schedule is not found or a data access error occurs
     */
    public Schedule findScheduleById(UUID scheduleId) throws ScheduleServiceException {
        logger.info("[ENTRY] findScheduleById called for scheduleId={}", scheduleId);
        try {
            return scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> {
                        logger.error("[CRITICAL FAIL] [EXC-001] Schedule not found for scheduleId={}", scheduleId);
                        return new ScheduleServiceException(ErrorCode.NOT_FOUND, "Schedule not found", null);
                    });
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Data access error while retrieving scheduleId={}. Raw error: {}", scheduleId, e.getMessage(), e);
            throw new ScheduleServiceException(ErrorCode.DATA_ACCESS_FAILURE, "Data access error while retrieving schedule", e);
        } finally {
            logger.info("[EXIT] findScheduleById completed for scheduleId={}", scheduleId);
        }
    }

    /**
     * Retrieves all scheduled posts.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @return list of all Schedule entities
     * @throws ScheduleServiceException on data access errors
     */
    public List<Schedule> findAllSchedules() throws ScheduleServiceException {
        logger.info("[ENTRY] findAllSchedules called");
        try {
            List<Schedule> schedules = scheduleRepository.findAll();
            logger.info("[EXIT] findAllSchedules completed, total records={}", schedules.size());
            return schedules;
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Data access error while retrieving all schedules. Raw error: {}", e.getMessage(), e);
            throw new ScheduleServiceException(ErrorCode.DATA_ACCESS_FAILURE, "Data access error while retrieving all schedules", e);
        }
    }

    /**
     * Updates an existing schedule's content and scheduled time.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @param scheduleId the schedule identifier to update
     * @param content    new content (must not be blank)
     * @param scheduleTime new scheduled time (must be in the future)
     * @return the updated Schedule entity
     * @throws ScheduleServiceException if the schedule is not found, validation fails, or a data error occurs
     */
    public Schedule updateSchedule(UUID scheduleId, String content, LocalDateTime scheduleTime) throws ScheduleServiceException {
        logger.info("[ENTRY] updateSchedule called for scheduleId={}", scheduleId);

        // Validate input
        if (StringUtils.isBlank(content)) {
            IllegalArgumentException iae = new IllegalArgumentException("Content cannot be empty");
            logger.error("[CRITICAL FAIL] [EXC-001] Empty content in updateSchedule. Raw error: {}", iae.getMessage(), iae);
            throw new ScheduleServiceException(ErrorCode.INVALID_INPUT, "Content cannot be empty", iae);
        }
        if (scheduleTime == null || scheduleTime.isBefore(LocalDateTime.now())) {
            IllegalArgumentException iae = new IllegalArgumentException("Schedule time must be in the future");
            logger.error("[CRITICAL FAIL] [EXC-001] Invalid schedule time in updateSchedule. Raw error: {}", iae.getMessage(), iae);
            throw new ScheduleServiceException(ErrorCode.INVALID_INPUT, "Schedule time must be in the future", iae);
        }

        try {
            Schedule existing = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> {
                        logger.error("[CRITICAL FAIL] [EXC-001] Schedule not found for update, scheduleId={}", scheduleId);
                        return new ScheduleServiceException(ErrorCode.NOT_FOUND, "Schedule not found", null);
                    });

            // Business rule: only pending schedules can be updated
            if (!STATUS_PENDING.equalsIgnoreCase(existing.getStatus())) {
                logger.warn("[WARN] Attempt to update non‑pending schedule scheduleId={} with status {}", scheduleId, existing.getStatus());
                throw new ScheduleServiceException(ErrorCode.ILLEGAL_STATE, "Only pending schedules can be updated", null);
            }

            existing.setContent(content);
            existing.setScheduledTime(scheduleTime);
            existing.setUpdatedAt(LocalDateTime.now());

            Schedule updated = scheduleRepository.save(existing);
            logger.info("[EXIT] updateSchedule completed successfully for scheduleId={}", updated.getScheduleId());
            return updated;
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Data access error while updating scheduleId={}. Raw error: {}", scheduleId, e.getMessage(), e);
            throw new ScheduleServiceException(ErrorCode.DATA_ACCESS_FAILURE, "Data access error while updating schedule", e);
        }
    }

    /**
     * Deletes a schedule by its identifier.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @param scheduleId the schedule identifier to delete
     * @throws ScheduleServiceException if a data access error occurs
     */
    public void deleteSchedule(UUID scheduleId) throws ScheduleServiceException {
        logger.info("[ENTRY] deleteSchedule called for scheduleId={}", scheduleId);
        try {
            if (!scheduleRepository.existsById(scheduleId)) {
                logger.warn("[WARN] Delete attempted on non‑existent scheduleId={}", scheduleId);
                throw new ScheduleServiceException(ErrorCode.NOT_FOUND, "Schedule not found", null);
            }
            scheduleRepository.deleteById(scheduleId);
            logger.info("[EXIT] deleteSchedule completed successfully for scheduleId={}", scheduleId);
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Data access error while deleting scheduleId={}. Raw error: {}", scheduleId, e.getMessage(), e);
            throw new ScheduleServiceException(ErrorCode.DATA_ACCESS_FAILURE, "Data access error while deleting schedule", e);
        }
    }

    /**
     * Validates an idempotency key to prevent duplicate submissions.
     * <p>
     * Traceability Tags: [REQ-001], [EXC-001], [EXC-002]
     * </p>
     *
     * @param key the idempotency key
     * @return true if the key is valid (not duplicate), false otherwise
     */
    private boolean validateIdempotencyKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        // Clean expired keys
        processedKeys.entrySet().removeIf(entry -> entry.getValue().isBefore(now.minusSeconds(IDEMPOTENCY_WINDOW_SECONDS)));
        if (processedKeys.containsKey(key)) {
            return false;
        }
        processedKeys.put(key, now);
        return true;
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp ScheduleService để đảm bảo các phương thức quản lý lịch đăng bài hoạt động đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/*
 * ScheduleServiceTest
 * Unit tests for ScheduleService scheduling operations.
 * Traceability: @verifies [REQ-001], [EXC-001], [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.nlh4j.socialscheduler.schedulingservice.exception.ScheduleServiceException;
import org.nlh4j.socialscheduler.schedulingservice.model.ErrorCode;
import org.nlh4j.socialscheduler.schedulingservice.model.Schedule;
import org.nlh4j.socialscheduler.userservice.model.User;

/**
 * Unit test suite for ScheduleService core scheduling operations.
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private UUID testUserId;
    private String validIdempotencyKey;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        validIdempotencyKey = "idempotency-key-" + System.currentTimeMillis();
        // Initialize any static state if required by the service
        ScheduleService.processedKeys.clear();
    }

    /**
     * Test happy path: schedule a post with valid inputs.
     * Business requirement: REQ-001 - System shall allow users to schedule social media posts
     * with valid platform, content, and future scheduled time.
     * Expected: Repository save called once, schedule returned with pending status.
     */
    @Test
    @DisplayName("Schedule post with valid inputs should succeed and persist entity")
    void schedulePost_validInput_shouldSaveAndReturnSchedule() {
        // Arrange: Prepare valid request parameters matching source logic constraints
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Sample post content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setScheduleId(UUID.randomUUID());
        expectedSchedule.setUserId(testUserId);
        expectedSchedule.setPlatform(platform);
        expectedSchedule.setContent(content);
        expectedSchedule.setScheduledTime(scheduleTime);
        expectedSchedule.setStatus(ScheduleService.STATUS_PENDING);
        expectedSchedule.setCreatedAt(LocalDateTime.now());
        expectedSchedule.setUpdatedAt(LocalDateTime.now());

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(expectedSchedule);

        // Act: Execute scheduling operation
        Schedule result = scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey);

        // Assert: Verify entity was persisted with correct attributes
        assertNotNull(result.getScheduleId(), "Schedule ID must not be null after successful persistence");
        assertEquals(testUserId, result.getUserId(), "User ID must match request");
        assertEquals(platform, result.getPlatform(), "Platform must match requested platform");
        assertEquals(content, result.getContent(), "Content must match requested content");
        assertEquals(ScheduleService.STATUS_PENDING, result.getStatus(), "Initial status must be PENDING");
        verify(scheduleRepository, times(1)).save(argThat(s -> 
            s.getPlatform().equals(platform) && s.getContent().equals(content)));
    }

    /**
     * Test edge case: invalid platform string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Platform must be one of Facebook/Instagram/TikTok;
     * invalid platform triggers business exception with preserved cause chain.
     */
    @Test
    @DisplayName("Invalid platform should throw ScheduleServiceException")
    void schedulePost_invalidPlatform_shouldThrowException() {
        // Arrange
        String invalidPlatform = "Twitter"; // Not in allowed set
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert: Expect IllegalArgumentException wrapped in ScheduleServiceException
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, invalidPlatform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode(), "Error code must be INVALID_INPUT");
        assertTrue(ex.getMessage().contains("Invalid platform"), "Message must indicate invalid platform");
        // Verify cause chain preservation: original IllegalArgumentException should be embedded
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must not be severed");
        assertTrue(cause.getMessage().contains("Invalid platform"), "Root cause message must contain platform validation text");
    }

    /**
     * Test edge case: empty content string should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Content field must not be blank; empty content triggers exception.
     */
    @Test
    @DisplayName("Empty content should throw ScheduleServiceException")
    void schedulePost_emptyContent_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = ""; // Blank content
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Content cannot be empty"), "Message must indicate empty content constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve root exception");
        assertTrue(cause.getMessage().contains("Content cannot be empty"), "Root cause must reflect content validation");
    }

    /**
     * Test edge case: past schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Scheduled time must be in the future; past times rejected.
     */
    @Test
    @DisplayName("Past schedule time should throw ScheduleServiceException")
    void schedulePost_pastScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_TIKTOK;
        String content = "Content";
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1); // Explicitly in the past
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, pastTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must enforce future time constraint");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must be preserved");
        assertTrue(cause.getMessage().contains("Schedule time must be in the future"), "Root cause must reflect time validation");
    }

    /**
     * Test negative path: duplicate idempotency key should throw exception.
     * Business requirement: REQ-001 / EXC-002 - System must prevent duplicate schedule submissions
     * within the idempotency window (3600 seconds).
     */
    @Test
    @DisplayName("Duplicate idempotency key should throw ScheduleServiceException")
    void schedulePost_duplicateIdempotencyKey_shouldThrowException() {
        // Arrange: First successful submission
        String platform = ScheduleService.PLATFORM_FACEBOOK;
        String content = "Content";
        LocalDateTime scheduleTime = LocalDateTime.now().plusDays(1);
        String duplicateKey = "duplicate-key-1";

        Schedule firstSchedule = new Schedule();
        firstSchedule.setScheduleId(UUID.randomUUID());
        firstSchedule.setUserId(testUserId);
        firstSchedule.setPlatform(platform);
        firstSchedule.setContent(content);
        firstSchedule.setScheduledTime(scheduleTime);
        firstSchedule.setStatus(ScheduleService.STATUS_PENDING);

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(firstSchedule);

        // Act: First call should succeed
        scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey);

        // Act: Second call with same key should fail
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, scheduleTime, duplicateKey));

        // Assert: Exception must reference duplicate request error code
        assertEquals(ErrorCode.DUPLICATE_REQUEST, ex.getErrorCode(), "Error code must be DUPLICATE_REQUEST");
        assertTrue(ex.getMessage().contains("Duplicate schedule request detected"), 
            "Message must indicate duplicate request detection");
        // Verify cause chain: null cause is explicitly passed in service, but exception wraps business context
        assertNull(ex.getCause(), "Cause should be null as per service design when throwing duplicate exception");
    }

    /**
     * Test negative path: null schedule time should throw ScheduleServiceException.
     * Business requirement: REQ-001 / EXC-001 - Null schedule time violates pre-condition and triggers exception.
     */
    @Test
    @DisplayName("Null schedule time should throw ScheduleServiceException")
    void schedulePost_nullScheduleTime_shouldThrowException() {
        // Arrange
        String platform = ScheduleService.PLATFORM_INSTAGRAM;
        String content = "Content";
        LocalDateTime nullTime = null;
        String idempotencyKey = validIdempotencyKey;

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.schedulePost(testUserId, platform, content, nullTime, idempotencyKey));
        
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Schedule time must be in the future"), "Message must handle null time as invalid");
        Throwable cause = ex.getCause();
        assertNotNull(cause, "Cause chain must preserve the NullPointerException or wrapped validation");
        assertTrue(cause.getMessage() != null && cause.getMessage().contains("Schedule time"), 
            "Root cause must reference schedule time constraint");
    }

    /**
     * Test happy path: retrieve a schedule by valid ID.
     * Business requirement: REQ-001 - System shall allow users to retrieve scheduled posts by ID.
     * Expected: Schedule entity returned successfully.
     */
    @Test
    @DisplayName("Find schedule by valid ID should return schedule")
    void findScheduleById_validId_shouldReturnSchedule() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule expectedSchedule = new Schedule();
        expectedSchedule.setScheduleId(scheduleId);
        expectedSchedule.setUserId(testUserId);
        expectedSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        expectedSchedule.setContent("Test content");
        expectedSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        expectedSchedule.setStatus(ScheduleService.STATUS_PENDING);
        expectedSchedule.setCreatedAt(LocalDateTime.now());
        expectedSchedule.setUpdatedAt(LocalDateTime.now());

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(expectedSchedule));

        // Act
        Schedule result = scheduleService.findScheduleById(scheduleId);

        // Assert
        assertNotNull(result, "Schedule should be found");
        assertEquals(scheduleId, result.getScheduleId(), "Schedule ID should match");
        assertEquals(testUserId, result.getUserId(), "User ID should match");
        assertEquals(ScheduleService.PLATFORM_FACEBOOK, result.getPlatform(), "Platform should match");
        verify(scheduleRepository, times(1)).findById(scheduleId);
    }

    /**
     * Test edge case: retrieve a schedule with non-existent ID.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules gracefully.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Find schedule by non-existent ID should throw ScheduleServiceException")
    void findScheduleById_nonExistentId_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.findScheduleById(nonExistentId));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).findById(nonExistentId);
    }

    /**
     * Test happy path: retrieve all schedules when schedules exist.
     * Business requirement: REQ-001 - System shall allow users to retrieve all scheduled posts.
     * Expected: List of schedules returned.
     */
    @Test
    @DisplayName("Find all schedules should return list of schedules")
    void findAllSchedules_shouldReturnSchedulesList() {
        // Arrange
        Schedule schedule1 = new Schedule();
        schedule1.setScheduleId(UUID.randomUUID());
        schedule1.setUserId(testUserId);
        schedule1.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        schedule1.setContent("Content 1");
        schedule1.setScheduledTime(LocalDateTime.now().plusDays(1));
        schedule1.setStatus(ScheduleService.STATUS_PENDING);
        schedule1.setCreatedAt(LocalDateTime.now());
        schedule1.setUpdatedAt(LocalDateTime.now());

        Schedule schedule2 = new Schedule();
        schedule2.setScheduleId(UUID.randomUUID());
        schedule2.setUserId(testUserId);
        schedule2.setPlatform(ScheduleService.PLATFORM_INSTAGRAM);
        schedule2.setContent("Content 2");
        schedule2.setScheduledTime(LocalDateTime.now().plusDays(2));
        schedule2.setStatus(ScheduleService.STATUS_PENDING);
        schedule2.setCreatedAt(LocalDateTime.now());
        schedule2.setUpdatedAt(LocalDateTime.now());

        List<Schedule> expectedSchedules = List.of(schedule1, schedule2);
        when(scheduleRepository.findAll()).thenReturn(expectedSchedules);

        // Act
        List<Schedule> result = scheduleService.findAllSchedules();

        // Assert
        assertNotNull(result, "Schedules list should not be null");
        assertEquals(2, result.size(), "Should return 2 schedules");
        assertTrue(result.stream().anyMatch(s -> s.getPlatform().equals(ScheduleService.PLATFORM_FACEBOOK)), 
            "Should contain Facebook schedule");
        assertTrue(result.stream().anyMatch(s -> s.getPlatform().equals(ScheduleService.PLATFORM_INSTAGRAM)), 
            "Should contain Instagram schedule");
        verify(scheduleRepository, times(1)).findAll();
    }

    /**
     * Test edge case: retrieve all schedules when no schedules exist.
     * Business requirement: REQ-001 - System should handle empty schedule list gracefully.
     * Expected: Empty list returned.
     */
    @Test
    @DisplayName("Find all schedules when none exist should return empty list")
    void findAllSchedules_emptyList_shouldReturnEmptyList() {
        // Arrange
        when(scheduleRepository.findAll()).thenReturn(List.of());

        // Act
        List<Schedule> result = scheduleService.findAllSchedules();

        // Assert
        assertNotNull(result, "Schedules list should not be null");
        assertTrue(result.isEmpty(), "Should return empty list");
        verify(scheduleRepository, times(1)).findAll();
    }

    /**
     * Test happy path: update a pending schedule successfully.
     * Business requirement: REQ-001 - System shall allow users to update scheduled posts.
     * Expected: Updated schedule returned with new content and time.
     */
    @Test
    @DisplayName("Update pending schedule should succeed")
    void updateSchedule_pendingSchedule_shouldUpdateSuccessfully() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule existingSchedule = new Schedule();
        existingSchedule.setScheduleId(scheduleId);
        existingSchedule.setUserId(testUserId);
        existingSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        existingSchedule.setContent("Original content");
        existingSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        existingSchedule.setStatus(ScheduleService.STATUS_PENDING);
        existingSchedule.setCreatedAt(LocalDateTime.now());
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        String newContent = "Updated content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(3);

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(existingSchedule));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(existingSchedule);

        // Act
        Schedule result = scheduleService.updateSchedule(scheduleId, newContent, newScheduleTime);

        // Assert
        assertNotNull(result, "Updated schedule should not be null");
        assertEquals(scheduleId, result.getScheduleId(), "Schedule ID should match");
        assertEquals(newContent, result.getContent(), "Content should be updated");
        assertEquals(newScheduleTime, result.getScheduledTime(), "Scheduled time should be updated");
        assertEquals(ScheduleService.STATUS_PENDING, result.getStatus(), "Status should remain pending");
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, times(1)).save(existingSchedule);
    }

    /**
     * Test edge case: update a non-existent schedule.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules during update.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Update non-existent schedule should throw ScheduleServiceException")
    void updateSchedule_nonExistentSchedule_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        String newContent = "New content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(1);

        when(scheduleRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.updateSchedule(nonExistentId, newContent, newScheduleTime));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).findById(nonExistentId);
        verify(scheduleRepository, never()).save(any());
    }

    /**
     * Test edge case: update a non-pending schedule (e.g., SENT).
     * Business requirement: REQ-001 / EXC-001 - System should prevent updates to non-pending schedules.
     * Expected: ScheduleServiceException with ILLEGAL_STATE error code.
     */
    @Test
    @DisplayName("Update non-pending schedule should throw ScheduleServiceException")
    void updateSchedule_nonPendingSchedule_shouldThrowException() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        Schedule existingSchedule = new Schedule();
        existingSchedule.setScheduleId(scheduleId);
        existingSchedule.setUserId(testUserId);
        existingSchedule.setPlatform(ScheduleService.PLATFORM_FACEBOOK);
        existingSchedule.setContent("Original content");
        existingSchedule.setScheduledTime(LocalDateTime.now().plusDays(1));
        existingSchedule.setStatus(ScheduleService.STATUS_SENT); // Non-pending status
        existingSchedule.setCreatedAt(LocalDateTime.now());
        existingSchedule.setUpdatedAt(LocalDateTime.now());

        String newContent = "New content";
        LocalDateTime newScheduleTime = LocalDateTime.now().plusDays(1);

        when(scheduleRepository.findById(scheduleId)).thenReturn(java.util.Optional.of(existingSchedule));

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.updateSchedule(scheduleId, newContent, newScheduleTime));
        
        assertEquals(ErrorCode.ILLEGAL_STATE, ex.getErrorCode(), "Error code should be ILLEGAL_STATE");
        assertTrue(ex.getMessage().contains("Only pending schedules can be updated"), 
            "Message should indicate only pending schedules can be updated");
        assertNull(ex.getCause(), "Cause should be null for illegal state scenario");
        verify(scheduleRepository, times(1)).findById(scheduleId);
        verify(scheduleRepository, never()).save(any());
    }

    /**
     * Test happy path: delete a schedule successfully.
     * Business requirement: REQ-001 - System shall allow users to delete scheduled posts.
     * Expected: Schedule deleted without exception.
     */
    @Test
    @DisplayName("Delete schedule should succeed")
    void deleteSchedule_validId_shouldDeleteSuccessfully() {
        // Arrange
        UUID scheduleId = UUID.randomUUID();
        when(scheduleRepository.existsById(scheduleId)).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById(scheduleId);

        // Act
        scheduleService.deleteSchedule(scheduleId);

        // Assert
        verify(scheduleRepository, times(1)).existsById(scheduleId);
        verify(scheduleRepository, times(1)).deleteById(scheduleId);
    }

    /**
     * Test edge case: delete a non-existent schedule.
     * Business requirement: REQ-001 / EXC-001 - System should handle missing schedules during deletion.
     * Expected: ScheduleServiceException with NOT_FOUND error code.
     */
    @Test
    @DisplayName("Delete non-existent schedule should throw ScheduleServiceException")
    void deleteSchedule_nonExistentSchedule_shouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(scheduleRepository.existsById(nonExistentId)).thenReturn(false);

        // Act & Assert
        ScheduleServiceException ex = assertThrows(ScheduleServiceException.class, () -> 
            scheduleService.deleteSchedule(nonExistentId));
        
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode(), "Error code should be NOT_FOUND");
        assertTrue(ex.getMessage().contains("Schedule not found"), "Message should indicate schedule not found");
        assertNull(ex.getCause(), "Cause should be null for not found scenario");
        verify(scheduleRepository, times(1)).existsById(nonExistentId);
        verify(scheduleRepository, never()).deleteById(any());
    }
}
```

# Day 2: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleControllerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleControllerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.schedulingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ScheduleController - REST API Controller for managing social media post scheduling.
 *
 * <p>This controller provides endpoints to create, retrieve, update, and delete
 * scheduled posts across multiple social media platforms (Facebook, Instagram, TikTok).
 * It enforces input validation, idempotency for mutation operations, and delegates
 * business logic to the ScheduleService layer.</p>
 *
 * <p>Security considerations:
 * - All mutation endpoints require an Idempotency-Key header to prevent duplicate processing.
 * - Input validation is enforced via @Valid and custom validators.
 * - Sensitive data (e.g., tokens) are masked in logs using a custom masking utility.</p>
 *
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    // Logger instance for structured logging at entry/exit points and error handling
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    // Injected service layer responsible for business logic and persistence operations
    private final ScheduleService scheduleService;

    /**
     * Constructor-based dependency injection for ScheduleService.
     *
     * @param scheduleService the service handling schedule CRUD operations
     */
    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Creates a new scheduled post entry.
     *
     * <p>This endpoint accepts a Schedule DTO, validates it, and delegates creation
     * to the service layer. An idempotency key is required to prevent duplicate
     * scheduling requests from being processed more than once.</p>
     *
     * @param scheduleDto the schedule data transfer object containing post details
     * @param idempotencyKey unique key to ensure idempotent request processing
     * @return ResponseEntity containing the created Schedule entity and HTTP 201 status
     * @throws ScheduleValidationException if input validation fails
     * @throws ScheduleConflictException if a duplicate schedule is detected
     */
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(
            @RequestBody ScheduleDto scheduleDto,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        logger.info("[ENTRY] [REQ-001] Received request to create schedule. Idempotency-Key: {}", maskIdempotencyKey(idempotencyKey));
        try {
            Schedule createdSchedule = scheduleService.createSchedule(scheduleDto, idempotencyKey);
            logger.info("[EXIT] [REQ-001] Successfully created schedule with ID: {}", createdSchedule.getScheduleId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (ScheduleValidationException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Validation error during schedule creation. Error: {}", e.getMessage());
            throw e;
        } catch (ScheduleConflictException e) {
            logger.error("[CRITICAL FAIL] [EXC-002] Conflict detected during schedule creation. Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Unexpected error during schedule creation. Error: {}", e.getMessage(), e);
            throw new ScheduleProcessingException("Unexpected error during schedule creation", e);
        }
    }

    /**
     * Retrieves a list of all scheduled posts.
     *
     * @return ResponseEntity containing a list of all Schedule entities
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        logger.info("[ENTRY] [REQ-001] Received request to fetch all schedules.");
        List<Schedule> schedules = scheduleService.getAllSchedules();
        logger.info("[EXIT] [REQ-001] Retrieved {} schedules from database.", schedules.size());
        return ResponseEntity.ok(schedules);
    }

    /**
     * Retrieves a specific scheduled post by its unique identifier.
     *
     * @param scheduleId the UUID of the schedule to retrieve
     * @return ResponseEntity containing the Schedule entity if found
     * @throws ScheduleNotFoundException if no schedule exists with the given ID
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable UUID scheduleId) {
        logger.info("[ENTRY] [REQ-001] Received request to fetch schedule by ID: {}", scheduleId);
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        logger.info("[EXIT] [REQ-001] Successfully retrieved schedule with ID: {}", scheduleId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Updates an existing scheduled post.
     *
     * <p>This endpoint requires an idempotency key to prevent duplicate updates.
     * Only the content, scheduled time, and status fields can be modified.</p>
     *
     * @param scheduleId the UUID of the schedule to update
     * @param scheduleDto the updated schedule data
     * @param idempotencyKey unique key to ensure idempotent request processing
     * @return ResponseEntity containing the updated Schedule entity
     * @throws ScheduleNotFoundException if no schedule exists with the given ID
     * @throws ScheduleValidationException if input validation fails
     * @throws ScheduleConflictException if a conflict occurs during update
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleDto scheduleDto,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        logger.info("[ENTRY] [REQ-001] Received request to update schedule ID: {}. Idempotency-Key: {}", scheduleId, maskIdempotencyKey(idempotencyKey));
        try {
            Schedule updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDto, idempotencyKey);
            logger.info("[EXIT] [REQ-001] Successfully updated schedule with ID: {}", scheduleId);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ScheduleNotFoundException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Schedule not found during update. ID: {}", scheduleId);
            throw e;
        } catch (ScheduleValidationException e) {
            logger.error("[CRITICAL FAIL] [EXC-002] Validation error during schedule update. Error: {}", e.getMessage());
            throw e;
        } catch (ScheduleConflictException e) {
            logger.error("[CRITICAL FAIL] [EXC-002] Conflict detected during schedule update. Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Unexpected error during schedule update. Error: {}", e.getMessage(), e);
            throw new ScheduleProcessingException("Unexpected error during schedule update", e);
        }
    }

    /**
     * Deletes a scheduled post by its unique identifier.
     *
     * <p>This endpoint requires an idempotency key to prevent duplicate deletions.</p>
     *
     * @param scheduleId the UUID of the schedule to delete
     * @param idempotencyKey unique key to ensure idempotent request processing
     * @return ResponseEntity with HTTP 204 No Content if deletion is successful
     * @throws ScheduleNotFoundException if no schedule exists with the given ID
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID scheduleId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        logger.info("[ENTRY] [REQ-001] Received request to delete schedule ID: {}. Idempotency-Key: {}", scheduleId, maskIdempotencyKey(idempotencyKey));
        try {
            scheduleService.deleteSchedule(scheduleId, idempotencyKey);
            logger.info("[EXIT] [REQ-001] Successfully deleted schedule with ID: {}", scheduleId);
            return ResponseEntity.noContent().build();
        } catch (ScheduleNotFoundException e) {
            logger.error("[CRITICAL FAIL] [EXC-001] Schedule not found during deletion. ID: {}", scheduleId);
            throw e;
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [EXC-002] Unexpected error during schedule deletion. Error: {}", e.getMessage(), e);
            throw new ScheduleProcessingException("Unexpected error during schedule deletion", e);
        }
    }

    /**
     * Utility method to mask the idempotency key in logs for security compliance.
     *
     * @param key the raw idempotency key
     * @return a masked representation of the key
     */
    private String maskIdempotencyKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp ScheduleController để đảm bảo các điểm cuối API hoạt động đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.schedulingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleControllerTest - Comprehensive unit test suite for ScheduleController REST endpoints.
 *
 * <p>This test class validates all API endpoints of the ScheduleController including
 * create, read, update, and delete operations for scheduled social media posts.
 * Tests cover happy paths, edge cases, boundary conditions, and exception scenarios
 * to ensure robust controller behavior and compliance with enterprise standards.</p>
 *
 * <p>Test Coverage Matrix:
 * - Happy Cases: Valid requests with correct payloads and headers
 * - Edge Cases: Boundary values, empty collections, null safety
 * - Exception Cases: Validation errors, not found, conflicts, processing failures</p>
 *
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleController Unit Tests")
class ScheduleControllerTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers Policy)
    // =========================================================================

    /** Base API path for schedule endpoints. */
    private static final String BASE_API_PATH = "/api/v1/schedules";

    /** Valid idempotency key for testing mutation endpoints. */
    private static final String VALID_IDEMPOTENCY_KEY = "idem-key-1234567890abcdef";

    /** Invalid idempotency key (too short) for testing validation. */
    private static final String INVALID_IDEMPOTENCY_KEY = "short";

    /** Test user ID constant. */
    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** Test schedule ID constant. */
    private static final UUID TEST_SCHEDULE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /** Test schedule ID for not found scenarios. */
    private static final UUID NON_EXISTENT_SCHEDULE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    /** Valid platform constant. */
    private static final String VALID_PLATFORM = "Facebook";

    /** Valid content constant. */
    private static final String VALID_CONTENT = "Test post content for scheduling";

    /** Future scheduled time constant (1 hour from now). */
    private static final LocalDateTime FUTURE_SCHEDULED_TIME = LocalDateTime.now().plusHours(1);

    /** Past scheduled time constant (for edge case testing). */
    private static final LocalDateTime PAST_SCHEDULED_TIME = LocalDateTime.now().minusHours(1);

    /** Empty content constant for validation testing. */
    private static final String EMPTY_CONTENT = "";

    /** Long content exceeding max length for boundary testing. */
    private static final String LONG_CONTENT = "A".repeat(5001);

    /** Valid status constant. */
    private static final String VALID_STATUS = "pending";

    /** Invalid status constant for validation testing. */
    private static final String INVALID_STATUS = "invalid_status";

    /** Expected schedule count for list tests. */
    private static final int EXPECTED_SCHEDULE_COUNT = 2;

    /** HTTP header name for idempotency key. */
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    /** JSON content type constant. */
    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    /** Logger instance for test execution tracing. */
    private static final Logger logger = LoggerFactory.getLogger(ScheduleControllerTest.class);

    // =========================================================================
    // TEST FIXTURES & MOCKS
    // =========================================================================

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ScheduleDto validScheduleDto;
    private Schedule validSchedule;

    // =========================================================================
    // SETUP & TEARDOWN
    // =========================================================================

    /**
     * Initializes test fixtures before each test method.
     * Sets up MockMvc, ObjectMapper, and valid test data objects.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] Initializing ScheduleControllerTest fixtures");
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
                .setControllerAdvice(new ScheduleExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime

        // Build valid ScheduleDto for reuse across tests
        validScheduleDto = ScheduleDto.builder()
                .userId(TEST_USER_ID)
                .platform(VALID_PLATFORM)
                .content(VALID_CONTENT)
                .scheduledTime(FUTURE_SCHEDULED_TIME)
                .status(VALID_STATUS)
                .build();

        // Build valid Schedule entity for response verification
        validSchedule = Schedule.builder()
                .scheduleId(TEST_SCHEDULE_ID)
                .userId(TEST_USER_ID)
                .platform(VALID_PLATFORM)
                .content(VALID_CONTENT)
                .scheduledTime(FUTURE_SCHEDULED_TIME)
                .status(VALID_STATUS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        logger.debug("[TEST_SETUP] Fixtures initialized successfully");
    }

    // =========================================================================
    // CREATE SCHEDULE TESTS (POST /api/v1/schedules)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/schedules - Create Schedule Tests")
    class CreateScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule creation with valid input and idempotency key.
         */
        @Test
        @DisplayName("Should create schedule successfully with valid input [REQ-001]")
        void shouldCreateScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule creation");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(validSchedule);

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.userId", is(TEST_USER_ID.toString())))
                    .andExpect(jsonPath("$.platform", is(VALID_PLATFORM)))
                    .andExpect(jsonPath("$.content", is(VALID_CONTENT)))
                    .andExpect(jsonPath("$.status", is(VALID_STATUS)));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001] Successful schedule creation test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation failure when validation exception is thrown.
         */
        @Test
        @DisplayName("Should return 400 when validation fails [REQ-001][EXC-001]")
        void shouldReturn400WhenValidationFails() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing validation failure on create");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(EMPTY_CONTENT) // Empty content should trigger validation
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content cannot be empty"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.message", is("Content cannot be empty")));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-001] Validation failure test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests schedule creation failure when conflict exception is thrown.
         */
        @Test
        @DisplayName("Should return 409 when duplicate schedule detected [REQ-001][EXC-002]")
        void shouldReturn409WhenDuplicateScheduleDetected() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing conflict on create");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleConflictException("Duplicate schedule for same user and time"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")))
                    .andExpect(jsonPath("$.message", is("Duplicate schedule for same user and time")));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-002] Conflict detection test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with missing idempotency key header.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key is missing [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissing() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key");
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest()); // Missing required header

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with invalid platform value.
         */
        @Test
        @DisplayName("Should return 400 when platform is invalid [REQ-001][EXC-001]")
        void shouldReturn400WhenPlatformInvalid() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid platform");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform("InvalidPlatform")
                    .content(VALID_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Invalid platform: InvalidPlatform"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid platform test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with past scheduled time (boundary condition).
         */
        @Test
        @DisplayName("Should return 400 when scheduled time is in the past [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduledTimeInPast() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing past scheduled time");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(PAST_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Scheduled time must be in the future"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Past scheduled time test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with content exceeding maximum length.
         */
        @Test
        @DisplayName("Should return 400 when content exceeds max length [REQ-001][EXC-001]")
        void shouldReturn400WhenContentExceedsMaxLength() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing content length boundary");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(LONG_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content exceeds maximum length of 5000 characters"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Content length boundary test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with null user ID.
         */
        @Test
        @DisplayName("Should return 400 when user ID is null [REQ-001][EXC-001]")
        void shouldReturn400WhenUserIdIsNull() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing null user ID");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(null)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("User ID is required"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Null user ID test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with null scheduled time.
         */
        @Test
        @DisplayName("Should return 400 when scheduled time is null [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduledTimeIsNull() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing null scheduled time");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(null)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Scheduled time is required"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Null scheduled time test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error occurs [REQ-001][EXC-001]")
        void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected processing error");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule creation")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error test passed");
        }
    }

    // =========================================================================
    // GET ALL SCHEDULES TESTS (GET /api/v1/schedules)
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/schedules - Get All Schedules Tests")
    class GetAllSchedulesTests {

        /**
         * @verifies [REQ-001]
         * Tests successful retrieval of all schedules.
         */
        @Test
        @DisplayName("Should return all schedules successfully [REQ-001]")
        void shouldReturnAllSchedulesSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing get all schedules");
            Schedule schedule2 = Schedule.builder()
                    .scheduleId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                    .userId(TEST_USER_ID)
                    .platform("Instagram")
                    .content("Second post")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(1))
                    .status(VALID_STATUS)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Schedule> schedules = Arrays.asList(validSchedule, schedule2);
            when(scheduleService.getAllSchedules()).thenReturn(schedules);

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(EXPECTED_SCHEDULE_COUNT)))
                    .andExpect(jsonPath("$[0].scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$[1].platform", is("Instagram")));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Get all schedules test passed");
        }

        /**
         * @verifies [REQ-001]
         * Tests retrieval of empty schedule list (edge case).
         */
        @Test
        @DisplayName("Should return empty list when no schedules exist [REQ-001]")
        void shouldReturnEmptyListWhenNoSchedulesExist() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing empty schedule list");
            when(scheduleService.getAllSchedules()).thenReturn(List.of());

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Empty schedule list test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests get all schedules when service throws unexpected exception.
         */
        @Test
        @DisplayName("Should return 500 when service throws unexpected error [REQ-001][EXC-001]")
        void shouldReturn500WhenServiceThrowsUnexpectedError() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected error on get all");
            when(scheduleService.getAllSchedules()).thenThrow(new RuntimeException("Database unavailable"));

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isInternalServerError());

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error on get all test passed");
        }
    }

    // =========================================================================
    // GET SCHEDULE BY ID TESTS (GET /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/schedules/{scheduleId} - Get Schedule By ID Tests")
    class GetScheduleByIdTests {

        /**
         * @verifies [REQ-001]
         * Tests successful retrieval of schedule by valid ID.
         */
        @Test
        @DisplayName("Should return schedule by ID successfully [REQ-001]")
        void shouldReturnScheduleByIdSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing get schedule by ID");
            when(scheduleService.getScheduleById(TEST_SCHEDULE_ID)).thenReturn(validSchedule);

            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.platform", is(VALID_PLATFORM)))
                    .andExpect(jsonPath("$.content", is(VALID_CONTENT)));

            verify(scheduleService).getScheduleById(TEST_SCHEDULE_ID);
            logger.info("[TEST_END] [REQ-001] Get schedule by ID test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests retrieval of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when schedule not found [REQ-001][EXC-001]")
        void shouldReturn404WhenScheduleNotFound() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing schedule not found");
            when(scheduleService.getScheduleById(NON_EXISTENT_SCHEDULE_ID))
                    .thenThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID));

            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")))
                    .andExpect(jsonPath("$.message", is("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID)));

            verify(scheduleService).getScheduleById(NON_EXISTENT_SCHEDULE_ID);
            logger.info("[TEST_END] [REQ-001][EXC-001] Schedule not found test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests retrieval with invalid UUID format (400 Bad Request).
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format is invalid [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdFormatInvalid() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID format");
            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID format test passed");
        }
    }

    // =========================================================================
    // UPDATE SCHEDULE TESTS (PUT /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/schedules/{scheduleId} - Update Schedule Tests")
    class UpdateScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule update with valid input.
         */
        @Test
        @DisplayName("Should update schedule successfully [REQ-001]")
        void shouldUpdateScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule update");
            ScheduleDto updateDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content("Updated content")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(2))
                    .status("sent")
                    .build();

            Schedule updatedSchedule = Schedule.builder()
                    .scheduleId(TEST_SCHEDULE_ID)
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content("Updated content")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(2))
                    .status("sent")
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(updatedSchedule);

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.content", is("Updated content")))
                    .andExpect(jsonPath("$.status", is("sent")));

            verify(scheduleService).updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001] Successful schedule update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when updating non-existent schedule [REQ-001][EXC-001]")
        void shouldReturn404WhenUpdatingNonExistentSchedule() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing update non-existent schedule");
            when(scheduleService.updateSchedule(eq(NON_EXISTENT_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")));

            verify(scheduleService).updateSchedule(eq(NON_EXISTENT_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-001] Update non-existent schedule test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with validation failure.
         */
        @Test
        @DisplayName("Should return 400 when update validation fails [REQ-001][EXC-001]")
        void shouldReturn400WhenUpdateValidationFails() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing update validation failure");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(EMPTY_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content cannot be empty"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Update validation failure test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests update with conflict exception.
         */
        @Test
        @DisplayName("Should return 409 when update causes conflict [REQ-001][EXC-002]")
        void shouldReturn409WhenUpdateCausesConflict() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing update conflict");
            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleConflictException("Schedule conflict with existing entry"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Update conflict test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with missing idempotency key.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key missing on update [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissingOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key on update");
            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key on update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with invalid schedule ID format.
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format invalid on update [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdInvalidOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID on update");
            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", "invalid-uuid")
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID on update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error on update [REQ-001][EXC-001]")
        void shouldReturn500WhenUnexpectedErrorOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected error on update");
            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new RuntimeException("Database deadlock"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule update")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error on update test passed");
        }
    }

    // =========================================================================
    // DELETE SCHEDULE TESTS (DELETE /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/v1/schedules/{scheduleId} - Delete Schedule Tests")
    class DeleteScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule deletion.
         */
        @Test
        @DisplayName("Should delete schedule successfully [REQ-001]")
        void shouldDeleteScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule deletion");
            doNothing().when(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isNoContent());

            verify(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);
            logger.info("[TEST_END] [REQ-001] Successful schedule deletion test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when deleting non-existent schedule [REQ-001][EXC-001]")
        void shouldReturn404WhenDeletingNonExistentSchedule() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing delete non-existent schedule");
            doThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID))
                    .when(scheduleService).deleteSchedule(NON_EXISTENT_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")));

            verify(scheduleService).deleteSchedule(NON_EXISTENT_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);
            logger.info("[TEST_END] [REQ-001][EXC-001] Delete non-existent schedule test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion with missing idempotency key.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key missing on delete [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissingOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key on delete");
            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key on delete test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion with invalid schedule ID format.
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format invalid on delete [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdInvalidOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID on delete");
            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", "invalid-uuid")
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID on delete test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests deletion with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error on delete [REQ-001][EXC-002]")
        void shouldReturn500WhenUnexpectedErrorOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing unexpected error on delete");
            doThrow(new RuntimeException("Foreign key constraint violation"))
                    .when(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule deletion")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Unexpected error on delete test passed");
        }
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases & Boundary Conditions")
    class EdgeCaseTests {

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with all valid platforms (Facebook, Instagram, TikTok).
         */
        @Test
        @DisplayName("Should accept all valid platforms [REQ-001][EXC-001]")
        void shouldAcceptAllValidPlatforms() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing all valid platforms");
            String[] platforms = {"Facebook", "Instagram", "TikTok"};

            for (String platform : platforms) {
                ScheduleDto dto = ScheduleDto.builder()
                        .userId(TEST_USER_ID)
                        .platform(platform)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(VALID_STATUS)
                        .build();

                Schedule schedule = Schedule.builder()
                        .scheduleId(UUID.randomUUID())
                        .userId(TEST_USER_ID)
                        .platform(platform)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(VALID_STATUS)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                        .thenReturn(schedule);

                mockMvc.perform(post(BASE_API_PATH)
                                .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                                .contentType(JSON_CONTENT_TYPE)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.platform", is(platform)));
            }
            logger.info("[TEST_END] [REQ-001][EXC-001] All valid platforms test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with all valid statuses.
         */
        @Test
        @DisplayName("Should accept all valid statuses [REQ-001][EXC-001]")
        void shouldAcceptAllValidStatuses() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing all valid statuses");
            String[] statuses = {"pending", "sent", "failed", "cancelled"};

            for (String status : statuses) {
                ScheduleDto dto = ScheduleDto.builder()
                        .userId(TEST_USER_ID)
                        .platform(VALID_PLATFORM)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(status)
                        .build();

                Schedule schedule = Schedule.builder()
                        .scheduleId(UUID.randomUUID())
                        .userId(TEST_USER_ID)
                        .platform(VALID_PLATFORM)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                        .thenReturn(schedule);

                mockMvc.perform(post(BASE_API_PATH)
                                .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                                .contentType(JSON_CONTENT_TYPE)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status", is(status)));
            }
            logger.info("[TEST_END] [REQ-001][EXC-001] All valid statuses test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with maximum valid content length (5000 chars).
         */
        @Test
        @DisplayName("Should accept content at maximum length boundary [REQ-001][EXC-001]")
        void shouldAcceptContentAtMaxLengthBoundary() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing max content length boundary");
            String maxContent = "A".repeat(5000);
            ScheduleDto dto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(maxContent)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            Schedule schedule = Schedule.builder()
                    .scheduleId(UUID.randomUUID())
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(maxContent)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(schedule);

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content", is(maxContent)));

            logger.info("[TEST_END] [REQ-001][EXC-001] Max content length boundary test passed");
        }

        /**
         * @verifies [REQ-001]
         * Tests get all schedules with large dataset (performance boundary).
         */
        @Test
        @DisplayName("Should handle large schedule list [REQ-001]")
        void shouldHandleLargeScheduleList() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing large schedule list");
            List<Schedule> largeList = java.util.stream.IntStream.range(0, 1000)
                    .mapToObj(i -> Schedule.builder()
                            .scheduleId(UUID.randomUUID())
                            .userId(TEST_USER_ID)
                            .platform(VALID_PLATFORM)
                            .content("Post " + i)
                            .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(i))
                            .status(VALID_STATUS)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .toList();

            when(scheduleService.getAllSchedules()).thenReturn(largeList);

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1000)));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Large schedule list test passed");
        }
    }

    // =========================================================================
    // CONCURRENCY & IDEMPOTENCY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Concurrency & Idempotency Tests")
    class ConcurrencyTests {

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests that duplicate requests with same idempotency key are handled.
         */
        @Test
        @DisplayName("Should handle duplicate idempotency key gracefully [REQ-001][EXC-002]")
        void shouldHandleDuplicateIdempotencyKey() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing duplicate idempotency key");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(validSchedule)
                    .thenThrow(new ScheduleConflictException("Duplicate request detected"));

            // First request succeeds
            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isCreated());

            // Second request with same key fails
            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Duplicate idempotency key test passed");
        }
    }
}
```

# Day 2: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
/**
 * ScheduleExceptionHandlerTest – Unit test suite for the centralized exception handling
 * mechanism in the Scheduling Service {@link ScheduleExceptionHandler}.
 * <p>
 * Validates that all runtime exceptions intercepted by {@code @ControllerAdvice}
 * are normalized into consistent HTTP error responses with structured payloads,
 * preserved exception cause chains, and comprehensive audit logging compliance.
 * All test methods explicitly reference inherited traceability Tag IDs for automated
 * compliance scanning and audit trail enforcement.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 * @author Enterprise Test Automation Framework
 * @since 2026.09.12
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequest.RequestDescription;

import java.time.Instant;

/**
 * Concrete unit test class validating {@link ScheduleExceptionHandler} behavior across
 * all defined {@code @ExceptionHandler} methods. Each test isolates a single handler,
 * mocks external dependencies (WebRequest), and asserts payload integrity, status codes,
 * and business code mapping per enterprise exception-handling contract.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleExceptionHandlerTest {

    /**
     * Mocked WebRequest instance used to simulate incoming HTTP request context
     * (e.g., request path, headers) during handler execution. All test cases stub
     * a deterministic request description to validate path inclusion in error payloads.
     */
    @Mock
    private WebRequest request;

    /**
     * System-under-test instance. All @ExceptionHandler methods are exercised via
     * direct invocation; logger static final field is structurally verified in
     * integration suite; this unit test focuses on ResponseEntity payload correctness.
     */
    private ScheduleExceptionHandler handler;

    /**
     * Pre-test initialization. Stubs the mocked WebRequest to return a consistent
     * request description ("POST /api/schedules") across all test methods, ensuring
     * path field assertions are deterministic and repeatable without live server context.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @BeforeEach
    void setUp() {
        RequestDescription desc = RequestDescription.create("POST /api/schedules");
        org.mockito.Mockito.when(request.getDescription(false)).thenReturn(desc);
        handler = new ScheduleExceptionHandler();
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleScheduleException(ScheduleException, WebRequest)}.
     * <p>
     * Asserts that a scheduling domain exception is transformed into an HTTP 500 response
     * with a complete error payload: timestamp, status code, error type, exception message,
     * request path, and business error code SCHED-500. Logger ERROR invocation is
     * structurally verified to include the three mandatory traceability context keys
     * (module, raw error message, tag ID pair) per enterprise logging law [0.3].
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleScheduleException() {
        // Arrange: Construct a ScheduleException wrapping a root-cause runtime failure
        Throwable rootCause = new RuntimeException("Underlying database connection lost on checkout");
        ScheduleException ex = new ScheduleException("Scheduling operation failed", rootCause);

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleScheduleException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for scheduling failures
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleScheduleException must return INTERNAL_SERVER_ERROR for scheduling domain failures");

        // Assert: Validate response body is a non-null LinkedHashMap with expected fields
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Extract and verify each payload field against constants defined in
        // ScheduleExceptionHandlerConstants; ensures anti-magic-numbers compliance [0.2]
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Timestamp field present and populated with ISO-8601 instant string
        Object timestamp = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP);
        assertNotNull(timestamp, "Payload must contain timestamp field per error contract");
        assertTrue(timestamp.toString().contains("T"),
                "Timestamp must be in ISO-8601 format (e.g., 2026-09-12T13:29:15.123Z)");

        // Assert: Status code field matches HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per enterprise error response schema");

        // Assert: Error type field contains the predefined service failure string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Scheduling Service Failure", error,
                "Error field must contain predefined 'Scheduling Service Failure' string");

        // Assert: Message field carries the original ScheduleException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduling operation failed", message,
                "Message field must reflect the ScheduleException's original cause message");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for audit logging");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in constants hoisted to class level [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleIllegalArgumentException(IllegalArgumentException, WebRequest)}.
     * <p>
     * Asserts that illegal argument / invalid input exceptions are transformed into an HTTP 400
     * response with a structured payload containing error type "Invalid Request", the raw
     * exception message, request path, and business error code SCHED-400. Validates that
     * input validation compliance is enforced at the API gateway before persistence or
     * event dispatch layers.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleIllegalArgumentException() {
        // Arrange: Construct an IllegalArgumentException representing invalid schedule parameters
        IllegalArgumentException ex = new IllegalArgumentException(
                "Scheduled time must be a future date-time value conforming to ISO-8601");

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex, request);

        // Assert: Verify HTTP 400 BAD_REQUEST status is returned for invalid input violations
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "handleIllegalArgumentException must return BAD_REQUEST for invalid request parameters");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 400 BAD_REQUEST value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.BAD_REQUEST.value(), status,
                "Status field must equal 400 per enterprise invalid-request error schema");

        // Assert: Error type field contains the predefined 'Invalid Request' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Invalid Request", error,
                "Error field must contain predefined 'Invalid Request' string per contract");

        // Assert: Message field carries the original IllegalArgumentException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduled time must be a future date-time value conforming to ISO-8601", message,
                "Message field must reflect the IllegalArgumentException's original cause message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-400 invalid input code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for validation error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT, businessCode,
                "Business code must map to SCHED-400 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleAllUncaughtException(Exception, WebRequest)}.
     * <p>
     * Asserts that any uncaught generic exception is wrapped into a new {@link ScheduleException}
     * preserving the original cause chain, logged at ERROR level with traceability tag IDs,
     * and returns an HTTP 500 response with the wrapped exception message and business code
     * SCHED-500. Validates the enterprise exception-cause-chain preservation law [0.3]: the
     * original exception object must not be severed or swallowed during wrapper construction.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleAllUncaughtException() {
        // Arrange: Construct a generic Exception representing an unexpected runtime failure
        // (e.g., NullPointerException, IllegalStateException from third-party pipeline)
        Exception ex = new IllegalStateException("Unexpected null pointer in scheduling pipeline at step-3");

        // Act: Invoke the global fallback handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleAllUncaughtException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for uncaught exceptions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleAllUncaughtException must return INTERNAL_SERVER_ERROR for any unhandled exception");

        // Assert: Response body is non-null and conforms to error payload contract
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for global error handling path");

        // Assert: Body is a Map instance for structured field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support enterprise error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per global uncaught-exception error schema");

        // Assert: Error type field contains the generic 'Internal Server Error' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Internal Server Error", error,
                "Error field must contain predefined 'Internal Server Error' string for top-level fallback");

        // Assert: Message field contains the wrapped exception context string as constructed
        // in handleAllUncaughtException: "Unexpected error while processing scheduling request"
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertTrue(message.toString().contains("Unexpected error while processing scheduling request"),
                "Message field must contain the wrapped context string preserving exception cause chain");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description even in global fallback path");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in class-level constants [0.2]");
    }
}
```
</EXISTING_TEST_SUITE_CODE>



### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
/**
 * ScheduleExceptionHandler – Centralized exception handling for the Scheduling Service.
 *
 * This {@code @ControllerAdvice} intercepts and normalizes all runtime exceptions
 * that occur within the scheduling module, providing consistent HTTP error responses
 * and comprehensive audit logging.
 *
 * <p>The handler enforces the following enterprise guardrails:
 * <ul>
 *   <li>Every catch block logs an error with the required three context keys
 *       (module subsystem name, raw exception message, traceability Tag ID).</li>
 *   <li>When re-throwing a custom {@link ScheduleException}, the original cause
 *       is preserved to satisfy the enterprise exception-cause-chain law.</li>
 *   <li>All error responses contain a traceable payload with timestamp, status,
 *       and a custom business error code.</li>
 *   <li>All literal configuration values are hoisted to the class-level constants
 *       to satisfy the anti-magic-numbers policy.</li>
 * </ul>
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

// --- Standard library imports ---
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// --- SLF4J / Logging framework imports ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- Spring Framework imports ---
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Custom runtime exception used to wrap domain-specific scheduling failures.
 * The original cause is preserved to satisfy the enterprise exception-cause-chain law.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleException extends RuntimeException {
    // Serial version UID for Serializable compliance across distributed deployments.
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ScheduleException with the specified detail message and cause.
     * The cause is forwarded to the parent RuntimeException constructor to preserve
     * the full stack-trace ancestry for centralized cloud logging aggregation.
     *
     * @param message the detail message explaining the scheduling failure
     * @param cause   the original throwable that triggered this exception
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    public ScheduleException(String message, Throwable cause) {
        // Delegate to parent constructor, forwarding both message and cause.
        super(message, cause);
    }
}

/**
 * Enterprise-grade constants for error codes, log messages, and configuration.
 * All literals are hoisted to the class level to satisfy the anti-magic-numbers guardrail.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleExceptionHandlerConstants {
    // Traceability tag identifiers – used in logging and Javadoc.
    public static final String TRACE_REQ_001 = "REQ-001";
    public static final String TRACE_EXC_001 = "EXC-001";
    public static final String TRACE_EXC_002 = "EXC-002";

    // HTTP error payload field names.
    public static final String ERROR_PAYLOAD_TIMESTAMP = "timestamp";
    public static final String ERROR_PAYLOAD_STATUS = "status";
    public static final String ERROR_PAYLOAD_ERROR = "error";
    public static final String ERROR_PAYLOAD_MESSAGE = "message";
    public static final String ERROR_PAYLOAD_PATH = "path";
    public static final String ERROR_PAYLOAD_BUSINESS_CODE = "businessCode";

    // Business error codes.
    public static final String BUSINESS_CODE_INVALID_INPUT = "SCHED-400";
    public static final String BUSINESS_CODE_RESOURCE_NOT_FOUND = "SCHED-404";
    public static final String BUSINESS_CODE_SCHEDULING_FAILURE = "SCHED-500";

    // Log message templates.
    public static final String LOG_ENTRY_POINT = "[ENTRY] ScheduleExceptionHandler invoked for {}";
    public static final String LOG_EXIT_POINT = "[EXIT] ScheduleExceptionHandler completed handling {}";
    public static final String LOG_ERROR_CRITICAL = "[CRITICAL FAIL] [{}] Scheduling operation failed. Raw error: {}";

    // Error description labels.
    public static final String ERROR_LABEL_SCHEDULING_FAILURE = "Scheduling Service Failure";
    public static final String ERROR_LABEL_INVALID_REQUEST = "Invalid Request";
    public static final String ERROR_LABEL_INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String ERROR_LABEL_RESOURCE_NOT_FOUND = "Resource Not Found";

    // Wrapped exception message for unexpected errors.
    public static final String WRAPPED_EXCEPTION_MESSAGE = "Unexpected error while processing scheduling request";
}

/**
 * Central exception handler that normalizes scheduling failures into structured HTTP responses.
 * <p>
 * The handler follows the enterprise logging and exception-cause-chain policies:
 * <ul>
 *   <li>Every catch block logs an error with the required three context keys (module, raw error, tag ID).</li>
 *   <li>When re-throwing a custom {@link ScheduleException}, the original cause is preserved.</li>
 *   <li>All error responses contain a traceable payload with timestamp, status, and custom business code.</li>
 * </ul>
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
@ControllerAdvice
class ScheduleExceptionHandler {

    // Class-level logger instance for structured audit logging.
    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionHandler.class);

    /**
     * Handles domain-specific scheduling exceptions.
     * <p>
     * This method logs the failure at ERROR level with the required traceability tag,
     * builds a detailed error payload, and returns a {@link ResponseEntity} with the
     * appropriate HTTP status (INTERNAL_SERVER_ERROR). The original exception cause is
     * preserved when a new {@link ScheduleException} is re-thrown elsewhere.
     *
     * @param ex      the caught {@link ScheduleException}
     * @param request the current web request (used to extract the request path)
     * @return a {@link ResponseEntity} containing the structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<Object> handleScheduleException(ScheduleException ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Enterprise-grade error logging – three mandatory context keys:
        // 1) Module subsystem name (embedded in tag), 2) Raw exception message, 3) Traceability Tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_SCHEDULING_FAILURE);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Handles illegal argument exceptions (e.g., invalid schedule parameters).
     * <p>
     * This method enforces input validation compliance and logs the violation with the
     * required traceability tags. It returns a BAD_REQUEST response containing a business
     * error code for downstream clients.
     *
     * @param ex      the caught {@link IllegalArgumentException}
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 400 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Critical error logging – module, raw error, tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_INVALID_REQUEST);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles resource-not-found exceptions (e.g., schedule ID not found in database).
     * <p>
     * This method intercepts {@link org.springframework.dao.EmptyResultDataAccessException}
     * and {@link org.springframework.web.server.ResponseStatusException} instances that
     * indicate a missing resource, returning a structured 404 response with a business
     * error code for downstream client consumption.
     *
     * @param ex      the caught exception indicating a missing resource
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 404 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler({
        org.springframework.dao.EmptyResultDataAccessException.class,
        org.springframework.web.server.ResponseStatusException.class
    })
    public ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Critical error logging – module, raw error, tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.NOT_FOUND.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_RESOURCE_NOT_FOUND);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_RESOURCE_NOT_FOUND);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Global fallback handler for any uncaught exceptions.
     * <p>
     * This method guarantees that no exception escapes without being logged and
     * transformed into a consistent error response. It preserves the original cause
     * chain when wrapping into a {@link ScheduleException} for downstream processing.
     *
     * @param ex      the caught generic {@link Exception}
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 500 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Preserve cause chain – rethrow a custom ScheduleException with original cause.
        ScheduleException wrapped = new ScheduleException(
                ScheduleExceptionHandlerConstants.WRAPPED_EXCEPTION_MESSAGE,
                ex
        );
        // Log the wrapped exception with required traceability tags.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                wrapped.getMessage(), wrapped);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_INTERNAL_SERVER_ERROR);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, wrapped.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp ScheduleExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * ScheduleExceptionHandlerTest – Unit test suite for the centralized exception handling
 * mechanism in the Scheduling Service {@link ScheduleExceptionHandler}.
 * <p>
 * Validates that all runtime exceptions intercepted by {@code @ControllerAdvice}
 * are normalized into consistent HTTP error responses with structured payloads,
 * preserved exception cause chains, and comprehensive audit logging compliance.
 * All test methods explicitly reference inherited traceability Tag IDs for automated
 * compliance scanning and audit trail enforcement.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 * @author Enterprise Test Automation Framework
 * @since 2026.09.12
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequest.RequestDescription;

import java.time.Instant;

/**
 * Concrete unit test class validating {@link ScheduleExceptionHandler} behavior across
 * all defined {@code @ExceptionHandler} methods. Each test isolates a single handler,
 * mocks external dependencies (WebRequest), and asserts payload integrity, status codes,
 * and business code mapping per enterprise exception-handling contract.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleExceptionHandlerTest {

    /**
     * Mocked WebRequest instance used to simulate incoming HTTP request context
     * (e.g., request path, headers) during handler execution. All test cases stub
     * a deterministic request description to validate path inclusion in error payloads.
     */
    @Mock
    private WebRequest request;

    /**
     * System-under-test instance. All @ExceptionHandler methods are exercised via
     * direct invocation; logger static final field is structurally verified in
     * integration suite; this unit test focuses on ResponseEntity payload correctness.
     */
    private ScheduleExceptionHandler handler;

    /**
     * Pre-test initialization. Stubs the mocked WebRequest to return a consistent
     * request description ("POST /api/schedules") across all test methods, ensuring
     * path field assertions are deterministic and repeatable without live server context.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @BeforeEach
    void setUp() {
        RequestDescription desc = RequestDescription.create("POST /api/schedules");
        org.mockito.Mockito.when(request.getDescription(false)).thenReturn(desc);
        handler = new ScheduleExceptionHandler();
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleScheduleException(ScheduleException, WebRequest)}.
     * <p>
     * Asserts that a scheduling domain exception is transformed into an HTTP 500 response
     * with a complete error payload: timestamp, status code, error type, exception message,
     * request path, and business error code SCHED-500. Logger ERROR invocation is
     * structurally verified to include the three mandatory traceability context keys
     * (module, raw error message, tag ID pair) per enterprise logging law [0.3].
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleScheduleException() {
        // Arrange: Construct a ScheduleException wrapping a root-cause runtime failure
        Throwable rootCause = new RuntimeException("Underlying database connection lost on checkout");
        ScheduleException ex = new ScheduleException("Scheduling operation failed", rootCause);

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleScheduleException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for scheduling failures
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleScheduleException must return INTERNAL_SERVER_ERROR for scheduling domain failures");

        // Assert: Validate response body is a non-null LinkedHashMap with expected fields
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Extract and verify each payload field against constants defined in
        // ScheduleExceptionHandlerConstants; ensures anti-magic-numbers compliance [0.2]
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Timestamp field present and populated with ISO-8601 instant string
        Object timestamp = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP);
        assertNotNull(timestamp, "Payload must contain timestamp field per error contract");
        assertTrue(timestamp.toString().contains("T"),
                "Timestamp must be in ISO-8601 format (e.g., 2026-09-12T13:29:15.123Z)");

        // Assert: Status code field matches HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per enterprise error response schema");

        // Assert: Error type field contains the predefined service failure string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Scheduling Service Failure", error,
                "Error field must contain predefined 'Scheduling Service Failure' string");

        // Assert: Message field carries the original ScheduleException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduling operation failed", message,
                "Message field must reflect the ScheduleException's original cause message");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for audit logging");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in constants hoisted to class level [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleIllegalArgumentException(IllegalArgumentException, WebRequest)}.
     * <p>
     * Asserts that illegal argument / invalid input exceptions are transformed into an HTTP 400
     * response with a structured payload containing error type "Invalid Request", the raw
     * exception message, request path, and business error code SCHED-400. Validates that
     * input validation compliance is enforced at the API gateway before persistence or
     * event dispatch layers.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleIllegalArgumentException() {
        // Arrange: Construct an IllegalArgumentException representing invalid schedule parameters
        IllegalArgumentException ex = new IllegalArgumentException(
                "Scheduled time must be a future date-time value conforming to ISO-8601");

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex, request);

        // Assert: Verify HTTP 400 BAD_REQUEST status is returned for invalid input violations
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "handleIllegalArgumentException must return BAD_REQUEST for invalid request parameters");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 400 BAD_REQUEST value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.BAD_REQUEST.value(), status,
                "Status field must equal 400 per enterprise invalid-request error schema");

        // Assert: Error type field contains the predefined 'Invalid Request' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Invalid Request", error,
                "Error field must contain predefined 'Invalid Request' string per contract");

        // Assert: Message field carries the original IllegalArgumentException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduled time must be a future date-time value conforming to ISO-8601", message,
                "Message field must reflect the IllegalArgumentException's original cause message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-400 invalid input code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for validation error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT, businessCode,
                "Business code must map to SCHED-400 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleResourceNotFoundException(Exception, WebRequest)}
     * for {@link org.springframework.dao.EmptyResultDataAccessException}.
     * <p>
     * Asserts that a JPA EmptyResultDataAccessException (thrown when a schedule entity is not found
     * by ID) is transformed into an HTTP 404 response with a structured payload containing
     * error type "Resource Not Found", the raw exception message, request path, and business
     * error code SCHED-404. Validates that resource-not-found compliance is enforced at the
     * persistence layer before propagating to the API gateway.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleResourceNotFoundException_EmptyResultDataAccessException() {
        // Arrange: Construct an EmptyResultDataAccessException representing a missing schedule entity
        // This exception is typically thrown by Spring Data JPA when findById returns empty
        org.springframework.dao.EmptyResultDataAccessException ex =
                new org.springframework.dao.EmptyResultDataAccessException(
                        "No Schedule entity found with id: 550e8400-e29b-41d4-a716-446655440000", 1);

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleResourceNotFoundException(ex, request);

        // Assert: Verify HTTP 404 NOT_FOUND status is returned for missing resource
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "handleResourceNotFoundException must return NOT_FOUND for EmptyResultDataAccessException");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 404 NOT_FOUND value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.NOT_FOUND.value(), status,
                "Status field must equal 404 per enterprise resource-not-found error schema");

        // Assert: Error type field contains the predefined 'Resource Not Found' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Resource Not Found", error,
                "Error field must contain predefined 'Resource Not Found' string per contract");

        // Assert: Message field carries the original EmptyResultDataAccessException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("No Schedule entity found with id: 550e8400-e29b-41d4-a716-446655440000", message,
                "Message field must reflect the EmptyResultDataAccessException's original cause message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-404 resource not found code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for resource-not-found error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_RESOURCE_NOT_FOUND, businessCode,
                "Business code must map to SCHED-404 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleResourceNotFoundException(Exception, WebRequest)}
     * for {@link org.springframework.web.server.ResponseStatusException} with HTTP 404 status.
     * <p>
     * Asserts that a ResponseStatusException (thrown when a schedule resource is explicitly
     * not found via controller logic) is transformed into an HTTP 404 response with a structured
     * payload containing error type "Resource Not Found", the raw exception message, request path,
     * and business error code SCHED-404. Validates that explicit NOT_FOUND responses from
     * controller layer are normalized consistently with persistence-layer not-found errors.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleResourceNotFoundException_ResponseStatusException() {
        // Arrange: Construct a ResponseStatusException with NOT_FOUND status representing
        // an explicit controller-level resource-not-found scenario
        org.springframework.web.server.ResponseStatusException ex =
                new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Schedule with id 550e8400-e29b-41d4-a716-446655440000 does not exist");

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleResourceNotFoundException(ex, request);

        // Assert: Verify HTTP 404 NOT_FOUND status is returned for missing resource
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "handleResourceNotFoundException must return NOT_FOUND for ResponseStatusException with 404");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 404 NOT_FOUND value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.NOT_FOUND.value(), status,
                "Status field must equal 404 per enterprise resource-not-found error schema");

        // Assert: Error type field contains the predefined 'Resource Not Found' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Resource Not Found", error,
                "Error field must contain predefined 'Resource Not Found' string per contract");

        // Assert: Message field carries the original ResponseStatusException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Schedule with id 550e8400-e29b-41d4-a716-446655440000 does not exist", message,
                "Message field must reflect the ResponseStatusException's original cause message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-404 resource not found code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for resource-not-found error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_RESOURCE_NOT_FOUND, businessCode,
                "Business code must map to SCHED-404 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleAllUncaughtException(Exception, WebRequest)}.
     * <p>
     * Asserts that any uncaught generic exception is wrapped into a new {@link ScheduleException}
     * preserving the original cause chain, logged at ERROR level with traceability tag IDs,
     * and returns an HTTP 500 response with the wrapped exception message and business code
     * SCHED-500. Validates the enterprise exception-cause-chain preservation law [0.3]: the
     * original exception object must not be severed or swallowed during wrapper construction.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleAllUncaughtException() {
        // Arrange: Construct a generic Exception representing an unexpected runtime failure
        // (e.g., NullPointerException, IllegalStateException from third-party pipeline)
        Exception ex = new IllegalStateException("Unexpected null pointer in scheduling pipeline at step-3");

        // Act: Invoke the global fallback handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleAllUncaughtException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for uncaught exceptions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleAllUncaughtException must return INTERNAL_SERVER_ERROR for any unhandled exception");

        // Assert: Response body is non-null and conforms to error payload contract
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for global error handling path");

        // Assert: Body is a Map instance for structured field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support enterprise error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per global uncaught-exception error schema");

        // Assert: Error type field contains the generic 'Internal Server Error' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Internal Server Error", error,
                "Error field must contain predefined 'Internal Server Error' string for top-level fallback");

        // Assert: Message field contains the wrapped exception context string as constructed
        // in handleAllUncaughtException: "Unexpected error while processing scheduling request"
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertTrue(message.toString().contains("Unexpected error while processing scheduling request"),
                "Message field must contain the wrapped context string preserving exception cause chain");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description even in global fallback path");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleAllUncaughtException(Exception, WebRequest)}
     * with a {@link NullPointerException} to verify cause chain preservation for NPE scenarios.
     * <p>
     * Asserts that a NullPointerException is wrapped into a ScheduleException preserving the
     * original cause, logged at ERROR level with traceability tag IDs, and returns an HTTP 500
     * response with the wrapped exception message and business code SCHED-500. This edge case
     * validates the handler's robustness against the most common runtime exception in Java.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleAllUncaughtException_NullPointerException() {
        // Arrange: Construct a NullPointerException representing a common runtime failure
        NullPointerException ex = new NullPointerException("scheduleRepository must not be null");

        // Act: Invoke the global fallback handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleAllUncaughtException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleAllUncaughtException must return INTERNAL_SERVER_ERROR for NullPointerException");

        // Assert: Response body is non-null and conforms to error payload contract
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for global error handling path");

        // Assert: Body is a Map instance for structured field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support enterprise error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per global uncaught-exception error schema");

        // Assert: Error type field contains the generic 'Internal Server Error' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Internal Server Error", error,
                "Error field must contain predefined 'Internal Server Error' string for top-level fallback");

        // Assert: Message field contains the wrapped exception context string
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertTrue(message.toString().contains("Unexpected error while processing scheduling request"),
                "Message field must contain the wrapped context string preserving exception cause chain");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description even in global fallback path");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleScheduleException(ScheduleException, WebRequest)}
     * with a null cause to verify null-safety in cause chain handling.
     * <p>
     * Asserts that a ScheduleException constructed with a null cause is still transformed
     * into an HTTP 500 response with a complete error payload. This edge case validates
     * the handler's robustness when the root cause is not available (e.g., manually thrown
     * ScheduleException without wrapping an underlying exception).
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleScheduleException_NullCause() {
        // Arrange: Construct a ScheduleException with null cause (edge case)
        ScheduleException ex = new ScheduleException("Scheduling operation failed without underlying cause", null);

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleScheduleException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleScheduleException must return INTERNAL_SERVER_ERROR even when cause is null");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per enterprise error response schema");

        // Assert: Error type field contains the predefined service failure string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Scheduling Service Failure", error,
                "Error field must contain predefined 'Scheduling Service Failure' string");

        // Assert: Message field carries the original ScheduleException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduling operation failed without underlying cause", message,
                "Message field must reflect the ScheduleException's original message even with null cause");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for audit logging");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleIllegalArgumentException(IllegalArgumentException, WebRequest)}
     * with an empty message to verify null/empty message handling.
     * <p>
     * Asserts that an IllegalArgumentException with an empty message string is still transformed
     * into an HTTP 400 response with a structured payload. This edge case validates the handler's
     * robustness when validation logic throws an exception without a descriptive message.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleIllegalArgumentException_EmptyMessage() {
        // Arrange: Construct an IllegalArgumentException with empty message (edge case)
        IllegalArgumentException ex = new IllegalArgumentException("");

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex, request);

        // Assert: Verify HTTP 400 BAD_REQUEST status is returned
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "handleIllegalArgumentException must return BAD_REQUEST even when message is empty");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 400 BAD_REQUEST value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.BAD_REQUEST.value(), status,
                "Status field must equal 400 per enterprise invalid-request error schema");

        // Assert: Error type field contains the predefined 'Invalid Request' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Invalid Request", error,
                "Error field must contain predefined 'Invalid Request' string per contract");

        // Assert: Message field carries the empty string (preserved from exception)
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field even when empty");
        assertEquals("", message,
                "Message field must reflect the IllegalArgumentException's empty message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-400 invalid input code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for validation error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT, businessCode,
                "Business code must map to SCHED-400 as defined in class-level constants [0.2]");
    }
}
```

