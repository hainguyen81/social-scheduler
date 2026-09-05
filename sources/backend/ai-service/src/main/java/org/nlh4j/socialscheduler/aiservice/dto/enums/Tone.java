// ---------------------------------------------------------------------------
// [FILE] Tone.java
// ---------------------------------------------------------------------------
/**
 * Enum representing the tone/mode for AI-generated social media content recommendations.
 * Used to influence prompt engineering and content style in the ai-service layer.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

public enum Tone {
    PROFESSIONAL("Professional"),
    CASUAL("Casual"),
    HUMOROUS("Humorous"),
    INSPIRATIONAL("Inspirational");

    private final String displayName;

    Tone(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name for this tone, to be injected into OpenAI prompt templates.
     * @return String prompt modifier describing the content tone.
     */
    public String getPromptModifier() {
        return switch (this) {
            case PROFESSIONAL -> "professional, formal and business-oriented";
            case CASUAL -> "casual, conversational and friendly";
            case HUMOROUS -> "humorous, light-hearted and witty";
            case INSPIRATIONAL -> "inspirational, motivational and uplifting";
        };
    }
}

// ---------------------------------------------------------------------------
// [FILE] Platform.java
// ---------------------------------------------------------------------------
/**
 * Enum representing the supported social media platforms for scheduling and content generation.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

public enum Platform {
    FACEBOOK("Facebook Graph API"),
    INSTAGRAM("Instagram Graph API"),
    TIKTOK("TikTok Open API");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name identifier for this platform, used in API requests and prompt contexts.
     * @return String display name for the social media platform.
     */
    public String getDisplayName() {
        return displayName;
    }
}

// ---------------------------------------------------------------------------
// [FILE] RecommendationRequestDto.java
// ---------------------------------------------------------------------------
/**
 * Data Transfer Object for AI content recommendation requests.
 * Encapsulates user input for generating personalized social media post recommendations.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Tone;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@lombok.experimental.Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationRequestDto(
        @NotNull(message = "userId is required")
        private final UUID userId,

        @NotNull(message = "platform is required")
        private final Platform platform,

        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        private final String topic,

        @Builder.Default
        private final Tone tone = Tone.PROFESSIONAL,

        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        private final Integer maxLength
) {
}

// ---------------------------------------------------------------------------
// [FILE] RecommendationResponseDto.java
// ---------------------------------------------------------------------------
/**
 * Data Transfer Object for AI content recommendation responses.
 * Carries the generated content payload back to the caller with metadata.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponseDto {

    private final UUID recommendationId;

    private final UUID userId;

    private final String platform;

    private final String content;

    private final BigDecimal confidenceScore;

    private final boolean isFallback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime generatedAt;

    public RecommendationResponseDto(UUID recommendationId, UUID userId, String platform, String content, BigDecimal confidenceScore, boolean isFallback, OffsetDateTime generatedAt) {
        this.recommendationId = recommendationId;
        this.userId = userId;
        this.platform = platform;
        this.content = content;
        this.confidenceScore = confidenceScore;
        this.isFallback = isFallback;
        this.generatedAt = generatedAt;
    }

    public UUID getRecommendationId() { return recommendationId; }

    public UUID getUserId() { return userId; }

    public String getPlatform() { return platform; }

    public String getContent() { return content; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }

    public boolean isFallback() { return isFallback; }

    public OffsetDateTime getGeneratedAt() { return generatedAt; }
}