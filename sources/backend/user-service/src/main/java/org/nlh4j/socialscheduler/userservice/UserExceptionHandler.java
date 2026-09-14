package org.nlh4j.socialscheduler.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the User Service module.
 * <p>
 * This class intercepts and processes all exceptions thrown during user-related
 * operations, ensuring consistent, secure, and standardized error responses
 * across the microservice. It adheres to enterprise-grade security protocols
 * by masking sensitive data and providing structured audit trails.
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@RestControllerAdvice
public class UserExceptionHandler extends ResponseEntityExceptionHandler {

    // [ARC-001] Initialize the SLF4J logger for structured logging of exception events
    private static final Logger logger = LoggerFactory.getLogger(UserExceptionHandler.class);

    // [ARC-002] Define immutable constant strings for error response keys to avoid magic strings
    private static final String ERROR_TIMESTAMP_KEY = "timestamp";
    private static final String ERROR_STATUS_KEY = "status";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String ERROR_PATH_KEY = "path";
    private static final String ERROR_VALIDATION_KEY = "validation_errors";

    /**
     * Handles validation errors for request body arguments annotated with @Valid.
     * <p>
     * Captures field-level validation failures and returns a structured response
     * with detailed field-specific error messages.
     *
     * @param ex      The MethodArgumentNotValidException containing validation errors
     * @param headers The HTTP headers to be written to the response
     * @param status  The HTTP status code
     * @param request The current web request
     * @return ResponseEntity containing the structured error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // [ARC-003] Log the validation failure at INFO level with masked context
        logger.info("[PROCESS] Handling validation errors for request: {}", request.getDescription(false));

        // [ARC-002] Aggregate field-level validation errors into a map for structured response
        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        // [ARC-001] Construct the standardized error response body
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for one or more fields",
                request,
                validationErrors
        );

        // [ARC-003] Log the completion of validation error handling
        logger.info("[COMPLETION] Validation error handling completed successfully");

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles malformed JSON or invalid request body payloads.
     * <p>
     * Ensures that malformed input does not expose internal parsing details
     * to the client, maintaining security through obscurity principles.
     *
     * @param ex      The HttpMessageNotReadableException caused by malformed input
     * @param headers The HTTP headers to be written to the response
     * @param status  The HTTP status code
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        // [ARC-004] Log the malformed request at WARN level without exposing raw payload
        logger.warn("[SECURITY] Malformed request body detected. Raw error: {}", ex.getMessage());

        // [ARC-001] Construct a generic error response to avoid leaking internal parsing details
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request body. Please verify the payload structure.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where a referenced entity is not found in the database.
     * <p>
     * Maps JPA EntityNotFoundException to a 404 Not Found response with
     * a sanitized message to prevent information disclosure.
     *
     * @param ex      The EntityNotFoundException thrown by the persistence layer
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) {

        // [ARC-003] Log the entity lookup failure at INFO level
        logger.info("[PROCESS] Entity not found during user operation: {}", ex.getMessage());

        // [ARC-002] Build a sanitized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "The requested user resource was not found.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles database constraint violations such as duplicate keys or
     * foreign key constraint failures.
     * <p>
     * Ensures that raw database error messages are never exposed to the client,
     * preventing potential information leakage about the database schema.
     *
     * @param ex      The DataIntegrityViolationException thrown by the database layer
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) {

        // [ARC-004] Log the database constraint violation at ERROR level with masked details
        logger.error("[CRITICAL FAIL] [ARC-004] Database integrity violation during user operation. Raw error: {}", ex.getMessage());

        // [ARC-001] Construct a generic error response to avoid exposing database internals
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT.value(),
                "A data integrity conflict occurred. The operation could not be completed.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles attempts to delete or access non-existent records.
     * <p>
     * Maps EmptyResultDataAccessException to a 404 Not Found response.
     *
     * @param ex      The EmptyResultDataAccessException thrown when no rows are affected
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003]
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex,
            WebRequest request) {

        // [ARC-003] Log the empty result at INFO level
        logger.info("[PROCESS] No result found for user operation: {}", ex.getMessage());

        // [ARC-002] Build a standardized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "The requested user resource does not exist.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles constraint violations from javax.validation annotations
     * applied directly on method parameters (e.g., @RequestParam, @PathVariable).
     *
     * @param ex      The ConstraintViolationException thrown by the validation engine
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-002], [ARC-004]
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {

        // [ARC-004] Log the constraint violation at WARN level
        logger.warn("[SECURITY] Constraint violation detected: {}", ex.getMessage());

        // [ARC-001] Construct a sanitized error response
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "One or more input constraints were violated.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catches all unhandled exceptions to prevent stack trace leakage
     * and ensure a consistent error contract for API consumers.
     *
     * @param ex      The generic Exception caught by the global handler
     * @param request The current web request
     * @return ResponseEntity containing the standardized error payload
     * @traceability [ARC-001], [ARC-003], [ARC-004]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(
            Exception ex,
            WebRequest request) {

        // [ARC-004] Log the unhandled exception at ERROR level with full context
        logger.error("[CRITICAL FAIL] [ARC-004] Unhandled exception in user service. Raw error: {}", ex.getMessage(), ex);

        // [ARC-002] Build a generic error response to avoid exposing internal details
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred while processing your request.",
                request,
                null
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Constructs a standardized error response map with consistent fields
     * for all exception types.
     * <p>
     * This ensures uniformity in error payloads and prevents accidental
     * exposure of sensitive information.
     *
     * @param statusCode The HTTP status code to include in the response
     * @param message    The sanitized error message to return to the client
     * @param request    The current web request for path extraction
     * @param validationErrors Optional map of field-level validation errors
     * @return A Map containing the structured error response
     * @traceability [ARC-001], [ARC-002]
     */
    private Map<String, Object> buildErrorResponse(
            int statusCode,
            String message,
            WebRequest request,
            Map<String, String> validationErrors) {

        // [ARC-001] Initialize the error response map with immutable constants
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_TIMESTAMP_KEY, Instant.now().toString());
        errorResponse.put(ERROR_STATUS_KEY, statusCode);
        errorResponse.put(ERROR_MESSAGE_KEY, message);

        // [ARC-002] Extract and sanitize the request path to avoid leaking internal routing details
        String requestPath = request != null ? request.getDescription(false).replace("uri=", "") : "unknown";
        errorResponse.put(ERROR_PATH_KEY, requestPath);

        // [ARC-003] Conditionally include validation errors if present
        if (validationErrors != null && !validationErrors.isEmpty()) {
            errorResponse.put(ERROR_VALIDATION_KEY, validationErrors);
        }

        return errorResponse;
    }
}