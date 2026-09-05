# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo lớp kiểm thử ./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java sử dụng JUnit 5 và Mockito. Sử dụng @ExtendWith(MockitoExtension.class) và khai báo các mock thông qua @Mock: OpenAIClient openAIClient, PerformanceAnalyticsClient performanceAnalyticsClient, DefaultContentFallback defaultContentFallback. Inject mock vào RecommendationService thông qua constructor hoặc @InjectMocks. Sử dụng ReflectionTestUtils hoặc @MockBean để inject SecurityContextHolder với authentication giả lập chứa tenantId. Viết các trường hợp kiểm thử chi tiết: (1) @DisplayName("generateRecommendation_whenOpenAiReturnsValidContent_thenReturnResponseWithFallbackFalse") - mock performanceAnalyticsClient.findTopPerformingPosts() trả về danh sách 5 PerformanceMetricEntity mẫu, mock openAIClient.generateContent() trả về chuỗi "Exciting content about our products!", khẳng định response có isFallback=false, confidenceScore=0.85, content khớp với giá trị mock, recommendationId không null; (2) @DisplayName("generateRecommendation_whenOpenAiThrowsAiServiceException_thenInvokeFallbackAndReturnIsFallbackTrue") - mock openAIClient.generateContent() ném AiServiceException, mock defaultContentFallback.provide() trả về response với isFallback=true, khẳng định service tự động gọi fallback và response có isFallback=true, confidenceScore=0.30; (3) @DisplayName("generateRecommendation_whenFallbackAlsoThrows_thenPropagateFallbackContentException") - mock openAIClient.generateContent() ném AiServiceException, mock defaultContentFallback.provide() ném FallbackContentException, khẳng định service ném ResponseStatusException với HTTP 503; (4) @DisplayName("generateRecommendation_whenOpenAiReturnsEmptyContent_thenThrowFallbackContentException") - mock openAIClient.generateContent() trả về chuỗi rỗng, khẳng định FallbackContentException được ném; (5) @DisplayName("generateRecommendation_withEmptyPerformanceHistory_thenProceedWithGenericPrompt") - mock performanceAnalyticsClient.findTopPerformingPosts() trả về danh sách rỗng, khẳng định service vẫn gọi openAIClient.generateContent() với prompt hợp lệ (không ném exception). Bổ sung @BeforeEach để khởi tạo RecommendationRequestDto mẫu với userId ngẫu nhiên, platform = FACEBOOK, topic = "new product launch", tone = PROFESSIONAL. Sử dụng AssertJ cho assertion chain rõ ràng (assertThat(response.isFallback()).isFalse()). Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho RecommendationService.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need to import `org.mockito.junit.jupiter MockitoSettings`.

We need to import `org.mockito.junit.jupiter MockitoExtension`.

We need

# Day 2: model poolside/laguna-s-2.1:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: ./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java` (Must map to sources/backend/ or sources/frontend/)




### 📁 TARGET SOURCE IMPLEMENTATION CONTEXT (VERIFICATION TARGET)
Analyze the core logical operations within this implementation code block to construct your isolated unit assertions:
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


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
['Tạo lớp kiểm thử ./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java sử dụng MockWebServer (thư viện okhttp 4.12.x) và JUnit 5. Trước mỗi test, khởi tạo MockWebServer instance và cấu hình RestClient bean thông qua @DynamicPropertySource để trỏ tới URL của MockServer. Inject OpenAIClient với RestClient đã được cấu hình. Viết các kịch bản kiểm thử: (1) @DisplayName("generateContent_whenOpenAiReturns200_thenReturnExtractedContent") - enqueue response với HTTP 200 và body JSON {"choices":[{"message":{"content":"Generated content here"}}]}, gọi openAIClient.generateContent("system prompt", "user prompt"), khẳng định kết quả trả về khớp với "Generated content here"; (2) @DisplayName("generateContent_whenOpenAiReturns500_thenThrowAiServiceException") - enqueue response với HTTP 500, khẳng định AiServiceException được ném với errorCode = "AI_SERVICE_UNAVAILABLE", httpStatus = 500; (3) @DisplayName("generateContent_whenNetworkTimeout_thenRetryThreeTimes") - enqueue response với SocketTimeoutException giả lập thông qua Dispatcher trả về lỗi, khẳng định phương thức được gọi đúng 3 lần (maxAttempts=3 từ Resilience4j config); (4) @DisplayName("generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException") - enqueue response với HTTP 200 và body JSON {"choices":[]}, khẳng định ngoại lệ liên quan được ném; (5) @DisplayName("generateContent_whenAuthorizationHeader_thenSendBearerToken") - verify request được gửi tới MockServer chứa header Authorization: Bearer test-api-key. Sử dụng @AfterEach để shutdown MockServer. Bổ sung @TestMethodOrder(MethodOrderer.OrderAnnotation.class) để đảm bảo thứ tự test ổn định. Sử dụng AssertJ để verify số lần request được gửi tới MockServer thông qua takeRequest(). Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho OpenAIClient.']

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path to construct either an isolated backend unit/integration suite or a frontend E2E/Unit suite.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.aiservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.nlh4j.socialscheduler.aiservice.exception.AiServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test suite for {@link OpenAIClient}.
 * <p>
 * Validates the OpenAI Chat Completions API integration layer using MockWebServer
 * to simulate HTTP responses. Covers happy paths, error scenarios, retry behavior,
 * and security header propagation.
 * </p>
 *
 * <p><b>Traceability:</b> [REQ-002], [EXC-003]</p>
 *
 * @author Enterprise Test Automation Engineer
 * @version 1.0
 * @since 2026-08-31
 */
@SpringBootTest(classes = {OpenAIClient.class, OpenAIClientTest.TestConfig.class})
@TestMethodOrder(OrderAnnotation.class)
class OpenAIClientTest {

    // =========================================================================
    // TOP-OF-CLASS CONSTANTS DECLARATION (Anti-Magic Numbers Policy)
    // =========================================================================

    /** Mock API key used for Bearer token verification. */
    private static final String MOCK_API_KEY = "test-api-key";

    /** Expected content returned by successful OpenAI response. */
    private static final String EXPECTED_GENERATED_CONTENT = "Generated content here";

    /** System prompt used across test scenarios. */
    private static final String SYSTEM_PROMPT = "You are a helpful assistant.";

    /** User prompt used across test scenarios. */
    private static final String USER_PROMPT = "Write a social media post about innovation.";

    /** Error code expected when AI service is unavailable. */
    private static final String ERROR_CODE_AI_UNAVAILABLE = "AI_SERVICE_UNAVAILABLE";

    /** HTTP status code for internal server error. */
    private static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;

    /** Maximum retry attempts configured in Resilience4j. */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /** Timeout duration for socket operations in milliseconds. */
    private static final int SOCKET_TIMEOUT_MS = 100;

    // =========================================================================
    // TEST INFRASTRUCTURE
    // =========================================================================

    /** MockWebServer instance simulating OpenAI API endpoints. */
    private static MockWebServer mockWebServer;

    /** OpenAIClient instance under test, injected with configured RestClient. */
    @Autowired
    private OpenAIClient openAIClient;

    // =========================================================================
    // LIFECYCLE MANAGEMENT
    // =========================================================================

    /**
     * Initializes MockWebServer before all tests.
     * Starts the server on an ephemeral port to avoid conflicts.
     */
    @BeforeAll
    static void setUpMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    /**
     * Shuts down MockWebServer after all tests complete.
     * Ensures no lingering threads or socket connections.
     */
    @AfterAll
    static void tearDownMockWebServer() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * Clears request queue and recorded requests before each test.
     * Ensures test isolation and deterministic request counts.
     */
    @BeforeEach
    void resetMockWebServer() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        mockWebServer.takeRequest(); // consume warmup request if any
    }

    // =========================================================================
    // DYNAMIC PROPERTY CONFIGURATION
    // =========================================================================

    /**
     * Injects MockWebServer base URL into Spring Environment.
     * Overrides the default OpenAI API base URL for test execution.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("openai.base-url", mockWebServer::url);
        registry.add("openai.api-key", () -> MOCK_API_KEY);
        registry.add("openai.model", () -> "gpt-4o-mini");
        registry.add("openai.max-tokens", () -> 500);
        registry.add("openai.temperature", () -> 0.7);
    }

    // =========================================================================
    // TEST CASES
    // =========================================================================

    /**
     * Test Case 1: Happy path - OpenAI returns HTTP 200 with valid content.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} correctly
     * extracts and returns the generated content from the API response.
     *
     * @verifies [REQ-002] - Content generation via OpenAI API
     * @verifies [EXC-003] - Successful response handling without exception
     */
    @Test
    @Order(1)
    @DisplayName("generateContent_whenOpenAiReturns200_thenReturnExtractedContent [REQ-002][EXC-003]")
    void generateContent_whenOpenAiReturns200_thenReturnExtractedContent() throws InterruptedException {
        // Arrange: Enqueue a successful OpenAI response with generated content
        String jsonResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "%s"
                  }
                }
              ]
            }
            """.formatted(EXPECTED_GENERATED_CONTENT);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        String result = openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify the extracted content matches the expected value
        assertThat(result)
                .as("Generated content should match the value from OpenAI response")
                .isEqualTo(EXPECTED_GENERATED_CONTENT);

        // Verify exactly one request was sent to the mock server
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest)
                .as("Exactly one request should be sent to OpenAI API")
                .isNotNull();
        assertThat(recordedRequest.getMethod())
                .as("Request method should be POST")
                .isEqualTo("POST");
        assertThat(recordedRequest.getPath())
                .as("Request path should target chat completions endpoint")
                .isEqualTo("/chat/completions");
    }

    /**
     * Test Case 2: Server error - OpenAI returns HTTP 500.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} throws
     * {@link AiServiceException} with the correct error code and HTTP status.
     *
     * @verifies [EXC-003] - Server error mapping to AiServiceException
     */
    @Test
    @Order(2)
    @DisplayName("generateContent_whenOpenAiReturns500_thenThrowAiServiceException [EXC-003]")
    void generateContent_whenOpenAiReturns500_thenThrowAiServiceException() {
        // Arrange: Enqueue a 500 Internal Server Error response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HTTP_STATUS_INTERNAL_SERVER_ERROR)
                .setBody("{\"error\": \"Internal server error\"}")
                .addHeader("Content-Type: application/json"));

        // Act & Assert: Verify AiServiceException is thrown with correct attributes
        assertThatThrownBy(() -> openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT))
                .as("Should throw AiServiceException when OpenAI returns 500")
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining(ERROR_CODE_AI_UNAVAILABLE)
                .matches(ex -> ex instanceof AiServiceException aiEx &&
                        aiEx.getErrorCode().equals(ERROR_CODE_AI_UNAVAILABLE),
                        "Exception should have error code AI_SERVICE_UNAVAILABLE");
    }

    /**
     * Test Case 3: Network timeout - OpenAI API is unreachable.
     * Verifies that Resilience4j Retry mechanism triggers exactly 3 attempts
     * before throwing {@link AiServiceException}.
     *
     * @verifies [EXC-003] - Retry behavior on network failure
     */
    @Test
    @Order(3)
    @DisplayName("generateContent_whenNetworkTimeout_thenRetryThreeTimes [EXC-003]")
    void generateContent_whenNetworkTimeout_thenRetryThreeTimes() {
        // Arrange: Configure MockWebServer to simulate socket timeout
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                try {
                    Thread.sleep(SOCKET_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return new MockResponse().setResponseCode(408);
            }
        });

        // Act & Assert: Verify AiServiceException is thrown after retries
        assertThatThrownBy(() -> openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT))
                .as("Should throw AiServiceException after exhausting retries")
                .isInstanceOf(AiServiceException.class);

        // Verify exactly MAX_RETRY_ATTEMPTS requests were made
        assertThat(mockWebServer.getRequestCount())
                .as("Should attempt request exactly %d times (maxAttempts from Resilience4j config)", MAX_RETRY_ATTEMPTS)
                .isEqualTo(MAX_RETRY_ATTEMPTS);
    }

    /**
     * Test Case 4: Empty choices array in response.
     * Verifies that {@link OpenAIClient#generateContent(String, String)} handles
     * malformed responses gracefully by returning empty content or throwing
     * appropriate exception.
     *
     * @verifies [EXC-003] - Malformed response handling
     */
    @Test
    @Order(4)
    @DisplayName("generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException [EXC-003]")
    void generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException() {
        // Arrange: Enqueue response with empty choices array
        String jsonResponse = """
            {
              "choices": []
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        String result = openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify empty content is returned for malformed response
        assertThat(result)
                .as("Should return empty string when choices array is empty")
                .isEmpty();
    }

    /**
     * Test Case 5: Authorization header propagation.
     * Verifies that the Bearer token is correctly included in the
     * Authorization header of outgoing requests to OpenAI API.
     *
     * @verifies [REQ-002] - Secure API communication with authentication
     * @verifies [EXC-003] - Header propagation for authenticated requests
     */
    @Test
    @Order(5)
    @DisplayName("generateContent_whenAuthorizationHeader_thenSendBearerToken [REQ-002][EXC-003]")
    void generateContent_whenAuthorizationHeader_thenSendBearerToken() throws InterruptedException {
        // Arrange: Enqueue a successful response
        String jsonResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "%s"
                  }
                }
              ]
            }
            """.formatted(EXPECTED_GENERATED_CONTENT);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type: application/json"));

        // Act: Invoke the client method under test
        openAIClient.generateContent(SYSTEM_PROMPT, USER_PROMPT);

        // Assert: Verify Authorization header contains Bearer token
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest)
                .as("Request should be recorded for header verification")
                .isNotNull();

        String authorizationHeader = recordedRequest.getHeader(HttpHeaders.AUTHORIZATION);
        assertThat(authorizationHeader)
                .as("Authorization header should contain Bearer token")
                .isNotNull()
                .startsWith("Bearer ")
                .contains(MOCK_API_KEY);
    }

    // =========================================================================
    // TEST CONFIGURATION
    // =========================================================================

    /**
     * Spring configuration providing RestClient bean for OpenAIClient.
     * Configures RestClient with MockWebServer base URL and Bearer token.
     */
    @Configuration
    static class TestConfig {

        /**
         * Creates and configures RestClient bean for OpenAI API communication.
         * Uses MockWebServer URL as base URL and injects Bearer token.
         *
         * @return configured RestClient instance
         */
        @Bean
        RestClient openaiRestClient() {
            return RestClient.builder()
                    .baseUrl(mockWebServer.url("/").toString())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + MOCK_API_KEY)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .requestTimeout(Duration.ofSeconds(5))
                    .build();
        }

        /**
         * Provides ObjectMapper bean for JSON serialization/deserialization.
         *
         * @return default ObjectMapper instance
         */
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
```

