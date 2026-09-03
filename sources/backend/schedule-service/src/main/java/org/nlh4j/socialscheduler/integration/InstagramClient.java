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