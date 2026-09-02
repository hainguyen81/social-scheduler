/**
 * RecommendationRequestDto - DTO encapsulating user intent for AI content generation requests.
 * Validates payload integrity via Jakarta Bean Validation annotations to prevent injection attacks
 * (OWASP A03) and ensures conformance to insecure design baselines (OWASP A04).
 * Supports immutable record semantics with Lombok Builder for service layer instantiation.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Tone;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lombok @Builder generates a static inner builder class enabling fluent, immutable construction
 * of the record from the AI service layer without exposing component accessors externally.
 * This enforces defensive copying and controlled object creation patterns.
 */
@lombok.Builder
/**
 * Jackson @JsonInclude configures the serializer to omit null-valued fields from the JSON
 * response payload. This adheres to the principle of minimal data exposure (NFR-002) and
 * reduces attack surface by transmitting only populated DTO attributes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationRequestDto(
        /**
         * Unique identifier of the requesting user. Must not be null.
         * Cross-referenced against user_schema.users under tenant isolation policy [DAT-001].
         * Validated non-null to guarantee every recommendation originates from an authenticated user.
         */
        @NotNull(message = "userId is required")
        UUID userId,

        /**
         * Target social media platform for content generation.
         * Restricted to whitelisted values: FACEBOOK, INSTAGRAM, TIKTOK via Platform enum.
         * @NotNull enforcement prevents routing to undefined platform handlers, supporting RBAC [ARC-001].
         */
        @NotNull(message = "platform is required")
        Platform platform,

        /**
         * Free-form topic or theme directive for the AI-generated content.
         * Subject to strict regex pattern validation to block XSS/HTML injection vectors
         * per OWASP A03. The pattern permits only alphanumeric characters, whitespace,
         * Unicode letters (\p{L}), permitted punctuation (\p{P}), and digits (\p{N}).
         * @Size(max=500) enforces downstream model token limits and prevents buffer overflow.
         * @NotBlank ensures a non-empty topic string is always supplied.
         */
        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        String topic,

        /**
         * Desired tone/style modifier for the AI completion prompt.
         * Optional field; if omitted, the service layer defaults to Tone.PROFESSIONAL
         * to maintain consistent content style across recommendation streams.
         * Used by OpenAIClient to inject getPromptModifier() into system message.
         */
        Tone tone,

        /**
         * Maximum character length constraint for the generated content body.
         * Must be an integer between 100 and 3000 inclusive.
         * @Min(100) prevents undersized content that fails model minimum token requirements.
         * @Max(3000) prevents oversized content that exceeds model context window and incurs
         * additional compute costs, aligning with NFR-001 throughput constraints.
         */
        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        Integer maxLength
) {
        // Java Record compiler-automatically generates:
        // - Private final components for each parameter
        // - Public getter methods (accessors)
        // - toString(), equals(), hashCode() implementations
        // - All-args constructor delegating to component initializers
        // Lombok @Builder statically generates a builder inner class
        // @JsonInclude NON_NULL ensures trailing nulls are omitted during JSON serialization
}