package org.nlh4j.socialscheduler.integration;

// [REQ-001] [EXC-001] Traceability tags injected for enterprise auditing compliance.

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Enterprise production-ready unit and integration test suite for FacebookClient.
 * Utilizes OkHttp MockWebServer to simulate Facebook Graph API HTTP responses, network timeouts,
 * and error conditions. Validates resilience, retry policies, and exception handling compliance.
 * 
 * @verifies [REQ-001], [EXC-001]
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FacebookClientTest {

    private static final Logger logger = LoggerFactory.getLogger(FacebookClientTest.class);

    // [REQ-001] Top-of-class immutable constant parameters for test configurations
    private static final String TEST_FACEBOOK_PAGE_ID = "test_page_123";
    private static final String TEST_FACEBOOK_ACCESS_TOKEN = "EAABwzL_Test_Token_1234567890abcdef";
    private static final String MOCK_SUCCESS_RESPONSE_BODY = "{\"id\":\"123_456\",\"status\":\"success\"}";
    private static final String MOCK_SERVER_ERROR_BODY = "{\"error\":{\"code\":2,\"message\":\"Internal error\"}}";
    private static final String MOCK_UNAUTHORIZED_BODY = "{\"error\":{\"code\":190,\"message\":\"Invalid OAuth access token.\"}}";

    private MockWebServer mockWebServer;
    private FacebookClient facebookClient;

    /**
     * Sets up the MockWebServer instance and instantiates the FacebookClient bean pointing to the mock server URL.
     * Runs before each test case execution.
     * 
     * @throws IOException if MockWebServer fails to bind to a local ephemeral port
     */
    @BeforeEach
    void setUp() throws IOException {
        logger.info("[TEST_START] [REQ-001] Initializing MockWebServer and FacebookClient for test execution.");
        
        // Initialize OkHttp MockWebServer for isolated integration testing
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        // Dynamically resolve mock server base URL
        String mockBaseUrl = this.mockWebServer.url("/").toString();
        // Remove trailing slash to match RestClient baseUrl configuration style
        if (mockBaseUrl.endsWith("/")) {
            mockBaseUrl = mockBaseUrl.substring(0, mockBaseUrl.length() - 1);
        }

        // Initialize RestClient.Builder instance for client injection
        RestClient.Builder restClientBuilder = RestClient.builder();

        // Instantiate FacebookClient with mock parameters
        this.facebookClient = new FacebookClient(
                restClientBuilder,
                mockBaseUrl,
                TEST_FACEBOOK_ACCESS_TOKEN,
                TEST_FACEBOOK_PAGE_ID
        );
    }

    /**
     * Shuts down the MockWebServer instance after each test case execution to release socket resources.
     * 
     * @throws IOException if MockWebServer shutdown fails
     */
    @AfterEach
    void tearDown() throws IOException {
        logger.info("[TEST_END] [REQ-001] Shutting down MockWebServer.");
        if (this.mockWebServer != null) {
            this.mockWebServer.shutdown();
        }
    }

    /**
     * Test case 1: Verifies that when Facebook Graph API returns HTTP 200, 
     * publishPost successfully parses the response and returns a valid PublishResult with correct upstream post ID.
     * 
     * @verifies [REQ-001]
     */
    @Test
    @Order(1)
    @DisplayName("publishPost_whenFacebookReturns200_thenReturnSuccessResponse [REQ-001]")
    void publishPost_whenFacebookReturns200_thenReturnSuccessResponse() {
        logger.info("[TEST_EXEC] [REQ-001] Executing publishPost_whenFacebookReturns200_thenReturnSuccessResponse");

        // Enqueue Mock HTTP 200 Success response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(MOCK_SUCCESS_RESPONSE_BODY));

        // Construct sample schedule entity
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setScheduleId(UUID.randomUUID());
        schedule.setTenantId("tenant_alpha");
        schedule.setContent("Hello Facebook from automated test!");
        schedule.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Execute publishing call
        FacebookClient.PublishResult result = facebookClient.publishPost(schedule);

        // Assertions verifying successful payload parsing and return mapping
        assertThat(result).isNotNull();
        assertThat(result.success()).isTrue();
        assertThat(result.externalPostId()).isEqualTo("123_456");
        assertThat(result.message()).contains("Published successfully");
        assertThat(result.publishedAt()).isNotNull();

        logger.info("[TEST_PASS] [REQ-001] publishPost_whenFacebookReturns200_thenReturnSuccessResponse completed successfully.");
    }

    /**
     * Test case 2: Verifies that when Facebook Graph API returns HTTP 500, 
     * publishPost intercepts the server error and wraps it in a retryable SocialPlatformException 
     * with HTTP status 500 and correct error code.
     * 
     * @verifies [REQ-001], [EXC-001]
     */
    @Test
    @Order(2)
    @DisplayName("publishPost_whenFacebookReturns500_thenThrowSocialPlatformException [REQ-001] [EXC-001]")
    void publishPost_whenFacebookReturns500_thenThrowSocialPlatformException() {
        logger.info("[TEST_EXEC] [REQ-001] [EXC-001] Executing publishPost_whenFacebookReturns500_thenThrowSocialPlatformException");

        // Enqueue Mock HTTP 500 Server Error response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody(MOCK_SERVER_ERROR_BODY));

        // Construct sample schedule entity
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setScheduleId(UUID.randomUUID());
        schedule.setTenantId("tenant_beta");
        schedule.setContent("Failing post test content");
        schedule.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Assertions verifying exception wrapping and retryable flag propagation
        assertThatThrownBy(() -> facebookClient.publishPost(schedule))
                .isInstanceOf(SocialPlatformException.class)
                .satisfies(ex -> {
                    SocialPlatformException spe = (SocialPlatformException) ex;
                    assertThat(spe.getHttpStatus()).isEqualTo(500);
                    assertThat(spe.getPlatform()).isEqualTo("FACEBOOK");
                    assertThat(spe.isRetryable()).isTrue();
                    assertThat(spe.getMessage()).contains("Facebook API client error")
                            .contains(MOCK_SERVER_ERROR_BODY);
                });

        logger.info("[TEST_PASS] [REQ-001] [EXC-001] publishPost_whenFacebookReturns500_thenThrowSocialPlatformException passed.");
    }

    /**
     * Test case 3: Verifies that when network connectivity drops or timeouts occur (ResourceAccessException), 
     * the method throws a retryable SocialPlatformException indicating a network timeout.
     * 
     * @verifies [REQ-001], [EXC-001]
     */
    @Test
    @Order(3)
    @DisplayName("publishPost_whenNetworkTimeout_thenThrowRetryableTimeoutException [REQ-001] [EXC-001]")
    void publishPost_whenNetworkTimeout_thenThrowRetryableTimeoutException() {
        logger.info("[TEST_EXEC] [REQ-001] [EXC-001] Executing publishPost_whenNetworkTimeout_thenThrowRetryableTimeoutException");

        // Shut down mock web server prematurely to force a ResourceAccessException (Connection refused / timeout)
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            logger.error("[TEST_ERROR] Failed to shutdown mock server for timeout test", e);
        }

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setScheduleId(UUID.randomUUID());
        schedule.setTenantId("tenant_gamma");
        schedule.setContent("Timeout test post");
        schedule.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Assertions verifying network exception handling and retryable flag
        assertThatThrownBy(() -> facebookClient.publishPost(schedule))
                .isInstanceOf(SocialPlatformException.class)
                .satisfies(ex -> {
                    SocialPlatformException spe = (SocialPlatformException) ex;
                    assertThat(spe.getHttpStatus()).isEqualTo(504);
                    assertThat(spe.getErrorCode()).isEqualTo("FB_NETWORK_TIMEOUT");
                    assertThat(spe.isRetryable()).isTrue();
                    assertThat(spe.getMessage()).contains("Network timeout while communicating with Facebook API");
                });

        logger.info("[TEST_PASS] [REQ-001] [EXC-001] publishPost_whenNetworkTimeout_thenThrowRetryableTimeoutException passed.");
    }

    /**
     * Test case 4: Verifies that when Facebook API returns HTTP 401 Unauthorized due to an invalid access token, 
     * publishPost throws a non-retryable SocialPlatformException with HTTP status 401.
     * 
     * @verifies [REQ-001], [EXC-001]
     */
    @Test
    @Order(4)
    @DisplayName("publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException [REQ-001] [EXC-001]")
    void publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException() {
        logger.info("[TEST_EXEC] [REQ-001] [EXC-001] Executing publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException");

        // Enqueue Mock HTTP 401 Unauthorized response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody(MOCK_UNAUTHORIZED_BODY));

        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setScheduleId(UUID.randomUUID());
        schedule.setTenantId("tenant_delta");
        schedule.setContent("Unauthorized post test");
        schedule.setScheduledTime(LocalDateTime.now().plusHours(1));

        // Assertions verifying non-retryable client error behavior on HTTP 401
        assertThatThrownBy(() -> facebookClient.publishPost(schedule))
                .isInstanceOf(SocialPlatformException.class)
                .satisfies(ex -> {
                    SocialPlatformException spe = (SocialPlatformException) ex;
                    assertThat(spe.getHttpStatus()).isEqualTo(401);
                    assertThat(spe.getErrorCode()).isEqualTo("FB_CLIENT_ERROR_401");
                    // HTTP 401 is client error (not 429 rate limit or 5xx server error), so isRetryable should be false
                    assertThat(spe.isRetryable()).isFalse();
                    assertThat(spe.getMessage()).contains("Facebook API client error")
                            .contains(MOCK_UNAUTHORIZED_BODY);
                });

        logger.info("[TEST_PASS] [REQ-001] [EXC-001] publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException passed.");
    }
}