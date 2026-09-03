/**
 * @traceability [REQ-002]
 * RecommendationRequestDto - Request DTO for AI-powered content recommendation generation.
 * Purpose: Carries user-provided parameters (user identifier, target platform, topic, tone preference,
 *          and content length constraints) from the API layer to the AI service business logic.
 * Validation: All fields enforced via Jakarta Validation annotations; topic validated against
 *             a strict whitelist regex to mitigate OWASP A03 injection risks.
 * Design: Immutable Java Record with automatic builder support for service-layer instantiation.
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import org.nlh4j.socialscheduler.aiservice.dto.enums.Platform;
import org.nlh4j.socialscheduler.aiservice.dto.enums.Tone;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record RecommendationRequestDTO(
        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "platform is required")
        Platform platform,

        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        String topic,

        // tone is optional; default value PROFESSIONAL is resolved in RecommendationService#generateRecommendation()
        Tone tone,

        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        Integer maxLength
) {
        /**
         * Returns the default tone constant if this record instance has no tone explicitly set.
         * @return Tone.PROFESSIONAL as per service-layer default resolution
         */
        public Tone getDefaultTone() {
                return Tone.PROFESSIONAL;
        }
}