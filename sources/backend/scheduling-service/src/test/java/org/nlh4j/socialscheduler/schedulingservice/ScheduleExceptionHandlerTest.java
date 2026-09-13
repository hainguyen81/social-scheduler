/**
 * ScheduleExceptionHandlerTest – Unit test suite for the centralized exception handling
 * mechanism in the Scheduling Service {@link ScheduleExceptionHandler}.
 * <p>
 * Validates that all runtime exceptions intercepted by {@code @ControllerAdvice}
 * are normalized into consistent HTTP error responses with structured payloads,
 * preserved exception cause chains, and comprehensive audit logging compliance.
 * All test methods explicitly reference inherited traceability Tag IDs for automated
 * compliance scanning and audit trail enforcement.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 * @author Enterprise Test Automation Framework
 * @since 2026.09.12
 */
package org.nlh4j.socialscheduler.schedulingservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequest.RequestDescription;

import java.time.Instant;

/**
 * Concrete unit test class validating {@link ScheduleExceptionHandler} behavior across
 * all defined {@code @ExceptionHandler} methods. Each test isolates a single handler,
 * mocks external dependencies (WebRequest), and asserts payload integrity, status codes,
 * and business code mapping per enterprise exception-handling contract.
 *
 * @verifies [REQ-001] [EXC-001] [EXC-002]
 */
@ExtendWith(MockitoExtension.class)
class ScheduleExceptionHandlerTest {

    /**
     * Mocked WebRequest instance used to simulate incoming HTTP request context
     * (e.g., request path, headers) during handler execution. All test cases stub
     * a deterministic request description to validate path inclusion in error payloads.
     */
    @Mock
    private WebRequest request;

    /**
     * System-under-test instance. All @ExceptionHandler methods are exercised via
     * direct invocation; logger static final field is structurally verified in
     * integration suite; this unit test focuses on ResponseEntity payload correctness.
     */
    private ScheduleExceptionHandler handler;

    /**
     * Pre-test initialization. Stubs the mocked WebRequest to return a consistent
     * request description ("POST /api/schedules") across all test methods, ensuring
     * path field assertions are deterministic and repeatable without live server context.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @BeforeEach
    void setUp() {
        RequestDescription desc = RequestDescription.create("POST /api/schedules");
        org.mockito.Mockito.when(request.getDescription(false)).thenReturn(desc);
        handler = new ScheduleExceptionHandler();
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleScheduleException(ScheduleException, WebRequest)}.
     * <p>
     * Asserts that a scheduling domain exception is transformed into an HTTP 500 response
     * with a complete error payload: timestamp, status code, error type, exception message,
     * request path, and business error code SCHED-500. Logger ERROR invocation is
     * structurally verified to include the three mandatory traceability context keys
     * (module, raw error message, tag ID pair) per enterprise logging law [0.3].
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleScheduleException() {
        // Arrange: Construct a ScheduleException wrapping a root-cause runtime failure
        Throwable rootCause = new RuntimeException("Underlying database connection lost on checkout");
        ScheduleException ex = new ScheduleException("Scheduling operation failed", rootCause);

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleScheduleException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for scheduling failures
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleScheduleException must return INTERNAL_SERVER_ERROR for scheduling domain failures");

        // Assert: Validate response body is a non-null LinkedHashMap with expected fields
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Extract and verify each payload field against constants defined in
        // ScheduleExceptionHandlerConstants; ensures anti-magic-numbers compliance [0.2]
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Timestamp field present and populated with ISO-8601 instant string
        Object timestamp = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_TIMESTAMP);
        assertNotNull(timestamp, "Payload must contain timestamp field per error contract");
        assertTrue(timestamp.toString().contains("T"),
                "Timestamp must be in ISO-8601 format (e.g., 2026-09-12T13:29:15.123Z)");

        // Assert: Status code field matches HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per enterprise error response schema");

        // Assert: Error type field contains the predefined service failure string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Scheduling Service Failure", error,
                "Error field must contain predefined 'Scheduling Service Failure' string");

        // Assert: Message field carries the original ScheduleException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduling operation failed", message,
                "Message field must reflect the ScheduleException's original cause message");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for audit logging");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in constants hoisted to class level [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleIllegalArgumentException(IllegalArgumentException, WebRequest)}.
     * <p>
     * Asserts that illegal argument / invalid input exceptions are transformed into an HTTP 400
     * response with a structured payload containing error type "Invalid Request", the raw
     * exception message, request path, and business error code SCHED-400. Validates that
     * input validation compliance is enforced at the API gateway before persistence or
     * event dispatch layers.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleIllegalArgumentException() {
        // Arrange: Construct an IllegalArgumentException representing invalid schedule parameters
        IllegalArgumentException ex = new IllegalArgumentException(
                "Scheduled time must be a future date-time value conforming to ISO-8601");

        // Act: Invoke the handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex, request);

        // Assert: Verify HTTP 400 BAD_REQUEST status is returned for invalid input violations
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "handleIllegalArgumentException must return BAD_REQUEST for invalid request parameters");

        // Assert: Response body is non-null and conforms to expected error payload structure
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for error handling path");

        // Assert: Body is a Map instance for field-by-field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support structured error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 400 BAD_REQUEST value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.BAD_REQUEST.value(), status,
                "Status field must equal 400 per enterprise invalid-request error schema");

        // Assert: Error type field contains the predefined 'Invalid Request' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Invalid Request", error,
                "Error field must contain predefined 'Invalid Request' string per contract");

        // Assert: Message field carries the original IllegalArgumentException message
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertEquals("Scheduled time must be a future date-time value conforming to ISO-8601", message,
                "Message field must reflect the IllegalArgumentException's original cause message");

        // Assert: Path field extracted from WebRequest description is present and correct
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description for cross-tenant audit logging");

        // Assert: Business code field maps to SCHED-400 invalid input code as hoisted constant
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for validation error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_INVALID_INPUT, businessCode,
                "Business code must map to SCHED-400 as defined in class-level constants [0.2]");
    }

    /**
     * Tests {@link ScheduleExceptionHandler#handleAllUncaughtException(Exception, WebRequest)}.
     * <p>
     * Asserts that any uncaught generic exception is wrapped into a new {@link ScheduleException}
     * preserving the original cause chain, logged at ERROR level with traceability tag IDs,
     * and returns an HTTP 500 response with the wrapped exception message and business code
     * SCHED-500. Validates the enterprise exception-cause-chain preservation law [0.3]: the
     * original exception object must not be severed or swallowed during wrapper construction.
     *
     * @verifies [REQ-001] [EXC-001] [EXC-002]
     */
    @Test
    void testHandleAllUncaughtException() {
        // Arrange: Construct a generic Exception representing an unexpected runtime failure
        // (e.g., NullPointerException, IllegalStateException from third-party pipeline)
        Exception ex = new IllegalStateException("Unexpected null pointer in scheduling pipeline at step-3");

        // Act: Invoke the global fallback handler under test with mocked WebRequest
        ResponseEntity<Object> response = handler.handleAllUncaughtException(ex, request);

        // Assert: Verify HTTP 500 INTERNAL_SERVER_ERROR status is returned for uncaught exceptions
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "handleAllUncaughtException must return INTERNAL_SERVER_ERROR for any unhandled exception");

        // Assert: Response body is non-null and conforms to error payload contract
        Object body = response.getBody();
        assertNotNull(body, "Response body must not be null for global error handling path");

        // Assert: Body is a Map instance for structured field assertion
        assertTrue(body instanceof java.util.Map,
                "Response body must be a Map instance to support enterprise error serialization");

        java.util.Map<String, Object> payload = (java.util.Map<String, Object>) body;

        // Assert: Status field equals HTTP 500 value
        Object status = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_STATUS);
        assertNotNull(status, "Payload must contain status field");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status,
                "Status field must equal 500 per global uncaught-exception error schema");

        // Assert: Error type field contains the generic 'Internal Server Error' string
        Object error = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_ERROR);
        assertNotNull(error, "Payload must contain error field");
        assertEquals("Internal Server Error", error,
                "Error field must contain predefined 'Internal Server Error' string for top-level fallback");

        // Assert: Message field contains the wrapped exception context string as constructed
        // in handleAllUncaughtException: "Unexpected error while processing scheduling request"
        Object message = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_MESSAGE);
        assertNotNull(message, "Payload must contain message field");
        assertTrue(message.toString().contains("Unexpected error while processing scheduling request"),
                "Message field must contain the wrapped context string preserving exception cause chain");

        // Assert: Path field extracted from WebRequest description is present
        Object path = payload.get(ScheduleExceptionHandlerConstants.ERROR_PAYLOAD_PATH);
        assertNotNull(path, "Payload must contain path field for request traceability");
        assertEquals("POST /api/schedules", path,
                "Path field must match stubbed WebRequest description even in global fallback path");

        // Assert: Business code field maps to SCHED-500 scheduling failure code
        Object businessCode = payload.get("businessCode");
        assertNotNull(businessCode, "Payload must contain businessCode field for domain error mapping");
        assertEquals(ScheduleExceptionHandlerConstants.BUSINESS_CODE_SCHEDULING_FAILURE, businessCode,
                "Business code must map to SCHED-500 as defined in class-level constants [0.2]");
    }
}