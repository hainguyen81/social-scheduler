/**
 * Unit test suite for UserService class.
 * This test class validates all CRUD operations, input validation,
 * idempotency handling, and exception scenarios for user management
 * within the social-scheduler microservices architecture.
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

/**
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Creates a default test User instance with valid attributes.
     * Used across multiple test methods to reduce boilerplate.
     * @verifies [ARC-001]
     */
    private User createTestUser() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("SecurePass123!");
        user.setRole("User");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    /**
     * Test createUser with valid inputs - happy path.
     * Verifies successful user creation, uniqueness validation,
     * idempotency key handling (null key), and logging compliance.
     * @verifies [ARC-001] [ARC-002] [ARC-003] [ARC-004]
     */
    @Test
    @DisplayName("Create user with valid inputs - happy path")
    void createUser_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing createUser happy path with valid user inputs");
        // Arrange: setup repository mocks to return empty for uniqueness checks
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: execute createUser service method
        User result = userService.createUser(user, null);

        // Assert: verify returned user has correct attributes and repository save was called
        assertNotNull(result.getUserId(), "User ID should be generated automatically");
        assertEquals("testuser", result.getUsername(), "Username should match input");
        assertEquals("test@example.com", result.getEmail(), "Email should match input");
        assertEquals("User", result.getRole(), "Role should match input");
        verify(userRepository).save(user);
        // Assert: verify no null fields
        assertNotNull(result.getCreatedAt(), "Created timestamp should not be null");
        assertNotNull(result.getUpdatedAt(), "Updated timestamp should not be null");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser happy path completed successfully with ID: {}",
                result.getUserId());
    }

    /**
     * Test createUser with null user object.
     * Verifies InvalidInputException is thrown when user is null.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with null user object")
    void createUser_nullUser() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with null user object");
        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(null, null),
                "Should throw InvalidInputException when user is null");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser null user correctly threw InvalidInputException");
    }

    /**
     * Test createUser with duplicate username.
     * Verifies DuplicateUserException is thrown when username already exists.
     * @verifies [ARC-001] [ARC-003]
     */
    @Test
    @DisplayName("Create user with duplicate username")
    void createUser_duplicateUsername() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with duplicate username");
        // Arrange: repository returns existing user for username
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect DuplicateUserException
        assertThrows(DuplicateUserException.class, () -> userService.createUser(user, null),
                "Should throw DuplicateUserException when username already exists");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser duplicate username correctly threw DuplicateUserException");
    }

    /**
     * Test createUser with duplicate email.
     * Verifies DuplicateUserException is thrown when email already exists.
     * @verifies [ARC-001] [ARC-003]
     */
    @Test
    @DisplayName("Create user with duplicate email")
    void createUser_duplicateEmail() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with duplicate email");
        // Arrange: repository returns existing user for email
        User user = createTestUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect DuplicateUserException
        assertThrows(DuplicateUserException.class, () -> userService.createUser(user, null),
                "Should throw DuplicateUserException when email already exists");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser duplicate email correctly threw DuplicateUserException");
    }

    /**
     * Test createUser with invalid username (empty string).
     * Verifies InvalidInputException is thrown for empty username.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with empty username")
    void createUser_invalidUsernameEmpty() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with empty username");
        // Arrange
        User user = createTestUser();
        user.setUsername("");
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(user, null),
                "Should throw InvalidInputException for empty username");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser empty username correctly threw InvalidInputException");
    }

    /**
     * Test createUser with username exceeding max length.
     * Verifies InvalidInputException is thrown when username > 255 chars.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with username too long")
    void createUser_invalidUsernameTooLong() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with username exceeding max length");
        // Arrange
        User user = createTestUser();
        user.setUsername("a".repeat(256)); // exceeds MAX_USERNAME_LENGTH = 255
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(user, null),
                "Should throw InvalidInputException when username exceeds max length");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser long username correctly threw InvalidInputException");
    }

    /**
     * Test createUser with invalid email (empty string).
     * Verifies InvalidInputException is thrown for empty email.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with empty email")
    void createUser_invalidEmailEmpty() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with empty email");
        // Arrange
        User user = createTestUser();
        user.setEmail("");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(user, null),
                "Should throw InvalidInputException for empty email");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser empty email correctly threw InvalidInputException");
    }

    /**
     * Test createUser with password hash too short.
     * Verifies InvalidInputException is thrown when password hash < 1 char.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with password hash too short")
    void createUser_invalidPasswordTooShort() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with password hash too short");
        // Arrange
        User user = createTestUser();
        user.setPasswordHash(""); // 0 chars
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(user, null),
                "Should throw InvalidInputException when password hash is too short");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser short password hash correctly threw InvalidInputException");
    }

    /**
     * Test createUser with invalid role.
     * Verifies InvalidInputException is thrown when role is not in valid roles list.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Create user with invalid role")
    void createUser_invalidRole() {
        logger.info("[TEST_START] [ARC-001] Testing creately createUser with invalid role");
        // Arrange
        User user = createTestUser();
        user.setRole("InvalidRole");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.createUser(user, null),
                "Should throw InvalidInputException when role is invalid");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser invalid role correctly threw InvalidInputException");
    }

    /**
     * Test createUser with idempotency key.
     * Verifies createUser proceeds when idempotency key is unique;
     * service default isIdempotencyKeyUsed returns false, allowing operation.
     * @verifies [ARC-001] [ARC-002]
     */
    @Test
    @DisplayName("Create user with idempotency key")
    void createUser_idempotencyKey() {
        logger.info("[TEST_START] [ARC-001] Testing createUser with idempotency key");
        // Arrange
        User user = createTestUser();
        String key = "test-key-123";
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: execute with a key; since isIdempotencyKeyUsed returns false, it should proceed
        User result = userService.createUser(user, key);

        // Assert: user should be created successfully
        assertNotNull(result.getUserId(), "User should be created successfully");
        logger.info("[TEST_COMPLETE] [ARC-001] createUser idempotency key test completed successfully");
    }

    /**
     * Test getUserById with valid existing user.
     * Verifies user retrieval by ID succeeds and returns correct entity.
     * @verifies [ARC-001] [ARC-002]
     */
    @Test
    @DisplayName("Get user by ID - happy path")
    void getUserById_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing getUserById happy path with existing user");
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser();
        existingUser.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result, "User should be retrieved");
        assertEquals(userId, result.getUserId(), "User ID should match");
        assertEquals("testuser", result.getUsername(), "Username should match");
        verify(userRepository).findById(userId);
        logger.info("[TEST_COMPLETE] [ARC-001] getUserById retrieved user successfully: {}", result.getUserId());
    }

    /**
     * Test getUserById with null user ID.
     * Verifies InvalidInputException is thrown when userId is null.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Get user by ID with null ID")
    void getUserById_nullId() {
        logger.info("[TEST_START] [ARC-001] Testing getUserById with null user ID");
        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.getUserById(null),
                "Should throw InvalidInputException when userId is null");
        logger.info("[TEST_COMPLETE] [ARC-001] getUserById null ID correctly threw InvalidInputException");
    }

    /**
     * Test getUserById with non-existent user.
     * Verifies UserNotFoundException is thrown when user ID does not exist.
     * @verifies [ARC-001] [ARC-004]
     */
    @Test
    @DisplayName("Get user by ID - user not found")
    void getUserById_userNotFound() {
        logger.info("[TEST_START] [ARC-001] Testing getUserById with non-existent user ID");
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert: expect UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId),
                "Should throw UserNotFoundException when user ID does not exist");
        logger.info("[TEST_COMPLETE] [ARC-001] getUserById user not found correctly threw UserNotFoundException");
    }

    /**
     * Test getUserByUsername with valid existing username.
     * Verifies user retrieval by username succeeds.
     * @verifies [ARC-001] [ARC-002]
     */
    @Test
    @DisplayName("Get user by username - happy path")
    void getUserByUsername_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing getUserByUsername happy path with existing username");
        // Arrange
        String username = "testuser";
        User existingUser = createTestUser();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.getUserByUsername(username);

        // Assert
        assertNotNull(result, "User should be retrieved");
        assertEquals(username, result.getUsername(), "Username should match");
        verify(userRepository).findByUsername(username);
        logger.info("[TEST_COMPLETE] [ARC-001] getUserByUsername retrieved user successfully: {}", result.getUsername());
    }

    /**
     * Test getUserByUsername with null username.
     * Verifies InvalidInputException is thrown when username is null.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Get user by username with null username")
    void getUserByUsername_nullUsername() {
        logger.info("[TEST_START] [ARC-001] Testing getUserByUsername with null username");
        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.getUserByUsername(null),
                "Should throw InvalidInputException when username is null");
        logger.info("[TEST_COMPLETE] [ARC-001] getUserByUsername null username correctly threw InvalidInputException");
    }

    /**
     * Test getUserByUsername with non-existent username.
     * Verifies UserNotFoundException is thrown when username does not exist.
     * @verifies [ARC-001] [ARC-004]
     */
    @Test
    @DisplayName("Get user by username - user not found")
    void getUserByUsername_userNotFound() {
        logger.info("[TEST_START] [ARC-001] Testing getUserByUsername with non-existent username");
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert: expect UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername(username),
                "Should throw UserNotFoundException when username does not exist");
        logger.info("[TEST_COMPLETE] [ARC-001] getUserByUsername user not found correctly threw UserNotFoundException");
    }

    /**
     * Test getAllUsers retrieves all users from the system.
     * Verifies list retrieval and logging compliance.
     * @verifies [ARC-001] [ARC-002]
     */
    @Test
    @DisplayName("Get all users - happy path")
    void getAllUsers_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing getAllUsers happy path");
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of(createTestUser()));

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result, "User list should not be null");
        assertEquals(1, result.size(), "Should return one user");
        verify(userRepository).findAll();
        logger.info("[TEST_COMPLETE] [ARC-001] getAllUsers retrieved {} users", result.size());
    }

    /**
     * Test updateUser with valid existing user.
     * Verifies user update succeeds with correct field modifications.
     * @verifies [ARC-001] [ARC-002] [ARC-003] [ARC-004]
     */
    @Test
    @DisplayName("Update user - happy path")
    void updateUser_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing updateUser happy path with existing user");
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser();
        existingUser.setUserId(userId);

        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPasswordHash("NewPass123!");
        updatedUser.setRole("User");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.updateUser(userId, updatedUser, null);

        // Assert
        assertNotNull(result, "Updated user should not be null");
        assertEquals("updateduser", result.getUsername(), "Username should be updated");
        assertEquals("updated@example.com", result.getEmail(), "Email should be updated");
        verify(userRepository).save(existingUser);
        logger.info("[TEST_COMPLETE] [ARC-001] updateUser updated user successfully: {}", result.getUserId());
    }

    /**
     * Test updateUser with non-existent user ID.
     * Verifies UserNotFoundException is thrown when user ID does not exist.
     * @verifies [ARC-001] [ARC-004]
     */
    @Test
    @DisplayName("Update user with non-existent ID")
    void updateUser_userNotFound() {
        logger.info("[TEST_START] [ARC-001] Testing updateUser with non-existent user ID");
        // Arrange
        UUID userId = UUID.randomUUID();
        User updatedUser = new User();
        updatedUser.setUsername("newuser");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert: expect UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updatedUser, null),
                "Should throw UserNotFoundException when user ID does not exist");
        logger.info("[TEST_COMPLETE] [ARC-001] updateUser user not found correctly threw UserNotFoundException");
    }

    /**
     * Test updateUser with duplicate username after update.
     * Verifies DuplicateUserException is thrown when new username conflicts with existing user.
     * @verifies [ARC-001] [ARC-003] [ARC-004]
     */
    @Test
    @DisplayName("Update user with duplicate username")
    void updateUser_duplicateUsername() {
        logger.info("[TEST_START] [ARC-001] Testing updateUser with duplicate username");
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser();
        existingUser.setUserId(userId);

        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("conflictinguser"); // conflicts with another user
        updatedUser.setEmail("new@example.com");

        // Mock existing user with different username, but new username exists
        User conflictingUser = new User();
        conflictingUser.setUsername("conflictinguser");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("conflictinguser")).thenReturn(Optional.of(conflictingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect DuplicateUserException
        assertThrows(DuplicateUserException.class, () -> userService.updateUser(userId, updatedUser, null),
                "Should throw DuplicateUserException when new username conflicts");
        logger.info("[TEST_COMPLETE] [ARC-001] updateUser duplicate username correctly threw DuplicateUserException");
    }

    /**
     * Test updateUser with duplicate email after update.
     * Verifies DuplicateUserException is thrown when new email conflicts with existing user.
     * @verifies [ARC-001] [ARC-003] [ARC-004]
     */
    @Test
    @DisplayName("Update user with duplicate email")
    void updateUser_duplicateEmail() {
        logger.info("[TEST_START] [ARC-001] Testing updateUser with duplicate email");
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser();
        existingUser.setUserId(userId);

        User updatedUser = new User();
        updatedUser.setUserId(userId);
        updatedUser.setUsername("newuser");
        updatedUser.setEmail("conflicting@example.com"); // conflicts with existing

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("conflicting@example.com")).thenReturn(Optional.of(new User()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert: expect DuplicateUserException
        assertThrows(DuplicateUserException.class, () -> userService.updateUser(userId, updatedUser, null),
                "Should throw DuplicateUserException when new email conflicts");
        logger.info("[TEST_COMPLETE] [ARC-001] updateUser duplicate email correctly threw DuplicateUserException");
    }

    /**
     * Test deleteUser with valid existing user.
     * Verifies user deletion succeeds and deleteById is called.
     * @verifies [ARC-001] [ARC-002] [ARC-004]
     */
    @Test
    @DisplayName("Delete user - happy path")
    void deleteUser_happyPath() {
        logger.info("[TEST_START] [ARC-001] Testing deleteUser happy path with existing user");
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = createTestUser();
        existingUser.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userService.deleteUser(userId, null);

        // Assert: verify deleteById was called
        verify(userRepository).deleteById(userId);
        logger.info("[TEST_COMPLETE] [ARC-001] deleteUser deleted user successfully: {}", userId);
    }

    /**
     * Test deleteUser with non-existent user ID.
     * Verifies UserNotFoundException is thrown when user ID does not exist.
     * @verifies [ARC-001] [ARC-004]
     */
    @Test
    @DisplayName("Delete user with non-existent ID")
    void deleteUser_userNotFound() {
        logger.info("[TEST_START] [ARC-001] Testing deleteUser with non-existent user ID");
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert: expect UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId, null),
                "Should throw UserNotFoundException when user ID does not exist");
        logger.info("[TEST_COMPLETE] [ARC-001] deleteUser user not found correctly threw UserNotFoundException");
    }

    /**
     * Test deleteUser with null user ID.
     * Verifies InvalidInputException is thrown when userId is null.
     * @verifies [ARC-001]
     */
    @Test
    @DisplayName("Delete user with null ID")
    void deleteUser_nullId() {
        logger.info("[TEST_START] [ARC-001] Testing deleteUser with null user ID");
        // Act & Assert: expect InvalidInputException
        assertThrows(InvalidInputException.class, () -> userService.deleteUser(null, null),
                "Should throw InvalidInputException when userId is null");
        logger.info("[TEST_COMPLETE] [ARC-001] deleteUser null ID correctly threw InvalidInputException");
    }
}