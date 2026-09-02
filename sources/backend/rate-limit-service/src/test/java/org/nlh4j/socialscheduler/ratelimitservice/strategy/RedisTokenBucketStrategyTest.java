package org.nlh4j.socialscheduler.ratelimitservice.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test suite for {@link RedisTokenBucketStrategy} using Testcontainers Redis.
 * Enforces high-fidelity verification of distributed token bucket rate limiting semantics,
 * Lua script atomicity under high concurrency, multi-endpoint bucket isolation, and error contracts.
 *
 * @author Enterprise Quality Assurance Engineering (QA Agent)
 * @version 1.0.0
 * @verifies [REQ-003], [EXC-005]
 */
@Tag("integration")
@Testcontainers
@SpringBootTest
public class RedisTokenBucketStrategyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTokenBucketStrategyTest.class);

    // [REQ-003] Top-of-Class Constants Declaration Law
    private static final String REDIS_IMAGE_NAME = "redis:7.2-alpine";
    private static final String SPRING_DATA_REDIS_HOST_PROPERTY = "spring.data.redis.host";
    private static final String SPRING_DATA_REDIS_PORT_PROPERTY = "spring.data.redis.port";
    private static final String TEST_ENDPOINT_SCHEDULES = "/api/v1/schedules";
    private static final String TEST_ENDPOINT_ANALYTICS = "/api/v1/analytics";
    private static final long DEFAULT_BUCKET_CAPACITY = 100L;
    private static final long SINGLE_TOKEN_CONSUMPTION = 1L;
    private static final long MULTI_TOKEN_CONSUMPTION_FIVE = 5L;
    private static final long ZERO_REMAINING_TOKENS = 0L;
    private static final int CONCURRENT_WORKER_THREADS = 100;
    private static final long CONCURRENT_TIMEOUT_SECONDS = 30L;
    private static final long REFILL_SLEEP_MILLIS = 2000L;
    private static final long EXPECTED_REFILLED_TOKENS_APPROX = 2L;

    // [REQ-003] Live Redis Testcontainer definition for real Lua execution
    @Container
    public static final RedisContainer REDIS_CONTAINER = new RedisContainer(
            DockerImageName.parse(REDIS_IMAGE_NAME)
    ).withExposedPorts(6379);

    @DynamicPropertySource
    public static void registerRedisProperties(DynamicPropertyRegistry registry) {
        // [REQ-003] Dynamic property routing to containerized Redis instance
        registry.add(SPRING_DATA_REDIS_HOST_PROPERTY, REDIS_CONTAINER::getHost);
        registry.add(SPRING_DATA_REDIS_PORT_PROPERTY, () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    @Autowired
    private RedisTokenBucketStrategy redisTokenBucketStrategy;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    public void setUpDatabaseState() {
        LOGGER.info("[TEST_SETUP] [REQ-003] Executing flushall on Redis container before test case execution");
        // Ensure isolation by clearing all Redis keys prior to each test case execution
        try {
            RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
            connection.serverCommands().flushAll();
            connection.close();
        } catch (Exception ex) {
            LOGGER.error("[SETUP_FAIL] [REQ-003] Failed to flush test Redis cache instance. Error: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to purge Redis state before test execution", ex);
        }
    }

    /**
     * Verifies that consuming a single token from a brand-new, full bucket succeeds,
     * decrements the remaining token count to 99, and returns true for isAllowed.
     *
     * @verifies [REQ-003]
     */
    @Test
    @DisplayName("[REQ-003] tryConsume: when bucket is full, consume 1 token, return allowed true and 99 remaining tokens")
    public void tryConsume_whenBucketIsFull_thenReturnAllowedTrue() {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [REQ-003] Verifying initial single token consumption for user: {}", userId);

        // Act: Consume single token from newly established capacity of 100
        TokenBucketResult result = redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                SINGLE_TOKEN_CONSUMPTION
        );

        // Assert: Validating contract properties
        assertThat(result)
                .as("Result envelope must never be null")
                .isNotNull();
        assertThat(result.isAllowed())
                .as("Consuming 1 token from 100 capacity bucket must be permitted")
                .isTrue();
        assertThat(result.getRemainingTokens())
                .as("Consuming 1 token from 100 capacity bucket must leave exactly 99 tokens")
                .isEqualTo(DEFAULT_BUCKET_CAPACITY - SINGLE_TOKEN_CONSUMPTION);
        assertThat(result.getRetryAfterSeconds())
                .as("Allowed request must have 0 retry after seconds")
                .isZero();

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Initial token consumption validated successfully");
    }

    /**
     * Verifies that consuming all 100 tokens sequentially exhausts the bucket, and the 101st
     * attempt explicitly throws {@link RateLimitExceededException} with retry-after metadata.
     *
     * @verifies [REQ-003], [EXC-005]
     */
    @Test
    @DisplayName("[EXC-005] tryConsume: when bucket is completely empty, throw RateLimitExceededException")
    public void tryConsume_whenBucketIsEmpty_thenThrowRateLimitExceededException() {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [EXC-005] Simulating bucket exhaustion for user: {}", userId);

        // Act: Drain the entire 100-token capacity sequentially
        for (long i = 1; i <= DEFAULT_BUCKET_CAPACITY; i++) {
            TokenBucketResult stepResult = redisTokenBucketStrategy.tryConsume(
                    userId,
                    TEST_ENDPOINT_SCHEDULES,
                    SINGLE_TOKEN_CONSUMPTION
            );
            assertThat(stepResult.isAllowed())
                    .as("Request %d within capacity must be allowed", i)
                    .isTrue();
        }

        LOGGER.info("[TEST_PROCESS] [EXC-005] Capacity exhausted, invoking 101st request to trigger boundary failure");

        // Assert: 101st request must violate rate boundary and raise RateLimitExceededException
        assertThatThrownBy(() -> redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                SINGLE_TOKEN_CONSUMPTION
        ))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(throwable -> {
                    RateLimitExceededException ex = (RateLimitExceededException) throwable;
                    LOGGER.info("[EXCEPTION_AUDIT] [EXC-005] Intercepted expected RateLimitExceededException: {}", ex.getMessage());
                    assertThat(ex.getUserId())
                            .as("Exception must preserve the rejected userId")
                            .isEqualTo(userId);
                    assertThat(ex.getEndpoint())
                            .as("Exception must preserve the violated endpoint")
                            .isEqualTo(TEST_ENDPOINT_SCHEDULES);
                    assertThat(ex.getRetryAfterSeconds())
                            .as("Exception must return positive retry-after duration")
                            .isGreaterThan(0);
                });

        LOGGER.info("[TEST_COMPLETE] [EXC-005] RateLimitExceededException verification passed");
    }

    /**
     * Verifies that after exhausting the bucket, waiting for a configured delay interval
     * replenishes tokens dynamically according to the token refill rate (1 token/sec).
     *
     * @verifies [REQ-003]
     */
    @Test
    @DisplayName("[REQ-003] tryConsume: when tokens are refilled after delay, then bucket capacity is restored")
    public void tryConsume_whenTokensRefilledAfterDelay_thenBucketRestored() throws InterruptedException {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [REQ-003] Testing time-based refill dynamics for user: {}", userId);

        // Drain the entire bucket
        for (int i = 0; i < DEFAULT_BUCKET_CAPACITY; i++) {
            redisTokenBucketStrategy.tryConsume(userId, TEST_ENDPOINT_SCHEDULES, SINGLE_TOKEN_CONSUMPTION);
        }

        // Sleep to permit token refill (rate is 60 tokens / minute = 1 token/sec)
        LOGGER.info("[TEST_PROCESS] [REQ-003] Sleeping for {}ms to allow refill replenishment", REFILL_SLEEP_MILLIS);
        Thread.sleep(REFILL_SLEEP_MILLIS);

        // Act: Attempt to consume 1 refilled token
        TokenBucketResult refilledResult = redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                SINGLE_TOKEN_CONSUMPTION
        );

        // Assert: Token must be replenished and request granted
        assertThat(refilledResult.isAllowed())
                .as("Request after sleep interval must be allowed due to token refill")
                .isTrue();
        assertThat(refilledResult.getRemainingTokens())
                .as("Remaining tokens after 2s refill and 1 token consumption must be at least 1")
                .isGreaterThanOrEqualTo(EXPECTED_REFILLED_TOKENS_APPROX - SINGLE_TOKEN_CONSUMPTION);

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Token refill verification passed with remaining tokens: {}",
                refilledResult.getRemainingTokens());
    }

    /**
     * Verifies strict concurrency and atomicity of Redis Token Bucket operations.
     * Launches 100 concurrent threads simultaneously requesting tokens against a capacity-100 bucket.
     * The sum of successful requests must never exceed 100, preventing race condition token over-drafting.
     *
     * @verifies [REQ-003]
     */
    @Test
    @DisplayName("[REQ-003] tryConsume: concurrent requests must preserve Lua script atomicity without token leakage")
    public void tryConsume_concurrentRequests_thenAtomicityPreserved() throws InterruptedException, ExecutionException {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [REQ-003] Launching {} concurrent threads for user: {}", CONCURRENT_WORKER_THREADS, userId);

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_WORKER_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(CONCURRENT_WORKER_THREADS);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_WORKER_THREADS; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    // Block until all threads are prepped to simulate true burst millisecond concurrency
                    startLatch.await();
                    TokenBucketResult result = redisTokenBucketStrategy.tryConsume(
                            userId,
                            TEST_ENDPOINT_SCHEDULES,
                            SINGLE_TOKEN_CONSUMPTION
                    );
                    if (result.isAllowed()) {
                        allowedCount.incrementAndGet();
                    }
                } catch (RateLimitExceededException ex) {
                    // Expected when bucket empties under load
                    rejectedCount.incrementAndGet();
                } catch (Exception ex) {
                    LOGGER.error("[CONCURRENCY_ERROR] [REQ-003] Unexpected thread interruption: {}", ex.getMessage(), ex);
                } finally {
                    finishLatch.countDown();
                }
            }));
        }

        // Release the barrier
        startLatch.countDown();

        // Await thread completion
        boolean completedNormally = finishLatch.await(CONCURRENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(completedNormally)
                .as("All concurrent worker threads must complete within timeout window")
                .isTrue();

        executorService.shutdown();

        LOGGER.info("[TEST_PROCESS] [REQ-003] Concurrent execution finished. Allowed: {}, Rejected: {}",
                allowedCount.get(), rejectedCount.get());

        // Assert: In an initial 100-token capacity bucket, exactly 100 requests must be allowed
        assertThat(allowedCount.get())
                .as("Exact token allowance under concurrency must strictly equal capacity 100")
                .isEqualTo((int) DEFAULT_BUCKET_CAPACITY);
        assertThat(rejectedCount.get())
                .as("Zero requests must be rejected when exactly 100 requests hit capacity 100")
                .isZero();

        // One additional request right after must be rejected
        assertThatThrownBy(() -> redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                SINGLE_TOKEN_CONSUMPTION
        ))
                .as("Immediate follow-up request after 100 concurrent consumptions must be rejected")
                .isInstanceOf(RateLimitExceededException.class);

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Concurrency atomicity validated successfully");
    }

    /**
     * Verifies that rate limiting buckets are fully isolated per endpoint for the same user.
     * Draining the bucket for one endpoint must not affect the remaining tokens of a distinct endpoint.
     *
     * @verifies [REQ-003]
     */
    @Test
    @DisplayName("[REQ-003] tryConsume: different endpoints maintain completely isolated bucket states")
    public void tryConsume_differentEndpoints_thenIndependentBuckets() {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [REQ-003] Validating bucket isolation between {} and {}",
                TEST_ENDPOINT_SCHEDULES, TEST_ENDPOINT_ANALYTICS);

        // Exhaust tokens exclusively for the schedules endpoint
        for (int i = 0; i < DEFAULT_BUCKET_CAPACITY; i++) {
            redisTokenBucketStrategy.tryConsume(userId, TEST_ENDPOINT_SCHEDULES, SINGLE_TOKEN_CONSUMPTION);
        }

        // Schedules endpoint must now be empty
        assertThatThrownBy(() -> redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                SINGLE_TOKEN_CONSUMPTION
        ))
                .isInstanceOf(RateLimitExceededException.class);

        // Act: Execute request against the analytics endpoint for the exact same user
        TokenBucketResult analyticsResult = redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_ANALYTICS,
                SINGLE_TOKEN_CONSUMPTION
        );

        // Assert: Analytics bucket must remain untouched and full (100 - 1 = 99)
        assertThat(analyticsResult.isAllowed())
                .as("Consumption on distinct endpoint must be granted regardless of schedules exhaustion")
                .isTrue();
        assertThat(analyticsResult.getRemainingTokens())
                .as("Analytics bucket must reflect fresh balance after 1 consumption")
                .isEqualTo(DEFAULT_BUCKET_CAPACITY - SINGLE_TOKEN_CONSUMPTION);

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Multi-endpoint bucket isolation confirmed");
    }

    /**
     * Verifies that consuming multiple tokens at once (batch token subtraction)
     * decrements the bucket balance correctly according to the requested amount.
     *
     * @verifies [REQ-003]
     */
    @Test
    @DisplayName("[REQ-003] tryConsume: consuming multiple tokens at once subtracts balance correctly")
    public void tryConsume_multipleTokensAtOnce_thenSubtractCorrectly() {
        final String userId = UUID.randomUUID().toString();
        LOGGER.info("[TEST_START] [REQ-003] Testing multi-token consumption of {} units for user: {}",
                MULTI_TOKEN_CONSUMPTION_FIVE, userId);

        // Act: Consume 5 tokens in a single atomic invocation
        TokenBucketResult result = redisTokenBucketStrategy.tryConsume(
                userId,
                TEST_ENDPOINT_SCHEDULES,
                MULTI_TOKEN_CONSUMPTION_FIVE
        );

        // Assert: 100 - 5 = 95 remaining tokens
        assertThat(result.isAllowed())
                .as("Consuming 5 tokens out of 100 must be allowed")
                .isTrue();
        assertThat(result.getRemainingTokens())
                .as("Remaining tokens must equal 95")
                .isEqualTo(DEFAULT_BUCKET_CAPACITY - MULTI_TOKEN_CONSUMPTION_FIVE);

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Multi-token subtraction assertion satisfied with {} remaining",
                result.getRemainingTokens());
    }
}