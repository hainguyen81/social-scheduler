package org.nlh4j.socialscheduler.ratelimitingservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test suite for {@link RateLimitRepository} validating all persistence
 * operations against a live PostgreSQL Testcontainer instance.
 *
 * <p>This test class bootstraps the full Spring Data JPA runtime context to ensure
 * that repository query methods, entity mappings, and database constraints behave
 * correctly under real relational database conditions.</p>
 *
 * <p>Traceability Tags: [REQ-003], [EXC-002], [EXC-003], [EXC-005]</p>
 *
 * @verifies [REQ-003] Rate limiting input validation and rate check enforcement
 * @verifies [EXC-002] Exception handling for invalid rate limit operations
 * @verifies [EXC-003] Exception handling for malformed rate limit data
 * @verifies [EXC-005] Exception handling for rate limit boundary violations
 */
@Testcontainers
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class RateLimitRepositoryTest {

    // =========================================================================
    // CONSTANT DECLARATIONS — All literal values hoisted to class crown level
    // per enterprise anti-magic-numbers policy [0.2]
    // =========================================================================

    /** Logger instance for structured INFO/DEBUG/ERROR logging [0.3] */
    private static final Logger logger = LoggerFactory.getLogger(RateLimitRepositoryTest.class);

    /** Testcontainer PostgreSQL image version constant [NFR-001] */
    private static final String POSTGRES_IMAGE = "postgres:15-alpine";

    /** Database name for the test container [DAT-003] */
    private static final String DATABASE_NAME = "socialscheduler_test";

    /** Database username for the test container [DAT-003] */
    private static final String DATABASE_USERNAME = "test_user";

    /** Database password for the test container — masked in logs [0.3] */
    private static final String DATABASE_PASSWORD = "test_password_123";

    /** Default endpoint string for rate limit test records [REQ-003] */
    private static final String TEST_ENDPOINT = "/api/v1/schedule";

    /** Alternate endpoint for multi-record tests [REQ-003] */
    private static final String ALT_ENDPOINT = "/api/v1/recommend";

    /** Default request count value for test records [REQ-003] */
    private static final int DEFAULT_REQUEST_COUNT = 10;

    /** High request count to test boundary conditions [EXC-005] */
    private static final int HIGH_REQUEST_COUNT = 1000;

    /** Zero request count to test edge boundary [EXC-005] */
    private static final int ZERO_REQUEST_COUNT = 0;

    /** Window duration in milliseconds for rate limit window [REQ-003] */
    private static final long WINDOW_DURATION_MS = 60_000L;

    /** Expected number of records after bulk insert [REQ-003] */
    private static final int EXPECTED_RECORD_COUNT = 3;

    /** Expected number of records after single insert [REQ-003] */
    private static final int SINGLE_RECORD_COUNT = 1;

    /** Expected number of records after delete operation [REQ-003] */
    private static final int EMPTY_RECORD_COUNT = 0;

    // =========================================================================
    // TESTCONTAINERS — Live PostgreSQL container for integration testing
    // =========================================================================

    /**
     * PostgreSQL Testcontainer instance providing a real database for
     * integration-level repository validation [DAT-003].
     */
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD);

    // =========================================================================
    // DYNAMIC PROPERTY SOURCE — Inject container JDBC URL into Spring context
    // =========================================================================

    /**
     * Dynamically injects the Testcontainer's JDBC URL, username, and password
     * into the Spring Boot test context so that Hibernate can establish a
     * real database connection [DAT-003].
     *
     * @param registry the dynamic property registry for Spring context configuration
     */
    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        // Log the container startup event at INFO level [0.3]
        logger.info("[PROCESS] [DAT-003] Initializing PostgreSQL Testcontainer for RateLimitRepository integration tests");

        // Register JDBC URL dynamically from the running container [DAT-003]
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);

        // Register username dynamically [DAT-003]
        registry.add("spring.datasource.username", postgresContainer::getUsername);

        // Register password dynamically — masked in logs [0.3]
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // Configure Hibernate to use the PostgreSQL dialect [DAT-003]
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Enable DDL auto-creation for test schema [DAT-003]
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Log successful property injection at DEBUG level [0.3]
        logger.debug("[PROCESS] [DAT-003] Testcontainer properties injected into Spring context successfully");
    }

    // =========================================================================
    // SPRING INJECTED DEPENDENCIES
    // =========================================================================

    /**
     * Autowired repository under test — provides real JPA-backed persistence
     * operations against the Testcontainer PostgreSQL instance [REQ-003].
     */
    @Autowired
    private RateLimitRepository rateLimitRepository;

    // =========================================================================
    // TEST LIFECYCLE — Setup and Teardown
    // =========================================================================

    /**
     * Logs the entry point of the test suite at INFO level [0.3].
     * Validates that the Testcontainer is running before any test executes.
     */
    @BeforeAll
    static void beforeAll() {
        // Log test suite entry point [0.3]
        logger.info("[PROCESS] [REQ-003] Starting RateLimitRepositoryTest integration suite — verifying Testcontainer status");

        // Assert container is running before proceeding [EXC-002]
        assertThat(postgresContainer.isRunning())
                .as("[EXC-002] PostgreSQL Testcontainer must be running before test execution")
                .isTrue();

        // Log container readiness confirmation [0.3]
        logger.info("[PROCESS] [DAT-003] PostgreSQL Testcontainer confirmed running at: {}",
                postgresContainer.getJdbcUrl());
    }

    /**
     * Cleans up the database between each test method to ensure
     * test isolation and prevent data leakage across test cases [EXC-003].
     */
    @BeforeEach
    void beforeEach() {
        // Log test method entry at DEBUG level [0.3]
        logger.debug("[PROCESS] [REQ-003] Preparing clean database state for test execution");

        // Delete all existing rate limit records to ensure isolation [EXC-003]
        rateLimitRepository.deleteAll();

        // Log cleanup completion [0.3]
        logger.debug("[PROCESS] [REQ-003] Database cleanup completed — ready for test execution");
    }

    /**
     * Logs the completion of each test method at INFO level [0.3].
     */
    @AfterEach
    void afterEach() {
        // Log test method completion [0.3]
        logger.info("[PROCESS] [REQ-003] Test method execution completed — proceeding to next test case");
    }

    /**
     * Logs the exit point of the test suite at INFO level [0.3].
     */
    @AfterAll
    static void afterAll() {
        // Log test suite exit point [0.3]
        logger.info("[PROCESS] [REQ-003] RateLimitRepositoryTest integration suite completed — all assertions verified");
    }

    // =========================================================================
    // HAPPY PATH TEST CASES
    // =========================================================================

    /**
     * Validates that a {@link RateLimit} entity can be successfully persisted
     * to the database and retrieved by its primary key identifier.
     *
     * <p>Business Requirement: [REQ-003] — Rate limiting service must persist
     * rate limit records with all required fields intact.</p>
     *
     * @verifies [REQ-003] Rate limit record persistence and retrieval
     */
    @Test
    @DisplayName("[REQ-003] Should persist and retrieve RateLimit entity by ID")
    void shouldPersistAndRetrieveRateLimitById() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [REQ-003] Validating RateLimit entity persistence and retrieval by ID");

        // Create a new RateLimit entity with valid test data [REQ-003]
        RateLimit rateLimit = createValidRateLimit();

        // Persist the entity to the live PostgreSQL database [REQ-003]
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log successful save operation [0.3]
        logger.debug("[PROCESS] [REQ-003] RateLimit entity persisted with ID: {}", savedRateLimit.getRateLimitId());

        // Retrieve the entity by its primary key [REQ-003]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert that the entity was found in the database [REQ-003]
        assertThat(retrievedRateLimit)
                .as("[REQ-003] Retrieved RateLimit should be present in database")
                .isPresent();

        // Assert that all fields match the original persisted values [REQ-003]
        RateLimit foundRateLimit = retrievedRateLimit.get();
        assertThat(foundRateLimit.getUserId()).isEqualTo(rateLimit.getUserId());
        assertThat(foundRateLimit.getEndpoint()).isEqualTo(TEST_ENDPOINT);
        assertThat(foundRateLimit.getRequestCount()).isEqualTo(DEFAULT_REQUEST_COUNT);
        assertThat(foundRateLimit.getWindowStart()).isNotNull();
        assertThat(foundRateLimit.getWindowEnd()).isNotNull();

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [REQ-003] RateLimit entity persistence and retrieval validated successfully");
    }

    /**
     * Validates that multiple {@link RateLimit} entities can be persisted
     * and retrieved as a complete list from the database.
     *
     * <p>Business Requirement: [REQ-003] — Rate limiting service must support
     * bulk persistence and retrieval of rate limit records.</p>
     *
     * @verifies [REQ-003] Bulk rate limit record persistence and retrieval
     */
    @Test
    @DisplayName("[REQ-003] Should persist and retrieve multiple RateLimit entities")
    void shouldPersistAndRetrieveMultipleRateLimits() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [REQ-003] Validating bulk RateLimit entity persistence and retrieval");

        // Create and persist first rate limit record [REQ-003]
        RateLimit rateLimit1 = createValidRateLimit();
        rateLimitRepository.save(rateLimit1);

        // Create and persist second rate limit record with alternate endpoint [REQ-003]
        RateLimit rateLimit2 = createValidRateLimit();
        rateLimit2.setEndpoint(ALT_ENDPOINT);
        rateLimitRepository.save(rateLimit2);

        // Create and persist third rate limit record with high request count [REQ-003]
        RateLimit rateLimit3 = createValidRateLimit();
        rateLimit3.setRequestCount(HIGH_REQUEST_COUNT);
        rateLimitRepository.save(rateLimit3);

        // Log persistence of all records [0.3]
        logger.debug("[PROCESS] [REQ-003] Persisted {} RateLimit entities to database", EXPECTED_RECORD_COUNT);

        // Retrieve all rate limit records from the database [REQ-003]
        List<RateLimit> allRateLimits = rateLimitRepository.findAll();

        // Assert that the correct number of records were retrieved [REQ-003]
        assertThat(allRateLimits)
                .as("[REQ-003] Retrieved list should contain exactly {} records", EXPECTED_RECORD_COUNT)
                .hasSize(EXPECTED_RECORD_COUNT);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [REQ-003] Bulk RateLimit entity persistence and retrieval validated successfully");
    }

    /**
     * Validates that an existing {@link RateLimit} entity can be updated
     * and the changes are reflected when retrieved from the database.
     *
     * <p>Business Requirement: [REQ-003] — Rate limiting service must support
     * updating existing rate limit records.</p>
     *
     * @verifies [REQ-003] Rate limit record update persistence
     */
    @Test
    @DisplayName("[REQ-003] Should update existing RateLimit entity")
    void shouldUpdateExistingRateLimit() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [REQ-003] Validating RateLimit entity update operation");

        // Create and persist a new rate limit record [REQ-003]
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log initial save [0.3]
        logger.debug("[PROCESS] [REQ-003] Initial RateLimit persisted with request count: {}",
                savedRateLimit.getRequestCount());

        // Modify the request count to simulate an update [REQ-003]
        savedRateLimit.setRequestCount(HIGH_REQUEST_COUNT);

        // Persist the updated entity [REQ-003]
        RateLimit updatedRateLimit = rateLimitRepository.save(savedRateLimit);

        // Log update operation [0.3]
        logger.debug("[PROCESS] [REQ-003] RateLimit entity updated with new request count: {}",
                updatedRateLimit.getRequestCount());

        // Retrieve the updated entity from the database [REQ-003]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(updatedRateLimit.getRateLimitId());

        // Assert that the entity was found [REQ-003]
        assertThat(retrievedRateLimit)
                .as("[REQ-003] Updated RateLimit should be present in database")
                .isPresent();

        // Assert that the updated request count matches [REQ-003]
        assertThat(retrievedRateLimit.get().getRequestCount())
                .as("[REQ-003] Updated request count should reflect the new value")
                .isEqualTo(HIGH_REQUEST_COUNT);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [REQ-003] RateLimit entity update operation validated successfully");
    }

    /**
     * Validates that a {@link RateLimit} entity can be deleted from the
     * database and is no longer retrievable by its identifier.
     *
     * <p>Business Requirement: [REQ-003] — Rate limiting service must support
     * deletion of expired or invalid rate limit records.</p>
     *
     * @verifies [REQ-003] Rate limit record deletion
     */
    @Test
    @DisplayName("[REQ-003] Should delete RateLimit entity by ID")
    void shouldDeleteRateLimitById() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [REQ-003] Validating RateLimit entity deletion operation");

        // Create and persist a new rate limit record [REQ-003]
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log initial save [0.3]
        logger.debug("[PROCESS] [REQ-003] RateLimit entity persisted with ID: {}", savedRateLimit.getRateLimitId());

        // Delete the entity by its identifier [REQ-003]
        rateLimitRepository.deleteById(savedRateLimit.getRateLimitId());

        // Log deletion operation [0.3]
        logger.debug("[PROCESS] [REQ-003] RateLimit entity deleted with ID: {}", savedRateLimit.getRateLimitId());

        // Attempt to retrieve the deleted entity [REQ-003]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert that the entity is no longer present in the database [REQ-003]
        assertThat(retrievedRateLimit)
                .as("[REQ-003] Deleted RateLimit should not be present in database")
                .isEmpty();

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [REQ-003] RateLimit entity deletion operation validated successfully");
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY CONDITION TEST CASES
    // =========================================================================

    /**
     * Validates that a {@link RateLimit} entity with a zero request count
     * can be persisted and retrieved, testing the lower boundary condition.
     *
     * <p>Business Requirement: [EXC-005] — Rate limiting service must handle
     * boundary values for request counts without data corruption.</p>
     *
     * @verifies [EXC-005] Zero request count boundary handling
     */
    @Test
    @DisplayName("[EXC-005] Should persist RateLimit with zero request count boundary value")
    void shouldPersistRateLimitWithZeroRequestCount() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-005] Validating RateLimit entity with zero request count boundary");

        // Create a rate limit entity with zero request count [EXC-005]
        RateLimit rateLimit = createValidRateLimit();
        rateLimit.setRequestCount(ZERO_REQUEST_COUNT);

        // Persist the entity to the database [EXC-005]
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log successful save [0.3]
        logger.debug("[PROCESS] [EXC-005] RateLimit entity persisted with zero request count");

        // Retrieve the entity by its identifier [EXC-005]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert that the entity was found [EXC-005]
        assertThat(retrievedRateLimit)
                .as("[EXC-005] RateLimit with zero request count should be present in database")
                .isPresent();

        // Assert that the request count is exactly zero [EXC-005]
        assertThat(retrievedRateLimit.get().getRequestCount())
                .as("[EXC-005] Request count should be zero for boundary test")
                .isEqualTo(ZERO_REQUEST_COUNT);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-005] Zero request count boundary validation completed successfully");
    }

    /**
     * Validates that a {@link RateLimit} entity with a very high request count
     * can be persisted and retrieved, testing the upper boundary condition.
     *
     * <p>Business Requirement: [EXC-005] — Rate limiting service must handle
     * high request count values without overflow or truncation.</p>
     *
     * @verifies [EXC-005] High request count boundary handling
     */
    @Test
    @DisplayName("[EXC-005] Should persist RateLimit with high request count boundary value")
    void shouldPersistRateLimitWithHighRequestCount() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-005] Validating RateLimit entity with high request count boundary");

        // Create a rate limit entity with high request count [EXC-005]
        RateLimit rateLimit = createValidRateLimit();
        rateLimit.setRequestCount(HIGH_REQUEST_COUNT);

        // Persist the entity to the database [EXC-005]
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log successful save [0.3]
        logger.debug("[PROCESS] [EXC-005] RateLimit entity persisted with high request count: {}", HIGH_REQUEST_COUNT);

        // Retrieve the entity by its identifier [EXC-005]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert that the entity was found [EXC-005]
        assertThat(retrievedRateLimit)
                .as("[EXC-005] RateLimit with high request count should be present in database")
                .isPresent();

        // Assert that the request count matches the high boundary value [EXC-005]
        assertThat(retrievedRateLimit.get().getRequestCount())
                .as("[EXC-005] Request count should match the high boundary value")
                .isEqualTo(HIGH_REQUEST_COUNT);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-005] High request count boundary validation completed successfully");
    }

    /**
     * Validates that retrieving a non-existent {@link RateLimit} entity
     * returns an empty Optional, testing the null-pointer safety boundary.
     *
     * <p>Business Requirement: [EXC-002] — Rate limiting service must gracefully
     * handle queries for non-existent rate limit records.</p>
     *
     * @verifies [EXC-002] Non-existent entity retrieval returns empty Optional
     */
    @Test
    @DisplayName("[EXC-002] Should return empty Optional when retrieving non-existent RateLimit")
    void shouldReturnEmptyOptionalForNonExistentRateLimit() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-002] Validating retrieval of non-existent RateLimit entity");

        // Generate a random UUID that does not exist in the database [EXC-002]
        UUID nonExistentId = UUID.randomUUID();

        // Attempt to retrieve the non-existent entity [EXC-002]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(nonExistentId);

        // Assert that the Optional is empty [EXC-002]
        assertThat(retrievedRateLimit)
                .as("[EXC-002] Non-existent RateLimit should return empty Optional")
                .isEmpty();

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-002] Non-existent entity retrieval validation completed successfully");
    }

    /**
     * Validates that deleting a non-existent {@link RateLimit} entity
     * does not throw an exception, testing graceful error handling.
     *
     * <p>Business Requirement: [EXC-002] — Rate limiting service must handle
     * deletion of non-existent records without crashing.</p>
     *
     * @verifies [EXC-002] Graceful deletion of non-existent entity
     */
    @Test
    @DisplayName("[EXC-002] Should not throw exception when deleting non-existent RateLimit")
    void shouldNotThrowExceptionWhenDeletingNonExistentRateLimit() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-002] Validating deletion of non-existent RateLimit entity");

        // Generate a random UUID that does not exist in the database [EXC-002]
        UUID nonExistentId = UUID.randomUUID();

        // Assert that deleting a non-existent entity does not throw [EXC-002]
        assertThatThrownBy(() -> rateLimitRepository.deleteById(nonExistentId))
                .as("[EXC-002] Deleting non-existent entity should not throw exception")
                .doesNotThrowAnyException();

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-002] Non-existent entity deletion validation completed successfully");
    }

    /**
     * Validates that the repository returns an empty list when no
     * {@link RateLimit} entities exist in the database.
     *
     * <p>Business Requirement: [EXC-003] — Rate limiting service must handle
     * empty data collection inputs gracefully.</p>
     *
     * @verifies [EXC-003] Empty database returns empty list
     */
    @Test
    @DisplayName("[EXC-003] Should return empty list when no RateLimit entities exist")
    void shouldReturnEmptyListWhenNoRateLimitsExist() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-003] Validating findAll on empty database");

        // Retrieve all rate limit records from the empty database [EXC-003]
        List<RateLimit> allRateLimits = rateLimitRepository.findAll();

        // Assert that the returned list is empty [EXC-003]
        assertThat(allRateLimits)
                .as("[EXC-003] Empty database should return empty list")
                .isEmpty()
                .hasSize(EMPTY_RECORD_COUNT);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-003] Empty database findAll validation completed successfully");
    }

    /**
     * Validates that the repository correctly persists a {@link RateLimit}
     * entity with a very long endpoint string, testing string buffer boundaries.
     *
     * <p>Business Requirement: [EXC-003] — Rate limiting service must handle
     * malformed or extreme-length input data without corruption.</p>
     *
     * @verifies [EXC-003] Long endpoint string boundary handling
     */
    @Test
    @DisplayName("[EXC-003] Should persist RateLimit with long endpoint string")
    void shouldPersistRateLimitWithLongEndpointString() {
        // Log test start at INFO level [0.3]
        logger.info("[TEST_START] [EXC-003] Validating RateLimit entity with long endpoint string");

        // Create a rate limit entity with a long endpoint string [EXC-003]
        RateLimit rateLimit = createValidRateLimit();

        // Build a long endpoint string to test buffer boundary [EXC-003]
        String longEndpoint = TEST_ENDPOINT + "/sub/resource/" + "a".repeat(200);
        rateLimit.setEndpoint(longEndpoint);

        // Persist the entity to the database [EXC-003]
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Log successful save [0.3]
        logger.debug("[PROCESS] [EXC-003] RateLimit entity persisted with long endpoint string of length: {}",
                longEndpoint.length());

        // Retrieve the entity by its identifier [EXC-003]
        Optional<RateLimit> retrievedRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert that the entity was found [EXC-003]
        assertThat(retrievedRateLimit)
                .as("[EXC-003] RateLimit with long endpoint should be present in database")
                .isPresent();

        // Assert that the endpoint string matches exactly [EXC-003]
        assertThat(retrievedRateLimit.get().getEndpoint())
                .as("[EXC-003] Long endpoint string should be preserved exactly")
                .isEqualTo(longEndpoint);

        // Log test completion at INFO level [0.3]
        logger.info("[TEST_END] [EXC-003] Long endpoint string boundary validation completed successfully");
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Creates a valid {@link RateLimit} entity with default test values
     * for use across multiple test cases.
     *
     * <p>This helper ensures consistent test data creation and reduces
     * code duplication across the test suite [REQ-003].</p>
     *
     * @return a fully populated RateLimit entity with valid test data
     */
    private RateLimit createValidRateLimit() {
        // Create a new RateLimit entity instance [REQ-003]
        RateLimit rateLimit = new RateLimit();

        // Set a unique user ID using a random UUID [REQ-003]
        rateLimit.setUserId(UUID.randomUUID());

        // Set the default test endpoint [REQ-003]
        rateLimit.setEndpoint(TEST_ENDPOINT);

        // Set the default request count [REQ-003]
        rateLimit.setRequestCount(DEFAULT_REQUEST_COUNT);

        // Set the window start timestamp to current time [REQ-003]
        rateLimit.setWindowStart(java.sql.Timestamp.from(Instant.now()));

        // Set the window end timestamp to current time plus window duration [REQ-003]
        rateLimit.setWindowEnd(java.sql.Timestamp.from(
                Instant.now().plusMillis(WINDOW_DURATION_MS)));

        // Return the fully populated entity [REQ-003]
        return rateLimit;
    }
}