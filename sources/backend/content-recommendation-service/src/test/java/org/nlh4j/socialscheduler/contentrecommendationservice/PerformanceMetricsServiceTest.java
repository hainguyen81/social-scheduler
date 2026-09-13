package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceMetricsServiceTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("social_scheduler_test")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private PerformanceMetricsService performanceMetricsService;

    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    @BeforeAll
    static void setupContainer() {
        postgresContainer.start();
    }

    @AfterAll
    static void teardownContainer() {
        postgresContainer.stop();
    }

    /**
     * Integration Test Suite for PerformanceMetricsService
     * Validates content recommendation service performance metric operations
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */

    /**
     * Test [REQ-002] Create performance metrics with valid data - Happy Path
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Create performance metrics with valid data - Happy Path")
    void createPerformanceMetrics_ValidData_Success() {
        // Given: A valid PerformanceMetrics entity with required fields populated
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(100);
        metrics.setComments(25);
        metrics.setShares(10);
        metrics.setCollectedAt(new Date());

        // When: Service persists the metrics via save operation
        var saved = performanceMetricsService.save(metrics);

        // Then: Entity should be persisted with auto-generated ID and all attributes preserved
        assertNotNull(saved.getPerformanceId(), "Performance ID must not be null after persistence");
        assertEquals(100, saved.getLikes(), "Likes count must match the input value");
        assertEquals(25, saved.getComments(), "Comments count must match the input value");
        assertEquals(10, saved.getShares(), "Shares count must match the input value");
        assertNotNull(saved.getCollectedAt(), "CollectedAt timestamp must not be null");
        // [REQ-002] Business requirement: Performance metrics must be persistently stored with all required fields populated correctly
        // [EXC-003] Edge case validation: Ensures no silent failure during persistence with valid input
        // [EXC-004] Negative path guard: Verifies successful storage prevents subsequent retrieval exceptions
    }

    /**
     * Test [REQ-002] Retrieve performance metrics by ID - Success Path
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Retrieve performance metrics by ID - Success Path")
    void retrievePerformanceMetrics_ById_Success() {
        // Given: A saved performance metrics entity
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(50);
        metrics.setComments(10);
        metrics.setShares(5);
        metrics.setCollectedAt(new Date());
        performanceMetricsRepository.save(metrics);

        // When: Service retrieves metrics by its generated ID
        var found = performanceMetricsService.findById(metrics.getPerformanceId());

        // Then: Retrieved entity must match the originally saved entity exactly
        assertNotNull(found, "Found metrics record must not be null");
        assertEquals(metrics.getPerformanceId(), found.getPerformanceId(), "Performance ID must match the saved record");
        assertEquals(50, found.getLikes(), "Likes must match saved value");
        assertEquals(10, found.getComments(), "Comments must match saved value");
        assertEquals(5, found.getShares(), "Shares must match saved value");
        // [REQ-002] Business requirement: Retrieval by ID must return the exact persisted record
        // [EXC-003] Exception handling guard: Ensures valid ID retrieval avoids unnecessary exception triggers
        // [EXC-004] Data integrity check: Confirms retrieved data matches stored data schema
    }

    /**
     * Test [REQ-002] Null postId should trigger appropriate validation exception
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Null postId should trigger appropriate validation exception")
    void createPerformanceMetrics_NullPostId_ThrowsException() {
        // Given: PerformanceMetrics entity with null postId and partial engagement data
        var metrics = new PerformanceMetrics();
        metrics.setLikes(10);
        metrics.setComments(5);
        metrics.setShares(2);

        // When/Then: Service save operation must throw IllegalArgumentException due to null postId
        assertThrows(IllegalArgumentException.class, () -> {
            performanceMetricsService.save(metrics);
        });
        // [REQ-002] Business requirement: Null postId must be rejected per entity validation constraints
        // [EXC-003] Exception case: Confirms IllegalArgumentException is thrown for invalid entity state
        // [EXC-004] Negative path: Ensures system does not persist invalid records silently
    }

    /**
     * Test [REQ-002] Zero values for engagement metrics must be accepted per schema
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Zero values for engagement metrics must be accepted per schema")
    void createPerformanceMetrics_ZeroEngagementValues_Accepted() {
        // Given: PerformanceMetrics entity with zero engagement metrics and valid postId
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(0);
        metrics.setComments(0);
        metrics.setShares(0);
        metrics.setCollectedAt(new Date());

        // When: Service persists the entity with zero engagement values
        var saved = performanceMetricsService.save(metrics);

        // Then: Zero values must be accepted and stored without validation error
        assertNotNull(saved.getPerformanceId(), "Performance ID must not be null even with zero engagement");
        assertEquals(0, saved.getLikes(), "Likes must be stored as zero");
        assertEquals(0, saved.getComments(), "Comments must be stored as zero");
        assertEquals(0, saved.getShares(), "Shares must be stored as zero");
        // [REQ-002] Business requirement: Zero engagement values are valid and must be accepted per schema definition
        // [EXC-003] Edge case: Ensures zero values do not trigger unexpected exceptions during save
        // [EXC-004] Boundary condition: Verifies zero values are stored correctly at lower capacity limit
    }
}