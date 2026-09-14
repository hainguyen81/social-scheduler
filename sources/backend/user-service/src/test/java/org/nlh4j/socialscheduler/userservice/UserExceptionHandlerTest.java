package org.nlh4j.socialscheduler.userservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserExceptionHandler} ensuring all exception handling paths
 * comply with enterprise security and logging standards.
 *
 * @verifies [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@ExtendWith(MockitoExtension.class)
class UserExceptionHandlerTest {

    @Mock
    private UserExceptionHandler userExceptionHandler;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private WebRequest webRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private HttpMessageNotReadableException httpMessageNotReadableException;

    @Mock
    private EntityNotFoundException entityNotFoundException;

    @Mock
    private DataIntegrityViolationException dataIntegrityViolationException;

    @Mock
    private EmptyResultDataAccessException emptyResultDataAccessException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private Exception globalException;

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of validation errors [ARC-001][ARC-003]")
    void testHandleMethodArgumentNotValid() throws Exception {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult())
                .thenReturn(mock(javax.validation.ValidationResult.class)); // placeholder for field errors
        // Simulate field errors via reflection on BindingResult
        var bindingResult = mock(javax.validation.ValidationResult.class);
        var fieldErrors = List.of(
                mockFieldError("username", "must not be blank"),
                mockFieldError("email", "must have valid format")
        );
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<Object> response = invokeHandleMethodArgumentNotValid(
                methodArgumentNotValidException,
                httpHeaders,
                HttpStatus.BAD_REQUEST,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body.get("timestamp"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals("Validation failed for one or more fields", body.get("message"));
        assertNotNull(body.get("validation_errors"));
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) body.get("validation_errors");
        assertEquals("must not be blank", validationErrors.get("username"));
        assertEquals("must have valid format", validationErrors.get("email"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of malformed JSON [ARC-002][ARC-004]")
    void testHandleHttpMessageNotReadable() throws Exception {
        // Arrange
        when(httpMessageNotReadableException.getMessage()).thenReturn("Invalid JSON");

        // Act
        ResponseEntity<Object> response = invokeHandleHttpMessageNotReadable(
                httpMessageNotReadableException,
                httpHeaders,
                HttpStatus.BAD_REQUEST,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Malformed JSON request body. Please verify the payload structure.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of entity not found [ARC-001][ARC-003]")
    void testHandleEntityNotFoundException() throws Exception {
        // Arrange
        when(entityNotFoundException.getMessage()).thenReturn("User 123 not found");

        // Act
        ResponseEntity<Object> response = invokeHandleEntityNotFoundException(
                entityNotFoundException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("The requested user resource was not found.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of data integrity violation [ARC-002][ARC-004]")
    void testHandleDataIntegrityViolationException() throws Exception {
        // Arrange
        when(dataIntegrityViolationException.getMessage()).thenReturn("Duplicate key");

        // Act
        ResponseEntity<Object> response = invokeHandleDataIntegrityViolationException(
                dataIntegrityViolationException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("A data integrity conflict occurred. The operation could not be completed.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of empty result access [ARC-001][ARC-003]")
    void testHandleEmptyResultDataAccessException() throws Exception {
        // Arrange
        when(emptyResultDataAccessException.getMessage()).thenReturn("No user found");

        // Act
        ResponseEntity<Object> response = invokeHandleEmptyResultDataAccessException(
                emptyResultDataAccessException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("The requested user resource does not exist.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of constraint violation [ARC-002][ARC-004]")
    void testHandleConstraintViolationException() throws Exception {
        // Arrange
        when(constraintViolationException.getMessage()).thenReturn("Invalid email format");

        // Act
        ResponseEntity<Object> response = invokeHandleConstraintViolationException(
                constraintViolationException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("One or more input constraints were violated.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test handling of global exception [ARC-001][ARC-003][ARC-004]")
    void testHandleGlobalException() throws Exception {
        // Arrange
        when(globalException.getMessage()).thenReturn("Unexpected error");

        // Act
        ResponseEntity<Object> response = invokeHandleGlobalException(
                globalException,
                webRequest
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("An unexpected error occurred while processing your request.", body.get("message"));
        assertNull(body.get("validation_errors"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Test buildErrorResponse includes required fields [ARC-001][ARC-002]")
    void testBuildErrorResponse() throws Exception {
        // Arrange
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("field1", "Invalid value");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/users");

        // Act
        Map<String, Object> response = invokeBuildErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                webRequest,
                validationErrors
        );

        // Assert
        assertNotNull(response.get("timestamp"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.get("status"));
        assertEquals("Validation failed", response.get("message"));
        assertEquals("/api/users", response.get("path"));
        @SuppressWarnings("unchecked")
        Map<String, String> validationMap = (Map<String, String>) response.get("validation_errors");
        assertEquals("Invalid value", validationMap.get("field1"));
    }

    // Helper methods using reflection to access protected methods
    private ResponseEntity<Object> invokeHandleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleMethodArgumentNotValid", MethodArgumentNotValidException.class, HttpHeaders.class, HttpStatus.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, headers, status, request);
    }

    private ResponseEntity<Object> invokeHandleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleHttpMessageNotReadable", HttpMessageNotReadableException.class, HttpHeaders.class, HttpStatus.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, headers, status, request);
    }

    private ResponseEntity<Object> invokeHandleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleEntityNotFoundException", EntityNotFoundException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleDataIntegrityViolationException", DataIntegrityViolationException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleEmptyResultDataAccessException", EmptyResultDataAccessException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleConstraintViolationException", ConstraintViolationException.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private ResponseEntity<Object> invokeHandleGlobalException(
            Exception ex,
            WebRequest request) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("handleGlobalException", Exception.class, WebRequest.class);
        method.setAccessible(true);
        return (ResponseEntity<Object>) method.invoke(userExceptionHandler, ex, request);
    }

    private Map<String, Object> invokeBuildErrorResponse(
            int statusCode,
            String message,
            WebRequest request,
            Map<String, String> validationErrors) throws Exception {

        java.lang.reflect.Method method = UserExceptionHandler.class
                .getDeclaredMethod("buildErrorResponse", int.class, String.class, WebRequest.class, Map.class);
        method.setAccessible(true);
        return (Map<String, Object>) method.invoke(userExceptionHandler, statusCode, message, request, validationErrors);
    }

    private FieldError mockFieldError(String field, String message) {
        FieldError fe = mock(FieldError.class);
        when(fe.getField()).thenReturn(field);
        when(fe.getDefaultMessage()).thenReturn(message);
        return fe;
    }
}