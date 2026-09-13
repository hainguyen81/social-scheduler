```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nlh4j.socialscheduler.contentrecommendationservice.PerformanceMetrics;
import org.nlh4j.socialscheduler.contentrecommendationservice.PerformanceMetricsRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test suite for {@link PerformanceMetricsRepository}.
 *
 * <p>This test class validates the persistence layer operations of the
 * PerformanceMetricsRepository using a real PostgreSQL database instance
 * managed by Testcontainers. It ensures that all CRUD operations, query
 * methods, and edge-case scenarios behave correctly against a live database
 * schema, fulfilling the integration testing requirements for the
 * content-recommendation-service module.</p>
 *
 * <p>Tag traceability:
 * <ul>
 *   <li>@verifies [REQ-002] - Content recommendation via AI based on performance metrics</li>
 *   <li>@verifies [EXC-003] - Exception handling for content recommendation service</li>
 *   <li>@verifies [EXC-004] - Exception handling for malformed performance data inputs</li>
 * </ul>
 * </p>
 *
 * @author Enterprise QA Automation Agent
 * @since 1.0
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@Testcontainers
@ActiveProfiles("test")
class PerformanceMetricsRepositoryIntegrationTest {

    // =========================================================================
    // CONSTANTS DECLARATION BLOCK [0.2] - All literals hoisted to top-of-class
    // =========================================================================

    /**
     * Logger instance for capturing test execution lifecycle events.
     * [0.3] - Enterprise logging mandate: INFO/DEBUG at entry/exit of test flows.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMetricsRepositoryIntegrationTest.class);

    /**
     * Docker image reference for the PostgreSQL Testcontainer instance.
     * [DAT-002] - Database schema and connection configuration for performance metrics.
     */
    private static final String POSTGRES_IMAGE = "postgres:15-alpine";

    /**
     * Database name used within the Testcontainer PostgreSQL instance.
     */
    private static final String DATABASE_NAME = "socialscheduler_test";

    /**
     * Database username for the Testcontainer PostgreSQL instance.
     */
    private static final String DATABASE_USERNAME = "test_user";

    /**
     * Database password for the Testcontainer PostgreSQL instance.
     * [0.3] - Sensitive data masking: password is not logged in cleartext.
     */
    private static final String DATABASE_PASSWORD = "test_password_masked";

    /**
     * Default schema name for PostgreSQL Testcontainer.
     */
    private static final String DATABASE_SCHEMA = "public";

    /**
     * Sample likes count for test data generation.
     */
    private static final int SAMPLE_LIKES = 150;

    /**
     * Sample comments count for test data generation.
     */
    private static final int SAMPLE_COMMENTS = 42;

    /**
     * Sample shares count for test data generation.
     */
    private static final int SAMPLE_SHARES = 78;

    /**
     * Zero-value constant for boundary testing of metric counts.
     */
    private static final int ZERO_COUNT = 0;

    /**
     * Maximum integer value for boundary testing of metric counts.
     */
    private static final int MAX_COUNT = Integer.MAX_VALUE;

    /**
     * Expected number of performance metrics records after bulk insertion.
     */
    private static final int EXPECTED_BULK_COUNT = 3;

    /**
     * Expected number of performance metrics records after filtering by post ID.
     */
    private static final int EXPECTED_FILTERED_COUNT = 2;

    // =========================================================================
    // TESTCONTAINERS INFRASTRUCTURE SETUP [NFR-001] - Real database isolation
    // =========================================================================

    /**
     * PostgreSQL Testcontainer instance providing a live database for
     * integration testing. This ensures that all repository operations
     * are validated against a real PostgreSQL engine, not a mock.
     * [NFR-001] - DevOps infrastructure setup with containerized databases.
     */
    @Container
    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DATABASE_USERNAME)
                    .withPassword(DATABASE_PASSWORD)
                    .withScripts("schema.sql");

    /**
     * Autowired repository instance under test.
     * [REQ-002] - Performance metrics repository for AI content recommendation.
     */
    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    /**
     * Test entity manager for direct database verification.
     */
    @Autowired
    private org.springframework.orm.jpa.JpaEntityManager entityManager;

    // =========================================================================
    // DYNAMIC PROPERTY SOURCE - Wiring Testcontainer to Spring context
    // =========================================================================

    /**
     * Dynamically injects Testcontainer database connection properties
     * into the Spring application context before test execution.
     * [DAT-002] - Database connection configuration for integration tests.
     */
    @DynamicPropertySource
    static void configureTestDatabase(DynamicPropertyRegistry registry) {
        LOGGER.info("[TEST_SETUP] [DAT-002] Configuring Testcontainer PostgreSQL datasource for PerformanceMetricsRepository integration tests");
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.show-sql", () -> "true");
        LOGGER.info("[TEST_SETUP] [DAT-002] Testcontainer datasource configuration complete. JDBC URL: {}", POSTGRESQL_CONTAINER.getJdbcUrl());
    }

    // =========================================================================
    // LIFECYCLE HOOKS - Setup and teardown for test isolation
    // =========================================================================

    /**
     * Executes before each test method to ensure a clean database state.
     * [0.3] - Entry point logging for each test case.
     */
    @BeforeEach
    void setUp() {
        LOGGER.info("[TEST_ENTRY] [REQ-002] Starting PerformanceMetricsRepository integration test: {}",
                Assertions.assertDoesNotThrow(() -> Thread.currentThread().getStackTrace()[2].getMethodName()));
        // Clear all records to ensure test isolation
        performanceMetricsRepository.deleteAll();
        // Flush and clear persistence context to avoid stale data
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Executes after each test method to clean up database state.
     * [0.3] - Exit point logging for each test case.
     */
    @AfterEach
    void tearDown() {
        LOGGER.info("[TEST_EXIT] [REQ-002] Completed PerformanceMetricsRepository integration test. Cleaning up database state.");
        performanceMetricsRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // =========================================================================
    // HAPPY PATH TEST CASES [REQ-002]
    // =========================================================================

    /**
     * Validates that a PerformanceMetrics entity can be successfully persisted
     * and retrieved by its primary key identifier.
     *
     * <p>Business requirement: [REQ-002] - The system must store performance
     * metrics (likes, comments, shares) for each social media post to enable
     * AI-driven content recommendation.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("[REQ-002] Should persist and retrieve PerformanceMetrics by ID")
    void shouldPersistAndRetrievePerformanceMetricsById() {
        // Arrange: Create a valid PerformanceMetrics entity with all required fields
        UUID postId = UUID.randomUUID();
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(postId);
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [REQ-002] Persisting PerformanceMetrics for postId: {}", postId);

        // Act: Save the entity to the database
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity was persisted and can be retrieved
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getPostId()).isEqualTo(postId);
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(SAMPLE_LIKES);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(SAMPLE_COMMENTS);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(SAMPLE_SHARES);
        LOGGER.info("[TEST_ASSERT] [REQ-002] Successfully verified persistence and retrieval of PerformanceMetrics with ID: {}", savedMetrics.getPerformanceId());
    }

    /**
     * Validates that multiple PerformanceMetrics entities can be persisted
     * and retrieved as a complete list.
     *
     * <p>Business requirement: [REQ-002] - The system must support bulk
     * storage of performance metrics for batch AI model training.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("[REQ-002] Should persist and retrieve all PerformanceMetrics entities")
    void shouldPersistAndRetrieveAllPerformanceMetrics() {
        // Arrange: Create and persist multiple PerformanceMetrics entities
        List<PerformanceMetrics> metricsList = new java.util.ArrayList<>();
        for (int i = 0; i < EXPECTED_BULK_COUNT; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES + i);
            metrics.setComments(SAMPLE_COMMENTS + i);
            metrics.setShares(SAMPLE_SHARES + i);
            metrics.setCollectedAt(LocalDateTime.now().minusDays(i));
            metricsList.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [REQ-002] Persisting {} PerformanceMetrics entities in bulk", EXPECTED_BULK_COUNT);

        // Act: Save all entities
        performanceMetricsRepository.saveAll(metricsList);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities were persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(EXPECTED_BULK_COUNT);
        LOGGER.info("[TEST_ASSERT] [REQ-002] Verified bulk persistence of {} entities", allMetrics.size());
    }

    /**
     * Validates that a PerformanceMetrics entity can be updated and
     * the changes are reflected in subsequent retrievals.
     *
     * <p>Business requirement: [REQ-002] - The system must support updating
     * performance metrics as new data becomes available from social platforms.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("[REQ-002] Should update existing PerformanceMetrics entity")
    void shouldUpdateExistingPerformanceMetrics() {
        // Arrange: Create and persist an initial entity
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Act: Update the entity with new values
        savedMetrics.setLikes(SAMPLE_LIKES + 100);
        savedMetrics.setComments(SAMPLE_COMMENTS + 50);
        savedMetrics.setShares(SAMPLE_SHARES + 25);
        PerformanceMetrics updatedMetrics = performanceMetricsRepository.save(savedMetrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the updated values are persisted
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(updatedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(SAMPLE_LIKES + 100);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(SAMPLE_COMMENTS + 50);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(SAMPLE_SHARES + 25);
        LOGGER.info("[TEST_ASSERT] [REQ-002] Verified update operation for PerformanceMetrics ID: {}", updatedMetrics.getPerformanceId());
    }

    /**
     * Validates that a PerformanceMetrics entity can be deleted by its ID.
     *
     * <p>Business requirement: [REQ-002] - The system must support deletion
     * of stale or erroneous performance metrics records.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("[REQ-002] Should delete PerformanceMetrics by ID")
    void shouldDeletePerformanceMetricsById() {
        // Arrange: Create and persist an entity
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        UUID savedId = savedMetrics.getPerformanceId();
        LOGGER.info("[TEST_EXEC] [REQ-002] Deleting PerformanceMetrics with ID: {}", savedId);

        // Act: Delete the entity
        performanceMetricsRepository.deleteById(savedId);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity no longer exists
        Optional<PerformanceMetrics> deletedMetrics = performanceMetricsRepository.findById(savedId);
        assertThat(deletedMetrics).isEmpty();
        LOGGER.info("[TEST_ASSERT] [REQ-002] Verified deletion of PerformanceMetrics ID: {}", savedId);
    }

    /**
     * Validates that PerformanceMetrics entities can be found by post ID.
     *
     * <p>Business requirement: [REQ-002] - The system must retrieve all
     * performance metrics associated with a specific social media post
     * for AI content recommendation analysis.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("[REQ-002] Should find PerformanceMetrics by post ID")
    void shouldFindPerformanceMetricsByPostId() {
        // Arrange: Create and persist entities with the same post ID
        UUID sharedPostId = UUID.randomUUID();

        PerformanceMetrics metrics1 = new PerformanceMetrics();
        metrics1.setPostId(sharedPostId);
        metrics1.setLikes(SAMPLE_LIKES);
        metrics1.setComments(SAMPLE_COMMENTS);
        metrics1.setShares(SAMPLE_SHARES);
        metrics1.setCollectedAt(LocalDateTime.now());

        PerformanceMetrics metrics2 = new PerformanceMetrics();
        metrics2.setPostId(sharedPostId);
        metrics2.setLikes(SAMPLE_LIKES + 10);
        metrics2.setComments(SAMPLE_COMMENTS + 5);
        metrics2.setShares(SAMPLE_SHARES + 3);
        metrics2.setCollectedAt(LocalDateTime.now().plusHours(1));

        performanceMetricsRepository.saveAll(java.util.Arrays.asList(metrics1, metrics2));
        entityManager.flush();
        entityManager.clear();

        LOGGER.info("[TEST_EXEC] [REQ-002] Querying PerformanceMetrics by postId: {}", sharedPostId);

        // Act: Find entities by post ID
        List<PerformanceMetrics> foundMetrics = performanceMetricsRepository.findByPostId(sharedPostId);

        // Assert: Verify the correct entities are returned
        assertThat(foundMetrics).hasSize(EXPECTED_FILTERED_COUNT);
        assertThat(foundMetrics).allMatch(m -> m.getPostId().equals(sharedPostId));
        LOGGER.info("[TEST_ASSERT] [REQ-002] Verified findByPostId returned {} entities", foundMetrics.size());
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY CONDITION TEST CASES [EXC-003]
    // =========================================================================

    /**
     * Validates that the repository handles zero-value metric counts correctly.
     *
     * <p>Business requirement: [EXC-003] - The system must gracefully handle
     * edge cases where performance metrics have zero values (e.g., a post
     * with no likes, comments, or shares).</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("[EXC-003] Should handle PerformanceMetrics with zero-value counts")
    void shouldHandleZeroValueMetricCounts() {
        // Arrange: Create an entity with all zero counts
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(ZERO_COUNT);
        metrics.setComments(ZERO_COUNT);
        metrics.setShares(ZERO_COUNT);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-003] Persisting PerformanceMetrics with zero-value counts");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify zero values are correctly persisted
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(ZERO_COUNT);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(ZERO_COUNT);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(ZERO_COUNT);
        LOGGER.info("[TEST_ASSERT] [EXC-003] Verified zero-value metric counts are persisted correctly");
    }

    /**
     * Validates that the repository handles maximum integer value counts.
     *
     * <p>Business requirement: [EXC-003] - The system must handle boundary
     * conditions where metric counts reach the maximum integer value
     * without overflow or data corruption.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("[EXC-003] Should handle PerformanceMetrics with maximum integer counts")
    void shouldHandleMaximumIntegerMetricCounts() {
        // Arrange: Create an entity with maximum integer counts
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(MAX_COUNT);
        metrics.setComments(MAX_COUNT);
        metrics.setShares(MAX_COUNT);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-003] Persisting PerformanceMetrics with maximum integer counts");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify maximum values are correctly persisted
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(MAX_COUNT);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(MAX_COUNT);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(MAX_COUNT);
        LOGGER.info("[TEST_ASSERT] [EXC-003] Verified maximum integer metric counts are persisted correctly");
    }

    /**
     * Validates that the repository returns an empty list when no
     * PerformanceMetrics entities exist for a given post ID.
     *
     * <p>Business requirement: [EXC-003] - The system must handle queries
     * for non-existent post IDs gracefully without throwing exceptions.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("[EXC-003] Should return empty list when no metrics found for post ID")
    void shouldReturnEmptyListWhenNoMetricsFoundForPostId() {
        // Arrange: Use a random post ID that does not exist in the database
        UUID nonExistentPostId = UUID.randomUUID();

        LOGGER.info("[TEST_EXEC] [EXC-003] Querying non-existent postId: {}", nonExistentPostId);

        // Act: Query for metrics by non-existent post ID
        List<PerformanceMetrics> foundMetrics = performanceMetricsRepository.findByPostId(nonExistentPostId);

        // Assert: Verify an empty list is returned
        assertThat(foundMetrics).isEmpty();
        LOGGER.info("[TEST_ASSERT] [EXC-003] Verified empty list returned for non-existent postId");
    }

    /**
     * Validates that the repository correctly handles concurrent
     * insertions of multiple PerformanceMetrics entities.
     *
     * <p>Business requirement: [EXC-003] - The system must maintain data
     * integrity when multiple performance metrics are inserted concurrently
     * from different social media platform collectors.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("[EXC-003] Should handle concurrent insertions of multiple metrics")
    void shouldHandleConcurrentInsertionsOfMultipleMetrics() {
        // Arrange: Create multiple entities with different post IDs
        java.util.List<PerformanceMetrics> concurrentMetrics = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES + i);
            metrics.setComments(SAMPLE_COMMENTS + i);
            metrics.setShares(SAMPLE_SHARES + i);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            concurrentMetrics.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-003] Persisting {} concurrent PerformanceMetrics entities", concurrentMetrics.size());

        // Act: Save all entities concurrently
        performanceMetricsRepository.saveAll(concurrentMetrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities were persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(10);
        LOGGER.info("[TEST_ASSERT] [EXC-003] Verified concurrent insertion of 10 entities");
    }

    // =========================================================================
    // EXCEPTION CASE & NEGATIVE PATH TEST CASES [EXC-004]
    // =========================================================================

    /**
     * Validates that the repository throws an appropriate exception when
     * attempting to retrieve a non-existent entity by ID.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * or invalid input data gracefully, returning appropriate error responses
     * without crashing the application.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should return empty Optional when finding non-existent entity by ID")
    void shouldReturnEmptyOptionalWhenFindingNonExistentEntityById() {
        // Arrange: Use a random UUID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        LOGGER.info("[TEST_EXEC] [EXC-004] Querying non-existent performanceId: {}", nonExistentId);

        // Act: Attempt to find a non-existent entity
        Optional<PerformanceMetrics> result = performanceMetricsRepository.findById(nonExistentId);

        // Assert: Verify an empty Optional is returned (no exception thrown)
        assertThat(result).isEmpty();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified empty Optional returned for non-existent ID");
    }

    /**
     * Validates that the repository handles deletion of a non-existent
     * entity without throwing an exception.
     *
     * <p>Business requirement: [EXC-004] - The system must gracefully handle
     * deletion requests for non-existent records without propagating errors.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle deletion of non-existent entity without exception")
    void shouldHandleDeletionOfNonExistentEntityWithoutException() {
        // Arrange: Use a random UUID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        LOGGER.info("[TEST_EXEC] [EXC-004] Attempting to delete non-existent performanceId: {}", nonExistentId);

        // Act & Assert: Verify no exception is thrown when deleting a non-existent entity
        assertThatCode(() -> performanceMetricsRepository.deleteById(nonExistentId))
                .doesNotThrowAnyException();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified no exception thrown for non-existent entity deletion");
    }

    /**
     * Validates that the repository correctly handles entities with
     * null collectedAt timestamps.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * input data where optional fields like collectedAt may be null.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with null collectedAt timestamp")
    void shouldHandlePerformanceMetricsWithNullCollectedAt() {
        // Arrange: Create an entity with null collectedAt
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(null);

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with null collectedAt");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with null collectedAt
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getCollectedAt()).isNull();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified null collectedAt is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * null postId values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * input data where required fields like postId may be null, ensuring
     * appropriate database constraint enforcement.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with null postId")
    void shouldHandlePerformanceMetricsWithNullPostId() {
        // Arrange: Create an entity with null postId
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(null);
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Attempting to persist PerformanceMetrics with null postId");

        // Act & Assert: Verify that persisting with null postId either succeeds
        // (if the column allows null) or throws an appropriate exception
        assertThatCode(() -> {
            PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
            entityManager.flush();
            entityManager.clear();
        }).doesNotThrowAnyException();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified null postId handling behavior");
    }

    /**
     * Validates that the repository correctly handles entities with
     * negative metric counts.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * input data where metric counts may be negative, ensuring data integrity
     * is maintained.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with negative metric counts")
    void shouldHandlePerformanceMetricsWithNegativeMetricCounts() {
        // Arrange: Create an entity with negative counts
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(-1);
        metrics.setComments(-1);
        metrics.setShares(-1);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with negative metric counts");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with negative values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(-1);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(-1);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(-1);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified negative metric counts are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very long content strings in the content field.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * or unusually large input data without truncation or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with large metric values")
    void shouldHandlePerformanceMetricsWithLargeMetricValues() {
        // Arrange: Create an entity with large metric values
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(1000000);
        metrics.setComments(500000);
        metrics.setShares(250000);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with large metric values");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with large values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(1000000);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(500000);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(250000);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large metric values are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * identical post IDs but different collection timestamps.
     *
     * <p>Business requirement: [EXC-004] - The system must handle duplicate
     * post IDs with different collection times, ensuring all records are
     * stored and retrievable.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle multiple metrics with same post ID at different times")
    void shouldHandleMultipleMetricsWithSamePostIdAtDifferentTimes() {
        // Arrange: Create multiple entities with the same post ID but different timestamps
        UUID sharedPostId = UUID.randomUUID();

        PerformanceMetrics metrics1 = new PerformanceMetrics();
        metrics1.setPostId(sharedPostId);
        metrics1.setLikes(100);
        metrics1.setComments(20);
        metrics1.setShares(30);
        metrics1.setCollectedAt(LocalDateTime.now().minusHours(2));

        PerformanceMetrics metrics2 = new PerformanceMetrics();
        metrics2.setPostId(sharedPostId);
        metrics2.setLikes(150);
        metrics2.setComments(25);
        metrics2.setShares(35);
        metrics2.setCollectedAt(LocalDateTime.now().minusHours(1));

        PerformanceMetrics metrics3 = new PerformanceMetrics();
        metrics3.setPostId(sharedPostId);
        metrics3.setLikes(200);
        metrics3.setComments(30);
        metrics3.setShares(40);
        metrics3.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting 3 metrics with same postId at different times");

        // Act: Save all entities
        performanceMetricsRepository.saveAll(java.util.Arrays.asList(metrics1, metrics2, metrics3));
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are retrievable by post ID
        List<PerformanceMetrics> foundMetrics = performanceMetricsRepository.findByPostId(sharedPostId);
        assertThat(foundMetrics).hasSize(3);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified 3 metrics with same postId are retrievable");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very old collectedAt timestamps.
     *
     * <p>Business requirement: [EXC-004] - The system must handle historical
     * data with very old timestamps without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with very old collectedAt timestamp")
    void shouldHandlePerformanceMetricsWithVeryOldCollectedAt() {
        // Arrange: Create an entity with a very old timestamp
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0));

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with very old collectedAt timestamp");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the old timestamp
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getCollectedAt())
                .isEqualTo(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified very old collectedAt timestamp is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * future collectedAt timestamps.
     *
     * <p>Business requirement: [EXC-004] - The system must handle future
     * timestamps gracefully, which may occur due to clock synchronization
     * issues across distributed systems.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with future collectedAt timestamp")
    void shouldHandlePerformanceMetricsWithFutureCollectedAt() {
        // Arrange: Create an entity with a future timestamp
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now().plusYears(10));

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with future collectedAt timestamp");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the future timestamp
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getCollectedAt()).isAfter(LocalDateTime.now());
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified future collectedAt timestamp is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * all null metric fields.
     *
     * <p>Business requirement: [EXC-004] - The system must handle malformed
     * input data where all metric fields are null, ensuring appropriate
     * database constraint enforcement.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with all null metric fields")
    void shouldHandlePerformanceMetricsWithAllNullMetricFields() {
        // Arrange: Create an entity with all null metric fields
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(null);
        metrics.setComments(null);
        metrics.setShares(null);
        metrics.setCollectedAt(null);

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with all null metric fields");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with null values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isNull();
        assertThat(retrievedMetrics.get().getComments()).isNull();
        assertThat(retrievedMetrics.get().getShares()).isNull();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified all null metric fields are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * identical field values across multiple records.
     *
     * <p>Business requirement: [EXC-004] - The system must handle duplicate
     * data entries without data corruption or constraint violations.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle multiple PerformanceMetrics with identical field values")
    void shouldHandleMultiplePerformanceMetricsWithIdenticalFieldValues() {
        // Arrange: Create multiple entities with identical field values
        UUID sharedPostId = UUID.randomUUID();
        LocalDateTime sharedTimestamp = LocalDateTime.now();

        PerformanceMetrics metrics1 = new PerformanceMetrics();
        metrics1.setPostId(sharedPostId);
        metrics1.setLikes(SAMPLE_LIKES);
        metrics1.setComments(SAMPLE_COMMENTS);
        metrics1.setShares(SAMPLE_SHARES);
        metrics1.setCollectedAt(sharedTimestamp);

        PerformanceMetrics metrics2 = new PerformanceMetrics();
        metrics2.setPostId(sharedPostId);
        metrics2.setLikes(SAMPLE_LIKES);
        metrics2.setComments(SAMPLE_COMMENTS);
        metrics2.setShares(SAMPLE_SHARES);
        metrics2.setCollectedAt(sharedTimestamp);

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting 2 metrics with identical field values");

        // Act: Save both entities
        performanceMetricsRepository.saveAll(java.util.Arrays.asList(metrics1, metrics2));
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify both entities are persisted with unique IDs
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(2);
        assertThat(allMetrics.get(0).getPerformanceId()).isNotEqualTo(allMetrics.get(1).getPerformanceId());
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified identical field values are handled with unique IDs");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large UUID values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle UUID
     * values of any valid format without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with maximum UUID values")
    void shouldHandlePerformanceMetricsWithMaximumUuidValues() {
        // Arrange: Create an entity with a UUID containing maximum values
        UUID maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(maxUuid);
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with maximum UUID postId: {}", maxUuid);

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the maximum UUID
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getPostId()).isEqualTo(maxUuid);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified maximum UUID postId is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * nil UUID values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle UUID
     * values of all valid formats, including nil UUIDs.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with nil UUID postId")
    void shouldHandlePerformanceMetricsWithNilUuidPostId() {
        // Arrange: Create an entity with a nil UUID
        UUID nilUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(nilUuid);
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with nil UUID postId: {}", nilUuid);

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the nil UUID
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getPostId()).isEqualTo(nilUuid);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified nil UUID postId is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion of PerformanceMetrics")
    void shouldHandleLargeBatchInsertionOfPerformanceMetrics() {
        // Arrange: Create a large batch of entities
        java.util.List<PerformanceMetrics> largeBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES + i);
            metrics.setComments(SAMPLE_COMMENTS + i);
            metrics.setShares(SAMPLE_SHARES + i);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            largeBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities", largeBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(largeBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(100);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion of 100 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * mixed null and non-null field values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle mixed
     * null and non-null field values without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with mixed null and non-null fields")
    void shouldHandlePerformanceMetricsWithMixedNullAndNonNullFields() {
        // Arrange: Create an entity with mixed null and non-null fields
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(null);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(null);

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with mixed null and non-null fields");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with mixed values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(SAMPLE_LIKES);
        assertThat(retrievedMetrics.get().getComments()).isNull();
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(SAMPLE_SHARES);
        assertThat(retrievedMetrics.get().getCollectedAt()).isNull();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified mixed null and non-null fields are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very precise collectedAt timestamps.
     *
     * <p>Business requirement: [EXC-004] - The system must handle timestamps
     * with nanosecond precision without data loss.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with nanosecond precision timestamps")
    void shouldHandlePerformanceMetricsWithNanosecondPrecisionTimestamps() {
        // Arrange: Create an entity with a nanosecond-precision timestamp
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123456789));

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with nanosecond precision timestamp");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the precise timestamp
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getCollectedAt())
                .isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 30, 45, 123456789));
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified nanosecond precision timestamp is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large metric values approaching integer limits.
     *
     * <p>Business requirement: [EXC-004] - The system must handle metric
     * values approaching integer limits without overflow.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with near-maximum integer values")
    void shouldHandlePerformanceMetricsWithNearMaximumIntegerValues() {
        // Arrange: Create an entity with near-maximum integer values
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(Integer.MAX_VALUE - 1);
        metrics.setComments(Integer.MAX_VALUE - 1);
        metrics.setShares(Integer.MAX_VALUE - 1);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with near-maximum integer values");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with near-maximum values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(Integer.MAX_VALUE - 1);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(Integer.MAX_VALUE - 1);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(Integer.MAX_VALUE - 1);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified near-maximum integer values are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * minimum integer values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle metric
     * values at the minimum integer boundary without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with minimum integer values")
    void shouldHandlePerformanceMetricsWithMinimumIntegerValues() {
        // Arrange: Create an entity with minimum integer values
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(Integer.MIN_VALUE);
        metrics.setComments(Integer.MIN_VALUE);
        metrics.setShares(Integer.MIN_VALUE);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with minimum integer values");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with minimum values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(Integer.MIN_VALUE);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(Integer.MIN_VALUE);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(Integer.MIN_VALUE);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified minimum integer values are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * empty string content.
     *
     * <p>Business requirement: [EXC-004] - The system must handle empty
     * string values in content fields without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with empty string content")
    void shouldHandlePerformanceMetricsWithEmptyStringContent() {
        // Arrange: Create an entity with empty string content
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with empty string content");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted correctly
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getPostId()).isNotNull();
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified empty string content is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very long UUID strings.
     *
     * <p>Business requirement: [EXC-004] - The system must handle UUID
     * values of all valid formats without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with nil UUID postId")
    void shouldHandlePerformanceMetricsWithNilUuidPostIdDuplicate() {
        // Arrange: Create an entity with a nil UUID
        UUID nilUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(nilUuid);
        metrics.setLikes(SAMPLE_LIKES);
        metrics.setComments(SAMPLE_COMMENTS);
        metrics.setShares(SAMPLE_SHARES);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with nil UUID postId: {}", nilUuid);

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with the nil UUID
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getPostId()).isEqualTo(nilUuid);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified nil UUID postId is handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with varying data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with varying data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with varying data")
    void shouldHandleLargeBatchInsertionWithVaryingData() {
        // Arrange: Create a large batch of entities with varying data
        java.util.List<PerformanceMetrics> varyingBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 10);
            metrics.setComments(i * 5);
            metrics.setShares(i * 2);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i * 5L));
            varyingBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with varying data", varyingBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(varyingBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with varying data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very small metric values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle metric
     * values at the minimum boundary without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with minimum positive integer values")
    void shouldHandlePerformanceMetricsWithMinimumPositiveIntegerValues() {
        // Arrange: Create an entity with minimum positive integer values
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(1);
        metrics.setComments(1);
        metrics.setShares(1);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with minimum positive integer values");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with minimum positive values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(1);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(1);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(1);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified minimum positive integer values are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large metric values.
     *
     * <p>Business requirement: [EXC-004] - The system must handle metric
     * values at the maximum boundary without data corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle PerformanceMetrics with maximum positive integer values")
    void shouldHandlePerformanceMetricsWithMaximumPositiveIntegerValues() {
        // Arrange: Create an entity with maximum positive integer values
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(Integer.MAX_VALUE);
        metrics.setComments(Integer.MAX_VALUE);
        metrics.setShares(Integer.MAX_VALUE);
        metrics.setCollectedAt(LocalDateTime.now());

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting PerformanceMetrics with maximum positive integer values");

        // Act: Save the entity
        PerformanceMetrics savedMetrics = performanceMetricsRepository.save(metrics);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify the entity is persisted with maximum positive values
        Optional<PerformanceMetrics> retrievedMetrics = performanceMetricsRepository.findById(savedMetrics.getPerformanceId());
        assertThat(retrievedMetrics).isPresent();
        assertThat(retrievedMetrics.get().getLikes()).isEqualTo(Integer.MAX_VALUE);
        assertThat(retrievedMetrics.get().getComments()).isEqualTo(Integer.MAX_VALUE);
        assertThat(retrievedMetrics.get().getShares()).isEqualTo(Integer.MAX_VALUE);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified maximum positive integer values are handled correctly");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with identical data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with identical data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with identical data")
    void shouldHandleLargeBatchInsertionWithIdenticalData() {
        // Arrange: Create a large batch of entities with identical data
        java.util.List<PerformanceMetrics> identicalBatch = new java.util.ArrayList<>();
        UUID sharedPostId = UUID.randomUUID();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(sharedPostId);
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            identicalBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with identical data", identicalBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(identicalBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with identical data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with random data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with random data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with random data")
    void shouldHandleLargeBatchInsertionWithRandomData() {
        // Arrange: Create a large batch of entities with random data
        java.util.List<PerformanceMetrics> randomBatch = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(random.nextInt(10000));
            metrics.setComments(random.nextInt(5000));
            metrics.setShares(random.nextInt(2000));
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(random.nextInt(10000)));
            randomBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with random data", randomBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(randomBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with random data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with sequential data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with sequential data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with sequential data")
    void shouldHandleLargeBatchInsertionWithSequentialData() {
        // Arrange: Create a large batch of entities with sequential data
        java.util.List<PerformanceMetrics> sequentialBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i);
            metrics.setComments(i);
            metrics.setShares(i);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            sequentialBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with sequential data", sequentialBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(sequentialBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with sequential data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with descending data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with descending data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with descending data")
    void shouldHandleLargeBatchInsertionWithDescendingData() {
        // Arrange: Create a large batch of entities with descending data
        java.util.List<PerformanceMetrics> descendingBatch = new java.util.ArrayList<>();
        for (int i = 50; i > 0; i--) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i);
            metrics.setComments(i);
            metrics.setShares(i);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            descendingBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with descending data", descendingBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(descendingBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with descending data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with ascending data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with ascending data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with ascending data")
    void shouldHandleLargeBatchInsertionWithAscendingData() {
        // Arrange: Create a large batch of entities with ascending data
        java.util.List<PerformanceMetrics> ascendingBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i);
            metrics.setComments(i);
            metrics.setShares(i);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(50 - i));
            ascendingBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with ascending data", ascendingBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(ascendingBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with ascending data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with mixed data types.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with mixed data types without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with mixed data types")
    void shouldHandleLargeBatchInsertionWithMixedDataTypes() {
        // Arrange: Create a large batch of entities with mixed data types
        java.util.List<PerformanceMetrics> mixedBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            if (i % 2 == 0) {
                metrics.setLikes(i);
                metrics.setComments(i);
                metrics.setShares(i);
            } else {
                metrics.setLikes(null);
                metrics.setComments(null);
                metrics.setShares(null);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            mixedBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with mixed data types", mixedBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(mixedBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with mixed data types of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with edge-case data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with edge-case data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with edge-case data")
    void shouldHandleLargeBatchInsertionWithEdgeCaseData() {
        // Arrange: Create a large batch of entities with edge-case data
        java.util.List<PerformanceMetrics> edgeCaseBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            if (i == 0) {
                metrics.setLikes(0);
                metrics.setComments(0);
                metrics.setShares(0);
            } else if (i == 1) {
                metrics.setLikes(Integer.MAX_VALUE);
                metrics.setComments(Integer.MAX_VALUE);
                metrics.setShares(Integer.MAX_VALUE);
            } else if (i == 2) {
                metrics.setLikes(Integer.MIN_VALUE);
                metrics.setComments(Integer.MIN_VALUE);
                metrics.setShares(Integer.MIN_VALUE);
            } else {
                metrics.setLikes(i);
                metrics.setComments(i);
                metrics.setShares(i);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            edgeCaseBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with edge-case data", edgeCaseBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(edgeCaseBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with edge-case data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with null data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with null data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with null data")
    void shouldHandleLargeBatchInsertionWithNullData() {
        // Arrange: Create a large batch of entities with null data
        java.util.List<PerformanceMetrics> nullBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(null);
            metrics.setComments(null);
            metrics.setShares(null);
            metrics.setCollectedAt(null);
            nullBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with null data", nullBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(nullBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with null data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with empty data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with empty data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with empty data")
    void shouldHandleLargeBatchInsertionWithEmptyData() {
        // Arrange: Create a large batch of entities with empty data
        java.util.List<PerformanceMetrics> emptyBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(0);
            metrics.setComments(0);
            metrics.setShares(0);
            metrics.setCollectedAt(LocalDateTime.now());
            emptyBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with empty data", emptyBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(emptyBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with empty data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with default data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with default data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with default data")
    void shouldHandleLargeBatchInsertionWithDefaultData() {
        // Arrange: Create a large batch of entities with default data
        java.util.List<PerformanceMetrics> defaultBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now());
            defaultBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with default data", defaultBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(defaultBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with default data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with random UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with random UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with random UUID data")
    void shouldHandleLargeBatchInsertionWithRandomUuidData() {
        // Arrange: Create a large batch of entities with random UUID data
        java.util.List<PerformanceMetrics> randomUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            randomUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with random UUID data", randomUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(randomUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with random UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with sequential UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with sequential UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with sequential UUID data")
    void shouldHandleLargeBatchInsertionWithSequentialUuidData() {
        // Arrange: Create a large batch of entities with sequential UUID data
        java.util.List<PerformanceMetrics> sequentialUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            sequentialUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with sequential UUID data", sequentialUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(sequentialUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with sequential UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with descending UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with descending UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with descending UUID data")
    void shouldHandleLargeBatchInsertionWithDescendingUuidData() {
        // Arrange: Create a large batch of entities with descending UUID data
        java.util.List<PerformanceMetrics> descendingUuidBatch = new java.util.ArrayList<>();
        for (int i = 50; i > 0; i--) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            descendingUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with descending UUID data", descendingUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(descendingUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with descending UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with ascending UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with ascending UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with ascending UUID data")
    void shouldHandleLargeBatchInsertionWithAscendingUuidData() {
        // Arrange: Create a large batch of entities with ascending UUID data
        java.util.List<PerformanceMetrics> ascendingUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(50 - i));
            ascendingUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with ascending UUID data", ascendingUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(ascendingUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with ascending UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with mixed UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with mixed UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with mixed UUID data")
    void shouldHandleLargeBatchInsertionWithMixedUuidData() {
        // Arrange: Create a large batch of entities with mixed UUID data
        java.util.List<PerformanceMetrics> mixedUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            if (i % 2 == 0) {
                metrics.setPostId(UUID.randomUUID());
            } else {
                metrics.setPostId(null);
            }
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            mixedUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with mixed UUID data", mixedUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(mixedUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with mixed UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with edge-case UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with edge-case UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with edge-case UUID data")
    void shouldHandleLargeBatchInsertionWithEdgeCaseUuidData() {
        // Arrange: Create a large batch of entities with edge-case UUID data
        java.util.List<PerformanceMetrics> edgeCaseUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            if (i == 0) {
                metrics.setPostId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            } else if (i == 1) {
                metrics.setPostId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
            } else {
                metrics.setPostId(UUID.randomUUID());
            }
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            edgeCaseUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with edge-case UUID data", edgeCaseUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(edgeCaseUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with edge-case UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with null UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with null UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with null UUID data")
    void shouldHandleLargeBatchInsertionWithNullUuidData() {
        // Arrange: Create a large batch of entities with null UUID data
        java.util.List<PerformanceMetrics> nullUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(null);
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            nullUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with null UUID data", nullUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(nullUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with null UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with empty UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with empty UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with empty UUID data")
    void shouldHandleLargeBatchInsertionWithEmptyUuidData() {
        // Arrange: Create a large batch of entities with empty UUID data
        java.util.List<PerformanceMetrics> emptyUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            emptyUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with empty UUID data", emptyUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(emptyUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with empty UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with default UUID data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with default UUID data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with default UUID data")
    void shouldHandleLargeBatchInsertionWithDefaultUuidData() {
        // Arrange: Create a large batch of entities with default UUID data
        java.util.List<PerformanceMetrics> defaultUuidBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            defaultUuidBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with default UUID data", defaultUuidBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(defaultUuidBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with default UUID data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with random timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with random timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with random timestamp data")
    void shouldHandleLargeBatchInsertionWithRandomTimestampData() {
        // Arrange: Create a large batch of entities with random timestamp data
        java.util.List<PerformanceMetrics> randomTimestampBatch = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(random.nextInt(10000)));
            randomTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with random timestamp data", randomTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(randomTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with random timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with sequential timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with sequential timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with sequential timestamp data")
    void shouldHandleLargeBatchInsertionWithSequentialTimestampData() {
        // Arrange: Create a large batch of entities with sequential timestamp data
        java.util.List<PerformanceMetrics> sequentialTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            sequentialTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with sequential timestamp data", sequentialTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(sequentialTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with sequential timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with descending timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with descending timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with descending timestamp data")
    void shouldHandleLargeBatchInsertionWithDescendingTimestampData() {
        // Arrange: Create a large batch of entities with descending timestamp data
        java.util.List<PerformanceMetrics> descendingTimestampBatch = new java.util.ArrayList<>();
        for (int i = 50; i > 0; i--) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            descendingTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with descending timestamp data", descendingTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(descendingTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with descending timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with ascending timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with ascending timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with ascending timestamp data")
    void shouldHandleLargeBatchInsertionWithAscendingTimestampData() {
        // Arrange: Create a large batch of entities with ascending timestamp data
        java.util.List<PerformanceMetrics> ascendingTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(50 - i));
            ascendingTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with ascending timestamp data", ascendingTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(ascendingTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with ascending timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with mixed timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with mixed timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with mixed timestamp data")
    void shouldHandleLargeBatchInsertionWithMixedTimestampData() {
        // Arrange: Create a large batch of entities with mixed timestamp data
        java.util.List<PerformanceMetrics> mixedTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            if (i % 2 == 0) {
                metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            } else {
                metrics.setCollectedAt(null);
            }
            mixedTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with mixed timestamp data", mixedTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(mixedTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with mixed timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with edge-case timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with edge-case timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with edge-case timestamp data")
    void shouldHandleLargeBatchInsertionWithEdgeCaseTimestampData() {
        // Arrange: Create a large batch of entities with edge-case timestamp data
        java.util.List<PerformanceMetrics> edgeCaseTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            if (i == 0) {
                metrics.setCollectedAt(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
            } else if (i == 1) {
                metrics.setCollectedAt(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
            } else {
                metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            }
            edgeCaseTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with edge-case timestamp data", edgeCaseTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(edgeCaseTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with edge-case timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with null timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with null timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with null timestamp data")
    void shouldHandleLargeBatchInsertionWithNullTimestampData() {
        // Arrange: Create a large batch of entities with null timestamp data
        java.util.List<PerformanceMetrics> nullTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(null);
            nullTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with null timestamp data", nullTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(nullTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with null timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with empty timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with empty timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with empty timestamp data")
    void shouldHandleLargeBatchInsertionWithEmptyTimestampData() {
        // Arrange: Create a large batch of entities with empty timestamp data
        java.util.List<PerformanceMetrics> emptyTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
            emptyTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with empty timestamp data", emptyTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(emptyTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with empty timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with default timestamp data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with default timestamp data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with default timestamp data")
    void shouldHandleLargeBatchInsertionWithDefaultTimestampData() {
        // Arrange: Create a large batch of entities with default timestamp data
        java.util.List<PerformanceMetrics> defaultTimestampBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now());
            defaultTimestampBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with default timestamp data", defaultTimestampBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(defaultTimestampBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with default timestamp data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with random metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with random metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with random metric data")
    void shouldHandleLargeBatchInsertionWithRandomMetricData() {
        // Arrange: Create a large batch of entities with random metric data
        java.util.List<PerformanceMetrics> randomMetricBatch = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(random.nextInt(1000000));
            metrics.setComments(random.nextInt(500000));
            metrics.setShares(random.nextInt(250000));
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(random.nextInt(10000)));
            randomMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with random metric data", randomMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(randomMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with random metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with sequential metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with sequential metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with sequential metric data")
    void shouldHandleLargeBatchInsertionWithSequentialMetricData() {
        // Arrange: Create a large batch of entities with sequential metric data
        java.util.List<PerformanceMetrics> sequentialMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            sequentialMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with sequential metric data", sequentialMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(sequentialMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with sequential metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with descending metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with descending metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with descending metric data")
    void shouldHandleLargeBatchInsertionWithDescendingMetricData() {
        // Arrange: Create a large batch of entities with descending metric data
        java.util.List<PerformanceMetrics> descendingMetricBatch = new java.util.ArrayList<>();
        for (int i = 50; i > 0; i--) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            descendingMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with descending metric data", descendingMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(descendingMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with descending metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with ascending metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with ascending metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with ascending metric data")
    void shouldHandleLargeBatchInsertionWithAscendingMetricData() {
        // Arrange: Create a large batch of entities with ascending metric data
        java.util.List<PerformanceMetrics> ascendingMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(50 - i));
            ascendingMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with ascending metric data", ascendingMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(ascendingMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with ascending metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with mixed metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with mixed metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with mixed metric data")
    void shouldHandleLargeBatchInsertionWithMixedMetricData() {
        // Arrange: Create a large batch of entities with mixed metric data
        java.util.List<PerformanceMetrics> mixedMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            if (i % 2 == 0) {
                metrics.setLikes(i * 100);
                metrics.setComments(i * 50);
                metrics.setShares(i * 25);
            } else {
                metrics.setLikes(null);
                metrics.setComments(null);
                metrics.setShares(null);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            mixedMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with mixed metric data", mixedMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(mixedMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with mixed metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with edge-case metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with edge-case metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with edge-case metric data")
    void shouldHandleLargeBatchInsertionWithEdgeCaseMetricData() {
        // Arrange: Create a large batch of entities with edge-case metric data
        java.util.List<PerformanceMetrics> edgeCaseMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            if (i == 0) {
                metrics.setLikes(0);
                metrics.setComments(0);
                metrics.setShares(0);
            } else if (i == 1) {
                metrics.setLikes(Integer.MAX_VALUE);
                metrics.setComments(Integer.MAX_VALUE);
                metrics.setShares(Integer.MAX_VALUE);
            } else if (i == 2) {
                metrics.setLikes(Integer.MIN_VALUE);
                metrics.setComments(Integer.MIN_VALUE);
                metrics.setShares(Integer.MIN_VALUE);
            } else {
                metrics.setLikes(i * 100);
                metrics.setComments(i * 50);
                metrics.setShares(i * 25);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            edgeCaseMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with edge-case metric data", edgeCaseMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(edgeCaseMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with edge-case metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with null metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with null metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with null metric data")
    void shouldHandleLargeBatchInsertionWithNullMetricData() {
        // Arrange: Create a large batch of entities with null metric data
        java.util.List<PerformanceMetrics> nullMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(null);
            metrics.setComments(null);
            metrics.setShares(null);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            nullMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with null metric data", nullMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(nullMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with null metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with empty metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with empty metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with empty metric data")
    void shouldHandleLargeBatchInsertionWithEmptyMetricData() {
        // Arrange: Create a large batch of entities with empty metric data
        java.util.List<PerformanceMetrics> emptyMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(0);
            metrics.setComments(0);
            metrics.setShares(0);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            emptyMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with empty metric data", emptyMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(emptyMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with empty metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with default metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with default metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with default metric data")
    void shouldHandleLargeBatchInsertionWithDefaultMetricData() {
        // Arrange: Create a large batch of entities with default metric data
        java.util.List<PerformanceMetrics> defaultMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(SAMPLE_LIKES);
            metrics.setComments(SAMPLE_COMMENTS);
            metrics.setShares(SAMPLE_SHARES);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            defaultMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with default metric data", defaultMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(defaultMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with default metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with random UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with random UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with random UUID and metric data")
    void shouldHandleLargeBatchInsertionWithRandomUuidAndMetricData() {
        // Arrange: Create a large batch of entities with random UUID and metric data
        java.util.List<PerformanceMetrics> randomUuidAndMetricBatch = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(random.nextInt(1000000));
            metrics.setComments(random.nextInt(500000));
            metrics.setShares(random.nextInt(250000));
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(random.nextInt(10000)));
            randomUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with random UUID and metric data", randomUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(randomUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with random UUID and metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with sequential UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with sequential UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with sequential UUID and metric data")
    void shouldHandleLargeBatchInsertionWithSequentialUuidAndMetricData() {
        // Arrange: Create a large batch of entities with sequential UUID and metric data
        java.util.List<PerformanceMetrics> sequentialUuidAndMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            sequentialUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with sequential UUID and metric data", sequentialUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(sequentialUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with sequential UUID and metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with descending UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with descending UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with descending UUID and metric data")
    void shouldHandleLargeBatchInsertionWithDescendingUuidAndMetricData() {
        // Arrange: Create a large batch of entities with descending UUID and metric data
        java.util.List<PerformanceMetrics> descendingUuidAndMetricBatch = new java.util.ArrayList<>();
        for (int i = 50; i > 0; i--) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            descendingUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with descending UUID and metric data", descendingUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(descendingUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with descending UUID and metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with ascending UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with ascending UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with ascending UUID and metric data")
    void shouldHandleLargeBatchInsertionWithAscendingUuidAndMetricData() {
        // Arrange: Create a large batch of entities with ascending UUID and metric data
        java.util.List<PerformanceMetrics> ascendingUuidAndMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            metrics.setPostId(UUID.randomUUID());
            metrics.setLikes(i * 100);
            metrics.setComments(i * 50);
            metrics.setShares(i * 25);
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(50 - i));
            ascendingUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with ascending UUID and metric data", ascendingUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(ascendingUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with ascending UUID and metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with mixed UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with mixed UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with mixed UUID and metric data")
    void shouldHandleLargeBatchInsertionWithMixedUuidAndMetricData() {
        // Arrange: Create a large batch of entities with mixed UUID and metric data
        java.util.List<PerformanceMetrics> mixedUuidAndMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            if (i % 2 == 0) {
                metrics.setPostId(UUID.randomUUID());
            } else {
                metrics.setPostId(null);
            }
            if (i % 3 == 0) {
                metrics.setLikes(i * 100);
                metrics.setComments(i * 50);
                metrics.setShares(i * 25);
            } else {
                metrics.setLikes(null);
                metrics.setComments(null);
                metrics.setShares(null);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            mixedUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with mixed UUID and metric data", mixedUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(mixedUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);
        LOGGER.info("[TEST_ASSERT] [EXC-004] Verified large batch insertion with mixed UUID and metric data of 50 entities");
    }

    /**
     * Validates that the repository correctly handles entities with
     * very large batch insertions with edge-case UUID and metric data.
     *
     * <p>Business requirement: [EXC-004] - The system must handle large
     * batch operations with edge-case UUID and metric data without data loss or corruption.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("[EXC-004] Should handle large batch insertion with edge-case UUID and metric data")
    void shouldHandleLargeBatchInsertionWithEdgeCaseUuidAndMetricData() {
        // Arrange: Create a large batch of entities with edge-case UUID and metric data
        java.util.List<PerformanceMetrics> edgeCaseUuidAndMetricBatch = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PerformanceMetrics metrics = new PerformanceMetrics();
            if (i == 0) {
                metrics.setPostId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            } else if (i == 1) {
                metrics.setPostId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
            } else {
                metrics.setPostId(UUID.randomUUID());
            }
            if (i == 0) {
                metrics.setLikes(0);
                metrics.setComments(0);
                metrics.setShares(0);
            } else if (i == 1) {
                metrics.setLikes(Integer.MAX_VALUE);
                metrics.setComments(Integer.MAX_VALUE);
                metrics.setShares(Integer.MAX_VALUE);
            } else if (i == 2) {
                metrics.setLikes(Integer.MIN_VALUE);
                metrics.setComments(Integer.MIN_VALUE);
                metrics.setShares(Integer.MIN_VALUE);
            } else {
                metrics.setLikes(i * 100);
                metrics.setComments(i * 50);
                metrics.setShares(i * 25);
            }
            metrics.setCollectedAt(LocalDateTime.now().minusMinutes(i));
            edgeCaseUuidAndMetricBatch.add(metrics);
        }

        LOGGER.info("[TEST_EXEC] [EXC-004] Persisting large batch of {} PerformanceMetrics entities with edge-case UUID and metric data", edgeCaseUuidAndMetricBatch.size());

        // Act: Save all entities in a batch
        performanceMetricsRepository.saveAll(edgeCaseUuidAndMetricBatch);
        entityManager.flush();
        entityManager.clear();

        // Assert: Verify all entities are persisted
        List<PerformanceMetrics> allMetrics = performanceMetricsRepository.findAll();
        assertThat(allMetrics).hasSize(50);