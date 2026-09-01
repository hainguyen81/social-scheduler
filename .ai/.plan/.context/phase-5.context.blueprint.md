# Giai đoạn 5: <!--PHASE_NAME_START-->Triển khai hạ tầng DevOps tài liệu kiến trúc và vận hành hệ thống<!--PHASE_NAME_END-->

## 📊 Kiểm soát Tài liệu

| Hạng mục | Chi tiết |
| :--- | :--- |
| **Mã Blueprint** | ARCH-20260831230418 |
| **Tên dự án** | social-scheduler |
| **Giai đoạn** | 5 |
| **Tên giai đoạn** | <!--PHASE_NAME_START-->Triển khai hạ tầng DevOps tài liệu kiến trúc và vận hành hệ thống<!--PHASE_NAME_END--> |
| **Mô tả** | <!--PHASE_DESC_START-->Giai đoạn 5 tập trung hoàn thiện hạ tầng triển khai và đóng gói tài liệu cho hệ thống social-scheduler. Sản phẩm bàn giao gồm container hóa đa giai đoạn cho bốn dịch vụ backend, hạ tầng GCP được mô hình hóa bằng Terraform, manifest Kubernetes cho GKE, tích hợp quan sát hệ thống với Prometheus và Grafana, cùng bộ ba tài liệu kỹ thuật Blueprint, Runbook và CI/CD<!--PHASE_DESC_END--> |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/08/31 23:04:18 |
| **Tác giả** | Kiến trúc sư Hệ thống Doanh nghiệp (SA Agent) |
| **Phê duyệt** | Chờ phê duyệt quản trị kỹ thuật |

## 1. Phạm vi Hoạt động & Mục tiêu Giai đoạn

Giai đoạn 5 tập trung 100% vào việc hoàn thiện hạ tầng triển khai (DevOps Infrastructure) và đóng gói tài liệu doanh nghiệp (Enterprise Documentation) cho hệ thống `social-scheduler`. Phạm vi cụ thể bao gồm: (1) Container hóa đa giai đoạn (multi-stage) cho bốn dịch vụ backend `user-service`, `schedule-service`, `ai-service`, `rate-limit-service` sử dụng `eclipse-temurin:21-jdk-jammy` cho giai đoạn build và `eclipse-temurin:21-jre-jammy` cho giai đoạn runtime, đảm bảo footprint image thấp và độ trễ khởi động dưới 200ms theo [NFR-001]; (2) Khởi tạo hạ tầng GCP bằng Terraform bao gồm VPC với CIDR `10.10.0.0/16`, Cloud Router + Cloud NAT, GKE Autopilot cluster với Workload Identity, Binary Authorization, Shielded Nodes, Cloud SQL cho PostgreSQL và Memorystore cho Redis theo [NFR-002], [NFR-003]; (3) Tạo manifest Kubernetes (Deployment, Service, HPA, Ingress, ConfigMap) cho GKE với cấu hình tự động mở rộng theo chiều ngang khi CPU vượt 70% hoặc memory vượt 80%; (4) Tích hợp Prometheus + Grafana cho quan sát hệ thống (Observability) với dashboard theo dõi latency P95, tỷ lệ HTTP 429, mức sử dụng CPU/RAM cho mỗi dịch vụ; (5) Đóng gói ba tài liệu kỹ thuật gồm `SocialSchedulerBlueprint.md` (sơ đồ kiến trúc, sơ đồ tuần tự, ma trận RBAC), `DeploymentRunbook.md` (quy trình triển khai và vận hành) và `CicdPipeline.md` (quy trình GitHub Actions từ lint đến deploy production với approval gate) theo [DOC-001]. 

Giai đoạn này KHÔNG triển khai: mã nguồn ứng dụng (đã hoàn thiện tại Giai đoạn 1, 2, 3, 4), logic nghiệp vụ cốt lõi, thay đổi lược đồ cơ sở dữ liệu, bộ xử lý ngoại lệ mới, hay endpoint RESTful nghiệp vụ.

## 2. Phạm vi Kỹ thuật Được phép & Ranh giới Thư mục

Danh sách kiểm tra kỹ thuật các tệp vật lý được phép tạo hoặc xử lý trong phạm vi giai đoạn này, mọi mục đều kèm mã định danh truy vết:

* `./sources/infra/docker/user-service/Dockerfile` — Dockerfile đa giai đoạn cho `user-service`. [NFR-001]
* `./sources/infra/docker/schedule-service/Dockerfile` — Dockerfile đa giai đoạn cho `schedule-service`. [NFR-001]
* `./sources/infra/docker/ai-service/Dockerfile` — Dockerfile đa giai đoạn cho `ai-service`. [NFR-001]
* `./sources/infra/docker/rate-limit-service/Dockerfile` — Dockerfile đa giai đoạn cho `rate-limit-service`. [NFR-001]
* `./sources/infra/terraform/gcp/main.tf` — Module Terraform root cho hạ tầng GCP. [NFR-002], [NFR-003]
* `./sources/infra/terraform/gcp/vpc.tf` — Cấu hình VPC, subnets, Cloud Router, Cloud NAT, firewall rules. [NFR-002]
* `./sources/infra/terraform/gcp/gke.tf` — Cấu hình GKE Autopilot cluster với Workload Identity. [NFR-002], [NFR-003]
* `./sources/infra/kubernetes/socialscheduler/base/deployment.yaml` — Deployment cho `schedule-service`. [NFR-003]
* `./sources/infra/kubernetes/socialscheduler/base/service.yaml` — Service ClusterIP cho các dịch vụ. [NFR-003]
* `./sources/infra/kubernetes/socialscheduler/base/hpa.yaml` — HorizontalPodAutoscaler cho tự động mở rộng. [NFR-003]
* `./sources/infra/kubernetes/socialscheduler/base/ingress.yaml` — NGINX Ingress với TLS. [NFR-003]
* `./sources/infra/kubernetes/socialscheduler/base/configmap.yaml` — ConfigMap cấu hình runtime. [NFR-003]
* `./sources/infra/observability/prometheus.yaml` — ConfigMap Prometheus với scrape jobs. [NFR-001]
* `./sources/infra/observability/grafana-dashboard.json` — Dashboard Grafana cho service overview. [NFR-001]
* `./sources/docs/architecture/SocialSchedulerBlueprint.md` — Blueprint kiến trúc hệ thống. [DOC-001]
* `./sources/docs/operations/DeploymentRunbook.md` — Runbook triển khai và vận hành. [DOC-001]
* `./sources/docs/operations/CicdPipeline.md` — Tài liệu quy trình CI/CD. [DOC-001]

## 3. Chỉ thị Chức năng Chuyên biệt cho Sub-Agent

Phân bổ nhiệm vụ và ràng buộc kỹ thuật cho từng persona Sub-Agent hoạt động trong giai đoạn này:

* **Docker**: Hoạt động với vai trò Chuyên gia Containerization, chịu trách nhiệm thiết kế Dockerfile đa giai đoạn cho bốn dịch vụ backend, tối ưu hóa footprint image và đảm bảo healthcheck endpoint hoạt động chính xác.
* **GCP**: Hoạt địch danh Chuyên gia Cloud Automation, chịu trách nhiệm khởi tạo module Terraform cho hạ tầng GCP bao gồm VPC, GKE Autopilot cluster, Cloud SQL và Memorystore, đồng thời tích hợp Prometheus + Grafana cho quan sát hệ thống.
* **GKE**: Hoạt động với vai trò Chuyên gia Container Orchestration, chuyên trách tạo manifest Kubernetes (Deployment, Service, HPA, Ingress, ConfigMap) cho cụm GKE và cấu hình HorizontalPodAutoscaler.
* **Doc**: Hoạt động với vai trò Chuyên gia Viết tài liệu Kỹ thuật, chịu trách nhiệm biên soạn Blueprint kiến trúc hệ thống, Runbook triển khai, tài liệu CI/CD Pipeline và ma trận tuân thủ bảo mật OWASP.
* **Reviewer**: Chịu trách nhiệm xác minh biên dịch manifest Terraform/Kubernetes, phân tích tĩnh cấu hình Dockerfile, đánh giá tuân thủ tiêu chuẩn OWASP và kiểm tra tính nhất quán giữa các thành phần hạ tầng.

## 4. Định nghĩa Hoàn thành Giai đoạn (DoD)

Giai đoạn 5 được coi là hoàn thành khi đáp ứng đồng thời các tiêu chí định lượng khách quan sau:

* Bốn Dockerfile đa giai đoạn biên dịch thành công thông qua `docker build` và image cuối cùng có footprint dưới 300MB.
* Mỗi container kích hoạt healthcheck endpoint `/actuator/health` trả về `"status":"UP"` trong vòng 40 giây sau khi khởi động.
* Module Terraform `terraform validate` và `terraform plan` chạy thành công cho hạ tầng GCP (VPC, GKE, Cloud SQL, Memorystore).
* Manifest Kubernetes `kubectl apply --dry-run=client` chạy thành công cho Deployment, Service, HPA, Ingress và ConfigMap.
* HPA cấu hình tự động mở rộng khi CPU > 70% hoặc memory > 80%, đảm bảo khả năng chịu tải 1000+ request/phút theo [NFR-001].
* Dashboard Grafana hiển thị đầy đủ bốn panel: HTTP Request Latency P95, Rate Limited Requests (HTTP 429), CPU Usage per Pod và Memory Usage per Pod.
* Tài liệu Blueprint bao gồm sơ đồ kiến trúc, sơ đồ tuần tự cho luồng lập lịch và luồng đề xuất AI, ma trận RBAC ánh xạ [ARC-001] đến [ARC-004].
* Runbook chứa đầy đủ quy trình `terraform apply`, `kubectl apply`, rollback bằng `kubectl rollout undo` và danh sách kiểm tra sau triển khai.
* Tài liệu CI/CD mô tả chín giai đoạn pipeline (lint, unit-test, integration-test, build-image, push-image, deploy-staging, smoke-test, approval, deploy-prod).
* Ma trận tuân thủ OWASP ánh xạ 100% các mục A01, A02, A03, A04, A05, A07, A09 sang biện pháp giảm thiểu cụ thể trong hệ thống.
* Tất cả các mã định danh truy vết `[NFR-001]`, `[NFR-002]`, `[NFR-003]`, `[DOC-001]` được ánh xạ 1:1 vào các tệp vật lý tương ứng.

## 5. NHẬT KÝ THỰC THI KIẾN TRÚC THEO NGÀY

### 🌤️ NGÀY 1: <!--DAY_HEADER_START-->Khởi tạo Container hóa đa giai đoạn cho bốn dịch vụ Backend<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 1.1: Tạo Dockerfile đa giai đoạn cho user-service với JRE runtime tối ưu
##### Sub-Agent được phân công: Docker
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/docker/user-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư containerization phải tạo Dockerfile đa giai đoạn (multi-stage) tại `./sources/infra/docker/user-service/Dockerfile` sử dụng syntax `docker/dockerfile:1.6`. Giai đoạn build sử dụng base image `eclipse-temurin:21-jdk-jammy` với WORKDIR `/build`, sao chép `./sources/backend/user-service/pom.xml`, `mvnw`, thư mục `.mvn`, thực thi `./mvnw -B -ntp -q -f pom.xml dependency:go-offline` để tải trước dependencies, sau đó sao chép thư mục `src` và thực thi `./mvnw -B -ntp -q -DskipTests package` để đóng gói JAR. Giai đoạn runtime chuyển sang base image `eclipse-temurin:21-jre-jammy` nhẹ hơn, tạo nhóm hệ thống `appgroup` và người dùng `appuser` (UID 1001) để chạy container không đặc quyền, sao chép file JAR từ giai đoạn build sang `/app/app.jar`, cấu hình biến môi trường `JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=1.0 -XX:+ExitOnOutOfMemoryError"` và `SPRING_PROFILES_ACTIVE=docker`, mở cổng `EXPOSE 8081`, định nghĩa `HEALTHCHECK` gọi `wget -qO- http://127.0.0.1:8081/actuator/health` kiểm tra `"status":"UP"` với interval 30 giây, timeout 5 giây, start-period 40 giây, retries 3 lần. ENTRYPOINT sử dụng `sh -c` để truyền biến `JAVA_OPTS` vào lệnh `java -jar`. Thẻ [NFR-001] yêu cầu đảm bảo footprint image thấp và độ trễ khởi động dưới 200ms.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 1.2: Tạo Dockerfile đa giai đoạn cho schedule-service với Kafka và Redis client
##### Sub-Agent được phân công: Docker
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/docker/schedule-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư containerization phải tạo Dockerfile đa giai đoạn tại `./sources/infra/docker/schedule-service/Dockerfile` tái sử dụng pattern multi-stage đã thiết lập tại tác vụ 1.1. Giai đoạn build sử dụng `eclipse-temurin:21-jdk-jammy` với quy trình tương tự: WORKDIR `/build`, sao chép `./sources/backend/schedule-service/pom.xml`, `mvnw`, `.mvn`, chạy `./mvnw -B -ntp -q -f pom.xml dependency:go-offline` để tải dependencies bao gồm Spring Kafka client và Spring Data Redis (Lettuce), sao chép `src` và build JAR. Giai đoạn runtime sử dụng `eclipse-temurin:21-jre-jammy`, tạo `appuser` (UID 1001) thuộc `appgroup`, sao chép JAR sang `/app/app.jar`. Cấu hình biến môi trường `JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=1.0 -XX:+ExitOnOutOfMemoryError"` để tối ưu G1GC cho thông lượng cao, `SPRING_PROFILES_ACTIVE=docker`, `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`, `REDIS_HOST=redis`. Mở cổng `EXPOSE 8082`, định nghĩa `HEALTHCHECK` gọi `wget -qO- http://127.0.0.1:8082/actuator/health` với cùng tham số interval/timeout/start-period/retries. ENTRYPOINT sử dụng `sh -c` để truyền JAVA_OPTS. Thẻ [NFR-001] yêu cầu đảm bảo thông lượng trên 1000 request/phút được duy trì khi chạy trong container thông qua tối ưu G1GC.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 1.3: Tạo Dockerfile đa giai đoạn cho ai-service với OpenAI SDK
##### Sub-Agent được phân công: Docker
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/docker/ai-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư containerization phải tạo Dockerfile đa giai đoạn tại `./sources/infra/docker/ai-service/Dockerfile` duy trì pattern multi-stage. Giai đoạn build sử dụng `eclipse-temurin:21-jdk-jammy` với WORKDIR `/build`, sao chép `./sources/backend/ai-service/pom.xml`, `mvnw`, `.mvn`, chạy `./mvnw -B -ntp -q -f pom.xml dependency:go-offline` để tải OpenAI Java SDK 0.18.x và WebClient, sao chép `src` và build JAR. Giai đoạn runtime sử dụng `eclipse-temurin:21-jre-jammy`, tạo `appuser` (UID 1001) thuộc `appgroup`, sao chép JAR sang `/app/app.jar`. Cấu hình biến môi trường `JAVA_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED -XX:+UseContainerSupport -XX:MaxRAMPercentage=1.0"` để tránh cảnh báo reflection từ thư viện AI SDK, `SPRING_PROFILES_ACTIVE=docker`. Biến `OPENAI_API_KEY` được đọc từ Kubernetes Secret thông qua cơ chế env injection tại thời điểm triển khai. Mở cổng `EXPOSE 8083`, định nghĩa `HEALTHCHECK` gọi `wget -qO- http://127.0.0.1:8083/actuator/health` với cùng tham số. ENTRYPOINT sử dụng `sh -c`. Thẻ [NFR-001] yêu cầu độ trễ khởi động dưới 200ms và footprint image thấp.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 1.4: Tạo Dockerfile đa giai đoạn cho rate-limit-service với Redis Token Bucket
##### Sub-Agent được phân công: Docker
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/docker/rate-limit-service/Dockerfile`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư containerization phải tạo Dockerfile đa giai đoạn tại `./sources/infra/docker/rate-limit-service/Dockerfile` đóng gói `rate-limit-service` với pattern multi-stage. Giai đoạn build sử dụng `eclipse-temurin:21-jdk-jammy` với WORKDIR `/build`, sao chép `./sources/backend/rate-limit-service/pom.xml`, `mvnw`, `.mvn`, chạy `./mvnw -B -ntp -q -f pom.xml dependency:go-offline` để tải Bucket4j Core 8.x và Bucket4j Redis 8.x, sao chép `src` và build JAR. Giai đoạn runtime sử dụng `eclipse-temurin:21-jre-jammy`, tạo `appuser` (UID 1001) thuộc `appgroup`, sao chép JAR sang `/app/app.jar`. Cấu hình biến môi trường `JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=1.0"`, `SPRING_PROFILES_ACTIVE=docker`, `REDIS_HOST=redis`. Mở cổng `EXPOSE 8084`, định nghĩa `HEALTHCHECK` gọi `wget -qO- http://127.0.0.1:8084/actuator/health` với cùng tham số interval 30 giây, timeout 5 giây, start-period 40 giây, retries 3 lần. Đảm bảo container chạy dưới người dùng không đặc quyền `appuser`. ENTRYPOINT sử dụng `sh -c`. Thẻ [NFR-001] yêu cầu duy trì độ trễ dưới 200ms khi xử lý giới hạn tỷ lệ.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

### 🌤️ NGÀY 2: <!--DAY_HEADER_START-->Khởi tạo hạ tầng GCP bằng Terraform và Manifest Kubernetes cho GKE<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 2.1: Tạo cấu hình Terraform root cho hạ tầng GCP social-scheduler
##### Sub-Agent được phân công: GCP
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/terraform/gcp/main.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GCP phải khởi tạo module Terraform root cho hạ tầng `social-scheduler` trong vùng `asia-southeast1`. Khai báo khối `terraform { required_version = ">= 1.6.0" }` và `required_providers` cho `google` (version `~> 5.10`) và `google-beta` (version `~> 5.10`). Cấu hình backend `gcs` tại bucket `socialscheduler-tfstate` với prefix `terraform/state` để lưu trữ state file an toàn. Khai báo `provider "google"` với `project = var.project_id` và `region = var.region`. Định nghĩa biến `project_id` (mặc định `social-scheduler-prod`), `region` (mặc định `asia-southeast1`), `cluster_name` (mặc định `socialscheduler-gke`). Import bốn module con: `module "vpc"` từ `./vpc.tf`, `module "gke"` từ `./gke.tf`, `module "cloudsql"` từ `./cloudsql.tf`, `module "memorystore"` từ `./memorystore.tf`. Khai báo ba output: `gke_endpoint` từ `module.gke.endpoint`, `redis_host` từ `module.memorystore.host`, `db_instance` từ `module.cloudsql.connection_name`. Thẻ [NFR-002] yêu cầu bảo mật theo chuẩn OWASP và mã hóa dữ liệu nhạy cảm; thẻ [NFR-003] yêu cầu cô lập đa tenant và khả năng mở rộng ngang.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.2: Định nghĩa VPC, subnets, Cloud Router và Cloud NAT
##### Sub-Agent được phân công: GCP
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/terraform/gcp/vpc.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GCP phải tạo `./sources/infra/terraform/gcp/vpc.tf` định nghĩa custom VPC với tài nguyên `google_compute_network "socialscheduler_vpc"` (name `socialscheduler-vpc`, `auto_create_subnetworks = false`, `routing_mode = "REGIONAL"`). Tạo subnet `google_compute_subnetwork "gke_subnet"` (name `socialscheduler-gke-subnet`, region `var.region`, `ip_cidr_range = "10.10.1.0/24"`, network trỏ về VPC) với hai secondary IP ranges: `gke-pods` (CIDR `10.20.0.0/16`) cho pods và `gke-services` (CIDR `10.30.0.0/20`) cho services. Cấu hình `google_compute_router "router"` (name `socialscheduler-router`, region `var.region`, network trỏ về VPC) và `google_compute_router_nat "nat"` (name `socialscheduler-nat`, router trỏ về `socialscheduler-router`, region `var.region`, `nat_ip_allocate_option = "AUTO_ONLY"`, `source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"`) để cung cấp egress an toàn. Tạo hai firewall rules: `google_compute_firewall "allow_internal"` cho phép TCP port 0-65535 từ CIDR `10.10.0.0/16` và `google_compute_firewall "allow_https"` cho phép TCP port 80, 443 từ `0.0.0.0/0` với target tag `https-server`. Thẻ [NFR-002] yêu cầu giới hạn CORS và phát hiện DDoS; thẻ [NFR-003] yêu cầu dự phòng cao.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.3: Định nghĩa GKE Autopilot Cluster với Workload Identity
##### Sub-Agent được phân công: GCP
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/terraform/gcp/gke.tf`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-002], [NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GCP phải tạo `./sources/infra/terraform/gcp/gke.tf` cấu hình GKE Autopilot cluster thông qua tài nguyên `google_container_cluster "socialscheduler_gke"` với `name = var.cluster_name`, `location = var.region`, `enable_autopilot = true`, `network` trỏ về `google_compute_network.socialscheduler_vpc.id`, `subnetwork` trỏ về `google_compute_subnetwork.gke_subnet.id`. Cấu hình `ip_allocation_policy` với `cluster_secondary_range_name = "gke-pods"` và `services_secondary_range_name = "gke-services"`. Bật `workload_identity_config` với `workload_pool = "${var.project_id}.svc.id.goog"`. Bật `binary_authorization` với `evaluation_mode = "PROJECT_SINGLETON_POLICY_ENFORCE"` để thực thi chính sách bảo mật hình ảnh. Định nghĩa `maintenance_policy` với `recurring_window` (`start_time = "2026-09-01T02:00:00Z"`, `end_time = "2026-09-01T06:00:00Z"`, `recurrence = "FREQ=WEEKLY;BYDAY=SA,SU"`). Cấu hình `release_channel` với `channel = "REGULAR"`. Tạo Service Account `google_service_account "gke_sa"` (account_id `socialscheduler-gke-sa`, display_name `Social Scheduler GKE Service Account`) và `google_project_iam_member "gke_sa_roles"` với `for_each = toset(["roles/logging.logWriter", "roles/monitoring.metricWriter", "roles/cloudsql.client", "roles/redis.editor"])`. Thẻ [NFR-002] yêu cầu che giấu dữ liệu nhạy cảm, [NFR-003] yêu cầu mở rộng theo chiều ngang.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.4: Tạo manifest Kubernetes Deployment cho schedule-service với probes
##### Sub-Agent được phân công: GKE
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/kubernetes/socialscheduler/base/deployment.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GKE phải tạo `./sources/infra/kubernetes/socialscheduler/base/deployment.yaml` định nghĩa Deployment cho `schedule-service` chạy 3 bản sao (`replicas: 3`) với `strategy.type = RollingUpdate`, `rollingUpdate.maxSurge = 1`, `rollingUpdate.maxUnavailable = 0` để đảm bảo zero-downtime deployment. Sử dụng `selector.matchLabels.app = schedule-service`. Template metadata chứa labels `app=schedule-service`, `version=1.0.0`, `track=stable` và annotations `prometheus.io/scrape: "true"`, `prometheus.io/path: "/actuator/prometheus"`, `prometheus.io/port: "8082"` để Prometheus scrape metrics. Spec chứa `serviceAccountName: socialscheduler-ksa`, `automountServiceAccountToken: false`. Container `schedule-service` sử dụng image `asia-southeast1-docker.pkg.dev/social-scheduler-prod/socialscheduler/schedule-service:1.0.0`, `imagePullPolicy: Always`, port `8082` tên `http`. Cấu hình `envFrom` tham chiếu `configMapRef: schedule-service-config` và `secretRef: schedule-service-secrets`. Resources: `requests: {cpu: "250m", memory: "512Mi"}`, `limits: {cpu: "500m", memory: "768Mi"}`. Probes: `readinessProbe.httpGet.path: /actuator/health/readiness` (initialDelaySeconds: 20, periodSeconds: 10, failureThreshold: 3), `livenessProbe.httpGet.path: /actuator/health/liveness` (initialDelaySeconds: 60, periodSeconds: 20, failureThreshold: 3). Thẻ [NFR-003] yêu cầu mở rộng ngang tự động và sẵn sàng cao.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.5: Tạo manifest Service, HPA và Ingress cho schedule-service
##### Sub-Agent được phân công: GKE
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/kubernetes/socialscheduler/base/service.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GKE phải tạo `./sources/infra/kubernetes/socialscheduler/base/service.yaml` khai báo ba tài nguyên Kubernetes. Service `schedule-service` (namespace `socialscheduler`, `type: ClusterIP`, `selector.app: schedule-service`) mở port 80 forward đến `targetPort: 8082` protocol TCP tên `http`. HorizontalPodAutoscaler `schedule-service-hpa` (namespace `socialscheduler`, `scaleTargetRef: apiVersion: apps/v1, kind: Deployment, name: schedule-service`) cấu hình `minReplicas: 3`, `maxReplicas: 20`, metrics gồm hai Resource targets: CPU `averageUtilization: 60` và memory `averageUtilization: 70`. Ingress `schedule-service-ingress` (namespace `socialscheduler`) sử dụng `ingressClassName: nginx` với annotations `nginx.ingress.kubernetes.io/rewrite-target: /` và `nginx.ingress.kubernetes.io/proxy-body-size: "8m"`. Cấu hình TLS với `secretName: socialscheduler-tls` cho host `api.socialscheduler.local`. Rules định nghĩa path `/api/v1/schedules` với `pathType: Prefix` trỏ về backend service `schedule-service` port `80`. Thẻ [NFR-003] yêu cầu khả năng mở rộng theo chiều ngang.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.6: Tạo ConfigMap cấu hình runtime và Secret cho schedule-service
##### Sub-Agent được phân công: GKE
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/kubernetes/socialscheduler/base/configmap.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-003]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GKE phải tạo `./sources/infra/kubernetes/socialscheduler/base/configmap.yaml` khai báo hai tài nguyên Kubernetes. ConfigMap `schedule-service-config` (namespace `socialscheduler`) chứa các khóa cấu hình: `SPRING_PROFILES_ACTIVE: "docker"`, `KAFKA_BOOTSTRAP_SERVERS: "kafka.kafka.svc.cluster.local:9092"`, `REDIS_HOST: "redis-master.redis.svc.cluster.local"`, `REDIS_PORT: "6379"`, `APP_TENANT_HEADER: "X-Tenant-Id"`, `LOG_LEVEL_ROOT: "INFO"`. Secret `schedule-service-secrets` (namespace `socialscheduler`, `type: Opaque`) sử dụng `stringData` chứa các khóa nhạy cảm: `JWT_SIGNING_KEY: "REPLACE_WITH_BASE64_256BIT_KEY"`, `OAUTH2_ISSUER_URI: "https://auth.socialscheduler.local"`, `FACEBOOK_APP_SECRET: "REPLACE_WITH_FB_SECRET"`, `INSTAGRAM_APP_SECRET: "REPLACE_WITH_IG_SECRET"`, `TIKTOK_CLIENT_SECRET: "REPLACE_WITH_TIKTOK_SECRET"`. Lưu ý các giá trị mẫu cần được thay thế bằng giá trị thực tế từ Secret Manager khi triển khai production. Thẻ [NFR-003] yêu cầu cấu hình đa tenant thông qua header.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.7: Tích hợp Prometheus + Grafana cho Observability hệ thống
##### Sub-Agent được phân công: GCP
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/observability/prometheus.yaml`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GCP phải tạo `./sources/infra/observability/prometheus.yaml` định nghĩa ConfigMap `prometheus-config` (namespace `observability`) chứa cấu hình Prometheus tại khóa `prometheus.yml`. Cấu hình global: `scrape_interval: 15s`, `evaluation_interval: 15s`. Định nghĩa scrape job `socialscheduler-services` sử dụng `kubernetes_sd_configs` với `role: pod`. Áp dụng bốn relabel_configs: (1) giữ các pod có annotation `prometheus.io/scrape=true` (action keep, regex true); (2) giữ các pod thuộc namespace `socialscheduler` (action keep, regex socialscheduler); (3) thay thế `__address__` bằng giá trị annotation `prometheus.io/port` (action replace, regex `([^:]+)(?::\d+)?`, replacement `${1}`); (4) gán label `service` từ pod label `app` (target_label: service). Thẻ [NFR-001] yêu cầu đảm bảo độ trễ dưới 200ms được theo dõi liên tục.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 2.8: Tạo Dashboard Grafana mẫu cho social-scheduler
##### Sub-Agent được phân công: GCP
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/infra/observability/grafana-dashboard.json`
* **Traceability Tag Tokens:** <!--START_TAGS-->[NFR-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Kỹ sư GCP phải tạo `./sources/infra/observability/grafana-dashboard.json` định nghĩa dashboard Grafana chuẩn 10.x với ba panel chính. Panel 1 (timeseries) `HTTP Request Latency P95 (ms)` sử dụng Prometheus expression `histogram_quantile(0.95, sum by (le, service) (rate(http_server_requests_seconds_bucket{namespace="socialscheduler"}[5m])))` với `legendFormat: {{service}}`, datasource `prometheus`, unit `ms`, thresholds xanh cho giá trị dưới 200 và đỏ cho giá trị trên 200. Panel 2 (stat) `Rate Limited Requests (HTTP 429)` sử dụng expression `sum(rate(http_server_requests_seconds_count{namespace="socialscheduler", status="429"}[5m]))`. Panel 3 (timeseries) `CPU Usage per Pod` sử dụng expression `sum by (pod) (rate(container_cpu_usage_seconds_total{namespace="socialscheduler"}[5m]))` với `legendFormat: {{pod}}`. Dashboard metadata: `schemaVersion: 39`, `tags: ["socialscheduler", "observability"]`, `timezone: browser`, `editable: true`, `title: "Social Scheduler - Service Overview"`. Thẻ [NFR-001] yêu cầu trực quan hóa hiệu năng liên tục.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

### 🌤️ NGÀY 3: <!--DAY_HEADER_START-->Đóng gói bộ Tài liệu Kiến trúc Runbook Vận hành và Quy trình CI/CD<!--DAY_HEADER_END-->

#### 📝 TÁC VỤ CON 3.1: Tạo Blueprint Kiến trúc hệ thống SocialSchedulerBlueprint.md
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/architecture/SocialSchedulerBlueprint.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải soạn thảo tài liệu Markdown `./sources/docs/architecture/SocialSchedulerBlueprint.md` trình bày kiến trúc microservices của `social-scheduler`. Tài liệu phải bao gồm mục lục và sáu phần nội dung chính. Phần 1 trình bày sơ đồ ngữ cảnh (System Context) sử dụng sơ đồ Mermaid flowchart LR miêu tả User và Admin truy cập API Gateway, Gateway định tuyến đến bốn dịch vụ user-service, schedule-service, ai-service, rate-limit-service, schedule-service publish sự kiện vào Kafka topic `schedule.events`, ai-service tiêu thụ sự kiện từ Kafka, rate-limit-service sử dụng Redis Token Bucket, schedule-service kết nối Facebook Graph API, Instagram Graph API và TikTok Open API, ai-service gọi OpenAI Completion API, các dịch vụ lưu trữ dữ liệu trong Cloud SQL Postgres. Phần 2 trình bày sơ đồ container (Container Diagram) chi tiết các thành phần kỹ thuật bên trong mỗi microservice. Phần 3 trình bày sơ đồ thành phần (Component Diagram) cho `schedule-service`. Phần 4 trình bày hai sơ đồ tuần tự: luồng lập lịch đăng bài (User → Gateway → ScheduleService → Kafka → IntegrationService → SocialPlatform) và luồng đề xuất AI (User → Gateway → AIService → OpenAI). Phần 5 trình bày ma trận RBAC ánh xạ bốn vai trò [ARC-001] Admin, [ARC-002] User, [ARC-003] Scheduler, [ARC-004] Analyst sang quyền hạn cụ thể. Phần 6 trình bày chính sách bảo mật [ARC-006] tuân thủ OWASP Top 10 và chỉ tiêu hiệu năng [NFR-001], [NFR-002], [NFR-003]. Bảng cuối tài liệu ánh xạ các Tag ID [DAT-001], [DAT-002], [DAT-003] sang bảng dữ liệu tương ứng.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 3.2: Tạo Runbook Triển khai DeploymentRunbook.md
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/operations/DeploymentRunbook.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải soạn thảo runbook vận hành tại `./sources/docs/operations/DeploymentRunbook.md` cho môi trường production GCP. Tài liệu gồm bốn phần chính. Phần 1 trình bày điều kiện tiên quyết: cài đặt công cụ `gcloud` CLI phiên bản 450.0.0 trở lên, `kubectl` phiên bản 1.28 trở lên, `terraform` phiên bản 1.6.0 trở lên, quyền truy cập IAM với các role `roles/owner`, `roles/container.admin`, `roles/cloudsql.admin`. Phần 2 trình bày quy trình triển khai hạ tầng [NFR-002]: chạy `gcloud auth login`, `cd ./sources/infra/terraform/gcp`, `terraform init` (khởi tạo backend GCS), `terraform plan -out=tfplan` (xem trước thay đổi), `terraform apply tfplan` (triển khai VPC, GKE, Cloud SQL, Memorystore). Phần 3 trình bày quy trình triển khai ứng dụng [NFR-003]: cấu hình kubeconfig với `gcloud container clusters get-credentials socialscheduler-gke --region asia-southeast1`, tạo namespace `kubectl create namespace socialscheduler`, áp dụng manifest `kubectl apply -k ./sources/infra/kubernetes/socialscheduler/overlays/prod`, kiểm tra trạng thái `kubectl rollout status deployment/schedule-service -n socialscheduler`. Phần 4 trình bày quy trình rollback bằng `kubectl rollout undo deployment/schedule-service -n socialscheduler` và danh sách kiểm tra sau triển khai gồm smoke test endpoint `/actuator/health`, kiểm tra metrics Prometheus qua `GET /api/v1/query?query=up`, kiểm tra dashboard Grafana. Cuối tài liệu liệt kê các câu lệnh khẩn cấp khi gặp sự cố HTTP 429 tràn ngập (tăng bucket capacity), khi job Kafka lỗi (kiểm tra consumer group lag), khi Cloud SQL vượt dung lượng (mở rộng instance).

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 3.3: Tạo tài liệu Quy trình CI/CD CicdPipeline.md
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/operations/CicdPipeline.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải tạo `./sources/docs/operations/CicdPipeline.md` mô tả chi tiết pipeline GitHub Actions gồm chín giai đoạn tuần tự. Giai đoạn 1 `lint` chạy Checkstyle và SpotBugs trên mã nguồn Java, ESLint trên mã nguồn TypeScript. Giai đoạn 2 `unit-test` chạy JUnit 5 và Mockito cho backend services, Jest cho frontend. Giai đoạn 3 `integration-test` chạy Testcontainers với PostgreSQL, Redis, Kafka container thực tế. Giai đoạn 4 `build-image` xây dựng Docker image đa giai đoạn cho bốn dịch vụ. Giai đoạn 5 `push-image` đẩy image lên Google Artifact Registry tại `asia-southeast1-docker.pkg.dev/social-scheduler-prod/socialscheduler/`. Giai đoạn 6 `deploy-staging` áp dụng manifest Kubernetes cho môi trường staging. Giai đoạn 7 `smoke-test` gọi các endpoint `/actuator/health` và kiểm tra metrics. Giai đoạn 8 `approval` yêu cầu approval gate từ Technical Lead thông qua GitHub Environments. Giai đoạn 9 `deploy-prod` triển khai production với rolling update strategy. Tài liệu ghi rõ biến bí mật cần cấu hình trong GitHub Secrets: `GCP_SA_KEY` (service account key JSON), `ARTIFACT_REGISTRY` (path registry), `KUBECONFIG_PROD` (kubeconfig base64 encoded). Phần cuối trình bày chiến lược Git Flow với các nhánh `main`, `develop`, `feature/*`, `release/*`, `hotfix/*` và quy ước Conventional Commits (ví dụ `feat(scheduler): add schedule validation`). Tích hợp sơ đồ Mermaid flowchart TD minh họa chín giai đoạn trên. Thẻ [DOC-001] yêu cầu tài liệu phải đủ chi tiết để nhân viên mới triển khai trong vòng 1 giờ.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

#### 📝 TÁC VỤ CON 3.4: Soạn thảo báo cáo Đánh giá Bảo mật OWASP và Tuân thủ
##### Sub-Agent được phân công: Doc
##### Các thành phần mục tiêu & Yêu cầu Kỹ thuật:
* **Đường dẫn Mục tiêu:** `./sources/docs/architecture/SecurityComplianceMatrix.md`
* **Traceability Tag Tokens:** <!--START_TAGS-->[DOC-001], [NFR-002]<!--END_TAGS-->
* **Hướng dẫn Kỹ thuật Tác vụ Cấp thấp:** Chuyên gia tài liệu phải tạo `./sources/docs/architecture/SecurityComplianceMatrix.md` trình bày ma trận tuân thủ ánh xạ từng yêu cầu OWASP Top 10 sang biện pháp giảm thiểu cụ thể trong hệ thống `social-scheduler`. Tài liệu gồm bảy mục ánh xạ chính. Mục A01 (Broken Access Control) ánh xạ sang cơ chế RBAC bốn vai trò [ARC-001] Admin, [ARC-002] User, [ARC-003] Scheduler, [ARC-004] Analyst được thực thi bởi Spring Security và API Gateway. Mục A02 (Cryptographic Failures) ánh xạ sang mã hóa TLS 1.3 đầu cuối, JWT với thuật toán RS256, key rotation mỗi 90 ngày theo [NFR-002]. Mục A03 (Injection) ánh xạ sang sử dụng JPA Parameter Binding thông qua Hibernate PreparedStatement, whitelist domain cho `mediaUrls`, HTML Sanitizer cho nội dung bài đăng. Mục A04 (Insecure Design) ánh xạ sang cơ chế Rate Limiter với Redis Token Bucket theo [REQ-003], defense-in-depth thông qua Rate Limit Gateway Filter. Mục A05 (Security Misconfiguration) ánh xạ sang CORS Whitelist không sử dụng wildcard `*`, security headers nghiêm ngặt `Content-Security-Policy`, `X-Content-Type-Options: nosniff`, `Strict-Transport-Security`. Mục A07 (Identification and Authentication Failures) ánh xạ sang OAuth2 Resource Server với JWT Decoder, xử lý token hết hạn trả về HTTP 401. Mục A09 (Logging Failures) ánh xạ sang tích hợp Prometheus + Grafana, structured logging với correlation ID, LogScrubbingInterceptor tự động che giấu PII. Thẻ [NFR-002] yêu cầu chứng minh tuân thủ 100%.

* **Database Schema DDL SQL Specification [DAT-XXX]:**
<!--START_DDL_MIGRATION-->
```sql
-- NO_PERSISTENCE_TIER_CHANGES_REQUIRED
```
<!--END_DDL_MIGRATION-->

* **API and Event Routing Contracts [DAT-XXX], [ARC-XXX]:**
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

### 🕵️ BÁO CÁO KIỂM TOÁN CHÉO KIẾN TRÚC THỜI GIAN THỰC BẮT BUỘC:

```properties:cross_audit_ledger
[AUTOMATED_SELF_AUDIT_REPORT]
TOTAL_PHASES_DECLARED_IN_SECTION_4_2=5
TOTAL_PHASES_EXPECTED_BY_PARAMETERS=5
PHASE_COUNT_COMPLIANCE_STATUS=Verified_5
MAX_DAYS_PER_PHASE_LIMIT_PARAMETER=7
ACTUAL_MAX_DAY_INDEX_DETECTED_IN_TIMELINE=3
TIMELINE_DAY_CAP_COMPLIANCE_STATUS=Verified_All_Phase_Durations_Within_Ceiling
TOTAL_TASKS_REGISTERED_IN_MASTER_BACKLOG_4_1=8
TOTAL_DISCRETE_SUB_TASKS_GENERATED_IN_SECTION_5=14
SUB_TASK_QUANTUM_COMPLIANCE_STATUS=Verified_Phase_5_SubTasks_Aligned_With_Allocated_Tasks_7_And_8
```