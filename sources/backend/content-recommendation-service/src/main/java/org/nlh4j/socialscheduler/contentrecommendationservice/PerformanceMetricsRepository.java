package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test suite for {@link PerformanceMetricsRepository}.
 *
 * <p>This test class validates the full lifecycle of the repository layer,
 * including happy-path CRUD operations, edge-case boundary conditions,
 * and exception-handling negative paths. It uses Mockito to isolate the
 * repository from the underlying database while still exercising the
 * real repository logic.</p>
 *
 * <p>Traceability Tags: [REQ-002], [EXC-003], [EXC-004]</p>
 *
 * @verifies [REQ-002]
 * @verifies [EXC-003]
 * @verifies [EXC-004]
 */
@ExtendWith(MockitoExtension.class)
class PerformanceMetricsRepositoryTest {

    /* -------------------------------------------------------------------------
       Enterprise-wide constants – hoisted to the class crown to satisfy the
       Anti-Magic-Numbers policy and enable centralized configuration.
       ------------------------------------------------------------------------- */
    /** Sample UUID used for test entity identification. */
    private static final UUID TEST_UUID = UUID.fromString("123e4567-e89b-12d6-abcd-123456789012");
    /** Sample post ID used for test entity identification. */
    private static final UUID TEST_POST_ID = UUID.fromString("987fcdeb-51a2-43f1-9876-543210fedcba");
    /** Sample number of likes for test entity. */
    private static final int SAMPLE_LIKES = 150;
    /** Sample number of comments for test entity. */
    private static final int SAMPLE_COMMENTS = 42;
    /** Sample number of shares for test entity. */
    private static final int SAMPLE_SHARES = 87;
    /** Sample collected timestamp for test entity. */
    private static final Date SAMPLE_COLLECTED_AT = new Date();

    /** Standard SLF4J logger – required by the Logging Audit Law. */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsRepositoryTest.class);

    /** Mock of the underlying Spring Data JPA repository. */
    @Mock
    private JpaRepository<PerformanceMetrics, UUID> mockJpaRepository;

    /** The repository under test, instantiated with the mocked JPA repository. */
    private PerformanceMetricsRepository repository;

    /**
     * Sets up the test environment before each test method.
     * Initializes the repository with the mocked JPA repository.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_START] Initializing PerformanceMetricsRepository test context");
        repository = new PerformanceMetricsRepository(mockJpaRepository);
    }

    /**
     * Tears down the test environment after each test method.
     * Releases any resources held during the test.
     */
    @AfterEach
    void tearDown() {
        logger.info("[TEST_END] Cleaning up PerformanceMetricsRepository test context");
        // Release mocks to prevent memory leaks
        Mockito.reset(mockJpaRepository);
    }

    /* =========================================================================
       HAPPY CASE TESTS
       ========================================================================= */

    /**
     * Tests the successful retrieval of a {@link PerformanceMetrics} entity by its ID.
     *
     * <p>Verifies that when a valid UUID is provided and the entity exists in the
     * database, the repository returns an {@link Optional} containing the entity.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("findById should return entity when valid ID is provided [REQ-002]")
    void findById_ShouldReturnEntity_WhenValidIdProvided() {
        logger.info("[TEST_START] [REQ-002] Testing findById with valid ID");

        // Arrange: Create a sample PerformanceMetrics entity
        PerformanceMetrics sampleEntity = createSamplePerformanceMetrics();

        // Mock the JPA repository to return the sample entity
        when(mockJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(sampleEntity));

        // Act: Call the repository method
        Optional<PerformanceMetrics> result = repository.findById(TEST_UUID);

        // Assert: Verify the result contains the expected entity
        assertTrue(result.isPresent(), "Expected entity to be present");
        assertEquals(sampleEntity.getPerformanceId(), result.get().getPerformanceId(),
                "Returned entity ID should match the requested ID");

        logger.info("[TEST_END] [REQ-002] findById happy path test completed successfully");
    }

    /**
     * Tests the successful persistence of a {@link PerformanceMetrics} entity.
     *
     * <p>Verifies that when a valid entity is provided, the repository saves it
     * and returns the saved entity with a non-null ID.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("save should persist and return entity with valid data [REQ-002]")
    void save_ShouldPersistAndReturnEntity_WhenValidEntityProvided() {
        logger.info("[TEST_START] [REQ-002] Testing save with valid entity");

        // Arrange: Create a sample PerformanceMetrics entity
        PerformanceMetrics sampleEntity = createSamplePerformanceMetrics();

        // Mock the JPA repository to return the saved entity
        when(mockJpaRepository.save(any(PerformanceMetrics.class))).thenReturn(sampleEntity);

        // Act: Call the repository method
        PerformanceMetrics savedEntity = repository.save(sampleEntity);

        // Assert: Verify the saved entity is returned correctly
        assertNotNull(savedEntity, "Saved entity should not be null");
        assertEquals(sampleEntity.getPerformanceId(), savedEntity.getPerformanceId(),
                "Saved entity ID should match the original entity ID");

        logger.info("[TEST_END] [REQ-002] save happy path test completed successfully");
    }

    /**
     * Tests the successful deletion of a {@link PerformanceMetrics} entity by its ID.
     *
     * <p>Verifies that when a valid UUID is provided, the repository successfully
     * delegates the deletion to the underlying JPA repository without throwing exceptions.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("deleteById should remove entity when valid ID is provided [REQ-002]")
    void deleteById_ShouldRemoveEntity_WhenValidIdProvided() {
        logger.info("[TEST_START] [REQ-002] Testing deleteById with valid ID");

        // Arrange: No specific arrangement needed for deletion

        // Mock the JPA repository to do nothing on delete
        doNothing().when(mockJpaRepository).deleteById(TEST_UUID);

        // Act & Assert: Call the repository method and verify no exception is thrown
        assertDoesNotThrow(() -> repository.deleteById(TEST_UUID),
                "deleteById should not throw an exception for a valid ID");

        // Verify the JPA repository's deleteById was called with the correct ID
        verify(mockJpaRepository, times(1)).deleteById(TEST_UUID);

        logger.info("[TEST_END] [REQ-002] deleteById happy path test completed successfully");
    }

    /**
     * Tests the successful retrieval of all {@link PerformanceMetrics} entities.
     *
     * <p>Verifies that the repository returns a list of all entities when the
     * underlying JPA repository contains multiple records.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("findAll should return all entities when records exist [REQ-002]")
    void findAll_ShouldReturnAllEntities_WhenRecordsExist() {
        logger.info("[TEST_START] [REQ-002] Testing findAll with existing records");

        // Arrange: Create a list of sample PerformanceMetrics entities
        List<PerformanceMetrics> sampleEntities = Arrays.asList(
                createSamplePerformanceMetrics(),
                createSamplePerformanceMetrics()
        );

        // Mock the JPA repository to return the sample list
        when(mockJpaRepository.findAll()).thenReturn(sampleEntities);

        // Act: Call the repository method
        List<PerformanceMetrics> results = repository.findAll();

        // Assert: Verify the results contain the expected number of entities
        assertNotNull(results, "Results should not be null");
        assertEquals(2, results.size(), "Should return exactly 2 entities");

        logger.info("[TEST_END] [REQ-002] findAll happy path test completed successfully");
    }

    /* =========================================================================
       EDGE CASE & BOUNDARY CONDITION TESTS
       ========================================================================= */

    /**
     * Tests the behavior of {@link PerformanceMetricsRepository#findById(UUID)}
     * when the entity does not exist in the database.
     *
     * <p>Verifies that an empty {@link Optional} is returned when the provided
     * UUID does not correspond to any existing record.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("findById should return empty Optional when entity does not exist [REQ-002]")
    void findById_ShouldReturnEmptyOptional_WhenEntityDoesNotExist() {
        logger.info("[TEST_START] [REQ-002] Testing findById with non-existent ID");

        // Arrange: Mock the JPA repository to return an empty Optional
        when(mockJpaRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

        // Act: Call the repository method
        Optional<PerformanceMetrics> result = repository.findById(TEST_UUID);

        // Assert: Verify the result is an empty Optional
        assertTrue(result.isEmpty(), "Expected empty Optional for non-existent entity");

        logger.info("[TEST_END] [REQ-002] findById edge case test completed successfully");
    }

    /**
     * Tests the behavior of {@link PerformanceMetricsRepository#findAll()}
     * when no records exist in the database.
     *
     * <p>Verifies that an empty list is returned when the underlying JPA
     * repository contains no records.</p>
     *
     * @verifies [REQ-002]
     */
    @Test
    @DisplayName("findAll should return empty list when no records exist [REQ-002]")
    void findAll_ShouldReturnEmptyList_WhenNoRecordsExist() {
        logger.info("[TEST_START] [REQ-002] Testing findAll with no records");

        // Arrange: Mock the JPA repository to return an empty list
        when(mockJpaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act: Call the repository method
        List<PerformanceMetrics> results = repository.findAll();

        // Assert: Verify the results are an empty list
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Expected empty list when no records exist");

        logger.info("[TEST_END] [REQ-002] findAll edge case test completed successfully");
    }

    /**
     * Tests the behavior of {@link PerformanceMetricsRepository#save(PerformanceMetrics)}
     * when a null entity is provided.
     *
     * <p>Verifies that the repository handles null input gracefully by delegating
     * to the underlying JPA repository, which may throw an appropriate exception.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("save should handle null entity gracefully [EXC-004]")
    void save_ShouldHandleNullEntity_Gracefully() {
        logger.info("[TEST_START] [EXC-004] Testing save with null entity");

        // Arrange: Mock the JPA repository to throw an IllegalArgumentException for null input
        when(mockJpaRepository.save(null)).thenThrow(new IllegalArgumentException("Entity must not be null"));

        // Act & Assert: Verify that the repository wraps the exception appropriately
        PerformanceMetricsRepository.PerformanceMetricsRepositoryException exception =
                assertThrows(PerformanceMetricsRepository.PerformanceMetricsRepositoryException.class,
                        () -> repository.save(null),
                        "Expected PerformanceMetricsRepositoryException for null entity");

        // Verify the exception message contains relevant context
        assertTrue(exception.getMessage().contains("Failed to save PerformanceMetrics entity"),
                "Exception message should contain failure context");

        logger.info("[TEST_END] [EXC-004] save null entity edge case test completed successfully");
    }

    /* =========================================================================
       EXCEPTION CASE & NEGATIVE PATH TESTS
       ========================================================================= */

    /**
     * Tests the exception handling behavior of {@link PerformanceMetricsRepository#findById(UUID)}
     * when a {@link DataAccessException} occurs during database access.
     *
     * <p>Verifies that the repository catches the {@link DataAccessException},
     * logs the error with the appropriate traceability tag, and wraps it in a
     * {@link PerformanceMetricsRepository.PerformanceMetricsRepositoryException}.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("findById should wrap DataAccessException in custom exception [EXC-003]")
    void findById_ShouldWrapDataAccessException_WhenDatabaseErrorOccurs() {
        logger.info("[TEST_START] [EXC-003] Testing findById with database access error");

        // Arrange: Mock the JPA repository to throw a DataAccessException
        DataAccessException mockException = new DataAccessException("Simulated database error") {};
        when(mockJpaRepository.findById(TEST_UUID)).thenThrow(mockException);

        // Act & Assert: Verify that the repository wraps the exception appropriately
        PerformanceMetricsRepository.PerformanceMetricsRepositoryException exception =
                assertThrows(PerformanceMetricsRepository.PerformanceMetricsRepositoryException.class,
                        () -> repository.findById(TEST_UUID),
                        "Expected PerformanceMetricsRepositoryException for database error");

        // Verify the exception message contains relevant context
        assertTrue(exception.getMessage().contains("Failed to retrieve PerformanceMetrics with id"),
                "Exception message should contain failure context");

        // Verify the original cause is preserved
        assertNotNull(exception.getCause(), "Original exception cause should be preserved");
        assertEquals(mockException, exception.getCause(),
                "Original DataAccessException should be the cause of the wrapped exception");

        logger.info("[TEST_END] [EXC-003] findById exception handling test completed successfully");
    }

    /**
     * Tests the exception handling behavior of {@link PerformanceMetricsRepository#save(PerformanceMetrics)}
     * when a {@link DataAccessException} occurs during database access.
     *
     * <p>Verifies that the repository catches the {@link DataAccessException},
     * logs the error with the appropriate traceability tag, and wraps it in a
     * {@link PerformanceMetricsRepository.PerformanceMetricsRepositoryException}.</p>
     *
     * @verifies [EXC-004]
     */
    @Test
    @DisplayName("save should wrap DataAccessException in custom exception [EXC-004]")
    void save_ShouldWrapDataAccessException_WhenDatabaseErrorOccurs() {
        logger.info("[TEST_START] [EXC-004] Testing save with database access error");

        // Arrange: Create a sample entity and mock the JPA repository to throw a DataAccessException
        PerformanceMetrics sampleEntity = createSamplePerformanceMetrics();
        DataAccessException mockException = new DataAccessException("Simulated database error") {};
        when(mockJpaRepository.save(any(PerformanceMetrics.class))).thenThrow(mockException);

        // Act & Assert: Verify that the repository wraps the exception appropriately
        PerformanceMetricsRepository.PerformanceMetricsRepositoryException exception =
                assertThrows(PerformanceMetricsRepository.PerformanceMetricsRepositoryException.class,
                        () -> repository.save(sampleEntity),
                        "Expected PerformanceMetricsRepositoryException for database error");

        // Verify the exception message contains relevant context
        assertTrue(exception.getMessage().contains("Failed to save PerformanceMetrics entity"),
                "Exception message should contain failure context");

        // Verify the original cause is preserved
        assertNotNull(exception.getCause(), "Original exception cause should be preserved");
        assertEquals(mockException, exception.getCause(),
                "Original DataAccessException should be the cause of the wrapped exception");

        logger.info("[TEST_END] [EXC-004] save exception handling test completed successfully");
    }

    /**
     * Tests the exception handling behavior of {@link PerformanceMetricsRepository#deleteById(UUID)}
     * when a {@link DataAccessException} occurs during database access.
     *
     * <p>Verifies that the repository catches the {@link DataAccessException},
     * logs the error with the appropriate traceability tag, and wraps it in a
     * {@link PerformanceMetricsRepository.PerformanceMetricsRepositoryException}.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("deleteById should wrap DataAccessException in custom exception [EXC-003]")
    void deleteById_ShouldWrapDataAccessException_WhenDatabaseErrorOccurs() {
        logger.info("[TEST_START] [EXC-003] Testing deleteById with database access error");

        // Arrange: Mock the JPA repository to throw a DataAccessException
        DataAccessException mockException = new DataAccessException("Simulated database error") {};
        doThrow(mockException).when(mockJpaRepository).deleteById(TEST_UUID);

        // Act & Assert: Verify that the repository wraps the exception appropriately
        PerformanceMetricsRepository.PerformanceMetricsRepositoryException exception =
                assertThrows(PerformanceMetricsRepository.PerformanceMetricsRepositoryException.class,
                        () -> repository.deleteById(TEST_UUID),
                        "Expected PerformanceMetricsRepositoryException for database error");

        // Verify the exception message contains relevant context
        assertTrue(exception.getMessage().contains("Failed to delete PerformanceMetrics with id"),
                "Exception message should contain failure context");

        // Verify the original cause is preserved
        assertNotNull(exception.getCause(), "Original exception cause should be preserved");
        assertEquals(mockException, exception.getCause(),
                "Original DataAccessException should be the cause of the wrapped exception");

        logger.info("[TEST_END] [EXC-003] deleteById exception handling test completed successfully");
    }

    /**
     * Tests the exception handling behavior of {@link PerformanceMetricsRepository#findAll()}
     * when a {@link DataAccessException} occurs during database access.
     *
     * <p>Verifies that the repository catches the {@link DataAccessException},
     * logs the error with the appropriate traceability tag, and wraps it in a
     * {@link PerformanceMetricsRepository.PerformanceMetricsRepositoryException}.</p>
     *
     * @verifies [EXC-003]
     */
    @Test
    @DisplayName("findAll should wrap DataAccessException in custom exception [EXC-003]")
    void findAll_ShouldWrapDataAccessException_WhenDatabaseErrorOccurs() {
        logger.info("[TEST_START] [EXC-003] Testing findAll with database access error");

        // Arrange: Mock the JPA repository to throw a DataAccessException
        DataAccessException mockException = new DataAccessException("Simulated database error") {};
        when(mockJpaRepository.findAll()).thenThrow(mockException);

        // Act & Assert: Verify that the repository wraps the exception appropriately
        PerformanceMetricsRepository.PerformanceMetricsRepositoryException exception =
                assertThrows(PerformanceMetricsRepository.PerformanceMetricsRepositoryException.class,
                        () -> repository.findAll(),
                        "Expected PerformanceMetricsRepositoryException for database error");

        // Verify the exception message contains relevant context
        assertTrue(exception.getMessage().contains("Failed to retrieve all PerformanceMetrics records"),
                "Exception message should contain failure context");

        // Verify the original cause is preserved
        assertNotNull(exception.getCause(), "Original exception cause should be preserved");
        assertEquals(mockException, exception.getCause(),
                "Original DataAccessException should be the cause of the wrapped exception");

        logger.info("[TEST_END] [EXC-003] findAll exception handling test completed successfully");
    }

    /* =========================================================================
       HELPER METHODS
       ========================================================================= */

    /**
     * Creates a sample {@link PerformanceMetrics} entity for testing purposes.
     *
     * <p>This method initializes a PerformanceMetrics object with predefined
     * values to ensure consistent test data across all test cases.</p>
     *
     * @return A populated PerformanceMetrics entity instance.
     */
    private PerformanceMetrics createSamplePerformanceMetrics() {
        PerformanceMetrics entity = new PerformanceMetrics();
        entity.setPerformanceId(TEST_UUID);
        entity.setPostId(TEST_POST_ID);
        entity.setLikes(SAMPLE_LIKES);
        entity.setComments(SAMPLE_COMMENTS);
        entity.setShares(SAMPLE_SHARES);
        entity.setCollectedAt(SAMPLE_COLLECTED_AT);
        return entity;
    }
}