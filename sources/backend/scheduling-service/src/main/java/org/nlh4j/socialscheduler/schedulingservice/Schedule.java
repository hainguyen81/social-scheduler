package org.nlh4j.socialscheduler.schedulingservice;

/**
 * Schedule entity representing a scheduled post for social media platforms.
 * <p>
 * This class encapsulates the core data model for managing scheduled posts,
 * including metadata such as the target platform, content, and execution status.
 * All business rules and validation logic are enforced through method contracts
 * and exception handling that adhere to enterprise traceability requirements.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
@Entity
@Table(name = "schedules")
public class Schedule {

    // Traceability constants for error logging and exception handling
    private static final String TRACE_REQ_ID = "REQ-001";
    private static final String TRACE_EXC_ID_1 = "EXC-001";
    private static final String TRACE_EXC_ID_2 = "EXC-002";

    // Logger for enterprise audit logging
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Schedule.class);

    // Platform constants – immutable, defined at class level to satisfy anti‑magic‑number rule
    public static final String PLATFORM_FACEBOOK = "Facebook";
    public static final String PLATFORM_INSTAGRAM = "Instagram";
    public static final String PLATFORM_TIKTOK = "TikTok";

    // Status constants – used for state transitions and validation
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Primary key – UUID ensures global uniqueness across micro‑services
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private java.util.UUID scheduleId;

    // Foreign key linking the schedule to its owning user
    @Column(name = "user_id", nullable = false)
    private java.util.UUID userId;

    // Target social platform – constrained by {@link #PLATFORM_FACEBOOK} etc.
    @Column(name = "platform", nullable = false)
    private String platform;

    // The actual post content – stored as TEXT to preserve formatting
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // Exact moment when the post should be published
    @Column(name = "scheduled_time", nullable = false)
    private java.time.LocalDateTime scheduledTime;

    // Current processing state of the schedule
    @Column(name = "status", nullable = false)
    private String status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default constructor required by JPA. */
    public Schedule() {
        // Initialise default status to PENDING – a safe, neutral state
        this.status = STATUS_PENDING;
    }

    /**
     * Full‑argument constructor for creating a new schedule instance.
     * <p>
     * This constructor performs lightweight validation and logs any anomalies
     * using the enterprise traceability identifiers.
     *
     * @param userId      the identifier of the user who owns the schedule
     * @param platform    the target social platform (must be one of the defined constants)
     * @param content     the post content to be published
     * @param scheduledTime the exact time at which the post should be sent
     * @throws ScheduleProcessingException if any validation rule is violated
     */
    public Schedule(java.util.UUID userId, String platform, String content, java.time.LocalDateTime scheduledTime)
            throws ScheduleProcessingException {
        // Defensive copy of immutable fields
        this.userId = userId;
        this.platform = validatePlatform(platform);
        this.content = validateContent(content);
        this.scheduledTime = validateScheduledTime(scheduledTime);
        this.status = STATUS_PENDING;

        // Log successful instantiation with traceability tags
        logger.info("[INFO] [{}] Schedule created successfully – ID: {}, Platform: {}", TRACE_REQ_ID, this.scheduleId, this.platform);
    }

    // -------------------------------------------------------------------------
    // Business‑logic & Validation Methods
    // -------------------------------------------------------------------------

    /**
     * Validates the platform against the allowed set of constants.
     * <p>
     * This method enforces the {@link #PLATFORM_FACEBOOK}, {@link #PLATFORM_INSTAGRAM},
     * and {@link #PLATFORM_TIKTOK} constraints. If an unsupported platform is supplied,
     * an {@link ScheduleProcessingException} is thrown and logged with the designated
     * exception traceability tags.
     *
     * @param platform the platform string to validate
     * @return the normalized platform string
     * @throws ScheduleProcessingException when the platform is illegal
     */
    private String validatePlatform(String platform) throws ScheduleProcessingException {
        if (platform == null || !(platform.equals(PLATFORM_FACEBOOK) ||
                                 platform.equals(PLATFORM_INSTAGRAM) ||
                                 platform.equals(PLATFORM_TIKTOK))) {
            String errorMsg = "Invalid platform supplied: " + platform;
            logger.error("[ERROR] [{}] [{}] Invalid platform – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, errorMsg);
            throw new ScheduleProcessingException(errorMsg, TRACE_EXC_ID_1, TRACE_EXC_ID_2);
        }
        return platform;
    }

    /**
     * Validates that the content is not null or empty.
     * <p>
     * Enforces a minimal length requirement (1 character) to avoid publishing
     * meaningless posts. Any violation is logged with the appropriate exception tags.
     *
     * @param content the post content to validate
     * @return the validated content
     * @throws ScheduleProcessingException when content is invalid
     */
    private String validateContent(String content) throws ScheduleProcessingException {
        if (content == null || content.trim().isEmpty()) {
            String errorMsg = "Schedule content cannot be null or empty";
            logger.error("[ERROR] [{}] [{}] Invalid content – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, errorMsg);
            throw new ScheduleProcessingException(errorMsg, TRACE_EXC_ID_1, TRACE_EXC_ID_2);
        }
        return content.trim();
    }

    /**
     * Validates that the scheduled time is in the future (or present).
     * <p>
     * This guard prevents back‑dating posts, which could violate platform policies.
     * Invalid timestamps are logged with the designated exception traceability tags.
     *
     * @param scheduledTime the time to validate
     * @return the validated scheduled time
     * @throws ScheduleProcessingException when the time is in the past
     */
    private java.time.LocalDateTime validateScheduledTime(java.time.LocalDateTime scheduledTime) throws ScheduleProcessingException {
        if (scheduledTime == null || scheduledTime.isBefore(java.time.LocalDateTime.now())) {
            String errorMsg = "Scheduled time must be in the future: " + scheduledTime;
            logger.error("[ERROR] [{}] [{}] Invalid scheduled time – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, errorMsg);
            throw new ScheduleProcessingException(errorMsg, TRACE_EXC_ID_1, TRACE_EXC_ID_2);
        }
        return scheduledTime;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters – JPA requires non‑final fields and public accessors
    // -------------------------------------------------------------------------

    public java.util.UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(java.util.UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public java.util.UUID getUserId() {
        return userId;
    }

    public void setUserId(java.util.UUID userId) {
        this.userId = userId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        // Re‑run validation on setter to maintain invariants
        try {
            this.platform = validatePlatform(platform);
        } catch (ScheduleProcessingException e) {
            // Wrap the validation exception into a runtime error to keep the setter signature simple
            logger.error("[ERROR] [{}] [{}] Platform validation failed during setter – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, platform, e);
            throw new IllegalArgumentException("Invalid platform: " + platform, e);
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        try {
            this.content = validateContent(content);
        } catch (ScheduleProcessingException e) {
            logger.error("[ERROR] [{}] [{}] Content validation failed during setter – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, content, e);
            throw new IllegalArgumentException("Invalid content: " + content, e);
        }
    }

    public java.time.LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(java.time.LocalDateTime scheduledTime) {
        try {
            this.scheduledTime = validateScheduledTime(scheduledTime);
        } catch (ScheduleProcessingException e) {
            logger.error("[ERROR] [{}] [{}] Scheduled time validation failed during setter – raw input: {}", TRACE_EXC_ID_1, TRACE_EXC_ID_2, scheduledTime, e);
            throw new IllegalArgumentException("Invalid scheduled time: " + scheduledTime, e);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status; // Status transitions are managed by service layer; simple setter for flexibility
    }

    // -------------------------------------------------------------------------
    // Utility & Debugging
    // -------------------------------------------------------------------------

    /**
     * Standard {@link Object#toString()} implementation for logging and debugging.
     * <p>
     * The method deliberately excludes sensitive payload data (e.g., full content)
     * to satisfy the enterprise data‑masking policy while still providing enough
     * context for operational troubleshooting.
     */
    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", platform='" + platform + '\'' +
                ", contentLength=" + (content != null ? content.length() : 0) +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                '}';
    }

    // -------------------------------------------------------------------------
    // Custom Exception – encapsulates traceability identifiers for downstream handling
    // -------------------------------------------------------------------------

    /**
     * Exception thrown when a schedule‑related business rule is violated.
     * <p>
     * This exception preserves the original cause chain and embeds the traceability
     * tags required by the enterprise audit framework.
     */
    @SuppressWarnings("serial")
    public static class ScheduleProcessingException extends RuntimeException {
        private final String excTag1;
        private final String excTag2;

        public ScheduleProcessingException(String message, String excTag1, String excTag2) {
            super(message);
            this.excTag1 = excTag1;
            this.excTag2 = excTag2;
        }

        public String getExcTag1() {
            return excTag1;
        }

        public String getExcTag2() {
            return excTag2;
        }
    }
}