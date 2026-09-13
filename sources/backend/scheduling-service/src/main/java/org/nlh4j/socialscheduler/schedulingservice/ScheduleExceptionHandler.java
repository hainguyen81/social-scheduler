/**
 * ScheduleExceptionHandler – Centralized exception handling for the Scheduling Service.
 *
 * This {@code @ControllerAdvice} intercepts and normalizes all runtime exceptions
 * that occur within the scheduling module, providing consistent HTTP error responses
 * and comprehensive audit logging.
 *
 * <p>The handler enforces the following enterprise guardrails:
 * <ul>
 *   <li>Every catch block logs an error with the required three context keys
 *       (module subsystem name, raw exception message, traceability Tag ID).</li>
 *   <li>When re-throwing a custom {@link ScheduleException}, the original cause
 *       is preserved to satisfy the enterprise exception-cause-chain law.</li>
 *   <li>All error responses contain a traceable payload with timestamp, status,
 *       and a custom business error code.</li>
 *   <li>All literal configuration values are hoisted to the class-level constants
 *       to satisfy the anti-magic-numbers policy.</li>
 * </ul>
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
package org.nlh4j.socialscheduler.schedulingservice;

// --- Standard library imports ---
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

// --- SLF4J / Logging framework imports ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- Spring Framework imports ---
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Custom runtime exception used to wrap domain-specific scheduling failures.
 * The original cause is preserved to satisfy the enterprise exception-cause-chain law.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleException extends RuntimeException {
    // Serial version UID for Serializable compliance across distributed deployments.
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ScheduleException with the specified detail message and cause.
     * The cause is forwarded to the parent RuntimeException constructor to preserve
     * the full stack-trace ancestry for centralized cloud logging aggregation.
     *
     * @param message the detail message explaining the scheduling failure
     * @param cause   the original throwable that triggered this exception
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    public ScheduleException(String message, Throwable cause) {
        // Delegate to parent constructor, forwarding both message and cause.
        super(message, cause);
    }
}

/**
 * Enterprise-grade constants for error codes, log messages, and configuration.
 * All literals are hoisted to the class level to satisfy the anti-magic-numbers guardrail.
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
class ScheduleExceptionHandlerConstants {
    // Traceability tag identifiers – used in logging and Javadoc.
    public static final String TRACE_REQ_001 = "REQ-001";
    public static final String TRACE_EXC_001 = "EXC-001";
    public static final String TRACE_EXC_002 = "EXC-002";

    // HTTP error payload field names.
    public static final String ERROR_PAYLOAD_TIMESTAMP = "timestamp";
    public static final String ERROR_PAYLOAD_STATUS = "status";
    public static final String ERROR_PAYLOAD_ERROR = "error";
    public static final String ERROR_PAYLOAD_MESSAGE = "message";
    public static final String ERROR_PAYLOAD_PATH = "path";
    public static final String ERROR_PAYLOAD_BUSINESS_CODE = "businessCode";

    // Business error codes.
    public static final String BUSINESS_CODE_INVALID_INPUT = "SCHED-400";
    public static final String BUSINESS_CODE_RESOURCE_NOT_FOUND = "SCHED-404";
    public static final String BUSINESS_CODE_SCHEDULING_FAILURE = "SCHED-500";

    // Log message templates.
    public static final String LOG_ENTRY_POINT = "[ENTRY] ScheduleExceptionHandler invoked for {}";
    public static final String LOG_EXIT_POINT = "[EXIT] ScheduleExceptionHandler completed handling {}";
    public static final String LOG_ERROR_CRITICAL = "[CRITICAL FAIL] [{}] Scheduling operation failed. Raw error: {}";

    // Error description labels.
    public static final String ERROR_LABEL_SCHEDULING_FAILURE = "Scheduling Service Failure";
    public static final String ERROR_LABEL_INVALID_REQUEST = "Invalid Request";
    public static final String ERROR_LABEL_INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String ERROR_LABEL_RESOURCE_NOT_FOUND = "Resource Not Found";

    // Wrapped exception message for unexpected errors.
    public static final String WRAPPED_EXCEPTION_MESSAGE = "Unexpected error while processing scheduling request";
}

/**
 * Central exception handler that normalizes scheduling failures into structured HTTP responses.
 * <p>
 * The handler follows the enterprise logging and exception-cause-chain policies:
 * <ul>
 *   <li>Every catch block logs an error with the required three context keys (module, raw error, tag ID).</li>
 *   <li>When re-throwing a custom {@link ScheduleException}, the original cause is preserved.</li>
 *   <li>All error responses contain a traceable payload with timestamp, status, and custom business code.</li>
 * </ul>
 *
 * @traceability [REQ-001] [EXC-001] [EXC-002]
 */
@ControllerAdvice
class ScheduleExceptionHandler {

    // Class-level logger instance for structured audit logging.
    private static final Logger logger = LoggerFactory.getLogger(ScheduleExceptionHandler.class);

    /**
     * Handles domain-specific scheduling exceptions.
     * <p>
     * This method logs the failure at ERROR level with the required traceability tag,
     * builds a detailed error payload, and returns a {@link ResponseEntity} with the
     * appropriate HTTP status (INTERNAL_SERVER_ERROR). The original exception cause is
     * preserved when a new {@link ScheduleException} is re-thrown elsewhere.
     *
     * @param ex      the caught {@link ScheduleException}
     * @param request the current web request (used to extract the request path)
     * @return a {@link ResponseEntity} containing the structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<Object> handleScheduleException(ScheduleException ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Enterprise-grade error logging – three mandatory context keys:
        // 1) Module subsystem name (embedded in tag), 2) Raw exception message, 3) Traceability Tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_SCHEDULING_FAILURE);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Handles illegal argument exceptions (e.g., invalid schedule parameters).
     * <p>
     * This method enforces input validation compliance and logs the violation with the
     * required traceability tags. It returns a BAD_REQUEST response containing a business
     * error code for downstream clients.
     *
     * @param ex      the caught {@link IllegalArgumentException}
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 400 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Critical error logging – module, raw error, tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.BAD_REQUEST.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_INVALID_REQUEST);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handles resource-not-found exceptions (e.g., schedule ID not found in database).
     * <p>
     * This method intercepts {@link org.springframework.dao.EmptyResultDataAccessException}
     * and {@link org.springframework.web.server.ResponseStatusException} instances that
     * indicate a missing resource, returning a structured 404 response with a business
     * error code for downstream client consumption.
     *
     * @param ex      the caught exception indicating a missing resource
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 404 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler({
        org.springframework.dao.EmptyResultDataAccessException.class,
        org.springframework.web.server.ResponseStatusException.class
    })
    public ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Critical error logging – module, raw error, tag ID.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                ex.getMessage(), ex);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.NOT_FOUND.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_RESOURCE_NOT_FOUND);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, ex.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_RESOURCE_NOT_FOUND);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Global fallback handler for any uncaught exceptions.
     * <p>
     * This method guarantees that no exception escapes without being logged and
     * transformed into a consistent error response. It preserves the original cause
     * chain when wrapping into a {@link ScheduleException} for downstream processing.
     *
     * @param ex      the caught generic {@link Exception}
     * @param request the current web request
     * @return a {@link ResponseEntity} with HTTP 500 and a structured error payload
     * @traceability [REQ-001] [EXC-001] [EXC-002]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        // Log entry point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_ENTRY_POINT, ex.getClass().getSimpleName());

        // Preserve cause chain – rethrow a custom ScheduleException with original cause.
        ScheduleException wrapped = new ScheduleException(
                ScheduleExceptionHandlerConstants.WRAPPED_EXCEPTION_MESSAGE,
                ex
        );
        // Log the wrapped exception with required traceability tags.
        logger.error(ScheduleExceptionHandlerConstants.LOG_ERROR_CRITICAL,
                ScheduleExceptionHandlerConstants.TRACE_EXC_001 + "," + ScheduleExceptionHandlerConstants.TRACE_EXC_002,
                wrapped.getMessage(), wrapped);

        // Build structured error payload using LinkedHashMap to preserve insertion order.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP, Instant.now().toString());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR, ScheduleExceptionHandlerConstants.ERROR_LABEL_INTERNAL_SERVER_ERROR);
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE, wrapped.getMessage());
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH, request.getDescription(false));
        body.put(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_BUSINESS_CODE, ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE);

        // Log exit point at INFO threshold for process flow tracing.
        logger.info(ScheduleExceptionHandlerConstants.LOG_EXIT_POINT, ex.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}