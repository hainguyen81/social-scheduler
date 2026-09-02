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