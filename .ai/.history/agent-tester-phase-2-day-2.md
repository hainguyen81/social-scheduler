# Day 2: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
```java
// Traceability Tags: [REQ-001], [EXC-001], [EXC-002]

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
            logger.error("[CRITICAL FAIL] [EXC-001] Unexpected error while scheduling post. Raw error:
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm thử dịch vụ lên lịch để đảm bảo rằng nó hoạt động đúng và đáp ứng các yêu cầu chức năng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


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

# Day 2: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java` (Must map to sources/backend/ or sources/frontend/)




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
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
@ControllerAdvice
package org.nlh4j.socialscheduler.schedulingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom runtime exception used to wrap domain‑specific scheduling failures.
 * The original cause is preserved to satisfy the enterprise exception‑cause‑chain law.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Enterprise‑grade constants for error codes, log messages, and configuration.
 * All literals are hoisted to the class level to satisfy the anti‑magic‑numbers guardrail.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleExceptionHandlerConstants {
    // Traceability tag identifiers – used in logging and Javadoc.
    public static final String TRACE_REQ_001 = "REQ-001";
    public static final String TRACE_EXC_001 = "EXC-001";
    public static final String TRACE_EXC_002 = "EXC-002";

    // HTTP error payloads.
    public static final String ERROR_PAYLOAD_TIMESTAMP = "timestamp";
    public static final String ERROR_PAYLOAD_STATUS = "status";
    public static final String ERROR_PAYLOAD_ERROR = "error";
    public static final String ERROR_PAYLOAD_MESSAGE = "message";
    public static final String ERROR_PAYLOAD_PATH = "path";

    // Business error codes.
    public static final String BUSINESS_CODE_INVALID_INPUT = "SCHED-400";
    public static final String BUSINESS_CODE_RESOURCE_NOT_FOUND = "SCHED-404";
    public static final String BUSINESS_CODE_SCHEDULING_FAILURE = "SCHED-500";

    // Log message templates.
    public static final String LOG_ENTRY_POINT = "[ENTRY] ScheduleExceptionHandler invoked for {}";
    public static final String LOG_EXIT_POINT = "[EXIT] ScheduleExceptionHandler completed handling {}";
    public static final String LOG_ERROR_CRITICAL = "[CRITICAL FAIL] [{}] Scheduling operation failed. Raw error: {}";
}

/**
 * Central exception handler that normalizes scheduling failures into structured HTTP responses.
 * <p>
 * The handler follows the enterprise logging and exception‑cause‑chain policies:
 * <ul>
 *   <li>Every catch block logs an error with the required three context keys (module, raw error, tag ID).</li>
 *   <li>When re‑throwing a custom {@link ScheduleException}, the original cause is preserved.</li>
 *   <li>All error responses contain a traceable payload with timestamp, status, and custom business code.</li>
 * </ul>
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
@ControllerAdvice
class ScheduleExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionHandler.class);

    /**
     * Handles domain‑specific scheduling exceptions.
     * <p>
     * This method logs the failure at ERROR level with the required traceability tag,
     * builds a detailed error payload, and returns a {@link ResponseEntity} with the
     * appropriate HTTP status (INTERNAL_SERVER_ERROR). The original exception cause is
     * preserved when a new {@link ScheduleException} is re‑thrown elsewhere.
     *
     * @param ex      the caught {@link ScheduleException}
     * @param request the current web request (used to extract the request path)
     * @return a {@link ResponseEntity} containing the structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<Object> handleScheduleException(ScheduleException ex, WebRequest request) {
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Enterprise‑grade error logging – three mandatory context keys.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, "Scheduling Service Failure");
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put("businessCode", ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

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
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Critical error logging – module, raw error, tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, "Invalid Request");
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put("businessCode", ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT);

        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Preserve cause chain – rethrow a custom ScheduleException with original cause.
        ScheduleException wrapped = new ScheduleException(
                "Unexpected error while processing scheduling request",
                ex
        );
        // Log the wrapped exception with required traceability tags.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                wrapped.getMessage(), wrapped);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, "Internal Server Error");
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, wrapped.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put("businessCode", ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm thử bộ xử lý ngoại lệ để đảm bảo rằng nó quản lý các ngoại lệ và lỗi một cách chính xác và hiệu quả.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


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

