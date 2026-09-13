# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for AI-powered content recommendation based on historical performance metrics.
 * <p>
 * This component implements the core recommendation engine that analyzes past post performance
 * (likes, comments, shares) to suggest optimal content for future scheduling.
 * The algorithm ranks posts by an aggregated engagement score and returns the top-N recommendations.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Retrieve performance metrics for a given user's posts.</li>
 *   <li>Apply a deterministic AI-inspired ranking algorithm (engagement score).</li>
 *   <li>Map ranked metrics back to {@link Schedule} entities for downstream processing.</li>
 *   <li>Provide comprehensive logging and exception handling with traceability tags.</li>
 * </ul>
 *
 * @traceability [REQ-002]
 */
@Service
public class PerformanceMetricsService {

    // Enterprise‑grade logger for audit and monitoring
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsService.class);

    // Configuration constants – anti‑magic‑numbers enforcement
    /** Maximum number of recommendations to return per request. */
    public static final int DEFAULT_TOP_N_RECOMMENDATIONS = 5;
    /** Version identifier for the recommendation algorithm – aids A/B testing and rollback. */
    public static final String RECOMMENDATION_ALGORITHM_VERSION = "v1.0";

    /** Repository for accessing performance metrics data – Spring Data JPA layer. */
    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    /** Repository for retrieving schedule details – required to return full post context. */
    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * Generates AI‑driven content recommendations for a specific user.
     * <p>
     * The method follows a strict three‑phase pipeline:
     * <ol>
     *   <li><b>Data Ingestion</b> – Fetch all {@link PerformanceMetrics} belonging to the user.</li>
     *   <li><b>Scoring & Ranking</b> – Compute an engagement score
     *       ({@code score = likes * 3 + comments * 2 + shares * 5}) and sort descending.</li>
     *   <li><b>Result Compilation</b> – Map the top‑N ranked metrics back to {@link Schedule}
     *       entities and return them.</li>
     * </ol>
     *
     * @param userId   Identifier of the user for whom recommendations are required.
     * @param topN     Optional limit on the number of recommendations; defaults to
     *                {@link #DEFAULT_TOP_N_RECOMMENDATIONS} if {@code <= 0}.
     * @return A list of {@link Schedule} objects representing the highest‑performing posts.
     * @throws PerformanceMetricsServiceException If any data access or processing error occurs.
     * @traceability [REQ-002]
     */
    public List<Schedule> recommendContentForUser(Long userId, int topN) {
        // Entry‑point logging – captures the full request context for audit trails
        logger.info("[REQ-002] Initiating AI content recommendation for userId={} (topN={})", userId, topN);

        try {
            // Normalize topN – enforce business rule that topN must be positive
            int limit = (topN > 0) ? topN : DEFAULT_TOP_N_RECOMMENDATIONS;

            // Phase 1: Retrieve raw performance metrics for the user
            List<PerformanceMetrics> metrics = performanceMetricsRepository
                    .findByScheduleUserId(userId);

            if (metrics == null || metrics.isEmpty()) {
                logger.info("[REQ-002] No performance metrics found for userId={}; returning empty recommendation list.", userId);
                return Collections.emptyList();
            }

            // Phase 2: Apply deterministic AI‑inspired ranking algorithm
            // Engagement score formula – weighted sum of interactions to emulate AI ranking
            List<PerformanceMetrics> rankedMetrics = metrics.stream()
                    .sorted((m1, m2) -> {
                        long score1 = m1.getLikes() * 3L + m1.getComments() * 2L + m1.getShares() * 5L;
                        long score2 = m2.getLikes() * 3L + m2.getComments() * 2L + m2.getShares() * 5L;
                        return Long.compare(score2, score1); // descending order
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            // Phase 3: Map ranked metrics back to full Schedule entities for downstream use
            List<Schedule> recommendedSchedules = rankedMetrics.stream()
                    .map(metric -> scheduleRepository.findById(metric.getScheduleId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Schedule not found for metric id=" + metric.getPerformanceId())))
                    .collect(Collectors.toList());

            // Exit‑point logging – includes the count of recommendations generated
            logger.info("[REQ-002] Completed AI recommendation for userId={}; returned {} schedules.", userId, recommendedSchedules.size());
            return recommendedSchedules;

        } catch (Exception e) {
            // Comprehensive error logging – preserves raw exception and traceability tag
            logger.error("[REQ-002] AI content recommendation failed for userId={}. Raw error: {}", userId, e.getMessage(), e);
            // Re‑throw as enterprise‑typed exception while preserving the original cause
            throw new PerformanceMetricsServiceException("Failed to generate content recommendations", e);
        }
    }

    /**
     * Convenience overload that uses the default top‑N limit.
     *
     * @param userId Identifier of the user.
     * @return Recommended schedules.
     * @see #recommendContentForUser(Long, int)
     */
    public List<Schedule> recommendContentForUser(Long userId) {
        return recommendContentForUser(userId, 0);
    }

    /**
     * Custom enterprise exception for this service – ensures consistent error handling
     * across the recommendation pipeline while preserving the original cause chain.
     */
    public static class PerformanceMetricsServiceException extends RuntimeException {
        public PerformanceMetricsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm tra dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for AI-powered content recommendation based on historical performance metrics.
 * <p>
 * This component implements the core recommendation engine that analyzes past post performance
 * (likes, comments, shares) to suggest optimal content for future scheduling.
 * The algorithm ranks posts by an aggregated engagement score and returns the top-N recommendations.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Retrieve performance metrics for a given user's posts.</li>
 *   <li>Apply a deterministic AI-inspired ranking algorithm (engagement score).</li>
 *   <li>Map ranked metrics back to {@link Schedule} entities for downstream processing.</li>
 *   <li>Provide comprehensive logging and exception handling with traceability tags.</li>
 * </ul>
 *
 * @traceability [REQ-002]
 */
@Service
public class PerformanceMetricsService {

    // Enterprise‑grade logger for audit and monitoring
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsService.class);

    // Configuration constants – anti‑magic‑numbers enforcement
    /** Maximum number of recommendations to return per request. */
    public static final int DEFAULT_TOP_N_RECOMMENDATIONS = 5;
    /** Version identifier for the recommendation algorithm – aids A/B testing and rollback. */
    public static final String RECOMMENDATION_ALGORITHM_VERSION = "v1.0";

    /** Repository for accessing performance metrics data – Spring Data JPA layer. */
    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    /** Repository for retrieving schedule details – required to return full post context. */
    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * Generates AI‑driven content recommendations for a specific user.
     * <p>
     * The method follows a strict three‑phase pipeline:
     * <ol>
     *   <li><b>Data Ingestion</b> – Fetch all {@link PerformanceMetrics} belonging to the user.</li>
     *   <li><b>Scoring & Ranking</b> – Compute an engagement score
     *       ({@code score = likes * 3 + comments * 2 + shares * 5}) and sort descending.</li>
     *   <li><b>Result Compilation</b> – Map the top‑N ranked metrics back to {@link Schedule}
     *       entities and return them.</li>
     * </ol>
     *
     * @param userId   Identifier of the user for whom recommendations are required.
     * @param topN     Optional limit on the number of recommendations; defaults to
     *                {@link #DEFAULT_TOP_N_RECOMMENDATIONS} if {@code <= 0}.
     * @return A list of {@link Schedule} objects representing the highest‑performing posts.
     * @throws PerformanceMetricsServiceException If any data access or processing error occurs.
     * @traceability [REQ-002]
     */
    public List<Schedule> recommendContentForUser(Long userId, int topN) {
        // Entry‑point logging – captures the full request context for audit trails
        logger.info("[REQ-002] Initiating AI content recommendation for userId={} (topN={})", userId, topN);

        try {
            // Normalize topN – enforce business rule that topN must be positive
            int limit = (topN > 0) ? topN : DEFAULT_TOP_N_RECOMMENDATIONS;

            // Phase 1: Retrieve raw performance metrics for the user
            List<PerformanceMetrics> metrics = performanceMetricsRepository
                    .findByScheduleUserId(userId);

            if (metrics == null || metrics.isEmpty()) {
                logger.info("[REQ-002] No performance metrics found for userId={}; returning empty recommendation list.", userId);
                return Collections.emptyList();
            }

            // Phase 2: Apply deterministic AI‑inspired ranking algorithm
            // Engagement score formula – weighted sum of interactions to emulate AI ranking
            List<PerformanceMetrics> rankedMetrics = metrics.stream()
                    .sorted((m1, m2) -> {
                        long score1 = m1.getLikes() * 3L + m1.getComments() * 2L + m1.getShares() * 5L;
                        long score2 = m2.getLikes() * 3L + m2.getComments() * 2L + m2.getShares() * 5L;
                        return Long.compare(score2, score1); // descending order
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            // Phase 3: Map ranked metrics back to full Schedule entities for downstream use
            List<Schedule

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsServiceTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm tra dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceMetricsServiceTest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("social_scheduler_test")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private PerformanceMetricsService performanceMetricsService;

    @Autowired
    private PerformanceMetricsRepository performanceMetricsRepository;

    @BeforeAll
    static void setupContainer() {
        postgresContainer.start();
    }

    @AfterAll
    static void teardownContainer() {
        postgresContainer.stop();
    }

    /**
     * Integration Test Suite for PerformanceMetricsService
     * Validates content recommendation service performance metric operations
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */

    /**
     * Test [REQ-002] Create performance metrics with valid data - Happy Path
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Create performance metrics with valid data - Happy Path")
    void createPerformanceMetrics_ValidData_Success() {
        // Given: A valid PerformanceMetrics entity with required fields populated
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(100);
        metrics.setComments(25);
        metrics.setShares(10);
        metrics.setCollectedAt(new Date());

        // When: Service persists the metrics via save operation
        var saved = performanceMetricsService.save(metrics);

        // Then: Entity should be persisted with auto-generated ID and all attributes preserved
        assertNotNull(saved.getPerformanceId(), "Performance ID must not be null after persistence");
        assertEquals(100, saved.getLikes(), "Likes count must match the input value");
        assertEquals(25, saved.getComments(), "Comments count must match the input value");
        assertEquals(10, saved.getShares(), "Shares count must match the input value");
        assertNotNull(saved.getCollectedAt(), "CollectedAt timestamp must not be null");
        // [REQ-002] Business requirement: Performance metrics must be persistently stored with all required fields populated correctly
        // [EXC-003] Edge case validation: Ensures no silent failure during persistence with valid input
        // [EXC-004] Negative path guard: Verifies successful storage prevents subsequent retrieval exceptions
    }

    /**
     * Test [REQ-002] Retrieve performance metrics by ID - Success Path
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Retrieve performance metrics by ID - Success Path")
    void retrievePerformanceMetrics_ById_Success() {
        // Given: A saved performance metrics entity
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(50);
        metrics.setComments(10);
        metrics.setShares(5);
        metrics.setCollectedAt(new Date());
        performanceMetricsRepository.save(metrics);

        // When: Service retrieves metrics by its generated ID
        var found = performanceMetricsService.findById(metrics.getPerformanceId());

        // Then: Retrieved entity must match the originally saved entity exactly
        assertNotNull(found, "Found metrics record must not be null");
        assertEquals(metrics.getPerformanceId(), found.getPerformanceId(), "Performance ID must match the saved record");
        assertEquals(50, found.getLikes(), "Likes must match saved value");
        assertEquals(10, found.getComments(), "Comments must match saved value");
        assertEquals(5, found.getShares(), "Shares must match saved value");
        // [REQ-002] Business requirement: Retrieval by ID must return the exact persisted record
        // [EXC-003] Exception handling guard: Ensures valid ID retrieval avoids unnecessary exception triggers
        // [EXC-004] Data integrity check: Confirms retrieved data matches stored data schema
    }

    /**
     * Test [REQ-002] Null postId should trigger appropriate validation exception
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Null postId should trigger appropriate validation exception")
    void createPerformanceMetrics_NullPostId_ThrowsException() {
        // Given: PerformanceMetrics entity with null postId and partial engagement data
        var metrics = new PerformanceMetrics();
        metrics.setLikes(10);
        metrics.setComments(5);
        metrics.setShares(2);

        // When/Then: Service save operation must throw IllegalArgumentException due to null postId
        assertThrows(IllegalArgumentException.class, () -> {
            performanceMetricsService.save(metrics);
        });
        // [REQ-002] Business requirement: Null postId must be rejected per entity validation constraints
        // [EXC-003] Exception case: Confirms IllegalArgumentException is thrown for invalid entity state
        // [EXC-004] Negative path: Ensures system does not persist invalid records silently
    }

    /**
     * Test [REQ-002] Zero values for engagement metrics must be accepted per schema
     * @verifies [REQ-002], [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("Test [REQ-002] Zero values for engagement metrics must be accepted per schema")
    void createPerformanceMetrics_ZeroEngagementValues_Accepted() {
        // Given: PerformanceMetrics entity with zero engagement metrics and valid postId
        var metrics = new PerformanceMetrics();
        metrics.setPostId(UUID.randomUUID());
        metrics.setLikes(0);
        metrics.setComments(0);
        metrics.setShares(0);
        metrics.setCollectedAt(new Date());

        // When: Service persists the entity with zero engagement values
        var saved = performanceMetricsService.save(metrics);

        // Then: Zero values must be accepted and stored without validation error
        assertNotNull(saved.getPerformanceId(), "Performance ID must not be null even with zero engagement");
        assertEquals(0, saved.getLikes(), "Likes must be stored as zero");
        assertEquals(0, saved.getComments(), "Comments must be stored as zero");
        assertEquals(0, saved.getShares(), "Shares must be stored as zero");
        // [REQ-002] Business requirement: Zero engagement values are valid and must be accepted per schema definition
        // [EXC-003] Edge case: Ensures zero values do not trigger unexpected exceptions during save
        // [EXC-004] Boundary condition: Verifies zero values are stored correctly at lower capacity limit
    }
}
```

# Day 1: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```java
/**
 * PerformanceMetricsExceptionHandler
 *
 * This class serves as a centralized exception handling component for the Content Recommendation Service.
 * It intercepts and processes exceptions thrown within the content recommendation module, ensuring
 * consistent error responses, detailed logging, and preservation of the original exception cause chain.
 *
 * Traceability Tags: [EXC-003], [EXC-004]
 */
package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataAccessException;

/**
 * Custom enterprise business exception for content recommendation failures.
 * This exception is used to wrap underlying causes while preserving the original stack trace.
 *
 * Traceability Tags: [EXC-003], [EXC-004]
 */
class ContentRecommendationException extends RuntimeException {
    public ContentRecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Global exception handler for the Content Recommendation Service.
 * Provides centralized handling for various exception types, logs detailed error information,
 * and re-throws wrapped business exceptions to maintain traceability.
 *
 * Traceability Tags: [EXC-003], [EXC-004]
 */
@ControllerAdvice
public class PerformanceMetricsExceptionHandler {

    /** Logger instance for capturing exception details and traceability information. */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsExceptionHandler.class);

    /**
     * Constant defining the service name for logging and error reporting.
     * This ensures a single source of truth for the service identifier.
     */
    public static final String SERVICE_NAME = "ContentRecommendationService";

    /**
     * Constant encapsulating traceability tag identifiers for this exception handler.
     * Used in log statements to satisfy audit requirements.
     */
    public static final String TRACEABILITY_TAGS = "[EXC-003], [EXC-004]";

    /**
     * Handles generic RuntimeException instances.
     * Logs the error with module name, raw exception message, and traceability tag,
     * then wraps and re-throws the exception preserving the original cause.
     *
     * @param ex   the caught RuntimeException
     * @param request the current web request (unused but required for signature)
     * @return a generic error response (not used due to rethrow)
     * @throws ContentRecommendationException always, wrapping the original exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        // Entry log for traceability
        logger.info("[ENTRY] Handling RuntimeException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);

        try {
            // Comprehensive error logging as per enterprise audit requirements
            logger.error("[CRITICAL FAIL] [EXC-003] Content recommendation processing failed due to unexpected runtime error. Raw error: {}. Traceability Tags: {}", ex.getMessage(), TRACEABILITY_TAGS, ex);
        } finally {
            // Exit log to mark completion of this exception handling path
            logger.info("[EXIT] Completed handling RuntimeException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);
        }

        // Preserve the original cause chain by wrapping in custom business exception
        ContentRecommendationException wrapped = new ContentRecommendationException(
            "Unexpected runtime error in content recommendation service", ex);
        // Re-throw to propagate up the stack while maintaining cause
        throw wrapped;
    }

    /**
     * Handles IllegalArgumentException instances.
     * Typically raised when invalid input parameters are supplied to recommendation algorithms.
     *
     * @param ex   the caught IllegalArgumentException
     * @param request the current web request
     * @return a generic error response (not used due to rethrow)
     * @throws ContentRecommendationException always, wrapping the original exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.info("[ENTRY] Handling IllegalArgumentException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);

        try {
            logger.error("[CRITICAL FAIL] [EXC-004] Invalid argument supplied to content recommendation service. Raw error: {}. Traceability Tags: {}", ex.getMessage(), TRACEABILITY_TAGS, ex);
        } finally {
            logger.info("[EXIT] Completed handling IllegalArgumentException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);
        }

        ContentRecommendationException wrapped = new ContentRecommendationException(
            "Invalid argument in content recommendation service", ex);
        throw wrapped;
    }

    /**
     * Handles DataAccessException instances.
     * Captures database access failures that may affect recommendation data retrieval.
     *
     * @param ex   the caught DataAccessException
     * @param request the current web request
     * @return a generic error response (not used due to rethrow)
     * @throws ContentRecommendationException always, wrapping the original exception
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex, WebRequest request) {
        logger.info("[ENTRY] Handling DataAccessException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);

        try {
            logger.error("[CRITICAL FAIL] [EXC-003] Database access failure in content recommendation service. Raw error: {}. Traceability Tags: {}", ex.getMessage(), TRACEABILITY_TAGS, ex);
        } finally {
            logger.info("[EXIT] Completed handling DataAccessException in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);
        }

        ContentRecommendationException wrapped = new ContentRecommendationException(
            "Database access error in content recommendation service", ex);
        throw wrapped;
    }

    /**
     * Handles generic Exception instances as a safety net.
     * Ensures that any unforeseen exception is logged and wrapped appropriately.
     *
     * @param ex   the caught generic Exception
     * @param request the current web request
     * @return a generic error response (not used due to rethrow)
     * @throws ContentRecommendationException always, wrapping the original exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex, WebRequest request) {
        logger.info("[ENTRY] Handling generic Exception in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);

        try {
            logger.error("[CRITICAL FAIL] [EXC-004] Unexpected error in content recommendation service. Raw error: {}. Traceability Tags: {}", ex.getMessage(), TRACEABILITY_TAGS, ex);
        } finally {
            logger.info("[EXIT] Completed handling generic Exception in {} - Traceability Tags: {}", SERVICE_NAME, TRACEABILITY_TAGS);
        }

        ContentRecommendationException wrapped = new ContentRecommendationException(
            "Unexpected error in content recommendation service", ex);
        throw wrapped;
    }
}
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các ngoại lệ liên quan đến đề xuất nội dung.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.contentrecommendationservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

/**
 * Integration test suite for {@link PerformanceMetricsExceptionHandler}.
 *
 * <p>Validates the centralized exception handling behavior of the Content Recommendation Service,
 * ensuring that all exception types are intercepted, logged with traceability tags, and wrapped
 * into {@link ContentRecommendationException} while preserving the original cause chain.</p>
 *
 * <p>This test suite operates under INTEGRATION_SCOPE, bootstrapping the full Spring MVC
 * exception handling pipeline through {@link ControllerAdvice} interception. All external
 * dependencies (e.g., database, Kafka) are mocked to isolate the exception handler logic
 * while validating multi-component interaction contracts.</p>
 *
 * Traceability Tags: [EXC-003], [EXC-004]
 *
 * @verifies [EXC-003] RuntimeException handling and cause chain preservation
 * @verifies [EXC-004] IllegalArgumentException and generic Exception handling
 */
@ExtendWith(MockitoExtension.class)
class PerformanceMetricsExceptionHandlerTest {

    /**
     * Logger instance for capturing test execution flow and traceability information.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsExceptionHandlerTest.class);

    /**
     * Constant defining the service name for logging and error reporting.
     * Ensures a single source of truth for the service identifier.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    private static final String SERVICE_NAME = "ContentRecommendationService";

    /**
     * Constant encapsulating traceability tag identifiers for this exception handler.
     * Used in log statements to satisfy audit requirements.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    private static final String TRACEABILITY_TAGS = "[EXC-003], [EXC-004]";

    /**
     * Mocked WebRequest instance to simulate HTTP request context during exception handling.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @Mock
    private WebRequest mockWebRequest;

    /**
     * Injected instance of the exception handler under test.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @InjectMocks
    private PerformanceMetricsExceptionHandler exceptionHandler;

    /**
     * Setup method executed before each test case.
     * Initializes the exception handler instance and logs the test start.
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @BeforeEach
    void setUp() {
        logger.info("[TEST_START] Initializing PerformanceMetricsExceptionHandler integration test suite - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that a RuntimeException is intercepted, logged with traceability tags,
     * and wrapped into a ContentRecommendationException preserving the original cause.
     *
     * <p>Business Requirement: [EXC-003] - Ensure runtime errors in content recommendation
     * are captured and propagated with full traceability.</p>
     *
     * Traceability Tags: [EXC-003]
     */
    @Test
    @DisplayName("handleRuntimeException should log error and wrap exception with cause chain [EXC-003]")
    void handleRuntimeException_ShouldLogAndWrapWithCauseChain() {
        // Arrange: Create a RuntimeException with a specific message to simulate an unexpected error
        RuntimeException runtimeException = new RuntimeException("Simulated runtime failure in recommendation engine");

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleRuntimeException(runtimeException, mockWebRequest),
            "Expected handleRuntimeException to throw ContentRecommendationException"
        );

        // Verify that the original exception is preserved as the cause
        assertEquals(runtimeException, thrown.getCause(),
            "The original RuntimeException must be preserved as the cause of the wrapped exception");

        // Verify that the error message is propagated correctly
        assertTrue(thrown.getMessage().contains("Unexpected runtime error"),
            "The wrapped exception message should indicate an unexpected runtime error");

        logger.info("[TEST_PASS] RuntimeException handling validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that an IllegalArgumentException is intercepted, logged with traceability tags,
     * and wrapped into a ContentRecommendationException preserving the original cause.
     *
     * <p>Business Requirement: [EXC-004] - Ensure invalid input parameters to recommendation
     * algorithms are captured and propagated with full traceability.</p>
     *
     * Traceability Tags: [EXC-004]
     */
    @Test
    @DisplayName("handleIllegalArgumentException should log error and wrap exception with cause chain [EXC-004]")
    void handleIllegalArgumentException_ShouldLogAndWrapWithCauseChain() {
        // Arrange: Create an IllegalArgumentException with a specific message to simulate invalid input
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Invalid recommendation parameter: null content vector");

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleIllegalArgumentException(illegalArgumentException, mockWebRequest),
            "Expected handleIllegalArgumentException to throw ContentRecommendationException"
        );

        // Verify that the original exception is preserved as the cause
        assertEquals(illegalArgumentException, thrown.getCause(),
            "The original IllegalArgumentException must be preserved as the cause of the wrapped exception");

        // Verify that the error message is propagated correctly
        assertTrue(thrown.getMessage().contains("Invalid argument"),
            "The wrapped exception message should indicate an invalid argument error");

        logger.info("[TEST_PASS] IllegalArgumentException handling validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that a DataAccessException is intercepted, logged with traceability tags,
     * and wrapped into a ContentRecommendationException preserving the original cause.
     *
     * <p>Business Requirement: [EXC-003] - Ensure database access failures affecting
     * recommendation data retrieval are captured and propagated with full traceability.</p>
     *
     * Traceability Tags: [EXC-003]
     */
    @Test
    @DisplayName("handleDataAccessException should log error and wrap exception with cause chain [EXC-003]")
    void handleDataAccessException_ShouldLogAndWrapWithCauseChain() {
        // Arrange: Create a DataAccessException with a specific message to simulate database failure
        DataAccessException dataAccessException = new DataAccessResourceFailureException("Database connection timeout during metrics retrieval");

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleDataAccessException(dataAccessException, mockWebRequest),
            "Expected handleDataAccessException to throw ContentRecommendationException"
        );

        // Verify that the original exception is preserved as the cause
        assertEquals(dataAccessException, thrown.getCause(),
            "The original DataAccessException must be preserved as the cause of the wrapped exception");

        // Verify that the error message is propagated correctly
        assertTrue(thrown.getMessage().contains("Database access error"),
            "The wrapped exception message should indicate a database access error");

        logger.info("[TEST_PASS] DataAccessException handling validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that a generic Exception is intercepted, logged with traceability tags,
     * and wrapped into a ContentRecommendationException preserving the original cause.
     *
     * <p>Business Requirement: [EXC-004] - Ensure any unforeseen exception is captured
     * and propagated with full traceability as a safety net.</p>
     *
     * Traceability Tags: [EXC-004]
     */
    @Test
    @DisplayName("handleAllOtherExceptions should log error and wrap exception with cause chain [EXC-004]")
    void handleAllOtherExceptions_ShouldLogAndWrapWithCauseChain() {
        // Arrange: Create a generic Exception with a specific message to simulate an unforeseen error
        Exception genericException = new Exception("Unexpected error in AI model inference pipeline");

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleAllOtherExceptions(genericException, mockWebRequest),
            "Expected handleAllOtherExceptions to throw ContentRecommendationException"
        );

        // Verify that the original exception is preserved as the cause
        assertEquals(genericException, thrown.getCause(),
            "The original Exception must be preserved as the cause of the wrapped exception");

        // Verify that the error message is propagated correctly
        assertTrue(thrown.getMessage().contains("Unexpected error"),
            "The wrapped exception message should indicate an unexpected error");

        logger.info("[TEST_PASS] Generic Exception handling validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that the exception handler correctly handles a null exception message
     * without causing a NullPointerException during logging or wrapping.
     *
     * <p>Edge Case: Ensures robustness when exceptions are thrown with null messages.</p>
     *
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("handleRuntimeException should handle null exception message gracefully [EXC-003][EXC-004]")
    void handleRuntimeException_ShouldHandleNullMessageGracefully() {
        // Arrange: Create a RuntimeException with a null message to test edge case handling
        RuntimeException runtimeExceptionWithNullMessage = new RuntimeException((String) null);

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleRuntimeException(runtimeExceptionWithNullMessage, mockWebRequest),
            "Expected handleRuntimeException to throw ContentRecommendationException even with null message"
        );

        // Verify that the original exception is preserved as the cause
        assertEquals(runtimeExceptionWithNullMessage, thrown.getCause(),
            "The original RuntimeException with null message must be preserved as the cause");

        logger.info("[TEST_PASS] Null message handling validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that the exception handler correctly handles a deeply nested exception chain,
     * ensuring the root cause is preserved through multiple wrapping layers.
     *
     * <p>Edge Case: Ensures cause chain integrity across nested exception scenarios.</p>
     *
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("handleRuntimeException should preserve deeply nested cause chain [EXC-003][EXC-004]")
    void handleRuntimeException_ShouldPreserveDeeplyNestedCauseChain() {
        // Arrange: Create a deeply nested exception chain to test cause preservation
        Throwable rootCause = new OutOfMemoryError("Root cause: Insufficient memory for AI model loading");
        Exception intermediateException = new Exception("Intermediate processing failure", rootCause);
        RuntimeException runtimeException = new RuntimeException("Top-level runtime failure", intermediateException);

        // Act & Assert: Verify that the handler throws a wrapped ContentRecommendationException
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleRuntimeException(runtimeException, mockWebRequest),
            "Expected handleRuntimeException to throw ContentRecommendationException with nested cause chain"
        );

        // Verify that the immediate cause is the original RuntimeException
        assertEquals(runtimeException, thrown.getCause(),
            "The immediate cause should be the original RuntimeException");

        // Verify that the root cause is preserved through the chain
        assertEquals(rootCause, thrown.getCause().getCause().getCause(),
            "The root cause should be preserved through the entire exception chain");

        logger.info("[TEST_PASS] Deeply nested cause chain preservation validated successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }

    /**
     * Validates that the exception handler logs the correct service name and traceability tags
     * during exception processing.
     *
     * <p>Audit Requirement: Ensures all log statements include the service name and traceability tags.</p>
     *
     * Traceability Tags: [EXC-003], [EXC-004]
     */
    @Test
    @DisplayName("handleRuntimeException should log service name and traceability tags [EXC-003][EXC-004]")
    void handleRuntimeException_ShouldLogServiceNameAndTraceabilityTags() {
        // Arrange: Create a RuntimeException with a specific message
        RuntimeException runtimeException = new RuntimeException("Test exception for logging validation");

        // Act: Capture the exception to verify logging behavior
        ContentRecommendationException thrown = assertThrows(
            ContentRecommendationException.class,
            () -> exceptionHandler.handleRuntimeException(runtimeException, mockWebRequest),
            "Expected handleRuntimeException to throw ContentRecommendationException"
        );

        // Assert: Verify that the exception was properly wrapped
        assertNotNull(thrown, "The wrapped exception should not be null");
        assertEquals(runtimeException, thrown.getCause(),
            "The original exception should be preserved as the cause");

        // Verify that the service name constant is correctly defined
        assertEquals(SERVICE_NAME, "ContentRecommendationService",
            "The service name constant should match the expected value");

        // Verify that the traceability tags constant is correctly defined
        assertEquals(TRACEABILITY_TAGS, "[EXC-003], [EXC-004]",
            "The traceability tags constant should match the expected value");

        logger.info("[TEST_PASS] Logging validation completed successfully - Traceability Tags: {}", TRACEABILITY_TAGS);
    }
}
```

# Day 1: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandlerTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandlerTest.java` (Must map to sources/backend/ or sources/frontend/)




### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandlerTest.java. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các ngoại lệ liên quan đến đề xuất nội dung.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

