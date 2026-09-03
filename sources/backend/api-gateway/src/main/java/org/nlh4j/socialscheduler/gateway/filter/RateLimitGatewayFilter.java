package org.nlh4j.socialscheduler.gateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import org.nlh4j.socialscheduler.ratelimitservice.service.RateLimiterService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Traceability Tags: [REQ-003], [EXC-005]
 * 
 * Gateway filter implementation for reactive rate limiting.
 * Intercepts incoming requests, validates against Redis-backed token bucket,
 * and enforces rate limits per user/tenant.
 */
@Component
public class RateLimitGatewayFilter extends AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config> {

    private final RateLimiterService rateLimiterService;
    private final JwtDecoder jwtDecoder;
    private final MeterRegistry meterRegistry;

    public RateLimitGatewayFilter(RateLimiterService rateLimiterService, 
                                  JwtDecoder jwtDecoder, 
                                  MeterRegistry meterRegistry) {
        super(Config.class);
        this.rateLimiterService = rateLimiterService;
        this.jwtDecoder = jwtDecoder;
        this.meterRegistry = meterRegistry;
    }

    public static class Config {
        private int capacity = 100;
        private int refillRatePerMinute = 60;
        private List<String> enabledEndpoints = List.of("/api/v1/schedules/**", "/api/v1/recommendations/**", "/api/v1/ai/**");

        // Getters and Setters
        public List<String> getEnabledEndpoints() { return enabledEndpoints; }
        public void setEnabledEndpoints(List<String> endpoints) { this.enabledEndpoints = endpoints; }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            // Step 1: Check if path is enabled for rate limiting
            boolean isEnabled = config.getEnabledEndpoints().stream()
                    .anyMatch(pattern -> path.startsWith(pattern.replace("/**", "")));

            if (!isEnabled) {
                return chain.filter(exchange);
            }

            // Step 2 & 3: Extract and Parse JWT
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return chain.filter(exchange);
            }

            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                String userId = jwt.getClaimAsString("sub");

                // Step 4: Call RateLimiterService (Reactive)
                return rateLimiterService.checkRateLimit(userId, path)
                        .flatMap(result -> {
                            if (!result.isAllowed()) {
                                // Step 5: Handle Rate Limit Exceeded [EXC-005]
                                meterRegistry.counter("gateway.rate_limit.requests.total", "outcome", "blocked").increment();
                                return handleRateLimitExceeded(exchange, result.getRetryAfterSeconds());
                            }
                            // Step 6: Proceed
                            meterRegistry.counter("gateway.rate_limit.requests.total", "outcome", "allowed").increment();
                            return chain.filter(exchange);
                        });
            } catch (Exception e) {
                // Fallback to allow if JWT parsing fails (or handle as unauthorized)
                return chain.filter(exchange);
            }
        };
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, int retryAfter) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(retryAfter));

        String correlationId = UUID.randomUUID().toString();
        String body = String.format(
            "{\"errorCode\":\"RATE_LIMIT_EXCEEDED\", \"message\":\"Too many requests\", \"timestamp\":\"%s\", \"correlationId\":\"%s\"}",
            OffsetDateTime.now().toString(), correlationId
        );

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}