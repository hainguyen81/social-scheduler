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