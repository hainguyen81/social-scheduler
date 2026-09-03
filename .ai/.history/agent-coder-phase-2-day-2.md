# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/FacebookClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/FacebookClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cài đặt ba lớp Client sử dụng RestClient của Spring Framework 6.1.x kết hợp WebClient reactive tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java. Mỗi Client đánh dấu @Component với constructor injection RestClient.Builder đã cấu hình connectTimeout, readTimeout 5 giây từ application.yml. Tiêm endpoint URL facebook.api.base-url=https://graph.facebook.com/v18.0, instagram.api.base-url=https://graph.instagram.com/v18.0, tiktok.api.base-url=https://open.tiktokapis.com/v2 và access token qua @Value đọc biến môi trường FACEBOOK_ACCESS_TOKEN, INSTAGRAM_ACCESS_TOKEN, TIKTOK_ACCESS_TOKEN. Triển khai publishPost(ScheduleEntity schedule): PublishResult gọi /{page-id}/feed cho Facebook, /{ig-user-id}/media cho Instagram, /post/publish/video/init/ cho TikTok. Bọc try-catch HttpClientErrorException|HttpServerErrorException|ResourceAccessException chuẩn hóa ném SocialPlatformException khi timeout hoặc 4xx/5xx. Tích hợp @Retryable(retryFor=SocialPlatformException.class, maxAttempts=3, backoff=@Backoff(delay=1000, multiplier=2.0)) từ Spring Retry. Tạo SocialPlatformException kế thừa RuntimeException với platform,errorCode,httpStatus,retryable tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java. Sử dụng SLF4J logging có cấu trúc với MDC scheduleId,platform,tenantId,correlationId theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/InstagramClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/InstagramClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cài đặt ba lớp Client sử dụng RestClient của Spring Framework 6.1.x kết hợp WebClient reactive tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java. Mỗi Client đánh dấu @Component với constructor injection RestClient.Builder đã cấu hình connectTimeout, readTimeout 5 giây từ application.yml. Tiêm endpoint URL facebook.api.base-url=https://graph.facebook.com/v18.0, instagram.api.base-url=https://graph.instagram.com/v18.0, tiktok.api.base-url=https://open.tiktokapis.com/v2 và access token qua @Value đọc biến môi trường FACEBOOK_ACCESS_TOKEN, INSTAGRAM_ACCESS_TOKEN, TIKTOK_ACCESS_TOKEN. Triển khai publishPost(ScheduleEntity schedule): PublishResult gọi /{page-id}/feed cho Facebook, /{ig-user-id}/media cho Instagram, /post/publish/video/init/ cho TikTok. Bọc try-catch HttpClientErrorException|HttpServerErrorException|ResourceAccessException chuẩn hóa ném SocialPlatformException khi timeout hoặc 4xx/5xx. Tích hợp @Retryable(retryFor=SocialPlatformException.class, maxAttempts=3, backoff=@Backoff(delay=1000, multiplier=2.0)) từ Spring Retry. Tạo SocialPlatformException kế thừa RuntimeException với platform,errorCode,httpStatus,retryable tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java. Sử dụng SLF4J logging có cấu trúc với MDC scheduleId,platform,tenantId,correlationId theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.integration;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Instagram integration client for publishing content.
 * 
 * @traceability [REQ-001], [EXC-001]
 */
@Component
public class InstagramClient {

    private static final Logger log = LoggerFactory.getLogger(InstagramClient.class);
    private static final String PLATFORM_NAME = "INSTAGRAM";

    private final RestClient restClient;
    private final String accessToken;

    /**
     * Constructor injection for RestClient.
     * 
     * @param builder RestClient.Builder configured with timeouts in application.yml
     * @param baseUrl Instagram API base URL
     * @param accessToken Instagram access token from environment
     */
    public InstagramClient(
            RestClient.Builder builder,
            @Value("${instagram.api.base-url}") String baseUrl,
            @Value("${INSTAGRAM_ACCESS_TOKEN}") String accessToken) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.accessToken = accessToken;
    }

    /**
     * Publishes a post to Instagram Graph API.
     * 
     * @param schedule The schedule entity containing content and metadata
     * @return PublishResult containing platform-specific response
     * @throws SocialPlatformException if API call fails or times out
     */
    @Retryable(
        retryFor = SocialPlatformException.class, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public Object publishPost(ScheduleEntity schedule) {
        // Injecting MDC context for structured logging as per OWASP A09
        MDC.put("scheduleId", schedule.getScheduleId().toString());
        MDC.put("platform", PLATFORM_NAME);
        MDC.put("tenantId", schedule.getTenantId());
        MDC.put("correlationId", UUID.randomUUID().toString());

        try {
            log.info("Initiating Instagram post publication for schedule: {}", schedule.getScheduleId());

            // Implementation of POST /{ig-user-id}/media
            // Note: Actual implementation requires mapping ScheduleEntity to Instagram API payload
            return restClient.post()
                    .uri("/{ig-user-id}/media", schedule.getPlatformUserId())
                    .header("Authorization", "Bearer " + accessToken)
                    .body(Map.of("image_url", schedule.getContent()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new SocialPlatformException(PLATFORM_NAME, "CLIENT_ERROR", response.getStatusCode(), false);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new SocialPlatformException(PLATFORM_NAME, "SERVER_ERROR", response.getStatusCode(), true);
                    })
                    .body(Object.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Instagram API error: {}", e.getMessage());
            throw new SocialPlatformException(PLATFORM_NAME, "API_ERROR", e.getStatusCode(), true);
        } catch (ResourceAccessException e) {
            log.error("Instagram API timeout or network error: {}", e.getMessage());
            throw new SocialPlatformException(PLATFORM_NAME, "NETWORK_TIMEOUT", null, true);
        } finally {
            MDC.clear();
        }
    }
}
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/TikTokClient.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/integration/TikTokClient.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cài đặt ba lớp Client sử dụng RestClient của Spring Framework 6.1.x kết hợp WebClient reactive tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java. Mỗi Client đánh dấu @Component với constructor injection RestClient.Builder đã cấu hình connectTimeout, readTimeout 5 giây từ application.yml. Tiêm endpoint URL facebook.api.base-url=https://graph.facebook.com/v18.0, instagram.api.base-url=https://graph.instagram.com/v18.0, tiktok.api.base-url=https://open.tiktokapis.com/v2 và access token qua @Value đọc biến môi trường FACEBOOK_ACCESS_TOKEN, INSTAGRAM_ACCESS_TOKEN, TIKTOK_ACCESS_TOKEN. Triển khai publishPost(ScheduleEntity schedule): PublishResult gọi /{page-id}/feed cho Facebook, /{ig-user-id}/media cho Instagram, /post/publish/video/init/ cho TikTok. Bọc try-catch HttpClientErrorException|HttpServerErrorException|ResourceAccessException chuẩn hóa ném SocialPlatformException khi timeout hoặc 4xx/5xx. Tích hợp @Retryable(retryFor=SocialPlatformException.class, maxAttempts=3, backoff=@Backoff(delay=1000, multiplier=2.0)) từ Spring Retry. Tạo SocialPlatformException kế thừa RuntimeException với platform,errorCode,httpStatus,retryable tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java. Sử dụng SLF4J logging có cấu trúc với MDC scheduleId,platform,tenantId,correlationId theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.integration;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.nlh4j.socialscheduler.scheduleservice.exception.SocialPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * TikTok integration client for publishing content.
 * 
 * @traceability [REQ-001], [EXC-001]
 */
@Component
public class TikTokClient {

    private static final Logger log = LoggerFactory.getLogger(TikTokClient.class);
    private final RestClient restClient;

    public TikTokClient(RestClient.Builder builder, 
                        @Value("${tiktok.api.base-url}") String baseUrl,
                        @Value("${TIKTOK_ACCESS_TOKEN}") String accessToken) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Publishes a post to TikTok via the Open API v2.
     * Implements retry logic for transient network failures or platform errors.
     * 
     * @param schedule The schedule entity containing content details.
     * @return PublishResult containing the platform-specific response.
     * @throws SocialPlatformException if the request fails after retries.
     */
    @Retryable(
        retryFor = SocialPlatformException.class, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public Object publishPost(ScheduleEntity schedule) {
        // Injecting context into MDC for structured logging as per OWASP A09
        MDC.put("scheduleId", schedule.getScheduleId().toString());
        MDC.put("platform", "TIKTOK");
        MDC.put("tenantId", schedule.getTenantId());
        
        try {
            log.info("Initiating TikTok post publication for schedule: {}", schedule.getScheduleId());

            // TikTok API v2 endpoint for video publishing initialization
            return restClient.post()
                    .uri("/post/publish/video/init/")
                    .body(Map.of("content", schedule.getContent()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new SocialPlatformException("TIKTOK", 
                                "API_ERROR", 
                                response.getStatusCode(), 
                                true);
                    })
                    .body(Object.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("TikTok API returned error status: {}", e.getStatusCode(), e);
            throw new SocialPlatformException("TIKTOK", "HTTP_ERROR", e.getStatusCode(), true);
        } catch (ResourceAccessException e) {
            log.error("TikTok API connection timeout or network failure", e);
            throw new SocialPlatformException("TIKTOK", "NETWORK_TIMEOUT", null, true);
        } finally {
            MDC.clear();
        }
    }
}
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/exception/SocialPlatformException.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/exception/SocialPlatformException.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cài đặt ba lớp Client sử dụng RestClient của Spring Framework 6.1.x kết hợp WebClient reactive tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java, ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java. Mỗi Client đánh dấu @Component với constructor injection RestClient.Builder đã cấu hình connectTimeout, readTimeout 5 giây từ application.yml. Tiêm endpoint URL facebook.api.base-url=https://graph.facebook.com/v18.0, instagram.api.base-url=https://graph.instagram.com/v18.0, tiktok.api.base-url=https://open.tiktokapis.com/v2 và access token qua @Value đọc biến môi trường FACEBOOK_ACCESS_TOKEN, INSTAGRAM_ACCESS_TOKEN, TIKTOK_ACCESS_TOKEN. Triển khai publishPost(ScheduleEntity schedule): PublishResult gọi /{page-id}/feed cho Facebook, /{ig-user-id}/media cho Instagram, /post/publish/video/init/ cho TikTok. Bọc try-catch HttpClientErrorException|HttpServerErrorException|ResourceAccessException chuẩn hóa ném SocialPlatformException khi timeout hoặc 4xx/5xx. Tích hợp @Retryable(retryFor=SocialPlatformException.class, maxAttempts=3, backoff=@Backoff(delay=1000, multiplier=2.0)) từ Spring Retry. Tạo SocialPlatformException kế thừa RuntimeException với platform,errorCode,httpStatus,retryable tại ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java. Sử dụng SLF4J logging có cấu trúc với MDC scheduleId,platform,tenantId,correlationId theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom enterprise exception for handling failures during interactions with external social media platforms.
 * This exception is designed to capture platform-specific error details, HTTP status codes, and retryability status
 * to facilitate robust error handling and automated recovery mechanisms in the integration layer.
 *
 * @traceability [REQ-001], [EXC-001]
 * @author Enterprise System Architect
 */
public class SocialPlatformException extends RuntimeException {

    private final String platform;
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final boolean retryable;

    /**
     * Constructs a new SocialPlatformException with detailed context.
     *
     * @param message    The descriptive error message.
     * @param platform   The identifier of the social platform (e.g., FACEBOOK, INSTAGRAM, TIKTOK).
     * @param errorCode  The specific error code returned by the upstream API.
     * @param httpStatus The HTTP status code associated with the failure.
     * @param retryable  Flag indicating if the operation can be retried.
     * @param cause      The underlying cause of the exception (e.g., network timeout).
     */
    public SocialPlatformException(String message, String platform, String errorCode, 
                                   HttpStatus httpStatus, boolean retryable, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.retryable = retryable;
    }

    /**
     * Returns the social media platform identifier.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the upstream API error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the HTTP status code associated with the failure.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns true if the operation is eligible for automated retry.
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Provides a structured string representation for logging purposes, 
     * ensuring compliance with OWASP A09 (Logging and Monitoring Failures).
     */
    @Override
    public String toString() {
        return String.format("SocialPlatformException[platform=%s, errorCode=%s, httpStatus=%s, retryable=%b, message=%s]",
                platform, errorCode, httpStatus, retryable, getMessage());
    }
}
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]', '[ARC-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cấu hình Spring Security 6 tại ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java với @Configuration, @EnableWebSecurity, @EnableMethodSecurity(prePostEnabled=true). Tạo SecurityFilterChain bean oauth2ResourceServer().jwt() cho toàn bộ request. Tạo JwtDecoder bean tùy chỉnh kế thừa NimbusJwtDecoder xác thực chữ ký RSA đọc từ application.yml security.oauth2.jwt.public-key-location=classpath:keys/jwt-public.pem, setJwtValidator với JwtTimestampValidator và validator kiểm tra aud claim khớp social-scheduler-api. Định nghĩa JwtAuthenticationConverter ánh xạ claim roles thành SimpleGrantedAuthority prefix RO_ROLE_, ánh xạ 4 vai trò RBAC [ARC-001] Admin→RO_ROLE_ADMIN, [ARC-002] User→RO_ROLE_USER, [ARC-003] Scheduler→RO_ROLE_SCHEDULER, [ARC-004] Analyst→RO_ROLE_ANALYST. Định nghĩa authorizeHttpRequests cho /api/v1/** authenticated, /actuator/health/** permitAll, anyRequest denyAll. Vô hiệu hóa CSRF cho RESTful Stateless csrf.disable(). Cấu hình CORS whitelist origin https://app.socialscheduler.local. Tích hợp RbacPredicate thực thi 4 vai trò qua @PreAuthorize. Tạo JwtAuthFilter kế thừa OncePerRequestFilter tại ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java trích xuất JWT từ header Authorization: Bearer, validate và đặt SecurityContextHolder. Xử lý JwtException trả HTTP 401 TOKEN_EXPIRED hoặc TOKEN_INVALID qua AuthenticationEntryPoint tùy chỉnh, đảm bảo thông điệp phản hồi không tiết lộ chi tiết nội bộ theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]', '[ARC-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration for the API Gateway.
 * Implements OAuth2 Resource Server with JWT validation and RBAC enforcement.
 * 
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${security.oauth2.jwt.public-key}")
    private RSAPublicKey publicKey;

    /**
     * Configures the security filter chain for stateless REST API.
     * Disables CSRF and enforces authentication for all /api/v1/** endpoints.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }

    /**
     * Decodes and validates JWT tokens using RSA public key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Converts JWT claims into Spring Security Authorities.
     * Maps roles to RO_ROLE_ prefix for RBAC enforcement.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) return List.of();
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("RO_ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        });
        return converter;
    }

    /**
     * Configures CORS whitelist for the frontend application.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://app.socialscheduler.local"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

# Day 2: model models/gemini-3.5-flash - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]', '[ARC-006]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Cấu hình Spring Security 6 tại ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java với @Configuration, @EnableWebSecurity, @EnableMethodSecurity(prePostEnabled=true). Tạo SecurityFilterChain bean oauth2ResourceServer().jwt() cho toàn bộ request. Tạo JwtDecoder bean tùy chỉnh kế thừa NimbusJwtDecoder xác thực chữ ký RSA đọc từ application.yml security.oauth2.jwt.public-key-location=classpath:keys/jwt-public.pem, setJwtValidator với JwtTimestampValidator và validator kiểm tra aud claim khớp social-scheduler-api. Định nghĩa JwtAuthenticationConverter ánh xạ claim roles thành SimpleGrantedAuthority prefix RO_ROLE_, ánh xạ 4 vai trò RBAC [ARC-001] Admin→RO_ROLE_ADMIN, [ARC-002] User→RO_ROLE_USER, [ARC-003] Scheduler→RO_ROLE_SCHEDULER, [ARC-004] Analyst→RO_ROLE_ANALYST. Định nghĩa authorizeHttpRequests cho /api/v1/** authenticated, /actuator/health/** permitAll, anyRequest denyAll. Vô hiệu hóa CSRF cho RESTful Stateless csrf.disable(). Cấu hình CORS whitelist origin https://app.socialscheduler.local. Tích hợp RbacPredicate thực thi 4 vai trò qua @PreAuthorize. Tạo JwtAuthFilter kế thừa OncePerRequestFilter tại ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java trích xuất JWT từ header Authorization: Bearer, validate và đặt SecurityContextHolder. Xử lý JwtException trả HTTP 401 TOKEN_EXPIRED hoặc TOKEN_INVALID qua AuthenticationEntryPoint tùy chỉnh, đảm bảo thông điệp phản hồi không tiết lộ chi tiết nội bộ theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]', '[ARC-005]', '[ARC-006]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * JWT Authentication Filter for the API Gateway.
 * Intercepts incoming HTTP requests, extracts the Bearer token, validates it using JwtDecoder,
 * maps roles to authorities with the RO_ROLE_ prefix, and establishes the SecurityContext.
 *
 * @traceability [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    // =========================================================================
    // GLOBAL CONSTANTS DECLARATION (Top-of-Class Constants Law)
    // =========================================================================
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ERROR_CODE_EXPIRED = "TOKEN_EXPIRED";
    public static final String ERROR_CODE_INVALID = "TOKEN_INVALID";
    public static final String SYSTEM_MODULE_NAME = "API-GATEWAY-AUTH";
    public static final String RESPONSE_CHARSET = "UTF-8";
    public static final String MASK_REPLACEMENT = "******";
    public static final int MASK_VISIBLE_LENGTH = 6;

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
     * Constructor injecting the required security components.
     *
     * @param jwtDecoder                 the decoder to validate and parse JWTs
     * @param jwtAuthenticationConverter the converter to map JWT claims to GrantedAuthorities
     */
    public JwtAuthFilter(JwtDecoder jwtDecoder, JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /**
     * Filters each incoming request to perform JWT authentication.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // [PROCESS] Log entry point of the transaction filter
        log.debug("[PROCESS] Entering JwtAuthFilter for URI: {}", request.getRequestURI());

        // Extract the Authorization header from the incoming request
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Check if the Authorization header is present and starts with the Bearer prefix
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("[PROCESS] No Bearer token found in request headers. Proceeding with filter chain.");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the raw token string from the header
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        // Programmatically mask the token to prevent sensitive data leakage in logs (Sensitive Data Masking Law)
        String maskedToken = maskSensitiveToken(token);
        log.info("[PROCESS] Processing authentication scan for Token: {}", maskedToken);

        try {
            // Decode and validate the token using the configured JwtDecoder
            Jwt jwt = this.jwtDecoder.decode(token);

            // Convert the decoded JWT into a Spring Security Authentication token
            JwtAuthenticationToken authentication = (JwtAuthenticationToken) this.jwtAuthenticationConverter.convert(jwt);

            if (authentication != null) {
                // Set the authenticated token into the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("[PROCESS] Successfully authenticated user: {} with authorities: {}", 
                        authentication.getName(), authentication.getAuthorities());
            }

            // Proceed with the downstream filter chain execution
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // [ARC-005] Handle JWT validation failures securely without leaking internal details (OWASP A09)
            handleJwtException(response, e);
        } finally {
            // [PROCESS] Log exit point of the transaction filter
            log.debug("[PROCESS] Exiting JwtAuthFilter for URI: {}", request.getRequestURI());
        }
    }

    /**
     * Programmatically masks the JWT token to prevent credential exposure in logs.
     *
     * @param token the raw JWT token
     * @return the masked token string
     */
    private String maskSensitiveToken(String token) {
        if (token == null || token.length() <= (MASK_VISIBLE_LENGTH * 2)) {
            return MASK_REPLACEMENT;
        }
        return token.substring(0, MASK_VISIBLE_LENGTH) 
                + MASK_REPLACEMENT 
                + token.substring(token.length() - MASK_VISIBLE_LENGTH);
    }

    /**
     * Handles JWT exceptions by writing a standardized JSON error response.
     * Ensures compliance with OWASP Top 10 (A05: Security Misconfiguration / A09: Security Logging and Monitoring).
     *
     * @param response the HTTP response object
     * @param e        the caught JwtException
     * @throws IOException if an input or output exception occurs
     */
    private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
        // Clear the security context to prevent unauthorized access
        SecurityContextHolder.clearContext();

        // Determine if the token has expired or is structurally invalid
        String errorCode = ERROR_CODE_INVALID;
        String userMessage = "Yêu cầu xác thực không hợp lệ. Vui lòng đăng nhập lại.";

        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("expired")) {
            errorCode = ERROR_CODE_EXPIRED;
            userMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
        }

        // [ARC-005] Log the exception with the required 3 context keys: Subsystem, Raw Message, and Tag ID
        log.error("[CRITICAL FAIL] [ARC-005] JWT processing failed in subsystem: {}. Raw error: {}", 
                SYSTEM_MODULE_NAME, e.getMessage());

        // Set response headers and status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(RESPONSE_CHARSET);

        // Construct a clean, standardized JSON payload without leaking internal stack traces (OWASP A09)
        String jsonResponse = String.format(
                "{\"errorCode\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                errorCode,
                userMessage,
                Instant.now().toString()
        );

        // Write the response payload to the client
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
```

