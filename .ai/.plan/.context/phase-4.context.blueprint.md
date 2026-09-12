# Giai đoạn 4: Quản lý người dùng và dịch vụ lên lịch

## 📊 Tài liệu kiểm soát

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260912141106 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 4 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Quản lý người dùng và dịch vụ lên lịch<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc triển khai các dịch vụ quản lý người dùng và dịch vụ lên lịch<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày/Giờ** | 2026/09/12 14:11:06 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn này tập trung vào việc triển khai các dịch vụ quản lý người dùng và dịch vụ lên lịch.

## 2. Phạm vi kỹ thuật và biên giới thư mục được phép (Tệp, đường dẫn và điểm cuối)
- **MANDATORY PLATFORM SKELETON MANIFEST INVARIANTS**:
  - When initializing the operational lifecycle blueprint (specifically bounded inside Phase 1 - DAY 1), you MUST explicitly inject and declare the primary repository infrastructure build descriptors before emitting any application source components.
  - For Microservices backend topologies, you MUST enforce the mandatory path definition of a parent project descriptor `./sources/backend/pom.xml` and isolated sub-module manifests `./sources/backend/<service-name>/pom.xml`.
  - For Frontend interface layer active applications, you MUST enforce the explicit configuration path registration of `./sources/frontend/package.json` and `./sources/frontend/tsconfig.json`. All generated scaffolding assets must map strictly to the architectural system tracking token `[ARC-000]`.

## 3. Hướng dẫn chức năng của tác nhân con được chỉ định
* **Coder**: Hoạt động như một Lập trình viên ứng dụng cấp cao/Chuyên gia. Trách nhiệm là triển khai mã nguồn ứng dụng thuần túy trên cả các dịch vụ backend và ứng dụng máy khách frontend/mobile. Bị cấm viết bộ kiểm thử hoặc biểu mẫu cơ sở hạ tầng.
* **Tester**: Hoạt động như một Trưởng/QA Chuyên gia. Chuyên về kỹ thuật bộ kiểm thử, xác nhận và cổng kiểm tra chất lượng. Trách nhiệm là tạo JUnit, bộ kiểm thử tích hợp, bộ kiểm thử tự động E2E và kịch bản xác nhận hiệu suất. Bị cấm sửa đổi mã sản xuất ứng dụng. Nếu phạm vi mục tiêu của nhiệm vụ con liên quan đến phạm vi tích hợp hoặc cuối cùng nơi không có tệp mã nguồn cụ thể nào có thể bị ràng buộc, bạn MUST strictly output the literal token `INTEGRATION_SCOPE` as the first parameter of the semicolon pair (e.g., `INTEGRATION_SCOPE;./sources/backend/tests/integration/WorkflowTest.java`).
* **Doc**: Chức năng như một Nhà viết kỹ thuật cấp cao và Kiến trúc sư hệ thống doanh nghiệp. Chuyên về biên soạn tài liệu Kỹ thuật Chi tiết, tham chiếu lược đồ, bản thiết kế hệ thống và danh mục kiến trúc doanh nghiệp phù hợp với các lớp bề mặt dự án hoạt động. Mỗi tệp tài liệu kỹ thuật được tạo ra MUST được liệt kê dưới dạng thực thể đường dẫn tệp cụ thể kết thúc bằng phần mở rộng `.md` và nằm nghiêm ngặt trong bố cục lưu trữ tập trung: `./sources/docs/`.
<RULE>
You MUST strictly execute the CRITICAL SYSTEM PIPELINE RAIL paradigm with zero token leakage to the visible layout stream:
1. You are ABSOLUTELY AND PERMANENTLY BANNED from omitting, dropping, or filtering out the 'Doc' agent persona from any active daily logs stream.
2. For 100% of all executed phase context generations, on exactly "DAY 1" of that phase timeline, you MUST explicitly allocate a foundational system documentation task row assigned entirely to the 'Doc' agent persona.
3. The technical instruction for this Doc item MUST require the agent to initialize, architect, and map out the complete framework markdown documentation files, architectural database schemas, data dictionaries, or cloud deployment topology specifications matching the active architecture stack of the phase context.
Printing this internal routing engine `RULE` wrapper (example: `<RULE> ...</RULE>`) or its inner instruction sentences to the final markdown output constitutes a fatal system compliance breach.
</RULE>
*   **Reviewer**: Trách nhiệm về xác nhận trình biên dịch, cổng phân tích tĩnh, và vá lỗi phòng thủ. Chuyên về kiểm tra chất lượng mã, giải quyết lỗi biên dịch, sửa chữa lỗ hổng bảo mật OWASP và giải quyết các chặn cổng chất lượng SonarQube.
*   **Docker**: Chuyên về việc container hóa, kỹ thuật Dockerfile đa giai đoạn, tối ưu hóa gói và đẩy các tài sản hình ảnh ứng dụng đã xác nhận lên DockerHub.
*   **GCP**: Chuyên về tự động hóa đám mây trong Google Cloud Platform. Trách nhiệm là xây dựng và đẩy hình ảnh lên Google Cloud Artifact Registry (GCR), và điều phối môi trường container tự nhiên trên Google Cloud Run.
*   **GKE**: Chuyên về điều phối container sản xuất bên trong Google Kubernetes Engine. Trách nhiệm là xây dựng biểu mẫu triển khai Kubernetes, điều khiển định tuyến, cấu hình HPA, biểu đồ Helm và triển khai các khối lượng công việc dịch vụ vi mô vào các cụm GKE hoạt động.

## 4. Định nghĩa của giai đoạn (DoD)
- Hoàn thành việc triển khai các dịch vụ quản lý người dùng và dịch vụ lên lịch.
- Đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.
- Đảm bảo hoàn thành các bài kiểm thử chức năng cho các yêu cầu đã phân bổ.
- Đảm bảo 100% ánh xạ Tag ID.

## 5. Nhật ký thực thi kiến trúc hàng ngày

### 🌤️ NGÀY 1: Triển khai dịch vụ quản lý người dùng
<!--DAY_HEADER_START-->Triển khai dịch vụ quản lý người dùng<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Triển khai lớp UserService
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserService với các phương thức quản lý người dùng cơ bản như tạo, đọc, cập nhật và xóa người dùng.

#### 📝 NHIỆM VỤ CON 2: Triển khai lớp UserController
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserController với các điểm cuối API để quản lý người dùng.

#### 📝 NHIỆM VỤ CON 3: Triển khai lớp UserExceptionHandler
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp UserExceptionHandler để xử lý các ngoại lệ liên quan đến người dùng.

#### 📝 NHIỆM VỤ CON 4: Kiểm thử lớp UserService
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserService.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserServiceTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserService để đảm bảo các phương thức quản lý người dùng hoạt động đúng.

#### 📝 NHIỆM VỤ CON 5: Kiểm thử lớp UserController
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserController.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserControllerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserController để đảm bảo các điểm cuối API hoạt động đúng.

#### 📝 NHIỆM VỤ CON 6: Kiểm thử lớp UserExceptionHandler
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandler.java;./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserExceptionHandlerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[ARC-001], [ARC-002], [ARC-003], [ARC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp UserExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.

### 🌤️ NGÀY 2: Triển khai dịch vụ lên lịch
<!--DAY_HEADER_START-->Triển khai dịch vụ lên lịch<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Triển khai lớp ScheduleService
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleService với các phương thức quản lý lịch đăng bài cơ bản như tạo, đọc, cập nhật và xóa lịch đăng bài.

#### 📝 NHIỆM VỤ CON 2: Triển khai lớp ScheduleController
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleController với các điểm cuối API để quản lý lịch đăng bài.

#### 📝 NHIỆM VỤ CON 3: Triển khai lớp ScheduleExceptionHandler
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai lớp ScheduleExceptionHandler để xử lý các ngoại lệ liên quan đến lịch đăng bài.

#### 📝 NHIỆM VỤ CON 4: Kiểm thử lớp ScheduleService
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleService.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleServiceTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleService để đảm bảo các phương thức quản lý lịch đăng bài hoạt động đúng.

#### 📝 NHIỆM VỤ CON 5: Kiểm thử lớp ScheduleController
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleController.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleControllerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleController để đảm bảo các điểm cuối API hoạt động đúng.

#### 📝 NHIỆM VỤ CON 6: Kiểm thử lớp ScheduleExceptionHandler
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandler.java;./sources/backend/scheduling-service/src/test/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleExceptionHandlerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-001], [EXC-001], [EXC-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Viết các bài kiểm thử cho lớp ScheduleExceptionHandler để đảm bảo các ngoại lệ được xử lý đúng.