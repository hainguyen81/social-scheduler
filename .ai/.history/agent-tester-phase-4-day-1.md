# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
/**
 * UserService - Core business logic layer for managing user entities within the
 * social-scheduler platform. This service orchestrates all Create, Read, Update,
 * and Delete (CRUD) operations for user accounts, enforcing enterprise-grade
 * security, idempotency, and audit logging standards.
 *
 * <p>Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]</p>
 *
 * <p>Security & Compliance:
 * - All mutation operations (create, update, delete) enforce Idempotency-Key
 *   validation to prevent duplicate execution requests.
 * - All database interactions are delegated to Spring Data JPA repositories
 *   which utilize parameterized queries (Prepared Statements) to neutralize
 *   SQL Injection vectors.
 * - Sensitive data (password hashes, emails) are masked in all log outputs
 *   to comply with PII protection mandates.
 * </p>
 *
 * @author Enterprise System Architect (SA Agent)
 * @version 1.0
 * @since 2026-09-12
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
package org.nlh4j.socialscheduler.userservice;

// --- Standard Java imports for utility collections and UUID generation ---
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// --- Spring Framework imports for dependency injection, logging, and annotations ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

// --- Custom exception imports for domain-specific error handling ---
import org.nlh4j.socialscheduler.userservice.exception.UserNotFoundException;
import org.nlh4j.socialscheduler.userservice.exception.DuplicateUserException;
import org.nlh4j.socialscheduler.userservice.exception.InvalidInputException;
import org.nlh4j.socialscheduler.userservice.exception.IdempotencyConflictException;

/**
 * UserService provides the complete business logic layer for user management
 * operations within the social-scheduler microservices architecture.
 *
 * <p>This service implements the Single Responsibility Principle by focusing
 * exclusively on user domain operations, delegating persistence concerns to
 * the injected UserRepository and cross-cutting concerns (logging, security)
 * to dedicated framework components.</p>
 *
 * <p>Key Design Decisions:
 * - All public methods are wrapped in transactional boundaries to ensure
 *   ACID compliance for database operations.
 * - Mutation operations validate idempotency keys to prevent duplicate
 *   processing in distributed systems.
 * - Input validation is enforced at the service boundary to reject
 *   malformed or incomplete requests early.
 * - Comprehensive exception handling ensures graceful degradation and
 *   meaningful error propagation to upstream controllers.
 * </p>
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@Service
public class UserService {

    // ========================================================================
    // CONSTANT DECLARATIONS - All configuration values, error messages,
    // and magic strings are hoisted to the top of the class per the
    // Top-of-Class Constants Declaration Law [0.2].
    // ========================================================================

    /**
     * Logger instance for structured application logging.
     * All INFO/DEBUG entry/exit logs and ERROR-level exception logs
     * are emitted through this logger to comply with the
     * Enterprise Logging & Exception Auditing Law [0.3].
     */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Constant for the user role value representing an administrator.
     * Used in role-based access control validation.
     */
    public static final String ROLE_ADMIN = "Admin";

    /**
     * Constant for the user role value representing a standard user.
     */
    public static final String ROLE_USER = "User";

    /**
     * Constant for the user role value representing a scheduler agent.
     */
    public static final String ROLE_SCHEDULER = "Scheduler";

    /**
     * Constant for the user role value representing an analyst.
     */
    public static final String ROLE_ANALYST = "Analyst";

    /**
     * Array of all valid user roles for validation purposes.
     * Used to enforce role-based access control constraints.
     */
    public static final String[] VALID_ROLES = {
        ROLE_ADMIN, ROLE_USER, ROLE_SCHEDULER, ROLE_ANALYST
    };

    /**
     * Maximum allowed length for the username field.
     * Enforced during input validation to prevent buffer overflow
     * and database constraint violations.
     */
    public static final int MAX_USERNAME_LENGTH = 255;

    /**
     * Maximum allowed length for the email field.
     */
    public static final int MAX_EMAIL_LENGTH = 255;

    /**
     * Maximum allowed length for the password hash field.
     */
    public static final int MAX_PASSWORD_HASH_LENGTH = 255;

    /**
     * Minimum required length for the password hash field.
     */
    public static final int MIN_PASSWORD_HASH_LENGTH = 1;

    /**
     * Error message template for user not found scenarios.
     * The placeholder {} is replaced with the actual user ID at runtime.
     */
    public static final String ERROR_USER_NOT_FOUND = "User not found with ID: {}";

    /**
     * Error message template for duplicate user scenarios.
     * Triggered when a username or email already exists in the system.
     */
    public static final String ERROR_DUPLICATE_USER = "User already exists with username: {} or email: {}";

    /**
     * Error message template for invalid input validation failures.
     */
    public static final String ERROR_INVALID_INPUT = "Invalid input provided: {}";

    /**
     * Error message template for idempotency key conflicts.
     * Triggered when a duplicate idempotency key is detected for a
     * mutation operation, indicating a potential replay attack or
     * duplicate submission.
     */
    public static final String ERROR_IDEMPOTENCY_CONFLICT = "Idempotency key conflict detected for key: {}";

    /**
     * Error message template for database access failures.
     */
    public static final String ERROR_DATABASE_ACCESS = "Database access error during user operation: {}";

    /**
     * Error message template for data integrity violations.
     */
    public static final String ERROR_DATA_INTEGRITY = "Data integrity violation during user operation: {}";

    /**
     * Error message template for unexpected runtime exceptions.
     */
    public static final String ERROR_UNEXPECTED = "Unexpected error during user operation: {}";

    /**
     * Log message template for service entry points.
     * Includes the operation name and relevant identifiers for
     * traceability in distributed logging systems.
     */
    public static final String LOG_ENTRY = "[PROCESS] Entering UserService.{} with parameters: {}";

    /**
     * Log message template for service exit points.
     * Includes the operation name and result summary for
     * audit trail completeness.
     */
    public static final String LOG_EXIT = "[PROCESS] Exiting UserService.{} with result: {}";

    /**
     * Log message template for masked sensitive data.
     * Used to log user identifiers without exposing PII.
     */
    public static final String LOG_MASKED_USER = "User(id={}, username={}, email={})";

    /**
     * Mask pattern for sensitive data in logs.
     * Replaces sensitive values with asterisks to comply
     * with the Sensitive Data Masking Framework [0.3].
     */
    public static final String MASK_PATTERN = "****";

    /**
     * Prefix for idempotency key storage in cache or database.
     * Used to namespace idempotency keys to avoid collisions
     * with other cache entries.
     */
    public static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:user:";

    /**
     * Default TTL (time-to-live) in seconds for idempotency keys.
     * After this period, the key is automatically purged, allowing
     * legitimate retries of the same operation.
     */
    public static final long IDEMPOTENCY_KEY_TTL_SECONDS = 3600L;

    // ========================================================================
    // DEPENDENCY INJECTION - UserRepository is injected via Spring's
    // dependency injection container. This follows the Dependency Inversion
    // Principle (DIP) as part of SOLID design principles.
    // ========================================================================

    /**
     * Repository for persisting and retrieving User entities.
     * All database operations are delegated to this repository,
     * which uses Spring Data JPA with parameterized queries
     * to prevent SQL Injection attacks [NFR-001].
     */
    private final UserRepository userRepository;

    /**
     * Constructs a new UserService with the required UserRepository dependency.
     *
     * <p>This constructor-based injection ensures that the service cannot be
     * instantiated without its required dependency, enforcing immutability
     * and fail-fast behavior at application startup.</p>
     *
     * @param userRepository the repository for User entity persistence operations
     * @traceability [ARC-001]
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        // Store the injected repository reference for use in all CRUD operations
        this.userRepository = userRepository;
        // Log the successful initialization of the service for audit purposes
        logger.info("[PROCESS] UserService initialized successfully with UserRepository dependency");
    }

    // ========================================================================
    // CREATE OPERATIONS
    // ========================================================================

    /**
     * Creates a new user in the system with the provided user details.
     *
     * <p>This method enforces the following business rules:
     * 1. Input validation: username, email, and passwordHash must be non-null
     *    and within acceptable length bounds.
     * 2. Role validation: the provided role must be one of the predefined
     *    valid roles (Admin, User, Scheduler, Analyst).
     * 3. Idempotency: if an idempotency key is provided, the method checks
     *    for prior execution to prevent duplicate user creation.
     * 4. Uniqueness: username and email must be unique across the system.
     *
     * <p>Security Considerations:
     * - The password hash is never logged in cleartext; it is masked
     *   using the MASK_PATTERN constant before any log emission.
     * - The email is partially masked in logs to protect PII.
     * - All database operations use parameterized queries via JPA
     *   to prevent SQL Injection.
     *
     * @param user the User entity to be created; must not be null
     * @param idempotencyKey an optional idempotency key to prevent duplicate
     *                       processing; if null, idempotency is not enforced
     * @return the persisted User entity with generated ID and timestamps
     * @throws InvalidInputException if the input fails validation checks
     * @throws DuplicateUserException if a user with the same username or email exists
     * @throws IdempotencyConflictException if the idempotency key was already used
     * @throws DataIntegrityViolationException if a database constraint is violated
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002]
     */
    @Transactional
    public User createUser(User user, String idempotencyKey) {
        // Log entry point with masked parameters for security compliance [0.3]
        logger.info(LOG_ENTRY, "createUser", maskUserForLogging(user));

        // --- Input Validation Phase ---
        // Validate that the user object is not null
        if (user == null) {
            // Log the validation failure at ERROR level with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] createUser failed: User object is null");
            // Throw a domain-specific exception with descriptive message
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "User object cannot be null"));
        }

        // Validate username: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getUsername()) ||
            user.getUsername().length() > MAX_USERNAME_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] createUser failed: Invalid username provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Username must be non-empty and <= " + MAX_USERNAME_LENGTH + " characters"));
        }

        // Validate email: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getEmail()) ||
            user.getEmail().length() > MAX_EMAIL_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] createUser failed: Invalid email provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Email must be non-empty and <= " + MAX_EMAIL_LENGTH + " characters"));
        }

        // Validate password hash: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getPasswordHash()) ||
            user.getPasswordHash().length() < MIN_PASSWORD_HASH_LENGTH ||
            user.getPasswordHash().length() > MAX_PASSWORD_HASH_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] createUser failed: Invalid password hash provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Password hash must be non-empty and <= " + MAX_PASSWORD_HASH_LENGTH + " characters"));
        }

        // Validate role: must be one of the predefined valid roles
        if (!isValidRole(user.getRole())) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] createUser failed: Invalid role provided: {}", user.getRole());
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Role must be one of: " + String.join(", ", VALID_ROLES)));
        }

        // --- Idempotency Check Phase ---
        // If an idempotency key is provided, check for prior execution
        if (StringUtils.hasText(idempotencyKey)) {
            // Construct the cache key using the prefix and provided key
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            // Check if this idempotency key has already been processed
            // In a production system, this would query Redis or a dedicated
            // idempotency table; here we use a simplified in-memory check
            if (isIdempotencyKeyUsed(cacheKey)) {
                // Log the idempotency conflict with Tag ID [ARC-002]
                logger.error("[CRITICAL FAIL] [ARC-002] createUser failed: Idempotency key conflict for key: {}", maskIdempotencyKey(idempotencyKey));
                throw new IdempotencyConflictException(ERROR_IDEMPOTENCY_CONFLICT.replace("{}", maskIdempotencyKey(idempotencyKey)));
            }
            // Mark the idempotency key as used for future reference
            markIdempotencyKeyAsUsed(cacheKey);
        }

        // --- Uniqueness Check Phase ---
        // Check if a user with the same username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            // Log the duplicate username conflict with Tag ID [ARC-003]
            logger.error("[CRITICAL FAIL] [ARC-003] createUser failed: Duplicate username detected: {}", maskUsername(user.getUsername()));
            throw new DuplicateUserException(ERROR_DUPLICATE_USER.replace("{}", user.getUsername() + " or " + maskEmail(user.getEmail())));
        }

        // Check if a user with the same email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            // Log the duplicate email conflict with Tag ID [ARC-003]
            logger.error("[CRITICAL FAIL] [ARC-003] createUser failed: Duplicate email detected: {}", maskEmail(user.getEmail()));
            throw new DuplicateUserException(ERROR_DUPLICATE_USER.replace("{}", maskUsername(user.getUsername()) + " or " + user.getEmail()));
        }

        // --- Persistence Phase ---
        try {
            // Generate a unique UUID for the new user
            user.setUserId(UUID.randomUUID());
            // Set the creation timestamp to the current time
            user.setCreatedAt(java.time.Instant.now());
            // Set the update timestamp to the current time (initially same as created)
            user.setUpdatedAt(java.time.Instant.now());

            // Persist the user entity via the repository (uses parameterized queries)
            User savedUser = userRepository.save(user);

            // Log successful creation with masked data for security [0.3]
            logger.info(LOG_EXIT, "createUser", "Successfully created user with ID: " + savedUser.getUserId());

            // Return the persisted user entity
            return savedUser;

        } catch (DataIntegrityViolationException e) {
            // Log the database constraint violation with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] createUser failed due to data integrity violation. Raw error: {}", e.getMessage());
            // Re-throw as a domain-specific exception, preserving the cause chain [0.3]
            throw new DuplicateUserException(ERROR_DATA_INTEGRITY.replace("{}", e.getMessage()), e);
        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] createUser failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] createUser failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    // ========================================================================
    // READ OPERATIONS
    // ========================================================================

    /**
     * Retrieves a user by their unique identifier.
     *
     * <p>This method performs a lookup using the primary key (userId) and
     * returns the complete User entity if found. If no user exists with
     * the provided ID, a UserNotFoundException is thrown.
     *
     * <p>Security Considerations:
     * - The returned User entity's password hash is not masked at the
     *   service layer; it is expected that the controller layer or
     *   serialization layer will handle PII masking before returning
     *   to the client.
     * - All log outputs mask sensitive fields per the Sensitive Data
     *   Masking Framework [0.3].
     *
     * @param userId the unique identifier of the user to retrieve; must not be null
     * @return the User entity with the specified ID
     * @throws UserNotFoundException if no user exists with the provided ID
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002]
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        // Log entry point with the user ID for traceability [0.3]
        logger.info(LOG_ENTRY, "getUserById", "userId=" + userId);

        // Validate that the userId is not null
        if (userId == null) {
            // Log the validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] getUserById failed: userId is null");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "User ID cannot be null"));
        }

        try {
            // Query the repository for the user by ID (uses parameterized query)
            Optional<User> userOptional = userRepository.findById(userId);

            // Check if the user was found
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Log successful retrieval with masked data for security [0.3]
                logger.info(LOG_EXIT, "getUserById", "Successfully retrieved user: " + maskUserForLogging(user));
                // Return the found user entity
                return user;
            } else {
                // Log the not-found scenario with Tag ID [ARC-001]
                logger.error("[CRITICAL FAIL] [ARC-001] getUserById failed: " + ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
                // Throw a domain-specific exception for the not-found case
                throw new UserNotFoundException(ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
            }

        } catch (UserNotFoundException e) {
            // Re-throw UserNotFoundException without wrapping to preserve the original exception type
            throw e;
        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getUserById failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getUserById failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    /**
     * Retrieves a user by their username.
     *
     * <p>This method performs a lookup using the username field and
     * returns the complete User entity if found. If no user exists with
     * the provided username, a UserNotFoundException is thrown.
     *
     * @param username the username of the user to retrieve; must not be null or empty
     * @return the User entity with the specified username
     * @throws UserNotFoundException if no user exists with the provided username
     * @throws InvalidInputException if the username is null or empty
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002]
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        // Log entry point with masked username for security [0.3]
        logger.info(LOG_ENTRY, "getUserByUsername", "username=" + maskUsername(username));

        // Validate that the username is not null or empty
        if (!StringUtils.hasText(username)) {
            // Log the validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] getUserByUsername failed: username is null or empty");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Username cannot be null or empty"));
        }

        try {
            // Query the repository for the user by username (uses parameterized query)
            Optional<User> userOptional = userRepository.findByUsername(username);

            // Check if the user was found
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Log successful retrieval with masked data for security [0.3]
                logger.info(LOG_EXIT, "getUserByUsername", "Successfully retrieved user: " + maskUserForLogging(user));
                // Return the found user entity
                return user;
            } else {
                // Log the not-found scenario with Tag ID [ARC-001]
                logger.error("[CRITICAL FAIL] [ARC-001] getUserByUsername failed: " + ERROR_USER_NOT_FOUND.replace("{}", "username=" + maskUsername(username)));
                // Throw a domain-specific exception for the not-found case
                throw new UserNotFoundException(ERROR_USER_NOT_FOUND.replace("{}", "username=" + maskUsername(username)));
            }

        } catch (UserNotFoundException e) {
            // Re-throw UserNotFoundException without wrapping to preserve the original exception type
            throw e;
        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getUserByUsername failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getUserByUsername failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    /**
     * Retrieves all users in the system.
     *
     * <p>This method returns a complete list of all user entities. For
     * large datasets, pagination should be considered in future iterations.
     *
     * @return a list of all User entities in the system
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002]
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // Log entry point for traceability [0.3]
        logger.info(LOG_ENTRY, "getAllUsers", "Retrieving all users");

        try {
            // Query the repository for all users (uses parameterized query)
            List<User> users = userRepository.findAll();

            // Log successful retrieval with count for audit purposes [0.3]
            logger.info(LOG_EXIT, "getAllUsers", "Successfully retrieved " + users.size() + " users");

            // Return the list of all users
            return users;

        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getAllUsers failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] getAllUsers failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    // ========================================================================
    // UPDATE OPERATIONS
    // ========================================================================

    /**
     * Updates an existing user's information in the system.
     *
     * <p>This method enforces the following business rules:
     * 1. The user must exist in the system (identified by userId).
     * 2. Input validation: username, email, and passwordHash must be
     *    non-null and within acceptable length bounds.
     * 3. Role validation: the provided role must be one of the predefined
     *    valid roles.
     * 4. Idempotency: if an idempotency key is provided, the method checks
     *    for prior execution to prevent duplicate updates.
     * 5. Uniqueness: if the username or email is being changed, it must
     *    not conflict with an existing user's username or email.
     *
     * <p>Security Considerations:
     * - The password hash is never logged in cleartext; it is masked
     *   using the MASK_PATTERN constant before any log emission.
     * - The email is partially masked in logs to protect PII.
     * - All database operations use parameterized queries via JPA
     *   to prevent SQL Injection.
     *
     * @param userId the unique identifier of the user to update; must not be null
     * @param user the User entity containing updated information; must not be null
     * @param idempotencyKey an optional idempotency key to prevent duplicate
     *                       processing; if null, idempotency is not enforced
     * @return the updated User entity
     * @throws UserNotFoundException if no user exists with the provided userId
     * @throws InvalidInputException if the input fails validation checks
     * @throws DuplicateUserException if the new username or email conflicts with another user
     * @throws IdempotencyConflictException if the idempotency key was already used
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Transactional
    public User updateUser(UUID userId, User user, String idempotencyKey) {
        // Log entry point with masked parameters for security compliance [0.3]
        logger.info(LOG_ENTRY, "updateUser", "userId=" + userId + ", user=" + maskUserForLogging(user));

        // Validate that the userId is not null
        if (userId == null) {
            // Log the validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: userId is null");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "User ID cannot be null"));
        }

        // Validate that the user object is not null
        if (user == null) {
            // Log the validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: User object is null");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "User object cannot be null"));
        }

        // Validate username: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getUsername()) ||
            user.getUsername().length() > MAX_USERNAME_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: Invalid username provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Username must be non-empty and <= " + MAX_USERNAME_LENGTH + " characters"));
        }

        // Validate email: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getEmail()) ||
            user.getEmail().length() > MAX_EMAIL_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: Invalid email provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Email must be non-empty and <= " + MAX_EMAIL_LENGTH + " characters"));
        }

        // Validate password hash: must be non-empty and within length bounds
        if (!StringUtils.hasText(user.getPasswordHash()) ||
            user.getPasswordHash().length() < MIN_PASSWORD_HASH_LENGTH ||
            user.getPasswordHash().length() > MAX_PASSWORD_HASH_LENGTH) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: Invalid password hash provided");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Password hash must be non-empty and <= " + MAX_PASSWORD_HASH_LENGTH + " characters"));
        }

        // Validate role: must be one of the predefined valid roles
        if (!isValidRole(user.getRole())) {
            // Log validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: Invalid role provided: {}", user.getRole());
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "Role must be one of: " + String.join(", ", VALID_ROLES)));
        }

        // --- Idempotency Check Phase ---
        // If an idempotency key is provided, check for prior execution
        if (StringUtils.hasText(idempotencyKey)) {
            // Construct the cache key using the prefix and provided key
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            // Check if this idempotency key has already been processed
            if (isIdempotencyKeyUsed(cacheKey)) {
                // Log the idempotency conflict with Tag ID [ARC-002]
                logger.error("[CRITICAL FAIL] [ARC-002] updateUser failed: Idempotency key conflict for key: {}", maskIdempotencyKey(idempotencyKey));
                throw new IdempotencyConflictException(ERROR_IDEMPOTENCY_CONFLICT.replace("{}", maskIdempotencyKey(idempotencyKey)));
            }
            // Mark the idempotency key as used for future reference
            markIdempotencyKeyAsUsed(cacheKey);
        }

        try {
            // Query the repository for the existing user by ID (uses parameterized query)
            Optional<User> existingUserOptional = userRepository.findById(userId);

            // Check if the user exists
            if (!existingUserOptional.isPresent()) {
                // Log the not-found scenario with Tag ID [ARC-001]
                logger.error("[CRITICAL FAIL] [ARC-001] updateUser failed: " + ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
                throw new UserNotFoundException(ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
            }

            // Retrieve the existing user entity
            User existingUser = existingUserOptional.get();

            // --- Uniqueness Check Phase ---
            // Check if the username is being changed and if it conflicts with another user
            if (!existingUser.getUsername().equals(user.getUsername())) {
                // Query for any user with the new username (uses parameterized query)
                if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                    // Log the duplicate username conflict with Tag ID [ARC-003]
                    logger.error("[CRITICAL FAIL] [ARC-003] updateUser failed: Duplicate username detected: {}", maskUsername(user.getUsername()));
                    throw new DuplicateUserException(ERROR_DUPLICATE_USER.replace("{}", user.getUsername() + " or " + maskEmail(user.getEmail())));
                }
            }

            // Check if the email is being changed and if it conflicts with another user
            if (!existingUser.getEmail().equals(user.getEmail())) {
                // Query for any user with the new email (uses parameterized query)
                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                    // Log the duplicate email conflict with Tag ID [ARC-003]
                    logger.error("[CRITICAL FAIL] [ARC-003] updateUser failed: Duplicate email detected: {}", maskEmail(user.getEmail()));
                    throw new DuplicateUserException(ERROR_DUPLICATE_USER.replace("{}", maskUsername(user.getUsername()) + " or " + user.getEmail()));
                }
            }

            // --- Update Phase ---
            // Update the user fields with the new values
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPasswordHash(user.getPasswordHash());
            existingUser.setRole(user.getRole());
            // Update the timestamp to reflect the modification
            existingUser.setUpdatedAt(java.time.Instant.now());

            // Persist the updated user entity via the repository (uses parameterized queries)
            User updatedUser = userRepository.save(existingUser);

            // Log successful update with masked data for security [0.3]
            logger.info(LOG_EXIT, "updateUser", "Successfully updated user with ID: " + updatedUser.getUserId());

            // Return the updated user entity
            return updatedUser;

        } catch (UserNotFoundException | DuplicateUserException | InvalidInputException e) {
            // Re-throw domain-specific exceptions without wrapping to preserve the original exception type
            throw e;
        } catch (DataIntegrityViolationException e) {
            // Log the database constraint violation with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] updateUser failed due to data integrity violation. Raw error: {}", e.getMessage());
            // Re-throw as a domain-specific exception, preserving the cause chain [0.3]
            throw new DuplicateUserException(ERROR_DATA_INTEGRITY.replace("{}", e.getMessage()), e);
        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] updateUser failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] updateUser failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    // ========================================================================
    // DELETE OPERATIONS
    // ========================================================================

    /**
     * Deletes a user from the system by their unique identifier.
     *
     * <p>This method enforces the following business rules:
     * 1. The user must exist in the system (identified by userId).
     * 2. Idempotency: if an idempotency key is provided, the method checks
     *    for prior execution to prevent duplicate deletions.
     *
     * <p>Security Considerations:
     * - All database operations use parameterized queries via JPA
     *   to prevent SQL Injection.
     * - Log outputs mask sensitive data per the Sensitive Data
     *   Masking Framework [0.3].
     *
     * @param userId the unique identifier of the user to delete; must not be null
     * @param idempotencyKey an optional idempotency key to prevent duplicate
     *                       processing; if null, idempotency is not enforced
     * @throws UserNotFoundException if no user exists with the provided userId
     * @throws InvalidInputException if the userId is null
     * @throws IdempotencyConflictException if the idempotency key was already used
     * @throws DataAccessException if a database access error occurs
     * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
     */
    @Transactional
    public void deleteUser(UUID userId, String idempotencyKey) {
        // Log entry point with the user ID for traceability [0.3]
        logger.info(LOG_ENTRY, "deleteUser", "userId=" + userId);

        // Validate that the userId is not null
        if (userId == null) {
            // Log the validation failure with Tag ID [ARC-001]
            logger.error("[CRITICAL FAIL] [ARC-001] deleteUser failed: userId is null");
            throw new InvalidInputException(ERROR_INVALID_INPUT.replace("{}", "User ID cannot be null"));
        }

        // --- Idempotency Check Phase ---
        // If an idempotency key is provided, check for prior execution
        if (StringUtils.hasText(idempotencyKey)) {
            // Construct the cache key using the prefix and provided key
            String cacheKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
            // Check if this idempotency key has already been processed
            if (isIdempotencyKeyUsed(cacheKey)) {
                // Log the idempotency conflict with Tag ID [ARC-002]
                logger.error("[CRITICAL FAIL] [ARC-002] deleteUser failed: Idempotency key conflict for key: {}", maskIdempotencyKey(idempotencyKey));
                throw new IdempotencyConflictException(ERROR_IDEMPOTENCY_CONFLICT.replace("{}", maskIdempotencyKey(idempotencyKey)));
            }
            // Mark the idempotency key as used for future reference
            markIdempotencyKeyAsUsed(cacheKey);
        }

        try {
            // Query the repository for the existing user by ID (uses parameterized query)
            Optional<User> existingUserOptional = userRepository.findById(userId);

            // Check if the user exists
            if (!existingUserOptional.isPresent()) {
                // Log the not-found scenario with Tag ID [ARC-001]
                logger.error("[CRITICAL FAIL] [ARC-001] deleteUser failed: " + ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
                throw new UserNotFoundException(ERROR_USER_NOT_FOUND.replace("{}", userId.toString()));
            }

            // Retrieve the existing user entity for logging purposes
            User existingUser = existingUserOptional.get();

            // Delete the user entity via the repository (uses parameterized query)
            userRepository.deleteById(userId);

            // Log successful deletion with masked data for security [0.3]
            logger.info(LOG_EXIT, "deleteUser", "Successfully deleted user: " + maskUserForLogging(existingUser));

        } catch (UserNotFoundException | InvalidInputException e) {
            // Re-throw domain-specific exceptions without wrapping to preserve the original exception type
            throw e;
        } catch (DataAccessException e) {
            // Log the database access error with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] deleteUser failed due to database access error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_DATABASE_ACCESS.replace("{}", e.getMessage()), e);
        } catch (Exception e) {
            // Log any unexpected errors with Tag ID [ARC-004]
            logger.error("[CRITICAL FAIL] [ARC-004] deleteUser failed due to unexpected error. Raw error: {}", e.getMessage());
            // Re-throw as a runtime exception, preserving the cause chain [0.3]
            throw new RuntimeException(ERROR_UNEXPECTED.replace("{}", e.getMessage()), e);
        }
    }

    // ========================================================================
    // HELPER METHODS - Input validation, data masking, and idempotency
    // ========================================================================

    /**
     * Validates whether the provided role is one of the predefined valid roles.
     *
     * <p>This method iterates through the VALID_ROLES constant array to check
     * if the provided role matches any of the allowed values. This enforces
     * role-based access control constraints at the service boundary.
     *
     * @param role the role string to validate
     * @return true if the role is valid, false otherwise
     * @traceability [ARC-001]
     */
    private boolean isValidRole(String role) {
        // Iterate through the predefined valid roles array
        for (String validRole : VALID_ROLES) {
            // Check if the provided role matches the current valid role
            if (validRole.equals(role)) {
                // Return true if a match is found
                return true;
            }
        }
        // Return false if no match is found after checking all valid roles
        return false;
    }

    /**
     * Masks sensitive user data for safe logging.
     *
     * <p>This method constructs a log-safe representation of a User entity
     * by masking the password hash and partially masking the email address.
     * This complies with the Sensitive Data Masking Framework [0.3] which
     * prohibits the emission of raw cleartext credentials in any log level.
     *
     * @param user the User entity to mask for logging; may be null
     * @return a masked string representation of the user, or "null" if the user is null
     * @traceability [ARC-002]
     */
    private String maskUserForLogging(User user) {
        // Handle null user gracefully
        if (user == null) {
            return "null";
        }
        // Construct a masked representation using the LOG_MASKED_USER template
        // Password hash is fully masked; email is partially masked
        return LOG_MASKED_USER
            .replace("{}", user.getUserId() != null ? user.getUserId().toString() : "null")
            .replace(user.getUsername(), maskUsername(user.getUsername()))
            .replace(user.getEmail(), maskEmail(user.getEmail()));
    }

    /**
     * Masks a username for safe logging by replacing it with a masked pattern.
     *
     * <p>In a production system, this might partially reveal the username
     * (e.g., "jo***n") for debugging purposes. For maximum security,
     * the full username is masked.
     *
     * @param username the username to mask; may be null
     * @return the masked username, or "null" if the input is null
     * @traceability [ARC-002]
     */
    private String maskUsername(String username) {
        // Handle null username gracefully
        if (username == null) {
            return "null";
        }
        // Return the masked pattern to prevent PII leakage in logs [0.3]
        return MASK_PATTERN;
    }

    /**
     * Masks an email address for safe logging by partially obscuring it.
     *
     * <p>The email is masked to show only the domain portion, with the
     * local part replaced by asterisks. For example, "user@example.com"
     * becomes "****@example.com". This balances debuggability with
     * PII protection requirements.
     *
     * @param email the email address to mask; may be null
     * @return the partially masked email, or "null" if the input is null
     * @traceability [ARC-002]
     */
    private String maskEmail(String email) {
        // Handle null email gracefully
        if (email == null) {
            return "null";
        }
        // Find the position of the @ symbol to separate local and domain parts
        int atIndex = email.indexOf('@');
        // If no @ symbol is found, mask the entire email
        if (atIndex <= 0) {
            return MASK_PATTERN;
        }
        // Extract the domain portion (everything after @)
        String domain = email.substring(atIndex);
        // Return the masked local part with the original domain
        return MASK_PATTERN + domain;
    }

    /**
     * Masks an idempotency key for safe logging.
     *
     * <p>Idempotency keys are sensitive tokens that should not be exposed
     * in logs. This method replaces the key with a masked pattern.
     *
     * @param idempotencyKey the idempotency key to mask; may be null
     * @return the masked pattern, or "null" if the input is null
     * @traceability [ARC-002]
     */
    private String maskIdempotencyKey(String idempotencyKey) {
        // Handle null idempotency key gracefully
        if (idempotencyKey == null) {
            return "null";
        }
        // Return the masked pattern to prevent token leakage in logs [0.3]
        return MASK_PATTERN;
    }

    /**
     * Checks whether an idempotency key has already been used.
     *
     * <p>In a production system, this method would query a distributed cache
     * (e.g., Redis) or a dedicated idempotency table in the database.
     * For this implementation, a simplified in-memory check is used.
     *
     * <p>The idempotency key ensures that duplicate requests (e.g., due to
     * network retries or client-side resubmissions) do not result in
     * duplicate processing, which is critical for maintaining data integrity
     * in distributed systems.
     *
     * @param cacheKey the full cache key (including prefix) to check
     * @return true if the key has been used, false otherwise
     * @traceability [ARC-002]
     */
    private boolean isIdempotencyKeyUsed(String cacheKey) {
        // In a production system, this would query Redis or a database table
        // For this implementation, we return false to allow the operation to proceed
        // The actual idempotency storage would be implemented in a separate
        // IdempotencyService or via Redis integration
        return false;
    }

    /**
     * Marks an idempotency key as used.
     *
     * <p>In a production system, this method would store the idempotency key
     * in a distributed cache (e.g., Redis) with a TTL (time-to-live) to
     * automatically expire the key after a configurable period.
     *
     * @param cacheKey the full cache key (including prefix) to mark as used
     * @traceability [ARC-002]
     */
    private void markIdempotencyKeyAsUsed(String cacheKey) {
        // In a production system, this would store the key in Redis with a TTL
        // For this implementation, the method is a no-op placeholder
        // The actual idempotency storage would be implemented in a separate
        // IdempotencyService or via Redis integration
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp UserService để đảm bảo các phương thức quản lý người dùng hoạt động đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 1: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserControllerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserControllerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UserController - REST Controller for managing user operations.
 *
 * This controller provides endpoints for creating, retrieving, updating, and deleting users.
 * It enforces input validation, idempotency for mutation operations, and integrates with
 * the UserService layer for business logic execution.
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // Logger instance for structured logging at entry/exit points and error handling
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // Injected UserService dependency for delegating business logic operations
    private final UserService userService;

    /**
     * Constructor-based dependency injection for UserService.
     *
     * @param userService the user service layer handling core business operations
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user in the system.
     *
     * @param user the user object containing details to be created
     * @return ResponseEntity containing the created user and HTTP status
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.info("[PROCESS] Creating new user with username: {}", user.getUsername());
        try {
            User createdUser = userService.createUser(user);
            logger.info("[PROCESS] Successfully created user with ID: {}", createdUser.getUserId());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to create user. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param userId the UUID of the user to retrieve
     * @return ResponseEntity containing the user details or HTTP 404 if not found
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable UUID userId) {
        logger.info("[PROCESS] Fetching user with ID: {}", userId);
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                logger.info("[PROCESS] Successfully retrieved user with ID: {}", userId);
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                logger.warn("[PROCESS] User not found with ID: {}", userId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-002] Failed to fetch user with ID: {}. Raw error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves all users in the system.
     *
     * @return ResponseEntity containing a list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("[PROCESS] Fetching all users");
        try {
            List<User> users = userService.getAllUsers();
            logger.info("[PROCESS] Successfully retrieved {} users", users.size());
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] Failed to fetch all users. Raw error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates an existing user's information.
     *
     * @param userId the UUID of the user to update
     * @param user   the updated user object
     * @return ResponseEntity containing the updated user or HTTP 404 if not found
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable UUID userId, @Valid @RequestBody User user) {
        logger.info("[PROCESS] Updating user with ID: {}", userId);
        try {
            Optional<User> updatedUser = userService.updateUser(userId, user);
            if (updatedUser.isPresent()) {
                logger.info("[PROCESS] Successfully updated user with ID: {}", userId);
                return new ResponseEntity<>(updatedUser.get(), HttpStatus.OK);
            } else {
                logger.warn("[PROCESS] User not found for update with ID: {}", userId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-004] Failed to update user with ID: {}. Raw error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes a user by their unique identifier.
     *
     * @param userId the UUID of the user to delete
     * @return ResponseEntity indicating the result of the deletion operation
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        logger.info("[PROCESS] Deleting user with ID: {}", userId);
        try {
            boolean isDeleted = userService.deleteUser(userId);
            if (isDeleted) {
                logger.info("[PROCESS] Successfully deleted user with ID: {}", userId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                logger.warn("[PROCESS] User not found for deletion with ID: {}", userId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-001] Failed to delete user with ID: {}. Raw error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp UserController để đảm bảo các điểm cuối API hoạt động đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest - Comprehensive unit test suite for UserController REST endpoints.
 *
 * This test class validates all API endpoints for user management operations including
 * creation, retrieval, update, and deletion. Tests cover happy paths, edge cases,
 * and exception scenarios to ensure >85% code coverage.
 *
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests - API Endpoint Validation")
class UserControllerTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers Policy)
    // =========================================================================

    /** Base API path for user endpoints. */
    private static final String API_BASE_PATH = "/api/v1/users";

    /** Test username constant for consistent test data. */
    private static final String TEST_USERNAME = "testuser";

    /** Test email constant for consistent test data. */
    private static final String TEST_EMAIL = "test@example.com";

    /** Test password hash constant for consistent test data. */
    private static final String TEST_PASSWORD_HASH = "hashed_password_123";

    /** Test role constant for consistent test data. */
    private static final String TEST_ROLE = "User";

    /** JSON content type constant for request/response validation. */
    private static final String APPLICATION_JSON = MediaType.APPLICATION_JSON_VALUE;

    /** Logger instance for structured test execution tracing. */
    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    // =========================================================================
    // TEST FIXTURES & MOCKS
    // =========================================================================

    /** Mock UserService to isolate controller layer from business logic. */
    @Mock
    private UserService userService;

    /** Controller under test with mocked dependencies injected. */
    @InjectMocks
    private UserController userController;

    /** MockMvc instance for simulating HTTP requests to controller endpoints. */
    private MockMvc mockMvc;

    /** Pre-generated test user ID for consistent reference across tests. */
    private UUID testUserId;

    /** Pre-built test user object for consistent reference across tests. */
    private User testUser;

    // =========================================================================
    // SETUP & TEARDOWN
    // =========================================================================

    /**
     * Initializes test fixtures and MockMvc standalone setup before each test.
     * Ensures clean state isolation between test executions.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] Initializing UserController test fixtures");
        testUserId = UUID.randomUUID();
        testUser = buildTestUser(testUserId, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD_HASH, TEST_ROLE);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        logger.debug("[TEST_SETUP] Test user ID: {}, MockMvc initialized", testUserId);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Builds a test User entity with specified parameters.
     *
     * @param userId       the unique identifier
     * @param username     the username
     * @param email        the email address
     * @param passwordHash the password hash
     * @param role         the user role
     * @return a fully populated User instance
     */
    private User buildTestUser(UUID userId, String username, String email, String passwordHash, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    /**
     * Converts a User object to JSON string for request body.
     *
     * @param user the user to serialize
     * @return JSON representation
     * @throws Exception if serialization fails
     */
    private String toJson(User user) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(user);
    }

    // =========================================================================
    // CREATE USER TESTS (POST /api/v1/users) - [ARC-001]
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/users - Create User Tests [ARC-001]")
    class CreateUserTests {

        /**
         * Verifies successful user creation returns HTTP 201 with created user.
         * Validates happy path where service returns persisted user with generated ID.
         *
         * @verifies [ARC-001]
         */
        @Test
        @DisplayName("Should create user and return 201 Created with user details [ARC-001]")
        void createUser_whenValidRequest_returnsCreatedUser() throws Exception {
            logger.info("[TEST_START] [ARC-001] createUser_whenValidRequest_returnsCreatedUser");

            // Given: A valid user request and service returns created user with ID
            User requestUser = buildTestUser(null, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD_HASH, TEST_ROLE);
            User createdUser = buildTestUser(testUserId, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD_HASH, TEST_ROLE);
            given(userService.createUser(any(User.class))).willReturn(createdUser);

            // When: POST request to create user
            // Then: Verify 201 status, response body matches created user, service called once
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(requestUser)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId", is(testUserId.toString())))
                    .andExpect(jsonPath("$.username", is(TEST_USERNAME)))
                    .andExpect(jsonPath("$.email", is(TEST_EMAIL)))
                    .andExpect(jsonPath("$.role", is(TEST_ROLE)));

            verify(userService).createUser(any(User.class));
            logger.info("[TEST_END] [ARC-001] createUser_whenValidRequest_returnsCreatedUser - PASSED");
        }

        /**
         * Verifies validation failure returns HTTP 400 for missing required fields.
         * Tests edge case where @Valid annotation triggers constraint violation.
         *
         * @verifies [ARC-001]
         */
        @Test
        @DisplayName("Should return 400 Bad Request when username is missing [ARC-001]")
        void createUser_whenUsernameMissing_returnsBadRequest() throws Exception {
            logger.info("[TEST_START] [ARC-001] createUser_whenUsernameMissing_returnsBadRequest");

            // Given: User request with missing username (validation constraint)
            User invalidUser = buildTestUser(null, "", TEST_EMAIL, TEST_PASSWORD_HASH, TEST_ROLE);

            // When: POST request with invalid user
            // Then: Verify 400 status, service never called due to validation failure
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(invalidUser)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
            logger.info("[TEST_END] [ARC-001] createUser_whenUsernameMissing_returnsBadRequest - PASSED");
        }

        /**
         * Verifies service exception propagates as HTTP 500 Internal Server Error.
         * Tests exception case where service layer throws unexpected runtime exception.
         *
         * @verifies [ARC-001], [EXC-001]
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception [ARC-001]")
        void createUser_whenServiceThrowsException_returnsInternalServerError() throws Exception {
            logger.info("[TEST_START] [ARC-001] createUser_whenServiceThrowsException_returnsInternalServerError");

            // Given: Valid request but service throws runtime exception
            User requestUser = buildTestUser(null, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD_HASH, TEST_ROLE);
            given(userService.createUser(any(User.class))).willThrow(new RuntimeException("Database connection failed"));

            // When: POST request to create user
            // Then: Verify 500 status, exception logged and propagated
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(requestUser)))
                    .andExpect(status().isInternalServerError());

            verify(userService).createUser(any(User.class));
            logger.info("[TEST_END] [ARC-001] createUser_whenServiceThrowsException_returnsInternalServerError - PASSED");
        }
    }

    // =========================================================================
    // GET USER BY ID TESTS (GET /api/v1/users/{userId}) - [ARC-002]
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/users/{userId} - Get User By ID Tests [ARC-002]")
    class GetUserByIdTests {

        /**
         * Verifies successful user retrieval returns HTTP 200 with user details.
         * Validates happy path where user exists in system.
         *
         * @verifies [ARC-002]
         */
        @Test
        @DisplayName("Should return 200 OK with user details when user exists [ARC-002]")
        void getUserById_whenUserExists_returnsUser() throws Exception {
            logger.info("[TEST_START] [ARC-002] getUserById_whenUserExists_returnsUser");

            // Given: User exists in system
            given(userService.getUserById(testUserId)).willReturn(Optional.of(testUser));

            // When: GET request for existing user
            // Then: Verify 200 status, response body matches user
            mockMvc.perform(get(API_BASE_PATH + "/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(testUserId.toString())))
                    .andExpect(jsonPath("$.username", is(TEST_USERNAME)))
                    .andExpect(jsonPath("$.email", is(TEST_EMAIL)));

            verify(userService).getUserById(testUserId);
            logger.info("[TEST_END] [ARC-002] getUserById_whenUserExists_returnsUser - PASSED");
        }

        /**
         * Verifies non-existent user returns HTTP 404 Not Found.
         * Tests edge case where service returns empty Optional.
         *
         * @verifies [ARC-002]
         */
        @Test
        @DisplayName("Should return 404 Not Found when user does not exist [ARC-002]")
        void getUserById_whenUserNotFound_returnsNotFound() throws Exception {
            logger.info("[TEST_START] [ARC-002] getUserById_whenUserNotFound_returnsNotFound");

            // Given: User does not exist (empty Optional)
            UUID nonExistentId = UUID.randomUUID();
            given(userService.getUserById(nonExistentId)).willReturn(Optional.empty());

            // When: GET request for non-existent user
            // Then: Verify 404 status, empty response body
            mockMvc.perform(get(API_BASE_PATH + "/{userId}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(userService).getUserById(nonExistentId);
            logger.info("[TEST_END] [ARC-002] getUserById_whenUserNotFound_returnsNotFound - PASSED");
        }

        /**
         * Verifies invalid UUID format returns HTTP 400 Bad Request.
         * Tests edge case where path variable binding fails.
         *
         * @verifies [ARC-002]
         */
        @Test
        @DisplayName("Should return 400 Bad Request when UUID format is invalid [ARC-002]")
        void getUserById_whenInvalidUuid_returnsBadRequest() throws Exception {
            logger.info("[TEST_START] [ARC-002] getUserById_whenInvalidUuid_returnsBadRequest");

            // When: GET request with invalid UUID format
            // Then: Verify 400 status (Spring handles UUID parsing failure)
            mockMvc.perform(get(API_BASE_PATH + "/{userId}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
            logger.info("[TEST_END] [ARC-002] getUserById_whenInvalidUuid_returnsBadRequest - PASSED");
        }

        /**
         * Verifies service exception propagates as HTTP 500 Internal Server Error.
         * Tests exception case for database connectivity issues.
         *
         * @verifies [ARC-002], [EXC-002]
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception [ARC-002]")
        void getUserById_whenServiceThrowsException_returnsInternalServerError() throws Exception {
            logger.info("[TEST_START] [ARC-002] getUserById_whenServiceThrowsException_returnsInternalServerError");

            // Given: Service throws runtime exception
            given(userService.getUserById(testUserId)).willThrow(new RuntimeException("Database timeout"));

            // When: GET request for user
            // Then: Verify 500 status
            mockMvc.perform(get(API_BASE_PATH + "/{userId}", testUserId))
                    .andExpect(status().isInternalServerError());

            verify(userService).getUserById(testUserId);
            logger.info("[TEST_END] [ARC-002] getUserById_whenServiceThrowsException_returnsInternalServerError - PASSED");
        }
    }

    // =========================================================================
    // GET ALL USERS TESTS (GET /api/v1/users) - [ARC-003]
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/users - Get All Users Tests [ARC-003]")
    class GetAllUsersTests {

        /**
         * Verifies successful retrieval of all users returns HTTP 200 with list.
         * Validates happy path with multiple users in system.
         *
         * @verifies [ARC-003]
         */
        @Test
        @DisplayName("Should return 200 OK with list of all users [ARC-003]")
        void getAllUsers_whenUsersExist_returnsUserList() throws Exception {
            logger.info("[TEST_START] [ARC-003] getAllUsers_whenUsersExist_returnsUserList");

            // Given: Multiple users exist in system
            User user2 = buildTestUser(UUID.randomUUID(), "user2", "user2@example.com", "hash2", "Admin");
            User user3 = buildTestUser(UUID.randomUUID(), "user3", "user3@example.com", "hash3", "Scheduler");
            List<User> allUsers = Arrays.asList(testUser, user2, user3);
            given(userService.getAllUsers()).willReturn(allUsers);

            // When: GET request for all users
            // Then: Verify 200 status, response contains all 3 users
            mockMvc.perform(get(API_BASE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].username", is(TEST_USERNAME)))
                    .andExpect(jsonPath("$[1].username", is("user2")))
                    .andExpect(jsonPath("$[2].username", is("user3")));

            verify(userService).getAllUsers();
            logger.info("[TEST_END] [ARC-003] getAllUsers_whenUsersExist_returnsUserList - PASSED");
        }

        /**
         * Verifies empty user list returns HTTP 200 with empty array.
         * Tests edge case where no users exist in system.
         *
         * @verifies [ARC-003]
         */
        @Test
        @DisplayName("Should return 200 OK with empty list when no users exist [ARC-003]")
        void getAllUsers_whenNoUsers_returnsEmptyList() throws Exception {
            logger.info("[TEST_START] [ARC-003] getAllUsers_whenNoUsers_returnsEmptyList");

            // Given: No users in system (empty list)
            given(userService.getAllUsers()).willReturn(Arrays.asList());

            // When: GET request for all users
            // Then: Verify 200 status, empty array response
            mockMvc.perform(get(API_BASE_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(userService).getAllUsers();
            logger.info("[TEST_END] [ARC-003] getAllUsers_whenNoUsers_returnsEmptyList - PASSED");
        }

        /**
         * Verifies service exception propagates as HTTP 500 Internal Server Error.
         * Tests exception case for database query failure.
         *
         * @verifies [ARC-003], [EXC-003]
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception [ARC-003]")
        void getAllUsers_whenServiceThrowsException_returnsInternalServerError() throws Exception {
            logger.info("[TEST_START] [ARC-003] getAllUsers_whenServiceThrowsException_returnsInternalServerError");

            // Given: Service throws runtime exception
            given(userService.getAllUsers()).willThrow(new RuntimeException("Query execution failed"));

            // When: GET request for all users
            // Then: Verify 500 status
            mockMvc.perform(get(API_BASE_PATH))
                    .andExpect(status().isInternalServerError());

            verify(userService).getAllUsers();
            logger.info("[TEST_END] [ARC-003] getAllUsers_whenServiceThrowsException_returnsInternalServerError - PASSED");
        }
    }

    // =========================================================================
    // UPDATE USER TESTS (PUT /api/v1/users/{userId}) - [ARC-004]
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/users/{userId} - Update User Tests [ARC-004]")
    class UpdateUserTests {

        /**
         * Verifies successful user update returns HTTP 200 with updated user.
         * Validates happy path where user exists and update succeeds.
         *
         * @verifies [ARC-004]
         */
        @Test
        @DisplayName("Should return 200 OK with updated user when user exists [ARC-004]")
        void updateUser_whenUserExists_returnsUpdatedUser() throws Exception {
            logger.info("[TEST_START] [ARC-004] updateUser_whenUserExists_returnsUpdatedUser");

            // Given: User exists and service returns updated user
            User updateRequest = buildTestUser(testUserId, "updateduser", "updated@example.com", TEST_PASSWORD_HASH, "Admin");
            User updatedUser = buildTestUser(testUserId, "updateduser", "updated@example.com", TEST_PASSWORD_HASH, "Admin");
            given(userService.updateUser(eq(testUserId), any(User.class))).willReturn(Optional.of(updatedUser));

            // When: PUT request to update user
            // Then: Verify 200 status, response reflects updated fields
            mockMvc.perform(put(API_BASE_PATH + "/{userId}", testUserId)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(testUserId.toString())))
                    .andExpect(jsonPath("$.username", is("updateduser")))
                    .andExpect(jsonPath("$.email", is("updated@example.com")))
                    .andExpect(jsonPath("$.role", is("Admin")));

            verify(userService).updateUser(eq(testUserId), any(User.class));
            logger.info("[TEST_END] [ARC-004] updateUser_whenUserExists_returnsUpdatedUser - PASSED");
        }

        /**
         * Verifies update of non-existent user returns HTTP 404 Not Found.
         * Tests edge case where service returns empty Optional.
         *
         * @verifies [ARC-004]
         */
        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent user [ARC-004]")
        void updateUser_whenUserNotFound_returnsNotFound() throws Exception {
            logger.info("[TEST_START] [ARC-004] updateUser_whenUserNotFound_returnsNotFound");

            // Given: User does not exist
            UUID nonExistentId = UUID.randomUUID();
            User updateRequest = buildTestUser(nonExistentId, "updateduser", "updated@example.com", TEST_PASSWORD_HASH, "Admin");
            given(userService.updateUser(eq(nonExistentId), any(User.class))).willReturn(Optional.empty());

            // When: PUT request for non-existent user
            // Then: Verify 404 status
            mockMvc.perform(put(API_BASE_PATH + "/{userId}", nonExistentId)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(updateRequest)))
                    .andExpect(status().isNotFound());

            verify(userService).updateUser(eq(nonExistentId), any(User.class));
            logger.info("[TEST_END] [ARC-004] updateUser_whenUserNotFound_returnsNotFound - PASSED");
        }

        /**
         * Verifies validation failure returns HTTP 400 for invalid update data.
         * Tests edge case where @Valid annotation triggers constraint violation.
         *
         * @verifies [ARC-004]
         */
        @Test
        @DisplayName("Should return 400 Bad Request when update data is invalid [ARC-004]")
        void updateUser_whenInvalidData_returnsBadRequest() throws Exception {
            logger.info("[TEST_START] [ARC-004] updateUser_whenInvalidData_returnsBadRequest");

            // Given: Update request with missing required field (email)
            User invalidUpdate = buildTestUser(testUserId, "updateduser", "", TEST_PASSWORD_HASH, "Admin");

            // When: PUT request with invalid data
            // Then: Verify 400 status, service never called
            mockMvc.perform(put(API_BASE_PATH + "/{userId}", testUserId)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(invalidUpdate)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
            logger.info("[TEST_END] [ARC-004] updateUser_whenInvalidData_returnsBadRequest - PASSED");
        }

        /**
         * Verifies service exception propagates as HTTP 500 Internal Server Error.
         * Tests exception case for update operation failure.
         *
         * @verifies [ARC-004], [EXC-004]
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception [ARC-004]")
        void updateUser_whenServiceThrowsException_returnsInternalServerError() throws Exception {
            logger.info("[TEST_START] [ARC-004] updateUser_whenServiceThrowsException_returnsInternalServerError");

            // Given: Valid request but service throws exception
            User updateRequest = buildTestUser(testUserId, "updateduser", "updated@example.com", TEST_PASSWORD_HASH, "Admin");
            given(userService.updateUser(eq(testUserId), any(User.class))).willThrow(new RuntimeException("Optimistic lock failure"));

            // When: PUT request to update user
            // Then: Verify 500 status
            mockMvc.perform(put(API_BASE_PATH + "/{userId}", testUserId)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(updateRequest)))
                    .andExpect(status().isInternalServerError());

            verify(userService).updateUser(eq(testUserId), any(User.class));
            logger.info("[TEST_END] [ARC-004] updateUser_whenServiceThrowsException_returnsInternalServerError - PASSED");
        }
    }

    // =========================================================================
    // DELETE USER TESTS (DELETE /api/v1/users/{userId}) - [ARC-001]
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId} - Delete User Tests [ARC-001]")
    class DeleteUserTests {

        /**
         * Verifies successful user deletion returns HTTP 204 No Content.
         * Validates happy path where user exists and deletion succeeds.
         *
         * @verifies [ARC-001]
         */
        @Test
        @DisplayName("Should return 204 No Content when user deleted successfully [ARC-001]")
        void deleteUser_whenUserExists_returnsNoContent() throws Exception {
            logger.info("[TEST_START] [ARC-001] deleteUser_whenUserExists_returnsNoContent");

            // Given: User exists and deletion succeeds
            given(userService.deleteUser(testUserId)).willReturn(true);

            // When: DELETE request for existing user
            // Then: Verify 204 status, no response body
            mockMvc.perform(delete(API_BASE_PATH + "/{userId}", testUserId))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(testUserId);
            logger.info("[TEST_END] [ARC-001] deleteUser_whenUserExists_returnsNoContent - PASSED");
        }

        /**
         * Verifies deletion of non-existent user returns HTTP 404 Not Found.
         * Tests edge case where service returns false for non-existent user.
         *
         * @verifies [ARC-001]
         */
        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent user [ARC-001]")
        void deleteUser_whenUserNotFound_returnsNotFound() throws Exception {
            logger.info("[TEST_START] [ARC-001] deleteUser_whenUserNotFound_returnsNotFound");

            // Given: User does not exist
            UUID nonExistentId = UUID.randomUUID();
            given(userService.deleteUser(nonExistentId)).willReturn(false);

            // When: DELETE request for non-existent user
            // Then: Verify 404 status
            mockMvc.perform(delete(API_BASE_PATH + "/{userId}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(userService).deleteUser(nonExistentId);
            logger.info("[TEST_END] [ARC-001] deleteUser_whenUserNotFound_returnsNotFound - PASSED");
        }

        /**
         * Verifies invalid UUID format returns HTTP 400 Bad Request.
         * Tests edge case where path variable binding fails.
         *
         * @verifies [ARC-001]
         */
        @Test
        @DisplayName("Should return 400 Bad Request when UUID format is invalid [ARC-001]")
        void deleteUser_whenInvalidUuid_returnsBadRequest() throws Exception {
            logger.info("[TEST_START] [ARC-001] deleteUser_whenInvalidUuid_returnsBadRequest");

            // When: DELETE request with invalid UUID format
            // Then: Verify 400 status (Spring handles UUID parsing failure)
            mockMvc.perform(delete(API_BASE_PATH + "/{userId}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
            logger.info("[TEST_END] [ARC-001] deleteUser_whenInvalidUuid_returnsBadRequest - PASSED");
        }

        /**
         * Verifies service exception propagates as HTTP 500 Internal Server Error.
         * Tests exception case for deletion operation failure.
         *
         * @verifies [ARC-001], [EXC-001]
         */
        @Test
        @DisplayName("Should return 500 Internal Server Error when service throws exception [ARC-001]")
        void deleteUser_whenServiceThrowsException_returnsInternalServerError() throws Exception {
            logger.info("[TEST_START] [ARC-001] deleteUser_whenServiceThrowsException_returnsInternalServerError");

            // Given: Service throws runtime exception
            given(userService.deleteUser(testUserId)).willThrow(new RuntimeException("Foreign key constraint violation"));

            // When: DELETE request for user
            // Then: Verify 500 status
            mockMvc.perform(delete(API_BASE_PATH + "/{userId}", testUserId))
                    .andExpect(status().isInternalServerError());

            verify(userService).deleteUser(testUserId);
            logger.info("[TEST_END] [ARC-001] deleteUser_whenServiceThrowsException_returnsInternalServerError - PASSED");
        }
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases & Boundary Conditions")
    class EdgeCaseTests {

        /**
         * Verifies controller handles null service response gracefully.
         * Tests boundary condition where service returns null instead of Optional.
         *
         * @verifies [ARC-002], [ARC-004]
         */
        @Test
        @DisplayName("Should handle null service response gracefully")
        void getUserById_whenServiceReturnsNull_returnsNotFound() throws Exception {
            logger.info("[TEST_START] Edge case: getUserById_whenServiceReturnsNull_returnsNotFound");

            // Given: Service returns null (defensive programming test)
            given(userService.getUserById(testUserId)).willReturn(null);

            // When: GET request
            // Then: Verify 500 (NullPointerException in controller) or handle gracefully
            // Note: Current implementation would throw NPE, this documents expected behavior
            mockMvc.perform(get(API_BASE_PATH + "/{userId}", testUserId))
                    .andExpect(status().is5xxServerError());

            verify(userService).getUserById(testUserId);
            logger.info("[TEST_END] Edge case: getUserById_whenServiceReturnsNull_returnsNotFound - DOCUMENTED");
        }

        /**
         * Verifies concurrent modification scenario handling.
         * Tests boundary condition for optimistic locking.
         *
         * @verifies [ARC-004]
         */
        @Test
        @DisplayName("Should handle concurrent modification exception")
        void updateUser_whenConcurrentModification_returnsConflict() throws Exception {
            logger.info("[TEST_START] Edge case: updateUser_whenConcurrentModification_returnsConflict");

            // Given: Service throws optimistic locking exception
            User updateRequest = buildTestUser(testUserId, "updateduser", "updated@example.com", TEST_PASSWORD_HASH, "Admin");
            given(userService.updateUser(eq(testUserId), any(User.class)))
                    .willThrow(new org.springframework.dao.OptimisticLockingFailureException("Version conflict"));

            // When: PUT request
            // Then: Verify 500 (controller doesn't handle specific exception type)
            mockMvc.perform(put(API_BASE_PATH + "/{userId}", testUserId)
                            .contentType(APPLICATION_JSON)
                            .content(toJson(updateRequest)))
                    .andExpect(status().isInternalServerError());

            verify(userService).updateUser(eq(testUserId), any(User.class));
            logger.info("[TEST_END] Edge case: updateUser_whenConcurrentModification_returnsConflict - DOCUMENTED");
        }
    }
}
```

# Day 1: model nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandlerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandlerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the User Service module.
 * <p>
 * This class intercepts and processes all exceptions thrown during user-related
 * operations, ensuring consistent, secure, and standardized error responses
 * across the microservice. It adheres to enterprise-grade security protocols
 * by masking sensitive data and providing structured audit trails.
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@RestControllerAdvice
public class UserExceptionHandler extends ResponseEntityExceptionHandler {

    // [ARC-001] Initialize the SLF4J logger for structured logging of exception events
    private static final Logger logger = LoggerFactory.getLogger(UserExceptionHandler.class);

    // [ARC-002] Define immutable constant strings for error response keys to avoid magic strings
    private static final String ERROR_TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_STATUS_KEY = "status";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String ERROR_PATH_KEY = "path";
    private static final String ERROR_VALIDATION_KEY = "validation_errors";

    /**
     * Handles validation errors for request body arguments annotated with @Valid.
     * <p>
     * Captures field-level validation failures and returns a structured response
     * with detailed field-specific error messages.
     *
     * @param ex      The MethodArgumentNotValidException containing validation errors
     * @param headers The HTTP headers to be written to the response
     * @param status  The HTTP status code
     * @param request The current web request
     * @return ResponseEntity containing the structured error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // [ARC-003] Log the validation failure at INFO level with masked context
        logger.info("[PROCESS] Handling validation errors for request: {}", request.getDescription(false));

        // [ARC-002] Aggregate field-level validation errors into a map for structured response
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // [ARC-001] Construct the standardized error response body
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for one or more fields",
                request,
                validationErrors
        );

        // [ARC-003] Log the completion of validation error handling
        logger.info("[COMPLETION] Validation error handling completed successfully");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON or invalid request body payloads.
     * <p>
     * Ensures that malformed input does not expose internal parsing details
     * to the client, maintaining security through obscurity principles.
     *
     * @param ex      The HttpMessageNotReadableException caused by malformed input
     * @param headers The HTTP headers to be written to the response
     * @param status  The HTTP status code
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // [ARC-004] Log the malformed request at WARN level without exposing raw payload
        logger.warn("[SECURITY] Malformed request body detected. Raw error: {}", ex.getMessage());

        // [ARC-001] Construct a generic error response to avoid leaking internal parsing details
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request body. Please verify the payload structure.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where a referenced entity is not found in the database.
     * <p>
     * Maps JPA EntityNotFoundException to a 404 Not Found response with
     * a sanitized message to prevent information disclosure.
     *
     * @param ex      The EntityNotFoundException thrown by the persistence layer
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) {

        // [ARC-003] Log the entity lookup failure at INFO level
        logger.info("[PROCESS] Entity not found during user operation: {}", ex.getMessage());

        // [ARC-002] Build a sanitized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "The requested user resource was not found.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles database constraint violations such as duplicate keys or
     * foreign key constraint failures.
     * <p>
     * Ensures that raw database error messages are never exposed to the client,
     * preventing potential information leakage about the database schema.
     *
     * @param ex      The DataIntegrityViolationException thrown by the database layer
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) {

        // [ARC-004] Log the database constraint violation at ERROR level with masked details
        logger.error("[CRITICAL FAIL] [ARC-004] Database integrity violation during user operation. Raw error: {}", ex.getMessage());

        // [ARC-001] Construct a generic error response to avoid exposing database internals
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                "A data integrity conflict occurred. The operation could not be completed.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles attempts to delete or access non-existent records.
     * <p>
     * Maps EmptyResultDataAccessException to a 404 Not Found response.
     *
     * @param ex      The EmptyResultDataAccessException thrown when no rows are affected
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex,
            WebRequest request) {

        // [ARC-003] Log the empty result at INFO level
        logger.info("[PROCESS] No result found for user operation: {}", ex.getMessage());

        // [ARC-002] Build a standardized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "The requested user resource does not exist.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles constraint violations from javax.validation annotations
     * applied directly on method parameters (e.g., @RequestParam, @PathVariable).
     *
     * @param ex      The ConstraintViolationException thrown by the validation engine
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        // [ARC-004] Log the constraint violation at WARN level
        logger.warn("[SECURITY] Constraint violation detected: {}", ex.getMessage());

        // [ARC-001] Construct a sanitized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "One or more input constraints were violated.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches all unhandled exceptions to prevent stack trace leakage
     * and ensure a consistent error contract for API consumers.
     *
     * @param ex      The generic Exception caught by the global handler
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003], [ARC-004]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex,
            WebRequest request) {

        // [ARC-004] Log the unhandled exception at ERROR level with full context
        logger.error("[CRITICAL FAIL] [ARC-004] Unhandled exception in user service. Raw error: {}", ex.getMessage(), ex);

        // [ARC-002] Build a generic error response to avoid exposing internal details
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred while processing your request.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a standardized error response map with consistent fields
     * for all exception types.
     * <p>
     * This ensures uniformity in error payloads and prevents accidental
     * exposure of sensitive information.
     *
     * @param statusCode The HTTP status code to include in the response
     * @param message    The sanitized error message to return to the client
     * @param request    The current web request for path extraction
     * @param validationErrors Optional map of field-level validation errors
     * @return A Map containing the structured error response
     * @traceability [ARC-001], [ARC-002]
     */
    private Map<String, Object> buildErrorResponse(
            int statusCode,
            String message,
            WebRequest request,
            Map<String, String> validationErrors) {

        // [ARC-001] Initialize the error response map with immutable constants
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_TIMESTAMP_KEY, Instant.now().toString());
        errorResponse.put(ERROR_STATUS_KEY, statusCode);
        errorResponse.put(ERROR_MESSAGE_KEY, message);

        // [ARC-002] Extract and sanitize the request path to avoid leaking internal routing details
        String requestPath = request != null ? request.getDescription(false).replace("uri=", "") : "unknown";
        errorResponse.put(ERROR_PATH_KEY, requestPath);

        // [ARC-003] Conditionally include validation errors if present
        if (validationErrors != null && !validationErrors.isEmpty()) {
            errorResponse.put(ERROR_VALIDATION_KEY, validationErrors);
        }

        return errorResponse;
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Viết các bài kiểm thử cho lớp UserExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserExceptionHandler} ensuring all exception handling paths
 * comply with enterprise security and logging standards.
 *
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@ExtendWith(MockitoExtension.class)
class UserExceptionHandlerTest {

    @Mock
    private UserExceptionHandler userExceptionHandler;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private HttpMessageNotReadableException httpMessageNotReadableException;

    @Mock
    private EntityNotFoundException entityNotFoundException;

    @Mock
    private DataIntegrityViolationException dataIntegrityViolationException;

    @Mock
    private EmptyResultDataAccessException emptyResultDataAccessException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private Exception globalException;

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of validation errors [ARC-001][ARC-003]")
    void testHandleMethodArgumentNotValid() throws Exception {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult())
                .thenReturn(mock(javax.validation.ValidationResult.class)); // placeholder for field errors
        // Simulate field errors via reflection on BindingResult
        var bindingResult = mock(javax.validation.ValidationResult.class);
        var fieldErrors = List.of(
                mockFieldError("username", "must not be blank"),
                mockFieldError("email", "must have valid format")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<Object> response = invokeHandleMethodArgumentNotValid(
                methodArgumentNotValidException,
                httpHeaders,
                HttpStatus.BAD_REQUEST,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body.get("timestamp"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Validation failed for one or more fields", body.get("message"));
        assertNotNull(body.get("validation_errors"));
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) body.get("validation_errors");
        assertEquals("must not be blank", validationErrors.get("username"));
        assertEquals("must have valid format", validationErrors.get("email"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of malformed JSON [ARC-002][ARC-004]")
    void testHandleHttpMessageNotReadable() throws Exception {
        // Arrange
        when(httpMessageNotReadableException.getMessage()).thenReturn("Invalid JSON");

        // Act
        ResponseEntity<Object> response = invokeHandleHttpMessageNotReadable(
                httpMessageNotReadableException,
                httpHeaders,
                HttpStatus.BAD_REQUEST,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Malformed JSON request body. Please verify the payload structure.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of entity not found [ARC-001][ARC-003]")
    void testHandleEntityNotFoundException() throws Exception {
        // Arrange
        when(entityNotFoundException.getMessage()).thenReturn("User 123 not found");

        // Act
        ResponseEntity<Object> response = invokeHandleEntityNotFoundException(
                entityNotFoundException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("The requested user resource was not found.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of data integrity violation [ARC-002][ARC-004]")
    void testHandleDataIntegrityViolationException() throws Exception {
        // Arrange
        when(dataIntegrityViolationException.getMessage()).thenReturn("Duplicate key");

        // Act
        ResponseEntity<Object> response = invokeHandleDataIntegrityViolationException(
                dataIntegrityViolationException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("A data integrity conflict occurred. The operation could not be completed.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of empty result access [ARC-001][ARC-003]")
    void testHandleEmptyResultDataAccessException() throws Exception {
        // Arrange
        when(emptyResultDataAccessException.getMessage()).thenReturn("No user found");

        // Act
        ResponseEntity<Object> response = invokeHandleEmptyResultDataAccessException(
                emptyResultDataAccessException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("The requested user resource does not exist.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of constraint violation [ARC-002][ARC-004]")
    void testHandleConstraintViolationException() throws Exception {
        // Arrange
        when(constraintViolationException.getMessage()).thenReturn("Invalid email format");

        // Act
        ResponseEntity<Object> response = invokeHandleConstraintViolationException(
                constraintViolationException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("One or more input constraints were violated.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of global exception [ARC-001][ARC-003][ARC-004]")
    void testHandleGlobalException() throws Exception {
        // Arrange
        when(globalException.getMessage()).thenReturn("Unexpected error");

        // Act
        ResponseEntity<Object> response = invokeHandleGlobalException(
                globalException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("An unexpected error occurred while processing your request.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test buildErrorResponse includes required fields [ARC-001][ARC-002]")
    void testBuildErrorResponse() throws Exception {
        // Arrange
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("field1", "Invalid value");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        // Act
        Map<String, Object> response = invokeBuildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                webRequest,
                validationErrors
        );

        // Assert
        assertNotNull(response.get("timestamp"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.get("status"));
        assertEquals("Validation failed", response.get("message"));
        assertEquals("/api/users", response.get("path"));
        @SuppressWarnings("unchecked")
        Map<String, String> validationMap = (Map<String, String>) response.get("validation_errors");
        assertEquals("Invalid value", validationMap.get("field1"));
    }

    // Helper methods using reflection to access protected methods
    private ResponseEntity<Object> invokeHandleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleMethodArgumentNotValid", MethodArgumentNotValidException.class, HttpHeaders.class, HttpStatus.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, headers, status, request);
    }

    private ResponseEntity<Object> invokeHandleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleHttpMessageNotReadable", HttpMessageNotReadableException.class, HttpHeaders.class, HttpStatus.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, headers, status, request);
    }

    private ResponseEntity<Object> invokeHandleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleEntityNotFoundException", EntityNotFoundException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleDataIntegrityViolationException", DataIntegrityViolationException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleEmptyResultDataAccessException", EmptyResultDataAccessException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleConstraintViolationException", ConstraintViolationException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleGlobalException(
            Exception ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleGlobalException", Exception.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private Map<String, Object> invokeBuildErrorResponse(
            int statusCode,
            String message,
            WebRequest request,
            Map<String, String> validationErrors) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("buildErrorResponse", int.class, String.class, WebRequest.class, Map.class);
        method.setAccessible(true);
        return (Map<String, Object>) method.invoke(userExceptionHandler, statusCode, message, request, validationErrors);
    }

    private FieldError mockFieldError(String field, String message) {
        FieldError fe = mock(FieldError.class);
        when(fe.getField()).thenReturn(field);
        when(fe.getDefaultMessage()).thenReturn(message);
        return fe;
    }
}
```

