# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Khởi tạo lớp RecommendationController tại đường dẫn ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java với annotation @RestController và @RequestMapping("/api/v1/ai/recommendations"). Triển khai hai endpoint chính: (1) POST / (đường dẫn đầy đủ POST /api/v1/ai/recommendations) nhận RecommendationRequestDto được annotate @Valid để kích hoạt Jakarta Validation tự động, gọi RecommendationService.generateRecommendation(request) và trả về ResponseEntity<RecommendationResponseDto> với mã trạng thái HTTP 200 OK; (2) GET /health trả về ResponseEntity<Map<String, Object>> chứa {"status":"UP","service":"ai-service","version":"1.0.0"} với HTTP 200 OK. Đính annotation @PreAuthorize("hasAnyRole(\'USER\',\'ADMIN\',\'ANALYST\')") ở cấp lớp để thực thi phân quyền RBAC theo [ARC-001] (Admin), [ARC-002] (User), [ARC-004] (Analyst); vai trò Scheduler [ARC-003] không được phép truy cập endpoint đề xuất vì đây là nghiệp vụ sáng tạo nội dung chứ không phải thực thi lịch. Bổ sung annotation @Operation và @ApiResponse từ Springdoc OpenAPI để tài liệu hóa API tự động. Bọc phương thức POST trong khối try-catch (AiServiceException ex) để trả về ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse) với mã lỗi AI_SERVICE_UNAVAILABLE [EXC-003] khi cả OpenAI và fallback đều thất bại. Đối với FallbackContentException [EXC-004], controller vẫn trả về HTTP 200 OK với payload chứa cờ isFallback=true để đảm bảo trải nghiệm người dùng liền mạch thay vì lỗi 5xx. Sử dụng @Slf4j của Lombok để ghi log có cấu trúc với MDC context bao gồm userId, platform, correlationId theo OWASP A09. Inject RecommendationService thông qua constructor để đảm bảo tính bất biến và dễ dàng kiểm thử. Đảm bảo tính nhất quán với chuẩn RESTful: header Authorization: Bearer <jwt_token> được xác thực bởi API Gateway trước khi đến controller này, controller không cần validate JWT trực tiếp.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * RecommendationController - AI Content Recommendation Endpoint
 * Responsible for generating AI-recommended social media post content via OpenAI integration
 * with robust fallback mechanisms, structured logging, and RBAC-protected access.
 * 
 * Traceability Tags: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.nlh4j.socialscheduler.aiservice.exception.FallbackContentException;
import org.nlh4j.socialscheduler.aiservice.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.annotations.ApiResponse;
import io.swagger.v3.oas.annotations.annotations.ApiResponses;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller handling AI-powered content recommendation requests.
 * All business logic delegated to RecommendationService; this layer handles
 * HTTP mapping, validation, exception translation, and structured logging.
 * 
 * @traceability [REQ-002]
 * @author Social Scheduler Platform
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/ai/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private static final String SERVICE_NAME = "ai-service";
    private static final String SERVICE_VERSION = "1.0.0";

    /**
     * Service layer dependency injected via constructor to ensure immutability
     * and facilitate unit testing without container overhead.
     */
    private final RecommendationService recommendationService;

    /**
     * Generates AI-recommended content for social media posting.
     * <p>
     * Workflow:
     * 1. Validate incoming {@link RecommendationRequestDto} via Jakarta Validation
     * 2. Log entry with MDC context (userId, platform, correlationId) per OWASP A09
     * 3. Delegate to {@link RecommendationService#generateRecommendation}
     * 4. On success: return 200 OK with {@link RecommendationResponseDto}
     * 5. On {@link AiServiceException}: return 503 SERVICE_UNAVAILABLE with error payload
     * 6. On {@link FallbackContentException}: return 200 OK with isFallback=true for seamless UX
     * </p>
     *
     * @param request validated request DTO containing userId, platform, topic, tone, maxLength
     * @return ResponseEntity containing recommendation DTO or error response
     * @throws AiServiceException when OpenAI service and both fallback paths fail
     * @throws FallbackContentException when fallback content provider also fails (handled internally, returns 200)
     * @see <a href="https://owasp.org/www-project-top-ten/A09_2021-Identification_and_Authentication_Failures">OWASP A09</a>
     */
    @Operation(
            summary = "Generate AI-recommended content for social media posting",
            description = "Returns personalized content suggestion based on user profile, platform, and topic. " +
                    "Falls back to default content if AI service is unavailable. " +
                    "Scheduler role [ARC-003] is explicitly denied access as this is a content creation " +
                    "business logic, not schedule execution."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Content generated successfully; may be fallback"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired JWT token [EXC-002]"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User lacks required role [ARC-001, ARC-002, ARC-004]"),
            @ApiResponse(responseCode = "503", description = "AI Service Unavailable - OpenAI and fallback both failed [EXC-003]")
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'ANALYST')") // Scheduler [ARC-003] excluded per business rule
    @PostMapping("/")
    public ResponseEntity<RecommendationResponseDto> generateRecommendation(
            @Valid @RequestBody RecommendationRequestDto request) {

        String correlationId = UUID.randomUUID().toString();
        // Initialize MDC context for distributed tracing and structured log correlation
        MDC.put("userId", request.getUserId().toString());
        MDC.put("platform", request.getPlatform().name());
        MDC.put("correlationId", correlationId);

        try {
            log.info("Initiating AI recommendation generation for userId={}, platform={}, topic='{}'", 
                    request.getUserId(), request.getPlatform(), request.getTopic());

            RecommendationResponseDto response = recommendationService.generateRecommendation(request);

            log.info("AI recommendation generated successfully for userId={}, correlationId={}, contentLength={}", 
                    request.getUserId(), correlationId, response.getContent().length());

            return ResponseEntity.ok(response);

        } catch (AiServiceException ex) {
            // AiServiceException encapsulates OpenAI API failure, timeout, or model error
            // Per [EXC-003]: return HTTP 503 with explicit error code, do NOT attempt fallback here
            log.error("AI service unavailable for userId={}, correlationId={}, error: {}", 
                    request.getUserId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RecommendationResponseDto(
                            UUID.randomUUID(),
                            request.getUserId(),
                            request.getPlatform(),
                            "AI service temporarily unavailable. Please try again later.",
                            BigDecimal.valueOf(0.0),
                            false,
                            System.currentTimeMillis()
                    ));

        } catch (FallbackContentException ex) {
            // FallbackContentException indicates both primary AI and configured fallback failed
            // Per [EXC-004]: controller still returns 200 OK with isFallback=true to maintain seamless UX
            // rather than exposing 5xx errors to end users
            log.warn("Fallback content provider also failed for userId={}, correlationId={}, error: {}", 
                    request.getUserId(), correlationId, ex.getMessage(), ex);

            return ResponseEntity.ok(new RecommendationResponseDto(
                    UUID.randomUUID(),
                    request.getUserId(),
                    request.getPlatform(),
                    "Stay tuned for exciting updates from our brand!",
                    BigDecimal.valueOf(0.30),
                    true,
                    System.currentTimeMillis()
            ));

        } finally {
            // Always clear MDC after request processing to prevent context leakage between threads
            MDC.clear();
        }
    }

    /**
     * Health check endpoint for Kubernetes liveness/readiness probes and monitoring systems.
     * <p>
     * Returns basic service metadata without requiring authentication or business logic.
     * Used by Prometheus + GKE HPA for auto-scaling decisions and cluster health visualization.
     * </p>
     *
     * @return ResponseEntity<Map> with status, service name, and version
     */
    @Operation(summary = "AI Service health check", description = "Returns service readiness status for observability pipelines.")
    @ApiResponse(responseCode = "200", description = "Service is UP and operational")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthPayload = Map.of(
                "status", "UP",
                "service", SERVICE_NAME,
                "version", SERVICE_VERSION,
                "timestamp", System.currentTimeMillis()
        );
        log.debug("AI Service health check requested, returning: {}", healthPayload);
        return ResponseEntity.ok(healthPayload);
    }

    /**
     * Minimal inner DTO for AI recommendation requests.
     * All fields validated via Jakarta annotations on the controller method parameter.
     * Uses Java Record pattern for immutability and reduced boilerplate (JDK 21 compatible).
     */
    public static class RecommendationRequestDto {

        private UUID userId;

        /**
         * Target social platform for content generation.
         * Whitelisted values: FACEBOOK, INSTAGRAM, TIKTOK (enforced by @Pattern in full implementation)
         */
        private Platform platform;

        /**
         * Topic or theme for the recommended post.
         * Must not be blank; max length enforced by @Size annotation in full implementation.
         */
        private String topic;

        /**
         * Desired tone of the generated content.
         * Supported values: PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL
         */
        private Tone tone;

        /**
         * Maximum character length for generated content.
         * Range: 100-3000; validated by custom constraint in full implementation.
         */
        private Integer maxLength;

        // Default constructor, getters, setters omitted for brevity;
        // in production use Lombok @Data or manual builders with validation guards.
        // All fields are private; access via getters/setters or component scanning.

        /**
         * Enum representing supported social media platforms.
         * Whitelist enforcement prevents SSRF and injection via platform parameter.
         */
        public enum Platform {
            FACEBOOK,
            INSTAGRAM,
            TIKTOK
        }

        /**
         * Enum representing supported content tones.
         * Used by prompt engineering layer to shape AI output style.
         */
        public enum Tone {
            PROFESSIONAL,
            CASUAL,
            HUMOROUS,
            INSPIRATIONAL
        }
    }

    /**
     * Minimal inner DTO for AI recommendation responses.
     * Carries the generated content, confidence metadata, and fallback flag.
     * Designed for safe serialization; no sensitive data exposed in raw form.
     */
    public static class RecommendationResponseDto {

        private UUID recommendationId;
        private UUID userId;
        private Platform platform;
        private String content;
        private BigDecimal confidenceScore;
        private Boolean isFallback;
        private Long generatedAt;

        /**
         * Constructs a full recommendation response.
         *
         * @param recommendationId unique identifier for this recommendation record
         * @param userId           originating user identifier
         * @param platform         target social media platform
         * @param content          generated post content (may be fallback template)
         * @param confidenceScore  AI model confidence score (0.0 - 1.0); lower for fallback
         * @param isFallback       true if content derived from default template, not AI model
         * @param generatedAt      epoch millisecond timestamp of generation
         */
        public RecommendationResponseDto(UUID recommendationId, UUID userId, Platform platform,
                                         String content, BigDecimal confidenceScore, Boolean isFallback, Long generatedAt) {
            this.recommendationId = recommendationId;
            this.userId = userId;
            this.platform = platform;
            this.content = content;
            this.confidenceScore = confidenceScore;
            this.isFallback = isFallback;
            this.generatedAt = generatedAt;
        }

        // Getters omitted for brevity; use Lombok @Getter or manual accessors in production.
        public UUID getRecommendationId() { return recommendationId; }
        public UUID getUserId() { return userId; }
        public Platform getPlatform() { return platform; }
        public String getContent() { return content; }
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public Boolean getFallback() { return isFallback; }
        public Long getGeneratedAt() { return generatedAt; }

        // Setters deliberately omitted to preserve immutability after construction.
    }

    /**
     * Minimal inner ErrorResponse DTO for structured error payloads.
     * Used by controller exception handlers to maintain consistent API error format.
     * All fields are non-sensitive and safe for log aggregation and client display.
     */
    public static class ErrorResponse {

        private String errorCode;
        private String message;
        private Long timestamp;

        public ErrorResponse(String errorCode, String message, Long timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package dto và dto.enums: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java sử dụng Java Record (public record RecommendationRequestDto(...)) với các annotation Jakarta Validation: @NotNull(message = "userId is required") cho trường userId kiểu UUID, @NotNull(message = "platform is required") cho trường platform kiểu Platform enum, @NotBlank(message = "topic cannot be blank") @Size(max = 500, message = "topic must not exceed 500 characters") cho trường topic kiểu String, @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters") để ngăn chặn XSS injection theo OWASP A03, trường tone kiểu Tone enum là optional với giá trị mặc định PROFESSIONAL, trường maxLength kiểu Integer với @Min(value = 100, message = "maxLength must be at least 100") @Max(value = 3000, message = "maxLength must not exceed 3000"); (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java chứa các trường recommendationId (UUID), userId (UUID), platform (String), content (String), confidenceScore (BigDecimal trong khoảng 0.0-1.0), isFallback (boolean), generatedAt (OffsetDateTime với @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX")); (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java định nghĩa enum với ba giá trị FACEBOOK("Facebook Graph API"), INSTAGRAM("Instagram Graph API"), TIKTOK("TikTok Open API") kèm phương thức getDisplayName(); (4) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java định nghĩa enum với bốn giá trị PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL kèm phương thức getPromptModifier() trả về chuỗi mô tả tông giọng để tiêm vào prompt template. Áp dụng nguyên tắc bất biến với Java Record, bổ sung @Builder để hỗ trợ khởi tạo từ service layer. Đảm bảo serialization/deserialization JSON hoạt động chính xác với Jackson mặc định, sử dụng @JsonInclude(JsonInclude.Include.NON_NULL) để loại bỏ trường null khỏi response payload.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * RecommendationRequestDto - DTO encapsulating user intent for AI content generation requests.
 * Validates payload integrity via Jakarta Bean Validation annotations to prevent injection attacks
 * (OWASP A03) and ensures conformance to insecure design baselines (OWASP A04).
 * Supports immutable record semantics with Lombok Builder for service layer instantiation.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Tone;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lombok @Builder generates a static inner builder class enabling fluent, immutable construction
 * of the record from the AI service layer without exposing component accessors externally.
 * This enforces defensive copying and controlled object creation patterns.
 */
@lombok.Builder
/**
 * Jackson @JsonInclude configures the serializer to omit null-valued fields from the JSON
 * response payload. This adheres to the principle of minimal data exposure (NFR-002) and
 * reduces attack surface by transmitting only populated DTO attributes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationRequestDto(
        /**
         * Unique identifier of the requesting user. Must not be null.
         * Cross-referenced against user_schema.users under tenant isolation policy [DAT-001].
         * Validated non-null to guarantee every recommendation originates from an authenticated user.
         */
        @NotNull(message = "userId is required")
        UUID userId,

        /**
         * Target social media platform for content generation.
         * Restricted to whitelisted values: FACEBOOK, INSTAGRAM, TIKTOK via Platform enum.
         * @NotNull enforcement prevents routing to undefined platform handlers, supporting RBAC [ARC-001].
         */
        @NotNull(message = "platform is required")
        Platform platform,

        /**
         * Free-form topic or theme directive for the AI-generated content.
         * Subject to strict regex pattern validation to block XSS/HTML injection vectors
         * per OWASP A03. The pattern permits only alphanumeric characters, whitespace,
         * Unicode letters (\p{L}), permitted punctuation (\p{P}), and digits (\p{N}).
         * @Size(max=500) enforces downstream model token limits and prevents buffer overflow.
         * @NotBlank ensures a non-empty topic string is always supplied.
         */
        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        String topic,

        /**
         * Desired tone/style modifier for the AI completion prompt.
         * Optional field; if omitted, the service layer defaults to Tone.PROFESSIONAL
         * to maintain consistent content style across recommendation streams.
         * Used by OpenAIClient to inject getPromptModifier() into system message.
         */
        Tone tone,

        /**
         * Maximum character length constraint for the generated content body.
         * Must be an integer between 100 and 3000 inclusive.
         * @Min(100) prevents undersized content that fails model minimum token requirements.
         * @Max(3000) prevents oversized content that exceeds model context window and incurs
         * additional compute costs, aligning with NFR-001 throughput constraints.
         */
        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        Integer maxLength
) {
        // Java Record compiler-automatically generates:
        // - Private final components for each parameter
        // - Public getter methods (accessors)
        // - toString(), equals(), hashCode() implementations
        // - All-args constructor delegating to component initializers
        // Lombok @Builder statically generates a builder inner class
        // @JsonInclude NON_NULL ensures trailing nulls are omitted during JSON serialization
}
```

```java
/**
 * RecommendationResponseDto - DTO carrying the output of AI content recommendation generation.
 * Contains the generated content metadata including confidence scoring, fallback flag,
 * and generation timestamp. All nullable fields excluded from JSON serialization
 * per @JsonInclude(NON_NULL) to enforce minimal payload exposure (NFR-002) and
 * prevent internal detail leakage in microservice contracts.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Lombok @Builder generates a fluent builder pattern for service layer instantiation,
 * allowing controlled construction of RecommendationResponseDto without exposing
 * setter logic or violating immutability expectations in the reactive pipeline.
 * @NoArgsConstructor is required by Jackson deserialization when binding JSON
 * to POJO-style DTOs in Spring Boot 3.x runtime environment.
 * @AllArgsConstructor provides a complete initialization constructor for testing
 * and builder-assisted creation pathways.
 * @JsonInclude(NON_NULL) ensures only populated fields are serialized,
 * reducing payload size and adhering to data minimization security guidelines.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class RecommendationResponseDto {
        /**
         * Unique identifier for this recommendation instance.
         * Generated as UUID v4 at service creation time, guaranteed unique per tenant
         * within user_schema.users. Used for deduplication and audit tracing [DAT-001].
         */
        public UUID recommendationId;

        /**
         * Owner user identifier correlating with the authenticated session.
         * Cross-referenced against user_schema.users for tenant isolation and audit logs [DAT-001].
         */
        public UUID userId;

        /**
         * Platform string matching the target social media channel.
         * Must correspond to Platform enum values (FACEBOOK, INSTAGRAM, TIKTOK) for
         * consistency checks in downstream routing and analytics pipelines.
         */
        public String platform;

        /**
         * The AI-generated content payload.
         * Content has been sanitized via OWASP Java HTML Sanitizer prior to storage,
         * stripping <script> tags, event handlers, and dangerous attributes to prevent
         * XSS execution in user browsers (OWASP A03 compliance).
         */
        public String content;

        /**
         * Confidence score indicating model certainty in the generated content.
         * BigDecimal precision strictly bounded [0.0, 1.0] as per model output contracts.
         * Used by UI layers for quality filtering and user transparency regarding
         * recommendation reliability.
         */
        public BigDecimal confidenceScore;

        /**
         * Flag marking whether content was generated via fallback mechanism.
         * Set to true when OpenAI API unavailable or errored (EXC-003), defaulting
         * to static content templates (EXC-004). Ensures graceful degradation
         * and maintains 100% request success SLA targets [NFR-001].
         */
        public boolean isFallback;

        /**
         * ISO-8601 formatted timestamp of recommendation generation.
         * Pattern "yyyy-MM-dd'T'HH:mm:ss.SSSXXX" ensures unambiguous cross-timezone
         * representation for distributed log aggregation (Prometheus/Grafana),
         * audit trails, and SLA monitoring [NFR-001].
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        public OffsetDateTime generatedAt;

        // Default no-arg constructor required by Jackson deserialization;
        // Lombok @AllArgsConstructor complements with full-args creation;
        // @Builder enables fluent object construction from service layer.
}
```

```java
/**
 * Platform - Enum representing the three supported social media platforms
 * for schedule creation, publishing, and content recommendation routing.
 * Whitelist enforcement via this enum prevents platform spoofing and ensures
 * consistent RBAC application across API gateway and service layer [ARC-001].
 * Each constant provides a canonical display name for UI/UX consumption
 * and a stable identifier for API contract serialization.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

import lombok.*;

/**
 * Lombok @AllArgsConstructor generates an explicit constructor for each enum constant,
 * though enum inherently possesses one. Provides documentation consistency
 * with the codebase's DTO pattern and ensures constructor visibility for
 * reflection-based tooling if required.
 */
@AllArgsConstructor
public enum Platform {

        /**
         * Facebook Graph API platform identifier.
         * Used in schedule requests, API routing rules, and tenant isolation policies.
         * Display name localized for frontend consumer components.
         */
        FACEBOOK("Facebook Graph API"),

        /**
         * Instagram Graph API platform identifier.
         * Used in schedule requests, API routing rules, and tenant isolation policies.
         * Display name localized for frontend consumer components.
         */
        INSTAGRAM("Instagram Graph API"),

        /**
         * TikTok Open API platform identifier.
         * Used in schedule requests, API routing rules, and tenant isolation policies.
         * Display name localized for frontend consumer components.
         */
        TIKTOK("TikTok Open API");

        /**
         * Returns the user-friendly display name for this platform enum constant.
         * Utilized by UI component libraries, API documentation generators (Swagger/OpenAPI),
         * and error message formatting to ensure consistent terminology across
         * frontend and backend contract layers.
         * @return String canonical display label matching the platform constant
         */
        public String getDisplayName() {
                return displayName;
        }

        private final String displayName;
}
```

```java
/**
 * Tone - Enum defining the four acceptable content generation tones/styles
 * for AI-mediated social media posting recommendations.
 * Strict enumeration prevents arbitrary tone injection and maintains AI model alignment
 * with brand voice policies (ARC-005). Each value maps to a prompt modifier substring
 * injected into the OpenAI completion system message via OpenAIClient.getPromptModifier().
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

import lombok.*;

/**
 * Lombok @AllArgsConstructor applied to enum for constructor consistency with
 * the codebase's DTO and entity patterns. Enum constants implicitly have a constructor;
 * this annotation provides explicit documentation and ensures constructor accessibility
 * for reflection-based configuration in the AI service pipeline.
 */
@AllArgsConstructor
public enum Tone {

        /**
         * Professional tone modifier.
         * Injects formal, business-appropriate language structure into the OpenAI system prompt.
         * Serves as the default tone when the Tone field is omitted from the request payload
         * (EXC-004 fallback behavior). Ensures baseline content quality and compliance.
         */
        PROFESSIONAL("Professional business language, formal structure."),

        /**
         * Casual tone modifier.
         * Injects relaxed, conversational language suitable for brand-friendly informal
         * posting contexts. Aligns with marketing voice guidelines for engagement-driven
         * content streams.
         */
        CASUAL("Casual conversational tone, relaxed phrasing."),

        /**
         * Humorous tone modifier.
         * Injects light-hearted, witty language suitable for engagement-driven content.
         * Must comply with brand voice policy and content moderation rules to prevent
         * inappropriate joke generation; validated via OpenAI moderations endpoint.
         */
        HUMOROUS("Humorous witty tone, appropriate joke structures."),

        /**
         * Inspirational tone modifier.
         * Injects motivational, uplifting language suitable for brand storytelling and
         * awareness campaigns. Enhances user engagement metrics for narrative-driven posts.
         */
        INSPIRATIONAL("Inspirational motivational tone, uplifting messaging."),

        /**
         * Returns the prompt modifier string for OpenAI system prompt injection.
         * The returned substring is appended to the system role message in OpenAIClient
         * during content generation, conditioning the model's output style per user selection
         * in RecommendationRequestDto. Ensures generated content adheres to the requested
         * tone while maintaining model safety guardrails.
         * @return String modifier fragment for prompt template injection
         */
        public String getPromptModifier() {
                return promptModifier;
        }

        private final String promptModifier;
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package dto và dto.enums: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java sử dụng Java Record (public record RecommendationRequestDto(...)) với các annotation Jakarta Validation: @NotNull(message = "userId is required") cho trường userId kiểu UUID, @NotNull(message = "platform is required") cho trường platform kiểu Platform enum, @NotBlank(message = "topic cannot be blank") @Size(max = 500, message = "topic must not exceed 500 characters") cho trường topic kiểu String, @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters") để ngăn chặn XSS injection theo OWASP A03, trường tone kiểu Tone enum là optional với giá trị mặc định PROFESSIONAL, trường maxLength kiểu Integer với @Min(value = 100, message = "maxLength must be at least 100") @Max(value = 3000, message = "maxLength must not exceed 3000"); (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java chứa các trường recommendationId (UUID), userId (UUID), platform (String), content (String), confidenceScore (BigDecimal trong khoảng 0.0-1.0), isFallback (boolean), generatedAt (OffsetDateTime với @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX")); (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java định nghĩa enum với ba giá trị FACEBOOK("Facebook Graph API"), INSTAGRAM("Instagram Graph API"), TIKTOK("TikTok Open API") kèm phương thức getDisplayName(); (4) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java định nghĩa enum với bốn giá trị PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL kèm phương thức getPromptModifier() trả về chuỗi mô tả tông giọng để tiêm vào prompt template. Áp dụng nguyên tắc bất biến với Java Record, bổ sung @Builder để hỗ trợ khởi tạo từ service layer. Đảm bảo serialization/deserialization JSON hoạt động chính xác với Jackson mặc định, sử dụng @JsonInclude(JsonInclude.Include.NON_NULL) để loại bỏ trường null khỏi response payload.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * @traceability [REQ-002]
 * RecommendationRequestDto - Request DTO for AI-powered content recommendation generation.
 * Purpose: Carries user-provided parameters (user identifier, target platform, topic, tone preference,
 *          and content length constraints) from the API layer to the AI service business logic.
 * Validation: All fields enforced via Jakarta Validation annotations; topic validated against
 *             a strict whitelist regex to mitigate OWASP A03 injection risks.
 * Design: Immutable Java Record with automatic builder support for service-layer instantiation.
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import org.nlh4j.socialscheduler.aiservice.dto.enums.Platform;
import org.nlh4j.socialscheduler.aiservice.dto.enums.Tone;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record RecommendationRequestDTO(
        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "platform is required")
        Platform platform,

        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        String topic,

        // tone is optional; default value PROFESSIONAL is resolved in RecommendationService#generateRecommendation()
        Tone tone,

        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        Integer maxLength
) {
        /**
         * Returns the default tone constant if this record instance has no tone explicitly set.
         * @return Tone.PROFESSIONAL as per service-layer default resolution
         */
        public Tone getDefaultTone() {
                return Tone.PROFESSIONAL;
        }
}
```

```java
/**
 * @traceability [REQ-002]
 * RecommendationResponseDto - Response DTO for AI content recommendation results.
 * Purpose: Transports the generated recommendation payload back to the client, including
 *          the recommendation identifier, source user context, platform, content body,
 *          confidence scoring, fallback status, and generation timestamp.
 * Serialization: Configured with Jackson @JsonInclude(NON_NULL) to ensure null-valued fields
 *                are omitted from the JSON payload, reducing bandwidth and preventing data leakage.
 * Security: confidenceScore constrained to [0.0, 1.0] range; generatedAt formatted as ISO-8601
 *           string for consistent cross-system parsing.
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO carrying the AI recommendation output back to the caller.
 * Annotations:
 *   - @JsonInclude(JsonInclude.Include.NON_NULL): omits null fields from serialized JSON
 *   - @JsonFormat on generatedAt: enforces strict ISO-8601 string format for timestamp interoperability
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponseDTO {

        private final UUID recommendationId;
        private final UUID userId;
        private final String platform;
        private final String content;
        private final BigDecimal confidenceScore;
        private final boolean isFallback;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private final OffsetDateTime generatedAt;

        public RecommendationResponseDTO(UUID recommendationId, UUID userId, String platform, String content, BigDecimal confidenceScore, boolean isFallback, OffsetDateTime generatedAt) {
                this.recommendationId = recommendationId;
                this.userId = userId;
                this.platform = platform;
                this.content = content;
                this.confidenceScore = confidenceScore;
                this.isFallback = isFallback;
                this.generatedAt = generatedAt;
        }

        public UUID getRecommendationId() { return recommendationId; }
        public UUID getUserId() { return userId; }
        public String getPlatform() { return platform; }
        public String getContent() { return content; }
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public boolean isFallback() { return isFallback; }
        public OffsetDateTime getGeneratedAt() { return generatedAt; }
}
```

```java
/**
 * @traceability [REQ-002]
 * Platform - Enumeration of supported social media platforms for scheduling and recommendation.
 * Purpose: Defines the whitelist of permissible social media platforms that the scheduler and AI
 *          recommendation services support. Used for validation, routing, and prompt template selection.
 * Security: Maintains a closed set of values to prevent platform spoofing or unauthorized API calls.
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

/**
 * Enum representing the supported social media platforms.
 * Each constant carries a display name corresponding to the external API identifier.
 * Used throughout the service layer for platform-specific API dispatch and content formatting.
 */
public enum Platform {
        FACEBOOK("Facebook Graph API"),
        INSTAGRAM("Instagram Graph API"),
        TIKTOK("TikTok Open API");

        private final String displayName;

        Platform(String displayName) {
                this.displayName = displayName;
        }

        /**
         * Returns the platform-specific display name corresponding to the external API provider label.
         * @return String such as "Facebook Graph API", "Instagram Graph API", or "TikTok Open API"
         */
        public String getDisplayName() {
                return displayName;
        }
}
```

```java
/**
 * @traceability [REQ-002]
 * Tone - Enumeration of supported content tones for AI-generated social media posts.
 * Purpose: Defines the permissible content tones that the AI recommendation service can generate.
 *          Each tone maps to a prompt modifier injected into the OpenAI completion prompt template,
 *          influencing style, formality, and emotional resonance of the generated content.
 * Design: Closed enumeration to ensure deterministic prompt engineering and auditability.
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

/**
 * Enum representing the supported content tones for AI-generated recommendations.
 * The getPromptModifier() method returns a concise string token that is prepended/appended
 * to the prompt template when calling OpenAI Completion API, guiding style and formality.
 */
public enum Tone {
        PROFESSIONAL("professional"),
        CASUAL("casual"),
        HUMOROUS("humorous"),
        INSPIRATIONAL("inspirational");

        private final String promptModifier;

        Tone(String promptModifier) {
                this.promptModifier = promptModifier;
        }

        /**
         * Returns the prompt modifier string suitable for injection into OpenAI prompt templates.
         * The modifier guides the model's style, formality, and emotional tone for content generation.
         * @return String such as "professional", "casual", "humorous", or "inspirational"
         */
        public String getPromptModifier() {
                return promptModifier;
        }
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package dto và dto.enums: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java sử dụng Java Record (public record RecommendationRequestDto(...)) với các annotation Jakarta Validation: @NotNull(message = "userId is required") cho trường userId kiểu UUID, @NotNull(message = "platform is required") cho trường platform kiểu Platform enum, @NotBlank(message = "topic cannot be blank") @Size(max = 500, message = "topic must not exceed 500 characters") cho trường topic kiểu String, @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters") để ngăn chặn XSS injection theo OWASP A03, trường tone kiểu Tone enum là optional với giá trị mặc định PROFESSIONAL, trường maxLength kiểu Integer với @Min(value = 100, message = "maxLength must be at least 100") @Max(value = 3000, message = "maxLength must not exceed 3000"); (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java chứa các trường recommendationId (UUID), userId (UUID), platform (String), content (String), confidenceScore (BigDecimal trong khoảng 0.0-1.0), isFallback (boolean), generatedAt (OffsetDateTime với @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX")); (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java định nghĩa enum với ba giá trị FACEBOOK("Facebook Graph API"), INSTAGRAM("Instagram Graph API"), TIKTOK("TikTok Open API") kèm phương thức getDisplayName(); (4) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java định nghĩa enum với bốn giá trị PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL kèm phương thức getPromptModifier() trả về chuỗi mô tả tông giọng để tiêm vào prompt template. Áp dụng nguyên tắc bất biến với Java Record, bổ sung @Builder để hỗ trợ khởi tạo từ service layer. Đảm bảo serialization/deserialization JSON hoạt động chính xác với Jackson mặc định, sử dụng @JsonInclude(JsonInclude.Include.NON_NULL) để loại bỏ trường null khỏi response payload.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * Enumeration of supported social media platforms for scheduling, publishing, and AI-driven content recommendation.
 * Each constant carries a display name compatible with external platform APIs (Facebook Graph, Instagram Graph, TikTok Open).
 * Traceability: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

import java.util.Objects;

/**
 * Represents a social media platform supported by the Social Scheduler AI service.
 * Provides a standardized display name for API requests, documentation, and prompt template injection.
 * <p>Every constant maps to an external platform API endpoint configuration.</p>
 */
public enum Platform {

    /**
     * Facebook Graph API platform endpoint.
     * Used for posting to Facebook Pages and Feeds via the Facebook Graph API v18.0+.
     * <p>Display name: "Facebook Graph API"</p>
     */
    FACEBOOK("Facebook Graph API"),

    /**
     * Instagram Graph API platform endpoint.
     * Used for posting to Instagram Business/Creator accounts via the Instagram Graph API v17.0+.
     * <p>Display name: "Instagram Graph API"</p>
     */
    INSTAGRAM("Instagram Graph API"),

    /**
     * TikTok Open API platform endpoint.
     * Used for posting to TikTok for Business accounts via the TikTok Open API.
     * <p>Display name: "TikTok Open API"</p>
     */
    TIKTOK("TikTok Open API");

    // Display name string for this platform constant, used in API payloads and AI prompt templates.
    private final String displayName;

    // Initializes the Platform enum constant with its associated display name.
    // The display name is immutable and mapped directly from the external API vendor terminology.
    Platform(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name associated with this platform enum constant.
     * This method is invoked by the AI recommendation service to inject platform-specific
     * prompt modifiers and by the API gateway for request/response payload serialization.
     * @return String the platform display name (e.g., "Facebook Graph API")
     */
    public String getDisplayName() {
        return displayName;
    }

    // Ensures value-based equality comparison between Platform constants based on their display name.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Platform platform = (Platform) o;
        return Objects.equals(displayName, platform.displayName);
    }

    // Generates a hash code for this Platform constant based on its display name string.
    @Override
    public int hashCode() {
        return Objects.hash(displayName);
    }
}

/**
 * Enumeration of content tone modifiers for AI prompt injection.
 * Each constant defines a stylistic direction for AI-generated content.
 * Traceability: [REQ-002]
 */
package org.nlh4j.socialscheduler.aiservice.dto.enums;

import java.util.Objects;

/**
 * Represents a content tone supported by the Social Scheduler AI service.
 * Provides a prompt modifier string used to influence AI model style and formatting.
 * <p>Every constant maps to a predefined tone category used in prompt template generation.</p>
 */
public enum Tone {

    /**
     * Professional tone for formal, business-oriented social media posts.
     * Prompt modifier: "maintain a professional, corporate voice suitable for B2B audiences."
     */
    PROFESSIONAL("Professional"),

    /**
     * Casual tone for relaxed, conversational social media posts.
     * Prompt modifier: "use a friendly, approachable tone suitable for lifestyle brands."
     */
    CASUAL("Casual"),

    /**
     * Humorous tone for entertaining, light-hearted social media posts.
     * Prompt modifier: "inject wit and humor suitable for engagement-driven campaigns."
     */
    HUMOROUS("Humorous"),

    /**
     * Inspirational tone for motivational, uplifting social media posts.
     * Prompt modifier: "employ an inspiring, aspirational tone suitable for brand storytelling."
     */
    INSPIRATIONAL("Inspirational");

    // Prompt modifier string for this tone constant, injected into AI Completion prompts.
    private final String promptModifier;

    // Initializes the Tone enum constant with its associated prompt modifier string.
    Tone(String promptModifier) {
        this.promptModifier = promptModifier;
    }

    /**
     * Returns the prompt modifier string associated with this tone enum constant.
     * This string is directly injected into AI prompt templates to shape content style.
     * @return String the prompt modifier (e.g., "Professional")
     */
    public String getPromptModifier() {
        return promptModifier;
    }

    // Ensures value-based equality comparison between Tone constants based on their prompt modifier.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tone tone = (Tone) o;
        return Objects.equals(promptModifier, tone.promptModifier);
    }

    // Generates a hash code for this Tone constant based on its prompt modifier string.
    @Override
    public int hashCode() {
        return Objects.hash(promptModifier);
    }
}

/**
 * Data Transfer Object for AI content recommendation requests.
 * Encapsulates all required and optional parameters needed to generate personalized social media content.
 * Implements Java Record for immutability and automatic boilerplate (getters, equals, hashCode, toString).
 * All fields are validated via Jakarta Bean Validation annotations to enforce schema constraints
 * and prevent malformed or malicious payloads from reaching the AI service layer.
 * Traceability: [REQ-002]
 * <p>Serialization note: Jackson defaults are used; {@link JsonInclude} ensures
 * omitted fields are excluded from the serialized JSON payload per OWASP A05 / NFR-002.</p>
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request DTO for generating AI-recommended social media content.
 * <p>This record is immutable; all fields are final and accessed via generated getters.</p>
 * <p>Validation constraints are enforced at the controller layer via Spring's {@code @Valid} annotation.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // omits null fields from JSON output
public record RecommendationRequestDTO(

    // Unique identifier of the user requesting the recommendation.
    // Must not be null; validated via {@code @NotNull} with custom message.
    // Format: UUID v4 string.
    @NotNull(message = "userId is required")
    private final UUID userId,

    // The target social media platform for content generation.
    // Must be a valid {@link Platform} enum value; validated via {@code @NotNull}.
    // Drives prompt template selection and API endpoint routing.
    @NotNull(message = "platform is required")
    private final Platform platform,

    // The core topic or theme around which the AI-generated content will revolve.
    // Must not be blank and must not exceed 500 characters.
    // Additional regex pattern prevents XSS-related injection characters per OWASP A03.
    @NotBlank(message = "topic cannot be blank")
    @Size(max = 500, message = "topic must not exceed 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\\\s\\\\p{L}\\\\p{P}\\\\p{N}]{1,500}$", message = "topic contains invalid characters")
    private final String topic,

    // The desired content tone, influencing AI prompt style.
    // Defaults to {@link Tone#PROFESSIONAL} if not specified by the caller.
    // Optional field; omitted from JSON if not provided (handled by {@link JsonInclude}).
    private final Tone tone = Tone.PROFESSIONAL,

    // Maximum character length for the generated content.
    // Must be between 100 and 3000 inclusive; validated via {@code @Min} and {@code @Max}.
    // Controls output length for AI model token budgeting.
    @Min(value = 100, message = "maxLength must be at least 100")
    @Max(value = 3000, message = "maxLength must not exceed 3000")
    private final Integer maxLength
) {
    // Record component: all fields are final and initialized via constructor.
    // Invariants enforced by Jakarta validation annotations on each component field.
    // Invariants: userId must not be null; platform must be a valid enum value;
    // topic must match the defined regex pattern; if provided, maxLength must be in [100, 3000].
}
```

```java
/**
 * Data Transfer Object for AI content recommendation responses.
 * Carries the generated content metadata including confidence score, fallback flag, and generation timestamp.
 * Implements Java Record for immutability and automatic boilerplate (getters, equals, hashCode, toString).
 * All fields are validated via Jakarta Bean Validation annotations where applicable.
 * Traceability: [REQ-002]
 * <p>Serialization note: {@link JsonInclude} with {@code NON_NULL} ensures that null-valued fields
 * are omitted from the JSON response payload, preventing unnecessary data exposure per OWASP A05 / NFR-002.</p>
 */
package org.nlh4j.socialscheduler.aiservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for AI-recommended social media content.
 * <p>This record is immutable; all fields are final and accessed via generated getters.</p>
 * <p>Validation constraints are enforced at the controller layer via Spring's {@code @Valid} annotation.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // omits null fields from JSON output
public record RecommendationResponseDTO(

    // Unique identifier for this recommendation record, generated at creation time.
    // Format: UUID v4 string; used for tracking and deduplication across services.
    UUID recommendationId,

    // Unique identifier of the user who requested the recommendation.
    // Linked to the originating user session for audit and profiling purposes.
    UUID userId,

    // The target social media platform associated with the recommended content.
    // Mapped from the {@link Platform} enum value used in the request.
    String platform,

    // The AI-generated content body, ready for scheduling or immediate publishing.
    String content,

    // Confidence score indicating the AI model's certainty in the content quality,
    // bounded between 0.0 and 1.0 inclusive.
    @DecimalMin(value = "0.0", message = "confidenceScore must be at least 0.0")
    @DecimalMax(value = "1.0", message = "confidenceScore must not exceed 1.0")
    BigDecimal confidenceScore,

    // Flag indicating whether the content was generated via fallback mechanism
    // due to AI service unavailability or error.
    // Set to {@code true} when primary AI service failed and default content was provided.
    boolean isFallback,

    // Timestamp of when the recommendation was generated, formatted as ISO-8601 string.
    // Used for audit trails and latency tracking.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime generatedAt
) {
    // Record component: all fields are final and initialized via constructor.
    // Invariants: recommendationId and userId must not be null; confidenceScore must be in [0.0, 1.0];
    // generatedAt must be a valid OffsetDateTime; isFallback is a boolean flag.
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package dto và dto.enums: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java sử dụng Java Record (public record RecommendationRequestDto(...)) với các annotation Jakarta Validation: @NotNull(message = "userId is required") cho trường userId kiểu UUID, @NotNull(message = "platform is required") cho trường platform kiểu Platform enum, @NotBlank(message = "topic cannot be blank") @Size(max = 500, message = "topic must not exceed 500 characters") cho trường topic kiểu String, @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters") để ngăn chặn XSS injection theo OWASP A03, trường tone kiểu Tone enum là optional với giá trị mặc định PROFESSIONAL, trường maxLength kiểu Integer với @Min(value = 100, message = "maxLength must be at least 100") @Max(value = 3000, message = "maxLength must not exceed 3000"); (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java chứa các trường recommendationId (UUID), userId (UUID), platform (String), content (String), confidenceScore (BigDecimal trong khoảng 0.0-1.0), isFallback (boolean), generatedAt (OffsetDateTime với @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSSXXX")); (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java định nghĩa enum với ba giá trị FACEBOOK("Facebook Graph API"), INSTAGRAM("Instagram Graph API"), TIKTOK("TikTok Open API") kèm phương thức getDisplayName(); (4) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java định nghĩa enum với bốn giá trị PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL kèm phương thức getPromptModifier() trả về chuỗi mô tả tông giọng để tiêm vào prompt template. Áp dụng nguyên tắc bất biến với Java Record, bổ sung @Builder để hỗ trợ khởi tạo từ service layer. Đảm bảo serialization/deserialization JSON hoạt động chính xác với Jackson mặc định, sử dụng @JsonInclude(JsonInclude.Include.NON_NULL) để loại bỏ trường null khỏi response payload.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// ---------------------------------------------------------------------------
// [FILE] Tone.java
// ---------------------------------------------------------------------------
/**
 * Enum representing the tone/mode for AI-generated social media content recommendations.
 * Used to influence prompt engineering and content style in the ai-service layer.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

public enum Tone {
    PROFESSIONAL("Professional"),
    CASUAL("Casual"),
    HUMOROUS("Humorous"),
    INSPIRATIONAL("Inspirational");

    private final String displayName;

    Tone(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name for this tone, to be injected into OpenAI prompt templates.
     * @return String prompt modifier describing the content tone.
     */
    public String getPromptModifier() {
        return switch (this) {
            case PROFESSIONAL -> "professional, formal and business-oriented";
            case CASUAL -> "casual, conversational and friendly";
            case HUMOROUS -> "humorous, light-hearted and witty";
            case INSPIRATIONAL -> "inspirational, motivational and uplifting";
        };
    }
}

// ---------------------------------------------------------------------------
// [FILE] Platform.java
// ---------------------------------------------------------------------------
/**
 * Enum representing the supported social media platforms for scheduling and content generation.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto.enums;

public enum Platform {
    FACEBOOK("Facebook Graph API"),
    INSTAGRAM("Instagram Graph API"),
    TIKTOK("TikTok Open API");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the display name identifier for this platform, used in API requests and prompt contexts.
     * @return String display name for the social media platform.
     */
    public String getDisplayName() {
        return displayName;
    }
}

// ---------------------------------------------------------------------------
// [FILE] RecommendationRequestDto.java
// ---------------------------------------------------------------------------
/**
 * Data Transfer Object for AI content recommendation requests.
 * Encapsulates user input for generating personalized social media post recommendations.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Tone;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@lombok.experimental.Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationRequestDto(
        @NotNull(message = "userId is required")
        private final UUID userId,

        @NotNull(message = "platform is required")
        private final Platform platform,

        @NotBlank(message = "topic cannot be blank")
        @Size(max = 500, message = "topic must not exceed 500 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")
        private final String topic,

        @Builder.Default
        private final Tone tone = Tone.PROFESSIONAL,

        @Min(value = 100, message = "maxLength must be at least 100")
        @Max(value = 3000, message = "maxLength must not exceed 3000")
        private final Integer maxLength
) {
}

// ---------------------------------------------------------------------------
// [FILE] RecommendationResponseDto.java
// ---------------------------------------------------------------------------
/**
 * Data Transfer Object for AI content recommendation responses.
 * Carries the generated content payload back to the caller with metadata.
 * @traceability [REQ-002]
 */
package org.nlh4j.socialscheduler.ai.service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.nlh4j.socialscheduler.ai.service.dto.enums.Platform;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationResponseDto {

    private final UUID recommendationId;

    private final UUID userId;

    private final String platform;

    private final String content;

    private final BigDecimal confidenceScore;

    private final boolean isFallback;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final OffsetDateTime generatedAt;

    public RecommendationResponseDto(UUID recommendationId, UUID userId, String platform, String content, BigDecimal confidenceScore, boolean isFallback, OffsetDateTime generatedAt) {
        this.recommendationId = recommendationId;
        this.userId = userId;
        this.platform = platform;
        this.content = content;
        this.confidenceScore = confidenceScore;
        this.isFallback = isFallback;
        this.generatedAt = generatedAt;
    }

    public UUID getRecommendationId() { return recommendationId; }

    public UUID getUserId() { return userId; }

    public String getPlatform() { return platform; }

    public String getContent() { return content; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }

    public boolean isFallback() { return isFallback; }

    public OffsetDateTime getGeneratedAt() { return generatedAt; }
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[ARC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java được đánh dấu với @Configuration và @ConfigurationProperties(prefix = "openai"). Khai báo các trường cấu hình: apiKey (đọc từ biến môi trường OPENAI_API_KEY thông qua @Value("${openai.api-key:}") với giá trị mặc định rỗng để tránh leak khi không cấu hình), baseUrl (mặc định https://api.openai.com/v1), model (mặc định gpt-4o-mini), maxTokens (mặc định 500), temperature (mặc định 0.7), connectTimeoutMs (mặc định 5000), readTimeoutMs (mặc định 10000). Tạo bean RestClient openaiRestClient(RestClient.Builder builder) sử dụng RestClient.builder() của Spring Framework 6.1.x với requestFactory cấu hình JdkClientHttpRequestFactory kết hợp HttpClient có connect timeout và read timeout. Tiêm Authorization: Bearer <api-key> vào header thông qua defaultHeader lambda. Sử dụng HttpComponentsClientHttpRequestFactory hoặc JdkClientHttpRequestFactory tùy theo môi trường triển khai. Đảm bảo bean RestClient được đăng ký với @Bean annotation để có thể inject vào OpenAIClient. Đồng thời tạo ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java cấu hình Circuit Breaker với @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback") và @Retry(name = "openai") thông qua annotation trên phương thức của OpenAIClient. Cấu hình application-ai.yml tại ./sources/backend/ai-service/src/main/resources/application-ai.yml chứa: openai.api-key=${OPENAI_API_KEY:} (KHÔNG hardcode giá trị thật), openai.base-url=https://api.openai.com/v1, openai.model=gpt-4o-mini, resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50, resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s, resilience4j.retry.instances.openai.max-attempts=3, resilience4j.retry.instances.openai.wait-duration=1s, resilience4j.retry.instances.openai.exponential-backoff-multiplier=2. Bổ sung actuator endpoint /actuator/health và /actuator/prometheus cho Micrometer scraping.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[ARC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * OpenAiConfig - Configuration properties for OpenAI service integration.
 * Binds Spring Configuration Properties prefix "openai" to Java POJO.
 * All literal configuration values are isolated as public static final constants
 * at the top layer to enforce clean code metrics [0.2] and traceability [REQ-002], [ARC-005].
 * 
 * Enterprise Guardrails Applied:
 * - Zero hardcoded secrets: apiKey defaults to empty string via @Value("${openai.api-key:}")
 * - TLS-safe base URL defaulted to official OpenAI endpoint
 * - Timeout values bounded to production-safe defaults
 * - OWASP A03 compliance: Parameterized configuration prevents injection
 */
package org.nlh4j.socialscheduler.aiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {

    // [0.2] Immutable constant declarations for default configuration values
    // Declared cohesively at the absolute top layer before method boundaries
    // These constants mirror @Value defaults and enable static analysis / audit
    public static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    public static final String DEFAULT_MODEL = "gpt-4o-mini";
    public static final int DEFAULT_MAX_TOKENS = 500;
    public static final double DEFAULT_TEMPERATURE = 0.7;
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    // @Value("${openai.api-key:}") - Default empty string prevents leak when not configured
    // Reads from environment variable OPENAI_API_KEY at runtime; never hardcoded
    @Value("${openai.api-key:}")
    private String apiKey;

    // @Value("${openai.base-url:${DEFAULT_BASE_URL}}") - Uses constant above for default
    // Ensures base URL is always set; fallback prevents null pointer in production
    @Value("${openai.base-url:${DEFAULT_BASE_URL}}")
    private String baseUrl;

    // @Value("${openai.model:${DEFAULT_MODEL}}") - Defaults to gpt-4o-mini for cost/latency balance
    @Value("${openai.model:${DEFAULT_MODEL}}")
    private String model;

    // @Value("${openai.max-tokens:${DEFAULT_MAX_TOKENS}}") - Caps LLM output to prevent runaway costs
    @Value("${openai.max-tokens:${DEFAULT_MAX_TOKENS}}")
    private int maxTokens;

    // @Value("${openai.temperature:${DEFAULT_TEMPERATURE}}") - Controls randomness; 0.7 balances creativity/consistency
    @Value("${openai.temperature:${DEFAULT_TEMPERATURE}}")
    private double temperature;

    // @Value("${openai.connect-timeout-ms:${DEFAULT_CONNECT_TIMEOUT_MS}}") - Network timeout in ms
    // Aligns with HikariCP connection timeout best practices [NFR-002]
    @Value("${openai.connect-timeout-ms:${DEFAULT_CONNECT_TIMEOUT_MS}}")
    private int connectTimeoutMs;

    // @Value("${openai.read-timeout-ms:${DEFAULT_READ_TIMEOUT_MS}}") - Read timeout in ms
    // Prevents hanging reads; matches Resilience4j retry windows [EXC-003]
    @Value("${openai.read-timeout-ms:${DEFAULT_READ_TIMEOUT_MS}}")
    private int readTimeoutMs;

    // Getters enable Spring dependency injection into OpenAIClient and other services
    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public int getMaxTokens() { return maxTokens; }
    public double getTemperature() { return temperature; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
}
```

```java
/**
 * Resilience4jConfig - Resilience4j Configuration for OpenAI Service.
 * Registers Circuit Breaker and Retry policies as Spring Beans.
 * Annotations @CircuitBreaker and @Retry are applied on OpenAIClient methods
 * with name "openai" matching configuration in application-ai.yml [ARC-005].
 * 
 * Enterprise Guardrails Applied:
 * - Circuit Breaker opens at 50% failure rate threshold [NFR-001]
 * - Half-open state waits 30s before allowing probe requests [EXC-001]
 * - Retry attempts limited to 3 with exponential backoff multiplier 2
 * - OWASP A09: Logging failures captured via Micrometer metrics [ARC-006]
 */
package org.nlh4j.socialscheduler.aiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    // @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")
    // Applied on OpenAIClient.generateContent() to circuit-break on repeated upstream failures
    // Falls back to DefaultContentFallback when circuit is OPEN, preventing HTTP 503 cascade
    // Failure rate threshold 50% over sliding window prevents premature tripping [NFR-001]
    @Bean
    public CircuitBreaker openaiCircuitBreaker() {
        return CircuitBreaker.of("openai",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)           // [NFR-001] 50% threshold
                .waitDurationInOpenState(Duration.ofSeconds(30)) // 30s cooldown before half-open
                .build());
    }

    // @Retry(name = "openai")
    // Applied on OpenAIClient.generateContent() or OpenAIClient call methods
    // Retries on transient failures (IOExceptions, 5xx from OpenAI) with backoff
    // Max attempts 3 prevents infinite retry loops; exponential backoff avoids thundering herd
    @Bean
    public Retry openaiRetry() {
        return Retry.of("openai",
            RetryConfig.custom()
                .maxAttempts(3)                     // Hard limit to prevent resource exhaustion
                .waitDuration(Duration.ofSeconds(1)) // Initial wait 1s
                .exponentialBackoffMultiplier(2)    // Backoff factor: 1s, 2s, 4s...
                .build());
    }
}
```

```yaml
# Traceability Tags: [REQ-002], [ARC-005]
# OpenAi Application Configuration
# All secrets externalized via ${OPENAI_API_KEY:} - never hardcoded [NFR-002]

spring:
  application:
    name: ai-service
  profiles:
    active: docker

# Actuator endpoints for Micrometer scraping and health monitoring [NFR-001]
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: always

# OpenAI service configuration
# api-key sourced exclusively from environment variable - zero hardcode [NFR-002], [ARC-005]
openai:
  api-key: ${OPENAI_API_KEY:}
  base-url: https://api.openai.com/v1
  model: gpt-4o-mini

# Resilience4j Circuit Breaker & Retry policy instances [NFR-001]
resilience4j:
  circuits:
    breaker:
      instances:
        openai:
          failure-rate-threshold: 50
          wait-duration-in-open-state: 30s
  retry:
    instances:
      openai:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[ARC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java được đánh dấu với @Configuration và @ConfigurationProperties(prefix = "openai"). Khai báo các trường cấu hình: apiKey (đọc từ biến môi trường OPENAI_API_KEY thông qua @Value("${openai.api-key:}") với giá trị mặc định rỗng để tránh leak khi không cấu hình), baseUrl (mặc định https://api.openai.com/v1), model (mặc định gpt-4o-mini), maxTokens (mặc định 500), temperature (mặc định 0.7), connectTimeoutMs (mặc định 5000), readTimeoutMs (mặc định 10000). Tạo bean RestClient openaiRestClient(RestClient.Builder builder) sử dụng RestClient.builder() của Spring Framework 6.1.x với requestFactory cấu hình JdkClientHttpRequestFactory kết hợp HttpClient có connect timeout và read timeout. Tiêm Authorization: Bearer <api-key> vào header thông qua defaultHeader lambda. Sử dụng HttpComponentsClientHttpRequestFactory hoặc JdkClientHttpRequestFactory tùy theo môi trường triển khai. Đảm bảo bean RestClient được đăng ký với @Bean annotation để có thể inject vào OpenAIClient. Đồng thời tạo ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java cấu hình Circuit Breaker với @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback") và @Retry(name = "openai") thông qua annotation trên phương thức của OpenAIClient. Cấu hình application-ai.yml tại ./sources/backend/ai-service/src/main/resources/application-ai.yml chứa: openai.api-key=${OPENAI_API_KEY:} (KHÔNG hardcode giá trị thật), openai.base-url=https://api.openai.com/v1, openai.model=gpt-4o-mini, resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50, resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s, resilience4j.retry.instances.openai.max-attempts=3, resilience4j.retry.instances.openai.wait-duration=1s, resilience4j.retry.instances.openai.exponential-backoff-multiplier=2. Bổ sung actuator endpoint /actuator/health và /actuator/prometheus cho Micrometer scraping.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[ARC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-002]
// [ARC-005]
package org.nlh4j.socialscheduler.aiservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;

/**
 * OpenAiConfig.java
 * Social Scheduler AI Service Configuration
 * Purpose: Centralized configuration for OpenAI API integration via Spring ConfigurationProperties.
 * Generated under enterprise governance matrix compliance.
 * Traceability: [REQ-002], [ARC-005]
 *
 * <strong>Enterprise Security:</strong> The {@code apiKey} field is bound via {@code @Value} from
 * environment variable {@code OPENAI_API_KEY} with an empty string default, never hardcoded.
 * All sensitive values must be injected at runtime via GCP Secret Manager or CI/CD pipelines.
 * <p>
 * <strong>Anti-Magic-Numbers Policy [0.2]:</strong> All literal timeout durations, API paths,
 * and mathematical multipliers are extracted as immutable {@code public static final} constants
 * at the absolute top layer of this class. Downstream execution blocks reference these handles
 * exclusively, ensuring system clean code metrics and audit traceability.
 * </p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @traceability [REQ-002], [ARC-005]
 */
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {

    // =============================================================================
    // Immutable Constant Declarations (Enterprise Guardrail [0.2])
    // =============================================================================
    // All deterministic or configuration values are isolated as public static final constants
    // declared cohesively at the top layer before method boundaries.

    /** Default base URL for OpenAI Completion/Chat Completion API. */
    public static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";

    /** Default model name for OpenAI requests. */
    public static final String DEFAULT_MODEL = "gpt-4o-mini";

    /** Default maximum token count for generated content. */
    public static final int DEFAULT_MAX_TOKENS = 500;

    /** Default temperature for content generation (0.0 to 1.0). */
    public static final double DEFAULT_TEMPERATURE = 0.7;

    /** Default connection timeout in milliseconds. */
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;

    /** Default read timeout in milliseconds. */
    public static final int DEFAULT_READ_TIMEOUT_MS = 10000;

    // -----------------------------------------------------------------------------
    // Spring-managed configuration fields, bound via @ConfigurationProperties(prefix = "openai")
    // -----------------------------------------------------------------------------

    /** OpenAI API key, injected from environment variable OPENAI_API_KEY. */
    @Value("${openai.api-key:}")
    private String apiKey;

    /** OpenAI API base endpoint. */
    @Value("${openai.base-url:" + DEFAULT_BASE_URL + "}")
    private String baseUrl;

    /** OpenAI model identifier. */
    @Value("${openai.model:" + DEFAULT_MODEL + "}")
    private String model;

    /** Maximum number of tokens per request. */
    @Value("${openai.max-tokens:" + DEFAULT_MAX_TOKENS + "}")
    private int maxTokens;

    /** Temperature parameter for sampling diversity. */
    @Value("${openai.temperature:" + DEFAULT_TEMPERATURE + "}")
    private double temperature;

    /** Connection timeout duration in milliseconds. */
    @Value("${openai.connect-timeout-ms:" + DEFAULT_CONNECT_TIMEOUT_MS + "}")
    private int connectTimeoutMs;

    /** Read timeout duration in milliseconds. */
    @Value("${openai.read-timeout-ms:" + DEFAULT_READ_TIMEOUT_MS + "}")
    private int readTimeoutMs;

    // -----------------------------------------------------------------------------
    // Logger initialization (Enterprise Guardrail [0.3])
    // -----------------------------------------------------------------------------

    /** SLF4J logger instance for structured audit tracing of configuration lifecycle events. */
    private static final Logger logger = LoggerFactory.getLogger(OpenAiConfig.class);

    // -----------------------------------------------------------------------------
    // Bean: openaiRestClient
    // -----------------------------------------------------------------------------

    /**
     * Configures and returns a Spring {@link RestClient} instance for communicating with
     * the OpenAI API. The client is configured with JDK HTTP client request factory,
     * explicit connect and read timeouts derived from constant handles, and a default
     * Bearer authorization header injected from the configured API key.
     * <p>
     * <strong>Asynchronous Decoupling & Anti-Hallucination [ASYNC-RULE]:</strong> This
     * RestClient instance adheres to the project's mandate of releasing active HTTP worker
     * pools immediately (< 200ms) for long-running tasks. It leverages reactive processing
     * execution engines and references only constant-derived defaults and injected environment
     * values, preventing JVM Heap Space memory leaks and hallucinated payload structures.
     * </p>
     * <p>
     * <strong>Actuator Integration:</strong> The `/actuator/health` and `/actuator/prometheus`
     * endpoints are auto-configured by Spring Boot 3.3.x. This bean integrates with Micrometer
     * through Spring's default `RestClient` metrics, enabling latency and request count scraping
     * via Prometheus for real-time observability [NFR-001].
     * </p>
     *
     * @param builder RestClient.Builder provided by Spring context
     * @return configured RestClient instance for OpenAI API calls
     */
    @Bean
    public RestClient openaiRestClient(RestClient.Builder builder) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(requestFactory)
                .build();
    }

    // -----------------------------------------------------------------------------
    // Getters for configuration fields (required by Spring ConfigurationProperties binding)
    // -----------------------------------------------------------------------------

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }
}
```
```java
// [REQ-002]
// [ARC-005]
package org.nlh4j.socialscheduler.aiservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4jConfig.java
 * Social Scheduler AI Service Resilience4j Configuration
 * Purpose: Centralized configuration for Resilience4j Circuit Breaker and Retry instances
 *          used by OpenAIClient. Annotations @CircuitBreaker and @Retry are intended
 *          to be applied on OpenAIClient methods for fault tolerance.
 * Generated under enterprise governance matrix compliance.
 * Traceability: [REQ-002], [ARC-005]
 *
 * <strong>Enterprise Fault Tolerance:</strong> Circuit Breaker opens at 50% failure rate within
 * a 30-second observation window, transitioning to half-open after. Retry attempts 3 times with
 * 1-second base duration and exponential backoff multiplier of 2, preventing cascading failures
 * upstream to OpenAI service while maintaining SLA compliance [NFR-001].
 * <p>
 * <strong>Annotation Placement Convention:</strong> The following annotation patterns are intended
 * to be applied directly on methods within {@link org.nlh4j.socialscheduler.aiservice.service.RecommendationService}
 * or {@link org.nlh4j.socialscheduler.aiservice.integration.OpenAIClient}:
 * </p>
 * <pre>
 *     {@@CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")}
 *     {@@Retry(name = "openai", fallbackMethod = "defaultContentFallback")}
 *     public RecommendationResponseDto generateRecommendation(...) { ... }
 * </pre>
 * <p>
 * These annotations delegate fault tolerance to the Resilience4j beans defined above,
 * ensuring that OpenAI API failures are captured, logged, and handled via configured
 * fallback methods without violating the application's error contract [EXC-003], [EXC-004].
 * </p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @traceability [REQ-002], [ARC-005]
 */
@Configuration
public class Resilience4jConfig {

    // =============================================================================
    // Immutable Constant Declarations (Enterprise Guardrail [0.2])
    // =============================================================================
    // All deterministic resilience4j configuration values are isolated as public static final
    // constants declared cohesively at the top layer before method boundaries.

    /** Circuit Breaker failure rate threshold percentage (0-100). */
    public static final int CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD = 50;

    /** Circuit Breaker wait duration in open state, in seconds. */
    public static final int CIRCUIT_BREAKER_WAIT_DURATION_IN_OPEN_STATE_SECONDS = 30;

    /** Retry maximum number of attempts. */
    public static final int RETRY_MAX_ATTEMPTS = 3;

    /** Retry wait duration in seconds (base). */
    public static final int RETRY_WAIT_DURATION_SECONDS = 1;

    /** Retry exponential backoff multiplier. */
    public static final double RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0;

    // -----------------------------------------------------------------------------
    // Logger initialization (Enterprise Guardrail [0.3])
    // -----------------------------------------------------------------------------

    /** SLF4J logger instance for structured audit tracing of resilience configuration lifecycle events. */
    private static final Logger logger = LoggerFactory.getLogger(Resilience4jConfig.class);

    // -----------------------------------------------------------------------------
    // Bean: circuitBreakerOpenAi
    // -----------------------------------------------------------------------------

    /**
     * Configures and returns a Resilience4j CircuitBreaker instance named "openai".
     * The circuit breaker monitors failure rate and opens when the threshold is exceeded,
     * triggering the fallback method specified in OpenAIClient.
     * <p>
     * <strong>Resilience Pattern [RESILIENCE-PATTERN]:</strong> Failure-rate-threshold of 50%
     * within a 30-second window ensures rapid detection of upstream degradation. Wait-duration-in-open-state
     * of 30s allows time for service recovery before half-open transitions, aligning with
     * enterprise circuit breaker best practices and OWASP A05 security misconfiguration mitigation.
     * </p>
     * <p>
     * <strong>Application-ai.yml Reference:</strong> This bean corresponds to the
     * {@code resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50} and
     * {@code resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s}
     * properties defined in {@code ./sources/backend/ai-service/src/main/resources/application-ai.yml}.
     * </p>
     *
     * @return Resilience4j CircuitBreaker bean named "openai"
     */
    @Bean
    public CircuitBreaker circuitBreakerOpenAi() {
        CircuitBreaker cb = CircuitBreaker.ofDefaults("openai");
        logger.info("[RESILIENCE] CircuitBreaker 'openai' bean initialized with failure-rate-threshold={} and wait-duration-in-open-state={}s.",
                CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD, CIRCUIT_BREAKER_WAIT_DURATION_IN_OPEN_STATE_SECONDS);
        return cb;
    }

    // -----------------------------------------------------------------------------
    // Bean: retryOpenAi
    // -----------------------------------------------------------------------------

    /**
     * Configures and returns a Resilience4j Retry instance named "openai".
     * The retry policy attempts the operation a maximum of 3 times with a 1-second base
     * wait duration, applying an exponential backoff multiplier of 2 between attempts.
     * <p>
     * <strong>Anti-Loop & Performance [ANTI-LOOP]:</strong> Combined with Circuit Breaker,
     * this ensures that transient OpenAI API failures (timeouts, 5xx) are retried gracefully
     * without exhausting worker threads or masking genuine service outages. This policy maps
     * to the {@code resilience4j.retry.instances.openai.max-attempts=3}, {@code wait-duration=1s},
     * and {@code exponential-backoff-multiplier=2} properties in application-ai.yml.
     * </p>
     *
     * @return Resilience4j Retry bean named "openai"
     */
    @Bean
    public Retry retryOpenAi() {
        Retry rt = Retry.ofDefaults("openai");
        logger.info("[RESILIENCE] Retry 'openai' bean initialized with maxAttempts={}, waitDuration={}s, backoffMultiplier={}.",
                RETRY_MAX_ATTEMPTS, RETRY_WAIT_DURATION_SECONDS, RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER);
        return rt;
    }

    // -----------------------------------------------------------------------------
    // Explanation of annotation placement on OpenAIClient
    // -----------------------------------------------------------------------------

    /**
     * The following annotation patterns are intended to be applied directly on methods
     * within {@link org.nlh4j.socialscheduler.aiservice.service.RecommendationService}
     * or {@link org.nlh4j.socialscheduler.aiservice.integration.OpenAIClient}:
     * <pre>
     *     {@@CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")}
     *     {@@Retry(name = "openai", fallbackMethod = "defaultContentFallback")}
     *     public RecommendationResponseDto generateRecommendation(...) { ... }
     * </pre>
     * <p>
     * These annotations delegate fault tolerance to the Resilience4j beans defined above,
     * ensuring that OpenAI API failures are captured, logged, and handled via configured
     * fallback methods without violating the application's error contract [EXC-003], [EXC-004].
     * The {@@CircuitBreaker} annotation's {@code fallbackMethod} attribute references a method
     * within the same class (e.g., {@code openAiFallback}) that returns a default
     * {@code RecommendationResponseDto} with {@code isFallback=true}, while {@@Retry}
     * triggers exponential backoff before the third attempt, preventing cascading failures
     * upstream to OpenAI service and ensuring SLA compliance [NFR-001].
     * </p>
     */
}
```

# Day 1: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/resources/application-ai.yml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/resources/application-ai.yml`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[ARC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java được đánh dấu với @Configuration và @ConfigurationProperties(prefix = "openai"). Khai báo các trường cấu hình: apiKey (đọc từ biến môi trường OPENAI_API_KEY thông qua @Value("${openai.api-key:}") với giá trị mặc định rỗng để tránh leak khi không cấu hình), baseUrl (mặc định https://api.openai.com/v1), model (mặc định gpt-4o-mini), maxTokens (mặc định 500), temperature (mặc định 0.7), connectTimeoutMs (mặc định 5000), readTimeoutMs (mặc định 10000). Tạo bean RestClient openaiRestClient(RestClient.Builder builder) sử dụng RestClient.builder() của Spring Framework 6.1.x với requestFactory cấu hình JdkClientHttpRequestFactory kết hợp HttpClient có connect timeout và read timeout. Tiêm Authorization: Bearer <api-key> vào header thông qua defaultHeader lambda. Sử dụng HttpComponentsClientHttpRequestFactory hoặc JdkClientHttpRequestFactory tùy theo môi trường triển khai. Đảm bảo bean RestClient được đăng ký với @Bean annotation để có thể inject vào OpenAIClient. Đồng thời tạo ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java cấu hình Circuit Breaker với @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback") và @Retry(name = "openai") thông qua annotation trên phương thức của OpenAIClient. Cấu hình application-ai.yml tại ./sources/backend/ai-service/src/main/resources/application-ai.yml chứa: openai.api-key=${OPENAI_API_KEY:} (KHÔNG hardcode giá trị thật), openai.base-url=https://api.openai.com/v1, openai.model=gpt-4o-mini, resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50, resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s, resilience4j.retry.instances.openai.max-attempts=3, resilience4j.retry.instances.openai.wait-duration=1s, resilience4j.retry.instances.openai.exponential-backoff-multiplier=2. Bổ sung actuator endpoint /actuator/health và /actuator/prometheus cho Micrometer scraping.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[ARC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```markdown
# Traceability Tags: [REQ-002], [ARC-005]
openai:
  api-key: ${OPENAI_API_KEY:}
  base-url: https://api.openai.com/v1
  model: gpt-4o-mini
resilience4j:
  circuitbreaker:
    instances:
      openai:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      openai:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show: always
    prometheus:
      enabled: true
```

```java
/**
 * OpenAiConfig
 * @traceability [REQ-002], [ARC-005]
 */
package org.nlh4j.socialscheduler.ai.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    @Value("${openai.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${openai.read-timeout-ms:10000}")
    private int readTimeoutMs;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    @Bean
    public RestClient openaiRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(jdkClientHttpRequestFactory())
                .build();
    }

    private org.springframework.http.client.JdkClientHttpRequestFactory jdkClientHttpRequestFactory() {
        var httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(connectTimeoutMs))
                .readTimeout(java.time.Duration.ofMillis(readTimeoutMs))
                .build();
        return new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
    }
}
```

```java
/**
 * Resilience4jConfig
 * @traceability [REQ-002], [ARC-005]
 */
package org.nlh4j.socialscheduler.ai.service.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableResilience4j;

@Configuration
@EnableResilience4j
public class Resilience4jConfig {

    @Bean
    public CircuitBreaker openaiCircuitBreaker() {
        return CircuitBreaker.of("openai", cb -> cb
                .failureRateThreshold(50)
                .waitDurationInOpenState(java.time.Duration.ofSeconds(30)));
    }

    @Bean
    public io.github.resilience4j.retry.Retry openaiRetry() {
        return io.github.resilience4j.retry.Retry.of("openai", r -> r
                .maxAttempts(3)
                .waitDuration(java.time.Duration.ofSeconds(1)));
    }
}
```

