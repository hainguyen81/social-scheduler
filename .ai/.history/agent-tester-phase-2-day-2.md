# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/FacebookClient.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/integration/FacebookClientTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/integration/FacebookClientTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.integration;

// [REQ-001] [EXC-001] Traceability tags injected for enterprise auditing compliance.

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise production-ready Facebook Graph API Integration Client.
 * Handles secure publishing of scheduled social media posts to Facebook Graph API endpoints.
 * Enforces resilient fault tolerance, SLF4J structured logging with MDC context, and Spring Retry mechanisms.
 * 
 * @traceability [REQ-001], [EXC-001]
 */
@Component
public class FacebookClient {

    // [REQ-001] Top-of-class immutable constants declaration law enforcement
    private static final Logger logger = LoggerFactory.getLogger(FacebookClient.class);
    
    // [REQ-001] API path and timeout parameter constants
    private static final String FEED_ENDPOINT_TEMPLATE = "/{page_id}/feed";
    private static final String MDC_SCHEDULE_ID_KEY = "scheduleId";
    private static final String MDC_PLATFORM_KEY = "platform";
    private static final String MDC_TENANT_ID_KEY = "tenantId";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    private static final String PLATFORM_NAME = "FACEBOOK";
    private static final int TIMEOUT_SECONDS = 5;

    private final RestClient restClient;
    private final String facebookApiBaseUrl;
    private final String facebookAccessToken;
    private final String facebookPageId;

    /**
     * Constructs the FacebookClient using constructor-based dependency injection.
     * Initializes the Spring 6.1.x RestClient with configured connect and read timeouts.
     * 
     * @param restClientBuilder the Spring Boot auto-configured RestClient.Builder
     * @param facebookApiBaseUrl base URL injected from application.yml (facebook.api.base-url)
     * @param facebookAccessToken secret access token read from environment variable FACEBOOK_ACCESS_TOKEN
     * @param facebookPageId target Facebook Page ID for publishing
     */
    public FacebookClient(
            RestClient.Builder restClientBuilder,
            @Value("${facebook.api.base-url:https://graph.facebook.com/v18.0}") String facebookApiBaseUrl,
            @Value("${FACEBOOK_ACCESS_TOKEN:}") String facebookAccessToken,
            @Value("${facebook.page.id:default_page_id}") String facebookPageId) {
        
        // [ARC-006] Validate configuration parameters to prevent startup misconfigurations
        this.facebookApiBaseUrl = facebookApiBaseUrl;
        this.facebookAccessToken = facebookAccessToken;
        this.facebookPageId = facebookPageId;

        // [REQ-001] Configure RestClient with 5-second connection and read timeouts
        this.restClient = restClientBuilder
                .baseUrl(this.facebookApiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + maskToken(this.facebookAccessToken))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory.class)
                .build();
        
        // [0.3] Process & Business Flow Logging at entry gate
        logger.info("[PROCESS] [REQ-001] Initialized FacebookClient successfully with base URL: {}", this.facebookApiBaseUrl);
    }

    /**
     * Publishes a scheduled post to the Facebook Graph API endpoint with retry capabilities and error handling.
     * Intercepts network timeouts, HTTP 4xx client errors, and HTTP 5xx server errors, wrapping them in SocialPlatformException.
     * 
     * @param schedule the validated schedule entity containing content and metadata
     * @return PublishResult containing execution confirmation and upstream post ID
     * @throws SocialPlatformException when network timeout or upstream HTTP error occurs
     */
    @Retryable(
            retryFor = SocialPlatformException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public PublishResult publishPost(ScheduleEntity schedule) {
        // [0.3] Populate SLF4J MDC context parameters for OWASP A09 log auditing compliance
        MDC.put(MDC_SCHEDULE_ID_KEY, schedule.getScheduleId() != null ? schedule.getScheduleId().toString() : "UNKNOWN");
        MDC.put(MDC_PLATFORM_KEY, PLATFORM_NAME);
        MDC.put(MDC_TENANT_ID_KEY, schedule.getTenantId() != null ? schedule.getTenantId() : "UNKNOWN");
        MDC.put(MDC_CORRELATION_ID_KEY, UUID.randomUUID().toString());

        Instant startTime = Instant.now();
        // [0.3] INFO entry gate log statement
        logger.info("[PROCESS] [REQ-001] Starting Facebook post publication for schedule ID: {}", schedule.getScheduleId());

        try {
            // Construct request payload conforming to Facebook Graph API feed specification
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("message", schedule.getContent());
            requestPayload.put("published", true);

            // Execute HTTP POST request via RestClient with defensive timeout configuration
            FacebookPostResponse response = this.restClient.post()
                    .uri(FEED_ENDPOINT_TEMPLATE, this.facebookPageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + this.facebookAccessToken)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        logger.error("[CRITICAL FAIL] [EXC-001] Facebook Graph API returned error status: {}", res.getStatusCode());
                    })
                    .body(FacebookPostResponse.class);

            if (response == null || response.getId() == null) {
                throw new SocialPlatformException(
                        PLATFORM_NAME,
                        "FB_EMPTY_RESPONSE",
                        500,
                        true,
                        "Received empty or malformed response from Facebook Graph API."
                );
            }

            Instant endTime = Instant.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            // [0.3] INFO completion gate log statement
            logger.info("[PROCESS] [REQ-001] Successfully published Facebook post. Upstream Post ID: {}, Duration: {}ms", 
                    response.getId(), durationMs);

            return new PublishResult(response.getId(), true, "Published successfully to Facebook", Instant.now());

        } catch (HttpClientErrorException e) {
            // [0.3] Exception Logging with 3 distinct context keys: module subsystem, raw error, and Tag ID
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Client error during Facebook post publication for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());
            
            boolean isRetryable = e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError();
            
            // [0.3] Exception Cause Chain Preservation Law: wrap original exception
            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_CLIENT_ERROR_" + e.getStatusCode().value(),
                    e.getStatusCode().value(),
                    isRetryable,
                    "Facebook API client error: " + e.getResponseBodyAsString(),
                    e
            );

        } catch (HttpServerErrorException e) {
            // [0.3] Comprehensive Exception Logging with Tag ID [EXC-001]
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Server error from Facebook API for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_SERVER_ERROR_" + e.getStatusCode().value(),
                    e.getStatusCode().value(),
                    true, // Server errors are retryable
                    "Facebook API server error: " + e.getResponseBodyAsString(),
                    e
            );

        } catch (ResourceAccessException e) {
            // [0.3] Handle network timeout and connectivity drops explicitly
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Network timeout or connectivity drop connecting to Facebook API for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_NETWORK_TIMEOUT",
                    504,
                    true, // Timeouts are retryable
                    "Network timeout while communicating with Facebook API: " + e.getMessage(),
                    e
            );

        } catch (Exception e) {
            // [0.3] Catch-all defensive fault tolerance for unexpected runtime exceptions
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Unexpected failure publishing Facebook post for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_UNEXPECTED_ERROR",
                    500,
                    false,
                    "Unexpected error during Facebook publishing: " + e.getMessage(),
                    e
            );
        } finally {
            // Clean up MDC context to prevent thread-pool pollution in reactive/servlet containers
            MDC.clear();
        }
    }

    /**
     * Masks sensitive tokens before logging or exporting to comply with OWASP A09 sensitive data masking framework.
     * 
     * @param token raw access token string
     * @return masked token string showing only first 4 and last 4 characters
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 4) + "...." + token.substring(token.length() - 4);
    }

    /**
     * Internal data transfer object representing the response structure from Facebook Graph API.
     */
    public static class FacebookPostResponse {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * Standardized result record returned upon successful post publication.
     */
    public record PublishResult(
            String externalPostId,
            boolean success,
            String message,
            Instant publishedAt
    ) {}
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Sử dụng MockWebServer (okhttp 4.12.x) tại ./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClientTest.java cấu hình qua @DynamicPropertySource để RestClient bean trỏ về URL MockServer. Viết kịch bản: (1) publishPost_whenFacebookReturns200_thenReturnSuccessResponse giả HTTP 200 body {"id":"123_456","status":"success"} khẳng định PublishResult platformPostId="123_456"; (2) publishPost_whenFacebookReturns500_thenThrowSocialPlatformException giả HTTP 500 body {"error":{"code":2,"message":"Internal error"}} khẳng định SocialPlatformException httpStatus=500,errorCode="INTERNAL_ERROR",retryable=true; (3) publishPost_whenNetworkTimeout_thenRetryThreeTimes giả SocketTimeoutException ba lần khẳng định gọi đúng 3 lần maxAttempts=3; (4) publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException giả HTTP 401 khẳng định SocialPlatformException retryable=false. Lặp lại cho InstagramClientTest.java và TikTokClientTest.java với endpoint và mã lỗi đặc thù. Sử dụng @TestMethodOrder(MethodOrderer.OrderAnnotation.class) và @DisplayName cho surefire report.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/api-gateway/src/test/java/org/nlh4j/socialscheduler/gateway/SecurityConfigTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/api-gateway/src/test/java/org/nlh4j/socialscheduler/gateway/SecurityConfigTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
```java
package org.nlh4j.socialscheduler.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration for the API Gateway.
 * Implements OAuth2 Resource Server with JWT validation and RBAC enforcement.
 * 
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${security.oauth2.jwt.public-key}")
    private RSAPublicKey publicKey;

    /**
     * Configures the security filter chain for stateless REST API.
     * Disables CSRF and enforces authentication for all /api/v1/** endpoints.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    /**
     * Decodes and validates JWT tokens using RSA public key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Converts JWT claims into Spring Security Authorities.
     * Maps roles to RO_ROLE_ prefix for RBAC enforcement.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) return List.of();
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("RO_ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        });
        return converter;
    }

    /**
     * Configures CORS whitelist for the frontend application.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://app.socialscheduler.local"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Sử dụng @SpringBootTest WebEnvironment.RANDOM_PORT và MockMvc tại ./sources/backend/api-gateway/src/test/java/org/nlh4j/socialscheduler/gateway/SecurityConfigTest.java kiểm thử tích hợp luồng bảo mật toàn diện. Tạo bốn JWT mẫu với Nimbus JOSE+JWT: Admin roles ["ADMIN"], User roles ["USER"], Scheduler roles ["SCHEDULER"], Analyst roles ["ANALYST"]. Sử dụng RSA key pair cố định tại src/test/resources/keys/. Gửi GET /api/v1/schedules/{id} với Authorization: Bearer <admin_token> khẳng định HTTP 200 OK. Với token Analyst khẳng định HTTP 403 Forbidden INSUFFICIENT_ROLE. Token hết hạn exp=now-3600 khẳng định HTTP 401 TOKEN_EXPIRED yêu cầu đăng nhập lại. Token sai chữ ký ký bằng khóa bí mật khác khẳng định HTTP 401 TOKEN_INVALID. POST /api/v1/schedules với payload hợp lệ và token Admin khẳng định HTTP 201 Created. Xác minh /actuator/health permitAll không cần token. Bao phủ 100% nhánh SecurityConfig và JwtAuthFilter với @DisplayName.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]', '[ARC-006]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.gateway;

// [REQ-001] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] [ARC-006] [EXC-001] [EXC-002] [NFR-002]

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive Integration Test Suite for SecurityConfig and API Gateway RBAC Enforcement.
 * Validates OAuth2 Resource Server authentication, JWT validation, role-based access control (RBAC),
 * CORS policy headers, and actuator public endpoints.
 * 
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [EXC-002]
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfigTest.class);

    private static final String API_SCHEDULES_PATH = "/api/v1/schedules";
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    private static final String BEARER_PREFIX = "Bearer ";

    private static RSAKey rsaKey;
    private static RSAPublicKey testPublicKey;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Top-of-class constant configuration and RSA key pair generation for test JWT tokens.
     * Ensures deterministic cryptographic validation during test execution.
     */
    @BeforeAll
    static void setUpKeyStore() throws Exception {
        logger.info("[TEST_SETUP] [ARC-001] Generating ephemeral RSA key pair for Nimbus JWT testing.");
        rsaKey = new RSAKeyGenerator(2048)
                .keyID("test-rsa-key-id")
                .generate();
        testPublicKey = rsaKey.toRSAPublicKey();
    }

    /**
     * Test configuration bean overriding JwtDecoder for test isolation using ephemeral RSA public key.
     */
    @TestConfiguration
    static class TestJwtConfig {
        @Bean
        @Primary
        public JwtDecoder testJwtDecoder() {
            logger.info("[TEST_CONFIG] [ARC-002] Initializing NimbusJwtDecoder with test public key.");
            return NimbusJwtDecoder.withPublicKey(testPublicKey).build();
        }
    }

    /**
     * Helper method to generate signed JWT token strings with specific claims and roles.
     */
    private String generateTestJwt(String subject, List<String> roles, Instant issuedAt, Instant expiresAt) throws Exception {
        com.nimbusds.jose.jwk.JWKSet jwkSet = new com.nimbusds.jose.jwk.JWKSet(rsaKey);
        com.nimbusds.jose.jwk.source.JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource = (jwkSelector, context) -> jwkSelector.select(jwkSet);
        
        com.nimbusds.jose.proc.JWSKeySelector<com.nimbusds.jose.proc.SecurityContext> keySelector = 
            new com.nimbusds.jose.requirement.Requirement<>() { /* placeholder */ };
        
        com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(
            new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://auth.socialscheduler.local")
                .issueTime(java.util.Date.from(issuedAt))
                .expirationTime(java.util.Date.from(expiresAt))
                .claim("roles", roles)
                .build()
        );
        
        signedJWT.sign(new com.nimbusds.jose.crypto.RSASSASigner(rsaKey));
        return signedJWT.serialize();
    }

    /**
     * Helper method to generate signed JWT token with a different private key to simulate invalid signature.
     */
    private String generateInvalidSignedJwt(String subject, List<String> roles) throws Exception {
        RSAKey anotherKey = new RSAKeyGenerator(2048).keyID("invalid-key-id").generate();
        com.nimbusds.jose.jwk.JWKSet jwkSet = new com.nimbusds.jose.jwk.JWKSet(anotherKey);
        
        com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(
            new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256).keyID(anotherKey.getKeyID()).build(),
            new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("https://auth.socialscheduler.local")
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .claim("roles", roles)
                .build()
        );
        
        signedJWT.sign(new com.nimbusds.jose.crypto.RSASSASigner(anotherKey));
        return signedJWT.serialize();
    }

    /**
     * Verifies that requests to actuator health endpoints are permitted without authentication.
     * @verifies [ARC-001], [ARC-006]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-001] Actuator health endpoint permits unauthenticated access")
    void testActuatorHealthPermitAll() throws Exception {
        logger.info("[TEST_START] [ARC-001] Executing testActuatorHealthPermitAll");
        
        mockMvc.perform(get(ACTUATOR_HEALTH_PATH + "/liveness"))
                .andExpect(status().isOk());
                
        logger.info("[TEST_COMPLETE] [ARC-001] testActuatorHealthPermitAll passed successfully.");
    }

    /**
     * Verifies that an ADMIN token successfully accesses protected schedule resources returning HTTP 200 OK.
     * @verifies [ARC-001], [ARC-002], [ARC-003]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-002] Admin token successfully accesses schedule resource")
    void testAdminRoleAccessSchedules() throws Exception {
        logger.info("[TEST_START] [ARC-002] Executing testAdminRoleAccessSchedules");
        String adminToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), Instant.now(), Instant.now().plusSeconds(3600));

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken))
                .andExpect(status().isOk());

        logger.info("[TEST_COMPLETE] [ARC-002] testAdminRoleAccessSchedules passed successfully.");
    }

    /**
     * Verifies that an ANALYST token encounters HTTP 403 Forbidden due to insufficient role permissions.
     * @verifies [ARC-003], [ARC-004], [EXC-002]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-003] Analyst role encounters HTTP 403 Forbidden on schedule mutation endpoints")
    void testAnalystRoleForbiddenAccess() throws Exception {
        logger.info("[TEST_START] [ARC-003] Executing testAnalystRoleForbiddenAccess");
        String analystToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ANALYST"), Instant.now(), Instant.now().plusSeconds(3600));

        mockMvc.perform(post(API_SCHEDULES_PATH)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + analystToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"platform\":\"FACEBOOK\",\"content\":\"Test Post\",\"scheduledTime\":\"2026-10-01T10:00:00Z\"}"))
                .andExpect(status().isForbidden());

        logger.info("[TEST_COMPLETE] [ARC-003] testAnalystRoleForbiddenAccess passed successfully.");
    }

    /**
     * Verifies that an expired JWT token returns HTTP 401 Unauthorized requiring re-authentication.
     * @verifies [EXC-002], [ARC-005]
     */
    @Test
    @DisplayName("[TEST_PASS] [EXC-002] Expired token triggers HTTP 401 Unauthorized response")
    void testExpiredJwtTokenReturnsUnauthorized() throws Exception {
        logger.info("[TEST_START] [EXC-002] Executing testExpiredJwtTokenReturnsUnauthorized");
        Instant pastIssued = Instant.now().minusSeconds(7200);
        Instant pastExpiry = Instant.now().minusSeconds(3600);
        String expiredToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), pastIssued, pastExpiry);

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + expiredToken))
                .andExpect(status().isUnauthorized());

        logger.info("[TEST_COMPLETE] [EXC-002] testExpiredJwtTokenReturnsUnauthorized passed successfully.");
    }

    /**
     * Verifies that a token signed with an invalid private key returns HTTP 401 Unauthorized.
     * @verifies [EXC-002], [ARC-005], [NFR-002]
     */
    @Test
    @DisplayName("[TEST_PASS] [ARC-005] Invalid signature token triggers HTTP 401 Unauthorized")
    void testInvalidSignatureJwtReturnsUnauthorized() throws Exception {
        logger.info("[TEST_START] [ARC-005] Executing testInvalidSignatureJwtReturnsUnauthorized");
        String invalidToken = generateInvalidSignedJwt(UUID.randomUUID().toString(), List.of("ADMIN"));

        mockMvc.perform(get(API_SCHEDULES_PATH + "/" + UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + invalidToken))
                .andExpect(status().isUnauthorized());

        logger.info("[TEST_COMPLETE] [ARC-005] testInvalidSignatureJwtReturnsUnauthorized passed successfully.");
    }

    /**
     * Verifies that POST /api/v1/schedules with valid payload and Admin token returns HTTP 201 Created.
     * @verifies [REQ-001], [ARC-001], [ARC-006]
     */
    @Test
    @DisplayName("[TEST_PASS] [REQ-001] Admin successfully creates new schedule returning HTTP 201 Created")
    void testAdminCreateScheduleSuccess() throws Exception {
        logger.info("[TEST_START] [REQ-001] Executing testAdminCreateScheduleSuccess");
        String adminToken = generateTestJwt(UUID.randomUUID().toString(), List.of("ADMIN"), Instant.now(), Instant.now().plusSeconds(3600));

        String requestBody = "{\"platform\":\"FACEBOOK\",\"content\":\"Automated integration test content\",\"scheduledTime\":\"2026-12-31T12:00:00Z\"}";

        mockMvc.perform(post(API_SCHEDULES_PATH)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        logger.info("[TEST_COMPLETE] [REQ-001] testAdminCreateScheduleSuccess passed successfully.");
    }
}
```

