# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai dịch vụ lên lịch cho hệ thống. Dịch vụ này sẽ quản lý việc lên lịch và gửi các bài đăng lên các nền tảng mạng xã hội khác nhau.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai bộ xử lý ngoại lệ cho dịch vụ lên lịch. Bộ xử lý này sẽ quản lý các ngoại lệ và lỗi xảy ra trong quá trình lên lịch và gửi các bài đăng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

