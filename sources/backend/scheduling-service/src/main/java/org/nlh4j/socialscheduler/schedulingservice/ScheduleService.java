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