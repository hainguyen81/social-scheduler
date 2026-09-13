package org.nlh4j.socialscheduler.schedulingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScheduleControllerTest - Comprehensive unit test suite for ScheduleController REST endpoints.
 *
 * <p>This test class validates all API endpoints of the ScheduleController including
 * create, read, update, and delete operations for scheduled social media posts.
 * Tests cover happy paths, edge cases, boundary conditions, and exception scenarios
 * to ensure robust controller behavior and compliance with enterprise standards.</p>
 *
 * <p>Test Coverage Matrix:
 * - Happy Cases: Valid requests with correct payloads and headers
 * - Edge Cases: Boundary values, empty collections, null safety
 * - Exception Cases: Validation errors, not found, conflicts, processing failures</p>
 *
 * @verifies [REQ-001], [EXC-001], [EXC-002]
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleController Unit Tests")
class ScheduleControllerTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers Policy)
    // =========================================================================

    /** Base API path for schedule endpoints. */
    private static final String BASE_API_PATH = "/api/v1/schedules";

    /** Valid idempotency key for testing mutation endpoints. */
    private static final String VALID_IDEMPOTENCY_KEY = "idem-key-1234567890abcdef";

    /** Invalid idempotency key (too short) for testing validation. */
    private static final String INVALID_IDEMPOTENCY_KEY = "short";

    /** Test user ID constant. */
    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** Test schedule ID constant. */
    private static final UUID TEST_SCHEDULE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    /** Test schedule ID for not found scenarios. */
    private static final UUID NON_EXISTENT_SCHEDULE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    /** Valid platform constant. */
    private static final String VALID_PLATFORM = "Facebook";

    /** Valid content constant. */
    private static final String VALID_CONTENT = "Test post content for scheduling";

    /** Future scheduled time constant (1 hour from now). */
    private static final LocalDateTime FUTURE_SCHEDULED_TIME = LocalDateTime.now().plusHours(1);

    /** Past scheduled time constant (for edge case testing). */
    private static final LocalDateTime PAST_SCHEDULED_TIME = LocalDateTime.now().minusHours(1);

    /** Empty content constant for validation testing. */
    private static final String EMPTY_CONTENT = "";

    /** Long content exceeding max length for boundary testing. */
    private static final String LONG_CONTENT = "A".repeat(5001);

    /** Valid status constant. */
    private static final String VALID_STATUS = "pending";

    /** Invalid status constant for validation testing. */
    private static final String INVALID_STATUS = "invalid_status";

    /** Expected schedule count for list tests. */
    private static final int EXPECTED_SCHEDULE_COUNT = 2;

    /** HTTP header name for idempotency key. */
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    /** JSON content type constant. */
    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    /** Logger instance for test execution tracing. */
    private static final Logger logger = LoggerFactory.getLogger(ScheduleControllerTest.class);

    // =========================================================================
    // TEST FIXTURES & MOCKS
    // =========================================================================

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ScheduleController scheduleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ScheduleDto validScheduleDto;
    private Schedule validSchedule;

    // =========================================================================
    // SETUP & TEARDOWN
    // =========================================================================

    /**
     * Initializes test fixtures before each test method.
     * Sets up MockMvc, ObjectMapper, and valid test data objects.
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_SETUP] Initializing ScheduleControllerTest fixtures");
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
                .setControllerAdvice(new ScheduleExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register JavaTimeModule for LocalDateTime

        // Build valid ScheduleDto for reuse across tests
        validScheduleDto = ScheduleDto.builder()
                .userId(TEST_USER_ID)
                .platform(VALID_PLATFORM)
                .content(VALID_CONTENT)
                .scheduledTime(FUTURE_SCHEDULED_TIME)
                .status(VALID_STATUS)
                .build();

        // Build valid Schedule entity for response verification
        validSchedule = Schedule.builder()
                .scheduleId(TEST_SCHEDULE_ID)
                .userId(TEST_USER_ID)
                .platform(VALID_PLATFORM)
                .content(VALID_CONTENT)
                .scheduledTime(FUTURE_SCHEDULED_TIME)
                .status(VALID_STATUS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        logger.debug("[TEST_SETUP] Fixtures initialized successfully");
    }

    // =========================================================================
    // CREATE SCHEDULE TESTS (POST /api/v1/schedules)
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/schedules - Create Schedule Tests")
    class CreateScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule creation with valid input and idempotency key.
         */
        @Test
        @DisplayName("Should create schedule successfully with valid input [REQ-001]")
        void shouldCreateScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule creation");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(validSchedule);

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.userId", is(TEST_USER_ID.toString())))
                    .andExpect(jsonPath("$.platform", is(VALID_PLATFORM)))
                    .andExpect(jsonPath("$.content", is(VALID_CONTENT)))
                    .andExpect(jsonPath("$.status", is(VALID_STATUS)));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001] Successful schedule creation test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation failure when validation exception is thrown.
         */
        @Test
        @DisplayName("Should return 400 when validation fails [REQ-001][EXC-001]")
        void shouldReturn400WhenValidationFails() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing validation failure on create");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(EMPTY_CONTENT) // Empty content should trigger validation
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content cannot be empty"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")))
                    .andExpect(jsonPath("$.message", is("Content cannot be empty")));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-001] Validation failure test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests schedule creation failure when conflict exception is thrown.
         */
        @Test
        @DisplayName("Should return 409 when duplicate schedule detected [REQ-001][EXC-002]")
        void shouldReturn409WhenDuplicateScheduleDetected() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing conflict on create");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleConflictException("Duplicate schedule for same user and time"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")))
                    .andExpect(jsonPath("$.message", is("Duplicate schedule for same user and time")));

            verify(scheduleService).createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-002] Conflict detection test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with missing idempotency key header.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key is missing [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissing() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key");
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest()); // Missing required header

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with invalid platform value.
         */
        @Test
        @DisplayName("Should return 400 when platform is invalid [REQ-001][EXC-001]")
        void shouldReturn400WhenPlatformInvalid() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid platform");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform("InvalidPlatform")
                    .content(VALID_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Invalid platform: InvalidPlatform"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid platform test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with past scheduled time (boundary condition).
         */
        @Test
        @DisplayName("Should return 400 when scheduled time is in the past [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduledTimeInPast() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing past scheduled time");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(PAST_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Scheduled time must be in the future"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Past scheduled time test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with content exceeding maximum length.
         */
        @Test
        @DisplayName("Should return 400 when content exceeds max length [REQ-001][EXC-001]")
        void shouldReturn400WhenContentExceedsMaxLength() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing content length boundary");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(LONG_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content exceeds maximum length of 5000 characters"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Content length boundary test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with null user ID.
         */
        @Test
        @DisplayName("Should return 400 when user ID is null [REQ-001][EXC-001]")
        void shouldReturn400WhenUserIdIsNull() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing null user ID");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(null)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("User ID is required"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Null user ID test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with null scheduled time.
         */
        @Test
        @DisplayName("Should return 400 when scheduled time is null [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduledTimeIsNull() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing null scheduled time");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(VALID_CONTENT)
                    .scheduledTime(null)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Scheduled time is required"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Null scheduled time test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests schedule creation with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error occurs [REQ-001][EXC-001]")
        void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected processing error");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule creation")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error test passed");
        }
    }

    // =========================================================================
    // GET ALL SCHEDULES TESTS (GET /api/v1/schedules)
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/schedules - Get All Schedules Tests")
    class GetAllSchedulesTests {

        /**
         * @verifies [REQ-001]
         * Tests successful retrieval of all schedules.
         */
        @Test
        @DisplayName("Should return all schedules successfully [REQ-001]")
        void shouldReturnAllSchedulesSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing get all schedules");
            Schedule schedule2 = Schedule.builder()
                    .scheduleId(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                    .userId(TEST_USER_ID)
                    .platform("Instagram")
                    .content("Second post")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(1))
                    .status(VALID_STATUS)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<Schedule> schedules = Arrays.asList(validSchedule, schedule2);
            when(scheduleService.getAllSchedules()).thenReturn(schedules);

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(EXPECTED_SCHEDULE_COUNT)))
                    .andExpect(jsonPath("$[0].scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$[1].platform", is("Instagram")));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Get all schedules test passed");
        }

        /**
         * @verifies [REQ-001]
         * Tests retrieval of empty schedule list (edge case).
         */
        @Test
        @DisplayName("Should return empty list when no schedules exist [REQ-001]")
        void shouldReturnEmptyListWhenNoSchedulesExist() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing empty schedule list");
            when(scheduleService.getAllSchedules()).thenReturn(List.of());

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Empty schedule list test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests get all schedules when service throws unexpected exception.
         */
        @Test
        @DisplayName("Should return 500 when service throws unexpected error [REQ-001][EXC-001]")
        void shouldReturn500WhenServiceThrowsUnexpectedError() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected error on get all");
            when(scheduleService.getAllSchedules()).thenThrow(new RuntimeException("Database unavailable"));

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isInternalServerError());

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error on get all test passed");
        }
    }

    // =========================================================================
    // GET SCHEDULE BY ID TESTS (GET /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("GET /api/v1/schedules/{scheduleId} - Get Schedule By ID Tests")
    class GetScheduleByIdTests {

        /**
         * @verifies [REQ-001]
         * Tests successful retrieval of schedule by valid ID.
         */
        @Test
        @DisplayName("Should return schedule by ID successfully [REQ-001]")
        void shouldReturnScheduleByIdSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing get schedule by ID");
            when(scheduleService.getScheduleById(TEST_SCHEDULE_ID)).thenReturn(validSchedule);

            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.platform", is(VALID_PLATFORM)))
                    .andExpect(jsonPath("$.content", is(VALID_CONTENT)));

            verify(scheduleService).getScheduleById(TEST_SCHEDULE_ID);
            logger.info("[TEST_END] [REQ-001] Get schedule by ID test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests retrieval of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when schedule not found [REQ-001][EXC-001]")
        void shouldReturn404WhenScheduleNotFound() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing schedule not found");
            when(scheduleService.getScheduleById(NON_EXISTENT_SCHEDULE_ID))
                    .thenThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID));

            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")))
                    .andExpect(jsonPath("$.message", is("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID)));

            verify(scheduleService).getScheduleById(NON_EXISTENT_SCHEDULE_ID);
            logger.info("[TEST_END] [REQ-001][EXC-001] Schedule not found test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests retrieval with invalid UUID format (400 Bad Request).
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format is invalid [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdFormatInvalid() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID format");
            mockMvc.perform(get(BASE_API_PATH + "/{scheduleId}", "invalid-uuid"))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID format test passed");
        }
    }

    // =========================================================================
    // UPDATE SCHEDULE TESTS (PUT /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/v1/schedules/{scheduleId} - Update Schedule Tests")
    class UpdateScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule update with valid input.
         */
        @Test
        @DisplayName("Should update schedule successfully [REQ-001]")
        void shouldUpdateScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule update");
            ScheduleDto updateDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content("Updated content")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(2))
                    .status("sent")
                    .build();

            Schedule updatedSchedule = Schedule.builder()
                    .scheduleId(TEST_SCHEDULE_ID)
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content("Updated content")
                    .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(2))
                    .status("sent")
                    .createdAt(LocalDateTime.now().minusHours(1))
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(updatedSchedule);

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.scheduleId", is(TEST_SCHEDULE_ID.toString())))
                    .andExpect(jsonPath("$.content", is("Updated content")))
                    .andExpect(jsonPath("$.status", is("sent")));

            verify(scheduleService).updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001] Successful schedule update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when updating non-existent schedule [REQ-001][EXC-001]")
        void shouldReturn404WhenUpdatingNonExistentSchedule() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing update non-existent schedule");
            when(scheduleService.updateSchedule(eq(NON_EXISTENT_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")));

            verify(scheduleService).updateSchedule(eq(NON_EXISTENT_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY));
            logger.info("[TEST_END] [REQ-001][EXC-001] Update non-existent schedule test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with validation failure.
         */
        @Test
        @DisplayName("Should return 400 when update validation fails [REQ-001][EXC-001]")
        void shouldReturn400WhenUpdateValidationFails() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing update validation failure");
            ScheduleDto invalidDto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(EMPTY_CONTENT)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleValidationException("Content cannot be empty"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Update validation failure test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests update with conflict exception.
         */
        @Test
        @DisplayName("Should return 409 when update causes conflict [REQ-001][EXC-002]")
        void shouldReturn409WhenUpdateCausesConflict() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing update conflict");
            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new ScheduleConflictException("Schedule conflict with existing entry"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Update conflict test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with missing idempotency key.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key missing on update [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissingOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key on update");
            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key on update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with invalid schedule ID format.
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format invalid on update [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdInvalidOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID on update");
            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", "invalid-uuid")
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID on update test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests update with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error on update [REQ-001][EXC-001]")
        void shouldReturn500WhenUnexpectedErrorOnUpdate() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing unexpected error on update");
            when(scheduleService.updateSchedule(eq(TEST_SCHEDULE_ID), any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenThrow(new RuntimeException("Database deadlock"));

            mockMvc.perform(put(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule update")));

            logger.info("[TEST_END] [REQ-001][EXC-001] Unexpected error on update test passed");
        }
    }

    // =========================================================================
    // DELETE SCHEDULE TESTS (DELETE /api/v1/schedules/{scheduleId})
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/v1/schedules/{scheduleId} - Delete Schedule Tests")
    class DeleteScheduleTests {

        /**
         * @verifies [REQ-001]
         * Tests successful schedule deletion.
         */
        @Test
        @DisplayName("Should delete schedule successfully [REQ-001]")
        void shouldDeleteScheduleSuccessfully() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing successful schedule deletion");
            doNothing().when(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isNoContent());

            verify(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);
            logger.info("[TEST_END] [REQ-001] Successful schedule deletion test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion of non-existent schedule (404 Not Found).
         */
        @Test
        @DisplayName("Should return 404 when deleting non-existent schedule [REQ-001][EXC-001]")
        void shouldReturn404WhenDeletingNonExistentSchedule() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing delete non-existent schedule");
            doThrow(new ScheduleNotFoundException("Schedule not found with ID: " + NON_EXISTENT_SCHEDULE_ID))
                    .when(scheduleService).deleteSchedule(NON_EXISTENT_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", NON_EXISTENT_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode", is("NOT_FOUND_ERROR")));

            verify(scheduleService).deleteSchedule(NON_EXISTENT_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);
            logger.info("[TEST_END] [REQ-001][EXC-001] Delete non-existent schedule test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion with missing idempotency key.
         */
        @Test
        @DisplayName("Should return 400 when idempotency key missing on delete [REQ-001][EXC-001]")
        void shouldReturn400WhenIdempotencyKeyMissingOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing missing idempotency key on delete");
            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Missing idempotency key on delete test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests deletion with invalid schedule ID format.
         */
        @Test
        @DisplayName("Should return 400 when schedule ID format invalid on delete [REQ-001][EXC-001]")
        void shouldReturn400WhenScheduleIdInvalidOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing invalid UUID on delete");
            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", "invalid-uuid")
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isBadRequest());

            logger.info("[TEST_END] [REQ-001][EXC-001] Invalid UUID on delete test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests deletion with unexpected processing exception.
         */
        @Test
        @DisplayName("Should return 500 when unexpected error on delete [REQ-001][EXC-002]")
        void shouldReturn500WhenUnexpectedErrorOnDelete() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing unexpected error on delete");
            doThrow(new RuntimeException("Foreign key constraint violation"))
                    .when(scheduleService).deleteSchedule(TEST_SCHEDULE_ID, VALID_IDEMPOTENCY_KEY);

            mockMvc.perform(delete(BASE_API_PATH + "/{scheduleId}", TEST_SCHEDULE_ID)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode", is("PROCESSING_ERROR")))
                    .andExpect(jsonPath("$.message", is("Unexpected error during schedule deletion")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Unexpected error on delete test passed");
        }
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases & Boundary Conditions")
    class EdgeCaseTests {

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with all valid platforms (Facebook, Instagram, TikTok).
         */
        @Test
        @DisplayName("Should accept all valid platforms [REQ-001][EXC-001]")
        void shouldAcceptAllValidPlatforms() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing all valid platforms");
            String[] platforms = {"Facebook", "Instagram", "TikTok"};

            for (String platform : platforms) {
                ScheduleDto dto = ScheduleDto.builder()
                        .userId(TEST_USER_ID)
                        .platform(platform)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(VALID_STATUS)
                        .build();

                Schedule schedule = Schedule.builder()
                        .scheduleId(UUID.randomUUID())
                        .userId(TEST_USER_ID)
                        .platform(platform)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(VALID_STATUS)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                        .thenReturn(schedule);

                mockMvc.perform(post(BASE_API_PATH)
                                .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                                .contentType(JSON_CONTENT_TYPE)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.platform", is(platform)));
            }
            logger.info("[TEST_END] [REQ-001][EXC-001] All valid platforms test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with all valid statuses.
         */
        @Test
        @DisplayName("Should accept all valid statuses [REQ-001][EXC-001]")
        void shouldAcceptAllValidStatuses() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing all valid statuses");
            String[] statuses = {"pending", "sent", "failed", "cancelled"};

            for (String status : statuses) {
                ScheduleDto dto = ScheduleDto.builder()
                        .userId(TEST_USER_ID)
                        .platform(VALID_PLATFORM)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(status)
                        .build();

                Schedule schedule = Schedule.builder()
                        .scheduleId(UUID.randomUUID())
                        .userId(TEST_USER_ID)
                        .platform(VALID_PLATFORM)
                        .content(VALID_CONTENT)
                        .scheduledTime(FUTURE_SCHEDULED_TIME)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                        .thenReturn(schedule);

                mockMvc.perform(post(BASE_API_PATH)
                                .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                                .contentType(JSON_CONTENT_TYPE)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status", is(status)));
            }
            logger.info("[TEST_END] [REQ-001][EXC-001] All valid statuses test passed");
        }

        /**
         * @verifies [REQ-001], [EXC-001]
         * Tests create with maximum valid content length (5000 chars).
         */
        @Test
        @DisplayName("Should accept content at maximum length boundary [REQ-001][EXC-001]")
        void shouldAcceptContentAtMaxLengthBoundary() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-001] Testing max content length boundary");
            String maxContent = "A".repeat(5000);
            ScheduleDto dto = ScheduleDto.builder()
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(maxContent)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .build();

            Schedule schedule = Schedule.builder()
                    .scheduleId(UUID.randomUUID())
                    .userId(TEST_USER_ID)
                    .platform(VALID_PLATFORM)
                    .content(maxContent)
                    .scheduledTime(FUTURE_SCHEDULED_TIME)
                    .status(VALID_STATUS)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(schedule);

            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content", is(maxContent)));

            logger.info("[TEST_END] [REQ-001][EXC-001] Max content length boundary test passed");
        }

        /**
         * @verifies [REQ-001]
         * Tests get all schedules with large dataset (performance boundary).
         */
        @Test
        @DisplayName("Should handle large schedule list [REQ-001]")
        void shouldHandleLargeScheduleList() throws Exception {
            logger.info("[TEST_START] [REQ-001] Testing large schedule list");
            List<Schedule> largeList = java.util.stream.IntStream.range(0, 1000)
                    .mapToObj(i -> Schedule.builder()
                            .scheduleId(UUID.randomUUID())
                            .userId(TEST_USER_ID)
                            .platform(VALID_PLATFORM)
                            .content("Post " + i)
                            .scheduledTime(FUTURE_SCHEDULED_TIME.plusHours(i))
                            .status(VALID_STATUS)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .toList();

            when(scheduleService.getAllSchedules()).thenReturn(largeList);

            mockMvc.perform(get(BASE_API_PATH))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1000)));

            verify(scheduleService).getAllSchedules();
            logger.info("[TEST_END] [REQ-001] Large schedule list test passed");
        }
    }

    // =========================================================================
    // CONCURRENCY & IDEMPOTENCY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Concurrency & Idempotency Tests")
    class ConcurrencyTests {

        /**
         * @verifies [REQ-001], [EXC-002]
         * Tests that duplicate requests with same idempotency key are handled.
         */
        @Test
        @DisplayName("Should handle duplicate idempotency key gracefully [REQ-001][EXC-002]")
        void shouldHandleDuplicateIdempotencyKey() throws Exception {
            logger.info("[TEST_START] [REQ-001][EXC-002] Testing duplicate idempotency key");
            when(scheduleService.createSchedule(any(ScheduleDto.class), eq(VALID_IDEMPOTENCY_KEY)))
                    .thenReturn(validSchedule)
                    .thenThrow(new ScheduleConflictException("Duplicate request detected"));

            // First request succeeds
            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isCreated());

            // Second request with same key fails
            mockMvc.perform(post(BASE_API_PATH)
                            .header(IDEMPOTENCY_HEADER, VALID_IDEMPOTENCY_KEY)
                            .contentType(JSON_CONTENT_TYPE)
                            .content(objectMapper.writeValueAsString(validScheduleDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode", is("CONFLICT_ERROR")));

            logger.info("[TEST_END] [REQ-001][EXC-002] Duplicate idempotency key test passed");
        }
    }
}