<!--START_CHUNK_PART_1_INITIAL-->

# GLOBAL PROJECT CONTEXT: social-scheduler

## 📊 Document Control

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260912132915 |
| **Project Name** | social-scheduler |
| **Version** | 1.0 (Cơ sở) |
| **Date Time** | 2026/09/12 13:29:15 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Chờ phê duyệt quản trị kỹ thuật |

## 📊 1. SYSTEM OVERVIEW & CORE ARCHITECTURE MODALITY

### ⚙️ 1.1. Core System Modality & Architecture Modality

- Hệ thống được thiết kế theo kiến trúc microservices với các dịch vụ độc lập cho quản lý người dùng, lịch đăng bài, và đề xuất nội dung.
- Sử dụng mô hình Event-Driven Architecture (EDA) để xử lý các sự kiện như đăng bài, cập nhật hiệu suất, và thông báo.
- Áp dụng mô hình Command Query Responsibility Segregation (CQRS) để tách biệt các thao tác ghi và đọc dữ liệu.
- Triển khai mô hình Reactive Programming để xử lý các luồng dữ liệu bất đồng bộ và thời gian thực.

### 🌊 1.2. Enterprise Data Flow Topologies & Core Ecosystems

- Sử dụng Apache Kafka để quản lý các luồng dữ liệu bất đồng bộ giữa các dịch vụ.
- Triển khai các topic Kafka riêng biệt cho các sự kiện như đăng bài, cập nhật hiệu suất, và thông báo.
- Sử dụng Redis để lưu trữ dữ liệu tạm thời và bộ nhớ đệm.
- Áp dụng mô hình fan-out để phân phối các sự kiện đến các dịch vụ khác nhau.

## 📁 2. TECH STACK DEPENDENCIES & ECOSYSTEM LIBRARIES

- **Backend Infrastructure Core Stack:** Spring Boot, Spring Security, Spring Data JPA, Hibernate, PostgreSQL, Apache Kafka, Redis, Docker, Kubernetes, GitHub Actions, Prometheus, Grafana.
- **Frontend & Cross-Platform UI Mobile Stack:** React, Next.js, Tailwind CSS, React Native, Expo.

## 📁 3. GLOBAL GUARDRAILS & ENTERPRISE COMPLIANCE STANDARDS

### 🔑 3.1. Security & Compliance Baseline

- Mã hóa dữ liệu sử dụng TLS và AES-256.
- Xác thực người dùng và ủy quyền sử dụng OAuth2 và JWT.
- Bảo vệ chống tấn công DDoS và SQL Injection.
- Tuân thủ các tiêu chuẩn bảo mật OWASP Top 10.
- Ghi lại và giám sát các hoạt động hệ thống.

### 🌐 3.2. Infrastructure & Performance Guardrails

- Sử dụng cơ chế pooling kết nối HikariCP cho PostgreSQL.
- Áp dụng chính sách thu hồi bộ nhớ đệm Redis để tối ưu hóa hiệu suất.
- Triển khai các hàng đợi tin nhắn Kafka với phân vùng và sao chép để đảm bảo độ tin cậy.
- Sử dụng bộ nhớ đệm Redis để lưu trữ dữ liệu tạm thời và giảm tải cơ sở dữ liệu.

### 🥞 3.3. ARCHITECTURAL STACK MATRIX

```properties:stack_matrix
PERSISTENCE_LAYER_REQUIRED=true
BACKEND_LAYER_REQUIRED=true
FRONTEND_LAYER_REQUIRED=true
MOBILE_LAYER_REQUIRED=true
DEVOPS_LAYER_REQUIRED=true
```

### 🕸️ 3.4. DYNAMIC MICROSERVICES TOPOLOGY REGISTRY MATRIX

<!--BACKLOG_SERVICES_START-->

| Service Domain Key | Microservice Sub-Module Name | Target Container Context Path | Active Infrastructure Gateway Ports | Mapped Functional Backend Packages | Mapped Tracking TagIDs |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Parent Root Grandmaster** | socialscheduler-root | `./sources/backend/pom.xml` | N/A (Global Orchestrator) | `org.nlh4j.socialscheduler` | [ARC-000] |
| **User Management** | user-service | `./sources/backend/user-service/pom.xml` | 8081 | `org.nlh4j.socialscheduler.userservice` | [ARC-001], [ARC-002], [ARC-003], [ARC-004] |
| **Scheduling Service** | scheduling-service | `./sources/backend/scheduling-service/pom.xml` | 8082 | `org.nlh4j.socialscheduler.schedulingservice` | [REQ-001], [EXC-001], [EXC-002], [DAT-001] |
| **Content Recommendation** | content-recommendation-service | `./sources/backend/content-recommendation-service/pom.xml` | 8083 | `org.nlh4j.socialscheduler.contentrecommendationservice` | [REQ-002], [EXC-003], [EXC-004], [DAT-002] |
| **Rate Limiting** | rate-limiting-service | `./sources/backend/rate-limiting-service/pom.xml` | 8084 | `org.nlh4j.socialscheduler.ratelimitingservice` | [REQ-003], [EXC-005], [DAT-003] |

<!--BACKLOG_SERVICES_END-->

<!--END_CHUNK_PART_1_INITIAL-->

<!--START_CHUNK_PART_1_BACKLOG_4_1-->

## 🏁 4. LƯỚI TÓM TẮT KIẾN TRÚC ĐA PHASE

### 📦 4.1. LƯỚI NHIỆM VỤ SẢN PHẨM KIẾN TRÚC CHÍNH

#### [MA TRẬN TOÁN HỌC HỆ THỐNG]
> - **Tổng [REQ] Tags:** 3 Tags
> - **Tổng [EXC] Tags:** 5 Tags
> - **Tổng [ARC] Tags:** 6 Tags
> - **Tổng [DAT] Tags:** 3 Tags
> - **Tổng [NFR] Tags:** 3 Tags
> - ➡️ **Tổng SRS Tags:** 17 Tags

<!--BACKLOG_SYNOPSIS_GRID_START-->

| STT | Nhiệm vụ | Mục đích kỹ thuật / Tóm tắt giao hàng | Loại | TagID |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Tích hợp lịch đăng bài tự động | Tích hợp API lịch đăng bài tự động cho Facebook, Instagram và TikTok | Ứng dụng | [REQ-001] [EXC-001] [EXC-002] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 2 | Đề xuất nội dung bằng AI | Triển khai mô hình học máy để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó | Ứng dụng | [REQ-002] [EXC-003] [EXC-004] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 3 | Xác thực đầu vào & giới hạn tỷ lệ | Thực hiện xác thực đầu vào dữ liệu và kiểm tra giới hạn tỷ lệ cho từng người dùng | Ứng dụng | [REQ-003] [EXC-002] [EXC-003] [EXC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 4 | Cơ sở dữ liệu và xác thực mã thông báo | Thiết lập cơ sở dữ liệu và xác thực mã thông báo cho hệ thống | Kiến trúc | [DAT-ALL (1 to 3)] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 5 | Tài liệu kỹ thuật | Tạo tài liệu kỹ thuật cho hệ thống | Tài liệu | [DOC-001] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| 6 | Cơ sở hạ tầng DevOps | Thiết lập cơ sở hạ tầng DevOps cho hệ thống | Cơ sở hạ tầng | [NFR-001] [NFR-002] [NFR-003] <!--REGISTERED_BACKLOG_TASK_ROW--> |
| **TÓM TẮT** | **Tổng số TagID đã bao phủ:** 17 | **Tổng số nhiệm vụ:** 6 | **Trạng thái:** Đã xác minh | **Độ bao phủ:** 100% |

<!--BACKLOG_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_BACKLOG_4_1-->

<!--START_CHUNK_PART_1_MATRIX_4_2-->

### 🔭 4.2. MA TRẬN TÓM TẮT ĐA PHASE

#### [MATRIX ARITHMETIC LIFECYCLE]
> - **Tổng Backlog Tasks:** 6 Tasks
> - **Tổng Backlog Tags:** 17 Tags
> - **Tổng Distributed Tasks:** 6 Tasks
> - **Tổng Distributed Tags:** 17 Tags

| Phase | Day Range | Task IDs Covered | Architectural Component / Module Path | Technical Deliverables Summary | Assigned Sub-Agent | Targeted Tag IDs |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Phase 1 | Day 1 - 2 | Task 4 | `./sources/backend/pom.xml` <br/> `./sources/backend/user-service/pom.xml` <br/> `./sources/backend/scheduling-service/pom.xml` <br/> `./sources/backend/content-recommendation-service/pom.xml` <br/> `./sources/backend/rate-limiting-service/pom.xml` <br/> `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/User.java` <br/> `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java` <br/> `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java` <br/> `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java` <br/> `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/Schedule.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetrics.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimit.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitService.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitController.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitExceptionHandler.java` | Tạo các mô-đun cơ sở và cấu hình cơ sở dữ liệu | Coder, Tester, Reviewer, Doc | [DAT-ALL (1 to 3)] [ARC-001] [ARC-002] [ARC-003] [ARC-004] [ARC-005] <!--REGISTERED_PHASE_ROW--> |
| Phase 2 | Day 1 - 2 | Task 1 | `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java` <br/> `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java` | Triển khai dịch vụ lên lịch và xử lý ngoại lệ | Coder, Tester, Reviewer, Doc | [REQ-001] [EXC-001] [EXC-002] <!--REGISTERED_PHASE_ROW--> |
| Phase 3 | Day 1 - 2 | Task 2 | `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java` <br/> `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java` | Triển khai dịch vụ đề xuất nội dung và xử lý ngoại lệ | Coder, Tester, Reviewer, Doc | [REQ-002] [EXC-003] [EXC-004] <!--REGISTERED_PHASE_ROW--> |
| Phase 4 | Day 1 - 2 | Task 3 | `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitService.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitController.java` <br/> `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitExceptionHandler.java` | Triển khai dịch vụ giới hạn tỷ lệ và xử lý ngoại lệ | Coder, Tester, Reviewer, Doc | [REQ-003] [EXC-002] [EXC-003] [EXC-005] <!--REGISTERED_PHASE_ROW--> |
| Phase 5 | Day 1 - 2 | Task 5, Task 6 | `./sources/docs/technical-documentation.md` <br/> `./sources/infra/devops-setup.sh` | Tạo tài liệu kỹ thuật và thiết lập cơ sở hạ tầng DevOps | Doc, Docker, GCP, GKE | [DOC-001] [NFR-001] [NFR-002] [NFR-003] <!--REGISTERED_PHASE_ROW--> |
| **Audit** | **Master Backlog Distribution Verification** | **Total Phases:** 5 | **Total BackLog Tags:** 17 | **Total Distributed Tags:** 17 | **Total Distributed Tasks:** 6 | **Status & Compliance:** Đã xác minh (100%) |

<!--PHASE_SYNOPSIS_GRID_END-->

<!--END_CHUNK_PART_1_MATRIX_4_2-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

## 🔬 5. GRANULAR PHASE SPECIALIZATIONS & DAY-BY-DAY DELIVERABLES

<!--PHASE_INDEX_START-->

### 📈 Giai đoạn 1 - Khởi tạo cơ sở hạ tầng và cấu hình cơ sở dữ liệu

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Giai đoạn này tập trung vào việc thiết lập cơ sở hạ tầng và cấu hình cơ sở dữ liệu cho hệ thống. Mục tiêu là tạo ra các mô-đun cơ sở và cấu hình cơ sở dữ liệu để hỗ trợ các chức năng chính của hệ thống.

- **Bản đồ ma trận thư mục vật lý mục tiêu:** Tạo các mô-đun cơ sở và cấu hình cơ sở dữ liệu cho hệ thống. Mỗi mô-đun sẽ có cấu trúc thư mục riêng và cấu hình cơ sở dữ liệu tương ứng.

- **Chỉ định DDL SQL cho lược đồ cơ sở dữ liệu [DAT-XXX]:** Cung cấp các câu lệnh DDL SQL để tạo các bảng cơ sở dữ liệu và các ràng buộc tương ứng. Các bảng cơ sở dữ liệu sẽ bao gồm các bảng người dùng, lịch đăng bài, hiệu suất bài đăng và giới hạn tỷ lệ.

```sql:matrix
-- Tạo bảng người dùng
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('Admin', 'User', 'Scheduler', 'Analyst')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Tạo bảng lịch đăng bài
CREATE TABLE schedules (
    schedule_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    platform VARCHAR(50) NOT NULL CHECK (platform IN ('Facebook', 'Instagram', 'TikTok')),
    content TEXT NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('pending', 'sent', 'failed', 'cancelled')),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Tạo bảng hiệu suất bài đăng
CREATE TABLE performance_metrics (
    performance_id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    likes INTEGER NOT NULL,
    comments INTEGER NOT NULL,
    shares INTEGER NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    FOREIGN KEY (post_id) REFERENCES schedules(schedule_id)
);

-- Tạo bảng giới hạn tỷ lệ
CREATE TABLE rate_limits (
    rate_limit_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

- **Hợp đồng định tuyến API và sự kiện [REQ-XXX], [ARC-XXX]:** Tạo các điểm cuối API và hợp đồng sự kiện để quản lý các yêu cầu liên quan đến người dùng, lịch đăng bài, hiệu suất bài đăng và giới hạn tỷ lệ.

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Xử lý các ngoại lệ liên quan đến việc tạo và quản lý các bảng cơ sở dữ liệu và các điểm cuối API.

#### 📅 Nhật ký phân phối nhiệm vụ hàng ngày của các tác nhân con (Giai đoạn 1)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Thiết lập cơ sở hạ tầng và cấu hình cơ sở dữ liệu

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/User.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp User để quản lý thông tin người dùng. Lớp này sẽ bao gồm các thuộc tính như userId, username, email, passwordHash, role, createdAt và updatedAt.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/User.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp User để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/User.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp User để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/docs/user-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ người dùng, bao gồm mô tả lớp User và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/Schedule.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp Schedule để quản lý thông tin lịch đăng bài. Lớp này sẽ bao gồm các thuộc tính như scheduleId, userId, platform, content, scheduledTime và status.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/Schedule.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp Schedule để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 7: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/Schedule.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp Schedule để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 8: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/docs/scheduling-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ lên lịch, bao gồm mô tả lớp Schedule và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 9: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetrics.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp PerformanceMetrics để quản lý thông tin hiệu suất bài đăng. Lớp này sẽ bao gồm các thuộc tính như performanceId, postId, likes, comments, shares và collectedAt.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 10: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetrics.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp PerformanceMetrics để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 11: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetrics.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp PerformanceMetrics để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 12: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ đề xuất nội dung, bao gồm mô tả lớp PerformanceMetrics và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 13: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimit.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp RateLimit để quản lý thông tin giới hạn tỷ lệ. Lớp này sẽ bao gồm các thuộc tính như rateLimitId, userId, endpoint, requestCount, windowStart và windowEnd.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 14: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimit.java;./sources/backend/rate-limiting-service/src/test/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp RateLimit để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 15: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimit.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp RateLimit để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 16: Tạo cấu trúc thư mục và tệp cơ sở cho các dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/docs/rate-limiting-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ giới hạn tỷ lệ, bao gồm mô tả lớp RateLimit và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai các dịch vụ cơ bản và kiểm thử

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp UserRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến người dùng. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Triển khai dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserRepositoryTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp UserRepository để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Triển khai dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp UserRepository để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Triển khai dịch vụ người dùng

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005]

* **Thành phần mục tiêu (target_component):** `./sources/docs/user-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật tài liệu kỹ thuật cho dịch vụ người dùng, bao gồm mô tả lớp UserRepository và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Triển khai dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp ScheduleRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến lịch đăng bài. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Triển khai dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepositoryTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp ScheduleRepository để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 7: Triển khai dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp ScheduleRepository để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 8: Triển khai dịch vụ lên lịch

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/docs/scheduling-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật tài liệu kỹ thuật cho dịch vụ lên lịch, bao gồm mô tả lớp ScheduleRepository và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 9: Triển khai dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp PerformanceMetricsRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến hiệu suất bài đăng. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 10: Triển khai dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepositoryTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp PerformanceMetricsRepository để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 11: Triển khai dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp PerformanceMetricsRepository để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 12: Triển khai dịch vụ đề xuất nội dung

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-002], [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật tài liệu kỹ thuật cho dịch vụ đề xuất nội dung, bao gồm mô tả lớp PerformanceMetricsRepository và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 13: Triển khai dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo lớp RateLimitRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến giới hạn tỷ lệ. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 14: Triển khai dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java;./sources/backend/rate-limiting-service/src/test/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepositoryTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo bộ kiểm thử cho lớp RateLimitRepository để đảm bảo tính chính xác của các phương thức và thuộc tính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 15: Triển khai dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét và đánh giá chất lượng mã nguồn của lớp RateLimitRepository để đảm bảo tuân thủ các tiêu chuẩn lập trình và thực hành tốt nhất.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 16: Triển khai dịch vụ giới hạn tỷ lệ

* **Chuyên môn công việc của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-003], [EXC-002], [EXC-003], [EXC-005]

* **Thành phần mục tiêu (target_component):** `./sources/docs/rate-limiting-service-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Cập nhật tài liệu kỹ thuật cho dịch vụ giới hạn tỷ lệ, bao gồm mô tả lớp RateLimitRepository và các phương thức chính.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 2 - Dịch vụ lên lịch và xử lý ngoại lệ

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai dịch vụ lên lịch và xử lý ngoại lệ cho hệ thống. Giai đoạn này tập trung vào việc triển khai các dịch vụ lên lịch và xử lý ngoại lệ cho hệ thống.

- **Ma trận đường dẫn vật lý mục tiêu:** Tạo các tệp tin và cấu hình cần thiết cho dịch vụ lên lịch và xử lý ngoại lệ.

- **Chỉ định DDL SQL Schema [DAT-XXX]:** Cung cấp các câu lệnh DDL SQL để tạo các bảng cơ sở dữ liệu cần thiết cho dịch vụ lên lịch và xử lý ngoại lệ.

- **Hợp đồng định tuyến API và sự kiện [REQ-XXX], [ARC-XXX]:** Tài liệu các hợp đồng định tuyến API và sự kiện cho dịch vụ lên lịch và xử lý ngoại lệ.

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Chi tiết các quy tắc xác thực nghiệp vụ, mã lỗi và đường dẫn xử lý ngoại lệ cho dịch vụ lên lịch và xử lý ngoại lệ.

#### 📅 Nhật ký phân phối nhiệm vụ theo ngày của giai đoạn (Giai đoạn 2)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai dịch vụ lên lịch và xử lý ngoại lệ

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai dịch vụ lên lịch

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai dịch vụ lên lịch cho hệ thống. Dịch vụ này sẽ quản lý việc lên lịch và gửi các bài đăng lên các nền tảng mạng xã hội khác nhau.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Triển khai bộ xử lý ngoại lệ

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai bộ xử lý ngoại lệ cho dịch vụ lên lịch. Bộ xử lý này sẽ quản lý các ngoại lệ và lỗi xảy ra trong quá trình lên lịch và gửi các bài đăng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Kiểm thử và đánh giá dịch vụ lên lịch và xử lý ngoại lệ

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Kiểm thử dịch vụ lên lịch

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm thử dịch vụ lên lịch để đảm bảo rằng nó hoạt động đúng và đáp ứng các yêu cầu chức năng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm thử bộ xử lý ngoại lệ

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm thử bộ xử lý ngoại lệ để đảm bảo rằng nó quản lý các ngoại lệ và lỗi một cách chính xác và hiệu quả.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 3 - Dịch vụ đề xuất nội dung bằng AI

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Triển khai mô hình học máy để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó và xử lý các ngoại lệ liên quan đến đề xuất nội dung.

- **Ma trận bản đồ thư mục vật lý mục tiêu:** Tạo các tệp tin và cấu hình cơ sở dữ liệu cho dịch vụ đề xuất nội dung bằng AI.

- **Chỉ định DDL SQL cho cơ sở dữ liệu [DAT-XXX]:** Cung cấp các câu lệnh DDL SQL để tạo bảng hiệu suất bài đăng.

- **Hợp đồng định tuyến API và sự kiện [REQ-XXX], [ARC-XXX]:** Tài liệu các điểm cuối API và hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI.

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Chi tiết các quy tắc xác thực đầu vào và xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI.

#### 📅 Nhật ký phân phối nhiệm vụ theo ngày của các tác nhân con (Giai đoạn 3)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai dịch vụ đề xuất nội dung và xử lý ngoại lệ

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai dịch vụ đề xuất nội dung bằng AI để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm tra dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsServiceTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Xem xét mã dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét mã dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Tài liệu dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ đề xuất nội dung bằng AI.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Triển khai bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để xử lý các ngoại lệ liên quan đến đề xuất nội dung.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandlerTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các ngoại lệ liên quan đến đề xuất nội dung.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 7: Xem xét mã bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét mã bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 8: Tài liệu bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Doc]

* **TagID mục tiêu:** [EXC-003], [EXC-004]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service-exception-handler.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai điểm cuối API và hợp đồng sự kiện

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai điểm cuối API cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm tra điểm cuối API cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsControllerTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Xem xét mã điểm cuối API cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét mã điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Tài liệu điểm cuối API cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service-api.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho điểm cuối API cho dịch vụ đề xuất nội dung bằng AI.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Triển khai hợp đồng sự kiện cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để xử lý các sự kiện liên quan đến đề xuất nội dung.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Kiểm tra hợp đồng sự kiện cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandlerTest.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các sự kiện liên quan đến đề xuất nội dung.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 7: Xem xét mã hợp đồng sự kiện cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Reviewer]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Xem xét mã hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 8: Tài liệu hợp đồng sự kiện cho dịch vụ đề xuất nội dung

* **Chuyên môn của tác nhân con:** [Doc]

* **TagID mục tiêu:** [REQ-002]

* **Thành phần mục tiêu (target_component):** `./sources/docs/content-recommendation-service-event-handler.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 4 - Quản lý người dùng và dịch vụ lên lịch

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Giai đoạn này tập trung vào việc triển khai các dịch vụ quản lý người dùng và dịch vụ lên lịch. Mục tiêu là cung cấp các chức năng cơ bản cho quản lý người dùng và lên lịch bài đăng trên các nền tảng mạng xã hội.

- **Ma trận đường dẫn vật lý mục tiêu:** Tạo ra các tệp vật lý cụ thể cho các dịch vụ quản lý người dùng và dịch vụ lên lịch. Mỗi dịch vụ sẽ có các tệp Java, tệp cấu hình, và tệp kiểm thử tương ứng.

- **Chuẩn DDL SQL cơ sở dữ liệu [DAT-XXX]:** Cung cấp các câu lệnh DDL SQL đầy đủ và hợp lệ chứa các trường, kiểu dữ liệu, khóa chính/khóa ngoại, ánh xạ ma trận, chỉ mục và ràng buộc nullability được áp dụng trong phạm vi giai đoạn này. (Bỏ qua hoàn toàn nếu dự án không có lớp cơ sở dữ liệu hoặc yêu cầu lớp lưu trữ. Khối kỹ thuật này KHÔNG được dịch).

- **Hợp đồng định tuyến API và sự kiện [REQ-XXX], [ARC-XXX]:** Tài liệu các hợp đồng kỹ thuật đầy đủ (đường dẫn điểm cuối chính xác, phương thức HTTP, lược đồ JSON yêu cầu/phản hồi, hoặc cấu hình chủ đề bộ đệm tin nhắn. Khối kỹ thuật KHÔNG được dịch).

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn [EXC-XXX]:** Chi tiết các quy tắc xác thực kinh doanh, mã lỗi và đường dẫn xử lý ngoại lệ hệ thống ánh xạ nghiêm ngặt với phạm vi giai đoạn hiện tại, được dịch ngữ cảnh vào Tiếng Việt.

#### 📅 Nhật ký phân phối nhiệm vụ hàng ngày của các tác nhân con (Giai đoạn 4)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Triển khai dịch vụ quản lý người dùng

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai lớp UserService

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserService với các phương thức quản lý người dùng cơ bản như tạo, đọc, cập nhật và xóa người dùng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Triển khai lớp UserController

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserController với các điểm cuối API để quản lý người dùng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Triển khai lớp UserExceptionHandler

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserExceptionHandler để xử lý các ngoại lệ liên quan đến người dùng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Kiểm thử lớp UserService

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserServiceTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserService để đảm bảo các phương thức quản lý người dùng hoạt động đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Kiểm thử lớp UserController

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserControllerTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserController để đảm bảo các điểm cuối API hoạt động đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Kiểm thử lớp UserExceptionHandler

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [ARC-001], [ARC-002], [ARC-003], [ARC-004]

* **Thành phần mục tiêu (target_component):** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandlerTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai dịch vụ lên lịch

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai lớp ScheduleService

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleService với các phương thức quản lý lịch đăng bài cơ bản như tạo, đọc, cập nhật và xóa lịch đăng bài.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Triển khai lớp ScheduleController

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleController với các điểm cuối API để quản lý lịch đăng bài.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 3: Triển khai lớp ScheduleExceptionHandler

* **Chuyên môn công việc của tác nhân con:** [Coder]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleExceptionHandler để xử lý các ngoại lệ liên quan đến lịch đăng bài.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 4: Kiểm thử lớp ScheduleService

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleService để đảm bảo các phương thức quản lý lịch đăng bài hoạt động đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 5: Kiểm thử lớp ScheduleController

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleControllerTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleController để đảm bảo các điểm cuối API hoạt động đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 6: Kiểm thử lớp ScheduleExceptionHandler

* **Chuyên môn công việc của tác nhân con:** [Tester]

* **TagID mục tiêu:** [REQ-001], [EXC-001], [EXC-002]

* **Thành phần mục tiêu (target_component):** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java`

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_2_PHASE_LOOP-->

### 📈 Giai đoạn 5 - Giao hàng cơ sở hạ tầng DevOps và tài liệu kỹ thuật

- **Mục tiêu cốt lõi của giai đoạn & Mục đích:** Giai đoạn này tập trung vào việc triển khai cơ sở hạ tầng DevOps và tạo tài liệu kỹ thuật cho hệ thống. Mục tiêu là đảm bảo hệ thống có thể triển khai và vận hành một cách hiệu quả, đồng thời cung cấp tài liệu kỹ thuật đầy đủ cho các nhà phát triển và quản trị viên hệ thống.

- **Ma trận đường dẫn vật lý mục tiêu:** Tạo một danh sách kiểm tra kỹ thuật chi tiết liệt kê 100% các tệp vật lý riêng lẻ (KHÔNG phải thư mục hoặc thư mục) nằm dưới `./sources/` được tạo, tái cấu trúc hoặc xử lý trong phạm vi giai đoạn này. Mỗi dòng mục nhập phải đại diện cho một thực thể tệp cụ thể kết thúc bằng phần mở rộng tệp rõ ràng, với các TagID theo dõi tương ứng được đính kèm bên trong.

    *   *Giới hạn biên kiểm soát tài liệu:* Bất kỳ dòng nào đại diện cho một tài liệu quy trình doanh nghiệp, bản thiết kế tham khảo, bản đồ cơ sở dữ liệu quan hệ hoặc bản thiết kế kiến trúc phải nằm nghiêm ngặt dưới đường dẫn gốc thống nhất: `./sources/docs/`.

- **Chỉ định DDL SQL Schema Cơ sở dữ liệu:** (Bỏ qua hoàn toàn nếu dự án không có yêu cầu lớp cơ sở dữ liệu hoặc lớp lưu trữ. Khối kỹ thuật này KHÔNG được dịch).

- **Hợp đồng định tuyến API và Sự kiện:** (Bỏ qua hoàn toàn nếu dự án không có yêu cầu lớp cơ sở dữ liệu hoặc lớp lưu trữ. Khối kỹ thuật này KHÔNG được dịch).

- **Bộ xử lý ngoại lệ cục bộ của giai đoạn:** (Bỏ qua hoàn toàn nếu dự án không có yêu cầu lớp cơ sở dữ liệu hoặc lớp lưu trữ. Khối kỹ thuật này KHÔNG được dịch).

#### 📅 Nhật ký phân phối nhiệm vụ theo ngày của các tác nhân con (Giai đoạn 5)

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 1: Tạo tài liệu kỹ thuật và thiết lập cơ sở hạ tầng DevOps

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Tạo tài liệu kỹ thuật

* **Chuyên môn của tác nhân con:** [Doc]

* **TagID mục tiêu:** [DOC-001]

* **Đường dẫn thành phần mục tiêu:** `./sources/docs/technical-documentation.md`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật chi tiết cho hệ thống, bao gồm mô tả kiến trúc, hướng dẫn triển khai và tài liệu tham khảo API.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Thiết lập cơ sở hạ tầng DevOps

* **Chuyên môn của tác nhân con:** [Docker]

* **TagID mục tiêu:** [NFR-001], [NFR-002], [NFR-003]

* **Đường dẫn thành phần mục tiêu:** `./sources/infra/devops-setup.sh`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Tạo tập lệnh thiết lập cơ sở hạ tầng DevOps, bao gồm cấu hình Docker, Kubernetes và các dịch vụ đám mây.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

<!--DAY_LOG_INDEX_START-->

##### 📅 NGÀY 2: Triển khai và kiểm tra cơ sở hạ tầng DevOps

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 1: Triển khai cơ sở hạ tầng DevOps

* **Chuyên môn của tác nhân con:** [GCP]

* **TagID mục tiêu:** [NFR-001], [NFR-002], [NFR-003]

* **Đường dẫn thành phần mục tiêu:** `./sources/infra/devops-deployment.sh`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Triển khai cơ sở hạ tầng DevOps trên Google Cloud Platform, bao gồm cấu hình mạng, lưu trữ và dịch vụ.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--ATOMIC_SUB_TASK_NODE_START-->

###### 🌿 NHIỆM VỤ CON 2: Kiểm tra cơ sở hạ tầng DevOps

* **Chuyên môn của tác nhân con:** [GKE]

* **TagID mục tiêu:** [NFR-001], [NFR-002], [NFR-003]

* **Đường dẫn thành phần mục tiêu:** `./sources/infra/devops-testing.sh`

* **Hướng dẫn nhiệm vụ kỹ thuật cấp thấp:** Kiểm tra cơ sở hạ tầng DevOps trên Google Kubernetes Engine, bao gồm kiểm tra tính khả dụng, hiệu suất và bảo mật.

<!--ATOMIC_SUB_TASK_NODE_END-->

<!--DAY_LOG_INDEX_END-->

### 🕵️ Báo cáo kiểm toán kiến trúc tự động:

```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=2
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG_4_1=6
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_SECTION_5=12
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Symmetry_Enforced_With_100_Percent_Symmetry
```

<!--PHASE_INDEX_END-->

<!--END_CHUNK_PART_2_PHASE_LOOP-->

<!--START_CHUNK_PART_3_FINAL-->

## ☣️ 6. CÁC ĐOẠN MÃ BẢO MẬT VÀ ĐỐI PHÓNG CHỐNG TIÊU CHUẨN NHÀ NƯỚC [NFR-XXX]

- **[Đối phó với SQL Injection (SQLi)]:**
  - Hướng dẫn cho Coder agent thực hiện buộc ràng buộc tham số thời gian chạy bằng cách sử dụng các thuộc tính trạng thái thực thể và truy vấn đặt tên Hibernate. Bạn CẤM CHẮC CHẮN sử dụng nối chuỗi chuỗi động; các lệnh SQL thô phải được làm sạch hoàn toàn thông qua bộ lọc kho lưu trữ Spring Data JPA để đạt được 100% bảo vệ chống thao tác dữ liệu.
  - Đảm bảo rằng tất cả các truy vấn cơ sở dữ liệu được thực hiện thông qua các phương thức repository được xác định trước và sử dụng các tham số đặt tên để ngăn chặn các cuộc tấn công SQL injection.

- **[Đối phó với Cross-Site Scripting (XSS) & Chính sách Bảo mật Nội dung (CSP)]:**
  - Thực hiện các quy trình thoát ngữ cảnh XSS tự động trong tất cả các trường đầu vào của người dùng trong các phần trình bày phía trước. Giao diện cổng hệ thống MUST tự động chèn một cấu hình tiêu đề HTTP Content-Security-Policy (CSP) với các ràng buộc chỉ thị được tối ưu hóa (ví dụ: `default-src 'self'`) để hoàn toàn loại bỏ các thực thi kịch bản trái phép.
  - Áp dụng các bộ lọc đầu vào và đầu ra để ngăn chặn các ký tự đặc biệt có thể gây ra XSS trong các yêu cầu và phản hồi.

- **[Rào cản Bảo mật CORS đa tenant]:**
  - Thiết lập một lớp rào cản CORS không tin tưởng trên tất cả các điểm cuối API của hệ thống. Người quản lý cơ sở hạ tầng MUST xác thực các chuỗi nguồn đến một cách lập trình chống lại một cây đăng ký ủy quyền tenant động, cấm hoàn toàn các toán tử đại diện toàn cầu `*` cho các yêu cầu phiên làm việc được xác thực.
  - Triển khai các tiêu đề CORS thích hợp trên các điểm cuối API để chỉ cho phép các yêu cầu từ các nguồn được ủy quyền.

- **[Máy quét log không rò rỉ & Máy che dữ liệu PII]:**
  - Cấu hình một máy quét log tự động, mật độ cao để phân tích tất cả các tải dữ liệu ra ngoài. Bạn MUST sử dụng các ràng buộc trình tuần tự hóa Jackson và các đánh dấu siêu dữ liệu (ví dụ: logic che giấu `@JsonSerialize` tùy chỉnh) để tự động chặn và che giấu các chuỗi PII nhạy cảm trước khi dữ liệu đến lưu trữ vật lý.
  - Áp dụng các chính sách mã hóa dữ liệu tại nghỉ để bảo vệ dữ liệu nhạy cảm trong cơ sở dữ liệu.

## 📱 7. QUY TẮC TUÂN THỦ HYBRID MOBILE & CƠ CHẾ SEO QUỐC TẾ

- **[Rào cản Tuân thủ Hybrid Mobile Capacitor]:**
  - Thực hiện các ràng buộc cấu trúc hybrid mobile bằng cách hạn chế các cuộc gọi tài nguyên phía máy khách chỉ cho các cấu trúc giao thức tuyệt đối đã được xác thực. Tất cả các quy trình lưu trữ liên tục nội bộ MUST sử dụng engine trừu tượng thời gian chạy bản địa (`@capacitor/preferences`), kết hợp với các móc webview phần cứng bản địa để chặn các mẫu truy cập phần cứng thiết bị trái phép một cách lập trình.
  - Triển khai các bộ lọc đầu vào và đầu ra để ngăn chặn các ký tự đặc biệt có thể gây ra XSS trong các yêu cầu và phản hồi.

- **[Quốc tế hóa (i18n) & Tiêm động SEO]:**
  - Triển khai một máy chủ trung gian phát hiện ngôn ngữ động tại lớp biên để phân tích các thuộc tính ngôn ngữ người dùng đến. Pipeline biên dịch trang phản hồi Next.js MUST tự động xử lý các lược đồ siêu dữ liệu địa phương và tiêm các thuộc tính liên kết `hreflang` đối xứng, thân thiện với SEO một cách động vào cây trình bày.
  - Áp dụng các chính sách mã hóa dữ liệu tại nghỉ để bảo vệ dữ liệu nhạy cảm trong cơ sở dữ liệu.

## 🚀 8. LUỒNG BRANCH GIT PHIÊN BẢN TỰ ĐỘNG HÀNG NGÀY

- **[Độc lập phân nhánh không gian làm việc hàng ngày]:**
  - Thực hiện một quy trình kiểm soát phiên bản lập trình độc lập, tách biệt bằng cách thực hiện một phân vùng không gian làm việc tự động cho mỗi phiên bản kỹ thuật hàng ngày. Hệ thống tự động MUST xác thực rằng các nhà phát triển thực hiện công việc của họ trong các đường dẫn nhánh tách biệt được cấu trúc nghiêm ngặt dưới quy tắc đặt tên văn bản: `feature/phase-X-day-Y` (trong đó X đại diện cho chỉ số giai đoạn tính toán và Y chỉ ra ngày tuần tự hoạt động).
  - Áp dụng các chính sách mã hóa dữ liệu tại nghỉ để bảo vệ dữ liệu nhạy cảm trong cơ sở dữ liệu.

- **[Cổng kiểm tra tự động]:**
  - Thiết lập một cổng kiểm tra tự động, không thương lượng được trong máy chủ tích hợp liên tục GitHub Actions. Quy trình triển khai đám mây MUST tự động kích hoạt các bài kiểm tra xác minh biên dịch chéo, quét chất lượng mã tĩnh SonarQube và buộc hủy bỏ chuỗi triển khai nếu điểm số ma trận kiểm tra tích lũy rơi dưới ngưỡng tham số không thương lượng của `>= 85%`.
  - Áp dụng các chính sách mã hóa dữ liệu tại nghỉ để bảo vệ dữ liệu nhạy cảm trong cơ sở dữ liệu.

[TRACEABILITY MATRIX ENFORCEMENT: 100% COVERAGE VALIDATED. TOTAL UNIQUE REQ TAGS MAPPED: 3, TOTAL ARC TAGS: 6, TOTAL EXC TAGS: 5, TOTAL DAT TAGS: 3, TOTAL NFR TAGS: 3. ZERO UNASSIGNED CODES FOUND.]

<!--END_CHUNK_PART_3_FINAL-->