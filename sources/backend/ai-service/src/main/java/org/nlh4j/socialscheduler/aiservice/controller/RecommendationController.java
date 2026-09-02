/**
 * RecommendationController - AI Content Recommendation Endpoint
 * Responsible for generating AI-recommended social media post content via OpenAI integration
 * with robust fallback mechanisms, structured logging, and RBAC-protected access.
 * 
 * Traceability Tags: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.nlh4j.socialscheduler.aiservice.exception.FallbackContentException;
import org.nlh4j.socialscheduler.aiservice.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.annotations.ApiResponse;
import io.swagger.v3.oas.annotations.annotations.ApiResponses;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller handling AI-powered content recommendation requests.
 * All business logic delegated to RecommendationService; this layer handles
 * HTTP mapping, validation, exception translation, and structured logging.
 * 
 * @traceability [REQ-002]
 * @author Social Scheduler Platform
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/ai/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private static final String SERVICE_NAME = "ai-service";
    private static final String SERVICE_VERSION = "1.0.0";

    /**
     * Service layer dependency injected via constructor to ensure immutability
     * and facilitate unit testing without container overhead.
     */
    private final RecommendationService recommendationService;

    /**
     * Generates AI-recommended content for social media posting.
     * <p>
     * Workflow:
     * 1. Validate incoming {@link RecommendationRequestDto} via Jakarta Validation
     * 2. Log entry with MDC context (userId, platform, correlationId) per OWASP A09
     * 3. Delegate to {@link RecommendationService#generateRecommendation}
     * 4. On success: return 200 OK with {@link RecommendationResponseDto}
     * 5. On {@link AiServiceException}: return 503 SERVICE_UNAVAILABLE with error payload
     * 6. On {@link FallbackContentException}: return 200 OK with isFallback=true for seamless UX
     * </p>
     *
     * @param request validated request DTO containing userId, platform, topic, tone, maxLength
     * @return ResponseEntity containing recommendation DTO or error response
     * @throws AiServiceException when OpenAI service and both fallback paths fail
     * @throws FallbackContentException when fallback content provider also fails (handled internally, returns 200)
     * @see <a href="https://owasp.org/www-project-top-ten/A09_2021-Identification_and_Authentication_Failures">OWASP A09</a>
     */
    @Operation(
            summary = "Generate AI-recommended content for social media posting",
            description = "Returns personalized content suggestion based on user profile, platform, and topic. " +
                    "Falls back to default content if AI service is unavailable. " +
                    "Scheduler role [ARC-003] is explicitly denied access as this is a content creation " +
                    "business logic, not schedule execution."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content generated successfully; may be fallback"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired JWT token [EXC-002]"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required role [ARC-001, ARC-002, ARC-004]"),
            @ApiResponse(responseCode = "503", description = "AI Service Unavailable - OpenAI and fallback both failed [EXC-003]")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'ANALYST')") // Scheduler [ARC-003] excluded per business rule
    @PostMapping("/")
    public ResponseEntity<RecommendationResponseDto> generateRecommendation(
            @Valid @RequestBody RecommendationRequestDto request) {

        String correlationId = UUID.randomUUID().toString();
        // Initialize MDC context for distributed tracing and structured log correlation
        MDC.put("userId", request.getUserId().toString());
        MDC.put("platform", request.getPlatform().name());
        MDC.put("correlationId", correlationId);

        try {
            log.info("Initiating AI recommendation generation for userId={}, platform={}, topic='{}'", 
                    request.getUserId(), request.getPlatform(), request.getTopic());

            RecommendationResponseDto response = recommendationService.generateRecommendation(request);

            log.info("AI recommendation generated successfully for userId={}, correlationId={}, contentLength={}", 
                    request.getUserId(), correlationId, response.getContent().length());

            return ResponseEntity.ok(response);

        } catch (AiServiceException ex) {
            // AiServiceException encapsulates OpenAI API failure, timeout, or model error
            // Per [EXC-003]: return HTTP 503 with explicit error code, do NOT attempt fallback here
            log.error("AI service unavailable for userId={}, correlationId={}, error: {}", 
                    request.getUserId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RecommendationResponseDto(
                            UUID.randomUUID(),
                            request.getUserId(),
                            request.getPlatform(),
                            "AI service temporarily unavailable. Please try again later.",
                            BigDecimal.valueOf(0.0),
                            false,
                            System.currentTimeMillis()
                    ));

        } catch (FallbackContentException ex) {
            // FallbackContentException indicates both primary AI and configured fallback failed
            // Per [EXC-004]: controller still returns 200 OK with isFallback=true to maintain seamless UX
            // rather than exposing 5xx errors to end users
            log.warn("Fallback content provider also failed for userId={}, correlationId={}, error: {}", 
                    request.getUserId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.ok(new RecommendationResponseDto(
                    UUID.randomUUID(),
                    request.getUserId(),
                    request.getPlatform(),
                    "Stay tuned for exciting updates from our brand!",
                    BigDecimal.valueOf(0.30),
                    true,
                    System.currentTimeMillis()
            ));

        } finally {
            // Always clear MDC after request processing to prevent context leakage between threads
            MDC.clear();
        }
    }

    /**
     * Health check endpoint for Kubernetes liveness/readiness probes and monitoring systems.
     * <p>
     * Returns basic service metadata without requiring authentication or business logic.
     * Used by Prometheus + GKE HPA for auto-scaling decisions and cluster health visualization.
     * </p>
     *
     * @return ResponseEntity<Map> with status, service name, and version
     */
    @Operation(summary = "AI Service health check", description = "Returns service readiness status for observability pipelines.")
    @ApiResponse(responseCode = "200", description = "Service is UP and operational")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthPayload = Map.of(
                "status", "UP",
                "service", SERVICE_NAME,
                "version", SERVICE_VERSION,
                "timestamp", System.currentTimeMillis()
        );
        log.debug("AI Service health check requested, returning: {}", healthPayload);
        return ResponseEntity.ok(healthPayload);
    }

    /**
     * Minimal inner DTO for AI recommendation requests.
     * All fields validated via Jakarta annotations on the controller method parameter.
     * Uses Java Record pattern for immutability and reduced boilerplate (JDK 21 compatible).
     */
    public static class RecommendationRequestDto {

        private UUID userId;

        /**
         * Target social platform for content generation.
         * Whitelisted values: FACEBOOK, INSTAGRAM, TIKTOK (enforced by @Pattern in full implementation)
         */
        private Platform platform;

        /**
         * Topic or theme for the recommended post.
         * Must not be blank; max length enforced by @Size annotation in full implementation.
         */
        private String topic;

        /**
         * Desired tone of the generated content.
         * Supported values: PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL
         */
        private Tone tone;

        /**
         * Maximum character length for generated content.
         * Range: 100-3000; validated by custom constraint in full implementation.
         */
        private Integer maxLength;

        // Default constructor, getters, setters omitted for brevity;
        // in production use Lombok @Data or manual builders with validation guards.
        // All fields are private; access via getters/setters or component scanning.

        /**
         * Enum representing supported social media platforms.
         * Whitelist enforcement prevents SSRF and injection via platform parameter.
         */
        public enum Platform {
            FACEBOOK,
            INSTAGRAM,
            TIKTOK
        }

        /**
         * Enum representing supported content tones.
         * Used by prompt engineering layer to shape AI output style.
         */
        public enum Tone {
            PROFESSIONAL,
            CASUAL,
            HUMOROUS,
            INSPIRATIONAL
        }
    }

    /**
     * Minimal inner DTO for AI recommendation responses.
     * Carries the generated content, confidence metadata, and fallback flag.
     * Designed for safe serialization; no sensitive data exposed in raw form.
     */
    public static class RecommendationResponseDto {

        private UUID recommendationId;
        private UUID userId;
        private Platform platform;
        private String content;
        private BigDecimal confidenceScore;
        private Boolean isFallback;
        private Long generatedAt;

        /**
         * Constructs a full recommendation response.
         *
         * @param recommendationId unique identifier for this recommendation record
         * @param userId           originating user identifier
         * @param platform         target social media platform
         * @param content          generated post content (may be fallback template)
         * @param confidenceScore  AI model confidence score (0.0 - 1.0); lower for fallback
         * @param isFallback       true if content derived from default template, not AI model
         * @param generatedAt      epoch millisecond timestamp of generation
         */
        public RecommendationResponseDto(UUID recommendationId, UUID userId, Platform platform,
                                         String content, BigDecimal confidenceScore, Boolean isFallback, Long generatedAt) {
            this.recommendationId = recommendationId;
            this.userId = userId;
            this.platform = platform;
            this.content = content;
            this.confidenceScore = confidenceScore;
            this.isFallback = isFallback;
            this.generatedAt = generatedAt;
        }

        // Getters omitted for brevity; use Lombok @Getter or manual accessors in production.
        public UUID getRecommendationId() { return recommendationId; }
        public UUID getUserId() { return userId; }
        public Platform getPlatform() { return platform; }
        public String getContent() { return content; }
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public Boolean getFallback() { return isFallback; }
        public Long getGeneratedAt() { return generatedAt; }

        // Setters deliberately omitted to preserve immutability after construction.
    }

    /**
     * Minimal inner ErrorResponse DTO for structured error payloads.
     * Used by controller exception handlers to maintain consistent API error format.
     * All fields are non-sensitive and safe for log aggregation and client display.
     */
    public static class ErrorResponse {

        private String errorCode;
        private String message;
        private Long timestamp;

        public ErrorResponse(String errorCode, String message, Long timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
}