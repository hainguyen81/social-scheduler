package org.nlh4j.socialscheduler.ratelimitservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Custom enterprise exception thrown when a client exceeds the allowed rate limit
 * threshold on a specific endpoint within the social-scheduler ecosystem.
 * Automatically mapped to HTTP status 429 Too Many Requests via @ResponseStatus.
 * 
 * @traceability [EXC-005]
 * @since 1.0.0
 */
@Slf4j
@Getter
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    // [REQ-003], [EXC-005] Top-of-class immutable serial version UID constant declaration
    @Serial
    private static final long serialVersionUID = 1L;

    // [REQ-003], [EXC-005] Top-of-class constant template for error message formatting
    private static final String ERROR_MESSAGE_TEMPLATE = "Rate limit exceeded for userId={0} endpoint={1} retryAfterSeconds={2}";

    // [REQ-003], [EXC-005] Immutable fields capturing precise request context for auditing and handling
    private final UUID userId;
    private final String endpoint;
    private final long retryAfterSeconds;
    private final OffsetDateTime timestamp;

    /**
     * Constructs a new RateLimitExceededException with the specified user identifier,
     * target endpoint, and retry-after duration. Automatically logs a WARN-level trace
     * for security monitoring and abuse detection (OWASP A09).
     *
     * @param userId            the unique identifier of the user who exceeded the limit [EXC-005]
     * @param endpoint          the target API endpoint path being accessed [EXC-005]
     * @param retryAfterSeconds the number of seconds the client must wait before retrying [EXC-005]
     */
    public RateLimitExceededException(UUID userId, String endpoint, long retryAfterSeconds) {
        super(MessageFormat.format(ERROR_MESSAGE_TEMPLATE, userId, endpoint, retryAfterSeconds));
        this.userId = userId;
        this.endpoint = endpoint;
        this.retryAfterSeconds = retryAfterSeconds;
        this.timestamp = OffsetDateTime.now();

        // [0.3] Process & Business Flow Logging: Emit explicit WARN log with 3 context keys
        // Module Subsystem: RateLimitSubsystem, Raw Error Message, and Tracking Tag ID [EXC-005]
        log.warn("[SECURITY_WARN] [RateLimitSubsystem] [EXC-005] Rate limit threshold breached. " +
                "User ID: {}, Endpoint: {}, Retry-After: {} seconds. Timestamp: {}",
                this.userId, this.endpoint, this.retryAfterSeconds, this.timestamp);
    }

    /**
     * Constructs a new RateLimitExceededException with the specified user identifier,
     * target endpoint, retry-after duration, and a root cause exception (exception cause chaining preservation).
     *
     * @param userId            the unique identifier of the user who exceeded the limit [EXC-005]
     * @param endpoint          the target API endpoint path being accessed [EXC-005]
     * @param retryAfterSeconds the number of seconds the client must wait before retrying [EXC-005]
     * @param cause             the underlying root cause exception (e.g., Redis Lettuce connection drop) [EXC-005]
     */
    public RateLimitExceededException(UUID userId, String endpoint, long retryAfterSeconds, Throwable cause) {
        super(MessageFormat.format(ERROR_MESSAGE_TEMPLATE, userId, endpoint, retryAfterSeconds), cause);
        this.userId = userId;
        this.endpoint = endpoint;
        this.retryAfterSeconds = retryAfterSeconds;
        this.timestamp = OffsetDateTime.now();

        // [0.3], [0.5] Comprehensive Exception Logging and Cause Chain Preservation
        log.error("[CRITICAL FAIL] [RateLimitSubsystem] [EXC-005] Rate limit evaluation failed due to upstream infrastructure fault. " +
                "User ID: {}, Endpoint: {}, Retry-After: {}s. Raw error: {}",
                this.userId, this.endpoint, this.retryAfterSeconds, cause.getMessage(), cause);
    }

    /**
     * Overrides the standard getMessage method to construct a localized, internationalization-ready
     * descriptive string detailing the exact rate limit breach metrics.
     *
     * @return structured error message string [EXC-005]
     */
    @Override
    public String getMessage() {
        // [REQ-003], [EXC-005] Dynamic message construction utilizing MessageFormat
        return MessageFormat.format(ERROR_MESSAGE_TEMPLATE, this.userId, this.endpoint, this.retryAfterSeconds);
    }
}