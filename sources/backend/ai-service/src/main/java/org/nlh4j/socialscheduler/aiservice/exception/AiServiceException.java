package org.nlh4j.socialscheduler.aiservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for AI service unavailability.
 * <p>
 * This exception is thrown when the AI service (e.g., OpenAI API) fails to generate content,
 * triggering the fallback mechanism. It encapsulates the original cause, platform context,
 * and HTTP status for structured error handling.
 * </p>
 * 
 * @traceability [REQ-002], [EXC-004]
 */
public class AiServiceException extends RuntimeException {

    /** Error code constant for AI service unavailability. */
    private static final String ERROR_CODE = "AI_SERVICE_UNAVAILABLE";

    /** The error code associated with this exception. */
    private final String errorCode;

    /** The platform (e.g., FACEBOOK, INSTAGRAM, TIKTOK) for which the AI service failed. */
    private final String platform;

    /** The original cause of the exception (e.g., OpenAI API exception). */
    private final Throwable originalCause;

    /** The HTTP status to be returned to the client (e.g., 503 Service Unavailable). */
    private final HttpStatus httpStatus;

    /**
     * Constructs a new AiServiceException with the specified message and cause.
     * <p>
     * This constructor initializes the exception with a default error code,
     * null platform, and HTTP 503 Service Unavailable status.
     * </p>
     * 
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public AiServiceException(String message, Throwable cause) {
        this(message, null, cause, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Constructs a new AiServiceException with the specified details.
     * 
     * @param message   the detail message
     * @param platform  the platform for which the AI service failed (can be null)
     * @param cause     the original cause of the exception
     * @param httpStatus the HTTP status to be returned to the client
     */
    public AiServiceException(String message, String platform, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = ERROR_CODE;
        this.platform = platform;
        this.originalCause = cause;
        this.httpStatus = httpStatus;
    }

    /**
     * Returns the error code associated with this exception.
     * 
     * @return the error code (always "AI_SERVICE_UNAVAILABLE")
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the platform for which the AI service failed.
     * 
     * @return the platform (may be null if not specified)
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the original cause of the exception.
     * 
     * @return the original cause (a Throwable)
     */
    public Throwable getOriginalCause() {
        return originalCause;
    }

    /**
     * Returns the HTTP status to be returned to the client.
     * 
     * @return the HTTP status (e.g., HttpStatus.SERVICE_UNAVAILABLE)
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}