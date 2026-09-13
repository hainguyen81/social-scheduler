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