package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test suite for PerformanceMetricsExceptionHandler.
 * Validates multi-component workflow for exception handling in content recommendation service.
 *
 * @verifies [EXC-003], [EXC-004]
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PerformanceMetricsExceptionHandlerTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic-Numbers Policy) [0.2]
    // =========================================================================

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMetricsExceptionHandlerTest.class);

    private static final String BASE_API_PATH = "/api/v1/performance-metrics";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String TEST_USER_ID = "test-user-id";
    private static final String INVALID_UUID_FORMAT = "invalid-uuid-format";
    private static final String NON_EXISTENT_POST_ID = "00000000-0000-0000-0000-000000000000";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final String ERROR_CODE_VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String ERROR_CODE_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    private static final String ERROR_CODE_INTERNAL_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String ERROR_CODE_DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
    private static final String TAG_EXC_003 = "[EXC-003]";
    private static final String TAG_EXC_004 = "[EXC-004]";
    private static final String LOG_PREFIX_TEST_START = "[TEST_START]";
    private static final String LOG_PREFIX_TEST_END = "[TEST_END]";
    private static final String LOG_PREFIX_ASSERTION = "[ASSERTION]";

    // =========================================================================
    // TESTCONTAINERS INFRASTRUCTURE SETUP
    // =========================================================================

    @Container
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("social_scheduler_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.redis.host", () -> "localhost");
        registry.add("spring.redis.port", () -> "6379");
    }

    // =========================================================================
    // DEPENDENCY INJECTION & RUNTIME STATE
    // =========================================================================

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    private String baseUrl;
    private HttpHeaders jsonHeaders;

    // =========================================================================
    // LIFECYCLE HOOKS
    // =========================================================================

    @BeforeEach
    void setUp() {
        LOGGER.info("{} [EXC-003][EXC-004] Initializing integration test environment for PerformanceMetricsExceptionHandler", LOG_PREFIX_TEST_START);
        baseUrl = "http://localhost:" + port + BASE_API_PATH;
        jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        performanceMetricsRepository.deleteAll();
        LOGGER.info("{} [EXC-003][EXC-004] Test environment initialized. Base URL: {}", LOG_PREFIX_TEST_START, baseUrl);
    }

    // =========================================================================
    // HAPPY PATH TESTS (Baseline Validation)
    // =========================================================================

    @Test
    @DisplayName("Happy Path: Create performance metrics successfully [EXC-003][EXC-004]")
    void createPerformanceMetrics_ValidRequest_ReturnsCreated() {
        // Given
        LOGGER.info("{} Happy Path: Testing valid performance metrics creation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating successful creation response [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("performanceId")).isNotNull();
        assertThat(response.getBody().get("postId")).isEqualTo(requestBody.get("postId"));
        assertThat(response.getBody().get("likes")).isEqualTo(requestBody.get("likes"));
        assertThat(response.getBody().get("comments")).isEqualTo(requestBody.get("comments"));
        assertThat(response.getBody().get("shares")).isEqualTo(requestBody.get("shares"));
        LOGGER.info("{} Happy Path test completed successfully [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Happy Path: Retrieve performance metrics by ID successfully [EXC-003][EXC-004]")
    void getPerformanceMetricsById_ExistingId_ReturnsMetrics() {
        // Given
        LOGGER.info("{} Happy Path: Testing retrieval of existing performance metrics [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        PerformanceMetrics savedMetrics = createAndPersistTestMetrics();

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + savedMetrics.getPerformanceId(),
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders),
                Map.class
        );

        // Then
        LOGGER.info("{} Validating successful retrieval response [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("performanceId")).isEqualTo(savedMetrics.getPerformanceId().toString());
        assertThat(response.getBody().get("postId")).isEqualTo(savedMetrics.getPostId().toString());
        LOGGER.info("{} Happy Path retrieval test completed successfully [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    // =========================================================================
    // EDGE CASE & BOUNDARY TESTS
    // =========================================================================

    @Test
    @DisplayName("Edge Case: Create performance metrics with zero engagement values [EXC-003][EXC-004]")
    void createPerformanceMetrics_ZeroEngagementValues_ReturnsCreated() {
        // Given
        LOGGER.info("{} Edge Case: Testing zero engagement values boundary [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("likes", 0);
        requestBody.put("comments", 0);
        requestBody.put("shares", 0);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating zero engagement values accepted [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_OK);
        assertThat(response.getBody().get("likes")).isEqualTo(0);
        assertThat(response.getBody().get("comments")).isEqualTo(0);
        assertThat(response.getBody().get("shares")).isEqualTo(0);
        LOGGER.info("{} Edge Case zero values test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Edge Case: Create performance metrics with maximum integer engagement values [EXC-003][EXC-004]")
    void createPerformanceMetrics_MaxIntegerValues_ReturnsCreated() {
        // Given
        LOGGER.info("{} Edge Case: Testing maximum integer boundary values [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("likes", Integer.MAX_VALUE);
        requestBody.put("comments", Integer.MAX_VALUE);
        requestBody.put("shares", Integer.MAX_VALUE);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating maximum integer values accepted [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_OK);
        assertThat(response.getBody().get("likes")).isEqualTo(Integer.MAX_VALUE);
        assertThat(response.getBody().get("comments")).isEqualTo(Integer.MAX_VALUE);
        assertThat(response.getBody().get("shares")).isEqualTo(Integer.MAX_VALUE);
        LOGGER.info("{} Edge Case max values test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Edge Case: Retrieve performance metrics with non-existent UUID returns 404 [EXC-003][EXC-004]")
    void getPerformanceMetricsById_NonExistentId_ReturnsNotFound() {
        // Given
        LOGGER.info("{} Edge Case: Testing non-existent resource retrieval [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + NON_EXISTENT_POST_ID,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders),
                Map.class
        );

        // Then
        LOGGER.info("{} Validating 404 Not Found response for non-existent resource [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_RESOURCE_NOT_FOUND);
        assertThat(response.getBody().get("message")).isNotNull();
        LOGGER.info("{} Edge Case non-existent resource test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    // =========================================================================
    // EXCEPTION CASES & NEGATIVE PATH TESTS (Core Exception Handler Validation)
    // =========================================================================

    @Test
    @DisplayName("Exception Case: Create performance metrics with missing required postId [EXC-003][EXC-004]")
    void createPerformanceMetrics_MissingPostId_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing missing required postId field [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("likes", 10);
        requestBody.put("comments", 5);
        requestBody.put("shares", 2);
        requestBody.put("collectedAt", LocalDateTime.now().toString());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for missing postId [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("postId");
        LOGGER.info("{} Exception Case missing postId test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with invalid postId UUID format [EXC-003][EXC-004]")
    void createPerformanceMetrics_InvalidPostIdFormat_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing invalid UUID format for postId [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("postId", INVALID_UUID_FORMAT);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for invalid UUID format [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("uuid");
        LOGGER.info("{} Exception Case invalid UUID format test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with negative likes value [EXC-003][EXC-004]")
    void createPerformanceMetrics_NegativeLikes_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing negative likes value validation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("likes", -5);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for negative likes [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("likes");
        LOGGER.info("{} Exception Case negative likes test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with negative comments value [EXC-003][EXC-004]")
    void createPerformanceMetrics_NegativeComments_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing negative comments value validation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("comments", -3);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for negative comments [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("comments");
        LOGGER.info("{} Exception Case negative comments test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with negative shares value [EXC-003][EXC-004]")
    void createPerformanceMetrics_NegativeShares_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing negative shares value validation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("shares", -1);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for negative shares [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("shares");
        LOGGER.info("{} Exception Case negative shares test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with missing collectedAt timestamp [EXC-003][EXC-004]")
    void createPerformanceMetrics_MissingCollectedAt_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing missing collectedAt timestamp [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.remove("collectedAt");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for missing collectedAt [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("collectedAt");
        LOGGER.info("{} Exception Case missing collectedAt test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with future collectedAt timestamp [EXC-003][EXC-004]")
    void createPerformanceMetrics_FutureCollectedAt_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing future collectedAt timestamp validation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("collectedAt", LocalDateTime.now().plusDays(1).toString());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for future timestamp [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("future");
        LOGGER.info("{} Exception Case future timestamp test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with invalid collectedAt format [EXC-003][EXC-004]")
    void createPerformanceMetrics_InvalidCollectedAtFormat_ReturnsValidationError() {
        // Given
        LOGGER.info("{} Exception Case: Testing invalid collectedAt format [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("collectedAt", "invalid-date-format");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating validation error response for invalid date format [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("date");
        LOGGER.info("{} Exception Case invalid date format test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create duplicate performance metrics for same postId [EXC-003][EXC-004]")
    void createPerformanceMetrics_DuplicatePostId_ReturnsConflictError() {
        // Given
        LOGGER.info("{} Exception Case: Testing duplicate postId constraint violation [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When - First creation succeeds
        ResponseEntity<Map> firstResponse = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);
        assertThat(firstResponse.getStatusCode().value()).isEqualTo(HTTP_OK);

        // When - Second creation with same postId should fail
        ResponseEntity<Map> secondResponse = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating conflict error response for duplicate postId [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(secondResponse.getStatusCode().value()).isEqualTo(HTTP_UNPROCESSABLE_ENTITY);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().get("errorCode")).isEqualTo(ERROR_CODE_DUPLICATE_RESOURCE);
        assertThat(secondResponse.getBody().get("message")).isNotNull();
        assertThat(secondResponse.getBody().get("message").toString()).containsIgnoringCase("duplicate");
        LOGGER.info("{} Exception Case duplicate resource test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Retrieve performance metrics with malformed UUID in path [EXC-003][EXC-004]")
    void getPerformanceMetricsById_MalformedUuid_ReturnsBadRequest() {
        // Given
        LOGGER.info("{} Exception Case: Testing malformed UUID in path parameter [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + INVALID_UUID_FORMAT,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders),
                Map.class
        );

        // Then
        LOGGER.info("{} Validating bad request response for malformed UUID [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        assertThat(response.getBody().get("message")).isNotNull();
        assertThat(response.getBody().get("message").toString()).containsIgnoringCase("uuid");
        LOGGER.info("{} Exception Case malformed UUID test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Update performance metrics with non-existent ID [EXC-003][EXC-004]")
    void updatePerformanceMetrics_NonExistentId_ReturnsNotFound() {
        // Given
        LOGGER.info("{} Exception Case: Testing update of non-existent resource [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("likes", 100);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + NON_EXISTENT_POST_ID,
                HttpMethod.PUT,
                request,
                Map.class
        );

        // Then
        LOGGER.info("{} Validating not found response for update of non-existent resource [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_RESOURCE_NOT_FOUND);
        LOGGER.info("{} Exception Case update non-existent test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Delete performance metrics with non-existent ID [EXC-003][EXC-004]")
    void deletePerformanceMetrics_NonExistentId_ReturnsNotFound() {
        // Given
        LOGGER.info("{} Exception Case: Testing deletion of non-existent resource [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/" + NON_EXISTENT_POST_ID,
                HttpMethod.DELETE,
                new HttpEntity<>(jsonHeaders),
                Map.class
        );

        // Then
        LOGGER.info("{} Validating not found response for deletion of non-existent resource [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_RESOURCE_NOT_FOUND);
        LOGGER.info("{} Exception Case delete non-existent test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with null request body [EXC-003][EXC-004]")
    void createPerformanceMetrics_NullRequestBody_ReturnsBadRequest() {
        // Given
        LOGGER.info("{} Exception Case: Testing null request body handling [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating bad request response for null body [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        LOGGER.info("{} Exception Case null body test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with empty request body [EXC-003][EXC-004]")
    void createPerformanceMetrics_EmptyRequestBody_ReturnsBadRequest() {
        // Given
        LOGGER.info("{} Exception Case: Testing empty request body handling [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(new HashMap<>(), jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating bad request response for empty body [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        LOGGER.info("{} Exception Case empty body test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    @Test
    @DisplayName("Exception Case: Create performance metrics with invalid content type [EXC-003][EXC-004]")
    void createPerformanceMetrics_InvalidContentType_ReturnsUnsupportedMediaType() {
        // Given
        LOGGER.info("{} Exception Case: Testing invalid content type handling [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        HttpHeaders xmlHeaders = new HttpHeaders();
        xmlHeaders.setContentType(MediaType.APPLICATION_XML);
        String xmlBody = "<performance><postId>" + UUID.randomUUID() + "</postId><likes>10</likes></performance>";
        HttpEntity<String> request = new HttpEntity<>(xmlBody, xmlHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating unsupported media type response [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        LOGGER.info("{} Exception Case invalid content type test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    // =========================================================================
    // DATABASE CONSTRAINT VIOLATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Exception Case: Database constraint violation on null postId [EXC-003][EXC-004]")
    void createPerformanceMetrics_NullPostId_DatabaseConstraintViolation() {
        // Given
        LOGGER.info("{} Exception Case: Testing database constraint violation for null postId [EXC-003][EXC-004]", LOG_PREFIX_TEST_START);
        Map<String, Object> requestBody = buildValidPerformanceMetricsRequest();
        requestBody.put("postId", null);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, jsonHeaders);

        // When
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request, Map.class);

        // Then
        LOGGER.info("{} Validating database constraint violation response [EXC-003][EXC-004]", LOG_PREFIX_ASSERTION);
        assertThat(response.getStatusCode().value()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("errorCode")).isEqualTo(ERROR_CODE_VALIDATION_FAILED);
        LOGGER.info("{} Exception Case null postId constraint test completed [EXC-003][EXC-004]", LOG_PREFIX_TEST_END);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private Map<String, Object> buildValidPerformanceMetricsRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("postId", UUID.randomUUID().toString());
        request.put("likes", 100);
        request.put("comments", 25);
        request.put("shares", 10);
        request.put("collectedAt", LocalDateTime.now().toString());
        return request;
    }

    private PerformanceMetrics createAndPersistTestMetrics() {
        PerformanceMetrics metrics = PerformanceMetrics.builder()
                .performanceId(UUID.randomUUID())
                .postId(UUID.randomUUID())
                .likes(50)
                .comments(15)
                .shares(5)
                .collectedAt(LocalDateTime.now())
                .build();
        return performanceMetricsRepository.save(metrics);
    }
}