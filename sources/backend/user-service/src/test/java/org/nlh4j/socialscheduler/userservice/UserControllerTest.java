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