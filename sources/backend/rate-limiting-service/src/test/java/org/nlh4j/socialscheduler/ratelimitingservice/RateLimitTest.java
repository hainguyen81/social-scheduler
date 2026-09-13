package org.nlh4j.socialscheduler.ratelimitingservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test suite for RateLimit entity and repository.
 * Validates multi-component workflows including database persistence,
 * entity mapping, and repository operations with live PostgreSQL instance.
 *
 * @verifies [REQ-003], [EXC-002], [EXC-003], [EXC-005]
 * @since 1.0
 */
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class RateLimitTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers Policy) [0.2]
    // =========================================================================

    /** Default test user ID for consistent test data. */
    private static final UUID DEFAULT_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** Alternate test user ID for multi-user scenarios. */
    private static final UUID ALTERNATE_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /** Default API endpoint path for rate limiting tests. */
    private static final String DEFAULT_ENDPOINT = "/api/v1/posts";

    /** Alternate API endpoint for boundary testing. */
    private static final String ALTERNATE_ENDPOINT = "/api/v1/comments";

    /** Default request count for happy path scenarios. */
    private static final int DEFAULT_REQUEST_COUNT = 10;

    /** Maximum request count for boundary testing (edge case). */
    private static final int MAX_REQUEST_COUNT = Integer.MAX_VALUE;

    /** Minimum request count for boundary testing (edge case). */
    private static final int MIN_REQUEST_COUNT = 0;

    /** Negative request count for exception testing. */
    private static final int NEGATIVE_REQUEST_COUNT = -1;

    /** Default time window duration in minutes. */
    private static final long WINDOW_DURATION_MINUTES = 15;

    /** Test container PostgreSQL image version. */
    private static final String POSTGRES_IMAGE = "postgres:16-alpine";

    /** Database name for test container. */
    private static final String TEST_DB_NAME = "rate_limit_test_db";

    /** Database username for test container. */
    private static final String TEST_DB_USER = "test_user";

    /** Database password for test container. */
    private static final String TEST_DB_PASSWORD = "test_password";

    /** Logger instance for test execution tracing [0.3]. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitTest.class);

    // =========================================================================
    // TESTCONTAINERS INFRASTRUCTURE SETUP
    // =========================================================================

    /**
     * PostgreSQL test container for integration testing.
     * Provides live database instance for repository validation.
     */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(TEST_DB_NAME)
                    .withUsername(TEST_DB_USER)
                    .withPassword(TEST_DB_PASSWORD)
                    .withReuse(true); // Reuse container across test class lifecycle

    /**
     * Dynamically registers container connection properties to Spring context.
     * Ensures test uses containerized database instead of local instance.
     *
     * @param registry Spring dynamic property registry
     */
    @DynamicPropertySource
    static void configureDataSourceProperties(DynamicPropertyRegistry registry) {
        LOGGER.info("[CONFIG] Registering Testcontainers PostgreSQL datasource properties");
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    // =========================================================================
    // DEPENDENCY INJECTION
    // =========================================================================

    @Autowired
    private RateLimitRepository rateLimitRepository;

    // =========================================================================
    // TEST LIFECYCLE MANAGEMENT
    // =========================================================================

    /**
     * Initializes test container before all test methods.
     * Starts PostgreSQL container and logs connection details.
     */
    @BeforeAll
    void startContainer() {
        LOGGER.info("[TEST_LIFECYCLE] Starting PostgreSQL test container...");
        POSTGRES_CONTAINER.start();
        LOGGER.info("[TEST_LIFECYCLE] PostgreSQL container started at: {}", POSTGRES_CONTAINER.getJdbcUrl());
    }

    /**
     * Stops test container after all test methods complete.
     * Ensures clean resource cleanup.
     */
    @AfterAll
    void stopContainer() {
        LOGGER.info("[TEST_LIFECYCLE] Stopping PostgreSQL test container...");
        POSTGRES_CONTAINER.stop();
        LOGGER.info("[TEST_LIFECYCLE] PostgreSQL container stopped");
    }

    /**
     * Cleans database before each test method.
     * Ensures test isolation and deterministic state.
     */
    @BeforeEach
    void cleanDatabase() {
        LOGGER.info("[TEST_SETUP] Cleaning database before test execution");
        rateLimitRepository.deleteAllInBatch();
        rateLimitRepository.flush();
        LOGGER.info("[TEST_SETUP] Database cleaned successfully");
    }

    // =========================================================================
    // HAPPY PATH TEST CASES [REQ-003]
    // =========================================================================

    /**
     * Tests successful persistence and retrieval of RateLimit entity.
     * Validates basic CRUD operations with valid data.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(1)
    @DisplayName("Happy Path: Should persist and retrieve RateLimit entity with valid data")
    void shouldPersistAndRetrieveRateLimitWithValidData() {
        // Given: A valid RateLimit entity with all required fields
        LOGGER.info("[TEST_START] [REQ-003] Testing RateLimit persistence with valid data");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Persisting the entity to database
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);
        LOGGER.debug("[REQ-003] Saved RateLimit with ID: {}", saved.getRateLimitId());

        // Then: Entity should be retrievable with all fields intact
        Optional<RateLimit> found = rateLimitRepository.findById(saved.getRateLimitId());

        assertThat(found).isPresent();
        RateLimit retrieved = found.get();
        assertThat(retrieved.getRateLimitId()).isEqualTo(rateLimit.getRateLimitId());
        assertThat(retrieved.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(retrieved.getEndpoint()).isEqualTo(DEFAULT_ENDPOINT);
        assertThat(retrieved.getRequestCount()).isEqualTo(DEFAULT_REQUEST_COUNT);
        assertThat(retrieved.getWindowStart()).isEqualTo(windowStart);
        assertThat(retrieved.getWindowEnd()).isEqualTo(windowEnd);
        LOGGER.info("[TEST_PASS] [REQ-003] RateLimit persisted and retrieved successfully");
    }

    /**
     * Tests finding RateLimit by user ID and endpoint combination.
     * Validates custom query method for rate limit lookup.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(2)
    @DisplayName("Happy Path: Should find RateLimit by userId and endpoint")
    void shouldFindRateLimitByUserIdAndEndpoint() {
        // Given: Multiple RateLimit entries for different endpoints
        LOGGER.info("[TEST_START] [REQ-003] Testing findByUserIdAndEndpoint query");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit limit1 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(5)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        RateLimit limit2 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(ALTERNATE_ENDPOINT)
                .requestCount(3)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        rateLimitRepository.saveAllAndFlush(List.of(limit1, limit2));

        // When: Querying by userId and specific endpoint
        Optional<RateLimit> found = rateLimitRepository.findByUserIdAndEndpoint(DEFAULT_USER_ID, DEFAULT_ENDPOINT);

        // Then: Should return the correct RateLimit for the endpoint
        assertThat(found).isPresent();
        assertThat(found.get().getEndpoint()).isEqualTo(DEFAULT_ENDPOINT);
        assertThat(found.get().getRequestCount()).isEqualTo(5);
        LOGGER.info("[TEST_PASS] [REQ-003] findByUserIdAndEndpoint returned correct entity");
    }

    /**
     * Tests finding all RateLimits for a specific user.
     * Validates repository query for user-scoped rate limits.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(3)
    @DisplayName("Happy Path: Should find all RateLimits for a user")
    void shouldFindAllRateLimitsForUser() {
        // Given: Multiple RateLimit entries for same user
        LOGGER.info("[TEST_START] [REQ-003] Testing findByUserId query");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit limit1 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(10)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        RateLimit limit2 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(ALTERNATE_ENDPOINT)
                .requestCount(7)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        rateLimitRepository.saveAllAndFlush(List.of(limit1, limit2));

        // When: Querying all rate limits for user
        List<RateLimit> found = rateLimitRepository.findByUserId(DEFAULT_USER_ID);

        // Then: Should return both entries
        assertThat(found).hasSize(2);
        assertThat(found).extracting(RateLimit::getEndpoint)
                .containsExactlyInAnyOrder(DEFAULT_ENDPOINT, ALTERNATE_ENDPOINT);
        LOGGER.info("[TEST_PASS] [REQ-003] findByUserId returned all user rate limits");
    }

    /**
     * Tests updating request count for existing RateLimit.
     * Validates increment operation for rate limiting logic.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(4)
    @DisplayName("Happy Path: Should update request count for existing RateLimit")
    void shouldUpdateRequestCountForExistingRateLimit() {
        // Given: An existing RateLimit entry
        LOGGER.info("[TEST_START] [REQ-003] Testing request count increment");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);
        UUID rateLimitId = saved.getRateLimitId();

        // When: Incrementing request count
        int newCount = DEFAULT_REQUEST_COUNT + 5;
        RateLimit toUpdate = rateLimitRepository.findById(rateLimitId).orElseThrow();
        toUpdate.setRequestCount(newCount);
        RateLimit updated = rateLimitRepository.saveAndFlush(toUpdate);

        // Then: Request count should be updated
        assertThat(updated.getRequestCount()).isEqualTo(newCount);

        // And: Database should reflect the change
        RateLimit reloaded = rateLimitRepository.findById(rateLimitId).orElseThrow();
        assertThat(reloaded.getRequestCount()).isEqualTo(newCount);
        LOGGER.info("[TEST_PASS] [REQ-003] Request count updated from {} to {}", DEFAULT_REQUEST_COUNT, newCount);
    }

    /**
     * Tests deletion of RateLimit entity.
     * Validates remove operation for cleanup scenarios.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(5)
    @DisplayName("Happy Path: Should delete RateLimit entity")
    void shouldDeleteRateLimitEntity() {
        // Given: An existing RateLimit entry
        LOGGER.info("[TEST_START] [REQ-003] Testing RateLimit deletion");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);
        UUID rateLimitId = saved.getRateLimitId();

        // When: Deleting the entity
        rateLimitRepository.deleteById(rateLimitId);
        rateLimitRepository.flush();

        // Then: Entity should no longer exist
        Optional<RateLimit> found = rateLimitRepository.findById(rateLimitId);
        assertThat(found).isEmpty();
        LOGGER.info("[TEST_PASS] [REQ-003] RateLimit deleted successfully");
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY CONDITION TEST CASES [REQ-003], [EXC-002], [EXC-003]
    // =========================================================================

    /**
     * Tests RateLimit with minimum request count (zero).
     * Validates boundary condition for request counting.
     *
     * @verifies [REQ-003], [EXC-002]
     */
    @Test
    @Order(6)
    @DisplayName("Edge Case: Should handle zero request count boundary")
    void shouldHandleZeroRequestCountBoundary() {
        // Given: RateLimit with zero request count
        LOGGER.info("[TEST_START] [REQ-003][EXC-002] Testing zero request count boundary");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(MIN_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Persisting zero count
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);

        // Then: Should persist successfully with zero count
        assertThat(saved.getRequestCount()).isEqualTo(MIN_REQUEST_COUNT);
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-002] Zero request count handled correctly");
    }

    /**
     * Tests RateLimit with maximum integer request count.
     * Validates upper boundary for request counting.
     *
     * @verifies [REQ-003], [EXC-002]
     */
    @Test
    @Order(7)
    @DisplayName("Edge Case: Should handle maximum integer request count boundary")
    void shouldHandleMaxRequestCountBoundary() {
        // Given: RateLimit with maximum request count
        LOGGER.info("[TEST_START] [REQ-003][EXC-002] Testing maximum request count boundary");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(MAX_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Persisting maximum count
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);

        // Then: Should persist successfully with max count
        assertThat(saved.getRequestCount()).isEqualTo(MAX_REQUEST_COUNT);
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-002] Maximum request count handled correctly");
    }

    /**
     * Tests RateLimit with window start equal to window end (zero duration).
     * Validates boundary condition for time window.
     *
     * @verifies [REQ-003], [EXC-003]
     */
    @Test
    @Order(8)
    @DisplayName("Edge Case: Should handle zero-duration time window")
    void shouldHandleZeroDurationTimeWindow() {
        // Given: RateLimit with identical start and end timestamps
        LOGGER.info("[TEST_START] [REQ-003][EXC-003] Testing zero-duration time window");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart; // Zero duration

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Persisting zero-duration window
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);

        // Then: Should persist successfully
        assertThat(saved.getWindowStart()).isEqualTo(saved.getWindowEnd());
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-003] Zero-duration window handled correctly");
    }

    /**
     * Tests RateLimit with very long endpoint string (255 chars max).
     * Validates string length boundary for endpoint field.
     *
     * @verifies [REQ-003], [EXC-002]
     */
    @Test
    @Order(9)
    @DisplayName("Edge Case: Should handle maximum length endpoint string")
    void shouldHandleMaxLengthEndpoint() {
        // Given: RateLimit with 255-character endpoint (typical VARCHAR limit)
        LOGGER.info("[TEST_START] [REQ-003][EXC-002] Testing maximum endpoint length");
        String longEndpoint = "/api/v1/" + "a".repeat(240); // Total ~255 chars
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(longEndpoint)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Persisting long endpoint
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);

        // Then: Should persist with full endpoint string
        assertThat(saved.getEndpoint()).isEqualTo(longEndpoint);
        assertThat(saved.getEndpoint()).hasSize(255);
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-002] Maximum endpoint length handled correctly");
    }

    /**
     * Tests concurrent RateLimit entries for same user and endpoint
     * with different time windows (non-overlapping).
     * Validates multiple windows per user-endpoint combination.
     *
     * @verifies [REQ-003], [EXC-003]
     */
    @Test
    @Order(10)
    @DisplayName("Edge Case: Should handle multiple non-overlapping windows for same user-endpoint")
    void shouldHandleMultipleNonOverlappingWindows() {
        // Given: Two RateLimits for same user/endpoint with different windows
        LOGGER.info("[TEST_START] [REQ-003][EXC-003] Testing multiple non-overlapping windows");
        Instant baseTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        RateLimit window1 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(10)
                .windowStart(baseTime)
                .windowEnd(baseTime.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES))
                .build();

        RateLimit window2 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(5)
                .windowStart(baseTime.plus(20, ChronoUnit.MINUTES))
                .windowEnd(baseTime.plus(35, ChronoUnit.MINUTES))
                .build();

        // When: Persisting both windows
        rateLimitRepository.saveAllAndFlush(List.of(window1, window2));

        // Then: Both should exist independently
        List<RateLimit> found = rateLimitRepository.findByUserIdAndEndpoint(DEFAULT_USER_ID, DEFAULT_ENDPOINT);
        // Note: findByUserIdAndEndpoint returns single, so we use findByUserId
        List<RateLimit> allForUser = rateLimitRepository.findByUserId(DEFAULT_USER_ID);
        assertThat(allForUser).hasSize(2);
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-003] Multiple non-overlapping windows handled correctly");
    }

    /**
     * Tests RateLimit with future-dated window (scheduled rate limit).
     * Validates temporal boundary for pre-configured limits.
     *
     * @verifies [REQ-003], [EXC-003]
     */
    @Test
    @Order(11)
    @DisplayName("Edge Case: Should handle future-dated rate limit window")
    void shouldHandleFutureDatedWindow() {
        // Given: RateLimit with window in the future
        LOGGER.info("[TEST_START] [REQ-003][EXC-003] Testing future-dated window");
        Instant futureStart = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant futureEnd = futureStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(futureStart)
                .windowEnd(futureEnd)
                .build();

        // When: Persisting future-dated window
        RateLimit saved = rateLimitRepository.saveAndFlush(rateLimit);

        // Then: Should persist with future timestamps
        assertThat(saved.getWindowStart()).isEqualTo(futureStart);
        assertThat(saved.getWindowEnd()).isEqualTo(futureEnd);
        assertThat(saved.getWindowStart()).isAfter(Instant.now());
        LOGGER.info("[TEST_PASS] [REQ-003][EXC-003] Future-dated window handled correctly");
    }

    // =========================================================================
    // EXCEPTION & NEGATIVE PATH TEST CASES [EXC-002], [EXC-003], [EXC-005]
    // =========================================================================

    /**
     * Tests persistence failure when userId is null (violates NOT NULL constraint).
     * Validates database constraint enforcement for required foreign key.
     *
     * @verifies [EXC-002], [EXC-005]
     */
    @Test
    @Order(12)
    @DisplayName("Exception Case: Should fail when userId is null (NOT NULL constraint)")
    void shouldFailWhenUserIdIsNull() {
        // Given: RateLimit with null userId
        LOGGER.info("[TEST_START] [EXC-002][EXC-005] Testing null userId constraint violation");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(null) // Violates NOT NULL constraint
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When/Then: Should throw constraint violation exception
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("null");
        LOGGER.info("[TEST_PASS] [EXC-002][EXC-005] Null userId correctly rejected by database");
    }

    /**
     * Tests persistence failure when endpoint is null (violates NOT NULL constraint).
     * Validates database constraint enforcement for required field.
     *
     * @verifies [EXC-002], [EXC-005]
     */
    @Test
    @Order(13)
    @DisplayName("Exception Case: Should fail when endpoint is null (NOT NULL constraint)")
    void shouldFailWhenEndpointIsNull() {
        // Given: RateLimit with null endpoint
        LOGGER.info("[TEST_START] [EXC-002][EXC-005] Testing null endpoint constraint violation");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(null) // Violates NOT NULL constraint
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When/Then: Should throw constraint violation exception
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("null");
        LOGGER.info("[TEST_PASS] [EXC-002][EXC-005] Null endpoint correctly rejected by database");
    }

    /**
     * Tests persistence failure when windowStart is null (violates NOT NULL constraint).
     * Validates database constraint enforcement for required timestamp.
     *
     * @verifies [EXC-003], [EXC-005]
     */
    @Test
    @Order(14)
    @DisplayName("Exception Case: Should fail when windowStart is null (NOT NULL constraint)")
    void shouldFailWhenWindowStartIsNull() {
        // Given: RateLimit with null windowStart
        LOGGER.info("[TEST_START] [EXC-003][EXC-005] Testing null windowStart constraint violation");
        Instant windowEnd = Instant.now().plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(null) // Violates NOT NULL constraint
                .windowEnd(windowEnd)
                .build();

        // When/Then: Should throw constraint violation exception
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("null");
        LOGGER.info("[TEST_PASS] [EXC-003][EXC-005] Null windowStart correctly rejected by database");
    }

    /**
     * Tests persistence failure when windowEnd is null (violates NOT NULL constraint).
     * Validates database constraint enforcement for required timestamp.
     *
     * @verifies [EXC-003], [EXC-005]
     */
    @Test
    @Order(15)
    @DisplayName("Exception Case: Should fail when windowEnd is null (NOT NULL constraint)")
    void shouldFailWhenWindowEndIsNull() {
        // Given: RateLimit with null windowEnd
        LOGGER.info("[TEST_START] [EXC-003][EXC-005] Testing null windowEnd constraint violation");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(null) // Violates NOT NULL constraint
                .build();

        // When/Then: Should throw constraint violation exception
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("null");
        LOGGER.info("[TEST_PASS] [EXC-003][EXC-005] Null windowEnd correctly rejected by database");
    }

    /**
     * Tests persistence failure when requestCount is null (violates NOT NULL constraint).
     * Validates database constraint enforcement for required integer field.
     *
     * @verifies [EXC-002], [EXC-005]
     */
    @Test
    @Order(16)
    @DisplayName("Exception Case: Should fail when requestCount is null (NOT NULL constraint)")
    void shouldFailWhenRequestCountIsNull() {
        // Given: RateLimit with null requestCount
        LOGGER.info("[TEST_START] [EXC-002][EXC-005] Testing null requestCount constraint violation");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(null) // Violates NOT NULL constraint
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When/Then: Should throw constraint violation exception
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("null");
        LOGGER.info("[TEST_PASS] [EXC-002][EXC-005] Null requestCount correctly rejected by database");
    }

    /**
     * Tests foreign key constraint violation for non-existent userId.
     * Validates referential integrity enforcement.
     *
     * @verifies [EXC-005]
     */
    @Test
    @Order(17)
    @DisplayName("Exception Case: Should fail when userId references non-existent user (FK constraint)")
    void shouldFailWhenUserIdReferencesNonExistentUser() {
        // Given: RateLimit with userId that doesn't exist in users table
        LOGGER.info("[TEST_START] [EXC-005] Testing foreign key constraint violation");
        UUID nonExistentUserId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(nonExistentUserId) // FK violation - user doesn't exist
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When/Then: Should throw foreign key constraint violation
        assertThatThrownBy(() -> rateLimitRepository.saveAndFlush(rateLimit))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("foreign key");
        LOGGER.info("[TEST_PASS] [EXC-005] Non-existent userId correctly rejected by FK constraint");
    }

    /**
     * Tests finding RateLimit by non-existent ID returns empty Optional.
     * Validates graceful handling of missing entities.
     *
     * @verifies [EXC-005]
     */
    @Test
    @Order(18)
    @DisplayName("Exception Case: Should return empty Optional for non-existent RateLimit ID")
    void shouldReturnEmptyForNonExistentRateLimitId() {
        // Given: Non-existent RateLimit ID
        LOGGER.info("[TEST_START] [EXC-005] Testing findById for non-existent entity");
        UUID nonExistentId = UUID.fromString("88888888-8888-8888-8888-888888888888");

        // When: Querying for non-existent ID
        Optional<RateLimit> found = rateLimitRepository.findById(nonExistentId);

        // Then: Should return empty Optional (not throw exception)
        assertThat(found).isEmpty();
        LOGGER.info("[TEST_PASS] [EXC-005] Non-existent ID returns empty Optional gracefully");
    }

    /**
     * Tests finding RateLimit by userId and endpoint returns empty for non-existent combination.
     * Validates graceful handling of missing query results.
     *
     * @verifies [EXC-005]
     */
    @Test
    @Order(19)
    @DisplayName("Exception Case: Should return empty Optional for non-existent user-endpoint combination")
    void shouldReturnEmptyForNonExistentUserEndpointCombination() {
        // Given: Non-existent user-endpoint combination
        LOGGER.info("[TEST_START] [EXC-005] Testing findByUserIdAndEndpoint for non-existent combination");
        UUID nonExistentUserId = UUID.fromString("77777777-7777-7777-7777-777777777777");

        // When: Querying for non-existent combination
        Optional<RateLimit> found = rateLimitRepository.findByUserIdAndEndpoint(nonExistentUserId, DEFAULT_ENDPOINT);

        // Then: Should return empty Optional
        assertThat(found).isEmpty();
        LOGGER.info("[TEST_PASS] [EXC-005] Non-existent user-endpoint returns empty Optional gracefully");
    }

    /**
     * Tests finding RateLimits for non-existent user returns empty list.
     * Validates graceful handling of empty result sets.
     *
     * @verifies [EXC-005]
     */
    @Test
    @Order(20)
    @DisplayName("Exception Case: Should return empty list for non-existent user")
    void shouldReturnEmptyListForNonExistentUser() {
        // Given: Non-existent user ID
        LOGGER.info("[TEST_START] [EXC-005] Testing findByUserId for non-existent user");
        UUID nonExistentUserId = UUID.fromString("66666666-6666-6666-6666-666666666666");

        // When: Querying for non-existent user
        List<RateLimit> found = rateLimitRepository.findByUserId(nonExistentUserId);

        // Then: Should return empty list (not null, not throw exception)
        assertThat(found).isEmpty();
        LOGGER.info("[TEST_PASS] [EXC-005] Non-existent user returns empty list gracefully");
    }

    /**
     * Tests RateLimit entity builder pattern with all fields.
     * Validates object construction and immutability patterns.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(21)
    @DisplayName("Happy Path: Should build RateLimit entity with all fields via builder")
    void shouldBuildRateLimitWithAllFieldsViaBuilder() {
        // Given/When: Building RateLimit using builder pattern
        LOGGER.info("[TEST_START] [REQ-003] Testing RateLimit builder pattern");
        UUID testId = UUID.randomUUID();
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(testId)
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // Then: All fields should be set correctly
        assertThat(rateLimit.getRateLimitId()).isEqualTo(testId);
        assertThat(rateLimit.getUserId()).isEqualTo(DEFAULT_USER_ID);
        assertThat(rateLimit.getEndpoint()).isEqualTo(DEFAULT_ENDPOINT);
        assertThat(rateLimit.getRequestCount()).isEqualTo(DEFAULT_REQUEST_COUNT);
        assertThat(rateLimit.getWindowStart()).isEqualTo(windowStart);
        assertThat(rateLimit.getWindowEnd()).isEqualTo(windowEnd);
        LOGGER.info("[TEST_PASS] [REQ-003] RateLimit builder pattern works correctly");
    }

    /**
     * Tests RateLimit entity equals and hashCode contract.
     * Validates proper entity identity implementation for collections.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(22)
    @DisplayName("Happy Path: Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given: Two RateLimit entities with same ID
        LOGGER.info("[TEST_START] [REQ-003] Testing equals and hashCode contract");
        UUID sharedId = UUID.randomUUID();
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant windowEnd = windowStart.plus(WINDOW_DURATION_MINUTES, ChronoUnit.MINUTES);

        RateLimit limit1 = RateLimit.builder()
                .rateLimitId(sharedId)
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        RateLimit limit2 = RateLimit.builder()
                .rateLimitId(sharedId)
                .userId(ALTERNATE_USER_ID) // Different user
                .endpoint(ALTERNATE_ENDPOINT) // Different endpoint
                .requestCount(999) // Different count
                .windowStart(windowStart.plus(1, ChronoUnit.HOURS)) // Different time
                .windowEnd(windowEnd.plus(1, ChronoUnit.HOURS))
                .build();

        // When/Then: Entities with same ID should be equal
        assertThat(limit1).isEqualTo(limit2);
        assertThat(limit1.hashCode()).isEqualTo(limit2.hashCode());

        // And: Entity should not equal null or different type
        assertThat(limit1).isNotEqualTo(null);
        assertThat(limit1).isNotEqualTo("not a rate limit");

        // And: Different IDs should not be equal
        RateLimit limit3 = RateLimit.builder()
                .rateLimitId(UUID.randomUUID())
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();
        assertThat(limit1).isNotEqualTo(limit3);
        LOGGER.info("[TEST_PASS] [REQ-003] equals and hashCode contract validated");
    }

    /**
     * Tests RateLimit toString method includes key fields.
     * Validates debugging and logging utility.
     *
     * @verifies [REQ-003]
     */
    @Test
    @Order(23)
    @DisplayName("Happy Path: Should generate meaningful toString representation")
    void shouldGenerateMeaningfulToString() {
        // Given: A RateLimit entity
        LOGGER.info("[TEST_START] [REQ-003] Testing toString representation");
        UUID testId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Instant windowStart = Instant.parse("2026-01-15T10:00:00Z");
        Instant windowEnd = Instant.parse("2026-01-15T10:15:00Z");

        RateLimit rateLimit = RateLimit.builder()
                .rateLimitId(testId)
                .userId(DEFAULT_USER_ID)
                .endpoint(DEFAULT_ENDPOINT)
                .requestCount(DEFAULT_REQUEST_COUNT)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .build();

        // When: Calling toString
        String toString = rateLimit.toString();

        // Then: Should contain key identifying fields
        assertThat(toString).contains("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        assertThat(toString).contains("11111111-1111-1111-1111-111111111111");
        assertThat(toString).contains("/api/v1/posts");
        assertThat(toString).contains("10");
        LOGGER.info("[TEST_PASS] [REQ-003] toString contains key fields: {}", toString);
    }
}