# Giai đoạn 3: <!--PHASE_NAME_START-->Dịch vụ Đề xuất Nội dung AI và Tích hợp OpenAI<!--PHASE_NAME_END-->

## 📊 Kiểm soát Tài liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Blueprint** | ARCH-20260831230418 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 3 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Dịch vụ Đề xuất Nội dung AI và Tích hợp OpenAI<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Triển khai hoàn chỉnh microservice ai-service cung cấp nội dung bài đăng được cá nhân hóa thông qua tích hợp OpenAI Completion API kết hợp phân tích hiệu suất lịch sử từ bảng performance_metrics, kèm cơ chế fallback an toàn khi mô hình AI không khả dụng, tuân thủ phân quyền RBAC 4 vai trò và tiêu chuẩn OWASP A04/A09<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/08/31 23:04:18 |
| **Tác giả** | Kiến trúc sư Hệ thống Doanh nghiệp (SA Agent) |
| **Phê duyệt** | Chờ phê duyệt quản trị kỹ thuật |

## 1. Phạm vi Hoạt động & Mục tiêu Giai đoạn

Giai đoạn 3 tập trung 100% vào việc kiến tạo hoàn chỉnh microservice `ai-service` chịu trách nhiệm cung cấp nội dung bài đăng được cá nhân hóa thông qua tích hợp OpenAI Completion API kết hợp phân tích hiệu suất lịch sử từ bảng `performance_metrics` đã được di trú tại Giai đoạn 1. Phạm vi cụ thể bao gồm: (1) Xây dựng RESTful controller `RecommendationController` với hai endpoint `POST /api/v1/ai/recommendations` (tạo đề xuất nội dung) và `GET /api/v1/ai/recommendations/health` (kiểm tra tình trạng dịch vụ), tích hợp annotation `@PreAuthorize` để thực thi phân quyền RBAC 4 vai trò theo [ARC-001] đến [ARC-004]; (2) Khởi tạo các lớp DTO `RecommendationRequestDto` và `RecommendationResponseDto` với annotation Jakarta Validation nghiêm ngặt, enum `Platform` (`FACEBOOK`, `INSTAGRAM`, `TIKTOK`) và enum `Tone` (`PROFESSIONAL`, `CASUAL`, `HUMOROUS`, `INSPIRATIONAL`); (3) Triển khai `OpenAIClient` với Resilience4j Circuit Breaker và Retry, kết nối tới endpoint `https://api.openai.com/v1/chat/completions` sử dụng mô hình `gpt-4o-mini`; (4) Xây dựng `PerformanceAnalyticsClient` truy vấn bảng `performance_metrics` kết hợp caching Caffeine với TTL 15 phút; (5) Phát triển `RecommendationService` thực thi quy trình prompt engineering tổng hợp, tích hợp metric Micrometer và bọc ngoại lệ `AiServiceException` [EXC-003] để kích hoạt cơ chế fallback; (6) Triển khai `DefaultContentFallback` cung cấp nội dung dự phòng an toàn khi mô hình AI thất bại [EXC-004]; (7) Soạn thảo hợp đồng OpenAPI 3.0 cho nhóm endpoint đề xuất nội dung. Toàn bộ logic được tách biệp hoàn toàn với các dịch vụ khác thông qua API Gateway và Kafka event bus.

Giai đoạn này KHÔNG triển khai: thay đổi lược đồ cơ sở dữ liệu (bảng `performance_metrics` đã hoàn thiện tại Giai đoạn 1 với [DAT-002]), bộ giới hạn tỷ lệ Token Bucket (ủy thác cho Giai đoạn 4), bộ xử lý ngoại lệ tập trung cho toàn hệ thống (ủy thác cho Giai đoạn 4), module lập lịch đa nền tảng Facebook/Instagram/TikTok (đã hoàn thiện tại Giai đoạn 2), hạ tầng DevOps và Terraform (ủy thác cho Giai đoạn 5).

## 2. Phạm vi Kỹ thuật Được phép & Ranh giới Thư mục

Danh sách kiểm tra kỹ thuật các tệp vật lý được phép tạo hoặc xử lý trong phạm vi giai đoạn này, mọi mục đều kèm mã định danh truy vết:

* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java` — RESTful controller cho module đề xuất nội dung AI. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java` — Logic nghiệp vụ đề xuất nội dung kết hợp fallback orchestration. [REQ-002], [EXC-003], [EXC-004]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java` — Bộ tích hợp OpenAI Completion API với Circuit Breaker. [REQ-002], [EXC-003]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java` — Bộ đọc dữ liệu hiệu suất lịch sử từ bảng `performance_metrics`. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java` — Cơ chế dự phòng nội dung mặc định khi AI thất bại. [REQ-002], [EXC-004]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java` — DTO cho payload yêu cầu đề xuất. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java` — DTO cho payload phản hồi đề xuất. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java` — Enum định nghĩa các nền tảng mạng xã hội được hỗ trợ. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java` — Enum định nghĩa các tông giọng nội dung. [REQ-002]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java` — Ngoại lệ chuyên biệt cho lỗi OpenAI API. [EXC-003]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java` — Ngoại lệ chuyên biệt cho lỗi fallback dự phòng. [EXC-004]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java` — Cấu hình RestClient/WebClient và biến môi trường OpenAI. [REQ-002], [ARC-005]
* `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java` — Cấu hình Circuit Breaker và Retry cho OpenAI client. [REQ-002], [ARC-005]
* `./sources/backend/ai-service/src/main/resources/application-ai.yml` — Tệp cấu hình runtime cho ai-service. [ARC-005]
* `./sources/backend/ai-service/src/main/resources/prompt-templates.yml` — External prompt templates cho prompt engineering. [REQ-002]
* `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java` — Bộ kiểm thử đơn vị cho RecommendationService. [REQ-002], [EXC-003], [EXC-004]
* `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java` — Bộ kiểm thử cho OpenAIClient sử dụng MockWebServer. [REQ-002], [EXC-003]
* `./sources/docs/api/RecommendationApiContract.yaml` — Hợp đồng OpenAPI 3.0 cho module đề xuất nội dung AI. [REQ-002], [DOC-001]

## 3. Chỉ thị Chức năng Chuyên biệt cho Sub-Agent

Phân bổ nhiệm vụ và ràng buộc kỹ thuật cho từng persona Sub-Agent hoạt động trong giai đoạn này:

* **Coder**: Hoạt động với vai trò Nhà phát triển Ứng dụng Cao cấp, chịu trách nhiệm triển khai toàn bộ mã nguồn ứng dụng cho `ai-service` bao gồm controller, service, DTO, enum, integration clients (OpenAI, PerformanceAnalytics), fallback component, exception classes và cấu hình Resilience4j. Bị cấm viết bộ kiểm thử, tệp Terraform, Docker manifest hoặc tài liệu OpenAPI.
* **Tester**: Hoạt động với vai trò Trưởng phòng QC/QA, chuyên trách kỹ thuật bộ kiểm thử JUnit 5 và Mockito cho `RecommendationService` và `OpenAIClient`, sử dụng MockWebServer cho bộ tích hợp OpenAI. Bị cấm sửa đổi mã nguồn sản phẩm.
* **Doc**: Hoạt động với vai trò Chuyên gia Viết tài liệu Kỹ thuật, chịu trách nhiệm biên soạn hợp đồng OpenAPI 3.0 cho module đề xuất nội dung tại `./sources/docs/api/RecommendationApiContract.yaml`, kèm tài liệu hóa cơ chế bảo mật `bearerAuth` JWT, schema chi tiết cho Request/Response và mã lỗi 401/403/422/429/500/503.
* **Reviewer**: Chịu trách nhiệm xác minh biên dịch, phân tích tĩnh mã nguồn, đánh giá chiến lược prompt engineering, phát hiện rò rỉ API key, đánh giá hiệu suất Caffeine cache dưới tải đồng thời, xác minh che giấu lỗi nội bộ theo OWASP A09.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)

Giai đoạn 3 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng khách quan sau:

* `ai-service` biên dịch sạch thông qua `mvn -f ./sources/backend/ai-service/pom.xml compile` và khởi động thành công với Spring Profile `docker`.
* Hai endpoint RESTful (`POST /api/v1/ai/recommendations`, `GET /api/v1/ai/recommendations/health`) trả về đúng mã HTTP 200/401/403/503 theo đặc tả.
* `OpenAIClient` hoàn tất gọi HTTP thành công tới OpenAI Completion API với API key mặc định; lỗi mạng hoặc HTTP 5xx kích hoạt `AiServiceException` với cơ chế Retry tối đa 3 lần và Circuit Breaker mở khi tỷ lệ lỗi vượt 50%.
* `PerformanceAnalyticsClient` truy vấn thành công bảng `performance_metrics` và trả về top 5 bài đăng có hiệu suất cao nhất với caching TTL 15 phút.
* `RecommendationService` thực thi đúng quy trình: truy vấn hiệu suất → xây dựng prompt → gọi OpenAI → trả về response với `isFallback=false`; khi OpenAI thất bại, kích hoạt `DefaultContentFallback` trả về `isFallback=true`.
* Bốn vai trò RBAC (ADMIN, USER, SCHEDULER, ANALYST) được kiểm tra đúng thông qua `@PreAuthorize`; truy cập trái phép trả về HTTP 403 với mã `INSUFFICIENT_ROLE`.
* Hợp đồng OpenAPI 3.0 `./sources/docs/api/RecommendationApiContract.yaml` được tạo ra với đầy đủ schema, phản hồi mẫu, tham chiếu bảo mật `bearerAuth` và mã lỗi 401/403/422/429/500/503.
* Tất cả các mã định danh truy vết `[REQ-002]`, `[EXC-003]`, `[EXC-004]` được ánh xạ 1:1 vào các tệp vật lý tương ứng.
* Độ phủ kiểm thử đơn vị đạt tối thiểu 85% cho `RecommendationService` và `OpenAIClient` theo yêu cầu của cổng chất lượng SonarQube.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo Controller, DTO, Enum và Hợp đồng API cho Dịch vụ AI Recommendation<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 1.1: Triển khai Controller RESTful và bảo vệ endpoint đề xuất nội dung bằng RBAC 4 vai trò
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải khởi tạo lớp `RecommendationController` tại đường dẫn `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/controller/RecommendationController.java` với annotation `@RestController` và `@RequestMapping("/api/v1/ai/recommendations")`. Triển khai hai endpoint chính: (1) `POST /` (đường dẫn đầy đủ `POST /api/v1/ai/recommendations`) nhận `RecommendationRequestDto` được annotate `@Valid` để kích hoạt Jakarta Validation tự động, gọi `RecommendationService.generateRecommendation(request)` và trả về `ResponseEntity<RecommendationResponseDto>` với mã trạng thái HTTP 200 OK; (2) `GET /health` trả về `ResponseEntity<Map<String, Object>>` chứa `{"status":"UP","service":"ai-service","version":"1.0.0"}` với HTTP 200 OK. Đính annotation `@PreAuthorize("hasAnyRole('USER','ADMIN','ANALYST')")` ở cấp lớp để thực thi phân quyền RBAC theo [ARC-001] (Admin), [ARC-002] (User), [ARC-004] (Analyst); vai trò Scheduler [ARC-003] không được phép truy cập endpoint đề xuất vì đây là nghiệp vụ sáng tạo nội dung chứ không phải thực thi lịch. Bổ sung annotation `@Operation` và `@ApiResponse` từ Springdoc OpenAPI để tài liệu hóa API tự động. Bọc phương thức POST trong khối `try-catch (AiServiceException ex)` để trả về `ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)` với mã lỗi `AI_SERVICE_UNAVAILABLE` [EXC-003] khi cả OpenAI và fallback đều thất bại. Đối với `FallbackContentException` [EXC-004], controller vẫn trả về HTTP 200 OK với payload chứa cờ `isFallback=true` để đảm bảo trải nghiệm người dùng liền mạch thay vì lỗi 5xx. Sử dụng `@Slf4j` của Lombok để ghi log có cấu trúc với MDC context bao gồm `userId`, `platform`, `correlationId` theo OWASP A09. Inject `RecommendationService` thông qua constructor để đảm bảo tính bất biến và dễ dàng kiểm thử. Đảm bảo tính nhất quán với chuẩn RESTful: header `Authorization: Bearer <jwt_token>` được xác thực bởi API Gateway trước khi đến controller này, controller không cần validate JWT trực tiếp.

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
  "endpoint": "POST /api/v1/ai/recommendations",
  "headers": {
    "Authorization": "Bearer {jwt_token}",
    "Content-Type": "application/json",
    "X-Correlation-Id": "uuid"
  },
  "request_payload": {
    "userId": "uuid (required, @NotNull)",
    "platform": "FACEBOOK | INSTAGRAM | TIKTOK (required, @NotNull)",
    "topic": "string (required, @NotBlank, @Size(max=500))",
    "tone": "PROFESSIONAL | CASUAL | HUMOROUS | INSPIRATIONAL (optional, default=PROFESSIONAL)",
    "maxLength": "integer (optional, @Min(100) @Max(3000), default=500)"
  },
  "response_payload_200": {
    "recommendationId": "uuid",
    "userId": "uuid",
    "platform": "string",
    "content": "string",
    "confidenceScore": "decimal (0.0-1.0)",
    "isFallback": "boolean",
    "generatedAt": "ISO-8601 timestamp"
  },
  "error_responses": {
    "401": "Unauthorized - JWT token invalid or expired",
    "403": "Forbidden - User lacks required role (INSUFFICIENT_ROLE)",
    "422": "Unprocessable Entity - Validation failed (VALIDATION_FAILED)",
    "429": "Too Many Requests - Rate limit exceeded",
    "500": "Internal Server Error",
    "503": "AI Service Unavailable - OpenAI API failure with no fallback (AI_SERVICE_UNAVAILABLE)"
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

#### 📝 TÁC VỤ CON 1.2: Tạo các lớp DTO Request/Response với Bean Validation nghiêm ngặt
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo bốn tệp nguồn trong package `dto` và `dto.enums`: (1) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationRequestDto.java` sử dụng Java Record (`public record RecommendationRequestDto(...)`) với các annotation Jakarta Validation: `@NotNull(message = "userId is required")` cho trường `userId` kiểu UUID, `@NotNull(message = "platform is required")` cho trường `platform` kiểu `Platform` enum, `@NotBlank(message = "topic cannot be blank") @Size(max = 500, message = "topic must not exceed 500 characters")` cho trường `topic` kiểu String, `@Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}\\p{P}\\p{N}]{1,500}$", message = "topic contains invalid characters")` để ngăn chặn XSS injection theo OWASP A03, trường `tone` kiểu `Tone` enum là optional với giá trị mặc định `PROFESSIONAL`, trường `maxLength` kiểu Integer với `@Min(value = 100, message = "maxLength must be at least 100") @Max(value = 3000, message = "maxLength must not exceed 3000")`; (2) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/RecommendationResponseDto.java` chứa các trường `recommendationId` (UUID), `userId` (UUID), `platform` (String), `content` (String), `confidenceScore` (BigDecimal trong khoảng 0.0-1.0), `isFallback` (boolean), `generatedAt` (OffsetDateTime với `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")`); (3) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Platform.java` định nghĩa enum với ba giá trị `FACEBOOK("Facebook Graph API")`, `INSTAGRAM("Instagram Graph API")`, `TIKTOK("TikTok Open API")` kèm phương thức `getDisplayName()`; (4) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/dto/enums/Tone.java` định nghĩa enum với bốn giá trị `PROFESSIONAL`, `CASUAL`, `HUMOROUS`, `INSPIRATIONAL` kèm phương thức `getPromptModifier()` trả về chuỗi mô tả tông giọng để tiêm vào prompt template. Áp dụng nguyên tắc bất biến với Java Record, bổ sung `@Builder` để hỗ trợ khởi tạo từ service layer. Đảm bảo serialization/deserialization JSON hoạt động chính xác với Jackson mặc định, sử dụng `@JsonInclude(JsonInclude.Include.NON_NULL)` để loại bỏ trường null khỏi response payload.

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

#### 📝 TÁC VỤ CON 1.3: Cấu hình OpenAI Client Bean và tích hợp biến môi trường bảo mật
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/OpenAiConfig.java` được đánh dấu với `@Configuration` và `@ConfigurationProperties(prefix = "openai")`. Khai báo các trường cấu hình: `apiKey` (đọc từ biến môi trường `OPENAI_API_KEY` thông qua `@Value("${openai.api-key:}")` với giá trị mặc định rỗng để tránh leak khi không cấu hình), `baseUrl` (mặc định `https://api.openai.com/v1`), `model` (mặc định `gpt-4o-mini`), `maxTokens` (mặc định 500), `temperature` (mặc định 0.7), `connectTimeoutMs` (mặc định 5000), `readTimeoutMs` (mặc định 10000). Tạo bean `RestClient openaiRestClient(RestClient.Builder builder)` sử dụng `RestClient.builder()` của Spring Framework 6.1.x với `requestFactory` cấu hình `JdkClientHttpRequestFactory` kết hợp `HttpClient` có connect timeout và read timeout. Tiêm `Authorization: Bearer <api-key>` vào header thông qua `defaultHeader` lambda. Sử dụng `HttpComponentsClientHttpRequestFactory` hoặc `JdkClientHttpRequestFactory` tùy theo môi trường triển khai. Đảm bảo bean `RestClient` được đăng ký với `@Bean` annotation để có thể inject vào `OpenAIClient`. Đồng thời tạo `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/config/Resilience4jConfig.java` cấu hình Circuit Breaker với `@CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")` và `@Retry(name = "openai")` thông qua annotation trên phương thức của `OpenAIClient`. Cấu hình `application-ai.yml` tại `./sources/backend/ai-service/src/main/resources/application-ai.yml` chứa: `openai.api-key=${OPENAI_API_KEY:}` (KHÔNG hardcode giá trị thật), `openai.base-url=https://api.openai.com/v1`, `openai.model=gpt-4o-mini`, `resilience4j.circuitbreaker.instances.openai.failure-rate-threshold=50`, `resilience4j.circuitbreaker.instances.openai.wait-duration-in-open-state=30s`, `resilience4j.retry.instances.openai.max-attempts=3`, `resilience4j.retry.instances.openai.wait-duration=1s`, `resilience4j.retry.instances.openai.exponential-backoff-multiplier=2`. Bổ sung actuator endpoint `/actuator/health` và `/actuator/prometheus` cho Micrometer scraping.

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

#### 📝 TÁC VỤ CON 1.4: Soạn thảo hợp đồng OpenAPI 3.0 đầy đủ cho endpoint Recommendation
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/api/RecommendationApiContract.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [DOC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải tạo tệp OpenAPI 3.0 chuẩn YAML tại `./sources/docs/api/RecommendationApiContract.yaml` mô tả đầy đủ hai endpoint `POST /api/v1/ai/recommendations` và `GET /api/v1/ai/recommendations/health`. Khai báo thông tin metadata: `openapi: 3.0.3`, `info: {title: "Social Scheduler - AI Recommendation API Contract", version: "1.0.0", description: "Microservice cung cấp nội dung bài đăng được cá nhân hóa thông qua OpenAI Completion API kết hợp phân tích hiệu suất lịch sử"}`. Định nghĩa `components.securitySchemes.bearerAuth` với `type: http`, `scheme: bearer`, `bearerFormat: JWT`, kèm `description: "JWT Bearer token chứa claim 'roles' để phân quyền RBAC 4 vai trò"`. Khai báo `components.schemas` cho các schema: `RecommendationRequest` (userId UUID required, platform enum required, topic string 1-500 required, tone enum optional default PROFESSIONAL, maxLength integer 100-3000 optional), `RecommendationResponse` (recommendationId UUID, userId UUID, platform string, content string, confidenceScore number 0.0-1.0, isFallback boolean, generatedAt date-time), `ErrorResponse` (errorCode string, message string, timestamp date-time, correlationId UUID), `ValidationErrorResponse` (errorCode string, message string, fieldErrors array of objects), `PlatformEnum` (FACEBOOK, INSTAGRAM, TIKTOK), `ToneEnum` (PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL). Định nghĩa `paths./api/v1/ai/recommendations.post` với: `summary: "Tạo đề xuất nội dung bài đăng"`, `tags: ["AI Recommendations"]`, `security: [{bearerAuth: []}]`, `requestBody` tham chiếu `RecommendationRequest`, `responses` bao gồm 200 (Success), 401 (Unauthorized - TOKEN_EXPIRED), 403 (Forbidden - INSUFFICIENT_ROLE), 422 (Unprocessable Entity - VALIDATION_FAILED), 429 (Too Many Requests - RATE_LIMIT_EXCEEDED), 500 (Internal Server Error), 503 (Service Unavailable - AI_SERVICE_UNAVAILABLE). Định nghĩa `paths./api/v1/ai/recommendations/health.get` với `summary: "Kiểm tra tình trạng dịch vụ AI"` trả về 200 với schema chứa `status`, `service`, `version`. Bổ sung `examples` cho mỗi trường hợp request và response thành công/thất bại. Sử dụng `$ref` để tái sử dụng schema giữa các endpoint. Tham chiếu ma trận phân quyền RBAC trong `info.description` hoặc extension field `x-rbac-matrix`. Đảm bảo tài liệu tuân thủ cấu trúc YAML chuẩn OpenAPI 3.0.3 và sử dụng `nullable: true` cho các trường optional.

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

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Tích hợp OpenAI Client, Service Logic, Fallback và Test Suite<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 2.1: Triển khai OpenAIClient với Resilience4j Circuit Breaker và Retry pattern
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo lớp `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java` được đánh dấu với `@Component` và `@Slf4j` (Lombok). Inject các dependency thông qua constructor: `RestClient openaiRestClient` (đã cấu hình tại `OpenAiConfig`), `@Value("${openai.model}") String model`, `@Value("${openai.max-tokens:500}") int maxTokens`, `@Value("${openai.temperature:0.7}") double temperature`. Triển khai interface `OpenAIClient` với phương thức `String generateContent(String systemPrompt, String userPrompt)` trả về chuỗi nội dung được sinh bởi mô hình AI. Phương thức này phải được annotate với `@CircuitBreaker(name = "openai", fallbackMethod = "openAiFallback")` và `@Retry(name = "openai")` từ thư viện Resilience4j để tự động thử lại với backoff lũy thừa khi gặp lỗi mạng hoặc timeout, đồng thời ngắt mạch khi tỷ lệ lỗi vượt ngưỡng cấu hình. Xây dựng payload JSON theo đặc tả OpenAI Chat Completions API: `{"model": "{model}", "messages": [{"role": "system", "content": "{systemPrompt}"}, {"role": "user", "content": "{userPrompt}"}], "max_tokens": {maxTokens}, "temperature": {temperature}}`. Sử dụng `openaiRestClient.post().uri("/chat/completions").body(payload).retrieve().body(JsonNode.class)` để gọi API. Trích xuất nội dung từ response JSON tại đường dẫn `choices[0].message.content` sử dụng Jackson `JsonNode`. Phương thức `openAiFallback(String systemPrompt, String userPrompt, Throwable ex)` phải ném `AiServiceException` với thông điệp có cấu trúc chứa `errorCode = "AI_SERVICE_UNAVAILABLE"`, `platform = "OPENAI"`, `originalCause = ex.getClass().getSimpleName()`, để lớp service phía trên có thể kích hoạt cơ chế dự phòng toàn cục. Bọc ngoại lệ `HttpClientErrorException` (4xx), `HttpServerErrorException` (5xx), `ResourceAccessException` (timeout/connect) trong khối `try-catch` để chuẩn hóa thành `AiServiceException`. Sử dụng SLF4J logging có cấu trúc với MDC context bao gồm `correlationId`, `model`, `promptLength` để hỗ trợ truy vết theo OWASP A09. Tuyệt đối KHÔNG ghi log API key hoặc nội dung prompt đầy đủ có thể chứa thông tin nhạy cảm. Đảm bảo `@Transactional` không áp dụng cho lớp này vì nó là bộ tích hợp bên ngoài.

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
  "external_api": {
    "provider": "OpenAI",
    "endpoint": "POST https://api.openai.com/v1/chat/completions",
    "headers": {
      "Authorization": "Bearer ${OPENAI_API_KEY}",
      "Content-Type": "application/json"
    },
    "request_body": {
      "model": "gpt-4o-mini",
      "messages": [
        {
          "role": "system",
          "content": "You are a social media content generator specialized in {platform}. Tone: {tone}. Generate engaging content under {maxLength} characters."
        },
        {
          "role": "user",
          "content": "{topic}"
        }
      ],
      "max_tokens": 500,
      "temperature": 0.7
    },
    "response_extraction": "choices[0].message.content"
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

#### 📝 TÁC VỤ CON 2.2: Triển khai PerformanceAnalyticsClient với Caffeine caching cho dữ liệu hiệu suất lịch sử
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo lớp `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/PerformanceAnalyticsClient.java` được đánh dấu với `@Component` và `@Slf4j` (Lombok). Sử dụng Spring Data JPA với entity `PerformanceMetricEntity` (ánh xạ bảng `performance_metrics` đã di trú tại Giai đoạn 1) và repository `PerformanceMetricRepository` kế thừa `JpaRepository<PerformanceMetricEntity, UUID>`. Inject `JdbcTemplate` hoặc `EntityManager` để thực thi truy vấn JPQL tối ưu hiệu suất. Triển khai phương thức `List<PerformanceMetricEntity> findTopPerformingPosts(UUID userId, String platform, int limit)` trả về danh sách các bài đăng có tổng tương tác (`likes + comments + shares`) cao nhất trong 30 ngày gần nhất. Sử dụng JPQL native query: `SELECT pm.* FROM ai_schema.performance_metrics pm JOIN schedule_schema.schedules s ON pm.post_id = s.schedule_id WHERE pm.tenant_id = :tenantId AND s.platform = :platform AND pm.collected_at >= :sinceDate ORDER BY (pm.likes + pm.comments + pm.shares) DESC LIMIT :limit`. Phương thức phải nhận tham số `tenantId` từ SecurityContextHolder để đảm bảo cô lập dữ liệu đa tenant theo [NFR-003]. Tích hợp caching với annotation `@Cacheable(cacheNames = "performanceMetrics", key = "#userId + ':' + #platform + ':' + #limit", unless = "#result.isEmpty()")` sử dụng Caffeine cache với TTL 15 phút cấu hình tại `application-ai.yml`: `spring.cache.caffeine.spec=expireAfterWrite=15m,maximumSize=10000`. Triển khai interface `PerformanceAnalyticsClient` để dễ dàng mock trong kiểm thử. Phương thức phải trả về danh sách rỗng (không null) khi không tìm thấy dữ liệu, KHÔNG ném ngoại lệ. Bổ sung metric Micrometer `ai.performance.fetch.duration` để theo dõi thời gian truy vấn. Đảm bảo truy vấn sử dụng `@Query` với JPQL named parameter để chống SQL injection theo OWASP A03.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
-- Truy vấn đọc trên bảng performance_metrics đã có ở Giai đoạn 1 (DAT-002)
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

#### 📝 TÁC VỤ CON 2.3: Triển khai RecommendationService với logic prompt engineering và fallback orchestration
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-003], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải tạo lớp `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java` được đánh dấu `@Service`, `@Transactional(readOnly = true)` và `@Slf4j` (Lombok). Inject các dependency thông qua constructor: `OpenAIClient openAIClient`, `PerformanceAnalyticsClient performanceAnalyticsClient`, `DefaultContentFallback defaultContentFallback`. Triển khai phương thức chính `RecommendationResponseDto generateRecommendation(RecommendationRequestDto request)` với luồng xử lý 4 bước: (Bước 1) Trích xuất `tenantId` từ `SecurityContextHolder.getContext().getAuthentication()` để đảm bảo cô lập dữ liệu đa tenant; gọi `performanceAnalyticsClient.findTopPerformingPosts(request.userId(), request.platform().name(), 5)` để lấy top 5 bài đăng có hiệu suất cao nhất, kết quả có thể rỗng. (Bước 2) Xây dựng prompt tổng hợp bằng cách tải template từ external config `./sources/backend/ai-service/src/main/resources/prompt-templates.yml` thông qua `@ConfigurationProperties(prefix = "prompt-templates")`; prompt phải bao gồm: chủ đề người dùng cung cấp (`topic`), tông giọng yêu cầu (`tone.getPromptModifier()`), nền tảng mục tiêu (`platform.getDisplayName()`), và các mẫu nội dung thành công trước đó (từ top 5 performance metrics). (Bước 3) Gọi `openAIClient.generateContent(systemPrompt, userPrompt)` để nhận nội dung đề xuất; nếu kết quả rỗng hoặc vượt quá `maxLength`, ném `FallbackContentException`. (Bước 4) Xây dựng `RecommendationResponseDto` với `recommendationId = UUID.randomUUID()`, `confidenceScore = BigDecimal.valueOf(0.85)`, `isFallback = false`, `generatedAt = OffsetDateTime.now()`. Toàn bộ phương thức được bao bọc trong khối `try-catch (AiServiceException ex)` để khi OpenAI thất bại, chuyển hướng sang `defaultContentFallback.provide(request)` trả về nội dung mặc định với `isFallback = true`. Bổ sung khối `catch (FallbackContentException ex)` để ghi log lỗi cấp `ERROR` với correlation ID và ném `ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex)`. Bổ sung metric Micrometer `ai.recommendation.generated.total` (counter với tag `outcome=success|fallback|failed`) và `ai.recommendation.duration` (timer). Sử dụng structured logging với MDC context bao gồm `correlationId`, `userId`, `platform`, `tone`, `isFallback` theo OWASP A09. Triển khai interface `RecommendationService` để dễ dàng mock trong kiểm thử.

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
// RecommendationService xử lý ngoại lệ nội bộ thông qua try-catch blocks
try {
    String generatedContent = openAIClient.generateContent(systemPrompt, userPrompt);
    if (generatedContent == null || generatedContent.isBlank()) {
        throw new FallbackContentException("OPENAI_RETURNED_EMPTY_CONTENT");
    }
    return RecommendationResponseDto.builder()
            .recommendationId(UUID.randomUUID())
            .userId(request.userId())
            .platform(request.platform().name())
            .content(generatedContent)
            .confidenceScore(BigDecimal.valueOf(0.85))
            .isFallback(false)
            .generatedAt(OffsetDateTime.now())
            .build();
} catch (AiServiceException ex) {
    log.warn("OpenAI service unavailable, activating fallback for userId={}", request.userId(), ex);
    return defaultContentFallback.provide(request);
} catch (FallbackContentException ex) {
    log.error("Fallback content provider also failed for userId={}", request.userId(), ex);
    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", ex);
}
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.4: Triển khai DefaultContentFallback và các lớp ngoại lệ chuyên biệt
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải tạo bốn tệp nguồn trong package `fallback` và `exception`: (1) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/fallback/DefaultContentFallback.java` được đánh dấu `@Component` và `@Slf4j` (Lombok), chứa một `Map<String, String> FALLBACK_TEMPLATES` được khởi tạo tĩnh với các mẫu nội dung mặc định theo key `"{platform}_{tone}"` (ví dụ: `FACEBOOK_PROFESSIONAL`, `INSTAGRAM_CASUAL`, `TIKTOK_HUMOROUS`, `FACEBOOK_INSPIRATIONAL`); mỗi mẫu là một chuỗi nội dung marketing chuyên nghiệp dài 200-300 ký tự phù hợp với từng nền tảng. Triển khai phương thức `RecommendationResponseDto provide(RecommendationRequestDto request)` thực hiện logic: chọn template phù hợp nhất dựa trên `request.platform().name() + "_" + request.tone().name()`, nếu không tìm thấy thì sử dụng template mặc định `"Stay tuned for exciting updates from our brand!"`; sinh `recommendationId = UUID.randomUUID()`, trả về `RecommendationResponseDto` với `confidenceScore = BigDecimal.valueOf(0.30)`, `isFallback = true`, `generatedAt = OffsetDateTime.now()`. Phương thức `provide` phải ném `FallbackContentException` nếu template rỗng hoặc xảy ra lỗi không mong muốn. (2) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/AiServiceException.java` kế thừa `RuntimeException` với các trường `errorCode = "AI_SERVICE_UNAVAILABLE"`, `platform`, `originalCause`, `httpStatus`; bổ sung constructor nhận message và cause. (3) `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/exception/FallbackContentException.java` kế thừa `RuntimeException` với `errorCode = "FALLBACK_CONTENT_FAILED"`. (4) Bổ sung annotation `@Slf4j` để ghi log cảnh báo khi kích hoạt fallback với thông điệp `"Fallback content provided for userId={} platform={} tone={}"` ở cấp độ `INFO` khi thành công và `ERROR` khi fallback cũng thất bại. Sử dụng `@Builder` của Lombok để hỗ trợ khởi tạo DTO một cách rõ ràng.

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
// DefaultContentFallback.provide() trả về nội dung dự phòng an toàn
RecommendationResponseDto fallback = RecommendationResponseDto.builder()
        .recommendationId(UUID.randomUUID())
        .userId(request.userId())
        .platform(request.platform().name())
        .content(FALLBACK_TEMPLATES.getOrDefault(
                request.platform().name() + "_" + request.tone().name(),
                "Stay tuned for exciting updates from our brand!"))
        .confidenceScore(BigDecimal.valueOf(0.30))
        .isFallback(true)
        .generatedAt(OffsetDateTime.now())
        .build();
log.info("Fallback content provided for userId={} platform={}", request.userId(), request.platform());
return fallback;
```
<!--END_EXC_HANDLER-->

#### 📝 TÁC VỤ CON 2.5: Biên soạn bộ Test Suite JUnit5 và Mockito toàn diện cho RecommendationService
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java;./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-003], [EXC-004]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải tạo lớp kiểm thử `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationServiceTest.java` sử dụng JUnit 5 và Mockito. Sử dụng `@ExtendWith(MockitoExtension.class)` và khai báo các mock thông qua `@Mock`: `OpenAIClient openAIClient`, `PerformanceAnalyticsClient performanceAnalyticsClient`, `DefaultContentFallback defaultContentFallback`. Inject mock vào `RecommendationService` thông qua constructor hoặc `@InjectMocks`. Sử dụng `ReflectionTestUtils` hoặc `@MockBean` để inject `SecurityContextHolder` với authentication giả lập chứa `tenantId`. Viết các trường hợp kiểm thử chi tiết: (1) `@DisplayName("generateRecommendation_whenOpenAiReturnsValidContent_thenReturnResponseWithFallbackFalse")` - mock `performanceAnalyticsClient.findTopPerformingPosts()` trả về danh sách 5 `PerformanceMetricEntity` mẫu, mock `openAIClient.generateContent()` trả về chuỗi "Exciting content about our products!", khẳng định response có `isFallback=false`, `confidenceScore=0.85`, `content` khớp với giá trị mock, `recommendationId` không null; (2) `@DisplayName("generateRecommendation_whenOpenAiThrowsAiServiceException_thenInvokeFallbackAndReturnIsFallbackTrue")` - mock `openAIClient.generateContent()` ném `AiServiceException`, mock `defaultContentFallback.provide()` trả về response với `isFallback=true`, khẳng định service tự động gọi fallback và response có `isFallback=true`, `confidenceScore=0.30`; (3) `@DisplayName("generateRecommendation_whenFallbackAlsoThrows_thenPropagateFallbackContentException")` - mock `openAIClient.generateContent()` ném `AiServiceException`, mock `defaultContentFallback.provide()` ném `FallbackContentException`, khẳng định service ném `ResponseStatusException` với HTTP 503; (4) `@DisplayName("generateRecommendation_whenOpenAiReturnsEmptyContent_thenThrowFallbackContentException")` - mock `openAIClient.generateContent()` trả về chuỗi rỗng, khẳng định `FallbackContentException` được ném; (5) `@DisplayName("generateRecommendation_withEmptyPerformanceHistory_thenProceedWithGenericPrompt")` - mock `performanceAnalyticsClient.findTopPerformingPosts()` trả về danh sách rỗng, khẳng định service vẫn gọi `openAIClient.generateContent()` với prompt hợp lệ (không ném exception). Bổ sung `@BeforeEach` để khởi tạo `RecommendationRequestDto` mẫu với `userId` ngẫu nhiên, `platform = FACEBOOK`, `topic = "new product launch"`, `tone = PROFESSIONAL`. Sử dụng AssertJ cho assertion chain rõ ràng (`assertThat(response.isFallback()).isFalse()`). Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho `RecommendationService`.

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

#### 📝 TÁC VỤ CON 2.6: Đánh giá mã nguồn và đề xuất chiến lược tối ưu prompt engineering
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải thực hiện đánh giá chuyên sâu mã nguồn `RecommendationService` và `OpenAIClient` tại đường dẫn `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/service/RecommendationService.java`. Kiểm tra bốn tiêu chí quan trọng: (1) Bảo mật API key - xác minh rằng biến `openai.api-key` chỉ được đọc từ biến môi trường `OPENAI_API_KEY` thông qua `@Value`, không hardcode giá trị thật trong source code; kiểm tra log statements không in ra API key hoặc Authorization header; sử dụng `git secrets` hoặc `trufflehog` để quét repository. (2) Độ dài prompt - đánh giá prompt template tại `./sources/backend/ai-service/src/main/resources/prompt-templates.yml` đảm bảo tổng độ dài system prompt + user prompt không vượt quá 4000 tokens (giới hạn của mô hình `gpt-4o-mini`); nếu vượt, đề xuất cơ chế tóm tắt (summarization) hoặc giảm số lượng top performing posts từ 5 xuống 3. (3) Xử lý race condition - kiểm tra Caffeine cache có sử dụng đồng bộ hóa atomic operations (`ConcurrentHashMap` nội bộ), xác nhận `@Cacheable` không gây memory leak khi cache key bị collision; đề xuất cấu hình `maximumSize` phù hợp với số lượng user active. (4) Che giấu lỗi nội bộ - đảm bảo phản hồi HTTP 503 với mã `AI_SERVICE_UNAVAILABLE` không tiết lộ stack trace hoặc chi tiết nội bộ của OpenAI client theo OWASP A09; chỉ log đầy đủ stack trace ở cấp `ERROR` cho developer, response cho client chỉ chứa mã lỗi và thông điệp ngắn gọn. Đề xuất bốn cải tiến: (a) Chuyển prompt template thành external config file `prompt-templates.yml` để dễ bảo trì và A/B testing; (b) Bổ sung rate limiter nội bộ cho OpenAI calls sử dụng Bucket4j để tránh vượt quota; (c) Triển khai prompt version control với annotation `@Version` để theo dõi hiệu quả từng phiên bản prompt; (d) Bổ sung fallback template theo locale (vi-VN, en-US) để hỗ trợ đa ngôn ngữ. Ghi nhận các vấn đề phát hiện vào nhật ký review và tạo pull request sửa lỗi nếu phát hiện BLOCKER hoặc CRITICAL theo SonarQube Quality Gate.

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

#### 📝 TÁC VỤ CON 2.7: Biên soạn bộ Test Suite cho OpenAIClient sử dụng MockWebServer
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClient.java;./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-002], [EXC-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải tạo lớp kiểm thử `./sources/backend/ai-service/src/test/java/org/nlh4j/socialscheduler/aiservice/integration/OpenAIClientTest.java` sử dụng MockWebServer (thư viện okhttp 4.12.x) và JUnit 5. Trước mỗi test, khởi tạo `MockWebServer` instance và cấu hình `RestClient` bean thông qua `@DynamicPropertySource` để trỏ tới URL của MockServer. Inject `OpenAIClient` với RestClient đã được cấu hình. Viết các kịch bản kiểm thử: (1) `@DisplayName("generateContent_whenOpenAiReturns200_thenReturnExtractedContent")` - enqueue response với HTTP 200 và body JSON `{"choices":[{"message":{"content":"Generated content here"}}]}`, gọi `openAIClient.generateContent("system prompt", "user prompt")`, khẳng định kết quả trả về khớp với "Generated content here"; (2) `@DisplayName("generateContent_whenOpenAiReturns500_thenThrowAiServiceException")` - enqueue response với HTTP 500, khẳng định `AiServiceException` được ném với `errorCode = "AI_SERVICE_UNAVAILABLE"`, `httpStatus = 500`; (3) `@DisplayName("generateContent_whenNetworkTimeout_thenRetryThreeTimes")` - enqueue response với `SocketTimeoutException` giả lập thông qua `Dispatcher` trả về lỗi, khẳng định phương thức được gọi đúng 3 lần (maxAttempts=3 từ Resilience4j config); (4) `@DisplayName("generateContent_whenResponseHasEmptyChoices_thenThrowFallbackContentException")` - enqueue response với HTTP 200 và body JSON `{"choices":[]}`, khẳng định ngoại lệ liên quan được ném; (5) `@DisplayName("generateContent_whenAuthorizationHeader_thenSendBearerToken")` - verify request được gửi tới MockServer chứa header `Authorization: Bearer test-api-key`. Sử dụng `@AfterEach` để shutdown MockServer. Bổ sung `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` để đảm bảo thứ tự test ổn định. Sử dụng AssertJ để verify số lần request được gửi tới MockServer thông qua `takeRequest()`. Đảm bảo độ phủ mã nguồn đạt tối thiểu 85% cho `OpenAIClient`.

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