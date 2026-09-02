package org.nlh4j.socialscheduler.aiservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final OpenAIClient openAIClient;
    private final PerformanceAnalyticsClient performanceAnalyticsClient;
    private final DefaultContentFallback defaultContentFallback;
    private final Counter recommendationCounter;
    private final Timer recommendationTimer;

    @Autowired
    public RecommendationService(OpenAIClient openAIClient,
                                 PerformanceAnalyticsClient performanceAnalyticsClient,
                                 DefaultContentFallback defaultContentFallback,
                                 Counter recommendationCounter,
                                 Timer recommendationTimer) {
        this.openAIClient = openAIClient;
        this.performanceAnalyticsClient = performanceAnalyticsClient;
        this.defaultContentFallback = defaultContentFallback;
        this.recommendationCounter = recommendationCounter;
        this.recommendationTimer = recommendationTimer;
    }

    /**
     * @traceability [REQ-002], [EXC-003], [EXC-004]
     * Generates a content recommendation for a user based on their request.
     * @param request the request containing userId, platform, topic, tone, and optional maxLength
     * @return RecommendationResponseDto containing the generated recommendation
     */
    public RecommendationResponseDto generateRecommendation(RecommendationRequestDto request) {
        // Step 1: Extract tenantId from SecurityContext
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Step 1: Get top 5 performing posts
        List<PerformanceMetricEntity> topPosts = performanceAnalyticsClient.findTopPerformingPosts(
                request.userId(), request.platform().name(), 5);

        // Step 2: Build prompt using templates
        PromptTemplateConfig templates = promptTemplates;
        String systemPrompt = buildSystemPrompt(templates, request);
        String userPrompt = buildUserPrompt(templates, request, topPosts);

        // Step 3: Generate content
        String generatedContent;
        try {
            generatedContent = openAIClient.generateContent(systemPrompt, userPrompt);
            if (generatedContent == null || generatedContent.length() > request.maxLength()) {
                throw new FallbackContentException("Generated content is empty or exceeds max length");
            }
        } catch (AiServiceException e) {
            log.warn("OpenAI service unavailable, activating fallback for userId={}, correlationId={}", request.userId(), MDC.get("correlationId"), e);
            generatedContent = defaultContentFallback.provide(request);
        } catch (FallbackContentException ex) {
            log.error("Fallback content provider also failed for userId={}", request.userId(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex);
        }

        // Step 4: Build response DTO
        UUID recommendationId = UUID.randomUUID();
        BigDecimal confidenceScore = BigDecimal.valueOf(0.85);
        boolean isFallback = false;

        if (generatedContent == null || generatedContent.length() > request.maxLength()) {
            isFallback = true;
        }

        RecommendationResponseDto response = RecommendationResponseDto.builder()
                .recommendationId(recommendationId)
                .userId(request.userId())
                .platform(request.platform().name())
                .content(generatedContent)
                .confidenceScore(confidenceScore)
                .isFallback(isFallback)
                .generatedAt(OffsetDateTime.now())
                .build();

        // Log with MDC
        MDC.put("correlationId", request.correlationId());
        MDC.put("userId", request.userId());
        MDC.put("platform", request.platform().name());
        MDC.put("tone", request.tone().name());
        MDC.put("isFallback", String.valueOf(isFallback));
        log.info("Recommendation generated successfully for userId={}, platform={}, isFallback={}", request.userId(), request.platform(), isFallback);

        // Record metrics
        recommendationCounter.increment();
        recommendationTimer.record(() -> {}, Collections.emptyMap());

        return response;
    }

    private String buildSystemPrompt(PromptTemplateConfig templates, RecommendationRequestDto request) {
        return String.format(templates.getSystemPromptTemplate(),
                request.tone().getPromptModifier(),
                request.platform().getDisplayName());
    }

    private String buildUserPrompt(PromptTemplateConfig templates, RecommendationRequestDto request, List<PerformanceMetricEntity> topPosts) {
        StringBuilder sb = new StringBuilder();
        sb.append(templates.getUserPromptTemplate());
        sb.append("Topic: ").append(request.topic());
        sb.append("; Tone: ").append(request.tone().getPromptModifier());
        sb.append("; Platform: ").append(request.platform().getDisplayName());
        if (!topPosts.isEmpty()) {
            sb.append("; Top performing posts:");
            for (PerformanceMetricEntity post : topPosts) {
                sb.append(" Likes: ").append(post.getLikes())
                  .append(", Comments: ").append(post.getComments())
                  .append(", Shares: ").append(post.getShares())
                  .append("; ");
            }
        }
        return sb.toString();
    }

    @ConfigurationProperties(prefix = "prompt-templates")
    private PromptTemplateConfig promptTemplates;

    public static class PromptTemplateConfig {
        private String systemPromptTemplate;
        private String userPromptTemplate;

        public String getSystemPromptTemplate() {
            return systemPromptTemplate;
        }

        public void setSystemPromptTemplate(String systemPromptTemplate) {
            this.systemPromptTemplate = systemPromptTemplate;
        }

        public String getUserPromptTemplate() {
            return userPromptTemplate;
        }

        public void setUserPromptTemplate(String userPromptTemplate) {
            this.userPromptTemplate = userPromptTemplate;
        }
    }
}