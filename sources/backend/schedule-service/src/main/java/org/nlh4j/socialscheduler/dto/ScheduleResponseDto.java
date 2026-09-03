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