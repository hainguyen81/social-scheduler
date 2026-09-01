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