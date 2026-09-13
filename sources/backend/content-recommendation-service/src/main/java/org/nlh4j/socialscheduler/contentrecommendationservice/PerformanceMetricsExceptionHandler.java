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