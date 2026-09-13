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