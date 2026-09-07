/**
 * RecommendationController - AI Content Recommendation Endpoint
 * Responsible for generating AI-recommended social media post content via OpenAI integration
 * with robust fallback mechanisms, structured logging, and RBAC-protected access.
 * 
 * Traceability Tags: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.service.RecommendationService;
import org.nlh4j.socialscheduler.exception.AiServiceException;
import org.nlh4j.socialscheduler.exception.FallbackContentException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        MDC.put("userId", request.userId().toString());
        MDC.put("platform", request.platform().name());
        MDC.put("correlationId", correlationId);

        try {
            log.info("Initiating AI recommendation generation for userId={}, platform={}, topic='{}'", 
                    request.userId(), request.platform(), request.topic());

            RecommendationResponseDto response = recommendationService.generateRecommendation(request);

            log.info("AI recommendation generated successfully for userId={}, correlationId={}, contentLength={}", 
                    request.userId(), correlationId, response.getContent().length());

            return ResponseEntity.ok(response);

        } catch (AiServiceException ex) {
            // AiServiceException encapsulates OpenAI API failure, timeout, or model error
            // Per [EXC-003]: return HTTP 503 with explicit error code, do NOT attempt fallback here
            log.error("AI service unavailable for userId={}, correlationId={}, error: {}", 
                    request.userId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RecommendationResponseDto(
                            UUID.randomUUID(),
                            request.userId(),
                            request.platform(),
                            "AI service temporarily unavailable. Please try again later.",
                            BigDecimal.valueOf(0.0),
                            false,
                            LocalDateTime.now().atOffset(ZoneOffset.UTC)
                    ));

        } catch (FallbackContentException ex) {
            // FallbackContentException indicates both primary AI and configured fallback failed
            // Per [EXC-004]: controller still returns 200 OK with isFallback=true to maintain seamless UX
            // rather than exposing 5xx errors to end users
            log.warn("Fallback content provider also failed for userId={}, correlationId={}, error: {}", 
                    request.userId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.ok(new RecommendationResponseDto(
                    UUID.randomUUID(),
                    request.userId(),
                    request.platform(),
                    "Stay tuned for exciting updates from our brand!",
                    BigDecimal.valueOf(0.30),
                    true,
                    LocalDateTime.now().atOffset(ZoneOffset.UTC)
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
}