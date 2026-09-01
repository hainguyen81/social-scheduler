# Giai đoạn 2: <!--PHASE_NAME_START-->Tích hợp lập lịch đa nền tảng và bảo mật cổng API<!--PHASE_NAME_END-->

## 📊 Kiểm soát Tài liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Blueprint** | ARCH-20260831230418 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 2 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Tích hợp lập lịch đa nền tảng và bảo mật cổng API<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Triển khai toàn bộ RESTful endpoint, logic nghiệp vụ và bộ tích hợp SDK bên thứ ba cho module lập lịch đa nền tảng (Facebook, Instagram, TikTok), đồng thời kiến trúc hóa hệ thống bảo mật phân quyền RBAC 4 vai trò dựa trên OAuth2 Resource Server và JWT tại tầng API Gateway, đảm bảo che chắn endpoint nội bộ và che giấu dữ liệu nhạy cảm theo tiêu chuẩn OWASP Top 10<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/08/31 23:04:18 |
| **Tác giả** | Kiến trúc sư Hệ thống Doanh nghiệp (SA Agent) |
| **Phê duyệt** | Chờ phê duyệt quản trị kỹ thuật |

## 1. Phạm vi Hoạt động & Mục tiêu Giai đoạn

Giai đoạn 2 tập trung 100% vào việc triển khai tầng nghiệp vụ cốt lõi cho module lập lịch đa nền tảng và hệ thống bảo mật cổng API của dự án `social-scheduler`. Phạm vi cụ thể bao gồm: (1) Xây dựng các RESTful endpoint trong `schedule-service` để tạo, truy vấn, cập nhật và xóa lịch đăng bài với quy trình chuyển đổi trạng thái nghiêm ngặt `pending`, `sent`, `failed`, `cancelled`; (2) Tích hợp ba bộ SDK của bên thứ ba (Facebook Graph API, Instagram Graph API, TikTok Open API) sử dụng `WebClient` reactive kèm cơ chế retry có backoff lũy thừa; (3) Kiến trúc hóa hệ thống bảo mật phân quyền RBAC 4 vai trò (Admin, User, Scheduler, Analyst) thông qua Spring Security 6 OAuth2 Resource Server với JWT Decoder tùy chỉnh; (4) Cấu hình API Gateway với chuỗi filter xác thực JWT, phân quyền theo claim `roles` và che chắn toàn bộ endpoint nhạy cảm; (5) Phát hành hợp đồng OpenAPI 3.0 cho nhóm endpoint lập lịch kèm tài liệu bảo mật tuân thủ OWASP A01 và A07.

Giai đoạn này KHÔNG triển khai: thay đổi lược đồ cơ sở dữ liệu (đã hoàn thiện tại Giai đoạn 1), bộ xử lý ngoại lệ tập trung cho toàn hệ thống (ủy thác cho Giai đoạn 4), dịch vụ AI/ML Recommendation (ủy thác cho Giai đoạn 3), bộ giới hạn tỷ lệ Token Bucket chi tiết (ủy thác cho Giai đoạn 4), hạ tầng DevOps và Terraform (ủy thác cho Giai đoạn 5).

## 2. Phạm vi Kỹ thuật Được phép & Ranh giới Thư mục

Danh sách kiểm tra kỹ thuật các tệp vật lý được phép tạo hoặc xử lý trong phạm vi giai đoạn này, mọi mục đều kèm mã định danh truy vết:

* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java` — RESTful controller cho module lịch đăng bài. [REQ-001], [EXC-001], [EXC-002]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java` — Logic nghiệp vụ lập lịch. [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java` — Tầng truy cập dữ liệu JPA. [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java` — Thực thể JPA ánh xạ bảng `schedules`. [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java` — DTO cho payload yêu cầu tạo lịch. [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java` — DTO cho payload phản hồi. [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java` — Bộ tích hợp Facebook Graph API. [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java` — Bộ tích hợp Instagram Graph API. [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java` — Bộ tích hợp TikTok Open API. [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java` — Ngoại lệ chuyên biệt cho SDK bên thứ ba. [EXC-001]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java` — Cấu hình Spring Security OAuth2 Resource Server. [ARC-005], [ARC-006]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java` — Bộ lọc xác thực JWT. [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/RbacPredicate.java` — Bộ dự đoán phân quyền 4 vai trò. [ARC-001], [ARC-002], [ARC-003], [ARC-004]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java` — Bộ điều phối gọi đúng client theo nền tảng. [REQ-001], [EXC-001]
* `./sources/docs/api/ScheduleApiContract.yaml` — Hợp đồng OpenAPI 3.0 cho module lập lịch. [REQ-001], [ARC-005]

**ĐƯỜNG DẪN VẬT LÝ MỤC TIÊU TỔNG THỂ**:
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java` [REQ-001], [EXC-001], [EXC-002]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java` [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java` [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java` [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java` [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java` [REQ-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java` [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java` [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java` [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java` [REQ-001], [EXC-001]
* `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java` [EXC-001]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java` [ARC-005], [ARC-006]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java` [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]
* `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/RbacPredicate.java` [ARC-001], [ARC-002], [ARC-003], [ARC-004]
* `./sources/docs/api/ScheduleApiContract.yaml` [REQ-001], [ARC-005]

## 3. Chỉ thị Chức năng Chuyên biệt cho Sub-Agent

Phân bổ nhiệm vụ và ràng buộc kỹ thuật cho từng persona Sub-Agent hoạt động trong giai đoạn này:

* **Coder**: Hoạt động với vai trò Nhà phát triển Ứng dụng Cao cấp, chịu trách nhiệm triển khai mã nguồn ứng dụng cho `schedule-service` (entity, repository, service, controller, DTO, dispatcher) và `api-gateway` (security config, JWT filter, RBAC predicate), đồng thời tích hợp ba bộ SDK mạng xã hội. Bị cấm viết bộ kiểm thử, tệp Terraform hoặc tài liệu OpenAPI.
* **Tester**: Hoạt động với vai trò Trưởng phòng QC/QA, chuyên trách kỹ thuật bộ kiểm thử JUnit 5 và Mockito cho entity, service, controller và các bộ SDK tích hợp. Sử dụng MockWebServer hoặc WireMock cho các bộ tích hợp SDK bên thứ ba. Bị cấm sửa đổi mã nguồn sản phẩm. Đối với các tác vụ kiểm thử tích hợp xác thực và phân quyền toàn hệ thống tại API Gateway, persona này phải sử dụng token `INTEGRATION_SCOPE` làm tham số đầu tiên trong cặp phân tách bằng dấu chấm phẩy.
* **Doc**: Hoạt động với vai trò Chuyên gia Viết tài liệu Kỹ thuật, chịu trách nhiệm biên soạn hợp đồng OpenAPI 3.0 cho module lập lịch, kèm tài liệu hóa cơ chế bảo mật `bearerAuth` JWT, ma trận vai trò quyền hạn cho từng endpoint và mã lỗi bảo mật 401/403.
* **Reviewer**: Chịu trách nhiệm xác minh biên dịch, phân tích tĩnh mã nguồn, rà soát tuân thủ OWASP A01 (Broken Access Control) và A07 (Identification and Authentication Failures), phát hiện xung đột dependency, xác minh tính nhất quán giữa các bộ tích hợp SDK.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)

Giai đoạn 2 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng khách quan sau:

* `schedule-service` biên dịch sạch thông qua `mvn -f ./sources/backend/schedule-service/pom.xml compile` và khởi động thành công với Spring Profile `docker`.
* Bốn endpoint RESTful (`POST /api/v1/schedules`, `GET /api/v1/schedules/{id}`, `PUT /api/v1/schedules/{id}`, `DELETE /api/v1/schedules/{id}`) trả về đúng mã HTTP 200/201/204 theo đặc tả.
* Ba bộ tích hợp SDK (Facebook, Instagram, TikTok) hoàn tất gọi HTTP thành công tới các API đồ thị tương ứng với khóa truy cập mặc định; lỗi mạng hoặc HTTP 5xx kích hoạt `SocialPlatformException` với cơ chế retry có backoff lũy thừa tối đa 3 lần.
* `api-gateway` biên dịch sạch, chuỗi filter xác thực JWT hoạt động đúng: token hợp lệ cho 4 vai trò (ADMIN, USER, SCHEDULER, ANALYST) được phép truy cập đúng endpoint tương ứng; token hết hạn trả về HTTP 401 với mã `TOKEN_EXPIRED`.
* Hợp đồng OpenAPI 3.0 `./sources/docs/api/ScheduleApiContract.yaml` được tạo ra với đầy đủ schema, phản hồi mẫu, tham chiếu bảo mật `bearerAuth` và mã lỗi 400/401/403/429.
* Tất cả các mã định danh truy vết `[REQ-001]`, `[EXC-001]`, `[EXC-002]`, `[ARC-001]`, `[ARC-002]`, `[ARC-003]`, `[ARC-004]`, `[ARC-005]`, `[ARC-006]` được ánh xạ 1:1 vào các tệp vật lý tương ứng.
* Không có cảnh báo OWASP Top 10 nào (A01, A02, A03, A07) bị phát hiện bởi dependency scan hoặc code review.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo module lịch đăng bài và bộ tích hợp đa nền tảng<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 1.1: Triển khai tầng Entity, Repository và DTO cho module lịch đăng bài
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải khởi tạo thực thể JPA `ScheduleEntity` ánh xạ chính xác tới bảng `schedules` đã được di trú tại Giai đoạn 1 theo sơ đồ ER. Sử dụng annotation `@Entity`, `@Table(name="schedules", schema="schedule_schema")` để khoanh vùnh đúng schema-per-tenant. Định nghĩa cột `scheduleId` với `@Id` và kiểu UUID, đồng thời thiết lập `@Column` với ràng buộc kiểm tra miền chuẩn ANSI SQL thông qua `@Check` cho cột `status` chỉ chấp nhận tập giá trị `('PENDING', 'SENT', 'FAILED', 'CANCELLED')` và cột `platform` chỉ chấp nhận `('FACEBOOK', 'INSTAGRAM', 'TIKTOK')`. Thiết lập mối quan hệ `@ManyToOne(fetch = FetchType.LAZY)` với thực thể `UserEntity` để tham chiếu khóa ngoại `userId` nhằm tránh N+1 query problem. Đảm bảo tính bất biến của các trường cốt lõi trong giai đoạn khởi tạo bằng cách sử dụng `@Column(updatable = false)` cho `createdAt` và `scheduleId`. Bổ sung annotation `@PrePersist` và `@PreUpdate` tại lớp `@EntityListeners(AuditingEntityListener.class)` để tự động cập nhật `createdAt` và `updatedAt`. Tích hợp Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` để giảm boilerplate. Tạo tệp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/repository/ScheduleRepository.java` kế thừa `JpaRepository<ScheduleEntity, UUID>` với các phương thức truy vấn tùy biến: `findByUserIdAndStatus`, `findByTenantIdAndScheduledTimeBetween` để phục vụ worker pool thực thi lịch. Đảm bảo sử dụng `@Query` với JPQL named parameter để chống SQL injection theo OWASP A03. Tạo tệp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleRequestDto.java` với các annotation `@NotNull`, `@NotBlank`, `@Size(min=1, max=5000)`, `@Pattern(regexp="^(Facebook|Instagram|TikTok)$")` cho trường `platform`. Tạo `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dto/ScheduleResponseDto.java` với các trường `scheduleId`, `userId`, `platform`, `content`, `scheduledTime`, `status`, `actualSentTime`, `retryCount`, `createdAt`, `updatedAt` được sinh tự động từ entity thông qua mapper method.

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

#### 📝 TÁC VỤ CON 1.2: Kiểm thử ánh xạ thực thể Schedule
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntity.java;./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntityTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải xây dựng lớp kiểm thử đơn vị JUnit 5 kết hợp AssertJ tại `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/entity/ScheduleEntityTest.java` để xác minh tính chính xác của ánh xạ JPA trên `ScheduleEntity`. Lớp kiểm thử phải sử dụng `@ExtendWith(MockitoExtension.class)` và khởi tạo instance thông qua builder pattern hoặc constructor đầy đủ. Viết các trường hợp kiểm thử khẳng định việc gán giá trị cho các trường `scheduleId`, `userId`, `platform`, `content`, `scheduledTime`, `status`, `actualSentTime`, `retryCount` thông qua các setter hoạt động đúng. Xác nhận rằng cơ chế sinh UUID thông qua `UUID.randomUUID()` và ép kiểu dữ liệu hoạt động đúng theo chuẩn JPA 3.1 khi persist xuống cơ sở dữ liệu. Kiểm thử phương thức `@PrePersist` tự động gán giá trị `createdAt = LocalDateTime.now()` và `@PreUpdate` gán `updatedAt = LocalDateTime.now()`. Xác nhận `@ManyToOne` lazy loading với `UserEntity` không gây N+1 query problem. Sử dụng Testcontainers PostgreSQL 16-alpine để verify rằng entity có thể persist thành công với đầy đủ ràng buộc CHECK trên cột `status` và `platform`. Khẳng định ngoại lệ `DataIntegrityViolationException` được ném khi chèn giá trị `status = 'INVALID_STATUS'`.

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

#### 📝 TÁC VỤ CON 1.3: Xây dựng Service và Controller cho module lịch đăng bài
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải phát triển `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java` chứa logic nghiệp vụ với annotation `@Service` và `@Transactional`. Triển khai bốn phương thức chính: (1) `createSchedule(ScheduleRequestDto request, UUID userId)` trả về `ScheduleResponseDto`, khởi tạo entity với `status = PENDING`, `retryCount = 0`, gán `userId` và `tenantId` từ SecurityContextHolder; (2) `getScheduleById(UUID scheduleId)` thực hiện truy vấn theo khóa chính, đồng thời kiểm tra quyền sở hữu theo `userId` để ngăn IDOR theo OWASP A01; (3) `updateStatusToSent(UUID scheduleId)` chuyển trạng thái sang `SENT` và ghi nhận `actualSentTime = LocalDateTime.now()`; (4) `deleteSchedule(UUID scheduleId)` chuyển trạng thái sang `CANCELLED` thay vì xóa cứng để bảo toàn lịch sử. Tích hợp `SocialPlatformDispatcher` để định tuyến yêu cầu tới client nền tảng phù hợp thông qua phương thức `dispatchPublish(ScheduleEntity entity)`. Bọc ngoại lệ `SocialPlatformException` trong khối `try-catch` để ghi log cấp độ `ERROR` kèm correlation ID, đồng thời kích hoạt logic thử lại với backoff lũy thừa tối đa 3 lần thông qua annotation `@Retryable` từ Resilience4j. Áp dụng kiểm tra phân quyền dựa trên SecurityContextHolder để đảm bảo chỉ chủ sở hữu lịch hoặc Admin mới có quyền thao tác. Tạo tệp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/controller/ScheduleController.java` với annotation `@RestController` và `@RequestMapping("/api/v1/schedules")`. Triển khai bốn endpoint: `POST /` (tạo mới, trả về 201 Created), `GET /{scheduleId}` (truy vấn, trả về 200 OK), `PUT /{scheduleId}/status` (cập nhật trạng thái), `DELETE /{scheduleId}` (hủy lịch, trả về 204 No Content). Áp dụng `@PreAuthorize("hasAnyRole('ADMIN','USER','SCHEDULER')")` cho endpoint tạo mới và `@PreAuthorize("hasAnyRole('ADMIN','USER')")` cho endpoint truy vấn/cập nhật theo RBAC 4 vai trò. Bổ sung annotation `@Valid` cho các payload request để kích hoạt Jakarta Validation tự động. Tạo tệp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/dispatcher/SocialPlatformDispatcher.java` đóng vai trò facade điều phối cuộc gọi tới client phù hợp dựa trên giá trị enum `Platform`.

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

#### 📝 TÁC VỤ CON 1.4: Kiểm thử tích hợp Controller và Service
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java;./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleServiceTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải sử dụng Mockito kết hợp với JUnit 5 để giả lập các thành phần phụ thuộc (`ScheduleRepository`, `SocialPlatformDispatcher`) tại lớp kiểm thử `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleServiceTest.java`. Sử dụng `@ExtendWith(MockitoExtension.class)` và annotation `@Mock` để khai báo các dependency giả lập. Viết kịch bản kiểm thử xác nhận `createSchedule` trả về thực thể với trạng thái `PENDING` và `retryCount = 0`. Đánh giá việc gọi `updateStatusToSent` thiết lập đúng thời gian gửi thực tế (`actualSentTime` gần với `LocalDateTime.now()`). Kiểm tra khả năng chịu lỗi khi `SocialPlatformDispatcher` ném ra `SocialPlatformException`, đảm bảo lỗi được ghi nhật ký cấp độ `ERROR` và phương thức `retry` được kích hoạt với số lần thử tối đa 3. Bổ sung kiểm thử tích hợp Controller sử dụng `@WebMvcTest(ScheduleController.class)` kết hợp `MockMvc` để xác minh: (1) endpoint `POST /api/v1/schedules` trả về HTTP 201 khi payload hợp lệ; (2) trả về HTTP 400 với mã `VALIDATION_FAILED` khi payload thiếu trường `platform` hoặc `content`; (3) trả về HTTP 403 khi SecurityContext thiếu role phù hợp; (4) trả về HTTP 401 khi không có JWT token. Đảm bảo test suite bao phủ 100% nhánh xử lý chính của `ScheduleService` và `ScheduleController`. Sử dụng `@DisplayName` cho mỗi test case để báo cáo rõ ràng trong surefire report.

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

#### 📝 TÁC VỤ CON 1.5: Rà soát mã nguồn và tối ưu hóa chuẩn code
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/service/ScheduleService.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải thực hiện đánh giá mã nguồn thủ công và tự động trên `ScheduleService` và `ScheduleController`. Phân tích độ phức tạp Cyclomatic và đề xuất phương án tách nhỏ phương thức nếu vượt ngưỡng 10 theo quy tắc SonarQube. Đảm bảo các chuẩn đặt tên biến (camelCase cho biến, PascalCase cho lớp), tiêm phụ thuộc (DI) qua constructor thay vì `@Autowired` field injection, và phong cách lập trình phản ứng (reactive) hoặc bất đồng bộ đều nhất quán với kiến trúc hệ thống. Xác nhận không có rò rỉ tài nguyên hoặc điều kiện đua (race condition) tiềm ẩn trong luồng cập nhật trạng thái bằng cách kiểm tra việc sử dụng `@Version` cho optimistic locking hoặc pessimistic lock thông qua `LockModeType`. Rà soát việc sử dụng `SecurityContextHolder` để đảm bảo ngăn chặn IDOR attack theo OWASP A01 thông qua việc kiểm tra `userId` của lịch đối chiếu với `principal` trong JWT. Xác minh tất cả annotation `@PreAuthorize` đều sử dụng đúng role literal `hasRole('ADMIN')` thay vì authority thô. Phát hiện và báo cáo các code smell thuộc nhóm BLOCKER hoặc CRITICAL trong SonarQube Quality Gate.

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

#### 📝 TÁC VỤ CON 1.6: Soạn thảo tài liệu hợp đồng API cho module lập lịch
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/api/ScheduleApiContract.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [ARC-005]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải tạo tệp OpenAPI 3.0 chuẩn YAML mô tả toàn bộ endpoint của module lập lịch tại `./sources/docs/api/ScheduleApiContract.yaml`. Khai báo thông tin `openapi: 3.0.3`, `info: {title: "Social Scheduler - Schedule API Contract", version: "1.0.0"}`. Định nghĩa `paths` cho bốn endpoint: `POST /api/v1/schedules`, `GET /api/v1/schedules/{scheduleId}`, `PUT /api/v1/schedules/{scheduleId}/status`, `DELETE /api/v1/schedules/{scheduleId}`. Mô tả chi tiết các DTO `ScheduleRequest`, `ScheduleResponse` với các ràng buộc `required`, `minLength`, `maxLength`, `pattern` cho trường `platform` theo whitelist `^(Facebook|Instagram|TikTok)$`. Đảm bảo tài liệu tham chiếu chính xác các giá trị enum của cột `platform` và `status` thông qua schema reference. Định nghĩa các mã phản hồi HTTP đầy đủ: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request - VALIDATION_FAILED), 401 (Unauthorized - TOKEN_EXPIRED), 403 (Forbidden - INSUFFICIENT_ROLE), 429 (Too Many Requests). Bổ sung `securitySchemes` với `bearerAuth` sử dụng HTTP Bearer với định dạng JWT. Tham chiếu `security` cho từng endpoint với `bearerAuth: []`. Bao gồm ví dụ mẫu (examples) cho mỗi request và response thành công. Tệp phải tuân thủ cấu trúc YAML chuẩn OpenAPI 3.0.3, sử dụng `$ref` để tái sử dụng schema và đảm bảo backend compiler có thể tái sử dụng để sinh client SDK.

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

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Triển khai tích hợp SDK mạng xã hội và bảo mật API Gateway<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 2.1: Xây dựng bộ tích hợp SDK mạng xã hội Facebook, Instagram, TikTok
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải cài đặt ba lớp Client sử dụng `RestClient` (đồng bộ) của Spring Framework 6.1.x kết hợp với `WebClient` reactive tại các đường dẫn `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java`, `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/InstagramClient.java`, `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/TikTokClient.java`. Mỗi Client được đánh dấu `@Component` với constructor injection nhận `RestClient.Builder` đã được cấu hình sẵn `connectTimeout`, `readTimeout` 5 giây từ `application.yml`. Tiêm các thuộc tính cấu hình endpoint URL (`facebook.api.base-url=https://graph.facebook.com/v18.0`, `instagram.api.base-url=https://graph.instagram.com/v18.0`, `tiktok.api.base-url=https://open.tiktokapis.com/v2`) và access token thông qua annotation `@Value` đọc từ biến môi trường `FACEBOOK_ACCESS_TOKEN`, `INSTAGRAM_ACCESS_TOKEN`, `TIKTOK_ACCESS_TOKEN`. Triển khai phương thức `publishPost(ScheduleEntity schedule): PublishResult` cho mỗi client với logic gọi API tương ứng: Facebook sử dụng endpoint `/{page-id}/feed`, Instagram sử dụng endpoint `/{ig-user-id}/media`, TikTok sử dụng endpoint `/post/publish/video/init/`. Bọc các cuộc gọi mạng bên trong khối `try-catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex)` chuẩn hóa để ném ra `SocialPlatformException` khi xảy ra lỗi timeout hoặc lỗi HTTP 4xx/5xx từ phía nhà cung cấp. Tích hợp annotation `@Retryable(retryFor = SocialPlatformException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))` từ Spring Retry để tự động thử lại với backoff lũy thừa. Tạo lớp `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/exception/SocialPlatformException.java` kế thừa `RuntimeException` với các trường `platform`, `errorCode`, `httpStatus`, `retryable` để mã hoá thông tin lỗi có cấu trúc. Sử dụng SLF4J logging có cấu trúc với MDC context bao gồm `scheduleId`, `platform`, `tenantId`, `correlationId` để hỗ trợ truy vết theo OWASP A09.

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

#### 📝 TÁC VỤ CON 2.2: Kiểm thử độ bền của bộ tích hợp SDK mạng xã hội
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClient.java;./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClientTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[REQ-001], [EXC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải sử dụng `MockWebServer` (thư viện okhttp MockWebServer 4.12.x) tại `./sources/backend/schedule-service/src/test/java/org/nlh4j/socialscheduler/scheduleservice/integration/FacebookClientTest.java` để giả lập phản hồi từ máy chủ Facebook. Cấu hình MockWebServer với URL động thông qua `@DynamicPropertySource` để `RestClient` bean được inject tự động trỏ về URL của MockServer trong môi trường test. Viết các kịch bản kiểm thử: (1) `publishPost_whenFacebookReturns200_thenReturnSuccessResponse` - giả lập phản hồi HTTP 200 với body JSON chứa `{"id":"123_456","status":"success"}`, khẳng định `PublishResult` được trả về với `platformPostId = "123_456"`; (2) `publishPost_whenFacebookReturns500_thenThrowSocialPlatformException` - giả lập phản hỗi HTTP 500 với body JSON chứa `{"error":{"code":2,"message":"Internal error"}}`, khẳng định `SocialPlatformException` được ném ra với `httpStatus = 500`, `errorCode = "INTERNAL_ERROR"`, `retryable = true`; (3) `publishPost_whenNetworkTimeout_thenRetryThreeTimes` - giả lập `SocketTimeoutException` ba lần liên tiếp, khẳng định phương thức `publishPost` được gọi đúng 3 lần (maxAttempts = 3); (4) `publishPost_whenAccessTokenInvalid_thenThrowNonRetryableException` - giả lập phản hồi HTTP 401, khẳng định `SocialPlatformException` với `retryable = false` (vì 401 không nên retry). Lặp lại quy trình tương tự cho `InstagramClientTest.java` và `TikTokClientTest.java` với cấu trúc endpoint và mã lỗi đặc thù của từng nền tảng. Sử dụng `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` để đảm bảo thứ tự thực thi test ổn định. Bổ sung `@DisplayName` cho mỗi test case để sinh báo cáo rõ ràng trong surefire report.

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

#### 📝 TÁC VỤ CON 2.3: Cấu hình tầng bảo mật OAuth2 Resource Server và JWT Decoder tại API Gateway
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải cấu hình Spring Security 6 tại `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java` với annotation `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity(prePostEnabled = true)`. Tạo `SecurityFilterChain` bean cấu hình `oauth2ResourceServer().jwt()` để kích hoạt xác thực JWT cho toàn bộ request. Tạo `JwtDecoder` bean tùy chỉnh kế thừa `NimbusJwtDecoder` để xác thực chữ ký số bằng khóa công khai RSA đọc từ `application.yml` (`security.oauth2.jwt.public-key-location=classpath:keys/jwt-public.pem`) và kiểm tra thời hạn của token với `setJwtValidator` bao gồm `JwtTimestampValidator` và custom validator kiểm tra `aud` claim khớp với `social-scheduler-api`. Định nghĩa `JwtAuthenticationConverter` để ánh xạ claim `roles` trong JWT thành `SimpleGrantedAuthority` với prefix `ROLE_`, ánh xạ chính xác 4 vai trò RBAC [ARC-001] (Admin) → `ROLE_ADMIN`, [ARC-002] (User) → `ROLE_USER`, [ARC-003] (Scheduler) → `ROLE_SCHEDULER`, [ARC-004] (Analyst) → `ROLE_ANALYST`. Định nghĩa chuỗi lọc yêu cầu xác thực cho tất cả các đường dẫn `/api/v1/**` thông qua `authorizeHttpRequests(authz -> authz.requestMatchers("/actuator/health/**").permitAll().requestMatchers("/api/v1/**").authenticated().anyRequest().denyAll())`. Vô hiệu hóa CSRF cho các RESTful API phù hợp với tiêu chuẩn Stateless (`csrf(csrf -> csrf.disable())`). Cấu hình CORS với whitelist origin đọc từ cấu hình: `cors(cors -> cors.configurationSource(corsConfigurationSource()))` chỉ chấp nhận origin `https://app.socialscheduler.local`. Đảm bảo tuân thủ chuẩn OWASP A01 bằng cách tích hợp `RbacPredicate` thực thi ánh xạ quyền hạn 4 vai trò trên từng endpoint cụ thể thông qua `@PreAuthorize`. Tạo lớp `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/JwtAuthFilter.java` kế thừa `OncePerRequestFilter` để trích xuất JWT từ header `Authorization: Bearer <token>`, validate token, và đặt `SecurityContextHolder` với thông tin principal. Bổ sung xử lý `JwtException` để trả về HTTP 401 với mã lỗi `TOKEN_EXPIRED` hoặc `TOKEN_INVALID` thông qua `AuthenticationEntryPoint` tùy chỉnh, đảm bảo thông điệp phản hồi không tiết lộ chi tiết nội bộ theo OWASP A09.

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

#### 📝 TÁC VỤ CON 2.4: Kiểm thử xác thực và phân quyền tại API Gateway
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java;./sources/backend/api-gateway/src/test/java/org/nlh4j/socialscheduler/gateway/SecurityConfigTest.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải sử dụng `@SpringBootTest` với `WebEnvironment.RANDOM_PORT` và `MockMvc` tại `./sources/backend/api-gateway/src/test/java/org/nlh4j/socialscheduler/gateway/SecurityConfigTest.java` để kiểm thử tích hợp luồng bảo mật toàn diện. Tạo bốn JWT token mẫu với role khác nhau thông qua `Nimbus JOSE + JWT` library: token cho Admin với claim `roles: ["ADMIN"]`, token cho User với `roles: ["USER"]`, token cho Scheduler với `roles: ["SCHEDULER"]`, token cho Analyst với `roles: ["ANALYST"]`. Sử dụng RSA key pair cố định trong thư mục `src/test/resources/keys/` để ký và xác thực token. Gửi yêu cầu `GET /api/v1/schedules/{id}` với header `Authorization: Bearer <admin_token>` và khẳng định phản hồi HTTP 200 OK. Gửi yêu cầu tương tự với token của Analyst và khẳng định phản hồi HTTP 403 Forbidden với mã `INSUFFICIENT_ROLE`. Kiểm thử trường hợp token hết hạn bằng cách tạo JWT với `exp = now - 3600`, hệ thống phải trả về HTTP 401 với mã lỗi `TOKEN_EXPIRED` và thông điệp yêu cầu đăng nhập lại. Kiểm thử trường hợp token sai chữ ký (ký bằng khóa bí mật khác), hệ thống phải trả về HTTP 401 với mã `TOKEN_INVALID`. Kiểm thử endpoint `POST /api/v1/schedules` với payload hợp lệ và token của Admin, khẳng định HTTP 201 Created. Xác minh rằng endpoint `/actuator/health` có thể truy cập mà không cần token (permitAll). Đảm bảo test suite bao phủ 100% các nhánh xử lý chính của `SecurityConfig` và `JwtAuthFilter`. Bổ sung `@DisplayName` cho mỗi test case.

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

#### 📝 TÁC VỤ CON 2.5: Rà soát cấu hình bảo mật và tuân thủ OWASP
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/SecurityConfig.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-005], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải thực hiện đánh giá chuyên sâu cấu hình Spring Security để phát hiện các lỗ hổng OWASP A01 (Broken Access Control) và A07 (Identification and Authentication Failures). Đảm bảo rằng mọi endpoint nhạy cảm đều yêu cầu xác thực và phân quyền chặt chẽ thông qua annotation `@PreAuthorize` hoặc URL-based authorization trong `SecurityFilterChain`. Xác minh việc vô hiệu hóa CSRF cho các RESTful API phù hợp với tiêu chuẩn Stateless (`csrf.disable()`), đồng thời đảm bảo CORS được cấu hình với whitelist origin cụ thể thay vì ký tự đại diện `*`. Kiểm tra tính bảo mật của việc lưu trữ khóa bí mật JWT: đảm bảo private key không được commit vào repository và chỉ được đọc từ biến môi trường hoặc Kubernetes Secret thông qua Spring Cloud Config. Xác minh rằng `JwtDecoder` sử dụng thuật toán mạnh (RS256 hoặc ES256) và từ chối thuật toán `none` hoặc `HS256` với khóa yếu. Đánh giá việc sử dụng `RbacPredicate` để đảm bảo không có vai trò nào được phép truy cập ngoài phạm vi đã khai báo trong [ARC-001] đến [ARC-004]. Phát hiện xung đột giữa URL-based authorization và method-level `@PreAuthorize` có thể dẫn đến bypass phân quyền. Rà soát tệp `./sources/backend/api-gateway/src/main/java/org/nlh4j/socialscheduler/gateway/RbacPredicate.java` để đảm bảo logic dự đoán phân quyền hoạt động chính xác cho 4 vai trò và không có hardcode bypass. Tạo báo cáo review ghi nhận tuân thủ OWASP A01/A07 và đề xuất cải tiến nếu phát hiện điểm yếu.

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

#### 📝 TÁC VỤ CON 2.6: Tài liệu hóa hợp đồng bảo mật và ma trận phân quyền RBAC
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/api/ScheduleApiContract.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải cập nhật tệp OpenAPI `./sources/docs/api/ScheduleApiContract.yaml` đính kèm các chi tiết về cơ chế bảo mật `bearerAuth` (JWT) tại khối `components.securitySchemes`. Định nghĩa rõ ràng `securitySchemes.bearerAuth` với `type: http`, `scheme: bearer`, `bearerFormat: JWT`, kèm mô tả `description: "JWT Bearer token được cấp bởi OAuth2 Authorization Server, chứa claim 'roles' để phân quyền RBAC 4 vai trò (ADMIN, USER, SCHEDULER, ANALYST)".` Bổ sung `security: - bearerAuth: []` cho toàn bộ các endpoint trong `paths`. Định nghĩa ma trận vai trò quyền hạn dưới dạng bảng Markdown trong phần `info.description` hoặc tệp bổ sung `x-rbac-matrix`, ánh xạ rõ ràng từng endpoint với role được phép truy cập: `POST /api/v1/schedules` yêu cầu `ADMIN, USER, SCHEDULER`; `GET /api/v1/schedules/{id}` yêu cầu `ADMIN, USER`; `PUT /api/v1/schedules/{id}/status` yêu cầu `ADMIN, SCHEDULER`; `DELETE /api/v1/schedules/{id}` yêu cầu `ADMIN, USER`. Bổ sung tài liệu mô tả chi tiết các mã lỗi bảo mật: `TOKEN_EXPIRED` (401), `TOKEN_INVALID` (401), `INSUFFICIENT_ROLE` (403) và quy trình xử lý token hết hạn (client phải gọi refresh token endpoint tại `auth-service`). Tích hợp tham chiếu tới chính sách tuân thủ OWASP Top 10 mà hệ thống áp dụng, đặc biệt là A01 (Broken Access Control), A02 (Cryptographic Failures), A07 (Identification and Authentication Failures). Bổ sung ví dụ mẫu request kèm header `Authorization: Bearer eyJhbGciOiJSUzI1NiIs...` cho mỗi endpoint. Đảm bảo tài liệu tuân thủ cấu trúc YAML chuẩn OpenAPI 3.0.3 và sử dụng extension field `x-` cho các metadata tùy biến.

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