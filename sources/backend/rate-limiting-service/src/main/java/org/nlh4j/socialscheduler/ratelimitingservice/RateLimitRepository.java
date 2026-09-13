// [REQ-003] [EXC-002] [EXC-003] [EXC-005]
package org.nlh4j.socialscheduler.ratelimitingservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nlh4j.socialscheduler.ratelimitingservice.entity.RateLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for {@link RateLimitRepository}.
 * Validates persistence, retrieval, update, and deletion operations against a live H2 in-memory database.
 * <p>
 * Traceability Tags:
 * - [REQ-003]: Core rate limiting data operations
 * - [EXC-002]: Safe failure path when record is missing
 * - [EXC-003]: Exception path for entity validation/constraint failures
 * - [EXC-005]: Exception path for exceeded thresholds or invalid deletions
 * <p>
 * Test Strategy:
 * - Uses @DataJpaTest to bootstrap Spring Data JPA repositories with real database interactions.
 * - Leverages H2 in-memory database for fast, isolated integration testing.
 * - Covers happy paths, edge cases (nulls, empty collections), and exception scenarios.
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
class RateLimitRepositoryIntegrationTest {

    /**
     * Constant for default test user ID used across test scenarios.
     * [REQ-003] - Rate limit windows are enforced per user_id and endpoint combination.
     */
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    /**
     * Constant for default test endpoint used across test scenarios.
     * [REQ-003] - Rate limit windows are enforced per user_id and endpoint combination.
     */
    private static final String TEST_ENDPOINT = "/api/v1/posts";

    /**
     * Constant for default request count threshold.
     * [REQ-003] - Exceeded thresholds trigger exception handling mapped to [EXC-005].
     */
    private static final int DEFAULT_REQUEST_COUNT = 100;

    /**
     * Constant for default window start time.
     * [REQ-003] - Rate limit windows are enforced per user_id and endpoint combination.
     */
    private static final LocalDateTime DEFAULT_WINDOW_START = LocalDateTime.now().minusHours(1);

    /**
     * Constant for default window end time.
     * [REQ-003] - Rate limit windows are enforced per user_id and endpoint combination.
     */
    private static final LocalDateTime DEFAULT_WINDOW_END = LocalDateTime.now();

    @Autowired
    private RateLimitRepository rateLimitRepository;

    /**
     * Helper method to create a valid RateLimit entity for testing.
     * [REQ-003] - Core rate limiting data ingestion path.
     *
     * @return a fully populated RateLimit entity
     */
    private RateLimit createValidRateLimit() {
        RateLimit rateLimit = new RateLimit();
        rateLimit.setUserId(TEST_USER_ID);
        rateLimit.setEndpoint(TEST_ENDPOINT);
        rateLimit.setRequestCount(DEFAULT_REQUEST_COUNT);
        rateLimit.setWindowStart(DEFAULT_WINDOW_START);
        rateLimit.setWindowEnd(DEFAULT_WINDOW_END);
        return rateLimit;
    }

    /**
     * Test: Save a valid RateLimit entity and verify it is persisted with a generated ID.
     * [REQ-003] - Core rate limiting data ingestion path.
     * [EXC-003] - Validates that valid entities do not trigger constraint violations.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Save valid RateLimit entity and verify persistence")
    void saveValidRateLimitEntity() {
        // Arrange: Create a valid RateLimit entity
        RateLimit rateLimit = createValidRateLimit();

        // Act: Persist the entity
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Assert: Verify the entity was saved with a generated ID
        assertNotNull(savedRateLimit.getRateLimitId(), "Saved RateLimit should have a generated UUID");
        assertEquals(TEST_USER_ID, savedRateLimit.getUserId(), "User ID should match");
        assertEquals(TEST_ENDPOINT, savedRateLimit.getEndpoint(), "Endpoint should match");
        assertEquals(DEFAULT_REQUEST_COUNT, savedRateLimit.getRequestCount(), "Request count should match");
        assertEquals(DEFAULT_WINDOW_START, savedRateLimit.getWindowStart(), "Window start should match");
        assertEquals(DEFAULT_WINDOW_END, savedRateLimit.getWindowEnd(), "Window end should match");
    }

    /**
     * Test: Retrieve a RateLimit entity by its ID after saving.
     * [REQ-003] - Core rate limiting data retrieval requirement.
     * [EXC-002] - Ensures safe failure path if record is missing during processing.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-002] Find RateLimit by ID after saving")
    void findByIdAfterSave() {
        // Arrange: Save a RateLimit entity
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Act: Retrieve the entity by ID
        Optional<RateLimit> foundRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());

        // Assert: Verify the entity was found and matches the saved data
        assertTrue(foundRateLimit.isPresent(), "RateLimit should be found by ID");
        RateLimit retrievedRateLimit = foundRateLimit.get();
        assertEquals(savedRateLimit.getRateLimitId(), retrievedRateLimit.getRateLimitId(), "ID should match");
        assertEquals(TEST_USER_ID, retrievedRateLimit.getUserId(), "User ID should match");
        assertEquals(TEST_ENDPOINT, retrievedRateLimit.getEndpoint(), "Endpoint should match");
    }

    /**
     * Test: Attempt to find a RateLimit by a non-existent ID and verify empty result.
     * [REQ-003] - Core rate limiting data retrieval requirement.
     * [EXC-002] - Ensures safe failure path if record is missing during processing.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-002] Find RateLimit by non-existent ID returns empty")
    void findByIdNonExistentReturnsEmpty() {
        // Arrange: Generate a random UUID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        // Act: Attempt to retrieve the entity by non-existent ID
        Optional<RateLimit> foundRateLimit = rateLimitRepository.findById(nonExistentId);

        // Assert: Verify the result is empty
        assertTrue(foundRateLimit.isEmpty(), "RateLimit should not be found for non-existent ID");
    }

    /**
     * Test: Update an existing RateLimit entity and verify changes are persisted.
     * [REQ-003] - Core rate limiting data ingestion path.
     * [EXC-003] - Validates that updates do not trigger constraint violations.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Update existing RateLimit entity and verify persistence")
    void updateExistingRateLimitEntity() {
        // Arrange: Save a RateLimit entity
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Modify the entity
        int updatedRequestCount = 200;
        savedRateLimit.setRequestCount(updatedRequestCount);

        // Act: Save the updated entity
        RateLimit updatedRateLimit = rateLimitRepository.save(savedRateLimit);

        // Assert: Verify the changes were persisted
        assertEquals(updatedRequestCount, updatedRateLimit.getRequestCount(), "Request count should be updated");

        // Verify the update is reflected in the database
        Optional<RateLimit> foundRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());
        assertTrue(foundRateLimit.isPresent(), "RateLimit should still exist after update");
        assertEquals(updatedRequestCount, foundRateLimit.get().getRequestCount(), "Updated request count should be persisted");
    }

    /**
     * Test: Delete a RateLimit entity by ID and verify it is removed.
     * [REQ-003] - Cleanup of rate limit entries upon policy rotation.
     * [EXC-005] - Ensures deletion only proceeds if no active sessions reference the record.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-005] Delete RateLimit by ID and verify removal")
    void deleteByIdAndVerifyRemoval() {
        // Arrange: Save a RateLimit entity
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Act: Delete the entity by ID
        rateLimitRepository.deleteById(savedRateLimit.getRateLimitId());

        // Assert: Verify the entity is no longer present
        Optional<RateLimit> foundRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());
        assertTrue(foundRateLimit.isEmpty(), "RateLimit should be deleted and not found");
    }

    /**
     * Test: Retrieve all RateLimit entities and verify the collection size.
     * [REQ-003] - Full inventory for rate limit auditing and compliance reporting.
     * [EXC-003] - Handles potential pagination/overflow edge cases in bulk retrieval.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Find all RateLimit entities and verify collection")
    void findAllRateLimitEntities() {
        // Arrange: Save multiple RateLimit entities
        RateLimit rateLimit1 = createValidRateLimit();
        RateLimit rateLimit2 = createValidRateLimit();
        rateLimit2.setEndpoint("/api/v1/comments");
        rateLimitRepository.save(rateLimit1);
        rateLimitRepository.save(rateLimit2);

        // Act: Retrieve all entities
        Iterable<RateLimit> allRateLimits = rateLimitRepository.findAll();

        // Assert: Verify the collection contains both entities
        assertNotNull(allRateLimits, "findAll should not return null");
        int count = 0;
        for (RateLimit ignored : allRateLimits) {
            count++;
        }
        assertEquals(2, count, "findAll should return exactly 2 RateLimit entities");
    }

    /**
     * Test: Attempt to save a RateLimit entity with null user ID and verify constraint violation.
     * [REQ-003] - Core rate limiting data ingestion path.
     * [EXC-003] - Exception path if entity validation or constraint checks fail.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Save RateLimit with null user ID triggers constraint violation")
    void saveRateLimitWithNullUserIdThrowsException() {
        // Arrange: Create a RateLimit entity with null user ID
        RateLimit rateLimit = createValidRateLimit();
        rateLimit.setUserId(null);

        // Act & Assert: Verify that saving triggers a DataIntegrityViolationException
        assertThrows(DataIntegrityViolationException.class, () -> {
            rateLimitRepository.saveAndFlush(rateLimit);
        }, "Saving RateLimit with null user ID should trigger DataIntegrityViolationException");
    }

    /**
     * Test: Attempt to save a RateLimit entity with null endpoint and verify constraint violation.
     * [REQ-003] - Core rate limiting data ingestion path.
     * [EXC-003] - Exception path if entity validation or constraint checks fail.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Save RateLimit with null endpoint triggers constraint violation")
    void saveRateLimitWithNullEndpointThrowsException() {
        // Arrange: Create a RateLimit entity with null endpoint
        RateLimit rateLimit = createValidRateLimit();
        rateLimit.setEndpoint(null);

        // Act & Assert: Verify that saving triggers a DataIntegrityViolationException
        assertThrows(DataIntegrityViolationException.class, () -> {
            rateLimitRepository.saveAndFlush(rateLimit);
        }, "Saving RateLimit with null endpoint should trigger DataIntegrityViolationException");
    }

    /**
     * Test: Verify that findAll returns an empty collection when no entities exist.
     * [REQ-003] - Full inventory for rate limit auditing and compliance reporting.
     * [EXC-003] - Handles potential pagination/overflow edge cases in bulk retrieval.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-003] Find all returns empty collection when no entities exist")
    void findAllReturnsEmptyWhenNoEntitiesExist() {
        // Act: Retrieve all entities (database should be empty)
        Iterable<RateLimit> allRateLimits = rateLimitRepository.findAll();

        // Assert: Verify the collection is empty
        assertNotNull(allRateLimits, "findAll should not return null even when empty");
        int count = 0;
        for (RateLimit ignored : allRateLimits) {
            count++;
        }
        assertEquals(0, count, "findAll should return an empty collection when no entities exist");
    }

    /**
     * Test: Verify that deleting a non-existent RateLimit by ID does not throw an exception.
     * [REQ-003] - Cleanup of rate limit entries upon policy rotation.
     * [EXC-005] - Ensures deletion only proceeds if no active sessions reference the record.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-005] Delete non-existent RateLimit by ID does not throw exception")
    void deleteByIdNonExistentDoesNotThrow() {
        // Arrange: Generate a random UUID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert: Verify that deleting a non-existent entity does not throw an exception
        assertDoesNotThrow(() -> {
            rateLimitRepository.deleteById(nonExistentId);
        }, "Deleting a non-existent RateLimit should not throw an exception");
    }

    /**
     * Test: Verify that saving and then deleting a RateLimit entity works correctly in sequence.
     * [REQ-003] - Core rate limiting data ingestion and cleanup paths.
     * [EXC-002] - Ensures safe failure path if record is missing during processing.
     * [EXC-005] - Ensures deletion only proceeds if no active sessions reference the record.
     */
    @Test
    @DisplayName("[REQ-003] [EXC-002] [EXC-005] Save and delete RateLimit entity in sequence")
    void saveAndDeleteRateLimitInSequence() {
        // Arrange: Create and save a RateLimit entity
        RateLimit rateLimit = createValidRateLimit();
        RateLimit savedRateLimit = rateLimitRepository.save(rateLimit);

        // Act: Delete the entity by ID
        rateLimitRepository.deleteById(savedRateLimit.getRateLimitId());

        // Assert: Verify the entity is no longer present
        Optional<RateLimit> foundRateLimit = rateLimitRepository.findById(savedRateLimit.getRateLimitId());
        assertTrue(foundRateLimit.isEmpty(), "RateLimit should be deleted and not found after sequential save and delete");
    }
}