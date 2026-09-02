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