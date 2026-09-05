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