package org.nlh4j.socialscheduler.scheduleservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the schedule-service.
 * Provides centralized error handling and standardized API error responses.
 * 
 * @traceability [EXC-002], [EXC-003], [EXC-005]
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Handles validation errors for incoming request DTOs.
     * [EXC-002]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("[VALIDATION_FAILED] [EXC-002] Input validation failed: {}", ex.getMessage());
        
        List<Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        String.valueOf(error.getRejectedValue()),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại các trường được đánh dấu.",
                fieldErrors,
                OffsetDateTime.now(),
                getCorrelationId());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles expired or invalid JWT tokens.
     * [EXC-002]
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(JwtException ex) {
        log.warn("[TOKEN_EXPIRED] [EXC-002] Authentication failed: {}", ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "TOKEN_EXPIRED",
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles upstream service errors (OpenAI, Social Platforms).
     * [EXC-003]
     */
    @ExceptionHandler({SocialPlatformException.class, HttpServerErrorException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorResponse> handleUpstreamError(Exception ex) {
        log.error("[UPSTREAM_SERVICE_ERROR] [EXC-003] Upstream service failure: {}", ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "UPSTREAM_SERVICE_ERROR",
                "Dịch vụ bên thứ ba tạm thời không khả dụng. Hệ thống sẽ tự động thử lại.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles rate limit violations.
     * [EXC-005]
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("[RATE_LIMIT_EXCEEDED] [EXC-005] User {} exceeded limit on {}", ex.getUserId(), ex.getEndpoint());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        
        ErrorResponse response = new ErrorResponse(
                "RATE_LIMIT_EXCEEDED",
                "Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ cho phép. Vui lòng thử lại sau " + ex.getRetryAfterSeconds() + " giây.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Generic handler for unexpected internal server errors.
     * [OWASP A09]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("[INTERNAL_SERVER_ERROR] [EXC-000] Unexpected error: ", ex);
        
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống nội bộ. Vui lòng liên hệ quản trị viên.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER) != null ? MDC.get(CORRELATION_ID_HEADER) : "N/A";
    }

    // Inner DTOs for structured error responses
    public record FieldErrorDetail(String field, String rejectedValue, String errorMessage) {}
    public record ValidationErrorResponse(String errorCode, String message, List<Object> fieldErrors, OffsetDateTime timestamp, String correlationId) {}
    public record ErrorResponse(String errorCode, String message, OffsetDateTime timestamp, String correlationId) {}
}