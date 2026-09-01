# Giai đoạn 1: <!--PHASE_NAME_START-->Khởi tạo khung Microservices và di trú lược đồ cơ sở dữ liệu ban đầu<!--PHASE_NAME_END-->

## 📊 Kiểm soát Tài liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Blueprint** | ARCH-20260831230418 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 1 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Khởi tạo khung Microservices và di trú lược đồ cơ sở dữ liệu ban đầu<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Xây dựng hạ tầng kỹ thuật nền tảng cho dự án social-scheduler, bao gồm descriptor build cha-con cho kiến trúc Microservices sử dụng Spring Boot 3 và Spring Cloud, đồng thời thiết lập toàn bộ lược đồ quan hệ ban đầu thông qua Flyway DDL cho bốn dịch vụ nghiệp vụ cốt lõi, đảm bảo tính cô lập kiến trúc và trật tự phụ thuộc nghiêm ngặt trước khi triển khai logic nghiệp vụ<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/08/31 23:04:18 |
| **Tác giả** | Kiến trúc sư Hệ thống Doanh nghiệp (SA Agent) |
| **Phê duyệt** | Chờ phê duyệt quản trị kỹ thuật |

## 1. Phạm vi Hoạt động & Mục tiêu Giai đoạn

Giai đoạn 1 tập trung 100% vào việc kiến tạo hạ tầng kỹ thuật nền tảng cho dự án `social-scheduler`, bao gồm khởi tạo descriptor build cha-con cho kiến trúc Microservices sử dụng Spring Boot 3.3.x trên JDK 21 LTS kết hợp Spring Cloud 2023.x, thiết lập quản lý phụ thuộc tập trung qua Maven `<dependencyManagement>`, đồng thời xây dựng toàn bộ lược đồ quan hệ ban đầu thông qua Flyway DDL cho bốn dịch vụ nghiệp vụ cốt lõi (`user-service`, `schedule-service`, `ai-service`, `rate-limit-service`). 

Phạm vi giai đoạn này tuyệt đối không chứa bất kỳ mã nguồn controller, service nghiệp vụ, bộ tích hợp SDK bên thứ ba hay bộ xử lý ngoại lệ tập trung nào; tất cả các tác vụ đó được ủy thác cho các giai đoạn tiếp theo nhằm đảm bảo tính cô lập kiến trúc và trật tự phụ thuộc nghiêm ngặt. Việc khởi tạo descriptor build cha-con tại Giai đoạn 1 đóng vai trò là tiền đề bắt buộc để các giai đoạn 2, 3, 4 có thể triển khai logic nghiệp vụ một cách an toàn và nhất quán. Bốn tệp Flyway DDL được tạo ra tại đây sẽ định nghĩa cấu trúc bảng dữ liệu cốt lõi phục vụ cho toàn bộ vòng đời hệ thống, đồng thời thiết lập cơ chế schema-per-tenant và các ràng buộc khóa ngoại theo sơ đồ ER đã được phê duyệt.

## 2. Phạm vi Kỹ thuật Được phép & Ranh giới Thư mục

Danh sách kiểm tra kỹ thuật các tệp vật lý được phép tạo hoặc xử lý trong phạm vi giai đoạn này, mọi mục đều kèm mã định danh truy vết:

* `./sources/backend/pom.xml` — Descriptor build cha cho toàn bộ dự án Microservices. [ARC-000]
* `./sources/backend/user-service/pom.xml` — Descriptor build con cho dịch vụ người dùng. [ARC-000]
* `./sources/backend/schedule-service/pom.xml` — Descriptor build con cho dịch vụ lịch đăng bài. [ARC-000]
* `./sources/backend/ai-service/pom.xml` — Descriptor build con cho dịch vụ AI/ML. [ARC-000]
* `./sources/backend/rate-limit-service/pom.xml` — Descriptor build con cho dịch vụ giới hạn tỷ lệ. [ARC-000]
* `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql` — Flyway DDL khởi tạo bảng `users`. [DAT-001]
* `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql` — Flyway DDL khởi tạo bảng `schedules`. [DAT-001]
* `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql` — Flyway DDL khởi tạo bảng `performance_metrics`. [DAT-002]
* `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql` — Flyway DDL khởi tạo bảng `rate_limits`. [DAT-003]
* `./sources/infra/test/maven-build-integration.sh` — Script kiểm thử tích hợp Maven đa mô-đun. [ARC-000]
* `./sources/docs/architecture/MicroservicesOverviewBlueprint.md` — Tài liệu kiến trúc tổng quan Microservices. [ARC-000]
* `./sources/docs/architecture/DatabaseSchemaCatalog.md` — Tài liệu catalog mô tả toàn bộ lược đồ quan hệ. [DAT-001], [DAT-002], [DAT-003]

**LƯU Ý ĐẶC BIỆT VỀ HẠ TẦNG FRONTEND/MOBILE**: Tại Giai đoạn 1, dự án chưa khởi tạo lớp frontend hay mobile. Toàn bộ tệp khai báo `package.json` và `tsconfig.json` sẽ được tạo tại giai đoạn phù hợp sau khi backend Microservices ổn định.

**CÁC ĐƯỜNG DẪN VẬT LÝ MỤC TIÊU TỔNG THỂ**: 
* `./sources/backend/pom.xml` [ARC-000]
* `./sources/backend/user-service/pom.xml` [ARC-000]
* `./sources/backend/schedule-service/pom.xml` [ARC-000]
* `./sources/backend/ai-service/pom.xml` [ARC-000]
* `./sources/backend/rate-limit-service/pom.xml` [ARC-000]
* `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql` [DAT-001], [DAT-ALL (1 to 3)]
* `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql` [DAT-001], [DAT-ALL (1 to 3)]
* `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql` [DAT-002], [DAT-ALL (1 to 3)]
* `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql` [DAT-003], [DAT-ALL (1 to 3)]
* `./sources/docs/architecture/DatabaseSchemaCatalog.md` [DAT-001], [DAT-002], [DAT-003], [DAT-ALL (1 to 3)]

## 3. Chỉ thị Chức năng Chuyên biệt cho Sub-Agent

Phân bổ nhiệm vụ và ràng buộc kỹ thuật cho từng persona Sub-Agent hoạt động trong giai đoạn này:

* **Coder**: Hoạt động với vai trò Nhà phát triển Ứng dụng Cao cấp, chịu trách nhiệm triển khai mã nguồn ứng dụng thuần túy bao gồm descriptor build Maven cha-con và các script Flyway DDL cho bốn dịch vụ nghiệp vụ cốt lõi. Bị cấm viết bộ kiểm thử, tệp cấu hình hạ tầng hoặc tài liệu kiến trúc tổng quan.
* **Tester**: Hoạt động với vai trò Trưởng phòng QC/QA, chuyên trách kỹ thuật bộ kiểm thử, xác thực chất lượng. Chịu trách nhiệm sinh script kiểm thử tích hợp Maven đa mô-đun và lớp Testcontainers cho di trú schema. Bị cấm sửa đổi mã nguồn sản phẩm. Đối với các tác vụ kiểm thử tích hợp bao trùm toàn bộ hệ thống mà không thể khoanh vùng một tệp nguồn cụ thể, persona này phải sử dụng token `INTEGRATION_SCOPE` làm tham số đầu tiên trong cặp phân tách bằng dấu chấm phẩy.
* **Doc**: Hoạt động với vai trò Chuyên gia Viết tài liệu Kỹ thuật và Kiến trúc sư Hệ thống Doanh nghiệp, chịu trách nhiệm biên soạn các tệp blueprint kiến trúc Microservices, catalog lược đồ cơ sở dữ liệu. Mọi tệp tài liệu kỹ thuật được sinh ra phải được liệt kê dưới dạng đường dẫn tệp thực thể kết thúc bằng phần mở rộng `.md` và nằm hoàn toàn trong cấu trúc lưu trữ tập trung `./sources/docs/`.
* **Reviewer**: Chịu trách nhiệm xác minh biên dịch, phân tích tĩnh và vá lỗi phòng thủ. Chuyên đánh giá chất lượng mã nguồn, phát hiện xung đột phiên bản descriptor build, xác minh tính nhất quán giữa các khóa ngoại trong lược đồ cơ sở dữ liệu, giải quyết các blocker của SonarQube Quality Gate.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)

Giai đoạn 1 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng khách quan sau:

* Descriptor build cha `./sources/backend/pom.xml` biên dịch thành công qua lệnh `mvn validate` và liệt kê đầy đủ năm module con (`user-service`, `schedule-service`, `ai-service`, `rate-limit-service`, `api-gateway`).
* Bốn descriptor build con (`user-service`, `schedule-service`, `ai-service`, `rate-limit-service`) biên dịch sạch thông qua `mvn -f ./sources/backend/<service-name>/pom.xml compile`.
* Bốn tệp Flyway DDL chạy thành công trên Testcontainers PostgreSQL, tạo ra bốn schema (`user_schema`, `schedule_schema`, `ai_schema`, `rate_limit_schema`) với đầy đủ ràng buộc khóa chính, khóa ngoại và ràng buộc kiểm tra.
* Tất cả các mã định danh truy vết `[ARC-000]`, `[DAT-001]`, `[DAT-002]`, `[DAT-003]`, `[DAT-ALL (1 to 3)]` được ánh xạ 1:1 vào các tệp vật lý tương ứng.
* Tài liệu blueprint kiến trúc Microservices và catalog lược đồ cơ sở dữ liệu được hoàn thiện với sơ đồ Mermaid minh họa quan hệ giữa các dịch vụ và thực thể.
* Không có cảnh báo OWASP Top 10 nào bị phát hiện bởi dependency scan trong descriptor build cha-con.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo descriptor build cha-con và hạ tầng Spring Cloud<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 1.1: Sinh descriptor build cha cho toàn dự án Microservices
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư cao cấp phải khởi tạo descriptor Maven cha `./sources/backend/pom.xml` với khai báo `<packaging>pom</packaging>` để đóng vai trò tổng hợp module, đồng thời liệt kê đầy đủ các module con trong phần `<modules>` bao gồm `user-service`, `schedule-service`, `ai-service`, `rate-limit-service`, `api-gateway`. Bắt buộc khai báo thẻ `<parent>` tham chiếu tới `spring-boot-starter-parent` phiên bản 3.3.5 và `spring-cloud-dependencies` phiên bản 2023.0.3 thông qua phần tử `<dependencyManagement>`. Khai báo `<properties>` định nghĩa `java.version=21`, `maven.compiler.source=21`, `maven.compiler.target=21`. Phần `<dependencyManagement>` phải bao gồm đầy đủ các phiên bản ổn định cho Spring Boot Starter Parent 3.3.5, Spring Cloud Starter, Flyway Core 10.20.x, PostgreSQL Driver 42.7.x, Apache Kafka Client 3.7.x, Lettuce Redis Client 6.4.x, OAuth2 Resource Server 6.3.x, Bucket4j Core 8.10.x, OpenAI Java SDK 0.18.x. Đảm bảo descriptor biên dịch thành công khi chạy `mvn validate` từ thư mục `./sources/backend/`. Mọi giá trị phiên bản phải được khóa cứng trong `<properties>` của descriptor cha để đảm bảo tính nhất quán và tránh xung đột giữa các module con.

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

#### 📝 TÁC VỤ CON 1.2: Sinh descriptor build con cho dịch vụ người dùng
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/user-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh `./sources/backend/user-service/pom.xml` với thẻ `<parent>` tham chiếu chính xác về `groupId`, `artifactId`, `version` của descriptor cha đã tạo tại Tác vụ Con 1.1. Khai báo `<artifactId>user-service</artifactId>` và `<version>1.0.0</version>`, `<packaging>jar</packaging>`. Phần `<dependencies>` phải bao gồm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data JPA 3.3.5, Spring Boot Starter Security 3.3.5, Spring Boot Starter OAuth2 Resource Server 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, Lombok 1.18.34, Springdoc OpenAPI Starter WebMVC UI 2.6.x. Phần `<build>` khai báo plugin `spring-boot-maven-plugin` phiên bản 3.3.5 với cấu hình `<mainClass>` trỏ về `org.nlh4j.socialscheduler.userservice.UserServiceApplication` và plugin `flyway-maven-plugin` phiên bản 10.20.x cấu hình `<url>`, `<user>`, `<password>` đọc từ biến môi trường. Đảm bảo descriptor biên dịch sạch thông qua lệnh `mvn -f ./sources/backend/user-service/pom.xml compile`. Thực thi nguyên tắc OWASP A06 (Vulnerable Components) bằng cách khoá cứng tất cả phiên bản dependency trong descriptor cha.

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

#### 📝 TÁC VỤ CON 1.3: Sinh descriptor build con cho dịch vụ lịch đăng bài
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh `./sources/backend/schedule-service/pom.xml` với `<parent>` tham chiếu descriptor cha, khai báo `<artifactId>schedule-service</artifactId>`, `<version>1.0.0</version>`, `<packaging>jar</packaging>`. Phần `<dependencies>` thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data JPA 3.3.5, Spring Kafka 3.2.x, Spring Data Redis (Lettuce) 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, RestClient (Spring Framework 6.1.x), Springdoc OpenAPI Starter WebMVC UI 2.6.x, Lombok 1.18.34, Resilience4j Spring Boot 3 Starter 2.2.x. Cấu hình `<build>` với plugin `spring-boot-maven-plugin` (mainClass `org.nlh4j.socialscheduler.scheduleservice.ScheduleServiceApplication`) và plugin `flyway-maven-plugin` 10.20.x. Tích hợp annotation processor của Lombok trong phần `<build>` để tự động sinh getter/setter. Đảm bảo khả năng mở rộng ngang thông qua việc cấu hình HikariCP mặc định 50 kết nối thông qua biến môi trường. Descriptor phải biên dịch sạch qua `mvn -f ./sources/backend/schedule-service/pom.xml compile`.

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

#### 📝 TÁC VỤ CON 1.4: Sinh descriptor build con cho dịch vụ AI/ML
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh `./sources/backend/ai-service/pom.xml` với `<parent>` tham chiếu descriptor cha, `<artifactId>ai-service</artifactId>`, `<version>1.0.0</version>`, `<packaging>jar</packaging>`. Phần `<dependencies>` thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter WebFlux 3.3.5 (cho OpenAI WebClient), Spring Boot Starter Data JPA 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, OpenAI Java SDK 0.18.x, Caffeine Cache 3.1.x, Springdoc OpenAPI Starter WebMVC UI 2.6.x, Lombok 1.18.34, Resilience4j Spring Boot 3 Starter 2.2.x. Cấu hình `<build>` với plugin `spring-boot-maven-plugin` (mainClass `org.nlh4j.socialscheduler.aiservice.AiServiceApplication`) và plugin `flyway-maven-plugin` 10.20.x. Đảm bảo khả năng reactive được kích hoạt thông qua việc bao gồm WebFlux starter. Biên dịch sạch qua `mvn -f ./sources/backend/ai-service/pom.xml compile`.

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

#### 📝 TÁC VỤ CON 1.5: Sinh descriptor build con cho dịch vụ giới hạn tỷ lệ
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh `./sources/backend/rate-limit-service/pom.xml` với `<parent>` tham chiếu descriptor cha, `<artifactId>rate-limit-service</artifactId>`, `<version>1.0.0</version>`, `<packaging>jar</packaging>`. Phần `<dependencies>` thêm Spring Boot Starter Web 3.3.5, Spring Boot Starter Data Redis (Lettuce) 3.3.5, Spring Boot Starter Validation 3.3.5, Spring Boot Starter Actuator 3.3.5, Flyway Core 10.20.x, Flyway Database PostgreSQL 10.20.x, PostgreSQL Driver 42.7.x, Bucket4j Core 8.10.x, Bucket4j Redis 8.10.x (Lettuce integration), Lombok 1.18.34, Springdoc OpenAPI Starter WebMVC UI 2.6.x. Cấu hình `<build>` với plugin `spring-boot-maven-plugin` (mainClass `org.nlh4j.socialscheduler.ratelimitservice.RateLimitServiceApplication`) và plugin `flyway-maven-plugin` 10.20.x. Đảm bảo dependency Bucket4j Redis được tích hợp chính xác để hỗ trợ giải thuật Token Bucket phân tán. Biên dịch sạch qua `mvn -f ./sources/backend/rate-limit-service/pom.xml compile`.

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

#### 📝 TÁC VỤ CON 1.6: Biên soạn bộ test tích hợp Maven đa mô-đun
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `INTEGRATION_SCOPE;./sources/infra/test/maven-build-integration.sh`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải sinh script shell `./sources/infra/test/maven-build-integration.sh` thực thi kiểm thử tích hợp cho toàn bộ descriptor build đa mô-đun. Script phải có cấu trúc POSIX-compliant với shebang `#!/usr/bin/env bash`, cờ `set -euo pipefail` để dừng ngay khi phát hiện lỗi. Trình tự thực thi gồm: (1) `mvn -f ./sources/backend/pom.xml clean validate` để xác minh cấu trúc POM cha, (2) `mvn -f ./sources/backend/pom.xml dependency:resolve` để kiểm tra khả năng tải toàn bộ dependency từ Maven Central, (3) `mvn -f ./sources/backend/user-service/pom.xml compile` cho dịch vụ người dùng, (4) `mvn -f ./sources/backend/schedule-service/pom.xml compile` cho dịch vụ lịch đăng bài, (5) `mvn -f ./sources/backend/ai-service/pom.xml compile` cho dịch vụ AI/ML, (6) `mvn -f ./sources/backend/rate-limit-service/pom.xml compile` cho dịch vụ giới hạn tỷ lệ. Mỗi bước phải ghi log dòng lệnh, phiên bản Maven được sử dụng (`mvn --version`), và trạng thái thoát. Kết thúc script trả về mã thoát `0` khi tất cả descriptor biên dịch sạch và mã thoát `1` khi có lỗi bất kỳ. Bao gồm bước `chmod +x ./sources/infra/test/maven-build-integration.sh` sau khi tạo tệp. Tích hợp báo cáo tóm tắt thời gian thực thi bằng cách sử dụng `time` cho mỗi module.

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

#### 📝 TÁC VỤ CON 1.7: Rà soát cấu trúc descriptor build và xác minh tính nhất quán
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/pom.xml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải thực hiện kiểm tra chất lượng toàn bộ descriptor build cha-con đã được tạo tại các tác vụ con 1.1 đến 1.5. Xác minh `<parent>` của mỗi descriptor con trỏ chính xác về `groupId`, `artifactId`, `version` của descriptor cha, đảm bảo không có sai lệch về namespace. Rà soát `<dependencyManagement>` để đảm bảo không khai báo trùng lặp phiên bản Spring Boot, Spring Cloud, Flyway, PostgreSQL Driver. Kiểm tra plugin `spring-boot-maven-plugin` được khai báo đầy đủ tại tất cả descriptor con với `<mainClass>` trỏ chính xác về lớp ứng dụng tương ứng. Xác nhận plugin `flyway-maven-plugin` được cấu hình đồng nhất tại tất cả module có di trú schema. Phát hiện xung đột version giữa các module và đề xuất chiến lược khắc phục bằng cách khoá version trong `<properties>` của descriptor cha. Đối chiếu tất cả mã định danh truy vết `[ARC-000]` xuất hiện đúng một lần tại mỗi descriptor. Tạo báo cáo review ghi nhận điểm đạt và điểm cần cải thiện.

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

#### 📝 TÁC VỤ CON 1.8: Soạn thảo tài liệu kiến trúc tổng quan Microservices
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/architecture/MicroservicesOverviewBlueprint.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[ARC-000]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải soạn thảo tài liệu Markdown `./sources/docs/architecture/MicroservicesOverviewBlueprint.md` mô tả sơ đồ kiến trúc Microservices gồm năm dịch vụ `user-service`, `schedule-service`, `ai-service`, `rate-limit-service`, `api-gateway`. Tài liệu phải bao gồm sơ đồ Mermaid miêu tả luồng giao tiếp giữa API Gateway và các dịch vụ nội bộ qua Kafka topic `social.scheduler.events`. Nêu rõ quy ước đặt tên package `org.nlh4j.socialscheduler.<service>` cho tất cả module Java. Mô tả cơ chế schema-per-tenant trong PostgreSQL với bốn schema `user_schema`, `schedule_schema`, `ai_schema`, `rate_limit_schema`. Bảng liệt kê ma trận trách nhiệm giữa các dịch vụ và mã định danh truy vết `[ARC-000]`. Đề cập rõ ràng công nghệ cốt lõi: Spring Boot 3.3.x, Spring Cloud 2023.x, Apache Kafka 3.7.x, Redis 7.x (Lettuce), PostgreSQL 16.x, Flyway 10.x. Tài liệu phải có mục lục, phần giới thiệu, sơ đồ kiến trúc, ma trận dịch vụ, và phần tham chiếu.

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

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Di trú schema cơ sở dữ liệu và tài liệu catalog<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 2.1: Sinh Flyway DDL cho bảng người dùng và schema-per-tenant
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh script Flyway `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql` thực thi di trú schema ban đầu cho bảng `users`. Đầu tiên, script phải tạo schema `user_schema` thông qua câu lệnh `CREATE SCHEMA IF NOT EXISTS user_schema`. Tiếp theo, tạo bảng `users` với các cột theo đúng thứ tự: `user_id UUID NOT NULL`, `tenant_id VARCHAR(64) NOT NULL`, `email VARCHAR(255) NOT NULL`, `password_hash VARCHAR(255) NOT NULL`, `role VARCHAR(32) NOT NULL`, `enabled BOOLEAN NOT NULL DEFAULT TRUE`, `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`, `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`. Khai báo khóa chính `pk_users` trên cột `user_id` thông qua ràng buộc `CONSTRAINT pk_users PRIMARY KEY (user_id)`. Khai báo khóa duy nhất `uk_users_tenant_email` trên cặp cột `(tenant_id, email)` để đảm bảo email là duy nhất trong phạm vi tenant. Khai báo ràng buộc kiểm tra `ck_users_role` với tập giá trị cho phép `('ADMIN', 'USER', 'SCHEDULER', 'ANALYST')` thông qua cú pháp `CHECK (role IN (...))`. Tạo chỉ mục phụ trợ `idx_users_tenant` trên cột `tenant_id` để tối ưu truy vấn đa-tenant. Đảm bảo bảng được tạo trong schema `user_schema` thông qua tiền tố schema trước tên bảng. Tuân thủ nguyên tắc OWASP A03 (Injection) bằng cách sử dụng kiểu dữ liệu UUID cho khóa chính thay vì SERIAL.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
CREATE TABLE users (
    user_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER', 'SCHEDULER', 'ANALYST'))
);

CREATE INDEX idx_users_tenant ON user_schema.users(tenant_id);
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

#### 📝 TÁC VỤ CON 2.2: Sinh Flyway DDL cho bảng lịch đăng bài và khóa ngoại
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh script Flyway `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql` thực thi di trú schema ban đầu cho bảng `schedules`. Đầu tiên, script phải tạo schema `schedule_schema` thông qua câu lệnh `CREATE SCHEMA IF NOT EXISTS schedule_schema`. Tiếp theo, tạo bảng `schedules` với các cột theo đúng thứ tự: `schedule_id UUID NOT NULL`, `user_id UUID NOT NULL`, `tenant_id VARCHAR(64) NOT NULL`, `platform VARCHAR(32) NOT NULL`, `content TEXT NOT NULL`, `scheduled_time TIMESTAMP NOT NULL`, `status VARCHAR(16) NOT NULL`, `actual_sent_time TIMESTAMP`, `retry_count INTEGER NOT NULL DEFAULT 0`, `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`, `updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`. Khai báo khóa chính phức hợp `pk_schedules` trên bộ bốn cột `(schedule_id, user_id, platform, scheduled_time)`. Khai báo khóa ngoại `fk_schedules_user` tham chiếu cột `user_id` của schema `user_schema.users` thông qua cú pháp `FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id)`. Khai báo ràng buộc kiểm tra `ck_schedules_platform` với tập giá trị cho phép `('FACEBOOK', 'INSTAGRAM', 'TIKTOK')`. Khai báo ràng buộc kiểm tra `ck_schedules_status` với tập giá trị cho phép `('PENDING', 'SENT', 'FAILED', 'CANCELLED')`. Tạo chỉ mục phụ trợ `idx_schedules_user_status` trên cặp cột `(user_id, status)` và chỉ mục `idx_schedules_tenant_time` trên cặp cột `(tenant_id, scheduled_time)` để tối ưu hiệu năng truy vấn lịch đăng bài theo tenant và theo khoảng thời gian. Tuân thủ nguyên tắc schema-per-tenant và OWASP A01 (Broken Access Control) thông qua cột `tenant_id` trong mọi câu truy vấn.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
CREATE TABLE schedules (
    schedule_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,
    actual_sent_time TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_schedules PRIMARY KEY (schedule_id, user_id, platform, scheduled_time),
    CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id),
    CONSTRAINT ck_schedules_platform CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'TIKTOK')),
    CONSTRAINT ck_schedules_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_schedules_user_status ON schedule_schema.schedules(user_id, status);
CREATE INDEX idx_schedules_tenant_time ON schedule_schema.schedules(tenant_id, scheduled_time);
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

#### 📝 TÁC VỤ CON 2.3: Sinh Flyway DDL cho bảng hiệu suất bài đăng
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-002], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh script Flyway `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql` thực thi di trú schema ban đầu cho bảng `performance_metrics`. Đầu tiên, script phải tạo schema `ai_schema` thông qua câu lệnh `CREATE SCHEMA IF NOT EXISTS ai_schema`. Tiếp theo, tạo bảng `performance_metrics` với các cột theo đúng thứ tự: `performance_id UUID NOT NULL`, `post_id UUID NOT NULL`, `tenant_id VARCHAR(64) NOT NULL`, `likes INTEGER NOT NULL DEFAULT 0`, `comments INTEGER NOT NULL DEFAULT 0`, `shares INTEGER NOT NULL DEFAULT 0`, `collected_at TIMESTAMP NOT NULL`. Khai báo khóa chính phức hợp `pk_performance` trên bộ ba cột `(performance_id, post_id, collected_at)`. Khai báo khóa ngoại `fk_performance_schedule` tham chiếu cột `schedule_id` của schema `schedule_schema.schedules` thông qua cú pháp `FOREIGN KEY (post_id) REFERENCES schedule_schema.schedules(schedule_id)`. Khai báo ba ràng buộc kiểm tra `ck_performance_likes`, `ck_performance_comments`, `ck_performance_shares` đảm bảo giá trị số nguyên không âm `>= 0`. Tạo chỉ mục phụ trợ `idx_performance_post` trên cột `post_id` để tối ưu truy vấn theo bài đăng. Tuân thủ nguyên tắc OWASP A03 (Injection) bằng cách sử dụng kiểu UUID cho mọi khóa chính và khóa ngoại.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
CREATE TABLE performance_metrics (
    performance_id UUID NOT NULL,
    post_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0,
    comments INTEGER NOT NULL DEFAULT 0,
    shares INTEGER NOT NULL DEFAULT 0,
    collected_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_performance PRIMARY KEY (performance_id, post_id, collected_at),
    CONSTRAINT fk_performance_schedule FOREIGN KEY (post_id) REFERENCES schedule_schema.schedules(schedule_id),
    CONSTRAINT ck_performance_likes CHECK (likes >= 0),
    CONSTRAINT ck_performance_comments CHECK (comments >= 0),
    CONSTRAINT ck_performance_shares CHECK (shares >= 0)
);

CREATE INDEX idx_performance_post ON ai_schema.performance_metrics(post_id);
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

#### 📝 TÁC VỤ CON 2.4: Sinh Flyway DDL cho bảng giới hạn tỷ lệ
##### Sub-Agent được phân công: Coder
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-003], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư phải sinh script Flyway `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql` thực thi di trú schema ban đầu cho bảng `rate_limits`. Đầu tiên, script phải tạo schema `rate_limit_schema` thông qua câu lệnh `CREATE SCHEMA IF NOT EXISTS rate_limit_schema`. Tiếp theo, tạo bảng `rate_limits` với các cột theo đúng thứ tự: `rate_limit_id UUID NOT NULL`, `user_id UUID NOT NULL`, `tenant_id VARCHAR(64) NOT NULL`, `endpoint VARCHAR(255) NOT NULL`, `request_count INTEGER NOT NULL`, `window_start TIMESTAMP NOT NULL`, `window_end TIMESTAMP NOT NULL`. Khai báo khóa chính phức hợp `pk_rate_limits` trên bộ ba cột `(rate_limit_id, endpoint, window_start)`. Khai báo khóa ngoại `fk_rate_limits_user` tham chiếu cột `user_id` của schema `user_schema.users` thông qua cú pháp `FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id)`. Khai báo ràng buộc kiểm tra `ck_rate_limits_endpoint` với tập giá trị cho phép `('/api/v1/schedules', '/api/v1/recommendations', '/api/v1/rate-limits', '/api/v1/users')` để ngăn chặn ghi log endpoint ngoài whitelist. Khai báo ràng buộc kiểm tra `ck_rate_limits_count` đảm bảo `request_count >= 0`. Tạo chỉ mục phụ trợ `idx_rate_limits_window` trên bộ ba cột `(user_id, endpoint, window_start)` để tối ưu truy vấn cửa sổ giới hạn tỷ lệ.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
CREATE TABLE rate_limits (
    rate_limit_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    CONSTRAINT pk_rate_limits PRIMARY KEY (rate_limit_id, endpoint, window_start),
    CONSTRAINT fk_rate_limits_user FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id),
    CONSTRAINT ck_rate_limits_endpoint CHECK (endpoint IN ('/api/v1/schedules', '/api/v1/recommendations', '/api/v1/rate-limits', '/api/v1/users')),
    CONSTRAINT ck_rate_limits_count CHECK (request_count >= 0)
);

CREATE INDEX idx_rate_limits_window ON rate_limit_schema.rate_limits(user_id, endpoint, window_start);
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

#### 📝 TÁC VỤ CON 2.5: Sinh bộ test tích hợp Flyway cho toàn bộ lược đồ
##### Sub-Agent được phân công: Tester
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `INTEGRATION_SCOPE;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserSchemaMigrationIT.java`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia QA phải sinh lớp kiểm thử tích hợp `./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserSchemaMigrationIT.java` sử dụng Testcontainers PostgreSQL phiên bản 16-alpine. Lớp kiểm thử phải được đánh dấu annotation `@SpringBootTest` và `@Testcontainers` để Spring tự động quản lý vòng đời container. Khai báo container PostgreSQL với phiên bản `postgres:16-alpine` và cấu hình container với biến môi trường `POSTGRES_DB=socialscheduler_test`, `POSTGRES_USER=test`, `POSTGRES_PASSWORD=test`. Lớp kiểm thử phải trỏ Flyway vào script `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql` thông qua annotation `@Sql` hoặc cấu hình datasource động. Xác minh các bảng `users` được tạo với đầy đủ cột thông qua câu truy vấn `SELECT column_name FROM information_schema.columns WHERE table_schema = 'user_schema' AND table_name = 'users'`. Xác minh ràng buộc khóa chính tồn tại thông qua truy vấn `information_schema.table_constraints`. Xác minh ràng buộc khóa duy nhất `uk_users_tenant_email`. Xác minh ràng buộc kiểm tra `ck_users_role` hoạt động đúng bằng cách thử chèn giá trị `role = 'INVALID_ROLE'` và khẳng định ngoại lệ `DataIntegrityViolationException` được ném ra. Đảm bảo kiểm thử thất bại khi chèn giá trị `role` không thuộc tập cho phép.

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

#### 📝 TÁC VỤ CON 2.6: Rà soát tính toàn vẹn tham chiếu giữa các lược đồ dịch vụ
##### Sub-Agent được phân công: Reviewer
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia đánh giá phải rà soát toàn bộ bốn tệp Flyway DDL đã được sinh ra tại các tác vụ con 2.1 đến 2.4 nhằm đảm bảo tính toàn vẹn tham chiếu giữa các schema. Xác minh ràng buộc khóa ngoại `fk_schedules_user` tham chiếu chính xác cột `user_id` của schema `user_schema.users`. Xác minh ràng buộc khóa ngoại `fk_performance_schedule` tham chiếu chính xác cột `schedule_id` của schema `schedule_schema.schedules`. Xác minh ràng buộc khóa ngoại `fk_rate_limits_user` tham chiếu chính xác cột `user_id` của schema `user_schema.users`. Xác minh mọi cột `tenant_id` đều có chỉ mục phụ trợ nhằm đảm bảo hiệu năng truy vấn đa tenant. Đánh giá chiến lược schema-per-tenant và đề xuất bổ sung cột `tenant_id` vào khóa chính phức hợp của bảng `schedules` và `performance_metrics` nếu cần thiết. Kiểm tra tính nhất quán của tập giá trị enum trong các ràng buộc CHECK giữa schema và logic nghiệp vụ dự kiến. Rà soát việc sử dụng kiểu UUID cho khóa chính nhằm đảm bảo chống enumeration attack theo OWASP A07. Lập báo cáo review ghi nhận các phát hiện và đề xuất cải tiến.

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

#### 📝 TÁC VỤ CON 2.7: Soạn thảo catalog tài liệu lược đồ cơ sở dữ liệu
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/architecture/DatabaseSchemaCatalog.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DAT-001], [DAT-002], [DAT-003], [DAT-ALL (1 to 3)]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải soạn thảo tài liệu Markdown `./sources/docs/architecture/DatabaseSchemaCatalog.md` mô tả chi tiết bốn bảng dữ liệu cốt lõi `users`, `schedules`, `performance_metrics`, `rate_limits`. Tài liệu phải chứa bốn bảng Markdown liệt kê cột, kiểu dữ liệu, ràng buộc khóa chính, khóa ngoại, ràng buộc kiểm tra và chỉ mục cho từng bảng. Kèm theo sơ đồ Mermaid ER miêu tả quan hệ giữa `users`, `schedules`, `performance_metrics` và `rate_limits` thông qua các khóa ngoại đã khai báo. Nêu rõ chiến lược schema-per-tenant: bốn schema `user_schema`, `schedule_schema`, `ai_schema`, `rate_limit_schema` được cô lập theo bounded context. Mô tả quy trình thực thi di trú Flyway: phiên bản `V1__init_<table_name>.sql` được áp dụng tự động khi khởi động dịch vụ tương ứng. Tài liệu phải có mục lục, phần giới thiệu, bốn bảng mô tả chi tiết, sơ đồ ER, phần chiến lược schema-per-tenant và phần tham chiếu mã định danh truy vết `[DAT-001]`, `[DAT-002]`, `[DAT-003]`, `[DAT-ALL (1 to 3)]`.

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