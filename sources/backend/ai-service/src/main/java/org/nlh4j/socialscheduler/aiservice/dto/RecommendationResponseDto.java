/**
 * Data Transfer Object for AI content recommendation responses.
 * Carries the generated content payload back to the caller with metadata.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.nlh4j.socialscheduler.common.Platform;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponseDto {

    private final UUID recommendationId;

    private final UUID userId;

    private final Platform platform;

    private final String content;

    private final BigDecimal confidenceScore;

    private final boolean isFallback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime generatedAt;

    public RecommendationResponseDto(UUID recommendationId, UUID userId, Platform platform, String content, BigDecimal confidenceScore, boolean isFallback, OffsetDateTime generatedAt) {
        this.recommendationId = recommendationId;
        this.userId = userId;
        this.platform = platform;
        this.content = content;
        this.confidenceScore = confidenceScore;
        this.isFallback = isFallback;
        this.generatedAt = generatedAt;
    }
}