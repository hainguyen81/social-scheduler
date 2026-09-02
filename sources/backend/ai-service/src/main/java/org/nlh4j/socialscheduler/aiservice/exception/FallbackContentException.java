package org.nlh4j.socialscheduler.aiservice.exception;

/**
 * Custom exception thrown when fallback content generation fails.
 * 
 * @traceability [REQ-002], [EXC-004]
 */
public class FallbackContentException extends RuntimeException {
    private static final String ERROR_CODE = "FALLBACK_CONTENT_FAILED";

    public FallbackContentException() {
        super(ERROR_CODE);
    }

    public FallbackContentException(String message) {
        super(message);
    }

    public FallbackContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public FallbackContentException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the error code associated with this exception.
     * 
     * @return error code constant "FALLBACK_CONTENT_FAILED"
     */
    public String getErrorCode() {
        return ERROR_CODE;
    }
}