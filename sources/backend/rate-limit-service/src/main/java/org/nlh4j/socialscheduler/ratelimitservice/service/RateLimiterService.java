package org.nlh4j.socialscheduler.ratelimitservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResult;
import org.nlh4j.socialscheduler.ratelimitservice.strategy.RedisTokenBucketStrategy;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for managing API rate limiting using Redis Token Bucket strategy.
 * 
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RateLimiterService {

    private final RedisTokenBucketStrategy tokenBucketStrategy;
    private final Timer checkDurationTimer;
    private final Counter resetCounter;

    /**
     * Constructor injection for required dependencies and Micrometer metrics.
     */
    public RateLimiterService(RedisTokenBucketStrategy tokenBucketStrategy, MeterRegistry meterRegistry) {
        this.tokenBucketStrategy = tokenBucketStrategy;
        this.checkDurationTimer = meterRegistry.timer("rate_limit.check.duration");
        this.resetCounter = meterRegistry.counter("rate_limit.reset.total");
    }

    /**
     * Checks if the request is allowed based on the rate limit strategy.
     * Uses Caffeine cache to optimize performance for high-frequency checks.
     * 
     * @param userId   The unique identifier of the user.
     * @param endpoint The API endpoint being accessed.
     * @return RateLimitResult containing status and retry information.
     */
    @Cacheable(cacheNames = "rateLimitCache", key = "#userId + ':' + #endpoint", sync = true)
    public RateLimitResult checkRateLimit(UUID userId, String endpoint) {
        // Start timer for performance monitoring
        return checkDurationTimer.record(() -> {
            // Setup MDC context for structured logging
            MDC.put("userId", userId.toString());
            MDC.put("endpoint", endpoint);

            try {
                // Execute strategy to consume token
                RateLimitResult result = tokenBucketStrategy.tryConsume(userId.toString(), endpoint, 1);

                if (result.isAllowed()) {
                    log.info("[PROCESS] Rate limit check passed for user: {}", userId);
                } else {
                    // [EXC-005] Log warning when rate limit is exceeded
                    log.warn("[CRITICAL FAIL] [EXC-005] Rate limit exceeded for user: {} on endpoint: {}", userId, endpoint);
                }

                return result;
            } finally {
                MDC.remove("userId");
                MDC.remove("endpoint");
            }
        });
    }

    /**
     * Resets the rate limit for a specific user and endpoint.
     * Restricted to ADMIN role only.
     * 
     * @param userId   The unique identifier of the user.
     * @param endpoint The API endpoint to reset.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "rateLimitCache", key = "#userId + ':' + #endpoint")
    public void resetLimit(UUID userId, String endpoint) {
        log.info("[ADMIN] Resetting rate limit for user: {} on endpoint: {}", userId, endpoint);
        
        // Execute reset strategy
        tokenBucketStrategy.reset(userId.toString(), endpoint);
        
        // Increment reset counter metric
        resetCounter.increment();
    }
}