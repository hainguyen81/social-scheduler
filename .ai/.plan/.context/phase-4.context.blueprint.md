<!--START_CHUNK_PART_4-->

# Giai đoạn 4: <!--PHASE_NAME_START-->Giới hạn tỷ lệ xác thực đầu vào và bộ xử lý ngoại lệ tập trung<!--PHASE_NAME_END-->

## 📊 Kiểm soát Tài liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Blueprint** | ARCH-20260831230418 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 4 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Giới hạn tỷ lệ xác thực đầu vào và bộ xử lý ngoại lệ tập trung<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Triển khai hoàn chỉnh tầng bảo vệ chủ động cho hệ thống social-scheduler thông qua xác thực payload nghiêm ngặt bằng Jakarta Validation cho mô-đun lịch đăng bài, tích hợp bộ giới hạn tỷ lệ Redis Token Bucket trả về HTTP 429 khi vượt ngưỡng, đồng thời thiết lập bộ xử lý ngoại lệ tập trung chuẩn hóa mã lỗi và thông điệp phản hồi theo nguyên tắc OWASP A04 và A05<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/08/31 23:04:18 |
| **Tác giả** | Kiến trúc sư Hệ thống Doanh nghiệp (SA Agent) |
| **Phê duyệt** | Chờ phê duyệt quản trị kỹ thuật |

## 1. Phạm vi Hoạt động & Mục tiêu Giai đoạn

Giai đoạn 4 tập trung 100% vào việc kiến tạo tầng bảo vệ chủ động cho dịch vụ lịch đăng bài và dịch vụ giới hạn tỷ lệ trong hệ thống `social-scheduler`. Phạm vi cụ thể bao gồm: (1) Khởi tạo DTO `ScheduleRequestDto` với các ràng buộc Jakarta Validation nghiêm ngặt (`@NotBlank`, `@NotNull`, `@Size`, `@Pattern`, `@Future`) để ngăn chặn injection giá trị ngoài whitelist và đảm bảo tính toàn vẹn dữ liệu đầu vào theo yêu cầu [REQ-003]; (2) Xây dựng `SchedulePayloadValidator` sử dụng `ConstraintValidator` để thực thi quy tắc nghiệp vụ phức tạp như giới hạn cửa sổ thời gian 90 ngày và whitelist domain cho `mediaUrls` chống SSRF; (3) Triển khai `RedisTokenBucketStrategy` sử dụng Redis Lua script để đảm bảo tính nguyên tử trong thao tác trừ token, cấu hình bucket mặc định 100 token và tốc độ bổ sung 60 token/phút theo [REQ-003] và [EXC-005]; (4) Tạo lớp ngoại lệ chuyên biệt `RateLimitExceededException` kế thừa `RuntimeException` với annotation `@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)`; (5) Triển khai `RateLimiterService` đóng vai trò điều phối viên giữa controller và strategy với cơ chế cache kết quả kiểm tra trong 1 giây; (6) Xây dựng `RateLimitController` với hai endpoint `POST /check` và `POST /reset` (chỉ dành cho Admin); (7) Tích hợp `RateLimitGatewayFilter` tại API Gateway để can thiệp trước khi yêu cầu đến microservice, trích xuất `userId` từ JWT đã xác thực; (8) Triển khai `GlobalExceptionHandler` sử dụng `@RestControllerAdvice` chuẩn hóa mã lỗi theo nguyên tắc OWASP A04 (Insecure Design) và A05 (Security Misconfiguration), xử lý `MethodArgumentNotValidException`, `JwtException`, `SocialPlatformException`, `RateLimitExceededException`. Tất cả các phản hồi lỗi đều kèm `timestamp` và `correlationId` để truy vết theo OWASP A09.

Giai đoạn này KHÔNG triển khai: thay đổi lược đồ cơ sở dữ liệu (bảng `rate_limits` đã được khởi tạo tại Giai đoạn 1 với [DAT-003]), module lập lịch đa nền tảng Facebook/Instagram/TikTok (đã hoàn thiện tại Giai đoạn 2), dịch vụ đề xuất nội dung AI (đã hoàn thiện tại Giai đoạn 3), hạ tầng DevOps và Terraform (ủy thác cho Giai đoạn 5), bộ mã nguồn module tích hợp OpenAI và AI service (đã hoàn thiện tại Giai đoạn 3).

## 2. Phạm vi Kỹ thuật Được phép & Ranh giới Thư mục

Danh sách kiểm tra kỹ thuật các tệp vật lý được phép tạo hoặc xử lý trong phạm vi giai đoạn này, mọi mục đều kèm mã định danh truy vết:

* `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java` — RESTful controller cho module giới hạn tỷ lệ. [REQ-003], [EXC-005]
* `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java` — Logic điều phối nghiệp vụ giới hạn tỷ lệ. [REQ-003], [EXC-005]
* `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java` — Chiến lược Redis Token Bucket với Lua script. [REQ-003], [EXC-005]
* `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java` — Ngoại lệ chuyên biệt cho giới hạn tỷ lệ. [EXC-005]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java` — DTO payload lịch đăng bài với Jakarta Validation. [REQ-003], [EXC-002]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java` — Bộ xác thực nghiệp vụ cho payload lịch. [REQ-003], [EXC-002]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java` — Bộ xử lý ngoại lệ tập trung cho schedule-service. [EXC-002], [EXC-003], [EXC-005]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java` — Gateway filter tích hợp giới hạn tỷ lệ tại API Gateway. [REQ-003], [EXC-005]
* `./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java` — Bộ kiểm thử đơn vị cho chiến lược Token Bucket. [REQ-003], [EXC-005]
* `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorTest.java` — Bộ kiểm thử đơn vị cho DTO và validator. [REQ-003], [EXC-002]
* `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java` — Bộ kiểm thử tích hợp cho Global Exception Handler. [REQ-003], [EXC-002], [EXC-003], [EXC-005]
* `./sources/docs/api/ValidationAndRateLimitContract.yaml` — Hợp đồng OpenAPI 3.0 cho Validation và Rate Limit. [DOC-001], [REQ-003]

## 3. Chỉ thị Chức năng Chuyên biệt cho Sub-Agent

Phân bổ nhiệm vụ và ràng buộc kỹ thuật cho từng persona Sub-Agent hoạt động trong giai đoạn này:

* **Coder**: Hoạt động với vai trò Nhà phát triển Ứng dụng Cao cấp, chịu trách nhiệm triển khai toàn bộ mã nguồn ứng dụng cho `rate-limit-service` (controller, service, strategy, exception) và `schedule-service` (DTO, validator, global exception handler) cùng `RateLimitGatewayFilter` tại API Gateway. Bị cấm viết bộ kiểm thử, tệp Terraform, Docker manifest hoặc tài liệu OpenAPI.
* **Tester**: Hoạt động với vai trò Trưởng phòng QC/QA, chuyên trách kỹ thuật bộ kiểm thử JUnit 5 và Mockito cho `RedisTokenBucketStrategy`, `SchedulePayloadValidator`, `ScheduleRequestDto` và tích hợp `GlobalExceptionHandler` thông qua `@WebMvcTest` với MockMvc. Bị cấm sửa đổi mã nguồn sản phẩm.
* **Doc**: Hoạt động với vai trò Chuyên gia Viết tài liệu Kỹ thuật, chịu trách nhiệm biên soạn hợp đồng OpenAPI 3.0 cho module Validation và Rate Limit tại `./sources/docs/api/ValidationAndRateLimitContract.yaml`, kèm tài liệu hóa tất cả mã lỗi `VALIDATION_FAILED`, `TOKEN_EXPIRED`, `UPSTREAM_SERVICE_ERROR`, `RATE_LIMIT_EXCEEDED`.
* **Reviewer**: Chịu trách nhiệm xác minh biên dịch, phân tích tĩnh mã nguồn, đánh giá tuân thủ OWASP Top 10 (A03, A04, A05), phát hiện hardcode thông tin nhạy cảm, đánh giá hiệu suất thuật toán Redis Token Bucket dưới tải 1000 request/giây, xác minh nguyên tử của Lua script.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)

Giai đoạn 4 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng khách quan sau:

* `rate-limit-service` và `schedule-service` biên dịch sạch thông qua `mvn -f ./sources/backend/rate-limit-service/pom.xml compile` và `mvn -f ./sources/backend/schedule-service/pom.xml compile`.
* Hai endpoint RESTful (`POST /api/v1/rate-limits/check`, `POST /api/v1/rate-limits/reset`) trả về đúng mã HTTP 200/401/403/429 theo đặc tả OpenAPI.
* `RedisTokenBucketStrategy` hoàn tất thao tác trừ token với tính nguyên tử thông qua Lua script; khi bucket rỗng, ném `RateLimitExceededException` với `retryAfterSeconds` chính xác.
* `RateLimitGatewayFilter` can thiệp thành công tại API Gateway, trích xuất `userId` từ JWT token và trả về HTTP 429 với header `Retry-After` khi vượt ngưỡng.
* `SchedulePayloadValidator` từ chối đúng các payload có `scheduledTime` ở quá khứ, quá xa trong tương lai (trên 90 ngày), `platform` không thuộc whitelist, `content` vượt quá 5000 ký tự, `mediaUrls` chứa domain ngoài whitelist.
* `GlobalExceptionHandler` xử lý chính xác bốn loại ngoại lệ: `MethodArgumentNotValidException` (400), `JwtException` (401), `SocialPlatformException` (502), `RateLimitExceededException` (429) với `timestamp` và `correlationId` cho mỗi phản hồi.
* Tất cả các mã định danh truy vết `[REQ-003]`, `[EXC-002]`, `[EXC-003]`, `[EXC-005]`, `[DOC-001]` được ánh xạ 1:1 vào các tệp vật lý tương ứng.
* Độ phủ kiểm thử đơn vị đạt tối thiểu 85% cho `RedisTokenBucketStrategy`, `SchedulePayloadValidator` và `GlobalExceptionHandler` theo yêu cầu của cổng chất lượng SonarQube.
* Hợp đồng OpenAPI 3.0 `./sources/docs/api/ValidationAndRateLimitContract.yaml` được tạo ra với đầy đủ schema, phản hồi mẫu, tham chiếu bảo mật `bearerAuth` và mã lỗi 400/401/429/502.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo Rate Limiter, DTO lịch đăng bài và Bộ xử lý Ngoại lệ Tập trung<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 1.1: Triển khai DTO lịch đăng bài với ràng buộc Jakarta Validation nghiêm ngặt
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `ScheduleRequestDto` tại đường dẫn `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java` sử dụng Java Record (`public record ScheduleRequestDto(...)`) với các annotation Jakarta Validation 3.0 nghiêm ngặt. Trường `platform` (String) phải annotate `@NotBlank(message = "platform is required") @Pattern(regexp = "^(Facebook|Instagram|TikTok)$", message = "platform must be one of Facebook, Instagram, TikTok")` để ngăn chặn injection giá trị ngoài whitelist theo [REQ-003]. Trường `content` (String) annotate `@NotBlank(message = "content cannot be blank") @Size(min = 1, max = 5000, message = "content must not exceed 5000 characters")`. Trường `scheduledTime` (OffsetDateTime) annotate `@NotNull(message = "scheduledTime is required") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") @Future(message = "scheduledTime must be in the future")` để đảm bảo thời gian đăng bài luôn ở tương lai [EXC-002]. Trường `mediaUrls` (List<String>) annotate `@Size(max = 10, message = "mediaUrls must not exceed 10 items")` cho phép tối đa 10 URL đính kèm. Trường `tenantId` (UUID) annotate `@NotNull(message = "tenantId is required")` để đảm bảo cô lập dữ liệu đa tenant theo [NFR-003]. Áp dụng annotation `@Builder` (Lombok) để hỗ trợ khởi tạo từ controller layer, `@JsonInclude(JsonInclude.Include.NON_NULL)` để loại bỏ trường null khỏi payload phản hồi. Đảm bảo DTO tương thích với Jackson serialization mặc định và có thể được validate tự động bởi `@Valid` annotation tại controller layer.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```json
{
  "endpoint": "POST /api/v1/schedules",
  "headers": {
    "Authorization": "Bearer {jwt_token}",
    "Content-Type": "application/json",
    "X-Tenant-Id": "uuid"
  },
  "request_payload": {
    "tenantId": "uuid (required, @NotNull)",
    "platform": "Facebook | Instagram | TikTok (required, @NotBlank, @Pattern)",
    "content": "string (required, @NotBlank, @Size 1-5000)",
    "scheduledTime": "ISO-8601 timestamp (required, @NotNull, @Future)",
    "mediaUrls": "array of string (optional, @Size max=10)"
  },
  "error_responses": {
    "400": "Bad Request - Validation failed (VALIDATION_FAILED)",
    "401": "Unauthorized - JWT token invalid or expired (TOKEN_EXPIRED)",
    "429": "Too Many Requests - Rate limit exceeded (RATE_LIMIT_EXCEEDED)"
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 1.2: Triển khai bộ xác thực payload với ConstraintValidator và whitelist domain chống SSRF
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo ba tệp nguồn tại package `validator`: (1) `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidator.java` đóng vai trò annotation marker `@interface ValidSchedulePayload` với các thuộc tính `@Constraint(validatedBy = {})` trỏ tới validator class; (2) `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorImpl.java` implements `ConstraintValidator<ValidSchedulePayload, ScheduleRequestDto>` để thực thi quy tắc nghiệp vụ phức tạp không thể biểu diễn bằng annotation đơn lẻ. Logic xác thực bao gồm: kiểm tra `scheduledTime` không nằm trong khoảng 90 ngày tới (`ChronoUnit.DAYS.between(LocalDateTime.now(), request.scheduledTime()) <= 90`) theo [REQ-003]; xác thực `mediaUrls` chỉ chứa các URL thuộc whitelist domain được cấu hình tĩnh trong `application-schedule-service.yml` dưới key `app.security.media-url-whitelist` (mặc định: `cdn.socialscheduler.com`, `s3.socialscheduler.com`, `storage.googleapis.com/social-scheduler-prod`) để ngăn chặn SSRF; xác thực `content` không chứa các pattern nguy hiểm như `<script>`, `javascript:`, `data:text/html` thông qua regex whitelist cho phép chỉ các ký tự chữ, số, dấu câu phổ biến và emoji theo OWASP A03; kiểm tra `platform` và `content` phù hợp với nhau (ví dụ: TikTok không cho phép `content` vượt quá 2200 ký tự). Phương thức `isValid()` trả về `true` nếu tất cả quy tắc thỏa mãn, ngược lại ghi log cảnh báo với correlation ID ở cấp `WARN` và ném `ConstraintViolationException` với thông điệp rõ ràng [EXC-002]. (3) Inject validator vào DTO thông qua annotation `@ValidSchedulePayload` trên lớp `ScheduleRequestDto`. Đảm bảo validator hoạt động đồng bộ với `@Valid` tại controller layer.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 1.3: Tích hợp chiến lược Redis Token Bucket với Lua script đảm bảo tính nguyên tử
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java` được đánh dấu `@Component` và `@Slf4j` (Lombok). Triển khai interface `RateLimitStrategy` với phương thức `RateLimitResult tryConsume(String userId, String endpoint, int tokens)` trả về đối tượng chứa `allowed`, `remainingTokens`, `retryAfterSeconds`. Inject `RedisTemplate<String, String>` thông qua constructor, đã được cấu hình với `LettuceConnectionFactory` tại `RedisConfig`. Tải Lua script từ classpath resource `./sources/backend/rate-limit-service/src/main/resources/scripts/token-bucket.lua` thông qua `DefaultRedisScript<Long>` bean. Script Lua phải đảm bảo tính nguyên tử bằng cách sử dụng `EVALSHA` hoặc `EVAL` của Redis với logic: (1) Kiểm tra sự tồn tại của khóa `rate_limit:{userId}:{endpoint}`; (2) Nếu chưa tồn tại, khởi tạo bucket với `capacity = 100`, `tokens = capacity`, `lastRefillTimestamp = now`; (3) Tính số token cần bổ sung dựa trên `refillRate = 60 token/phút` và thời gian trôi qua kể từ `lastRefillTimestamp`; (4) Cập nhật `tokens = min(capacity, tokens + refillAmount)`; (5) Nếu `tokens >= requestedTokens`, trừ token và trả về `{1, remainingTokens}`; (6) Ngược lại, trả về `{0, 0}` và `retryAfterSeconds = ceil((requestedTokens - tokens) / refillRate * 60)`. Cấu hình tham số bucket thông qua `@ConfigurationProperties(prefix = "rate-limit.token-bucket")`: `capacity = 100`, `refillRatePerMinute = 60`, `keyPrefix = "rate_limit:"` [REQ-003]. Khi `tryConsume` trả về `allowed = false`, ném `RateLimitExceededException` kèm `retryAfterSeconds` [EXC-005]. Bổ sung metric Micrometer `rate_limit.tokens.consumed.total` và `rate_limit.exceeded.total` để theo dõi. Sử dụng structured logging với MDC context bao gồm `correlationId`, `userId`, `endpoint`, `tokensRequested`, `tokensRemaining` theo OWASP A09.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```json
{
  "external_dependency": {
    "provider": "Redis (Lettuce Client)",
    "endpoint": "EVAL token-bucket.lua KEYS[1] ARGV[...]",
    "key_format": "rate_limit:{userId}:{endpoint}",
    "atomicity": "Lua script EVAL ensures atomic operation"
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// Khi bucket rỗng, ném RateLimitExceededException với retryAfterSeconds
if (!result.isAllowed()) {
    log.warn("Rate limit exceeded for userId={} endpoint={} retryAfterSeconds={}", 
             userId, endpoint, result.getRetryAfterSeconds());
    throw new RateLimitExceededException(userId, endpoint, result.getRetryAfterSeconds());
}
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 1.4: Tạo lớp ngoại lệ RateLimitExceededException với HTTP 429 và Retry-After
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo lớp `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/exception/RateLimitExceededException.java` kế thừa `RuntimeException` với annotation `@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)` để Spring tự động trả về HTTP 429 [EXC-005]. Khai báo các trường private final: `userId` (UUID), `endpoint` (String), `retryAfterSeconds` (long), `timestamp` (OffsetDateTime). Bổ sung constructor nhận ba tham số `userId`, `endpoint`, `retryAfterSeconds` để khởi tạo giá trị và sinh `timestamp = OffsetDateTime.now()`. Triển khai các phương thức getter cho bốn trường trên. Override phương thức `getMessage()` trả về thông điệp có cấu trúc `"Rate limit exceeded for userId={userId} endpoint={endpoint} retryAfterSeconds={retryAfterSeconds}"` bằng `MessageFormat` để hỗ trợ internationalization. Áp dụng annotation `@Slf4j` (Lombok) để ghi log cảnh báo tại cấp độ `WARN` trong constructor với thông tin `userId`, `endpoint` để hỗ trợ giám sát và phát hiện lạm dụng theo OWASP A09. Bổ sung constructor phụ nhận tham số `Throwable cause` để hỗ trợ chaining exception từ Redis client. Đảm bảo lớp ngoại lệ được `GlobalExceptionHandler` tại `schedule-service` có thể bắt và chuyển đổi thành `ErrorResponse` chuẩn với HTTP 429, header `Retry-After` và thông điệp giải thích rõ ràng bằng tiếng Việt.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// RateLimitExceededException được xử lý bởi GlobalExceptionHandler
@ExceptionHandler(RateLimitExceededException.class)
public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
        RateLimitExceededException ex, HttpServletRequest request) {
    ErrorResponse error = ErrorResponse.builder()
            .errorCode("RATE_LIMIT_EXCEEDED")
            .message("Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ cho phép. Vui lòng thử lại sau " 
                    + ex.getRetryAfterSeconds() + " giây.")
            .timestamp(OffsetDateTime.now())
            .correlationId(MDC.get("correlationId"))
            .build();
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
            .body(error);
}
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 1.5: Biên soạn bộ Test Suite JUnit5 cho DTO và SchedulePayloadValidator
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java;./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải tạo lớp kiểm thử `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/validator/SchedulePayloadValidatorTest.java` sử dụng JUnit 5 kết hợp AssertJ và Jakarta Validation `ValidatorFactory` (`Validation.buildDefaultValidatorFactory()`). Sử dụng `@ExtendWith(MockitoExtension.class)` cho validator layer. Viết các trường hợp kiểm thử chi tiết: (1) `@DisplayName("validatePayload_whenAllFieldsValid_thenNoConstraintViolations")` - khởi tạo `ScheduleRequestDto` với đầy đủ trường hợp lệ (`platform = "Facebook"`, `content = "Valid content"`, `scheduledTime = now + 1 day`, `tenantId = UUID.randomUUID()`, `mediaUrls = []`), khẳng định `validator.validate(payload)` trả về danh sách rỗng [REQ-003]; (2) `@DisplayName("validatePayload_whenPlatformNotInWhitelist_thenConstraintViolation")` - thiết lập `platform = "YouTube"`, khẳng định có đúng một constraint violation với message chứa "platform must be one of Facebook, Instagram, TikTok"; (3) `@DisplayName("validatePayload_whenContentExceeds5000Characters_thenConstraintViolation")` - thiết lập `content` với chuỗi 5001 ký tự, khẳng định constraint violation với message chứa "content must not exceed 5000 characters"; (4) `@DisplayName("validatePayload_whenScheduledTimeInPast_thenConstraintViolation")` - thiết lập `scheduledTime = now - 1 hour`, khẳng định constraint violation với message chứa "scheduledTime must be in the future" [EXC-002]; (5) `@DisplayName("validatePayload_whenScheduledTimeBeyond90Days_thenConstraintViolation")` - thiết lập `scheduledTime = now + 91 days`, khẳng định constraint violation với message chứa "scheduledTime must be within 90 days" [REQ-003]; (6) `@DisplayName("validatePayload_whenMediaUrlOutsideWhitelist_thenConstraintViolation")` - thiết lập `mediaUrls = ["https://malicious-site.com/image.jpg"]`, khẳng định constraint violation với message chứa "media URL domain not in whitelist"; (7) `@DisplayName("validatePayload_whenContentContainsScriptTag_thenConstraintViolation")` - thiết lập `content = "<script>alert('xss')</script>"`, khẳng định constraint violation chống XSS theo OWASP A03 [EXC-002]; (8) `@DisplayName("validatePayload_whenMissingRequiredField_thenConstraintViolation")` - bỏ trống từng trường `@NotNull`/`@NotBlank` một, khẳng định có constraint violation tương ứng. Sử dụng `ParameterizedTest` với `@ValueSource` cho các giá trị `platform` không hợp lệ. Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho `SchedulePayloadValidator` và `ScheduleRequestDto`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Hoàn thiện Service, Controller, Gateway Filter và Tài liệu Hợp đồng OpenAPI<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 2.1: Triển khai RateLimiterService điều phối giữa controller và strategy với caching
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java` được đánh dấu `@Service`, `@Transactional(readOnly = true)` và `@Slf4j` (Lombok). Inject các dependency thông qua constructor: `RedisTokenBucketStrategy tokenBucketStrategy`, `CaffeineCacheManager cacheManager`. Triển khai phương thức chính `RateLimitResult checkRateLimit(UUID userId, String endpoint)` với luồng xử lý: (Bước 1) Sinh cache key theo định dạng `rateLimit:{userId}:{endpoint}`; (Bước 2) Kiểm tra cache với TTL 1 giây thông qua `@Cacheable(cacheNames = "rateLimitCache", key = "#userId + ':' + #endpoint", sync = true)` để giảm tải Redis khi có lưu lượng cao [REQ-003]; (Bước 3) Nếu cache miss, gọi `tokenBucketStrategy.tryConsume(userId.toString(), endpoint, 1)` để kiểm tra bucket; (Bước 4) Trả về `RateLimitResult` chứa `allowed`, `remainingTokens`, `retryAfterSeconds`; (Bước 5) Ghi log cấp độ `INFO` cho trường hợp `allowed=true` và `WARN` cho `allowed=false` [EXC-005]. Triển khai phương thức `resetLimit(UUID userId, String endpoint)` được annotate `@PreAuthorize("hasRole('ADMIN')")` để hỗ trợ quản trị viên gỡ bỏ giới hạn khi cần thiết, gọi `tokenBucketStrategy.reset(userId.toString(), endpoint)` và xóa cache entry. Bổ sung metric Micrometer `rate_limit.check.duration` (timer) và `rate_limit.reset.total` (counter với tag `actor=admin`). Cấu hình Caffeine cache tại `RateLimitCacheConfig` với `expireAfterWrite=1s`, `maximumSize=50000`. Đảm bảo tất cả phương thức đều được ghi log với structured logging bao gồm MDC context `correlationId`, `userId`, `endpoint`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```json
{
  "internal_api": {
    "method": "RateLimiterService.checkRateLimit",
    "input": {
      "userId": "UUID",
      "endpoint": "String"
    },
    "output": {
      "allowed": "boolean",
      "remainingTokens": "integer",
      "retryAfterSeconds": "long"
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.2: Triển khai RateLimitController với hai endpoint check và reset có phân quyền
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo lớp `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/controller/RateLimitController.java` với annotation `@RestController` và `@RequestMapping("/api/v1/rate-limits")`. Triển khai hai endpoint: (1) `POST /check` (đường dẫn đầy đủ `POST /api/v1/rate-limits/check`) nhận `RateLimitCheckRequestDto` được annotate `@Valid` để kích hoạt Jakarta Validation tự động, gọi `RateLimiterService.checkRateLimit(request.userId(), request.endpoint())` và trả về `ResponseEntity<RateLimitCheckResponseDto>` với mã trạng thái HTTP 200 OK chứa `remainingTokens` và `retryAfterSeconds` [REQ-003]. (2) `POST /reset` (đường dẫn đầy đủ `POST /api/v1/rate-limits/reset`) chỉ khả dụng cho vai trò Admin với annotation `@PreAuthorize("hasRole('ADMIN')")` tại cấp phương thức [EXC-005], nhận `RateLimitResetRequestDto` chứa `userId` và `endpoint`, gọi `RateLimiterService.resetLimit(...)` và trả về `ResponseEntity<RateLimitResetResponseDto>` với `success=true` và `message="Giới hạn tỷ lệ đã được đặt lại thành công."`. Bổ sung annotation `@Operation` và `@ApiResponse` từ Springdoc OpenAPI để tài liệu hóa API tự động. Inject `RateLimiterService` thông qua constructor. Sử dụng `@Slf4j` (Lombok) để ghi log có cấu trúc với MDC context bao gồm `userId`, `endpoint`, `correlationId` theo OWASP A09. Đảm bảo tính nhất quán với chuẩn RESTful: header `Authorization: Bearer <jwt_token>` được xác thực bởi API Gateway trước khi đến controller này. Đối với endpoint `/check`, controller phải xử lý ngoại lệ `RateLimitExceededException` bằng cách để `GlobalExceptionHandler` tại schedule-service bắt và chuyển đổi thành HTTP 429 với header `Retry-After`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```json
{
  "endpoint_check": {
    "method": "POST",
    "path": "/api/v1/rate-limits/check",
    "request_payload": {
      "userId": "uuid (required, @NotNull)",
      "endpoint": "string (required, @NotBlank, @Size max=255)"
    },
    "response_200": {
      "allowed": "boolean",
      "remainingTokens": "integer",
      "retryAfterSeconds": "long (0 if allowed)"
    },
    "response_429": {
      "errorCode": "RATE_LIMIT_EXCEEDED",
      "message": "string (Vietnamese)",
      "timestamp": "ISO-8601",
      "correlationId": "uuid"
    },
    "headers": {
      "Retry-After": "integer (seconds, only on 429)"
    }
  },
  "endpoint_reset": {
    "method": "POST",
    "path": "/api/v1/rate-limits/reset",
    "security": "Bearer JWT with role ADMIN",
    "request_payload": {
      "userId": "uuid (required)",
      "endpoint": "string (required)"
    },
    "response_200": {
      "success": "boolean",
      "message": "string (Vietnamese)"
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.3: Triển khai RateLimitGatewayFilter tích hợp giới hạn tỷ lệ tại API Gateway
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/filter/RateLimitGatewayFilter.java` kế thừa `AbstractGatewayFilterFactory<RateLimitGatewayFilter.Config>` để can thiệp trước khi yêu cầu đến microservice [REQ-003]. Khai báo lớp static `Config` chứa các thuộc tính cấu hình: `capacity` (mặc định 100), `refillRatePerMinute` (mặc định 60), `enabledEndpoints` (danh sách các route áp dụng filter, mặc định `/api/v1/schedules/**`, `/api/v1/recommendations/**`, `/api/v1/ai/**`). Override phương thức `apply(Config config)` trả về `GatewayFilter` với luồng xử lý: (Bước 1) Kiểm tra path hiện tại có nằm trong `config.enabledEndpoints` không; nếu không, chain.filter(exchange) và return; (Bước 2) Trích xuất JWT token từ header `Authorization: Bearer <token>`; nếu không tồn tại, chain.filter(exchange) và return; (Bước 3) Parse JWT token bằng `JwtDecoder` đã cấu hình tại Giai đoạn 2 để lấy claim `sub` (userId) và `tenant_id`; (Bước 4) Gọi `RateLimiterService.checkRateLimit(userId, requestPath)` thông qua WebClient hoặc OpenFeign client với timeout 100ms; (Bước 5) Nếu `allowed=false`, đặt HTTP status 429, header `Retry-After: {retryAfterSeconds}`, body chứa JSON `{"errorCode": "RATE_LIMIT_EXCEEDED", "message": "Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ.", "timestamp": "...", "correlationId": "..."}` và return exchange.mutate().response(...).build(); (Bước 6) Ngược lại, chain.filter(exchange) [EXC-005]. Bổ sung metric Micrometer `gateway.rate_limit.requests.total` với tag `outcome=allowed|blocked`. Đăng ký filter với tên `RateLimit` trong `application-gateway.yml` với cấu hình `spring.cloud.gateway.server.webflux.routes.filters`. Đảm bảo filter hoạt động bất đồng bộ (reactive) để không chặn event loop.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```json
{
  "gateway_filter_behavior": {
    "intercept_order": "Sau JwtAuthFilter, trước khi routing đến microservice",
    "request_header_requirements": ["Authorization: Bearer <jwt_token>"],
    "response_on_block": {
      "status": 429,
      "headers": {"Retry-After": "integer seconds"},
      "body": {
        "errorCode": "RATE_LIMIT_EXCEEDED",
        "message": "Vietnamese localized message",
        "timestamp": "ISO-8601",
        "correlationId": "uuid"
      }
    }
  }
}
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.4: Triển khai GlobalExceptionHandler xử lý tập trung bốn loại ngoại lệ theo OWASP A04/A05
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[EXC-002], [EXC-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandler.java` được đánh dấu với `@RestControllerAdvice` và `@Slf4j` (Lombok) để bắt tất cả ngoại lệ từ các controller trong `schedule-service` [EXC-002]. Triển khai bốn phương thức xử lý ngoại lệ chuyên biệt: (1) `@ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request)` trích xuất `BindingResult.getFieldErrors()` để tạo `ValidationErrorResponse` chứa `errorCode = "VALIDATION_FAILED"`, `message = "Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại các trường được đánh dấu."`, `fieldErrors` array chứa các đối tượng `{field, rejectedValue, errorMessage}` cho mỗi trường vi phạm, trả về HTTP 400 [EXC-002]. (2) `@ExceptionHandler(JwtException.class) public ResponseEntity<ErrorResponse> handleTokenExpired(JwtException ex, HttpServletRequest request)` phát hiện token hết hạn hoặc không hợp lệ, trả về `ErrorResponse` với `errorCode = "TOKEN_EXPIRED"`, `message = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục."`, HTTP 401 [EXC-002]. (3) `@ExceptionHandler({SocialPlatformException.class, HttpServerErrorException.class, ResourceAccessException.class}) public ResponseEntity<ErrorResponse> handleUpstreamError(Exception ex, HttpServletRequest request)` xử lý lỗi từ dịch vụ bên thứ ba (Facebook, Instagram, TikTok), trả về `ErrorResponse` với `errorCode = "UPSTREAM_SERVICE_ERROR"`, `message = "Dịch vụ bên thứ ba tạm thời không khả dụng. Hệ thống sẽ tự động thử lại."`, HTTP 502, đồng thời kích hoạt cơ chế thử lại với backoff lũy thừa thông qua `@Retry` annotation của Resilience4j [EXC-003]. (4) `@ExceptionHandler(RateLimitExceededException.class) public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, HttpServletRequest request)` trả về HTTP 429 với `errorCode = "RATE_LIMIT_EXCEEDED"`, `message = "Yêu cầu đã bị từ chối do vượt quá giới hạn tỷ lệ cho phép. Vui lòng thử lại sau {retryAfterSeconds} giây."`, header `Retry-After: {retryAfterSeconds}` [EXC-005]. Mỗi phản hồi lỗi đều kèm `timestamp = OffsetDateTime.now()` và `correlationId` được trích xuất từ MDC context hoặc header `X-Correlation-Id` của request để truy vết theo OWASP A09. Bổ sung `@ExceptionHandler(Exception.class) public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request)` để bắt các ngoại lệ không mong muốn, trả về HTTP 500 với `errorCode = "INTERNAL_SERVER_ERROR"` và message chung (không tiết lộ stack trace theo OWASP A09). Đảm bảo bộ xử lý có thứ tự ưu tiên từ cụ thể đến chung (Spring chọn handler gần nhất với kiểu ngoại lệ).

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// GlobalExceptionHandler xử lý tập trung tất cả ngoại lệ
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ValidationErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> FieldErrorResponse.builder()
                    .field(fe.getField())
                    .rejectedValue(String.valueOf(fe.getRejectedValue()))
                    .errorMessage(fe.getDefaultMessage())
                    .build())
            .collect(Collectors.toList());
    ValidationErrorResponse error = ValidationErrorResponse.builder()
            .errorCode("VALIDATION_FAILED")
            .message("Dữ liệu đầu vào không hợp lệ. Vui lòng kiểm tra lại các trường được đánh dấu.")
            .fieldErrors(fieldErrors)
            .timestamp(OffsetDateTime.now())
            .correlationId(MDC.get("correlationId"))
            .build();
    log.warn("Validation failed for path={} errors={}", request.getRequestURI(), fieldErrors.size());
    return ResponseEntity.badRequest().body(error);
}
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.5: Biên soạn bộ Test Suite JUnit5 cho RedisTokenBucketStrategy với Testcontainers
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategy.java;./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải tạo lớp kiểm thử `./sources/backend/rate-limit-service/src/test/java/org/nlh4j/socialscheduler/ratelimitservice/strategy/RedisTokenBucketStrategyTest.java` sử dụng JUnit 5 kết hợp Testcontainers Redis (`org.testcontainers:redis:2.2.x`) và `@DynamicPropertySource` để khởi tạo Redis container thực tế. Sử dụng `@SpringBootTest` với annotation `@Testcontainers` và `@Container static RedisContainer redis = new RedisContainer("redis:7.2-alpine")`. Inject `RedisTokenBucketStrategy` thông qua `@Autowired`. Viết các trường hợp kiểm thử: (1) `@DisplayName("tryConsume_whenBucketIsFull_thenReturnAllowedTrue")` - bucket mặc định có 100 token, gọi `tryConsume(userId, endpoint, 1)`, khẳng định `result.isAllowed() == true` và `result.getRemainingTokens() == 99`; (2) `@DisplayName("tryConsume_whenBucketIsEmpty_thenThrowRateLimitExceededException")` - tiêu thụ hết 100 token liên tiếp, gọi `tryConsume` lần thứ 101, khẳng định `RateLimitExceededException` được ném với `retryAfterSeconds` chính xác (mặc định 1 giây cho refillRate 60 token/phút) [EXC-005]; (3) `@DisplayName("tryConsume_whenTokensRefilledAfterDelay_thenBucketRestored")` - tiêu thụ hết token, sử dụng `Thread.sleep(2000)` để chờ refill, khẳng định bucket được bổ sung đúng 2 token (60 token/phút = 1 token/giây) [REQ-003]; (4) `@DisplayName("tryConsume_concurrentRequests_thenAtomicityPreserved")` - sử dụng `ExecutorService` với 100 luồng đồng thời gọi `tryConsume` cho cùng `userId`, khẳng định tổng số lần `allowed=true` không vượt quá `capacity=100`, xác minh tính nguyên tử của Lua script; (5) `@DisplayName("tryConsume_differentEndpoints_thenIndependentBuckets")` - gọi `tryConsume` với các endpoint khác nhau cho cùng `userId`, khẳng định bucket được quản lý độc lập theo từng endpoint; (6) `@DisplayName("tryConsume_multipleTokensAtOnce_thenSubtractCorrectly")` - gọi `tryConsume(userId, endpoint, 5)`, khẳng định `remainingTokens == 95`. Sử dụng `@BeforeEach` để flush Redis database và khởi tạo `RateLimiterConfig` với `capacity=100`, `refillRatePerMinute=60`. Bổ sung `@AfterEach` để cleanup. Sử dụng AssertJ cho fluent assertion chain. Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho `RedisTokenBucketStrategy`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.6: Viết bộ kiểm thử tích hợp cho GlobalExceptionHandler với MockMvc và WebMvcTest
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** INTEGRATION_SCOPE;./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-002], [EXC-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải tạo lớp kiểm thử `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/exception/GlobalExceptionHandlerIntegrationTest.java` sử dụng `@WebMvcTest(controllers = ScheduleController.class)` kết hợp `@MockBean ScheduleService` và `@Import(GlobalExceptionHandler.class)`. Inject `MockMvc` thông qua `@Autowired MockMvc mockMvc`. Sử dụng `ObjectMapper` để serialize/deserialize JSON payload. Viết các kịch bản kiểm thử: (1) `@DisplayName("handleValidationException_whenPayloadInvalid_thenReturn400")` - gửi POST request với payload thiếu trường `platform`, khẳng định response có HTTP 400, body chứa `errorCode = "VALIDATION_FAILED"`, `fieldErrors` chứa thông tin trường vi phạm, `correlationId` không null [EXC-002] [REQ-003]; (2) `@DisplayName("handleTokenExpired_whenJwtInvalid_thenReturn401")` - cấu hình `@MockBean JwtDecoder` ném `JwtException`, gửi request, khẳng định response có HTTP 401, body chứa `errorCode = "TOKEN_EXPIRED"`, `message` bằng tiếng Việt [EXC-002]; (3) `@DisplayName("handleUpstreamError_whenSocialPlatformThrows_thenReturn502")` - cấu hình `@MockBean ScheduleService.createSchedule()` ném `SocialPlatformException`, khẳng định response có HTTP 502, body chứa `errorCode = "UPSTREAM_SERVICE_ERROR"`, `correlationId` không null [EXC-003]; (4) `@DisplayName("handleRateLimitExceeded_thenReturn429WithRetryAfter")` - cấu hình `@MockBean RateLimiterService` ném `RateLimitExceededException(userId, endpoint, 60)`, gửi request, khẳng định response có HTTP 429, header `Retry-After: 60`, body chứa `errorCode = "RATE_LIMIT_EXCEEDED"` [EXC-005]; (5) `@DisplayName("handleGenericException_whenUnexpectedError_thenReturn500WithoutStackTrace")` - cấu gửi request với `@MockBean ScheduleService` ném `RuntimeException("Database connection failed")`, khẳng định response có HTTP 500, body chứa `errorCode = "INTERNAL_SERVER_ERROR"`, message KHÔNG tiết lộ stack trace hoặc thông tin nội bộ theo OWASP A09. Sử dụng `header().string("X-Correlation-Id", notNullValue())` để xác minh correlation ID có trong response header. Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho `GlobalExceptionHandler`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.7: Rà soát mã nguồn và đánh giá tuân thủ OWASP Top 10 cho toàn bộ giai đoạn 4
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/java/org/nlh4j/socialscheduler/ratelimitservice/service/RateLimiterService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-003], [EXC-002], [EXC-003], [EXC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải thực hiện đánh giá chuyên sâu mã nguồn cho toàn bộ tệp được tạo trong Giai đoạn 4 bao gồm `RedisTokenBucketStrategy`, `RateLimiterService`, `RateLimitController`, `ScheduleRequestDto`, `SchedulePayloadValidator`, `GlobalExceptionHandler`, `RateLimitGatewayFilter` và `RateLimitExceededException` [REQ-003]. Kiểm tra tính tuân thủ OWASP Top 10: (1) A03 (Injection) - xác minh rằng `RedisTokenBucketStrategy` sử dụng Lua script tham số hóa thông qua `KEYS[]` và `ARGV[]` của Redis, KHÔng có string concatenation cho key construction; xác minh `SchedulePayloadValidator` sử dụng whitelist domain cho `mediaUrls` thay vì blacklist, ngăn chặn SSRF. (2) A04 (Insecure Design) - đánh giá cơ chế defense-in-depth thông qua Rate Limiter làm lớp bảo vệ thứ hai sau JWT authentication, đảm bảo cấu hình `capacity` và `refillRate` được đọc từ external config chứ không hardcode [EXC-002]. (3) A05 (Security Misconfiguration) - xác minh `GlobalExceptionHandler` không tiết lộ stack trace hoặc thông tin nội bộ trong response, chỉ trả về `errorCode` và `message` thân thiện với người dùng; đảm bảo CORS không được cấu hình `allowedOrigins = "*"`. (4) Xác minh không có mã thông tin nhạy cảm (API key, JWT signing secret, Redis password) bị hardcode trong source code, tất cả phải được đọc từ biến môi trường hoặc Secret Manager [NFR-002]. (5) Đánh giá hiệu suất thuật toán Redis Token Bucket dưới tải 1000 request/giây thông qua JMeter hoặc k6 script, xác minh `p95 < 5ms` cho `tryConsume`. (6) Kiểm tra SonarQube Quality Gate: tỷ lệ trùng lặp mã dưới 3%, độ phức tạp Cyclomatic không vượt quá 10 cho mỗi phương thức, không có code smell BLOCKER/CRITICAL. Đề xuất bốn cải tiến: (a) Bổ sung distributed tracing với OpenTelemetry cho luồng rate limit; (b) Triển khai circuit breaker cho Redis client sử dụng Resilience4j; (c) Thêm rate limit dashboard trong Grafana cho biết tỷ lệ blocked requests; (d) Tách `GlobalExceptionHandler` thành các lớp chuyên biệt theo bounded context. Ghi nhận các vấn đề phát hiện vào nhật ký review và tạo pull request sửa lỗi nếu phát hiện BLOCKER hoặc CRITICAL theo SonarQube Quality Gate [EXC-003] [EXC-005].

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.8: Soạn thảo hợp đồng OpenAPI 3.0 đầy đủ cho Validation và Rate Limit
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/api/ValidationAndRateLimitContract.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [REQ-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải tạo tệp OpenAPI 3.0 chuẩn YAML tại `./sources/docs/api/ValidationAndRateLimitContract.yaml` mô tả đầy đủ hai endpoint `POST /api/v1/rate-limits/check`, `POST /api/v1/rate-limits/reset` và bổ sung chi tiết endpoint `POST /api/v1/schedules` với các ràng buộc validation. Khai báo thông tin metadata: `openapi: 3.0.3`, `info: {title: "Social Scheduler - Validation and Rate Limit API Contract", version: "1.0.0", description: "Hợp đồng API cho module xác thực đầu vào và giới hạn tỷ lệ"}`. Định nghĩa `components.securitySchemes.bearerAuth` với `type: http`, `scheme: bearer`, `bearerFormat: JWT`. Khai báo `components.schemas` cho các schema: `RateLimitCheckRequest` (userId UUID required, endpoint string 1-255 required), `RateLimitCheckResponse` (allowed boolean, remainingTokens integer, retryAfterSeconds long), `RateLimitResetRequest` (userId UUID, endpoint string), `RateLimitResetResponse` (success boolean, message string Vietnamese), `ScheduleRequestDto` (tenantId UUID required, platform string regex `^(Facebook|Instagram|TikTok)$` required, content string 1-5000 required, scheduledTime date-time required future, mediaUrls array of string max 10), `ValidationErrorResponse` (errorCode string, message string, fieldErrors array of {field, rejectedValue, errorMessage}, timestamp, correlationId), `ErrorResponse` (errorCode, message, timestamp, correlationId). Định nghĩa `paths./api/v1/rate-limits/check.post` với phản hồi 200, 401 (TOKEN_EXPIRED), 429 (RATE_LIMIT_EXCEEDED với header `Retry-After`). Định nghĩa `paths./api/v1/rate-limits/reset.post` với security `bearerAuth` yêu cầu role ADMIN, phản hồi 200, 401, 403 (INSUFFICIENT_ROLE). Định nghĩa `paths./api/v1/schedules.post` với phản hồi 201, 400 (VALIDATION_FAILED), 401, 429. Bổ sung `examples` cho mỗi trường hợp request và response thành công/thất bại. Sử dụng `$ref` để tái sử dụng schema giữa các endpoint. Tích hợp ví dụ cấu hình gateway filter trong `info.description` hoặc extension field `x-gateway-config`.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [REQ-XXX], [ARC-XXX]:**
<!--START_API_CONTRACT-->
```markdown
# NO_EXTERNAL_API_OR_EVENT_CONTRACTS_REQUIRED
```
<!--END_API_CONTRACT-->

* **Phase Localized Exception Handlers [EXC-XXX]:**
<!--START_EXC_HANDLER-->
```java
// NO_LOCALIZED_EXCEPTION_HANDLERS_REQUIRED
```
<!--END_EXC_HANDLER-->

<!--END_CHUNK_PART_4-->