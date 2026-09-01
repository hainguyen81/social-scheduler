package org.nlh4j.socialscheduler.scheduleservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom enterprise exception for handling failures during interactions with external social media platforms.
 * This exception is designed to capture platform-specific error details, HTTP status codes, and retryability status
 * to facilitate robust error handling and automated recovery mechanisms in the integration layer.
 *
 * @traceability [REQ-001], [EXC-001]
 * @author Enterprise System Architect
 */
public class SocialPlatformException extends RuntimeException {

    private final String platform;
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param message    The descriptive error message.
     * @param platform   The identifier of the social platform (e.g., FACEBOOK, INSTAGRAM, TIKTOK).
     * @param errorCode  The specific error code returned by the upstream API.
     * @param httpStatus The HTTP status code associated with the failure.
     * @param retryable  Flag indicating if the operation can be retried.
     * @param cause      The underlying cause of the exception (e.g., network timeout).
     */
    public SocialPlatformException(String message, String platform, String errorCode, 
                                   HttpStatus httpStatus, boolean retryable, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    /**
     * Returns the social media platform identifier.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the upstream API error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the HTTP status code associated with the failure.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns true if the operation is eligible for automated retry.
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Provides a structured string representation for logging purposes, 
     * ensuring compliance with OWASP A09 (Logging and Monitoring Failures).
     */
    @Override
    public String toString() {
        return String.format("SocialPlatformException[platform=%s, errorCode=%s, httpStatus=%s, retryable=%b, message=%s]",
                platform, errorCode, httpStatus, retryable, getMessage());
    }
}