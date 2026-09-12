# Giai đoạn 5: Giao hàng cơ sở hạ tầng DevOps và tài liệu kỹ thuật

## 📊 Tài liệu kiểm soát

| Mục | Chi tiết |
| :--- | :--- |
| **Mã bản thiết kế** | ARCH-20260912141106 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 5 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Giao hàng cơ sở hạ tầng DevOps và tài liệu kỹ thuật<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn này tập trung vào việc triển khai cơ sở hạ tầng DevOps và tạo tài liệu kỹ thuật cho hệ thống<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày/Giờ** | 2026/09/12 14:11:06 |
| **Tác giả** | Kiến trúc sư hệ thống doanh nghiệp (SA Agent) |
| **Phê duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. Phạm vi hoạt động và mục tiêu của giai đoạn
Giai đoạn này tập trung vào việc triển khai cơ sở hạ tầng DevOps và tạo tài liệu kỹ thuật cho hệ thống.

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
- Hoàn thành việc triển khai cơ sở hạ tầng DevOps.
- Đảm bảo tuân thủ các tiêu chuẩn bảo mật OWASP.
- Đảm bảo hoàn thành các bài kiểm thử chức năng cho các yêu cầu đã phân bổ.
- Đảm bảo 100% ánh xạ Tag ID.

## 5. Nhật ký thực thi kiến trúc hàng ngày

### 🌤️ NGÀY 1: Tạo tài liệu kỹ thuật và thiết lập cơ sở hạ tầng DevOps
<!--DAY_HEADER_START-->Tạo tài liệu kỹ thuật và thiết lập cơ sở hạ tầng DevOps<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Tạo tài liệu kỹ thuật
##### Tác nhân con được chỉ định: Doc
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/docs/technical-documentation.md`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[DOC-001]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tài liệu kỹ thuật chi tiết cho hệ thống, bao gồm mô tả kiến trúc, hướng dẫn triển khai và tài liệu tham khảo API.

#### 📝 NHIỆM VỤ CON 2: Thiết lập cơ sở hạ tầng DevOps
##### Tác nhân con được chỉ định: Docker
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/infra/devops-setup.sh`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Tạo tập lệnh thiết lập cơ sở hạ tầng DevOps, bao gồm cấu hình Docker, Kubernetes và các dịch vụ đám mây.

### 🌤️ NGÀY 2: Triển khai và kiểm tra cơ sở hạ tầng DevOps
<!--DAY_HEADER_START-->Triển khai và kiểm tra cơ sở hạ tầng DevOps<!--DAY_HEADER_END-->

#### 📝 NHIỆM VỤ CON 1: Triển khai cơ sở hạ tầng DevOps
##### Tác nhân con được chỉ định: GCP
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/infra/devops-deployment.sh`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Triển khai cơ sở hạ tầng DevOps trên Google Cloud Platform, bao gồm cấu hình mạng, lưu trữ và dịch vụ.

#### 📝 NHIỆM VỤ CON 2: Kiểm tra cơ sở hạ tầng DevOps
##### Tác nhân con được chỉ định: GKE
##### Thành phần mục tiêu:
* **Đường dẫn mục tiêu:** `./sources/infra/devops-testing.sh`

* **Traceability Tag Tokens:**
<!--START_TAGS-->[NFR-001], [NFR-002], [NFR-003]<!--END_TAGS-->

* **Hướng dẫn kỹ thuật cấp thấp:** Kiểm tra cơ sở hạ tầng DevOps trên Google Kubernetes Engine, bao gồm kiểm tra tính khả dụng, hiệu suất và bảo mật.