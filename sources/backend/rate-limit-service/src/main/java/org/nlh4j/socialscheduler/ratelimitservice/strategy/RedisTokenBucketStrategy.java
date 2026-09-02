package org.nlh4j.socialscheduler.ratelimitservice.strategy;

// [REQ-003] Rate limiting enforcement with Redis Token Bucket strategy
// [EXC-005] Exception handling and retry-after calculation for rate limit exceedance

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Enterprise Token Bucket Rate Limiting Strategy implemented using Redis and Lua scripting.
 * Guarantees atomic evaluations of request rates across distributed cluster nodes.
 *
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@Component
public class RedisTokenBucketStrategy {

    // Top-of-class immutable constants and configuration handles [0.2]
    private static final String LUA_SCRIPT_PATH = "scripts/token-bucket.lua";
    private static final String METRIC_CONSUMED_TOTAL = "rate_limit.tokens.consumed.total";
    private static final String METRIC_EXCEEDED_TOTAL = "rate_limit.exceeded.total";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_ENDPOINT = "endpoint";
    private static final String MDC_TOKENS_REQUESTED = "tokensRequested";
    private static final String MDC_TOKENS_REMAINING = "tokensRemaining";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> redisScript;
    private final MeterRegistry meterRegistry;

    // Configurable token bucket parameters injected from application properties
    @Value("${rate-limit.token-bucket.capacity:100}")
    private long capacity;

    @Value("${rate-limit.token-bucket.refill-rate-per-minute:60}")
    private long refillRatePerMinute;

    @Value("${rate-limit.token-bucket.key-prefix:rate_limit:}")
    private long keyPrefix; // Stored as string prefix via formatted keys

    private Counter tokensConsumedCounter;
    private Counter rateLimitExceededCounter;

    /**
     * Constructor-based dependency injection for Redis string template and Micrometer metrics.
     *
     * @param redisTemplate Redis template for executing atomic script evaluations
     * @param meterRegistry Micrometer metrics registry for operational telemetry
     */
    public RedisTokenBucketStrategy(StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        
        // Initialize the atomic Redis Lua script template
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_SCRIPT_PATH)));
        this.redisScript.setResultType(List.class);
    }

    /**
     * Post-construct initialization hook to register custom metrics securely.
     */
    @PostConstruct
    public void initMetrics() {
        this.tokensConsumedCounter = Counter.builder(METRIC_CONSUMED_TOTAL)
                .description("Total number of successfully consumed rate-limit tokens")
                .register(meterRegistry);
        this.rateLimitExceededCounter = Counter.builder(METRIC_EXCEEDED_TOTAL)
                .description("Total number of rate limit exceedance exceptions triggered")
                .register(meterRegistry);
    }

    /**
     * Attempts to consume specified tokens for a given user and endpoint transaction.
     * Evaluates atomic Lua script against Redis cluster.
     *
     * @param userId   Target unique user identifier
     * @param endpoint Target protected API endpoint
     * @param tokens   Number of tokens requested for this operation
     * @return RateLimitResult containing consumption status, remaining tokens, and retry window
     * @throws RateLimitExceededException if allowed evaluates to false
     */
    public RateLimitResult tryConsume(String userId, String endpoint, int tokens) {
        // Populate MDC logging context with tracing attributes for OWASP A09 logging compliance
        MDC.put(MDC_USER_ID, userId != null ? userId : "ANONYMOUS");
        MDC.put(MDC_ENDPOINT, endpoint != null ? endpoint : "UNKNOWN");
        MDC.put(MDC_TOKENS_REQUESTED, String.valueOf(tokens));

        try {
            log.info("[PROCESS] [REQ-003] Evaluating Redis Token Bucket rate limit for user: {} on endpoint: {}", userId, endpoint);

            // Construct Redis key following the strict prefix convention: rate_limit:{userId}:{endpoint}
            String redisKey = String.format("rate_limit:%s:%s", userId, endpoint);
            List<String> keys = Collections.singletonList(redisKey);

            // Prepare arguments for Lua script execution: [capacity, refillRatePerMinute, requestedTokens, currentTimeMillis]
            long currentTimeMillis = System.currentTimeMillis();
            Object[] args = new Object[] {
                    String.valueOf(capacity),
                    String.valueOf(refillRatePerMinute),
                    String.valueOf(tokens),
                    String.valueOf(currentTimeMillis)
            };

            // Execute Lua script atomically on Redis cluster
            List<Long> result = redisTemplate.execute(redisScript, keys, args);

            if (result == null || result.size() < 3) {
                log.error("[CRITICAL FAIL] [EXC-005] Redis Lua script returned malformed execution payload for user: {}", userId);
                throw new IllegalStateException("Rate limiter evaluation failed due to malformed Redis response.");
            }

            boolean allowed = result.get(0) == 1L;
            long remainingTokens = result.get(1);
            long retryAfterSeconds = result.get(2);

            MDC.put(MDC_TOKENS_REMAINING, String.valueOf(remainingTokens));

            if (allowed) {
                // Increment successful consumption metric counter
                tokensConsumedCounter.increment(tokens);
                log.debug("[SUCCESS] [REQ-003] Rate limit passed. Remaining tokens: {} for user: {}", remainingTokens, userId);
                return new RateLimitResult(true, remainingTokens, 0L);
            } else {
                // Increment rate limit exceeded metric counter
                rateLimitExceededCounter.increment();
                log.warn("[RATE_LIMIT_EXCEEDED] [EXC-005] Rate limit exceeded for user: {} on endpoint: {}. Retry after: {}s", 
                        userId, endpoint, retryAfterSeconds);
                
                // Throw explicit enterprise business exception mapping to EXC-005 requirement
                throw new RateLimitExceededException(
                        userId,
                        endpoint,
                        retryAfterSeconds,
                        String.format("Rate limit exceeded. Please retry after %d seconds.", retryAfterSeconds)
                );
            }

        } catch (RateLimitExceededException e) {
            // Re-throw business exception directly without swallowing cause chain
            throw e;
        } catch (Exception e) {
            // Defensive fault-tolerance: Log unexpected infrastructure failures and fail-open or fail-secure
            log.error("[CRITICAL FAIL] [EXC-005] Redis Token Bucket evaluation failed due to network or connection drop. Raw error: {}", e.getMessage(), e);
            // In high-availability architectures, we wrap and propagate as a runtime exception
            throw new RuntimeException("Rate limiter backend unavailable", e);
        } finally {
            // Clean up MDC context to prevent thread-pool memory leaks
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_ENDPOINT);
            MDC.remove(MDC_TOKENS_REQUESTED);
            MDC.remove(MDC_TOKENS_REMAINING);
        }
    }

    /**
     * Immutable value carrier representing the outcome of a token bucket consumption evaluation.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingTokens;
        private final long retryAfterSeconds;

        public RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getRemainingTokens() {
            return remainingTokens;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}