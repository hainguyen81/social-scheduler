package org.nlh4j.socialscheduler.exception;

import java.util.Objects;

import org.nlh4j.socialscheduler.common.Platform;
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

    /** */
	private static final long serialVersionUID = 1L;
	private final Platform platform;
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param message    The descriptive error message.
     */
    public SocialPlatformException(String message) {
        super(message);
        this.platform = Platform.GENERAL;
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
    }

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param message    The descriptive error message.
     * @param cause      The underlying cause of the exception (e.g., network timeout).
     */
    public SocialPlatformException(String message, Throwable cause) {
        super(message, cause);
        this.platform = Platform.GENERAL;
        this.errorCode = "UNKNOWN_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.retryable = false;
    }

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
    public SocialPlatformException(String message, Platform platform, String errorCode, 
                                   HttpStatus httpStatus, boolean retryable, Throwable cause) {
        super(message, cause);
        this.platform = Objects.requireNonNullElse(platform, Platform.GENERAL);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param message    The descriptive error message.
     * @param platform   The identifier of the social platform (e.g., FACEBOOK, INSTAGRAM, TIKTOK).
     * @param errorCode  The specific error code returned by the upstream API.
     * @param httpStatus The HTTP status code associated with the failure.
     * @param retryable  Flag indicating if the operation can be retried.
     */
    public SocialPlatformException(String message, Platform platform, String errorCode, 
                                   HttpStatus httpStatus, boolean retryable) {
        super(message);
        this.platform = Objects.requireNonNullElse(platform, Platform.GENERAL);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param platform   The identifier of the social platform (e.g., FACEBOOK, INSTAGRAM, TIKTOK).
     * @param errorCode  The specific error code returned by the upstream API.
     * @param httpStatus The HTTP status code associated with the failure.
     * @param retryable  Flag indicating if the operation can be retried.
     */
    public SocialPlatformException(Platform platform, String errorCode, 
                                   HttpStatus httpStatus, boolean retryable) {
        super();
        this.platform = Objects.requireNonNullElse(platform, Platform.GENERAL);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    /**
     * Returns the social media platform identifier.
     */
    public Platform getPlatform() {
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