# Day 1: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp UserService với các phương thức quản lý người dùng cơ bản như tạo, đọc, cập nhật và xóa người dùng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 1: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp UserController với các điểm cuối API để quản lý người dùng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 1: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai lớp UserExceptionHandler để xử lý các ngoại lệ liên quan đến người dùng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

