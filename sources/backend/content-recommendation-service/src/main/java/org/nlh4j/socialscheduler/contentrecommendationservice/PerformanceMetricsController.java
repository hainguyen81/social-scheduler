/**
 * PerformanceMetricsController provides REST endpoints for AI-driven content recommendation.
 * Traceability Tags: [REQ-002]
 */
package org.nlh4j.socialscheduler.contentrecommendationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;

/**
 * Controller layer for handling content recommendation requests.
 * Traceability Tags: [REQ-002]
 */
@Slf4j
@Controller
@RequestMapping("/api/v1/content-recommendations")
public class PerformanceMetricsController {

    // Enterprise constants for messages, error codes, and header names
    private static final String LOG_PREFIX = "PerformanceMetricsController";
    private static final String ERR_RECOMMENDATION_FAILED = "Failed to generate content recommendation";
    private static final String HEADER_IDEMPOTENCY = "Idempotency-Key";
    private static final String ERR_CODE_REC_SVC = "REC-001";

    private final PerformanceMetricsService performanceMetricsService;

    /**
     * Constructor-based dependency injection for service layer.
     * Traceability Tags: [REQ-002]
     */
    public PerformanceMetricsController(PerformanceMetricsService performanceMetricsService) {
        this.performanceMetricsService = performanceMetricsService;
        log.info("[INIT] {} loaded successfully.", LOG_PREFIX);
    }

    /**
     * Endpoint to generate content recommendation based on previous performance metrics.
     * Validates idempotency key, request payload, and delegates to service.
     * Traceability Tags: [REQ-002]
     */
    @PostMapping
    public ResponseEntity<ContentRecommendationResponse> generateRecommendation(
            @RequestHeader(value = HEADER_IDEMPOTENCY, required = true) String idempotencyKey,
            @Valid @RequestBody PerformanceMetricsRequest request,
            BindingResult bindingResult,
            jakarta.servlet.http.HttpServletRequest servletRequest) {

        // Entry logging for request trace
        log.info("[ENTRY] {} processing recommendation request for userId: {}, idempotencyKey: {}",
                LOG_PREFIX, request.getUserId(), idempotencyKey);

        // Validate request payload to prevent malformed input
        if (bindingResult.hasErrors()) {
            String validationError = bindingResult.getAllErrors().stream()
                    .map(Object::toString).reduce("", String::concat);
            log.warn("[VALIDATION_FAIL] [REQ-002] Invalid request payload: {}", validationError);
            throw new IllegalArgumentException("Invalid request: " + validationError);
        }

        try {
            // Delegate recommendation logic to service layer (business operation)
            ContentRecommendationResponse response = performanceMetricsService.recommendContent(request);

            // Exit logging for successful processing
            log.info("[EXIT] {} recommendation generated for userId: {}", LOG_PREFIX, request.getUserId());

            // Return response with idempotency header for client tracking
            return ResponseEntity.ok()
                    .header(HEADER_IDEMPOTENCY, idempotencyKey)
                    .body(response);

        } catch (DataAccessException e) {
            // Preserve original cause and log detailed error with traceability tag
            String errorMsg = ERR_RECOMMENDATION_FAILED + " - Data access error";
            log.error("[CRITICAL_FAIL] [REQ-002] {} for userId: {}. Raw error: {}",
                    errorMsg, request.getUserId(), e.getMessage(), e);
            throw new ContentRecommendationException(ERR_CODE_REC_SVC, errorMsg, e);
        } catch (Exception e) {
            // Catch-all for unexpected runtime errors
            String errorMsg = ERR_RECOMMENDATION_FAILED;
            log.error("[CRITICAL_FAIL] [REQ-002] {} for userId: {}. Raw error: {}",
                    errorMsg, request.getUserId(), e.getMessage(), e);
            throw new ContentRecommendationException(ERR_CODE_REC_SVC, errorMsg, e);
        }
    }

    /**
     * Global exception handler for validation failures.
     * Traceability Tags: [REQ-002]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Request payload validation failed",
                Instant.now()
        );
        log.error("[VALIDATION_FAIL] [REQ-002] Validation error during recommendation request: {}",
                ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Custom runtime exception for recommendation service failures.
     * Traceability Tags: [REQ-002]
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class ContentRecommendationException extends RuntimeException {
        private final String errorCode;

        public ContentRecommendationException(String errorCode, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    // -------------------------------------------------
    // Data Transfer Objects (DTOs) for request/response
    // -------------------------------------------------

    public static class PerformanceMetricsRequest {
        private UUID userId;
        private String platform;
        // Additional fields such as date range, content type, etc. can be added here.

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }

    public static class ContentRecommendationResponse {
        private UUID recommendationId;
        private String suggestedContent;
        private Instant generatedAt;

        public UUID getRecommendationId() {
            return recommendationId;
        }

        public void setRecommendationId(UUID recommendationId) {
            this.recommendationId = recommendationId;
        }

        public String getSuggestedContent() {
            return suggestedContent;
        }

        public void setSuggestedContent(String suggestedContent) {
            this.suggestedContent = suggestedContent;
        }

        public Instant getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
        }
    }

    public static class ErrorResponse {
        private String code;
        private String message;
        private Instant timestamp;

        public ErrorResponse(String code, String message, Instant timestamp) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}