# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java đánh dấu @Service, @Transactional(readOnly = true) và @Slf4j. Inject các dependency thông qua constructor: RedisTokenBucketStrategy tokenBucketStrategy, CaffeineCacheManager cacheManager. Triển khai phương thức chính RateLimitResult checkRateLimit(UUID userId, String endpoint): (Bước 1) Sinh cache key theo định dạng rateLimit:{userId}:{endpoint}; (Bước 2) Kiểm tra cache với TTL 1 giây thông qua @Cacheable(cacheNames = "rateLimitCache", key = "#userId + \':\' + #endpoint", sync = true) [REQ-003]; (Bước 3) Nếu cache miss, gọi tokenBucketStrategy.tryConsume(userId.toString(), endpoint, 1); (Bước 4) Trả về RateLimitResult chứa allowed, remainingTokens, retryAfterSeconds; (Bước 5) Ghi log INFO cho allowed=true và WARN cho allowed=false [EXC-005]. Triển khai phương thức resetLimit(UUID userId, String endpoint) annotated @PreAuthorize("hasRole(\'ADMIN\')") gọi tokenBucketStrategy.reset() và xóa cache entry. Bổ sung metric Micrometer rate_limit.check.duration (timer) và rate_limit.reset.total (counter). Cấu hình Caffeine cache tại RateLimitCacheConfig với expireAfterWrite=1s, maximumSize=50000. Structured logging với MDC context correlationId, userId, endpoint.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.ratelimitservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResult;
import org.nlh4j.socialscheduler.ratelimitservice.strategy.RedisTokenBucketStrategy;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for managing API rate limiting using Redis Token Bucket strategy.
 * 
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class RateLimiterService {

    private final RedisTokenBucketStrategy tokenBucketStrategy;
    private final Timer checkDurationTimer;
    private final Counter resetCounter;

    /**
     * Constructor injection for required dependencies and Micrometer metrics.
     */
    public RateLimiterService(RedisTokenBucketStrategy tokenBucketStrategy, MeterRegistry meterRegistry) {
        this.tokenBucketStrategy = tokenBucketStrategy;
        this.checkDurationTimer = meterRegistry.timer("rate_limit.check.duration");
        this.resetCounter = meterRegistry.counter("rate_limit.reset.total");
    }

    /**
     * Checks if the request is allowed based on the rate limit strategy.
     * Uses Caffeine cache to optimize performance for high-frequency checks.
     * 
     * @param userId   The unique identifier of the user.
     * @param endpoint The API endpoint being accessed.
     * @return RateLimitResult containing status and retry information.
     */
    @Cacheable(cacheNames = "rateLimitCache", key = "#userId + ':' + #endpoint", sync = true)
    public RateLimitResult checkRateLimit(UUID userId, String endpoint) {
        // Start timer for performance monitoring
        return checkDurationTimer.record(() -> {
            // Setup MDC context for structured logging
            MDC.put("userId", userId.toString());
            MDC.put("endpoint", endpoint);

            try {
                // Execute strategy to consume token
                RateLimitResult result = tokenBucketStrategy.tryConsume(userId.toString(), endpoint, 1);

                if (result.isAllowed()) {
                    log.info("[PROCESS] Rate limit check passed for user: {}", userId);
                } else {
                    // [EXC-005] Log warning when rate limit is exceeded
                    log.warn("[CRITICAL FAIL] [EXC-005] Rate limit exceeded for user: {} on endpoint: {}", userId, endpoint);
                }

                return result;
            } finally {
                MDC.remove("userId");
                MDC.remove("endpoint");
            }
        });
    }

    /**
     * Resets the rate limit for a specific user and endpoint.
     * Restricted to ADMIN role only.
     * 
     * @param userId   The unique identifier of the user.
     * @param endpoint The API endpoint to reset.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "rateLimitCache", key = "#userId + ':' + #endpoint")
    public void resetLimit(UUID userId, String endpoint) {
        log.info("[ADMIN] Resetting rate limit for user: {} on endpoint: {}", userId, endpoint);
        
        // Execute reset strategy
        tokenBucketStrategy.reset(userId.toString(), endpoint);
        
        // Increment reset counter metric
        resetCounter.increment();
    }
}
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java với annotation @RestController và @RequestMapping("/api/v1/rate-limits"). Triển khai hai endpoint: (1) POST /check (đường dẫn đầy đủ POST /api/v1/rate-limits/check) nhận RateLimitCheckRequestDto annotated @Valid kích hoạt Jakarta Validation, gọi RateLimiterService.checkRateLimit(request.userId(), request.endpoint()) và trả về ResponseEntity<RateLimitCheckResponseDto> với HTTP 200 chứa remainingTokens và retryAfterSeconds [REQ-003]. (2) POST /reset (đường dẫn đầy đủ POST /api/v1/rate-limits/reset) chỉ khả dụng cho vai trò Admin với annotation @PreAuthorize("hasRole(\'ADMIN\')") [EXC-005], nhận RateLimitResetRequestDto chứa userId và endpoint, gọi RateLimiterService.resetLimit() và trả về ResponseEntity<RateLimitResetResponseDto> với success=true và message="Giới hạn tỷ lệ đã được đặt lại thành công.". Bổ sung annotation @Operation và @ApiResponse từ Springdoc OpenAPI. Inject RateLimiterService thông qua constructor. Sử dụng @Slf4j ghi log có cấu trúc với MDC context userId, endpoint, correlationId theo OWASP A09. Đối với endpoint /check, controller xử lý RateLimitExceededException bằng cách để GlobalExceptionHandler tại schedule-service bắt và chuyển đổi thành HTTP 429 với header Retry-After.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.ratelimitservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitCheckRequestDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitCheckResponseDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResetRequestDto;
import org.nlh4j.socialscheduler.ratelimitservice.dto.RateLimitResetResponseDto;
import org.nlh4j.socialscheduler.ratelimitservice.service.RateLimiterService;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsible for managing and checking API rate limits.
 * 
 * @traceability [REQ-003], [EXC-005]
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rate-limits")
@RequiredArgsConstructor
@Tag(name = "Rate Limit Management", description = "Endpoints for checking and resetting API rate limits")
public class RateLimitController {

    private final RateLimiterService rateLimiterService;

    private static final String MDC_USER_ID = "userId";
    private static final String MDC_ENDPOINT = "endpoint";

    /**
     * Checks if the current request is within the allowed rate limit threshold.
     * 
     * @param request The request containing userId and endpoint to check.
     * @return ResponseEntity containing remaining tokens and retry information.
     * @traceability [REQ-003]
     */
    @Operation(summary = "Check rate limit", description = "Verifies if the user has remaining tokens for the specified endpoint.")
    @ApiResponse(responseCode = "200", description = "Request within limits")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @PostMapping("/check")
    public ResponseEntity<RateLimitCheckResponseDto> checkRateLimit(@Valid @RequestBody RateLimitCheckRequestDto request) {
        // Injecting context into MDC for structured logging (OWASP A09)
        MDC.put(MDC_USER_ID, request.userId().toString());
        MDC.put(MDC_ENDPOINT, request.endpoint());
        
        log.info("Checking rate limit for user: {} on endpoint: {}", request.userId(), request.endpoint());
        
        try {
            RateLimitCheckResponseDto response = rateLimiterService.checkRateLimit(request.userId(), request.endpoint());
            return ResponseEntity.ok(response);
        } finally {
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_ENDPOINT);
        }
    }

    /**
     * Resets the rate limit for a specific user and endpoint.
     * Restricted to ADMIN role.
     * 
     * @param request The request containing userId and endpoint to reset.
     * @return ResponseEntity indicating success.
     * @traceability [EXC-005]
     */
    @Operation(summary = "Reset rate limit", description = "Admin-only endpoint to reset rate limits for a user.")
    @ApiResponse(responseCode = "200", description = "Rate limit reset successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset")
    public ResponseEntity<RateLimitResetResponseDto> resetLimit(@Valid @RequestBody RateLimitResetRequestDto request) {
        MDC.put(MDC_USER_ID, request.userId().toString());
        MDC.put(MDC_ENDPOINT, request.endpoint());
        
        log.warn("Admin resetting rate limit for user: {} on endpoint: {}", request.userId(), request.endpoint());
        
        rateLimiterService.resetLimit(request.userId(), request.endpoint());
        
        RateLimitResetResponseDto response = new RateLimitResetResponseDto(
            true, 
            "Giới hạn tỷ lệ đã được đặt lại thành công."
        );
        
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_ENDPOINT);
        
        return ResponseEntity.ok(response);
    }
}
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java kế thừa AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config> để can thiệp trước khi yêu cầu đến microservice [REQ-003]. Khai báo lớp static Config chứa các thuộc tính: capacity (mặc định 100), refillRatePerMinute (mặc định 60), enabledEndpoints (danh sách các route áp dụng filter, mặc định /api/v1/schedules/**, /api/v1/recommendations/**, /api/v1/ai/**). Override apply(Config config) trả về GatewayFilter với luồng xử lý: (Bước 1) Kiểm tra path hiện tại có nằm trong config.enabledEndpoints không; (Bước 2) Trích xuất JWT token từ header Authorization: Bearer <token>; (Bước 3) Parse JWT token bằng JwtDecoder lấy claim sub (userId) và tenant_id; (Bước 4) Gọi RateLimiterService.checkRateLimit(userId, requestPath) thông qua WebClient hoặc OpenFeign client với timeout 100ms; (Bước 5) Nếu allowed=false, đặt HTTP status 429, header Retry-After: {retryAfterSeconds}, body JSON chứa errorCode="RATE_LIMIT_EXCEEDED", message, timestamp, correlationId và return; (Bước 6) Ngược lại chain.filter(exchange) [EXC-005]. Bổ sung metric Micrometer gateway.rate_limit.requests.total với tag outcome=allowed|blocked. Đăng ký filter với tên RateLimit trong application-gateway.yml. Đảm bảo filter hoạt động bất đồng bộ (reactive) không chặn event loop.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model models/gemini-3.1-flash-lite - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java`
*   Traceability Audit Tags For This Task: ['[EXC-002]', '[EXC-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java đánh dấu @RestControllerAdvice và @Slf4j để bắt tất cả ngoại lệ từ các controller trong schedule-service [EXC-002]. Triển khai bốn phương thức: (1) @ExceptionHandler(MethodArgumentNotValidException.class) trích xuất BindingResult.getFieldErrors() tạo ValidationErrorResponse chứa errorCode="VALIDATION_FAILED", message="Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại các trường được đánh dấu.", fieldErrors array chứa {field, rejectedValue, errorMessage}, trả về HTTP 400 [EXC-002]. (2) @ExceptionHandler(JwtException.class) trả về ErrorResponse với errorCode="TOKEN_EXPIRED", message="Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục.", HTTP 401 [EXC-002]. (3) @ExceptionHandler({SocialPlatformException.class, HttpServerErrorException.class, ResourceAccessException.class}) trả về ErrorResponse với errorCode="UPSTREAM_SERVICE_ERROR", message="Dịch vụ bên thứ ba tạm thời không khả dụng. Hệ thống sẽ tự động thử lại.", HTTP 502, kích hoạt cơ chế thử lại với backoff lũy thừa thông qua @Retry của Resilience4j [EXC-003]. (4) @ExceptionHandler(RateLimitExceededException.class) trả về HTTP 429 với errorCode="RATE_LIMIT_EXCEEDED", message="Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ cho phép. Vui lòng thử lại sau {retryAfterSeconds} giây.", header Retry-After [EXC-005]. Mỗi phản hồi kèm timestamp=OffsetDateTime.now() và correlationId từ MDC context hoặc header X-Correlation-Id theo OWASP A09. Bổ sung @ExceptionHandler(Exception.class) trả về HTTP 500 với errorCode="INTERNAL_SERVER_ERROR" không tiết lộ stack trace theo OWASP A09.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[EXC-002]', '[EXC-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.scheduleservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.nlh4j.socialscheduler.ratelimitservice.exception.RateLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the schedule-service.
 * Provides centralized error handling and standardized API error responses.
 * 
 * @traceability [EXC-002], [EXC-003], [EXC-005]
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    /**
     * Handles validation errors for incoming request DTOs.
     * [EXC-002]
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("[VALIDATION_FAILED] [EXC-002] Input validation failed: {}", ex.getMessage());
        
        List<Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        String.valueOf(error.getRejectedValue()),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());

        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại các trường được đánh dấu.",
                fieldErrors,
                OffsetDateTime.now(),
                getCorrelationId());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles expired or invalid JWT tokens.
     * [EXC-002]
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(JwtException ex) {
        log.warn("[TOKEN_EXPIRED] [EXC-002] Authentication failed: {}", ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "TOKEN_EXPIRED",
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles upstream service errors (OpenAI, Social Platforms).
     * [EXC-003]
     */
    @ExceptionHandler({SocialPlatformException.class, HttpServerErrorException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorResponse> handleUpstreamError(Exception ex) {
        log.error("[UPSTREAM_SERVICE_ERROR] [EXC-003] Upstream service failure: {}", ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "UPSTREAM_SERVICE_ERROR",
                "Dịch vụ bên thứ ba tạm thời không khả dụng. Hệ thống sẽ tự động thử lại.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles rate limit violations.
     * [EXC-005]
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("[RATE_LIMIT_EXCEEDED] [EXC-005] User {} exceeded limit on {}", ex.getUserId(), ex.getEndpoint());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        
        ErrorResponse response = new ErrorResponse(
                "RATE_LIMIT_EXCEEDED",
                "Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ cho phép. Vui lòng thử lại sau " + ex.getRetryAfterSeconds() + " giây.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Generic handler for unexpected internal server errors.
     * [OWASP A09]
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("[INTERNAL_SERVER_ERROR] [EXC-000] Unexpected error: ", ex);
        
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống nội bộ. Vui lòng liên hệ quản trị viên.",
                OffsetDateTime.now(),
                getCorrelationId());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER) != null ? MDC.get(CORRELATION_ID_HEADER) : "N/A";
    }

    // Inner DTOs for structured error responses
    public record FieldErrorDetail(String field, String rejectedValue, String errorMessage) {}
    public record ValidationErrorResponse(String errorCode, String message, List<Object> fieldErrors, OffsetDateTime timestamp, String correlationId) {}
    public record ErrorResponse(String errorCode, String message, OffsetDateTime timestamp, String correlationId) {}
}
```

