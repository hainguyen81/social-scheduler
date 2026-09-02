package org.nlh4j.socialscheduler.scheduleservice.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.JsonInclude;

/**
 * Data Transfer Object for creating a new publishing schedule request.
 * Enforces strict input validation per REQ-003 and exception handling per EXC-002.
 * Guarantees scheduled times are future-dated, platform is whitelisted,
 * content size is bounded, and tenant isolation is supported via tenantId.
 * All validation annotations are enforced during deserialization by Spring Boot's DataBinder.
 * @traceability [REQ-003], [EXC-002]
 */
@Builder // Lombok-generated fluent builder for immutable record construction
@JsonInclude(JsonInclude.Include.NON_NULL) // Ensures only non-null fields are serialized into JSON payload
public record ScheduleRequestDto(
        // Platform must be one of the whitelisted social media services; blocks injection of arbitrary values per [REQ-003]
        @NotBlank(message = "platform is required")
        @Pattern(regexp = "^(Facebook|Instagram|TikTok)$", message = "platform must be one of Facebook, Instagram, TikTok")
        String platform,

        // Content body cannot be empty and must not exceed 5000 characters per business rule and database column limit
        @NotBlank(message = "content cannot be blank")
        @Size(min = 1, max = 5000, message = "content must not exceed 5000 characters")
        String content,

        // Scheduled execution time must be a future timestamp; prevents past-dated or immediate submissions per [EXC-002]
        @NotNull(message = "scheduledTime is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        @Future(message = "scheduledTime must be in the future")
        OffsetDateTime scheduledTime,

        // Maximum 10 media attachments per schedule to prevent resource exhaustion and enforce payload caps
        @Size(max = 10, message = "mediaUrls must not exceed 10 items")
        List<String> mediaUrls,

        // Tenant identifier enforces multi-tenant data isolation at the service layer per [NFR-003]
        @NotNull(message = "tenantId is required")
        UUID tenantId
) {
        // Record auto-generates private final fields, public getters, equals(), hashCode(), and toString().
        // Lombok @Builder enables construction via: ScheduleRequestDto.builder().platform("FACEBOOK").content("...").build()
        // All Jakarta Validation constraints are applied at the field level and validated by Spring's @Valid or @Validated.
        // tenantId must be populated by the interceptor/filter before reaching this DTO to ensure schema-per-tenant isolation.
}