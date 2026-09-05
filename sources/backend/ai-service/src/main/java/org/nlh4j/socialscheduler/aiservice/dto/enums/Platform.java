/**
 * Enumeration of supported social media platforms for scheduling, publishing, and AI-driven content recommendation.
 * Each constant carries a display name compatible with external platform APIs (Facebook Graph, Instagram Graph, TikTok Open).
 * Traceability: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

import java.util.Objects;

/**
 * Represents a social media platform supported by the Social Scheduler AI service.
 * Provides a standardized display name for API requests, documentation, and prompt template injection.
 * <p>Every constant maps to an external platform API endpoint configuration.</p>
 */
public enum Platform {

    /**
     * Facebook Graph API platform endpoint.
     * Used for posting to Facebook Pages and Feeds via the Facebook Graph API v18.0+.
     * <p>Display name: "Facebook Graph API"</p>
     */
    FACEBOOK("Facebook Graph API"),

    /**
     * Instagram Graph API platform endpoint.
     * Used for posting to Instagram Business/Creator accounts via the Instagram Graph API v17.0+.
     * <p>Display name: "Instagram Graph API"</p>
     */
    INSTAGRAM("Instagram Graph API"),

    /**
     * TikTok Open API platform endpoint.
     * Used for posting to TikTok for Business accounts via the TikTok Open API.
     * <p>Display name: "TikTok Open API"</p>
     */
    TIKTOK("TikTok Open API");

    // Display name string for this platform constant, used in API payloads and AI prompt templates.
    private final String displayName;

    // Initializes the Platform enum constant with its associated display name.
    // The display name is immutable and mapped directly from the external API vendor terminology.
    Platform(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name associated with this platform enum constant.
     * This method is invoked by the AI recommendation service to inject platform-specific
     * prompt modifiers and by the API gateway for request/response payload serialization.
     * @return String the platform display name (e.g., "Facebook Graph API")
     */
    public String getDisplayName() {
        return displayName;
    }

    // Ensures value-based equality comparison between Platform constants based on their display name.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Platform platform = (Platform) o;
        return Objects.equals(displayName, platform.displayName);
    }

    // Generates a hash code for this Platform constant based on its display name string.
    @Override
    public int hashCode() {
        return Objects.hash(displayName);
    }
}

/**
 * Enumeration of content tone modifiers for AI prompt injection.
 * Each constant defines a stylistic direction for AI-generated content.
 * Traceability: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

import java.util.Objects;

/**
 * Represents a content tone supported by the Social Scheduler AI service.
 * Provides a prompt modifier string used to influence AI model style and formatting.
 * <p>Every constant maps to a predefined tone category used in prompt template generation.</p>
 */
public enum Tone {

    /**
     * Professional tone for formal, business-oriented social media posts.
     * Prompt modifier: "maintain a professional, corporate voice suitable for B2B audiences."
     */
    PROFESSIONAL("Professional"),

    /**
     * Casual tone for relaxed, conversational social media posts.
     * Prompt modifier: "use a friendly, approachable tone suitable for lifestyle brands."
     */
    CASUAL("Casual"),

    /**
     * Humorous tone for entertaining, light-hearted social media posts.
     * Prompt modifier: "inject wit and humor suitable for engagement-driven campaigns."
     */
    HUMOROUS("Humorous"),

    /**
     * Inspirational tone for motivational, uplifting social media posts.
     * Prompt modifier: "employ an inspiring, aspirational tone suitable for brand storytelling."
     */
    INSPIRATIONAL("Inspirational");

    // Prompt modifier string for this tone constant, injected into AI Completion prompts.
    private final String promptModifier;

    // Initializes the Tone enum constant with its associated prompt modifier string.
    Tone(String promptModifier) {
        this.promptModifier = promptModifier;
    }

    /**
     * Returns the prompt modifier string associated with this tone enum constant.
     * This string is directly injected into AI prompt templates to shape content style.
     * @return String the prompt modifier (e.g., "Professional")
     */
    public String getPromptModifier() {
        return promptModifier;
    }

    // Ensures value-based equality comparison between Tone constants based on their prompt modifier.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tone tone = (Tone) o;
        return Objects.equals(promptModifier, tone.promptModifier);
    }

    // Generates a hash code for this Tone constant based on its prompt modifier string.
    @Override
    public int hashCode() {
        return Objects.hash(promptModifier);
    }
}

/**
 * Data Transfer Object for AI content recommendation requests.
 * Encapsulates all required and optional parameters needed to generate personalized social media content.
 * Implements Java Record for immutability and automatic boilerplate (getters, equals, hashCode, toString).
 * All fields are validated via Jakarta Bean Validation annotations to enforce schema constraints
 * and prevent malformed or malicious payloads from reaching the AI service layer.
 * Traceability: [REQ-002]
 * <p>Serialization note: Jackson defaults are used; {@link JsonInclude} ensures
 * omitted fields are excluded from the serialized JSON payload per OWASP A05 / NFR-002.</p>
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request DTO for generating AI-recommended social media content.
 * <p>This record is immutable; all fields are final and accessed via generated getters.</p>
 * <p>Validation constraints are enforced at the controller layer via Spring's {@code @Valid} annotation.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // omits null fields from JSON output
public record RecommendationRequestDTO(

    // Unique identifier of the user requesting the recommendation.
    // Must not be null; validated via {@code @NotNull} with custom message.
    // Format: UUID v4 string.
    @NotNull(message = "userId is required")
    private final UUID userId,

    // The target social media platform for content generation.
    // Must be a valid {@link Platform} enum value; validated via {@code @NotNull}.
    // Drives prompt template selection and API endpoint routing.
    @NotNull(message = "platform is required")
    private final Platform platform,

    // The core topic or theme around which the AI-generated content will revolve.
    // Must not be blank and must not exceed 500 characters.
    // Additional regex pattern prevents XSS-related injection characters per OWASP A03.
    @NotBlank(message = "topic cannot be blank")
    @Size(max = 500, message = "topic must not exceed 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\\\s\\\\p{L}\\\\p{P}\\\\p{N}]{1,500}$", message = "topic contains invalid characters")
    private final String topic,

    // The desired content tone, influencing AI prompt style.
    // Defaults to {@link Tone#PROFESSIONAL} if not specified by the caller.
    // Optional field; omitted from JSON if not provided (handled by {@link JsonInclude}).
    private final Tone tone = Tone.PROFESSIONAL,

    // Maximum character length for the generated content.
    // Must be between 100 and 3000 inclusive; validated via {@code @Min} and {@code @Max}.
    // Controls output length for AI model token budgeting.
    @Min(value = 100, message = "maxLength must be at least 100")
    @Max(value = 3000, message = "maxLength must not exceed 3000")
    private final Integer maxLength
) {
    // Record component: all fields are final and initialized via constructor.
    // Invariants enforced by Jakarta validation annotations on each component field.
    // Invariants: userId must not be null; platform must be a valid enum value;
    // topic must match the defined regex pattern; if provided, maxLength must be in [100, 3000].
}