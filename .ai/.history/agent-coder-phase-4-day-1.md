# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ScheduleRequestDto tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java sử dụng Java Record (public record ScheduleRequestDto(...)) với các annotation Jakarta Validation 3.0 nghiêm ngặt. Trường platform (String) annotate @NotBlank(message = "platform is required") @Pattern(regexp = "^(Facebook|Instagram|TikTok)$", message = "platform must be one of Facebook, Instagram, TikTok") để ngăn chặn injection giá trị ngoài whitelist theo [REQ-003]. Trường content (String) annotate @NotBlank(message = "content cannot be blank") @Size(min = 1, max = 5000, message = "content must not exceed 5000 characters"). Trường scheduledTime (OffsetDateTime) annotate @NotNull(message = "scheduledTime is required") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX") @Future(message = "scheduledTime must be in the future") để đảm bảo thời gian đăng bài luôn ở tương lai [EXC-002]. Trường mediaUrls (List<String>) annotate @Size(max = 10, message = "mediaUrls must not exceed 10 items"). Trường tenantId (UUID) annotate @NotNull(message = "tenantId is required") để đảm bảo cô lập dữ liệu đa tenant theo [NFR-003]. Áp dụng @Builder (Lombok) và @JsonInclude(JsonInclude.Include.NON_NULL).']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.JsonInclude;

/**
 * Data Transfer Object for creating a new publishing schedule request.
 * Enforces strict input validation per REQ-003 and exception handling per EXC-002.
 * Guarantees scheduled times are future-dated, platform is whitelisted,
 * content size is bounded, and tenant isolation is supported via tenantId.
 * All validation annotations are enforced during deserialization by Spring Boot's DataBinder.
 * @traceability [REQ-003], [EXC-002]
 */
@Builder // Lombok-generated fluent builder for immutable record construction
@JsonInclude(JsonInclude.Include.NON_NULL) // Ensures only non-null fields are serialized into JSON payload
public record ScheduleRequestDto(
        // Platform must be one of the whitelisted social media services; blocks injection of arbitrary values per [REQ-003]
        @NotBlank(message = "platform is required")
        @Pattern(regexp = "^(Facebook|Instagram|TikTok)$", message = "platform must be one of Facebook, Instagram, TikTok")
        String platform,

        // Content body cannot be empty and must not exceed 5000 characters per business rule and database column limit
        @NotBlank(message = "content cannot be blank")
        @Size(min = 1, max = 5000, message = "content must not exceed 5000 characters")
        String content,

        // Scheduled execution time must be a future timestamp; prevents past-dated or immediate submissions per [EXC-002]
        @NotNull(message = "scheduledTime is required")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        @Future(message = "scheduledTime must be in the future")
        OffsetDateTime scheduledTime,

        // Maximum 10 media attachments per schedule to prevent resource exhaustion and enforce payload caps
        @Size(max = 10, message = "mediaUrls must not exceed 10 items")
        List<String> mediaUrls,

        // Tenant identifier enforces multi-tenant data isolation at the service layer per [NFR-003]
        @NotNull(message = "tenantId is required")
        UUID tenantId
) {
        // Record auto-generates private final fields, public getters, equals(), hashCode(), and toString().
        // Lombok @Builder enables construction via: ScheduleRequestDto.builder().platform("FACEBOOK").content("...").build()
        // All Jakarta Validation constraints are applied at the field level and validated by Spring's @Valid or @Validated.
        // tenantId must be populated by the interceptor/filter before reaching this DTO to ensure schema-per-tenant isolation.
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo ba tệp nguồn tại package validator cho SchedulePayloadValidator: (1) ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java đóng vai trò annotation marker @interface ValidSchedulePayload với @Constraint(validatedBy = {}); (2) ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorImpl.java implements ConstraintValidator<ValidSchedulePayload, ScheduleRequestDto> thực thi quy tắc nghiệp vụ phức tạp. Logic xác thực: kiểm tra scheduledTime không nằm trong khoảng 90 ngày tới (ChronoUnit.DAYS.between(LocalDateTime.now(), request.scheduledTime()) <= 90) theo [REQ-003]; xác thực mediaUrls chỉ chứa URL thuộc whitelist domain cấu hình tĩnh trong application-schedule-service.yml dưới key app.security.media-url-whitelist (mặc định: cdn.socialscheduler.com, s3.socialscheduler.com, storage.googleapis.com/social-scheduler-prod) chống SSRF; xác thực content không chứa pattern nguy hiểm <script>, javascript:, data:text/html thông qua regex whitelist theo OWASP A03; kiểm tra platform và content phù hợp (TikTok không cho phép content vượt quá 2200 ký tự). Phương thức isValid() trả về true nếu tất cả quy tắc thỏa mãn, ngược lại ghi log WARN với correlation ID và ném ConstraintViolationException [EXC-002]. (3) Inject validator vào DTO thông qua annotation @ValidSchedulePayload trên lớp ScheduleRequestDto.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```markdown
```java
/**
 * Custom validation annotation for schedule payload inputs.
 * Enforces business rules on scheduled posting requests including time constraints,
 * media URL whitelisting, content sanitization, and platform-specific limits.
 * 
 * @traceability [REQ-003], [EXC-002]
 * @see SchedulePayloadValidatorImpl
 */
package org.nlh4j.socialscheduler.scheduleservice.validator;

import javax.validation.Constraint;
import javax.validation.ConstraintPayload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SchedulePayloadValidatorImpl.class)
public @interface ValidSchedulePayload {
    
    String message() default "Schedule payload validation failed";
    
    Class<?>[] groups() default {};
    
    Class<? extends ConstraintPayload>[] payload() default {};
}
```

```java
/**
 * Implementation of custom constraint validation for @ValidSchedulePayload.
 * Validates schedule payload against business rules: scheduled time window,
 * media URL security whitelist, content sanitization against XSS patterns,
 * and platform-specific content length limits (e.g., TikTok 2200 char limit).
 * 
 * @traceability [REQ-003], [EXC-002]
 * @see ValidSchedulePayload
 */
package org.nlh4j.socialscheduler.scheduleservice.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SchedulePayloadValidatorImpl implements ConstraintValidator<ValidSchedulePayload, ScheduleRequestDto> {

    private static final Logger log = LoggerFactory.getLogger(SchedulePayloadValidatorImpl.class);

    // [REQ-003] Whitelist of allowed media domains to prevent SSRF and unauthorized storage access
    private static final String[] MEDIA_WHITELIST = {
            "cdn.socialscheduler.com",
            "s3.socialscheduler.com",
            "storage.googleapis.com/social-scheduler-prod"
    };

    // Compiled regex pattern for XSS detection (OWASP A03)
    private static final java.util.regex.Pattern XSS_PATTERN = 
            java.util.regex.Pattern.compile("<script|javascript:|data:text/html", java.util.regex.Pattern.CASE_INSENSITIVE);
    
    // [REQ-003] Maximum content length for TikTok platform
    private static final int TIKTOK_MAX_CONTENT_LENGTH = 2200;

    @Override
    public boolean isValid(ScheduleRequestDto value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        boolean isValid = true;
        String correlationId = UUID.randomUUID().toString();

        // [REQ-003] Validate scheduledTime: must not be within 90 days from now
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = value.getScheduledTime();
        if (scheduledTime != null) {
            long daysBetween = ChronoUnit.DAYS.between(now, scheduledTime);
            if (daysBetween <= 90) {
                log.warn("[{}] Scheduled time {} is within 90 days from now. Rejecting payload.", correlationId);
                // [EXC-002] Record constraint violation for scheduled time window violation
                context.disableDefaultConstraintViolation()
                       .addConstraintViolationWithTemplate("Scheduled time must be more than 90 days from current date.");
                isValid = false;
            }
        }

        // [REQ-003] Validate mediaUrls: must only contain whitelisted domains
        if (isValid && value.getMediaUrls() != null) {
            String[] urls = value.getMediaUrls().split(",");
            for (String url : urls) {
                url = url.trim();
                if (!isMediaUrlWhitelisted(url)) {
                    log.warn("[{}] Media URL {} does not match whitelist. Potential SSRF risk.", correlationId, url);
                    context.disableDefaultConstraintViolation()
                           .addConstraintViolationWithTemplate("Media URL must be from whitelisted domains: cdn.socialscheduler.com, s3.socialscheduler.com, storage.googleapis.com/social-scheduler-prod.");
                    isValid = false;
                    break; // Stop on first invalid URL
                }
            }
        }

        // [REQ-003][EXC-003] Validate content: must not contain XSS patterns
        if (isValid && value.getContent() != null) {
            if (XSS_PATTERN.matcher(value.getContent()).find()) {
                log.warn("[{}] Content contains dangerous XSS pattern.", correlationId);
                context.disableDefaultConstraintViolation()
                       .addConstraintViolationWithTemplate("Content must not contain script, javascript, or data URI patterns.");
                isValid = false;
            }
        }

        // [REQ-003] Validate platform-content compatibility: TikTok content max 2200 characters
        if (isValid && value.getPlatform() != null && value.getContent() != null) {
            String platform = value.getPlatform().trim();
            if ("TikTok".equalsIgnoreCase(platform) && value.getContent().length() > TIKTOK_MAX_CONTENT_LENGTH) {
                log.warn("[{}] TikTok content exceeds maximum length of {} characters.", correlationId, TIKTOK_MAX_CONTENT_LENGTH);
                context.disableDefaultConstraintViolation()
                       .addConstraintViolationWithTemplate("TikTok content must not exceed " + TIKTOK_MAX_CONTENT_LENGTH + " characters.");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Checks if a given media URL matches any of the configured whitelist domains.
     * Implements SSRF protection by rejecting localhost, private, and reserved IP ranges.
     */
    private boolean isMediaUrlWhitelisted(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        // SSRF: reject internal/loopback/IP addresses
        if (url.matches("^(http|https)://(127\\.0\\.0\\.1|localhost|10\\.|172\\.1[6-9]\\.|172\\.2[0-9]\\.|172\\.3[0-1]\\.|192\\.168\\.)")) {
            return false;
        }
        // Whitelist domain check
        for (String pattern : MEDIA_WHITELIST) {
            if (url.matches("^(http|https)?:?\\/\\/" + java.util.regex.Pattern.quote(pattern) + "(.*)?$")) {
                return true;
            }
        }
        return false;
    }
}
```

```java
/**
 * Data transfer object for schedule creation/request payloads.
 * Annotated with @ValidSchedulePayload for custom cross-field validation.
 * 
 * @traceability [REQ-003]
 */
package org.nlh4j.socialscheduler.scheduleservice.validator;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public class ScheduleRequestDto {

    @NotNull(message = "Platform is required")
    @Pattern(regexp = "^(Facebook|Instagram|TikTok)$", message = "Platform must be Facebook, Instagram, or TikTok")
    private String platform;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Future(message = "Scheduled time must be a future date")
    private LocalDateTime scheduledTime;

    private String mediaUrls;

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(String mediaUrls) { this.mediaUrls = mediaUrls; }
}
```
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorImpl.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorImpl.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo ba tệp nguồn tại package validator cho SchedulePayloadValidator: (1) ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java đóng vai trò annotation marker @interface ValidSchedulePayload với @Constraint(validatedBy = {}); (2) ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorImpl.java implements ConstraintValidator<ValidSchedulePayload, ScheduleRequestDto> thực thi quy tắc nghiệp vụ phức tạp. Logic xác thực: kiểm tra scheduledTime không nằm trong khoảng 90 ngày tới (ChronoUnit.DAYS.between(LocalDateTime.now(), request.scheduledTime()) <= 90) theo [REQ-003]; xác thực mediaUrls chỉ chứa URL thuộc whitelist domain cấu hình tĩnh trong application-schedule-service.yml dưới key app.security.media-url-whitelist (mặc định: cdn.socialscheduler.com, s3.socialscheduler.com, storage.googleapis.com/social-scheduler-prod) chống SSRF; xác thực content không chứa pattern nguy hiểm <script>, javascript:, data:text/html thông qua regex whitelist theo OWASP A03; kiểm tra platform và content phù hợp (TikTok không cho phép content vượt quá 2200 ký tự). Phương thức isValid() trả về true nếu tất cả quy tắc thỏa mãn, ngược lại ghi log WARN với correlation ID và ném ConstraintViolationException [EXC-002]. (3) Inject validator vào DTO thông qua annotation @ValidSchedulePayload trên lớp ScheduleRequestDto.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java đánh dấu @Component và @Slf4j (Lombok). Triển khai interface RateLimitStrategy với phương thức RateLimitResult tryConsume(String userId, String endpoint, int tokens) trả về đối tượng chứa allowed, remainingTokens, retryAfterSeconds. Inject RedisTemplate<String, String> thông qua constructor cấu hình với LettuceConnectionFactory tại RedisConfig. Tải Lua script từ classpath resource ./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua thông qua DefaultRedisScript<Long> bean. Script Lua đảm bảo tính nguyên tử với logic: (1) Kiểm tra sự tồn tại khóa rate_limit:{userId}:{endpoint}; (2) Nếu chưa tồn tại, khởi tạo bucket với capacity=100, tokens=capacity, lastRefillTimestamp=now; (3) Tính số token cần bổ sung dựa trên refillRate=60 token/phút; (4) Cập nhật tokens = min(capacity, tokens + refillAmount); (5) Nếu tokens >= requestedTokens, trừ token và trả về {1, remainingTokens}; (6) Ngược lại trả về {0, 0} và retryAfterSeconds. Cấu hình @ConfigurationProperties(prefix = "rate-limit.token-bucket"): capacity=100, refillRatePerMinute=60, keyPrefix="rate_limit:" [REQ-003]. Khi tryConsume trả về allowed=false, ném RateLimitExceededException kèm retryAfterSeconds [EXC-005]. Bổ sung metric Micrometer rate_limit.tokens.consumed.total và rate_limit.exceeded.total. Sử dụng structured logging với MDC context correlationId, userId, endpoint, tokensRequested, tokensRemaining theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.ratelimitservice.strategy;

// [REQ-003] Rate limiting enforcement with Redis Token Bucket strategy
// [EXC-005] Exception handling and retry-after calculation for rate limit exceedance

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Enterprise Token Bucket Rate Limiting Strategy implemented using Redis and Lua scripting.
 * Guarantees atomic evaluations of request rates across distributed cluster nodes.
 *
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@Component
public class RedisTokenBucketStrategy {

    // Top-of-class immutable constants and configuration handles [0.2]
    private static final String LUA_SCRIPT_PATH = "scripts/token-bucket.lua";
    private static final String METRIC_CONSUMED_TOTAL = "rate_limit.tokens.consumed.total";
    private static final String METRIC_EXCEEDED_TOTAL = "rate_limit.exceeded.total";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_ENDPOINT = "endpoint";
    private static final String MDC_TOKENS_REQUESTED = "tokensRequested";
    private static final String MDC_TOKENS_REMAINING = "tokensRemaining";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> redisScript;
    private final MeterRegistry meterRegistry;

    // Configurable token bucket parameters injected from application properties
    @Value("${rate-limit.token-bucket.capacity:100}")
    private long capacity;

    @Value("${rate-limit.token-bucket.refill-rate-per-minute:60}")
    private long refillRatePerMinute;

    @Value("${rate-limit.token-bucket.key-prefix:rate_limit:}")
    private long keyPrefix; // Stored as string prefix via formatted keys

    private Counter tokensConsumedCounter;
    private Counter rateLimitExceededCounter;

    /**
     * Constructor-based dependency injection for Redis string template and Micrometer metrics.
     *
     * @param redisTemplate Redis template for executing atomic script evaluations
     * @param meterRegistry Micrometer metrics registry for operational telemetry
     */
    public RedisTokenBucketStrategy(StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
        
        // Initialize the atomic Redis Lua script template
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_SCRIPT_PATH)));
        this.redisScript.setResultType(List.class);
    }

    /**
     * Post-construct initialization hook to register custom metrics securely.
     */
    @PostConstruct
    public void initMetrics() {
        this.tokensConsumedCounter = Counter.builder(METRIC_CONSUMED_TOTAL)
                .description("Total number of successfully consumed rate-limit tokens")
                .register(meterRegistry);
        this.rateLimitExceededCounter = Counter.builder(METRIC_EXCEEDED_TOTAL)
                .description("Total number of rate limit exceedance exceptions triggered")
                .register(meterRegistry);
    }

    /**
     * Attempts to consume specified tokens for a given user and endpoint transaction.
     * Evaluates atomic Lua script against Redis cluster.
     *
     * @param userId   Target unique user identifier
     * @param endpoint Target protected API endpoint
     * @param tokens   Number of tokens requested for this operation
     * @return RateLimitResult containing consumption status, remaining tokens, and retry window
     * @throws RateLimitExceededException if allowed evaluates to false
     */
    public RateLimitResult tryConsume(String userId, String endpoint, int tokens) {
        // Populate MDC logging context with tracing attributes for OWASP A09 logging compliance
        MDC.put(MDC_USER_ID, userId != null ? userId : "ANONYMOUS");
        MDC.put(MDC_ENDPOINT, endpoint != null ? endpoint : "UNKNOWN");
        MDC.put(MDC_TOKENS_REQUESTED, String.valueOf(tokens));

        try {
            log.info("[PROCESS] [REQ-003] Evaluating Redis Token Bucket rate limit for user: {} on endpoint: {}", userId, endpoint);

            // Construct Redis key following the strict prefix convention: rate_limit:{userId}:{endpoint}
            String redisKey = String.format("rate_limit:%s:%s", userId, endpoint);
            List<String> keys = Collections.singletonList(redisKey);

            // Prepare arguments for Lua script execution: [capacity, refillRatePerMinute, requestedTokens, currentTimeMillis]
            long currentTimeMillis = System.currentTimeMillis();
            Object[] args = new Object[] {
                    String.valueOf(capacity),
                    String.valueOf(refillRatePerMinute),
                    String.valueOf(tokens),
                    String.valueOf(currentTimeMillis)
            };

            // Execute Lua script atomically on Redis cluster
            List<Long> result = redisTemplate.execute(redisScript, keys, args);

            if (result == null || result.size() < 3) {
                log.error("[CRITICAL FAIL] [EXC-005] Redis Lua script returned malformed execution payload for user: {}", userId);
                throw new IllegalStateException("Rate limiter evaluation failed due to malformed Redis response.");
            }

            boolean allowed = result.get(0) == 1L;
            long remainingTokens = result.get(1);
            long retryAfterSeconds = result.get(2);

            MDC.put(MDC_TOKENS_REMAINING, String.valueOf(remainingTokens));

            if (allowed) {
                // Increment successful consumption metric counter
                tokensConsumedCounter.increment(tokens);
                log.debug("[SUCCESS] [REQ-003] Rate limit passed. Remaining tokens: {} for user: {}", remainingTokens, userId);
                return new RateLimitResult(true, remainingTokens, 0L);
            } else {
                // Increment rate limit exceeded metric counter
                rateLimitExceededCounter.increment();
                log.warn("[RATE_LIMIT_EXCEEDED] [EXC-005] Rate limit exceeded for user: {} on endpoint: {}. Retry after: {}s", 
                        userId, endpoint, retryAfterSeconds);
                
                // Throw explicit enterprise business exception mapping to EXC-005 requirement
                throw new RateLimitExceededException(
                        userId,
                        endpoint,
                        retryAfterSeconds,
                        String.format("Rate limit exceeded. Please retry after %d seconds.", retryAfterSeconds)
                );
            }

        } catch (RateLimitExceededException e) {
            // Re-throw business exception directly without swallowing cause chain
            throw e;
        } catch (Exception e) {
            // Defensive fault-tolerance: Log unexpected infrastructure failures and fail-open or fail-secure
            log.error("[CRITICAL FAIL] [EXC-005] Redis Token Bucket evaluation failed due to network or connection drop. Raw error: {}", e.getMessage(), e);
            // In high-availability architectures, we wrap and propagate as a runtime exception
            throw new RuntimeException("Rate limiter backend unavailable", e);
        } finally {
            // Clean up MDC context to prevent thread-pool memory leaks
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_ENDPOINT);
            MDC.remove(MDC_TOKENS_REQUESTED);
            MDC.remove(MDC_TOKENS_REMAINING);
        }
    }

    /**
     * Immutable value carrier representing the outcome of a token bucket consumption evaluation.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingTokens;
        private final long retryAfterSeconds;

        public RateLimitResult(boolean allowed, long remainingTokens, long retryAfterSeconds) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getRemainingTokens() {
            return remainingTokens;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }
    }
}
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java đánh dấu @Component và @Slf4j (Lombok). Triển khai interface RateLimitStrategy với phương thức RateLimitResult tryConsume(String userId, String endpoint, int tokens) trả về đối tượng chứa allowed, remainingTokens, retryAfterSeconds. Inject RedisTemplate<String, String> thông qua constructor cấu hình với LettuceConnectionFactory tại RedisConfig. Tải Lua script từ classpath resource ./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua thông qua DefaultRedisScript<Long> bean. Script Lua đảm bảo tính nguyên tử với logic: (1) Kiểm tra sự tồn tại khóa rate_limit:{userId}:{endpoint}; (2) Nếu chưa tồn tại, khởi tạo bucket với capacity=100, tokens=capacity, lastRefillTimestamp=now; (3) Tính số token cần bổ sung dựa trên refillRate=60 token/phút; (4) Cập nhật tokens = min(capacity, tokens + refillAmount); (5) Nếu tokens >= requestedTokens, trừ token và trả về {1, remainingTokens}; (6) Ngược lại trả về {0, 0} và retryAfterSeconds. Cấu hình @ConfigurationProperties(prefix = "rate-limit.token-bucket"): capacity=100, refillRatePerMinute=60, keyPrefix="rate_limit:" [REQ-003]. Khi tryConsume trả về allowed=false, ném RateLimitExceededException kèm retryAfterSeconds [EXC-005]. Bổ sung metric Micrometer rate_limit.tokens.consumed.total và rate_limit.exceeded.total. Sử dụng structured logging với MDC context correlationId, userId, endpoint, tokensRequested, tokensRemaining theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```lua
-- =============================================================================
-- Enterprise Master Governance Guardrails & Traceability Compliance
-- =============================================================================
-- Target Project Identity: social-scheduler
-- Target Component Destination Path: ./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua
-- Traceability Audit Tags: [REQ-003], [EXC-005]
-- =============================================================================
-- Business Context & Technical Architecture:
-- This Lua script implements a high-performance, atomic Token Bucket rate-limiting
-- algorithm directly inside Redis. It guarantees thread-safe execution across
-- distributed microservice instances by leveraging Redis EVAL commands.
-- 
-- Algorithm Flow:
-- 1. KEYS[1]: Redis key representing the rate limit bucket (e.g., rate_limit:{userId}:{endpoint})
-- 2. ARGV[1]: Current timestamp in milliseconds (supplied by Redis server time or application)
-- 3. ARGV[2]: Maximum capacity of the token bucket (e.g., 100)
-- 4. ARGV[3]: Refill rate in tokens per minute (e.g., 60)
-- 5. ARGV[4]: Number of tokens requested for the current transaction (e.g., 1)
-- 
-- Return Structure (Redis Multi-Bulk Reply / Lua Table converted to array):
-- - Index 1: 1 if allowed, 0 if rate limit exceeded (rejected)
-- - Index 2: Remaining tokens in the bucket after consumption (or 0 if rejected)
-- - Index 3: Retry-after duration in seconds (0 if allowed, calculated delay if rejected)
-- =============================================================================

-- [REQ-003] Extract input arguments with explicit type coercion to ensure robust arithmetic
local rateLimitKey = KEYS[1]
local currentTimeMs = tonumber(ARGV[1])
local maxCapacity = tonumber(ARGV[2])
local refillRatePerMinute = tonumber(ARGV[3])
local requestedTokens = tonumber(ARGV[4])

-- [REQ-003] Calculate token refill parameters per millisecond for high-precision sliding window
local refillRatePerMs = refillRatePerMinute / 60000.0

-- [REQ-003] Retrieve existing bucket state from Redis hash structure
-- Fields: 'tokens' (current available tokens), 'lastRefill' (timestamp of last refill in ms)
local bucketData = redis.call('HMGET', rateLimitKey, 'tokens', 'lastRefill')

local currentTokens
local lastRefillTimestamp

-- [REQ-003] Initialize bucket if it does not exist in Redis (Lazy Initialization Pattern)
if not bucketData[1] or not bucketData[2] then
    currentTokens = maxCapacity
    lastRefillTimestamp = currentTimeMs
    -- Persist initial state with a TTL safety net (24 hours to prevent memory leaks)
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
else
    currentTokens = tonumber(bucketData[1])
    lastRefillTimestamp = tonumber(bucketData[2])
    
    -- [REQ-003] Compute elapsed time since the last token replenishment
    local elapsedMs = currentTimeMs - lastRefillTimestamp
    
    if elapsedMs > 0 then
        -- Calculate newly generated tokens based on elapsed time and refill rate
        local generatedTokens = elapsedMs * refillRatePerMs
        
        -- Replenish tokens up to the maximum capacity ceiling
        currentTokens = math.min(maxCapacity, currentTokens + generatedTokens)
        
        -- Update the last refill timestamp to the current transaction timestamp
        lastRefillTimestamp = currentTimeMs
    end
end

-- [REQ-003], [EXC-005] Evaluate whether the bucket contains sufficient tokens for the request
if currentTokens >= requestedTokens then
    -- Deduct requested tokens from the bucket balance
    currentTokens = currentTokens - requestedTokens
    
    -- Update Redis state with the new token balance and updated timestamp
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
    
    -- Return success response: [Allowed (1), Remaining Tokens, Retry-After (0)]
    return {1, math.floor(currentTokens), 0}
else
    -- [EXC-005] Rate Limit Exceeded: Calculate exact retry-after duration in seconds
    local deficit = requestedTokens - currentTokens
    local msUntilSufficientTokens = deficit / refillRatePerMs
    local retryAfterSeconds = math.ceil(msUntilSufficientTokens / 1000.0)
    
    -- Ensure retry-after is at least 1 second to prevent immediate spamming
    if retryAfterSeconds < 1 then
        retryAfterSeconds = 1
    end
    
    -- Persist the current state (with potential partial refill) without deducting rejected tokens
    redis.call('HMSET', rateLimitKey, 'tokens', currentTokens, 'lastRefill', lastRefillTimestamp)
    redis.call('EXPIRE', rateLimitKey, 86400)
    
    -- Return failure response: [Rejected (0), Remaining Tokens (0), Retry-After Seconds]
    return {0, math.floor(currentTokens), retryAfterSeconds}
end
```

# Day 1: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java`
*   Traceability Audit Tags For This Task: ['[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java kế thừa RuntimeException với annotation @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS) để Spring tự động trả về HTTP 429 [EXC-005]. Khai báo các trường private final: userId (UUID), endpoint (String), retryAfterSeconds (long), timestamp (OffsetDateTime). Bổ sung constructor nhận ba tham số userId, endpoint, retryAfterSeconds để khởi tạo giá trị và sinh timestamp = OffsetDateTime.now(). Triển khai các phương thức getter cho bốn trường. Override phương thức getMessage() trả về thông điệp cấu trúc "Rate limit exceeded for userId={userId} endpoint={endpoint} retryAfterSeconds={retryAfterSeconds}" bằng MessageFormat hỗ trợ internationalization. Áp dụng @Slf4j ghi log WARN trong constructor với userId, endpoint hỗ trợ giám sát và phát hiện lạm dụng theo OWASP A09. Bổ sung constructor phụ nhận Throwable cause hỗ trợ chaining exception từ Redis client. Đảm bảo lớp ngoại lệ được GlobalExceptionHandler tại schedule-service bắt và chuyển đổi thành ErrorResponse chuẩn với HTTP 429, header Retry-After.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

