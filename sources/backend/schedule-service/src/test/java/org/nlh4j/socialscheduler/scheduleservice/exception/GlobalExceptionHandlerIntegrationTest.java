package org.nlh4j.socialscheduler.scheduleservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.nlh4j.socialscheduler.ratelimitservice.service.RateLimiterService;
import org.nlh4j.socialscheduler.scheduleservice.controller.ScheduleController;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleResponseDto;
import org.nlh4j.socialscheduler.scheduleservice.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test suite for {@link GlobalExceptionHandler} mapping REST responses to centralized error contracts.
 * Verifies compliance with OWASP Top 10 standards (A04 Insecure Design, A05 Security Misconfiguration, A09 Logging Failures).
 *
 * @verifies [REQ-003] Input validation constraints and Rate Limiter integration
 * @verifies [EXC-002] Handling expired or invalid JWT security tokens
 * @verifies [EXC-003] Handling downstream and third-party social provider communication failures
 * @verifies [EXC-005] Handling token-bucket rate limit violations with HTTP 429 and Retry-After headers
 */
@WebMvcTest(controllers = ScheduleController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
public class GlobalExceptionHandlerIntegrationTest {

    // [REQ-003] Logger initialization for audit tracing and process verification
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandlerIntegrationTest.class);

    // [REQ-003] Top-of-Class Immutable API and Header Constants
    public static final String SCHEDULE_API_ENDPOINT = "/api/v1/schedules";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String RETRY_AFTER_HEADER = "Retry-After";

    // [REQ-003] Standard Error Code Constants
    public static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERROR_CODE_TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_UPSTREAM_SERVICE_ERROR = "UPSTREAM_SERVICE_ERROR";
    public static final String ERROR_CODE_RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String ERROR_CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // [REQ-003] Test Fixture Domain Values
    public static final String TEST_USER_ID_STRING = "11111111-2222-3333-4444-555555555555";
    public static final String VALID_PLATFORM_FACEBOOK = "Facebook";
    public static final String VALID_CONTENT_PAYLOAD = "Enterprise Grade Social Scheduling Post Content #automated";
    public static final long DEFAULT_RETRY_AFTER_SECONDS = 60L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private ScheduleRequestDto validScheduleRequestDto;

    /**
     * Initializes test fixtures before each test execution run.
     */
    @BeforeEach
    void setUp() {
        // [REQ-003] Setup baseline valid scheduling request DTO with future timestamp
        validScheduleRequestDto = new ScheduleRequestDto();
        validScheduleRequestDto.setPlatform(VALID_PLATFORM_FACEBOOK);
        validScheduleRequestDto.setContent(VALID_CONTENT_PAYLOAD);
        validScheduleRequestDto.setScheduledTime(OffsetDateTime.now().plusDays(2));
    }

    /**
     * Verifies that submitting a malformed payload missing required fields triggers Jakarta Validation,
     * returning HTTP 400 Bad Request with granular field error mappings and correlation ID.
     *
     * @verifies [REQ-003] Input validation enforcement via Jakarta Validation
     * @verifies [EXC-002] Security and payload exception handling
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Missing Platform Field Should Return HTTP 400 and VALIDATION_FAILED")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleValidationException_whenPayloadInvalid_thenReturn400() throws Exception {
        // [REQ-003] Entry logging
        LOGGER.info("[TEST_START] [REQ-003] [EXC-002] Executing input validation failure test case");

        // Construct invalid payload missing the mandatory platform field
        ScheduleRequestDto invalidDto = new ScheduleRequestDto();
        invalidDto.setPlatform(null);
        invalidDto.setContent(VALID_CONTENT_PAYLOAD);
        invalidDto.setScheduledTime(OffsetDateTime.now().plusDays(1));

        String requestBody = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 400 Bad Request
                .andExpect(status().isBadRequest())
                // Verify standard enterprise error envelope
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_VALIDATION_FAILED)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItem("platform")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                // Verify tracing header contract
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] [EXC-002] Validation failure scenario successfully verified");
    }

    /**
     * Verifies that submitting an expired or corrupt JWT token throws a JwtException,
     * which is intercepted by GlobalExceptionHandler to return HTTP 401 Unauthorized.
     *
     * @verifies [EXC-002] Token expiration lifecycle handling
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Expired JWT Token Should Return HTTP 401 and TOKEN_EXPIRED")
    void handleTokenExpired_whenJwtInvalid_thenReturn401() throws Exception {
        LOGGER.info("[TEST_START] [EXC-002] Executing invalid/expired JWT token verification");

        // Mock JwtDecoder to simulate token expiration failure
        given(jwtDecoder.decode(any())).willThrow(new JwtException("Token has expired or signature is invalid"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expiredTokenPayload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_TOKEN_EXPIRED)))
                .andExpect(jsonPath("$.message", containsString("Token has expired")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-002] Token expiration handling verified with HTTP 401");
    }

    /**
     * Verifies that third-party downstream platform network timeouts or communication errors
     * return HTTP 502 Bad Gateway with UPSTREAM_SERVICE_ERROR and correlation ID.
     *
     * @verifies [EXC-003] Third-party social platform upstream error boundary isolation
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Downstream Social API Failure Should Return HTTP 502 and UPSTREAM_SERVICE_ERROR")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleUpstreamError_whenSocialPlatformThrows_thenReturn502() throws Exception {
        LOGGER.info("[TEST_START] [EXC-003] Executing downstream social provider exception mapping test");

        // Mock downstream integration failure inside ScheduleService
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new SocialPlatformException("Facebook Graph API connection timed out: 504 Gateway Timeout"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 502 Bad Gateway
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_UPSTREAM_SERVICE_ERROR)))
                .andExpect(jsonPath("$.message", containsString("Facebook Graph API")))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-003] Upstream service failure successfully mapped to HTTP 502");
    }

    /**
     * Verifies that exceeding rate limits returns HTTP 429 Too Many Requests,
     * accompanied by the explicit Retry-After header and localized error payload.
     *
     * @verifies [REQ-003] Redis Token Bucket Rate Limiting integration
     * @verifies [EXC-005] Rate Limit Exceeded threshold enforcement with HTTP 429
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Rate Limit Exceeded Should Return HTTP 429 and Retry-After Header")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleRateLimitExceeded_thenReturn429WithRetryAfter() throws Exception {
        LOGGER.info("[TEST_START] [REQ-003] [EXC-005] Executing Redis token-bucket rate limiter boundary test");

        UUID userId = UUID.fromString(TEST_USER_ID_STRING);

        // Mock RateLimiterService or ScheduleService interceptor throwing RateLimitExceededException
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new RateLimitExceededException(
                        userId,
                        SCHEDULE_API_ENDPOINT,
                        DEFAULT_RETRY_AFTER_SECONDS
                ));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 429 Too Many Requests
                .andExpect(status().isTooManyRequests())
                // Validate required RFC HTTP Retry-After header
                .andExpect(header().string(RETRY_AFTER_HEADER, String.valueOf(DEFAULT_RETRY_AFTER_SECONDS)))
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_RATE_LIMIT_EXCEEDED)))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.retryAfterSeconds", is((int) DEFAULT_RETRY_AFTER_SECONDS)))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] [EXC-005] HTTP 429 and Retry-After header successfully verified");
    }

    /**
     * Verifies unhandled generic exceptions return HTTP 500 without leaking stack traces or SQL internals (OWASP A05 & A09).
     *
     * @verifies [EXC-002] Defense-in-depth secure exception abstraction
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Unexpected Runtime Exception Should Return HTTP 500 Without Stack Trace")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void handleGenericException_whenUnexpectedError_thenReturn500WithoutStackTrace() throws Exception {
        LOGGER.info("[TEST_START] [EXC-002] Executing generic unexpected exception shielding test");

        // Mock sudden low-level runtime or DB crash
        given(scheduleService.createSchedule(any(ScheduleRequestDto.class)))
                .willThrow(new RuntimeException("Database connection failed: FATAL: password authentication failed for user postgres"));

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 500 Internal Server Error
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode", is(ERROR_CODE_INTERNAL_SERVER_ERROR)))
                .andExpect(jsonPath("$.correlationId", notNullValue()))
                // Guarantee OWASP compliance: NO stack traces or internal DB drivers revealed to caller
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("FATAL"))))
                .andExpect(jsonPath("$.message", not(containsString("password"))))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [EXC-002] Unhandled exception shielded securely without stack trace leakage");
    }

    /**
     * Verifies the happy path execution returning HTTP 201 Created when the request payload is compliant.
     *
     * @verifies [REQ-003] Standard happy path schedule creation contract
     */
    @Test
    @DisplayName("POST /api/v1/schedules - Compliant Payload Should Return HTTP 201 Created")
    @WithMockUser(username = "scheduler_user", roles = {"SCHEDULER"})
    void createSchedule_whenPayloadValid_thenReturn201Created() throws Exception {
        LOGGER.info("[TEST_START] [REQ-003] Executing happy path schedule creation test case");

        ScheduleResponseDto mockResponse = new ScheduleResponseDto();
        mockResponse.setScheduleId(UUID.randomUUID());
        mockResponse.setPlatform(VALID_PLATFORM_FACEBOOK);
        mockResponse.setContent(VALID_CONTENT_PAYLOAD);
        mockResponse.setStatus("PENDING");
        mockResponse.setScheduledTime(validScheduleRequestDto.getScheduledTime());

        given(scheduleService.createSchedule(any(ScheduleRequestDto.class))).willReturn(mockResponse);

        String requestBody = objectMapper.writeValueAsString(validScheduleRequestDto);

        mockMvc.perform(post(SCHEDULE_API_ENDPOINT)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                // Assert HTTP 201 Created
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId", notNullValue()))
                .andExpect(jsonPath("$.platform", is(VALID_PLATFORM_FACEBOOK)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(header().string(CORRELATION_ID_HEADER, notNullValue()));

        LOGGER.info("[TEST_COMPLETE] [REQ-003] Schedule successfully created with status PENDING");
    }
}