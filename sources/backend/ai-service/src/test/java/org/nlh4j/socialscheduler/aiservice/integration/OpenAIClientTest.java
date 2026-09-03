package org.nlh4j.socialscheduler.aiservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test suite for {@link OpenAIClient}.
 * <p>
 * Validates the OpenAI Chat Completions API integration layer using MockWebServer
 * to simulate HTTP responses. Covers happy paths, error scenarios, retry behavior,
 * and security header propagation.
 * </p>
 *
 * <p><b>Traceability:</b> [REQ-002], [EXC-003]</p>
 *
 * @author Enterprise Test Automation Engineer
 * @version 1.0
 * @since 2026-08-31
 */
@SpringBootTest(classes = {OpenAIClient.class, OpenAIClientTest.TestConfig.class})
@TestMethodOrder(OrderAnnotation.class)
class OpenAIClientTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic Numbers Policy)
    // =========================================================================

    /** Mock API key used for Bearer token verification. */
    private static final String MOCK_API_KEY = "test-api-key";

    /** Expected content returned by successful OpenAI response. */
    private static final String EXPECTED_GENERATED_CONTENT = "Generated content here";

    /** System prompt used across test scenarios. */
    private static final String SYSTEM_PROMPT = "You are a helpful assistant.";

    /** User prompt used across test scenarios. */
    private static final String USER_PROMPT = "Write a social media post about innovation.";

    /** Error code expected when AI service is unavailable. */
    private static final String ERROR_CODE_AI_UNAVAILABLE = "AI_SERVICE_UNAVAILABLE";

    /** HTTP status code for internal server error. */
    private static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    /** Maximum retry attempts configured in Resilience4j. */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /** Timeout duration for socket operations in milliseconds. */
    private static final int SOCKET_TIMEOUT_MS = 100;

    // =========================================================================
    // TEST INFRASTRUCTURE
    // =========================================================================

    /** MockWebServer instance simulating OpenAI API endpoints. */
    private static MockWebServer mockWebServer;

    /** OpenAIClient instance under test, injected with configured RestClient. */
    @Autowired
    private OpenAIClient openAIClient;

    // =========================================================================
    // LIFECYCLE MANAGEMENT
    // =========================================================================

    /**
     * Initializes MockWebServer before all tests.
     * Starts the server on an ephemeral port to avoid conflicts.
     */
    @BeforeAll
    static void setUpMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    /**
     * Shuts down MockWebServer after all tests complete.
     * Ensures no lingering threads or socket connections.
     */
    @AfterAll
    static void tearDownMockWebServer() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * Clears request queue and recorded requests before each test.
     * Ensures test isolation and deterministic request counts.
     */
    @BeforeEach
    void resetMockWebServer() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        mockWebServer.takeRequest(); // consume warmup request if any
    }

    // =========================================================================
    // DYNAMIC PROPERTY CONFIGURATION
    // =========================================================================

    /**
     * Injects MockWebServer base URL into Spring Environment.
     * Overrides the default OpenAI API base URL for test execution.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("openai.base-url", mockWebServer::url);
        registry.add("openai.api-key", () -> MOCK_API_KEY);
        registry.add("openai.model", () -> "gpt-4o-mini");
        registry.add("openai.max-tokens", () -> 500);
        registry.add("openai.temperature", () -> 0.7);
    }

    // =========================================================================
    // TEST CASES
    // =========================================================================

    /**
     * Test Case 1: Happy path - OpenAI returns HTTP 200 with valid content.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} correctly
     * extracts and returns the generated content from the API response.
     *
     * @verifies [REQ-002] - Content generation via OpenAI API
     * @verifies [EXC-003] - Successful response handling without exception
     */
    @Test
    @Order(1)
    @DisplayName("generateContent_whenOpenAiReturns200_thenReturnExtractedContent [REQ-002][EXC-003]")
    void generateContent_whenOpenAiReturns200_thenReturnExtractedContent() throws InterruptedException {
        // Arrange: Enqueue a successful OpenAI response with generated content
        String jsonResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "%s"
                  }
                }
              ]
            }
            """.formatted(EXPECTED_GENERATED_CONTENT);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        String result = openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify the extracted content matches the expected value
        assertThat(result)
                .as("Generated content should match the value from OpenAI response")
                .isEqualTo(EXPECTED_GENERATED_CONTENT);

        // Verify exactly one request was sent to the mock server
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest)
                .as("Exactly one request should be sent to OpenAI API")
                .isNotNull();
        assertThat(recordedRequest.getMethod())
                .as("Request method should be POST")
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .as("Request path should target chat completions endpoint")
                .isEqualTo("/chat/completions");
    }

    /**
     * Test Case 2: Server error - OpenAI returns HTTP 500.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} throws
     * {@link AiServiceException} with the correct error code and HTTP status.
     *
     * @verifies [EXC-003] - Server error mapping to AiServiceException
     */
    @Test
    @Order(2)
    @DisplayName("generateContent_whenOpenAiReturns500_thenThrowAiServiceException [EXC-003]")
    void generateContent_whenOpenAiReturns500_thenThrowAiServiceException() {
        // Arrange: Enqueue a 500 Internal Server Error response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HTTP_STATUS_INTERNAL_SERVER_ERROR)
                .setBody("{\"error\": \"Internal server error\"}")
                .addHeader("Content-Type: application/json"));

        // Act & Assert: Verify AiServiceException is thrown with correct attributes
        assertThatThrownBy(() -> openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT))
                .as("Should throw AiServiceException when OpenAI returns 500")
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining(ERROR_CODE_AI_UNAVAILABLE)
                .matches(ex -> ex instanceof AiServiceException aiEx &&
                        aiEx.getErrorCode().equals(ERROR_CODE_AI_UNAVAILABLE),
                        "Exception should have error code AI_SERVICE_UNAVAILABLE");
    }

    /**
     * Test Case 3: Network timeout - OpenAI API is unreachable.
     * Verifies that Resilience4j Retry mechanism triggers exactly 3 attempts
     * before throwing {@link AiServiceException}.
     *
     * @verifies [EXC-003] - Retry behavior on network failure
     */
    @Test
    @Order(3)
    @DisplayName("generateContent_whenNetworkTimeout_thenRetryThreeTimes [EXC-003]")
    void generateContent_whenNetworkTimeout_thenRetryThreeTimes() {
        // Arrange: Configure MockWebServer to simulate socket timeout
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                try {
                    Thread.sleep(SOCKET_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new MockResponse().setResponseCode(408);
            }
        });

        // Act & Assert: Verify AiServiceException is thrown after retries
        assertThatThrownBy(() -> openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT))
                .as("Should throw AiServiceException after exhausting retries")
                .isInstanceOf(AiServiceException.class);

        // Verify exactly MAX_RETRY_ATTEMPTS requests were made
        assertThat(mockWebServer.getRequestCount())
                .as("Should attempt request exactly %d times (maxAttempts from Resilience4j config)", MAX_RETRY_ATTEMPTS)
                .isEqualTo(MAX_RETRY_ATTEMPTS);
    }

    /**
     * Test Case 4: Empty choices array in response.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} handles
     * malformed responses gracefully by returning empty content or throwing
     * appropriate exception.
     *
     * @verifies [EXC-003] - Malformed response handling
     */
    @Test
    @Order(4)
    @DisplayName("generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException [EXC-003]")
    void generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException() {
        // Arrange: Enqueue response with empty choices array
        String jsonResponse = """
            {
              "choices": []
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        String result = openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify empty content is returned for malformed response
        assertThat(result)
                .as("Should return empty string when choices array is empty")
                .isEmpty();
    }

    /**
     * Test Case 5: Authorization header propagation.
     * Verifies that the Bearer token is correctly included in the
     * Authorization header of outgoing requests to OpenAI API.
     *
     * @verifies [REQ-002] - Secure API communication with authentication
     * @verifies [EXC-003] - Header propagation for authenticated requests
     */
    @Test
    @Order(5)
    @DisplayName("generateContent_whenAuthorizationHeader_thenSendBearerToken [REQ-002][EXC-003]")
    void generateContent_whenAuthorizationHeader_thenSendBearerToken() throws InterruptedException {
        // Arrange: Enqueue a successful response
        String jsonResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "%s"
                  }
                }
              ]
            }
            """.formatted(EXPECTED_GENERATED_CONTENT);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify Authorization header contains Bearer token
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest)
                .as("Request should be recorded for header verification")
                .isNotNull();

        String authorizationHeader = recordedRequest.getHeader(HttpHeaders.AUTHORIZATION);
        assertThat(authorizationHeader)
                .as("Authorization header should contain Bearer token")
                .isNotNull()
                .startsWith("Bearer ")
                .contains(MOCK_API_KEY);
    }

    // =========================================================================
    // TEST CONFIGURATION
    // =========================================================================

    /**
     * Spring configuration providing RestClient bean for OpenAIClient.
     * Configures RestClient with MockWebServer base URL and Bearer token.
     */
    @Configuration
    static class TestConfig {

        /**
         * Creates and configures RestClient bean for OpenAI API communication.
         * Uses MockWebServer URL as base URL and injects Bearer token.
         *
         * @return configured RestClient instance
         */
        @Bean
        RestClient openaiRestClient() {
            return RestClient.builder()
                    .baseUrl(mockWebServer.url("/").toString())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_API_KEY)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .requestTimeout(Duration.ofSeconds(5))
                    .build();
        }

        /**
         * Provides ObjectMapper bean for JSON serialization/deserialization.
         *
         * @return default ObjectMapper instance
         */
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}