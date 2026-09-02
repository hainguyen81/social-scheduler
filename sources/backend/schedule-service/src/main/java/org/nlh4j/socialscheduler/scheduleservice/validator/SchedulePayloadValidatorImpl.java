package org.nlh4j.socialscheduler.scheduleservice.validator;

// [REQ-003] Business validation requirement for scheduling time window and payload safety.
// [EXC-002] Defensive exception handling, correlation tracking, and validation failure auditing.

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION - CODER AGENT IMPLEMENTATION
 * 
 * <p>Production-ready implementation of {@link ConstraintValidator} for validating 
 * {@link ScheduleRequestDto} payloads against complex business rules, Server-Side Request 
 * Forgery (SSRF) media URL whitelists, Cross-Site Scripting (XSS) injection vectors, 
 * and platform-specific content length boundaries.</p>
 * 
 * @traceability [REQ-003], [EXC-002]
 * @author Enterprise System Architect (SA Agent Pipeline)
 * @since 1.0.0
 */
@Component
public class SchedulePayloadValidatorImpl implements ConstraintValidator<ValidSchedulePayload, ScheduleRequestDto> {

    // [0.2] TOP-OF-CLASS CONSTANTS DECLARATION LAW: Immutable configuration handles & regex patterns.
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulePayloadValidatorImpl.class);
    
    // [REQ-003] Maximum allowed scheduling window in days from current timestamp.
    private static final long MAX_SCHEDULING_DAYS_WINDOW = 90L;
    
    // [REQ-003] Maximum allowed content length for TikTok platform publications.
    private static final int TIKTOK_MAX_CONTENT_LENGTH = 2200;
    
    // [REQ-003] Target social media platform identifiers.
    private static final String PLATFORM_TIKTOK = "TIKTOK";
    
    // [REQ-003] OWASP A03 / XSS dangerous pattern regex matching malicious injection payloads.
    private static final Pattern XSS_DANGEROUS_PATTERN = Pattern.compile(
            "(?i)(<script\\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>|javascript:|data:text/html|vbscript:|onload=|onerror=)",
            Pattern.CASE_INSENSITIVE
    );

    // [0.2] Configuration injection for SSRF media domain whitelist.
    @Value("${app.security.media-url-whitelist:cdn.socialscheduler.com,s3.socialscheduler.com,storage.googleapis.com/social-scheduler-prod}")
    private String mediaUrlWhitelistConfig;

    /**
     * Initializes the constraint validator.
     * 
     * @param constraintAnnotation the annotation instance for a given constraint declaration
     * @traceability [REQ-003]
     */
    @Override
    public void initialize(ValidSchedulePayload constraintAnnotation) {
        LOGGER.info("[PROCESS] [REQ-003] Initializing SchedulePayloadValidatorImpl with strict security checks.");
    }

    /**
     * Evaluates a given constraint against the incoming {@link ScheduleRequestDto} payload.
     * 
     * <p>Enforces rigorous validation logic:</p>
     * <ul>
     *   <li>Verifies that the scheduled timestamp does not exceed 90 days in the future ([REQ-003]).</li>
     *   <li>Inspects media URLs against the configured whitelist to mitigate SSRF vectors ([REQ-003]).</li>
     *   <li>Scans content strings for malicious XSS/injection patterns per OWASP A03 ([REQ-003]).</li>
     *   <li>Enforces platform-specific constraints (e.g., TikTok maximum 2200 characters) ([REQ-003]).</li>
     * </ul>
     * 
     * @param value the object to validate
     * @param context context in which the constraint is evaluated
     * @return true if the payload is valid, false or violation triggered otherwise
     * @traceability [REQ-003], [EXC-002]
     */
    @Override
    public boolean isValid(ScheduleRequestDto value, ConstraintValidatorContext context) {
        String correlationId = UUID.randomUUID().toString();
        LOGGER.info("[PROCESS] [REQ-003] [Correlation ID: {}] Starting schedule payload validation.", correlationId);

        // Guard clause for null payload objects
        if (value == null) {
            LOGGER.warn("[WARN] [EXC-002] [Correlation ID: {}] SchedulePayloadValidator received a null request payload.", correlationId);
            buildConstraintViolation(context, "Schedule request payload cannot be null.");
            return false;
        }

        try {
            // 1. Validate scheduling time window (Max 90 days in future) per [REQ-003]
            if (value.scheduledTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                long daysBetween = ChronoUnit.DAYS.between(now, value.scheduledTime());
                if (value.scheduledTime().isBefore(now) || daysBetween > MAX_SCHEDULING_DAYS_WINDOW) {
                    LOGGER.warn("[WARN] [EXC-002] [Correlation ID: {}] Scheduled time invalid: {}. Must be between now and {} days.",
                            correlationId, value.scheduledTime(), MAX_SCHEDULING_DAYS_WINDOW);
                    buildConstraintViolation(context, "Scheduled time must be in the future and within " + MAX_SCHEDULING_DAYS_WINDOW + " days.");
                    return false;
                }
            }

            // 2. Validate media URLs against SSRF whitelist per [REQ-003]
            if (value.mediaUrls() != null && !value.mediaUrls().isEmpty()) {
                List<String> whitelistDomains = Arrays.asList(mediaUrlWhitelistConfig.split(","));
                for (String mediaUrl : value.mediaUrls()) {
                    if (!isUrlWhitelisted(mediaUrl, whitelistDomains)) {
                        LOGGER.warn("[WARN] [EXC-002] [Correlation ID: {}] Potential SSRF attempt detected. Media URL not whitelisted: {}",
                                correlationId, mediaUrl);
                        buildConstraintViolation(context, "Media URL does not belong to authorized platform storage domain whitelist.");
                        return false;
                    }
                }
            }

            // 3. Validate content against XSS / injection patterns per OWASP A03 & [REQ-003]
            if (value.content() != null) {
                if (XSS_DANGEROUS_PATTERN.matcher(value.content()).find()) {
                    LOGGER.warn("[WARN] [EXC-002] [Correlation ID: {}] Malicious script pattern detected in schedule content.", correlationId);
                    buildConstraintViolation(context, "Content contains prohibited or dangerous script patterns.");
                    return false;
                }

                // 4. Enforce platform-specific length restrictions (e.g., TikTok <= 2200 chars) per [REQ-003]
                if (value.platform() != null && value.platform().equalsIgnoreCase(PLATFORM_TIKTOK)) {
                    if (value.content().length() > TIKTOK_MAX_CONTENT_LENGTH) {
                        LOGGER.warn("[WARN] [EXC-002] [Correlation ID: {}] TikTok content length violation: {} characters.",
                                correlationId, value.content().length());
                        buildConstraintViolation(context, "TikTok content length cannot exceed " + TIKTOK_MAX_CONTENT_LENGTH + " characters.");
                        return false;
                    }
                }
            }

            LOGGER.info("[PROCESS] [REQ-003] [Correlation ID: {}] Schedule payload validation successfully completed.", correlationId);
            return true;

        } catch (Exception e) {
            // [0.1] COMPREHENSIVE EXCEPTION LOGGING & TRACEABILITY LAW
            LOGGER.error("[CRITICAL FAIL] [EXC-002] [Correlation ID: {}] Unexpected exception during schedule payload validation. Raw error: {}",
                    correlationId, e.getMessage(), e);
            buildConstraintViolation(context, "Internal validation processing error occurred.");
            return false;
        }
    }

    /**
     * Helper method to verify if a given media URL matches the configured SSRF domain whitelist.
     * 
     * @param urlString the raw media URL string to inspect
     * @param whitelistDomains list of permitted domain suffixes or hosts
     * @return true if the URL host matches a whitelisted domain, false otherwise
     * @traceability [REQ-003]
     */
    private boolean isUrlWhitelisted(String urlString, List<String> whitelistDomains) {
        try {
            URI uri = URI.create(urlString);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }
            // Check host against each configured whitelist domain entry
            for (String whitelistedDomain : whitelistDomains) {
                String trimmedDomain = whitelistedDomain.trim();
                if (host.equalsIgnoreCase(trimmedDomain) || host.endsWith("." + trimmedDomain)) {
                    return true;
                }
            }
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("[WARN] [EXC-003] Failed to parse media URL string for SSRF validation: {}", urlString);
            return false;
        }
    }

    /**
     * Helper method to attach custom violation messages to the constraint context.
     * 
     * @param context the constraint validator context
     * @param message the descriptive error message
     * @traceability [EXC-002]
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}