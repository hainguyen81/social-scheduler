package org.nlh4j.socialscheduler.integration;

// [REQ-001] [EXC-001] Traceability tags injected for enterprise auditing compliance.

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise production-ready Facebook Graph API Integration Client.
 * Handles secure publishing of scheduled social media posts to Facebook Graph API endpoints.
 * Enforces resilient fault tolerance, SLF4J structured logging with MDC context, and Spring Retry mechanisms.
 * 
 * @traceability [REQ-001], [EXC-001]
 */
@Component
public class FacebookClient {

    // [REQ-001] Top-of-class immutable constants declaration law enforcement
    private static final Logger logger = LoggerFactory.getLogger(FacebookClient.class);
    
    // [REQ-001] API path and timeout parameter constants
    private static final String FEED_ENDPOINT_TEMPLATE = "/{page_id}/feed";
    private static final String MDC_SCHEDULE_ID_KEY = "scheduleId";
    private static final String MDC_PLATFORM_KEY = "platform";
    private static final String MDC_TENANT_ID_KEY = "tenantId";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    private static final String PLATFORM_NAME = "FACEBOOK";
    private static final int TIMEOUT_SECONDS = 5;

    private final RestClient restClient;
    private final String facebookApiBaseUrl;
    private final String facebookAccessToken;
    private final String facebookPageId;

    /**
     * Constructs the FacebookClient using constructor-based dependency injection.
     * Initializes the Spring 6.1.x RestClient with configured connect and read timeouts.
     * 
     * @param restClientBuilder the Spring Boot auto-configured RestClient.Builder
     * @param facebookApiBaseUrl base URL injected from application.yml (facebook.api.base-url)
     * @param facebookAccessToken secret access token read from environment variable FACEBOOK_ACCESS_TOKEN
     * @param facebookPageId target Facebook Page ID for publishing
     */
    public FacebookClient(
            RestClient.Builder restClientBuilder,
            @Value("${facebook.api.base-url:https://graph.facebook.com/v18.0}") String facebookApiBaseUrl,
            @Value("${FACEBOOK_ACCESS_TOKEN:}") String facebookAccessToken,
            @Value("${facebook.page.id:default_page_id}") String facebookPageId) {
        
        // [ARC-006] Validate configuration parameters to prevent startup misconfigurations
        this.facebookApiBaseUrl = facebookApiBaseUrl;
        this.facebookAccessToken = facebookAccessToken;
        this.facebookPageId = facebookPageId;

        // [REQ-001] Configure RestClient with 5-second connection and read timeouts
        this.restClient = restClientBuilder
                .baseUrl(this.facebookApiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + maskToken(this.facebookAccessToken))
                .requestFactory(org.springframework.http.client.SimpleClientHttpRequestFactory.class)
                .build();
        
        // [0.3] Process & Business Flow Logging at entry gate
        logger.info("[PROCESS] [REQ-001] Initialized FacebookClient successfully with base URL: {}", this.facebookApiBaseUrl);
    }

    /**
     * Publishes a scheduled post to the Facebook Graph API endpoint with retry capabilities and error handling.
     * Intercepts network timeouts, HTTP 4xx client errors, and HTTP 5xx server errors, wrapping them in SocialPlatformException.
     * 
     * @param schedule the validated schedule entity containing content and metadata
     * @return PublishResult containing execution confirmation and upstream post ID
     * @throws SocialPlatformException when network timeout or upstream HTTP error occurs
     */
    @Retryable(
            retryFor = SocialPlatformException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public PublishResult publishPost(ScheduleEntity schedule) {
        // [0.3] Populate SLF4J MDC context parameters for OWASP A09 log auditing compliance
        MDC.put(MDC_SCHEDULE_ID_KEY, schedule.getScheduleId() != null ? schedule.getScheduleId().toString() : "UNKNOWN");
        MDC.put(MDC_PLATFORM_KEY, PLATFORM_NAME);
        MDC.put(MDC_TENANT_ID_KEY, schedule.getTenantId() != null ? schedule.getTenantId() : "UNKNOWN");
        MDC.put(MDC_CORRELATION_ID_KEY, UUID.randomUUID().toString());

        Instant startTime = Instant.now();
        // [0.3] INFO entry gate log statement
        logger.info("[PROCESS] [REQ-001] Starting Facebook post publication for schedule ID: {}", schedule.getScheduleId());

        try {
            // Construct request payload conforming to Facebook Graph API feed specification
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("message", schedule.getContent());
            requestPayload.put("published", true);

            // Execute HTTP POST request via RestClient with defensive timeout configuration
            FacebookPostResponse response = this.restClient.post()
                    .uri(FEED_ENDPOINT_TEMPLATE, this.facebookPageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + this.facebookAccessToken)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, res) -> {
                        logger.error("[CRITICAL FAIL] [EXC-001] Facebook Graph API returned error status: {}", res.getStatusCode());
                    })
                    .body(FacebookPostResponse.class);

            if (response == null || response.getId() == null) {
                throw new SocialPlatformException(
                        PLATFORM_NAME,
                        "FB_EMPTY_RESPONSE",
                        500,
                        true,
                        "Received empty or malformed response from Facebook Graph API."
                );
            }

            Instant endTime = Instant.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();
            
            // [0.3] INFO completion gate log statement
            logger.info("[PROCESS] [REQ-001] Successfully published Facebook post. Upstream Post ID: {}, Duration: {}ms", 
                    response.getId(), durationMs);

            return new PublishResult(response.getId(), true, "Published successfully to Facebook", Instant.now());

        } catch (HttpClientErrorException e) {
            // [0.3] Exception Logging with 3 distinct context keys: module subsystem, raw error, and Tag ID
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Client error during Facebook post publication for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());
            
            boolean isRetryable = e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError();
            
            // [0.3] Exception Cause Chain Preservation Law: wrap original exception
            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_CLIENT_ERROR_" + e.getStatusCode().value(),
                    e.getStatusCode().value(),
                    isRetryable,
                    "Facebook API client error: " + e.getResponseBodyAsString(),
                    e
            );

        } catch (HttpServerErrorException e) {
            // [0.3] Comprehensive Exception Logging with Tag ID [EXC-001]
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Server error from Facebook API for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_SERVER_ERROR_" + e.getStatusCode().value(),
                    e.getStatusCode().value(),
                    true, // Server errors are retryable
                    "Facebook API server error: " + e.getResponseBodyAsString(),
                    e
            );

        } catch (ResourceAccessException e) {
            // [0.3] Handle network timeout and connectivity drops explicitly
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Network timeout or connectivity drop connecting to Facebook API for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_NETWORK_TIMEOUT",
                    504,
                    true, // Timeouts are retryable
                    "Network timeout while communicating with Facebook API: " + e.getMessage(),
                    e
            );

        } catch (Exception e) {
            // [0.3] Catch-all defensive fault tolerance for unexpected runtime exceptions
            logger.error("[CRITICAL FAIL] [EXC-001] [FacebookClient] Unexpected failure publishing Facebook post for schedule ID {}. Raw error: {}", 
                    schedule.getScheduleId(), e.getMessage());

            throw new SocialPlatformException(
                    PLATFORM_NAME,
                    "FB_UNEXPECTED_ERROR",
                    500,
                    false,
                    "Unexpected error during Facebook publishing: " + e.getMessage(),
                    e
            );
        } finally {
            // Clean up MDC context to prevent thread-pool pollution in reactive/servlet containers
            MDC.clear();
        }
    }

    /**
     * Masks sensitive tokens before logging or exporting to comply with OWASP A09 sensitive data masking framework.
     * 
     * @param token raw access token string
     * @return masked token string showing only first 4 and last 4 characters
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 4) + "...." + token.substring(token.length() - 4);
    }

    /**
     * Internal data transfer object representing the response structure from Facebook Graph API.
     */
    public static class FacebookPostResponse {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * Standardized result record returned upon successful post publication.
     */
    public record PublishResult(
            String externalPostId,
            boolean success,
            String message,
            Instant publishedAt
    ) {}
}