/**
 * Integration test suite for UserRepository.
 * Verifies CRUD operations with full runtime infrastructure.
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

/**
 * Integration test class for UserRepository.
 * This class exercises the repository layer against a real PostgreSQL database using Testcontainers,
 * ensuring end‑to‑end data persistence, retrieval, update, and deletion workflows.
 * All test methods embed the required traceability Tag IDs for audit compliance.
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=postgres",
    "spring.datasource.password=secret"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class UserRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    // -------------------------------------------------------------------------
    // Test Constants – isolated at class level to satisfy anti‑magic‑numbers guardrails
    // -------------------------------------------------------------------------
    private static final UUID TEST_USER_ID_1 = UUID.randomUUID();
    private static final UUID TEST_USER_ID_2 = UUID.randomUUID();
    private static final String TEST_USERNAME_1 = "testuser1";
    private static final String TEST_EMAIL_1 = "test1@example.com";
    private static final String TEST_ROLE_1 = "User";

    /**
     * Container definition for an isolated PostgreSQL test database.
     * This ensures each test run operates on a clean, ephemeral DB instance.
     * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("postgres")
        .withPassword("secret");

    /**
     * Dynamically inject Testcontainers properties into the Spring context.
     * This guarantees that the application uses the container‑managed datasource.
     * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertySource.PropertySourceRegistrar registrar) {
        registrar.add("spring.datasource.url", postgresContainer.getJdbcUrl());
        registrar.add("spring.datasource.username", postgresContainer.getUsername());
        registrar.add("spring.datasource.password", postgresContainer.getPassword());
    }

    /**
     * Repository under test – automatically wired by Spring.
     * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository repository;

    private User testUser1;

    /**
     * Setup hook executed before each test method.
     * Initializes a canonical User entity for use in happy‑path scenarios.
     * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @BeforeEach
    void setUp() {
        logger.info("[SETUP] Initializing test user entity for integration tests.");
        testUser1 = new User();
        testUser1.setUserId(TEST_USER_ID_1);
        testUser1.setUsername(TEST_USERNAME_1);
        testUser1.setEmail(TEST_EMAIL_1);
        testUser1.setPasswordHash("dummyHash");
        testUser1.setRole(TEST_ROLE_1);
        // Assuming User entity contains timestamps; if not, they can be omitted.
        // testUser1.setCreatedAt(java.time.Instant.now());
        // testUser1.setUpdatedAt(java.time.Instant.now());
    }

    /**
     * Cleanup hook executed after each test method.
     * Releases references to avoid memory leaks and ensures test isolation.
     * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @AfterEach
    void tearDown() {
        logger.info("[TEARDOWN] Cleaning up test data.");
        testUser1 = null;
    }

    // -------------------------------------------------------------------------
    // Test Cases – each method embeds traceability tags in Javadoc and inline comments
    // -------------------------------------------------------------------------

    /**
     * Happy‑path test: retrieve an existing User by its primary key.
     * Validates that the repository correctly maps persisted data back to the domain object.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testFindById_Success() {
        logger.info("[TEST] testFindById_Success started.");
        // Business requirement: Users can be retrieved by their unique identifier.
        User saved = repository.save(testUser1);
        Assertions.assertNotNull(saved, "Saved user should not be null");
        User found = repository.findById(saved.getUserId());
        Assertions.assertNotNull(found, "Retrieved user should not be null");
        Assertions.assertEquals(saved.getUserId(), found.getUserId(), "User IDs must match");
        Assertions.assertEquals(saved.getUsername(), found.getUsername(), "Usernames must match");
        logger.info("[TEST] testFindById_Success passed.");
    }

    /**
     * Edge‑case test: query for a non‑existent User identifier.
     * Confirms that the repository returns {@code null} rather than throwing an unexpected exception.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testFindById_NotFound() {
        logger.info("[TEST] testFindById_NotFound started.");
        // Business requirement: Non‑existent IDs yield null to simplify client‑side handling.
        User notFound = repository.findById(TEST_USER_ID_2);
        Assertions.assertNull(notFound, "Non‑existent user should return null");
        logger.info("[TEST] testFindById_NotFound passed.");
    }

    /**
     * Happy‑path test: persist a brand‑new User entity.
     * Ensures the repository correctly generates and stores the primary key.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testSave_NewUser() {
        logger.info("[TEST] testSave_NewUser started.");
        // Business requirement: New users can be created and stored.
        User newUser = new User();
        newUser.setUserId(TEST_USER_ID_2);
        newUser.setUsername("newUser");
        newUser.setEmail("new@example.com");
        newUser.setPasswordHash("hash");
        newUser.setRole("User");
        User saved = repository.save(newUser);
        Assertions.assertNotNull(saved, "Saved user should not be null");
        Assertions.assertEquals(TEST_USER_ID_2, saved.getUserId(), "User ID must match");
        logger.info("[TEST] testSave_NewUser passed.");
    }

    /**
     * Update test: modify an existing User’s attributes and re‑persist.
     * Validates that the repository supports mutable state updates.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testSave_UpdateUser() {
        logger.info("[TEST] testSave_UpdateUser started.");
        // Persist initial user
        repository.save(testUser1);
        // Mutate fields
        testUser1.setUsername("updatedUser");
        testUser1.setEmail("updated@example.com");
        User updated = repository.save(testUser1);
        Assertions.assertNotNull(updated, "Updated user should not be null");
        Assertions.assertEquals("updatedUser", updated.getUsername(), "Username should be updated");
        Assertions.assertEquals("updated@example.com", updated.getEmail(), "Email should be updated");
        logger.info("[TEST] testSave_UpdateUser passed.");
    }

    /**
     * Deletion test: remove a User and verify it no longer exists.
     * Confirms that the repository’s delete operation is durable.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testDelete() {
        logger.info("[TEST] testDelete started.");
        // Persist user
        repository.save(testUser1);
        // Delete the user
        repository.delete(testUser1.getUserId());
        // Verify deletion
        User deleted = repository.findById(testUser1.getUserId());
        Assertions.assertNull(deleted, "Deleted user should not be found");
        logger.info("[TEST] testDelete passed.");
    }

    /**
     * Retrieval test: fetch all User entities from the store.
     * Ensures the repository’s findAll method returns a complete snapshot.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testFindAll() {
        logger.info("[TEST] testFindAll started.");
        // Insert two distinct users
        User userA = new User();
        userA.setUserId(UUID.randomUUID());
        userA.setUsername("userA");
        userA.setEmail("a@example.com");
        userA.setPasswordHash("hashA");
        userA.setRole("User");
        repository.save(userA);

        User userB = new User();
        userB.setUserId(UUID.randomUUID());
        userB.setUsername("userB");
        userB.setEmail("b@example.com");
        userB.setPasswordHash("hashB");
        userB.setRole("User");
        repository.save(userB);

        List<User> all = repository.findAll();
        Assertions.assertNotNull(all, "User list should not be null");
        Assertions.assertTrue(all.size() >= 2, "At least two users should exist");
        boolean containsA = all.stream().anyMatch(u -> u.getUsername().equals("userA"));
        boolean containsB = all.stream().anyMatch(u -> u.getUsername().equals("userB"));
        Assertions.assertTrue(containsA, "User A should be present");
        Assertions.assertTrue(containsB, "User B should be present");
        logger.info("[TEST] testFindAll passed.");
    }

    /**
     * Edge‑case test: retrieve all Users when the store is empty.
     * Validates that findAll gracefully returns an empty collection rather than throwing.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testFindAll_Empty() {
        logger.info("[TEST] testFindAll_Empty started.");
        // Clean any residual data from previous tests
        repository.findAll().forEach(u -> repository.delete(u.getUserId()));
        List<User> all = repository.findAll();
        Assertions.assertNotNull(all, "User list should not be null");
        Assertions.assertTrue(all.isEmpty(), "User list should be empty");
        logger.info("[TEST] testFindAll_Empty passed.");
    }

    /**
     * Edge‑case test: invoke findById with a {@code null} argument.
     * Confirms that the repository throws an appropriate exception (IllegalArgumentException)
     * rather than silently accepting the null value.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testFindById_NullParameter() {
        logger.info("[TEST] testFindById_NullParameter started.");
        // Business requirement: Null identifiers must be rejected to enforce data integrity.
        Assertions.assertThrows(IllegalArgumentException.class, () -> repository.findById(null),
            "Passing null to findById should raise an exception");
        logger.info("[TEST] testFindById_NullParameter passed.");
    }

    /**
     * Edge‑case test: attempt to save a {@code null} User entity.
     * Ensures the repository enforces non‑null constraints at the persistence layer.
     * Verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Test
    void testSave_NullEntity() {
        logger.info("[TEST] testSave_NullEntity started.");
        // Business requirement: Null entities must be rejected to maintain referential integrity.
        Assertions.assertThrows(IllegalArgumentException.class, () -> repository.save(null),
            "Saving null entity should raise an exception");
        logger.info("[TEST] testSave_NullEntity passed.");
    }
}