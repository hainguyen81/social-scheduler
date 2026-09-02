package org.nlh4j.socialscheduler.ratelimitservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitCheckRequestDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitCheckResponseDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResetRequestDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResetResponseDto;
import org.nlh4j.socialscheduler.ratelimitservice.service.RateLimiterService;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsible for managing and checking API rate limits.
 * 
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rate-limits")
@RequiredArgsConstructor
@Tag(name = "Rate Limit Management", description = "Endpoints for checking and resetting API rate limits")
public class RateLimitController {

    private final RateLimiterService rateLimiterService;

    private static final String MDC_USER_ID = "userId";
    private static final String MDC_ENDPOINT = "endpoint";

    /**
     * Checks if the current request is within the allowed rate limit threshold.
     * 
     * @param request The request containing userId and endpoint to check.
     * @return ResponseEntity containing remaining tokens and retry information.
     * @traceability [REQ-003]
     */
    @Operation(summary = "Check rate limit", description = "Verifies if the user has remaining tokens for the specified endpoint.")
    @ApiResponse(responseCode = "200", description = "Request within limits")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @PostMapping("/check")
    public ResponseEntity<RateLimitCheckResponseDto> checkRateLimit(@Valid @RequestBody RateLimitCheckRequestDto request) {
        // Injecting context into MDC for structured logging (OWASP A09)
        MDC.put(MDC_USER_ID, request.userId().toString());
        MDC.put(MDC_ENDPOINT, request.endpoint());
        
        log.info("Checking rate limit for user: {} on endpoint: {}", request.userId(), request.endpoint());
        
        try {
            RateLimitCheckResponseDto response = rateLimiterService.checkRateLimit(request.userId(), request.endpoint());
            return ResponseEntity.ok(response);
        } finally {
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_ENDPOINT);
        }
    }

    /**
     * Resets the rate limit for a specific user and endpoint.
     * Restricted to ADMIN role.
     * 
     * @param request The request containing userId and endpoint to reset.
     * @return ResponseEntity indicating success.
     * @traceability [EXC-005]
     */
    @Operation(summary = "Reset rate limit", description = "Admin-only endpoint to reset rate limits for a user.")
    @ApiResponse(responseCode = "200", description = "Rate limit reset successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset")
    public ResponseEntity<RateLimitResetResponseDto> resetLimit(@Valid @RequestBody RateLimitResetRequestDto request) {
        MDC.put(MDC_USER_ID, request.userId().toString());
        MDC.put(MDC_ENDPOINT, request.endpoint());
        
        log.warn("Admin resetting rate limit for user: {} on endpoint: {}", request.userId(), request.endpoint());
        
        rateLimiterService.resetLimit(request.userId(), request.endpoint());
        
        RateLimitResetResponseDto response = new RateLimitResetResponseDto(
            true, 
            "Giới hạn tỷ lệ đã được đặt lại thành công."
        );
        
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_ENDPOINT);
        
        return ResponseEntity.ok(response);
    }
}