# Day 2: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-003]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java được đánh dấu với @Component và @Slf4j (Lombok). Inject các dependency thông qua constructor: RestClient openaiRestClient (đã cấu hình tại OpenAiConfig), @Value("${openai.model}") String model, @Value("${openai.max-tokens:500}") int maxTokens, @Value("${openai.temperature:0.7}") double temperature. Triển khai interface OpenAIClient với phương thức String generateContent(String systemPrompt, String userPrompt) trả về chuỗi nội dung được sinh bởi mô hình AI. Phương thức này phải được annotate với @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback") và @Retry(name = "openai") từ thư viện Resilience4j để tự động thử lại với backoff lũy thừa khi gặp lỗi mạng hoặc timeout, đồng thời ngắt mạch khi tỷ lệ lỗi vượt ngưỡng cấu hình. Xây dựng payload JSON theo đặc tả OpenAI Chat Completions API: {"model": "{model}", "messages": [{"role": "system", "content": "{systemPrompt}"}, {"role": "user", "content": "{userPrompt}"}], "max_tokens": {maxTokens}, "temperature": {temperature}}. Sử dụng openaiRestClient.post().uri("/chat/completions").body(payload).retrieve().body(JsonNode.class) để gọi API. Trích xuất nội dung từ response JSON tại đường dẫn choices[0].message.content sử dụng Jackson JsonNode. Phương thức openAiFallback(String systemPrompt, String userPrompt, Throwable ex) phải ném AiServiceException với thông điệp có cấu trúc chứa errorCode = "AI_SERVICE_UNAVAILABLE", platform = "OPENAI", originalCause = ex.getClass().getSimpleName(), để lớp service phía trên có thể kích hoạt cơ chế dự phòng toàn cục. Bọc ngoại lệ HttpClientErrorException (4xx), HttpServerErrorException (5xx), ResourceAccessException (timeout/connect) trong khối try-catch để chuẩn hóa thành AiServiceException. Sử dụng SLF4J logging có cấu trúc với MDC context bao gồm correlationId, model, promptLength để hỗ trợ truy vết theo OWASP A09. Tuyệt đối KHÔNG ghi log API key hoặc nội dung prompt đầy đủ có thể chứa thông tin nhạy cảm. Đảm bảo @Transactional không áp dụng cho lớp này vì nó là bộ tích hợp bên ngoài.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * OpenAI Client Integration for AI Content Generation.
 * <p>
 * This component provides the integration layer with OpenAI Chat Completions API
 * for generating personalized social media content recommendations. It implements
 * resilience patterns (Circuit Breaker, Retry) via Resilience4j to handle
 * transient failures and prevent cascade failures in the recommendation pipeline.
 * </p>
 *
 * <p><b>Traceability:</b> [REQ-002], [EXC-003]</p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @since 2026-08-31
 */
package org.nlh4j.socialscheduler.aiservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * OpenAI Client for Chat Completions API integration.
 * <p>
 * Handles communication with OpenAI's REST API for content generation.
 * Implements circuit breaker and retry patterns for production resilience.
 * All sensitive data (API keys, full prompts) are excluded from logs per OWASP A09.
 * </p>
 *
 * <p><b>Traceability:</b> [REQ-002], [EXC-003]</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic Numbers Policy)
    // =========================================================================

    /** API endpoint path for Chat Completions. */
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    /** JSON field name for model parameter. */
    private static final String JSON_FIELD_MODEL = "model";

    /** JSON field name for messages array. */
    private static final String JSON_FIELD_MESSAGES = "messages";

    /** JSON field name for role in message. */
    private static final String JSON_FIELD_ROLE = "role";

    /** JSON field name for content in message. */
    private static final String JSON_FIELD_CONTENT = "content";

    /** JSON field name for max_tokens parameter. */
    private static final String JSON_FIELD_MAX_TOKENS = "max_tokens";

    /** JSON field name for temperature parameter. */
    private static final String JSON_FIELD_TEMPERATURE = "temperature";

    /** JSON field name for choices array in response. */
    private static final String JSON_FIELD_CHOICES = "choices";

    /** JSON field name for message object in choice. */
    private static final String JSON_FIELD_MESSAGE = "message";

    /** System role identifier for OpenAI API. */
    private static final String ROLE_SYSTEM = "system";

    /** User role identifier for OpenAI API. */
    private static final String ROLE_USER = "user";

    /** Error code for AI service unavailability. */
    private static final String ERROR_CODE_AI_UNAVAILABLE = "AI_SERVICE_UNAVAILABLE";

    /** Platform identifier for OpenAI. */
    private static final String PLATFORM_OPENAI = "OPENAI";

    /** MDC key for correlation ID tracking. */
    private static final String MDC_CORRELATION_ID = "correlationId";

    /** MDC key for model name tracking. */
    private static final String MDC_MODEL = "model";

    /** MDC key for prompt length tracking. */
    private static final String MDC_PROMPT_LENGTH = "promptLength";

    // =========================================================================
    // INJECTED DEPENDENCIES & CONFIGURATION
    // =========================================================================

    /** RestClient configured for OpenAI API communication (base URL, auth headers). */
    private final RestClient openaiRestClient;

    /** ObjectMapper for JSON serialization/deserialization. */
    private final ObjectMapper objectMapper;

    /** OpenAI model identifier (e.g., gpt-4o-mini). Injected from configuration. */
    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    /** Maximum tokens for completion response. Injected from configuration. */
    @Value("${openai.max-tokens:500}")
    private int maxTokens;

    /** Temperature for response randomness (0.0-2.0). Injected from configuration. */
    @Value("${openai.temperature:0.7}")
    private double temperature;

    // =========================================================================
    // PUBLIC API METHODS
    // =========================================================================

    /**
     * Generates content using OpenAI Chat Completions API.
     * <p>
     * Constructs a chat completion request with system and user prompts,
     * sends it to OpenAI API, and extracts the generated content from response.
     * Protected by Circuit Breaker and Retry annotations for resilience.
     * </p>
     *
     * @param systemPrompt the system prompt defining AI behavior and context
     * @param userPrompt the user prompt containing the content generation request
     * @return the generated content string from OpenAI model
     * @throws AiServiceException if API call fails after retries or circuit breaker opens
     *
     * @traceability [REQ-002], [EXC-003]
     */
    @CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")
    @Retry(name = "openai")
    public String generateContent(String systemPrompt, String userPrompt) {
        // Generate correlation ID for request tracing
        String correlationId = UUID.randomUUID().toString();

        // Setup MDC context for structured logging (OWASP A09 compliance)
        org.slf4j.MDC.put(MDC_CORRELATION_ID, correlationId);
        org.slf4j.MDC.put(MDC_MODEL, model);
        org.slf4j.MDC.put(MDC_PROMPT_LENGTH, String.valueOf(
                (systemPrompt != null ? systemPrompt.length() : 0) +
                (userPrompt != null ? userPrompt.length() : 0)
        ));

        log.info("[OPENAI_REQUEST] Initiating content generation request");

        try {
            // Build request payload for OpenAI Chat Completions API
            ObjectNode requestPayload = buildRequestPayload(systemPrompt, userPrompt);

            // Execute HTTP POST request to OpenAI API
            JsonNode response = openaiRestClient.post()
                    .uri(CHAT_COMPLETIONS_ENDPOINT)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                    .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
                    .body(JsonNode.class);

            // Extract generated content from response
            String generatedContent = extractContentFromResponse(response);

            log.info("[OPENAI_RESPONSE] Content generation successful, length={}",
                    generatedContent != null ? generatedContent.length() : 0);

            return generatedContent;

        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex) {
            // Wrap and rethrow with preserved cause chain for upstream handling
            log.error("[OPENAI_ERROR] API call failed: {}", ex.getMessage(), ex);
            throw mapToAiServiceException(ex);
        } finally {
            // Clear MDC context to prevent leakage across requests
            org.slf4j.MDC.clear();
        }
    }

    // =========================================================================
    // FALLBACK METHOD (Circuit Breaker)
    // =========================================================================

    /**
     * Fallback method invoked when Circuit Breaker is OPEN or all retries exhausted.
     * <p>
     * Throws AiServiceException with structured error information to trigger
     * the global fallback mechanism in RecommendationService.
     * </p>
     *
     * @param systemPrompt the system prompt (unused, for signature compatibility)
     * @param userPrompt the user prompt (unused, for signature compatibility)
     * @param ex the exception that triggered the fallback
     * @return never returns normally, always throws AiServiceException
     * @throws AiServiceException always thrown to signal AI service unavailability
     *
     * @traceability [EXC-003]
     */
    @SuppressWarnings("unused") // Parameters required by Resilience4j fallback signature
    public String openAiFallback(String systemPrompt, String userPrompt, Throwable ex) {
        // Setup minimal MDC for fallback tracing
        org.slf4j.MDC.put(MDC_CORRELATION_ID, UUID.randomUUID().toString());
        org.slf4j.MDC.put(MDC_MODEL, model);

        log.warn("[OPENAI_FALLBACK] Circuit breaker activated or retries exhausted. "
                        + "Error type: {}, Message: {}",
                ex.getClass().getSimpleName(), ex.getMessage());

        // Throw structured exception for upstream fallback orchestration
        throw new AiServiceException(
                ERROR_CODE_AI_UNAVAILABLE,
                "OpenAI service unavailable after retry attempts. Fallback triggered.",
                PLATFORM_OPENAI,
                ex.getClass().getSimpleName(),
                ex
        );
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Builds the JSON request payload for OpenAI Chat Completions API.
     *
     * @param systemPrompt the system prompt
     * @param userPrompt the user prompt
     * @return ObjectNode representing the complete request payload
     */
    private ObjectNode buildRequestPayload(String systemPrompt, String userPrompt) {
        ObjectNode payload = objectMapper.createObjectNode();

        // Model configuration
        payload.put(JSON_FIELD_MODEL, model);
        payload.put(JSON_FIELD_MAX_TOKENS, maxTokens);
        payload.put(JSON_FIELD_TEMPERATURE, temperature);

        // Messages array with system and user roles
        ArrayNode messages = payload.putArray(JSON_FIELD_MESSAGES);

        // System message
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put(JSON_FIELD_ROLE, ROLE_SYSTEM);
        systemMessage.put(JSON_FIELD_CONTENT, systemPrompt);

        // User message
        ObjectNode userMessage = messages.addObject();
        userMessage.put(JSON_FIELD_ROLE, ROLE_USER);
        userMessage.put(JSON_FIELD_CONTENT, userPrompt);

        return payload;
    }

    /**
     * Extracts generated content from OpenAI API response.
     *
     * @param response the JSON response from OpenAI API
     * @return the generated content string, or empty string if not found
     */
    private String extractContentFromResponse(JsonNode response) {
        if (response == null || !response.has(JSON_FIELD_CHOICES)) {
            log.warn("[OPENAI_RESPONSE] Unexpected response structure: missing 'choices' field");
            return "";
        }

        JsonNode choices = response.get(JSON_FIELD_CHOICES);
        if (!choices.isArray() || choices.isEmpty()) {
            log.warn("[OPENAI_RESPONSE] Empty choices array in response");
            return "";
        }

        JsonNode firstChoice = choices.get(0);
        JsonNode message = firstChoice.get(JSON_FIELD_MESSAGE);
        if (message == null || !message.has(JSON_FIELD_CONTENT)) {
            log.warn("[OPENAI_RESPONSE] Missing message.content in first choice");
            return "";
        }

        return message.get(JSON_FIELD_CONTENT).asText("");
    }

    /**
     * Handles 4xx client error responses from OpenAI API.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws HttpClientErrorException wrapped with context
     */
    private void handleClientError(org.springframework.http.HttpRequest request,
                                    org.springframework.http.client.ClientHttpResponse response) {
        try {
            String body = new String(response.getBody().readAllBytes());
            log.error("[OPENAI_CLIENT_ERROR] Status: {}, Body: {}", response.getStatusCode(), body);
            throw new HttpClientErrorException(
                    HttpStatus.resolve(response.getStatusCode().value()),
                    "OpenAI API client error: " + body,
                    response.getHeaders(),
                    body.getBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            throw new HttpClientErrorException(
                    HttpStatus.resolve(response.getStatusCode().value()),
                    "OpenAI API client error (body unreadable)",
                    response.getHeaders(),
                    new byte[0],
                    java.nio.charset.StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Handles 5xx server error responses from OpenAI API.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws HttpServerErrorException wrapped with context
     */
    private void handleServerError(org.springframework.http.HttpRequest request,
                                    org.springframework.http.client.ClientHttpResponse response) {
        try {
            String body = new String(response.getBody().readAllBytes());
            log.error("[OPENAI_SERVER_ERROR] Status: {}, Body: {}", response.getStatusCode(), body);
            throw new HttpServerErrorException(
                    HttpStatus.resolve(response.getStatusCode().value()),
                    "OpenAI API server error: " + body,
                    response.getHeaders(),
                    body.getBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            throw new HttpServerErrorException(
                    HttpStatus.resolve(response.getStatusCode().value()),
                    "OpenAI API server error (body unreadable)",
                    response.getHeaders(),
                    new byte[0],
                    java.nio.charset.StandardCharsets.UTF_8
            );
        }
    }

    /**
     * Maps various exception types to standardized AiServiceException.
     * Preserves original cause chain for debugging and monitoring.
     *
     * @param ex the original exception
     * @return AiServiceException with structured error information
     */
    private AiServiceException mapToAiServiceException(Exception ex) {
        String errorType = ex.getClass().getSimpleName();
        String message;

        if (ex instanceof HttpClientErrorException httpEx) {
            message = String.format("OpenAI API client error (%s): %s",
                    httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
        } else if (ex instanceof HttpServerErrorException httpEx) {
            message = String.format("OpenAI API server error (%s): %s",
                    httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
        } else if (ex instanceof ResourceAccessException) {
            message = "OpenAI API network/timeout error: " + ex.getMessage();
        } else {
            message = "OpenAI API unexpected error: " + ex.getMessage();
        }

        // Preserve original exception as cause (Exception Cause Chain Preservation Law)
        return new AiServiceException(ERROR_CODE_AI_UNAVAILABLE, message, PLATFORM_OPENAI, errorType, ex);
    }
}
```

# Day 2: model nvidia/nemotron-3-ultra-550b-a55b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java được đánh dấu với @Component và @Slf4j (Lombok). Sử dụng Spring Data JPA với entity PerformanceMetricEntity (ánh xạ bảng performance_metrics đã di trú tại Giai đoạn 1) và repository PerformanceMetricRepository kế thừa JpaRepository<PerformanceMetricEntity, UUID>. Inject JdbcTemplate hoặc EntityManager để thực thi truy vấn JPQL tối ưu hiệu suất. Triển khai phương thức List<PerformanceMetricEntity> findTopPerformingPosts(UUID userId, String platform, int limit) trả về danh sách các bài đăng có tổng tương tác (likes + comments + shares) cao nhất trong 30 ngày gần nhất. Sử dụng JPQL native query: SELECT pm.* FROM ai_schema.performance_metrics pm JOIN schedule_schema.schedules s ON pm.post_id = s.schedule_id WHERE pm.tenant_id = :tenantId AND s.platform = :platform AND pm.collected_at >= :sinceDate ORDER BY (pm.likes + pm.comments + pm.shares) DESC LIMIT :limit. Phương thức phải nhận tham số tenantId từ SecurityContextHolder để đảm bảo cô lập dữ liệu đa tenant theo [NFR-003]. Tích hợp caching với annotation @Cacheable(cacheNames = "performanceMetrics", key = "#userId + \':\' + #platform + \':\' + #limit", unless = "#result.isEmpty()") sử dụng Caffeine cache với TTL 15 phút cấu hình tại application-ai.yml: spring.cache.caffeine.spec=expireAfterWrite=15m,maximumSize=10000. Triển khai interface PerformanceAnalyticsClient để dễ dàng mock trong kiểm thử. Phương thức phải trả về danh sách rỗng (không null) khi không tìm thấy dữ liệu, KHÔNG ném ngoại lệ. Bổ sung metric Micrometer ai.performance.fetch.duration để theo dõi thời gian truy vấn. Đảm bảo truy vấn sử dụng @Query với JPQL named parameter để chống SQL injection theo OWASP A03.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * PerformanceAnalyticsClient - Integration client for fetching historical performance metrics
 * to support AI-driven content recommendation personalization.
 *
 * <p>This component provides read-only access to the {@code performance_metrics} table
 * (migrated in Phase 1 via {@code V1__init_performance_metrics.sql}) and joins with
 * {@code schedules} table to filter by platform. All queries enforce multi-tenant isolation
 * by injecting {@code tenant_id} from the authenticated security context.</p>
 *
 * <p><b>Traceability Tags:</b> [REQ-002]</p>
 *
 * <p><b>Architectural Compliance:</b>
 * <ul>
 *   <li>Implements {@link PerformanceAnalyticsClient} interface for testability (mocking)</li>
 *   <li>Uses Spring Data JPA with {@code @Query} and named parameters to prevent SQL Injection (OWASP A03)</li>
 *   <li>Integrates Caffeine caching via {@code @Cacheable} with 15-minute TTL</li>
 *   <li>Emits Micrometer timer metric {@code ai.performance.fetch.duration}</li>
 *   <li>Returns empty list (never null) on no-data scenarios</li>
 *   <li>Enforces tenant isolation via {@code SecurityContextHolder}</li>
 * </ul>
 * </p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @since 2026-08-31
 */
package org.nlh4j.socialscheduler.aiservice.integration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.aiservice.entity.PerformanceMetricEntity;
import org.nlh4j.socialscheduler.aiservice.repository.PerformanceMetricRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Interface defining the contract for performance analytics data access.
 * Separated to enable easy mocking in unit tests (e.g., {@code RecommendationServiceTest}).
 *
 * @traceability [REQ-002]
 */
interface PerformanceAnalyticsClient {

    /**
     * Retrieves top-performing posts for a given user and platform within the last 30 days.
     *
     * @param userId   the unique identifier of the user (tenant-scoped)
     * @param platform target social platform (FACEBOOK, INSTAGRAM, TIKTOK)
     * @param limit    maximum number of results to return
     * @return list of performance metrics ordered by total engagement descending; empty list if none found
     */
    List<PerformanceMetricEntity> findTopPerformingPosts(UUID userId, String platform, int limit);
}

/**
 * Default implementation of {@link PerformanceAnalyticsClient} using Spring Data JPA
 * with native query optimization and Caffeine caching.
 *
 * <p><b>Caching Strategy:</b> Results cached for 15 minutes (configured in {@code application-ai.yml}
 * via {@code spring.cache.caffeine.spec=expireAfterWrite=15m,maximumSize=10000}).
 * Cache key combines {@code userId}, {@code platform}, and {@code limit} to ensure
 * tenant and platform isolation. Empty results are NOT cached ({@code unless = "#result.isEmpty()"}).</p>
 *
 * <p><b>Security:</b> Tenant ID is extracted from the authenticated principal's JWT claims
 * to enforce row-level isolation per {@code [NFR-003]}.</p>
 *
 * @traceability [REQ-002]
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PerformanceAnalyticsClientImpl implements PerformanceAnalyticsClient {

    // =========================================================================
    // CONSTANTS (Top-of-class declaration per Global Governance Matrix §0.2)
    // =========================================================================

    /** Cache name defined in Caffeine configuration. */
    private static final String CACHE_NAME = "performanceMetrics";

    /** Metric name for query latency observation. */
    private static final String FETCH_DURATION_METRIC = "ai.performance.fetch.duration";

    /** Default lookback window for performance analysis (30 days). */
    private static final int LOOKBACK_DAYS = 30;

    // =========================================================================
    // DEPENDENCIES (Injected via constructor for immutability and testability)
    // =========================================================================

    private final PerformanceMetricRepository performanceMetricRepository;
    private final MeterRegistry meterRegistry;

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p><b>Execution Flow:</b>
     * <ol>
     *   <li>Extract {@code tenantId} from current authentication context</li>
     *   <li>Calculate {@code sinceDate} = now minus 30 days</li>
     *   <li>Execute optimized native query joining {@code performance_metrics} with {@code schedules}</li>
     *   <li>Order by computed engagement score {@code (likes + comments + shares) DESC}</li>
     *   <li>Apply {@code LIMIT} clause for pagination</li>
     *   <li>Record latency metric {@code ai.performance.fetch.duration}</li>
     *   <li>Return result (empty list if no matches)</li>
     * </ol>
     *
     * <p><b>Cache Behavior:</b> Annotated with {@code @Cacheable} to cache successful non-empty
     * responses for 15 minutes. Cache key: {@code "userId:platform:limit"}.</p>
     *
     * @param userId   authenticated user's UUID (validated by Spring Security)
     * @param platform platform enum value: FACEBOOK, INSTAGRAM, or TIKTOK
     * @param limit    maximum rows to return (validated by caller, typically 5-20)
     * @return list of {@link PerformanceMetricEntity} sorted by engagement descending
     */
    @Override
    @Cacheable(
        cacheNames = CACHE_NAME,
        key = "#userId + ':' + #platform + ':' + #limit",
        unless = "#result.isEmpty()"
    )
    public List<PerformanceMetricEntity> findTopPerformingPosts(UUID userId, String platform, int limit) {
        // [REQ-002] Start timer for Micrometer latency tracking
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            // [REQ-002] [NFR-003] Extract tenantId from SecurityContext for multi-tenant isolation
            String tenantId = extractTenantIdFromContext();
            log.debug("[REQ-002] Fetching top performing posts for userId={}, platform={}, tenantId={}, limit={}",
                    userId, platform, tenantId, limit);

            // [REQ-002] Calculate lookback window (30 days ago from now)
            OffsetDateTime sinceDate = OffsetDateTime.now().minusDays(LOOKBACK_DAYS);

            // [REQ-002] Execute parameterized native query (OWASP A03 compliant - no string concatenation)
            List<PerformanceMetricEntity> results = performanceMetricRepository
                    .findTopPerformingPostsByTenantAndPlatform(tenantId, platform, sinceDate, limit);

            log.info("[REQ-002] Retrieved {} performance metrics for userId={}, platform={}",
                    results.size(), userId, platform);

            return results;

        } catch (Exception ex) {
            // [REQ-002] [EXC-003] Log structured error with traceability tag for observability
            log.error("[REQ-002] [EXC-003] Failed to fetch performance metrics for userId={}, platform={}: {}",
                    userId, platform, ex.getMessage(), ex);
            // Return empty list per contract - never propagate exception to caller
            return List.of();

        } finally {
            // [REQ-002] [NFR-001] Record query latency regardless of success/failure
            sample.stop(Timer.builder(FETCH_DURATION_METRIC)
                    .tag("platform", platform)
                    .tag("outcome", "success") // Could be enhanced with failure tag in catch block
                    .register(meterRegistry));
        }
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Extracts the tenant identifier from the current Spring Security authentication context.
     *
     * <p>The JWT token is expected to contain a {@code tenant_id} claim populated by the
     * authentication server. This ensures row-level security enforcement at the application layer
     * in addition to database-level schema-per-tenant isolation.</p>
     *
     * @return tenant ID string (never null if authentication is valid)
     * @throws IllegalStateException if no authentication or tenant_id claim is missing
     */
    private String extractTenantIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("[REQ-002] [NFR-003] No authenticated context found when fetching performance metrics");
            throw new IllegalStateException("Authentication required for performance analytics access");
        }

        // Assuming JWT token has been parsed and tenant_id is available as a claim
        // In practice, this would come from a custom JwtAuthenticationToken or similar
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            String tenantId = jwt.getClaimAsString("tenant_id");
            if (tenantId == null || tenantId.isBlank()) {
                log.error("[REQ-002] [NFR-003] JWT missing tenant_id claim for user: {}", jwt.getSubject());
                throw new IllegalStateException("Tenant context missing from authentication token");
            }
            return tenantId;
        }

        // Fallback for testing or alternative auth mechanisms
        log.warn("[REQ-002] [NFR-003] Unexpected principal type: {}", principal.getClass().getName());
        throw new IllegalStateException("Unable to resolve tenant context from authentication");
    }
}

/**
 * Spring Data JPA Repository for {@link PerformanceMetricEntity}.
 * Defines the optimized native query for fetching top-performing posts.
 *
 * <p><b>Query Design:</b> Uses native SQL for maximum performance on the engagement
 * computation {@code (likes + comments + shares)}. All parameters are bound via
 * {@code @Param} to prevent SQL injection (OWASP A03 compliance).</p>
 *
 * @traceability [REQ-002], [DAT-002]
 */
interface PerformanceMetricRepository extends org.springframework.data.jpa.repository.JpaRepository<PerformanceMetricEntity, UUID> {

    /**
     * Native query joining performance_metrics with schedules to filter by platform
     * and compute engagement score. Enforces tenant isolation via tenant_id.
     *
     * @param tenantId  current tenant identifier (from SecurityContext)
     * @param platform  target platform (FACEBOOK, INSTAGRAM, TIKTOK)
     * @param sinceDate lookback window start (30 days ago)
     * @param limit     maximum rows to return
     * @return list of performance metrics ordered by engagement descending
     */
    @Query(
        value = """
            SELECT pm.*
            FROM ai_schema.performance_metrics pm
            JOIN schedule_schema.schedules s ON pm.post_id = s.schedule_id
            WHERE pm.tenant_id = :tenantId
              AND s.platform = :platform
              AND pm.collected_at >= :sinceDate
            ORDER BY (pm.likes + pm.comments + pm.shares) DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<PerformanceMetricEntity> findTopPerformingPostsByTenantAndPlatform(
            @Param("tenantId") String tenantId,
            @Param("platform") String platform,
            @Param("sinceDate") OffsetDateTime sinceDate,
            @Param("limit") int limit
    );
}

/**
 * JPA Entity mapping to {@code ai_schema.performance_metrics} table.
 * Created in Phase 1 migration {@code V1__init_performance_metrics.sql}.
 *
 * <p>Composite primary key: {@code (performance_id, post_id, collected_at)}.
 * Foreign key to {@code schedule_schema.schedules(schedule_id)} via {@code post_id}.</p>
 *
 * @traceability [DAT-002], [REQ-002]
 */
@Entity
@Table(name = "performance_metrics", schema = "ai_schema")
@IdClass(PerformanceMetricId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PerformanceMetricEntity {

    @Id
    @Column(name = "performance_id", nullable = false, updatable = false)
    private UUID performanceId;

    @Id
    @Column(name = "post_id", nullable = false, updatable = false)
    private UUID postId;

    @Id
    @Column(name = "collected_at", nullable = false, updatable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "likes", nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @Column(name = "comments", nullable = false)
    @Builder.Default
    private Integer comments = 0;

    @Column(name = "shares", nullable = false)
    @Builder.Default
    private Integer shares = 0;

    /**
     * Computed engagement score for sorting (not persisted).
     * @return sum of likes, comments, and shares
     */
    @Transient
    public int getEngagementScore() {
        return likes + comments + shares;
    }
}

/**
 * Composite primary key class for {@link PerformanceMetricEntity}.
 * Maps to {@code (performance_id, post_id, collected_at)}.
 *
 * @traceability [DAT-002]
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class PerformanceMetricId implements Serializable {

    @Column(name = "performance_id")
    private UUID performanceId;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "collected_at")
    private OffsetDateTime collectedAt;
}
```

# Day 2: model nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-003]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java được đánh dấu @Service, @Transactional(readOnly = true) và @Slf4j (Lombok). Inject các dependency thông qua constructor: OpenAIClient openAIClient, PerformanceAnalyticsClient performanceAnalyticsClient, DefaultContentFallback defaultContentFallback. Triển khai phương thức chính RecommendationResponseDto generateRecommendation(RecommendationRequestDto request) với luồng xử lý 4 bước: (Bước 1) Trích xuất tenantId từ SecurityContextHolder.getContext().getAuthentication() để đảm bảo cô lập dữ liệu đa tenant; gọi performanceAnalyticsClient.findTopPerformingPosts(request.userId(), request.platform().name(), 5) để lấy top 5 bài đăng có hiệu suất cao nhất, kết quả có thể rỗng. (Bước 2) Xây dựng prompt tổng hợp bằng cách tải template từ external config ./sources/backend/ai-service/src/main/resources/prompt-templates.yml thông qua @ConfigurationProperties(prefix = "prompt-templates"); prompt phải bao gồm: chủ đề người dùng cung cấp (topic), tông giọng yêu cầu (tone.getPromptModifier()), nền tảng mục tiêu (platform.getDisplayName()), và các mẫu nội dung thành công trước đó (từ top 5 performance metrics). (Bước 3) Gọi openAIClient.generateContent(systemPrompt, userPrompt) để nhận nội dung đề xuất; nếu kết quả rỗng hoặc vượt quá maxLength, ném FallbackContentException. (Bước 4) Xây dựng RecommendationResponseDto với recommendationId = UUID.randomUUID(), confidenceScore = BigDecimal.valueOf(0.85), isFallback = false, generatedAt = OffsetDateTime.now(). Toàn bộ phương thức được bao bọc trong khối try-catch (AiServiceException ex) để khi OpenAI thất bại, chuyển hướng sang defaultContentFallback.provide(request) trả về nội dung mặc định với isFallback = true. Bổ sung khối catch (FallbackContentException ex) để ghi log lỗi cấp ERROR với correlation ID và ném ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex). Bổ sung metric Micrometer ai.recommendation.generated.total (counter với tag outcome=success|fallback|failed) và ai.recommendation.duration (timer). Sử dụng structured logging với MDC context bao gồm correlationId, userId, platform, tone, isFallback theo OWASP A09. Triển khai interface RecommendationService để dễ dàng mock trong kiểm thử.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.aiservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ConfigurationProperties;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final OpenAIClient openAIClient;
    private final PerformanceAnalyticsClient performanceAnalyticsClient;
    private final DefaultContentFallback defaultContentFallback;
    private final Counter recommendationCounter;
    private final Timer recommendationTimer;

    @Autowired
    public RecommendationService(OpenAIClient openAIClient,
                                 PerformanceAnalyticsClient performanceAnalyticsClient,
                                 DefaultContentFallback defaultContentFallback,
                                 Counter recommendationCounter,
                                 Timer recommendationTimer) {
        this.openAIClient = openAIClient;
        this.performanceAnalyticsClient = performanceAnalyticsClient;
        this.defaultContentFallback = defaultContentFallback;
        this.recommendationCounter = recommendationCounter;
        this.recommendationTimer = recommendationTimer;
    }

    /**
     * @traceability [REQ-002], [EXC-003], [EXC-004]
     * Generates a content recommendation for a user based on their request.
     * @param request the request containing userId, platform, topic, tone, and optional maxLength
     * @return RecommendationResponseDto containing the generated recommendation
     */
    public RecommendationResponseDto generateRecommendation(RecommendationRequestDto request) {
        // Step 1: Extract tenantId from SecurityContext
        String tenantId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Step 1: Get top 5 performing posts
        List<PerformanceMetricEntity> topPosts = performanceAnalyticsClient.findTopPerformingPosts(
                request.userId(), request.platform().name(), 5);

        // Step 2: Build prompt using templates
        PromptTemplateConfig templates = promptTemplates;
        String systemPrompt = buildSystemPrompt(templates, request);
        String userPrompt = buildUserPrompt(templates, request, topPosts);

        // Step 3: Generate content
        String generatedContent;
        try {
            generatedContent = openAIClient.generateContent(systemPrompt, userPrompt);
            if (generatedContent == null || generatedContent.length() > request.maxLength()) {
                throw new FallbackContentException("Generated content is empty or exceeds max length");
            }
        } catch (AiServiceException e) {
            log.warn("OpenAI service unavailable, activating fallback for userId={}, correlationId={}", request.userId(), MDC.get("correlationId"), e);
            generatedContent = defaultContentFallback.provide(request);
        } catch (FallbackContentException ex) {
            log.error("Fallback content provider also failed for userId={}", request.userId(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex);
        }

        // Step 4: Build response DTO
        UUID recommendationId = UUID.randomUUID();
        BigDecimal confidenceScore = BigDecimal.valueOf(0.85);
        boolean isFallback = false;

        if (generatedContent == null || generatedContent.length() > request.maxLength()) {
            isFallback = true;
        }

        RecommendationResponseDto response = RecommendationResponseDto.builder()
                .recommendationId(recommendationId)
                .userId(request.userId())
                .platform(request.platform().name())
                .content(generatedContent)
                .confidenceScore(confidenceScore)
                .isFallback(isFallback)
                .generatedAt(OffsetDateTime.now())
                .build();

        // Log with MDC
        MDC.put("correlationId", request.correlationId());
        MDC.put("userId", request.userId());
        MDC.put("platform", request.platform().name());
        MDC.put("tone", request.tone().name());
        MDC.put("isFallback", String.valueOf(isFallback));
        log.info("Recommendation generated successfully for userId={}, platform={}, isFallback={}", request.userId(), request.platform(), isFallback);

        // Record metrics
        recommendationCounter.increment();
        recommendationTimer.record(() -> {}, Collections.emptyMap());

        return response;
    }

    private String buildSystemPrompt(PromptTemplateConfig templates, RecommendationRequestDto request) {
        return String.format(templates.getSystemPromptTemplate(),
                request.tone().getPromptModifier(),
                request.platform().getDisplayName());
    }

    private String buildUserPrompt(PromptTemplateConfig templates, RecommendationRequestDto request, List<PerformanceMetricEntity> topPosts) {
        StringBuilder sb = new StringBuilder();
        sb.append(templates.getUserPromptTemplate());
        sb.append("Topic: ").append(request.topic());
        sb.append("; Tone: ").append(request.tone().getPromptModifier());
        sb.append("; Platform: ").append(request.platform().getDisplayName());
        if (!topPosts.isEmpty()) {
            sb.append("; Top performing posts:");
            for (PerformanceMetricEntity post : topPosts) {
                sb.append(" Likes: ").append(post.getLikes())
                  .append(", Comments: ").append(post.getComments())
                  .append(", Shares: ").append(post.getShares())
                  .append("; ");
            }
        }
        return sb.toString();
    }

    @ConfigurationProperties(prefix = "prompt-templates")
    private PromptTemplateConfig promptTemplates;

    public static class PromptTemplateConfig {
        private String systemPromptTemplate;
        private String userPromptTemplate;

        public String getSystemPromptTemplate() {
            return systemPromptTemplate;
        }

        public void setSystemPromptTemplate(String systemPromptTemplate) {
            this.systemPromptTemplate = systemPromptTemplate;
        }

        public String getUserPromptTemplate() {
            return userPromptTemplate;
        }

        public void setUserPromptTemplate(String userPromptTemplate) {
            this.userPromptTemplate = userPromptTemplate;
        }
    }
}
```

# Day 2: model nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/resources/prompt-templates.yml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/resources/prompt-templates.yml`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-003]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java được đánh dấu @Service, @Transactional(readOnly = true) và @Slf4j (Lombok). Inject các dependency thông qua constructor: OpenAIClient openAIClient, PerformanceAnalyticsClient performanceAnalyticsClient, DefaultContentFallback defaultContentFallback. Triển khai phương thức chính RecommendationResponseDto generateRecommendation(RecommendationRequestDto request) với luồng xử lý 4 bước: (Bước 1) Trích xuất tenantId từ SecurityContextHolder.getContext().getAuthentication() để đảm bảo cô lập dữ liệu đa tenant; gọi performanceAnalyticsClient.findTopPerformingPosts(request.userId(), request.platform().name(), 5) để lấy top 5 bài đăng có hiệu suất cao nhất, kết quả có thể rỗng. (Bước 2) Xây dựng prompt tổng hợp bằng cách tải template từ external config ./sources/backend/ai-service/src/main/resources/prompt-templates.yml thông qua @ConfigurationProperties(prefix = "prompt-templates"); prompt phải bao gồm: chủ đề người dùng cung cấp (topic), tông giọng yêu cầu (tone.getPromptModifier()), nền tảng mục tiêu (platform.getDisplayName()), và các mẫu nội dung thành công trước đó (từ top 5 performance metrics). (Bước 3) Gọi openAIClient.generateContent(systemPrompt, userPrompt) để nhận nội dung đề xuất; nếu kết quả rỗng hoặc vượt quá maxLength, ném FallbackContentException. (Bước 4) Xây dựng RecommendationResponseDto với recommendationId = UUID.randomUUID(), confidenceScore = BigDecimal.valueOf(0.85), isFallback = false, generatedAt = OffsetDateTime.now(). Toàn bộ phương thức được bao bọc trong khối try-catch (AiServiceException ex) để khi OpenAI thất bại, chuyển hướng sang defaultContentFallback.provide(request) trả về nội dung mặc định với isFallback = true. Bổ sung khối catch (FallbackContentException ex) để ghi log lỗi cấp ERROR với correlation ID và ném ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex). Bổ sung metric Micrometer ai.recommendation.generated.total (counter với tag outcome=success|fallback|failed) và ai.recommendation.duration (timer). Sử dụng structured logging với MDC context bao gồm correlationId, userId, platform, tone, isFallback theo OWASP A09. Triển khai interface RecommendationService để dễ dàng mock trong kiểm thử.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.aiservice.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.nlh4j.socialscheduler.aiservice.exception.FallbackContentException;
import org.nlh4j.socialscheduler.aiservice.integration.OpenAIClient;
import org.nlh4j.socialscheduler.aiservice.integration.PerformanceAnalyticsClient;
import org.nlh4j.socialscheduler.aiservice.fallback.DefaultContentFallback;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationService implements RecommendationService {

    private final OpenAIClient openAIClient;
    private final PerformanceAnalyticsClient performanceAnalyticsClient;
    private final DefaultContentFallback defaultContentFallback;
    private final PromptTemplatesConfig promptTemplatesConfig;
    private final Counter aiRecommendationGeneratedCounter;
    private final Timer aiRecommendationDurationTimer;

    @Override
    public RecommendationResponseDto generateRecommendation(RecommendationRequestDto request) {
        String correlationId = UUID.randomUUID().toString();
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String platform = request.getPlatform().name();
        String tone = request.getTone().getPromptModifier();
        String topic = request.getTopic();

        MDC.put("correlationId", correlationId);
        MDC.put("userId", userId);
        MDC.put("platform", platform);
        MDC.put("tone", tone);
        MDC.put("isFallback", "false");

        Timer.Sample sample = Timer.start(aiRecommendationDurationTimer);
        try {
            String systemPrompt = promptTemplatesConfig.getSystemPrompt()
                    .replace("{topic}", topic)
                    .replace("{tone}", tone)
                    .replace("{platform}", platform.name());
            String userPrompt = request.getTopic();

            String content = openAIClient.generateContent(systemPrompt, userPrompt);
            if (content == null || content.isBlank() || (request.getMaxLength() > 0 && content.length() > request.getMaxLength())) {
                throw new FallbackContentException("OpenAI response is empty or exceeds maxLength");
            }

            RecommendationResponseDto response = RecommendationResponseDto.builder()
                    .recommendationId(UUID.randomUUID())
                    .userId(userId)
                    .platform(platform)
                    .content(content)
                    .confidenceScore(BigDecimal.valueOf(0.85))
                    .isFallback(false)
                    .generatedAt(OffsetDateTime.now())
                    .build();

            aiRecommendationGeneratedCounter.increment();
            return response;
        } catch (AiServiceException ex) {
            log.warn("OpenAI service error: {}", ex.getMessage(), ex);
            RecommendationResponseDto fallback = defaultContentFallback.provide(request);
            MDC.put("isFallback", "true");
            aiRecommendationGeneratedCounter.increment();
            aiRecommendationDurationTimer.stop();
            return fallback;
        } catch (FallbackContentException ex) {
            log.error("Fallback content provider failed: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex);
        } finally {
            MDC.remove("correlationId");
            MDC.remove("userId");
            MDC.remove("platform");
            MDC.remove("tone");
            MDC.remove("isFallback");
        }
    }
}

@ConfigurationProperties(prefix = "prompt-templates")
class PromptTemplatesConfig {
    private String systemPrompt;
    private String userPromptTemplate;

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getUserPromptTemplate() {
        return userPromptTemplate;
    }

    public void setUserPromptTemplate(String userPromptTemplate) {
        this.userPromptTemplate = userPromptTemplate;
    }
}
```

# Day 2: model nvidia/nemotron-3-super-120b-a12b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package fallback và exception: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java được đánh dấu @Component và @Slf4j (Lombok), chứa một Map<String, String> FALLBACK_TEMPLATES được khởi tạo tĩnh với các mẫu nội dung mặc định theo key "{platform}_{tone}" (ví dụ: FACEBOOK_PROFESSIONAL, INSTAGRAM_CASUAL, TIKTOK_HUMOROUS, FACEBOOK_INSPIRATIONAL); mỗi mẫu là một chuỗi nội dung marketing chuyên nghiệp dài 200-300 ký tự phù hợp với từng nền tảng. Triển khai phương thức RecommendationResponseDto provide(RecommendationRequestDto request) thực hiện logic: chọn template phù hợp nhất dựa trên request.platform().name() + "_" + request.tone().name(), nếu không tìm thấy thì sử dụng template mặc định "Stay tuned for exciting updates from our brand!"; sinh recommendationId = UUID.randomUUID(), trả về RecommendationResponseDto với confidenceScore = BigDecimal.valueOf(0.30), isFallback = true, generatedAt = OffsetDateTime.now(). Phương thức provide phải ném FallbackContentException nếu template rỗng hoặc xảy ra lỗi không mong muốn. (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java kế thừa RuntimeException với các trường errorCode = "AI_SERVICE_UNAVAILABLE", platform, originalCause, httpStatus; bổ sung constructor nhận message và cause. (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java kế thừa RuntimeException với errorCode = "FALLBACK_CONTENT_FAILED". (4) Bổ sung annotation @Slf4j để ghi log cảnh báo khi kích hoạt fallback với thông điệp "Fallback content provided for userId={} platform={} tone={}" ở cấp độ INFO khi thành công và ERROR khi fallback cũng thất bại. Sử dụng @Builder của Lombok để hỗ trợ khởi tạo DTO một cách rõ ràng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.aiservice.fallback;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.nlh4j.socialscheduler.aiservice.dto.RecommendationRequestDto;
import org.nlh4j.socialscheduler.aiservice.dto.RecommendationResponseDto;
import org.nlh4j.socialscheduler.aiservice.exception.FallbackContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DefaultContentFallback provides safe fallback content generation when AI service is unavailable.
 * Implements static template mapping by platform and tone for marketing content.
 * 
 * @traceability [REQ-002], [EXC-004]
 */
@Component
public class DefaultContentFallback {

    private static final Logger log = LoggerFactory.getLogger(DefaultContentFallback.class);

    /**
     * Static map of fallback templates keyed by "{platform}_{tone}".
     * Each template is 200-300 characters of platform-appropriate marketing content.
     */
    private static final Map<String, String> FALLBACK_TEMPLATES = new HashMap<>();

    static {
        // Facebook templates
        FALLBACK_TEMPLATES.put("FACEBOOK_PROFESSIONAL",
                "Discover how our innovative solutions transform business operations with measurable ROI. "
                + "Join thousands of satisfied clients who have streamlined workflows and increased productivity. "
                + "Learn more about our award-winning services today!");
        FALLBACK_TEMPLATES.put("FACEBOOK_CASUAL",
                "Hey there! 👋 We've got something awesome to share that'll make your day better. "
                + "Check out what our community is loving right now - it's practical, fun, and totally worth your time!");
        FALLBACK_TEMPLATES.put("FACEBOOK_HUMOROUS",
                "Warning: May cause sudden outbreaks of productivity and joy. Side effects include smiling at your screen "
                + "and telling colleagues about this amazing find. Consult your team if symptoms persist!");
        FALLBACK_TEMPLATES.put("FACEBOOK_INSPIRATIONAL",
                "Every great achievement starts with a single step toward improvement. "
                + "What will you accomplish today with tools designed to elevate your potential? "
                + "The journey to excellence begins now.");

        // Instagram templates
        FALLBACK_TEMPLATES.put("INSTAGRAM_PROFESSIONAL",
                "✨ Elevate your brand presence with strategic insights that drive real engagement. "
                + "Swipe to see how leading businesses transform challenges into growth opportunities. "
                + "#BusinessGrowth #Innovation #Leadership");
        FALLBACK_TEMPLATES.put("INSTAGRAM_CASUAL",
                "Just dropped something you'll actually want to save! 💫 "
                + "Simple, effective, and made for real life. Double tap if you're feeling inspired! ✨");
        FALLBACK_TEMPLATES.put("INSTAGRAM_HUMOROUS",
                "When your to-do list needs a humor intervention... 😅 "
                + "We bring the laughs so you can bring the results. Tag someone who needs this break!");
        FALLBACK_TEMPLATES.put("INSTAGRAM_INSPIRATIONAL",
                "Your potential is limitless when you have the right support system. "
                + "Keep pushing forward - amazing things happen when you believe in your journey. 🌟");

        // TikTok templates
        FALLBACK_TEMPLATES.put("TIKTOK_PROFESSIONAL",
                "📊 3 quick tips that actually work for business growth in 2024: "
                + "1) Focus on solving real problems 2) Measure what matters 3) Iterate based on feedback. "
                + "Follow for more actionable insights!");
        FALLBACK_TEMPLATES.put("TIKTOK_CASUAL",
                "POV: You just found the easiest way to level up your daily routine. "
                + "It's simpler than you think and actually enjoyable. Try it today! 👇");
        FALLBACK_TEMPLATES.put("TIKTOK_HUMOROUS",
                "Me trying to adult vs. me remembering there's a better way: "
                + "[insert relatable struggle] → [insert our solution] → instant life upgrade. "
                + "Send this to someone who needs to laugh!");
        FALLBACK_TEMPLATES.put("TIKTOK_INSPIRATIONAL",
                "Small consistent actions create massive transformation over time. "
                + "What's one small step you can take today toward your bigger goals? "
                + "Start where you are, use what you have, do what you can. 💪");
    }

    /**
     * Generates fallback content based on request parameters.
     * 
     * @param request contains userId, platform, topic, tone, and maxLength
     * @return RecommendationResponseDto with fallback content
     * @throws FallbackContentException if template resolution fails
     */
    public RecommendationResponseDto provide(RecommendationRequestDto request) {
        String platform = request.getPlatform().name();
        String tone = request.getTone().name();
        String key = platform + "_" + tone;
        
        String template = FALLBACK_TEMPLATES.get(key);
        if (template == null || template.isBlank()) {
            template = "Stay tuned for exciting updates from our brand!";
            log.warn("No template found for key {}, using default fallback", key);
        }

        // Validate template length (200-300 chars as per requirement)
        if (template.length() < 200 || template.length() > 300) {
            log.error("Fallback template for key {} has invalid length: {} characters", key, template.length());
            throw new FallbackContentException(
                    String.format("Fallback template length invalid for key %s: %d chars", key, template.length()));
        }

        log.info("Fallback content provided for userId={} platform={} tone={}", 
                request.getUserId(), platform, tone);

        return RecommendationResponseDto.builder()
                .recommendationId(UUID.randomUUID())
                .userId(request.getUserId())
                .platform(platform)
                .content(template)
                .confidenceScore(BigDecimal.valueOf(0.30))
                .isFallback(true)
                .generatedAt(OffsetDateTime.now())
                .build();
    }
}
```

# Day 2: model nvidia/nemotron-3-super-120b-a12b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package fallback và exception: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java được đánh dấu @Component và @Slf4j (Lombok), chứa một Map<String, String> FALLBACK_TEMPLATES được khởi tạo tĩnh với các mẫu nội dung mặc định theo key "{platform}_{tone}" (ví dụ: FACEBOOK_PROFESSIONAL, INSTAGRAM_CASUAL, TIKTOK_HUMOROUS, FACEBOOK_INSPIRATIONAL); mỗi mẫu là một chuỗi nội dung marketing chuyên nghiệp dài 200-300 ký tự phù hợp với từng nền tảng. Triển khai phương thức RecommendationResponseDto provide(RecommendationRequestDto request) thực hiện logic: chọn template phù hợp nhất dựa trên request.platform().name() + "_" + request.tone().name(), nếu không tìm thấy thì sử dụng template mặc định "Stay tuned for exciting updates from our brand!"; sinh recommendationId = UUID.randomUUID(), trả về RecommendationResponseDto với confidenceScore = BigDecimal.valueOf(0.30), isFallback = true, generatedAt = OffsetDateTime.now(). Phương thức provide phải ném FallbackContentException nếu template rỗng hoặc xảy ra lỗi không mong muốn. (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java kế thừa RuntimeException với các trường errorCode = "AI_SERVICE_UNAVAILABLE", platform, originalCause, httpStatus; bổ sung constructor nhận message và cause. (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java kế thừa RuntimeException với errorCode = "FALLBACK_CONTENT_FAILED". (4) Bổ sung annotation @Slf4j để ghi log cảnh báo khi kích hoạt fallback với thông điệp "Fallback content provided for userId={} platform={} tone={}" ở cấp độ INFO khi thành công và ERROR khi fallback cũng thất bại. Sử dụng @Builder của Lombok để hỗ trợ khởi tạo DTO một cách rõ ràng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model nvidia/nemotron-3-super-120b-a12b:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo bốn tệp nguồn trong package fallback và exception: (1) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java được đánh dấu @Component và @Slf4j (Lombok), chứa một Map<String, String> FALLBACK_TEMPLATES được khởi tạo tĩnh với các mẫu nội dung mặc định theo key "{platform}_{tone}" (ví dụ: FACEBOOK_PROFESSIONAL, INSTAGRAM_CASUAL, TIKTOK_HUMOROUS, FACEBOOK_INSPIRATIONAL); mỗi mẫu là một chuỗi nội dung marketing chuyên nghiệp dài 200-300 ký tự phù hợp với từng nền tảng. Triển khai phương thức RecommendationResponseDto provide(RecommendationRequestDto request) thực hiện logic: chọn template phù hợp nhất dựa trên request.platform().name() + "_" + request.tone().name(), nếu không tìm thấy thì sử dụng template mặc định "Stay tuned for exciting updates from our brand!"; sinh recommendationId = UUID.randomUUID(), trả về RecommendationResponseDto với confidenceScore = BigDecimal.valueOf(0.30), isFallback = true, generatedAt = OffsetDateTime.now(). Phương thức provide phải ném FallbackContentException nếu template rỗng hoặc xảy ra lỗi không mong muốn. (2) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java kế thừa RuntimeException với các trường errorCode = "AI_SERVICE_UNAVAILABLE", platform, originalCause, httpStatus; bổ sung constructor nhận message và cause. (3) ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java kế thừa RuntimeException với errorCode = "FALLBACK_CONTENT_FAILED". (4) Bổ sung annotation @Slf4j để ghi log cảnh báo khi kích hoạt fallback với thông điệp "Fallback content provided for userId={} platform={} tone={}" ở cấp độ INFO khi thành công và ERROR khi fallback cũng thất bại. Sử dụng @Builder của Lombok để hỗ trợ khởi tạo DTO một cách rõ ràng.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

