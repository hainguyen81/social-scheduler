# Giai đoạn 3: Dịch vụ đề xuất nội dung bằng AI

## 📊 Tài liệu kiểm soát

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260912141106 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 3 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Dịch vụ đề xuất nội dung bằng AI<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc triển khai dịch vụ đề xuất nội dung bằng AI và xử lý các ngoại lệ liên quan đến đề xuất nội dung<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày/Giờ** | 2026/09/12 14:11:06 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn này tập trung vào việc triển khai dịch vụ đề xuất nội dung bằng AI và xử lý các ngoại lệ liên quan đến đề xuất nội dung.

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
- Hoàn thành việc triển khai dịch vụ đề xuất nội dung bằng AI và xử lý các ngoại lệ liên quan đến đề xuất nội dung.
- Đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.
- Đảm bảo hoàn thành các bài kiểm thử chức năng cho các yêu cầu đã phân bổ.
- Đảm bảo 100% ánh xạ Tag ID.

## 5. Nhật ký thực thi kiến trúc hàng ngày

### 🌤️ NGÀY 1: Triển khai dịch vụ đề xuất nội dung và xử lý ngoại lệ
<!--DAY_HEADER_START-->Triển khai dịch vụ đề xuất nội dung và xử lý ngoại lệ<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Triển khai dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai dịch vụ đề xuất nội dung bằng AI để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

#### 📝 NHIỆM VỤ CON 2: Kiểm tra dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsServiceTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

#### 📝 NHIỆM VỤ CON 3: Xem xét mã dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Reviewer
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsService.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

#### 📝 NHIỆM VỤ CON 4: Tài liệu dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Doc
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/docs/content-recommendation-service.md`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho dịch vụ đề xuất nội dung bằng AI.

#### 📝 NHIỆM VỤ CON 5: Triển khai bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[EXC-003], [EXC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để xử lý các ngoại lệ liên quan đến đề xuất nội dung.

#### 📝 NHIỆM VỤ CON 6: Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandlerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[EXC-003], [EXC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các ngoại lệ liên quan đến đề xuất nội dung.

#### 📝 NHIỆM VỤ CON 7: Xem xét mã bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Reviewer
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsExceptionHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[EXC-003], [EXC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

#### 📝 NHIỆM VỤ CON 8: Tài liệu bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Doc
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/docs/content-recommendation-service-exception-handler.md`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[EXC-003], [EXC-004]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho bộ xử lý ngoại lệ cho dịch vụ đề xuất nội dung bằng AI.

### 🌤️ NGÀY 2: Triển khai điểm cuối API và hợp đồng sự kiện
<!--DAY_HEADER_START-->Triển khai điểm cuối API và hợp đồng sự kiện<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Triển khai điểm cuối API cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

#### 📝 NHIỆM VỤ CON 2: Kiểm tra điểm cuối API cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsControllerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó đề xuất nội dung bài đăng dựa trên hiệu suất trước đó.

#### 📝 NHIỆM VỤ CON 3: Xem xét mã điểm cuối API cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Reviewer
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsController.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã điểm cuối API cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

#### 📝 NHIỆM VỤ CON 4: Tài liệu điểm cuối API cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Doc
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/docs/content-recommendation-service-api.md`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho điểm cuối API cho dịch vụ đề xuất nội dung bằng AI.

#### 📝 NHIỆM VỤ CON 5: Triển khai hợp đồng sự kiện cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Coder
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để xử lý các sự kiện liên quan đến đề xuất nội dung.

#### 📝 NHIỆM VỤ CON 6: Kiểm tra hợp đồng sự kiện cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Tester
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java;./sources/backend/content-recommendation-service/src/test/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandlerTest.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để đảm bảo nó xử lý các sự kiện liên quan đến đề xuất nội dung.

#### 📝 NHIỆM VỤ CON 7: Xem xét mã hợp đồng sự kiện cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Reviewer
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsEventHandler.java`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Xem xét mã hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI để đảm bảo chất lượng mã và tuân thủ các tiêu chuẩn lập trình.

#### 📝 NHIỆM VỤ CON 8: Tài liệu hợp đồng sự kiện cho dịch vụ đề xuất nội dung
##### Tác nhân con được chỉ định: Doc
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/docs/content-recommendation-service-event-handler.md`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[REQ-002]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật cho hợp đồng sự kiện cho dịch vụ đề xuất nội dung bằng AI.