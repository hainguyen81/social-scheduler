# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

<EXISTING_SOURCE_CODE_WORKSPACE>
* **File Existence Status:** PROCOVERY_MAINTENANCE
* **Current Disk Content Base:**
<EXISTING_SOURCE_CODE>
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
</EXISTING_SOURCE_CODE>
</EXISTING_SOURCE_CODE_WORKSPACE>


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp ScheduleService với các phương thức quản lý lịch đăng bài cơ bản như tạo, đọc, cập nhật và xóa lịch đăng bài.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Analyze the `<EXISTING_SOURCE_CODE_WORKSPACE>` layout, you MUST perform an AST-level incremental insertion of the requested sub-tasks into the current file content. You ARE STRICTLY FORBIDDEN from dropping old logic.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 2: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp ScheduleController với các điểm cuối API để quản lý lịch đăng bài.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 2: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

<EXISTING_SOURCE_CODE_WORKSPACE>
* **File Existence Status:** PROCOVERY_MAINTENANCE
* **Current Disk Content Base:**
<EXISTING_SOURCE_CODE>
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
</EXISTING_SOURCE_CODE>
</EXISTING_SOURCE_CODE_WORKSPACE>


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp ScheduleExceptionHandler để xử lý các ngoại lệ liên quan đến lịch đăng bài.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Analyze the `<EXISTING_SOURCE_CODE_WORKSPACE>` layout, you MUST perform an AST-level incremental insertion of the requested sub-tasks into the current file content. You ARE STRICTLY FORBIDDEN from dropping old logic.


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

