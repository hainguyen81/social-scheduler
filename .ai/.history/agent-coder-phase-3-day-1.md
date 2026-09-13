# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai dịch vụ đề xuất nội dung bằng AI để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[EXC-003]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Triển khai bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để xử lý các ngoại lệ liên quan đến đề xuất nội dung.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

