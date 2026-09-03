# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo lớp kiểm thử ./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java sử dụng JUnit 5 kết hợp Testcontainers Redis (org.testcontainers:redis:2.2.x) và @DynamicPropertySource để khởi tạo Redis container thực tế. Sử dụng @SpringBootTest với @Testcontainers và @Container static RedisContainer redis = new RedisContainer("redis:7.2-alpine"). Inject RedisTokenBucketStrategy thông qua @Autowired. Viết các trường hợp kiểm thử: (1) tryConsume_whenBucketIsFull_thenReturnAllowedTrue - bucket 100 token, gọi tryConsume(userId, endpoint, 1), khẳng định result.isAllowed()==true và result.getRemainingTokens()==99; (2) tryConsume_whenBucketIsEmpty_thenThrowRateLimitExceededException - tiêu thụ 100 token liên tiếp, lần 101 ném RateLimitExceededException với retryAfterSeconds [EXC-005]; (3) tryConsume_whenTokensRefilledAfterDelay_thenBucketRestored - Thread.sleep(2000) chờ refill, khẳng định bucket bổ sung 2 token [REQ-003]; (4) tryConsume_concurrentRequests_thenAtomicityPreserved - ExecutorService với 100 luồng đồng thời gọi tryConsume, khẳng định tổng allowed=true không vượt quá capacity=100; (5) tryConsume_differentEndpoints_thenIndependentBuckets - khẳng định bucket quản lý độc lập theo endpoint; (6) tryConsume_multipleTokensAtOnce_thenSubtractCorrectly - tryConsume(userId, endpoint, 5), remainingTokens==95. Sử dụng @BeforeEach flush Redis và khởi tạo RateLimiterConfig. AssertJ cho fluent assertion. Độ phủ mã nguồn đạt tối thiểu 85%.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model models/gemini-flash-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo lớp kiểm thử ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java sử dụng @WebMvcTest(controllers = ScheduleController.class) kết hợp @MockBean ScheduleService và @Import(GlobalExceptionHandler.class). Inject MockMvc thông qua @Autowired MockMvc mockMvc. Sử dụng ObjectMapper để serialize/deserialize JSON payload. Viết các kịch bản: (1) handleValidationException_whenPayloadInvalid_thenReturn400 - gửi POST request payload thiếu platform, khẳng định HTTP 400, body errorCode="VALIDATION_FAILED", fieldErrors chứa thông tin trường vi phạm, correlationId không null [EXC-002] [REQ-003]; (2) handleTokenExpired_whenJwtInvalid_thenReturn401 - cấu hình @MockBean JwtDecoder ném JwtException, HTTP 401, body errorCode="TOKEN_EXPIRED" [EXC-002]; (3) handleUpstreamError_whenSocialPlatformThrows_thenReturn502 - @MockBean ScheduleService.createSchedule() ném SocialPlatformException, HTTP 502, errorCode="UPSTREAM_SERVICE_ERROR", correlationId không null [EXC-003]; (4) handleRateLimitExceeded_thenReturn429WithRetryAfter - @MockBean RateLimiterService ném RateLimitExceededException(userId, endpoint, 60), HTTP 429, header Retry-After: 60, errorCode="RATE_LIMIT_EXCEEDED" [EXC-005]; (5) handleGenericException_whenUnexpectedError_thenReturn500WithoutStackTrace - RuntimeException("Database connection failed"), HTTP 500, errorCode="INTERNAL_SERVER_ERROR", không tiết lộ stack trace theo OWASP A09. Sử dụng header().string("X-Correlation-Id", notNullValue()) xác minh correlation ID trong response header. Độ phủ mã nguồn đạt tối thiểu 85% cho GlobalExceptionHandler.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]', '[EXC-003]', '[EXC-005]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.nlh4j.socialscheduler.ratelimitservice.service.RateLimiterService;
import org.nlh4j.socialscheduler.scheduleservice.controller.ScheduleController;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test suite for {@link GlobalExceptionHandler} mapping REST responses to centralized error contracts.
 * Verifies compliance with OWASP Top 10 standards (A04 Insecure Design, A05 Security Misconfiguration, A09 Logging Failures).
 *
 * @verifies [REQ-003] Input validation constraints and Rate Limiter integration
 * @verifies [EXC-002] Handling expired or invalid JWT security tokens
 * @verifies [EXC-003] Handling downstream and third-party social provider communication failures
 * @verifies [EXC-005] Handling token-bucket rate limit violations with HTTP 429 and Retry-After headers
 */
@WebMvcTest(controllers = ScheduleController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerIntegrationTest {

    // [REQ-003] Logger initialization for audit tracing and process verification
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandlerIntegrationTest.class);

    // [REQ-003] Top-of-Class Immutable API and Header Constants
    public static final String SCHEDULE_API_ENDPOINT = "/api/v1/schedules";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String RETRY_AFTER_HEADER = "Retry-After";

    // [REQ-003] Standard Error Code Constants
    public static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERROR_CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_UPSTREAM_SERVICE_ERROR = "UPSTREAM_SERVICE_ERROR";
    public static final String ERROR_CODE_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String ERROR_CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // [REQ-003] Test Fixture Domain Values
    public static final String TEST_USER_ID_STRING = "11111111-2222-3333-4444-555555555555";
    public static final String VALID_PLATFORM_FACEBOOK = "Facebook";
    public static final String VALID_CONTENT_PAYLOAD = "Enterprise Grade Social Scheduling Post Content #automated";
    public static final long DEFAULT_RETRY_AFTER_SECONDS = 60L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private ScheduleRequestDto validScheduleRequestDto;

    /**
     * Initializes test fixtures before each test execution run.
     */
    @BeforeEach
    void setUp() {
        // [REQ-003] Setup baseline valid scheduling request DTO with future timestamp
        validScheduleRequestDto = new ScheduleRequestDto();
        validScheduleRequestDto.setPlatform(VALID_PLATFORM_FACEBOOK);
        validScheduleRequestDto.setContent(VALID_CONTENT_PAYLOAD);
        validScheduleRequestDto.setScheduledTime(OffsetDateTime.now().plusDays(2));
    }

    /**
     * Verifies that submitting a malformed payload missing required fields triggers Jakarta Validation,
     * returning HTTP 400 Bad Request with granular field error mappings and correlation ID.
     *
     * @verifies [REQ-003] Input validation enforcement via Jakarta Validation
     * @verifies [EXC-002] Security and payload exception handling
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Missing Platform Field Should Return HTTP 400 and VALIDATION_FAILED")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleValidationException_whenPayloadInvalid_thenReturn400() throws Exception {
        // [REQ-003] Entry logging
        LOGGER.info("[TEST_START] [REQ-003] [EXC-002] Executing input validation failure test case");

        // Construct invalid payload missing the mandatory platform field
        ScheduleRequestDto invalidDto = new ScheduleRequestDto();
        invalidDto.setPlatform(null);
        invalidDto.setContent(VALID_CONTENT_PAYLOAD);
        invalidDto.setScheduledTime(OffsetDateTime.now().plusDays(1));

        String requestBody = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 400 Bad Request
                .andExpect(status().isBadRequest())
                // Verify standard enterprise error envelope
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_VALIDATION_FAILED)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("platform")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                // Verify tracing header contract
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] [EXC-002] Validation failure scenario successfully verified");
    }

    /**
     * Verifies that submitting an expired or corrupt JWT token throws a JwtException,
     * which is intercepted by GlobalExceptionHandler to return HTTP 401 Unauthorized.
     *
     * @verifies [EXC-002] Token expiration lifecycle handling
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Expired JWT Token Should Return HTTP 401 and TOKEN_EXPIRED")
    void handleTokenExpired_whenJwtInvalid_thenReturn401() throws Exception {
        LOGGER.info("[TEST_START] [EXC-002] Executing invalid/expired JWT token verification");

        // Mock JwtDecoder to simulate token expiration failure
        given(jwtDecoder.decode(any())).willThrow(new JwtException("Token has expired or signature is invalid"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expiredTokenPayload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_TOKEN_EXPIRED)))
                .andExpect(jsonPath("$.message", containsString("Token has expired")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-002] Token expiration handling verified with HTTP 401");
    }

    /**
     * Verifies that third-party downstream platform network timeouts or communication errors
     * return HTTP 502 Bad Gateway with UPSTREAM_SERVICE_ERROR and correlation ID.
     *
     * @verifies [EXC-003] Third-party social platform upstream error boundary isolation
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Downstream Social API Failure Should Return HTTP 502 and UPSTREAM_SERVICE_ERROR")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleUpstreamError_whenSocialPlatformThrows_thenReturn502() throws Exception {
        LOGGER.info("[TEST_START] [EXC-003] Executing downstream social provider exception mapping test");

        // Mock downstream integration failure inside ScheduleService
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new SocialPlatformException("Facebook Graph API connection timed out: 504 Gateway Timeout"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 502 Bad Gateway
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_UPSTREAM_SERVICE_ERROR)))
                .andExpect(jsonPath("$.message", containsString("Facebook Graph API")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-003] Upstream service failure successfully mapped to HTTP 502");
    }

    /**
     * Verifies that exceeding rate limits returns HTTP 429 Too Many Requests,
     * accompanied by the explicit Retry-After header and localized error payload.
     *
     * @verifies [REQ-003] Redis Token Bucket Rate Limiting integration
     * @verifies [EXC-005] Rate Limit Exceeded threshold enforcement with HTTP 429
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Rate Limit Exceeded Should Return HTTP 429 and Retry-After Header")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleRateLimitExceeded_thenReturn429WithRetryAfter() throws Exception {
        LOGGER.info("[TEST_START] [REQ-003] [EXC-005] Executing Redis token-bucket rate limiter boundary test");

        UUID userId = UUID.fromString(TEST_USER_ID_STRING);

        // Mock RateLimiterService or ScheduleService interceptor throwing RateLimitExceededException
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new RateLimitExceededException(
                        userId,
                        SCHEDULE_API_ENDPOINT,
                        DEFAULT_RETRY_AFTER_SECONDS
                ));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 429 Too Many Requests
                .andExpect(status().isTooManyRequests())
                // Validate required RFC HTTP Retry-After header
                .andExpect(header().string(RETRY_AFTER_HEADER, String.valueOf(DEFAULT_RETRY_AFTER_SECONDS)))
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_RATE_LIMIT_EXCEEDED)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.retryAfterSeconds", is((int) DEFAULT_RETRY_AFTER_SECONDS)))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] [EXC-005] HTTP 429 and Retry-After header successfully verified");
    }

    /**
     * Verifies unhandled generic exceptions return HTTP 500 without leaking stack traces or SQL internals (OWASP A05 & A09).
     *
     * @verifies [EXC-002] Defense-in-depth secure exception abstraction
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Unexpected Runtime Exception Should Return HTTP 500 Without Stack Trace")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleGenericException_whenUnexpectedError_thenReturn500WithoutStackTrace() throws Exception {
        LOGGER.info("[TEST_START] [EXC-002] Executing generic unexpected exception shielding test");

        // Mock sudden low-level runtime or DB crash
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new RuntimeException("Database connection failed: FATAL: password authentication failed for user postgres"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 500 Internal Server Error
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_INTERNAL_SERVER_ERROR)))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                // Guarantee OWASP compliance: NO stack traces or internal DB drivers revealed to caller
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("FATAL"))))
                .andExpect(jsonPath("$.message", not(containsString("password"))))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-002] Unhandled exception shielded securely without stack trace leakage");
    }

    /**
     * Verifies the happy path execution returning HTTP 201 Created when the request payload is compliant.
     *
     * @verifies [REQ-003] Standard happy path schedule creation contract
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Compliant Payload Should Return HTTP 201 Created")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void createSchedule_whenPayloadValid_thenReturn201Created() throws Exception {
        LOGGER.info("[TEST_START] [REQ-003] Executing happy path schedule creation test case");

        ScheduleResponseDto mockResponse = new ScheduleResponseDto();
        mockResponse.setScheduleId(UUID.randomUUID());
        mockResponse.setPlatform(VALID_PLATFORM_FACEBOOK);
        mockResponse.setContent(VALID_CONTENT_PAYLOAD);
        mockResponse.setStatus("PENDING");
        mockResponse.setScheduledTime(validScheduleRequestDto.getScheduledTime());

        given(scheduleService.createSchedule(any(ScheduleRequestDto.class))).willReturn(mockResponse);

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 201 Created
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId", notNullValue()))
                .andExpect(jsonPath("$.platform", is(VALID_PLATFORM_FACEBOOK)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Schedule successfully created with status PENDING");
    }
}
```

