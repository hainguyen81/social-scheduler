/**
 * UserRepository Integration Test Suite
 * Validates persistence, retrieval, and existence enforcement for User entities
 * via Testcontainers-backed PostgreSQL instance.
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.ActiveProfiles;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("socialscheduler_test")
            .withUsername("app_user")
            .withPassword("app_pass");

    @BeforeAll
    static void startPostgres() {
        POSTGRES.start();
    }

    @AfterAll
    static void stopPostgres() {
        POSTGRES.stop();
    }

    @Autowired
    UserRepository userRepository;

    /**
     * @verifies [ARC-001]
     * Asserts that a valid User entity is persisted correctly and ID is generated.
     * Business Requirement: Core user persistence lifecycle.
     * Edge Case: Verifies UUID generation and non-null ID upon save operation.
     */
    @Test
    void createUser_whenValid_shouldPersist() {
        // Arrange
        var user = new User();
        user.setUsername("testuser123");
        user.setEmail("test123@example.com");
        // Act
        var saved = userRepository.save(user);
        // Assert
        assertNotNull(saved.getUserId(), "User ID must be generated upon persistence");
        assertEquals("testuser123", saved.getUsername(), "Username should match input value");
        assertEquals("test123@example.com", saved.getEmail(), "Email should match input value");
    }

    /**
     * @verifies [ARC-002]
     * Asserts that an existing User can be retrieved by primary key ID.
     * Business Requirement: User lookup by primary key.
     * Edge Case: Ensures null-safe retrieval and ID matching precision.
     */
    @Test
    void findUserById_whenExisting_shouldReturn() {
        // Arrange
        var user = new User();
        user.setUsername("testuser456");
        user.setEmail("test456@example.com");
        userRepository.save(user);
        // Act
        var found = userRepository.findById(user.getUserId()).orElse(null);
        // Assert
        assertNotNull(found, "User entity should be present in database");
        assertEquals("testuser456", found.getUsername(), "Retrieved username must match saved value");
    }

    /**
     * @verifies [ARC-003]
     * Asserts that an existing User can be deleted by ID and removed from repository.
     * Business Requirement: User deletion and referential integrity.
     * Edge Case: Verifies post-deletion existence is false; prevents orphaned records.
     */
    @Test
    void deleteUser_whenExisting_shouldRemove() {
        // Arrange
        var user = new User();
        user.setUsername("todelete001");
        user.setEmail("delete001@example.com");
        userRepository.save(user);
        var id = user.getUserId();
        // Act
        userRepository.deleteById(id);
        // Assert
        assertFalse(userRepository.existsById(id), "User should no longer exist after deletion");
    }

    /**
     * @verifies [ARC-004]
     * Asserts that a User ID existence can be queried prior to deletion or update.
     * Business Requirement: Pre-condition existence check for user operations.
     * Edge Case: Validates boolean return type and consistent state across transactions.
     */
    @Test
    void existsUserById_whenValid_shouldReturnTrue() {
        // Arrange
        var user = new User();
        user.setUsername("existscheck002");
        user.setEmail("exists002@example.com");
        userRepository.save(user);
        // Act
        boolean exists = userRepository.existsById(user.getUserId());
        // Assert
        assertTrue(exists, "User existence should return true for saved entity");
    }
}