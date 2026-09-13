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