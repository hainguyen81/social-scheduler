```markdown
# Deployment Runbook - Social Scheduler Production GCP

## 📋 TRACEABILITY MATRIX REFERENCE
| Section | Targeted Tag IDs |
| :--- | :--- |
| Prerequisites & Tooling | [DOC-001] |
| Infrastructure Provisioning (Terraform) | [NFR-002], [DOC-001] |
| Application Deployment (Kubernetes) | [NFR-003], [DOC-001] |
| Rollback & Post-Deployment Verification | [NFR-003], [DOC-001] |
| Emergency Commands & Incident Response | [NFR-001], [NFR-002], [DOC-001] |

---

## 🏗️ PHẦN 1: ĐIỀU KIỆN TIẾN QUYỀN & CÔNG CỤ

### 1.1. Phiên bản công cụ bắt buộc
- **gcloud CLI**: Phiên bản `450.0.0` trở lên. Yêu cầu cài đặt và xác thực Google Cloud Account: `gcloud auth login`.
- **kubectl**: Phiên bản `1.28` trở lên. Phải khớp với version GKE cluster.
- **Terraform**: Phiên bản `1.6.0` trở lên. Yêu cầu cài đặt binary `terraform` trên workstation.
- **Quyền truy cập IAM**: Cần gán các role sau cho tài khoản thực hiện deploy:
  - `roles/owner` (hoặc `roles/container.admin` cho GKE operations)
  - `roles/cloudsql.admin` (quản lý Cloud SQL instances)
  - `roles/redis.admin` (quản lý Memorystore instances)

### 1.2. Kiểm tra môi trường
```bash
# Kiểm tra phiên bản gcloud
gcloud version

# Kiểm tra phiên bản kubectl
kubectl version --client

# Kiểm tra phiên bản terraform
terraform version
```

### 1.3. Cấu hình xác thực
```bash
# Đăng nhập Google Cloud
gcloud auth login

# Đặt project mặc định (thay social-scheduler-prod bằng project thực tế)
gcloud config set project social-scheduler-prod

# Cấu hình region mặc định
gcloud config set region asia-southeast1
```

---

## 🏗️ PHẦN 2: TRIỂN KHAI HẬU CẤU HÌNH TERRAFORM [NFR-002], [DOC-001]

### 2.1. Khởi tạo backend Terraform (GCS Bucket)
```bash
# Di chuyển vào thư mục terraform GCP
cd ./sources/infra/terraform/gcp

# Khởi tạo backend Terraform cho GCS bucket (socialscheduler-tfstate đã được tạo sẵn)
terraform init \
  -backend-config="bucket=socialscheduler-tfstate" \
  -backend-config="prefix=terraform/state/prod"
```

### 2.2. Xem trước thay đổi (Plan)
```bash
# Tạo file tfplan với tên mô tả
terraform plan -out=tfplan

# Hoặc xem preview trực tiếp qua UI (nếu có enable)
terraform plan
```

### 2.3. Triển khai hạ tầng (Apply)
```bash
# Áp dụng thay đổi đã được kiểm tra
terraform apply tfplan
```

**Kết quả sau khi apply xong:**
- VPC với CIDR `10.10.0.0/16` và 3 subnets vùng `asia-southeast1`
- GKE Cluster Autopilot bật Workload Identity, Shielded Nodes
- Cloud SQL PostgreSQL instance với schema-per-tenant
- Memorystore Redis instance cho Token Bucket Rate Limiter
- Cloud Router + Cloud NAT cho egress an toàn

### 2.4. Xác minh tài nguyên đã tạo
```bash
# Liệt kê VPC
gcloud compute networks list

# Liệt kê GKE clusters
gcloud container clusters list

# Liệt kê Cloud SQL instances
gcloud sql instances list

# Liệt kê Memorystore instances
gcloud redis instances list
```

---

## 📦 PHẦN 3: TRIỂN KHAI ỨNG DỤNG KUBERNETES [NFR-003], [DOC-001]

### 3.1. Cấu hình kubeconfig
```bash
# Lấy credentials cho GKE cluster (thay socialscheduler-gke bằng tên cluster thực tế)
gcloud container clusters get-credentials socialscheduler-gke --region asia-southeast1

# Kiểm tra kết nối
kubectl cluster-info
```

### 3.2. Tạo namespace cho môi trường sản xuất
```bash
# Tạo namespace socialscheduler nếu chưa tồn tại
kubectl create namespace socialscheduler

# Kiểm tra namespace
kubectl get namespaces
```

### 3.3. Áp dụng manifest Kubernetes (Overlays Production)
```bash
# Áp dụng toàn bộ manifest từ overlays production
kubectl apply -k ./sources/infra/kubernetes/socialscheduler/overlays/prod
```

**Danh sách tài nguyên sẽ được tạo:**
- Deployment cho 4 microservice (user-service, schedule-service, ai-service, rate-limit-service)
- Service (ClusterIP cho internal, NodePort cho metrics)
- HorizontalPodAutoscaler (HPA) cho từng deployment
- Ingress với TLS termination
- ConfigMap chứa biến môi trường runtime
- Secret chứa khóa bí mật (JWT, API keys)

### 3.4. Theo dõi trạng thái rollout
```bash
# Kiểm tra trạng thái rollout cho schedule-service
kubectl rollout status deployment/schedule-service -n socialscheduler

# Kiểm tra trạng thái rollout cho tất cả deployment
kubectl get pods -n socialscheduler -l app in (user-service, schedule-service, ai-service, rate-limit-service)

# Xem logs từ pod đầu tiên
kubectl logs -n socialscheduler $(kubectl get pods -n socialscheduler -l app=schedule-service -o jsonpath='{.items[0].metadata.name}')
```

---

## 🔄 PHẦN 4: QUY TRÌNH ROLLBACK & KIỂM TRA SAU TRIỂN KHAI

### 4.1. Rollback deployment
```bash
# Rollback về revision trước cho schedule-service
kubectl rollout undo deployment/schedule-service -n socialscheduler

# Rollback về revision cụ thể (ví dụ revision 3)
kubectl rollout undo deployment/schedule-service -n socialscheduler --to-revision=3

# Kiểm tra lại trạng thái sau rollback
kubectl rollout status deployment/schedule-service -n socialscheduler
```

### 4.2. Danh sách kiểm tra sau triển khai (Post-Deployment Verification)

#### 4.2.1. Smoke test endpoint sức khỏe
```bash
# Test endpoint health cho schedule-service
curl -s http://api.socialscheduler.local/actuator/health

# Kết quả mong đợi: {"status":"UP", "components": {...}}

# Test health chi tiết cho từng service
kubectl port-forward -n socialscheduler svc/schedule-service 8082:8082 &
curl -s http://localhost:8082/actuator/health
```

#### 4.2.2. Kiểm tra metrics Prometheus
```bash
# Query tổng quan về trạng thái hệ thống
curl -s "http://prometheus.observability.svc.cluster.local:9090/api/v1/query?query=up"

# Query latency P95 theo service
curl -s "http://prometheus.observability.svc.cluster.local:9090/api/v1/query?query=histogram_quantile(0.95, sum by (le, service) (rate(http_server_requests_seconds_bucket{namespace=\"socialscheduler\"}[5m])))"

# Query tỷ lệ request bị rate limit (HTTP 429)
curl -s "http://prometheus.observability.svc.cluster.local:9090/api/v1/query?query=sum(rate(http_server_requests_seconds_count{namespace=\"socialscheduler\", status=\"429\"}[5m]))"
```

#### 4.2.3. Kiểm tra dashboard Grafana
```bash
# Import dashboard socialscheduler-overview.json qua Grafana UI
# Hoặc query trực tiếp API
curl -s "http://grafana.socialscheduler.local/api/dashboards/uid/socialscheduler-overview"

# Kiểm tra các panel chính:
# - HTTP Request Latency P95 (ms) - ngưỡng cảnh báo tại 200ms
# - Rate Limited Requests (HTTP 429)
# - CPU Usage per Pod
# - Job Kafka fail count
```

### 4.3. Quy trình rollback khi gặp sự cố

#### 4.3.1. Rollback do deployment lỗi
```bash
# Nếu có lỗi sau khi deploy, thực hiện rollback tức thì
kubectl rollout undo deployment/schedule-service -n socialscheduler

# Hoặc quay về revision cụ thể
kubectl rollout undo deployment/schedule-service -n socialscheduler --to-revision=2
```

#### 4.3.2. Khẩn cấp - HTTP 429 Rate Limit Tràn Ngập
```bash
# Tăng capacity cho Redis Token Bucket (temporary fix)
# Cách 1: Thêm token thông qua Redis CLI
redis-cli -h redis-master.redis.svc.cluster.local SETNX rate_limit:{userId}:{endpoint} 200

# Cách 2: Temporarily tăng burst size trong code (hotfix)
# Cậpật parameter bucker4j.config.burstTokens trong application-docker.yml
# Hoặc override thông qua environment variable: REDIS_BURST_TOKENS=500

# Kiểm tra consumer group lag cho Kafka
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=kafka -o jsonpath='{.items[0].metadata.name}') -- kafka-consumer-groups --bootstrap-server kafka:9092 --describe
```

#### 4.3.3. Khẩn cấp - Job Kafka Lỗi
```bash
# Restart Kafka consumer group
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=kafka -o jsonpath='{.items[0].metadata.name}') -- \
  kafka-consumer-groups --bootstrap-server kafka:9092 --group social-scheduler-consumer --reset-offsets --to-earliest --execute

# Hoặc restart deployment kafka-consumer
kubectl rollout restart deployment/kafka-consumer -n socialscheduler

# Kiểm tra logs Kafka
kubectl logs -n socialscheduler $(kubectl get pods -n socialscheduler -l app=kafka -o jsonpath='{.items[0].metadata.name}') | tail -100
```

#### 4.3.4. Khẩn cấp - Cloud SQL vượt dung lượng
```bash
# Kiểm tra dung lượng instance
gcloud sql instances describe socialscheduler-db --format="value(currentDiskSize, diskSize, storageAutoResize)

# Mở rộng instance tự động (nếu đã bật)
gcloud sql instances patch socialscheduler-db --disk-size=500GB

# Mở rộng instance thủ công với storage auto-resize
gcloud sql instances patch socialscheduler-db --disk-auto-resize-size=100GB

# Tạo snapshot backup trước khi thay đổi
gcloud sql backups create socialscheduler-db --snapshot-name="pre-resize-$(date +%Y%m%d%H%M%S)"
```

---

## 🚨 PHẦN 5: CÂU LỆNH KHÁN CẤP & PHÁT HIỆN SỰ CỐ

### 5.1. HTTP 429 Rate Limit Tràn Ngập (Emergency Response)
```bash
# 1. Tăng burst capacity Redis tạm thời
export REDIS_BURST_TOKENS=1000
# Hoặc qua Redis CLI
redis-cli -h redis-master.redis.svc.cluster.local CONFIG SET maxclients 1000

# 2. Kiểm tra consumer lag Kafka
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=kafka) -- \
  kafka-consumer-groups --bootstrap-server kafka:9092 --describe

# 3. Phát hiện tenant nào đang lạm dụng
# Sử dụng Prometheus query
curl -s "http://prometheus.observability.svc.cluster.local:9090/api/v1/query?query=rate(rate_limits_requests_total{namespace=\"socialscheduler\"}[5m])"

# 4. Temporarily disable rate limiter cho tenant cụ thể (hotfix)
# Cậpật flag trong Redis hoặc disable endpoint feature flag
```

### 5.2. Phát hiện và khắc phục Database Connection Pool Exhausted
```bash
# Kiểm tra connection pool status
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=user-service) -- \
  curl -s http://localhost:8081/actuator/hikarimetrics | grep -E "activeConnections|idleConnections|pendingConnections"

# Tăng max pool size nếu cần
# Cậpật spring.datasource.hikari.maximum-pool-size trong ConfigMap hoặc Secret

# Restart service nếu pool bị block
kubectl rollout restart deployment/user-service -n socialscheduler
```

### 5.3. Phát hiện và khắc phục OpenAI API Failure
```bash
# Kiểm tra circuit breaker status
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=ai-service) -- \
  curl -s http://localhost:8083/actuator/circuitbreaker

# Xem metrics Resilience4j
curl -s "http://prometheus.observability.svc.cluster.local:9090/api/v1/query?query=resilience4j_circuitbreaker_state{service=\"ai-service\"}"

# Fallback content sẽ được tự động cung cấp khi circuit breaker mở
# Monitor logs cho fallback events
kubectl logs -n socialscheduler $(kubectl get pods -n socialscheduler -l app=ai-service) | grep -i "fallback"
```

### 5.4. Khôi phục dữ liệu sau sự cố lớn
```bash
# Restore Cloud SQL từ backup
gcloud sql backups restore socialscheduler-db --backup-id="20260901_000001"

# Restore Memorystore từ snapshot
gcloud redis backups create redis-master.redis.svc.cluster.local --snapshot-name="emergency-snapshot-$(date +%Y%m%d%H%M%S)"

# Verify data integrity
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=user-service) -- \
  psql -U postgres -d user_schema -c "SELECT count(*) FROM users;"

# Kiểm tra consistency giữa các schema
kubectl exec -n socialscheduler $(kubectl get pods -n socialscheduler -l app=schedule-service) -- \
  psql -U postgres -d schedule_schema -c "SELECT count(*) FROM schedules WHERE status='PENDING';"
```

---

## 📊 PHẦN 6: BẢNG ÁNH XÁP TRUY VẤT TAG ID [DOC-001]

| Mã đoạn tài liệu | Tag ID liên kết | Mô tả |
| :--- | :--- | :--- |
| Phần 1: Điều kiện tiên quyết | [DOC-001] | Yêu cầu cài đặt công cụ và quyền IAM |
| Phần 2: Terraform provisioning | [NFR-002], [DOC-001] | Triển khai VPC, GKE, Cloud SQL, Memorystore |
| Phần 3: Kubernetes deployment | [NFR-003], [DOC-001] | Manifest apply, rollout status, namespace |
| Phần 4: Rollback & verification | [NFR-003], [DOC-001] | kubectl rollout undo, smoke test, metrics |
| Phần 5: Emergency commands | [NFR-001], [NFR-002], [DOC-001] | HTTP 429, Kafka errors, Cloud SQL capacity |
| Traceability Matrix Reference | [DOC-001] | Ánh xáp toàn bộ sections về DOC-001 |

---

## 📝 KẾT LUẬN & TÀI LIỆU THÊM

Runbook này được tạo dựa trên kiến trúc microservices `social-scheduler` phiên bản 1.0, tuân thủ các tiêu chí:
- **NFR-001**: Độ trễ dưới 200ms cho tác vụ lên lịch, footprint container tối thiểu
- **NFR-002**: Bảo mật OWASP, mã hóa TLS, VPC isolation, least privilege access
- **NFR-003**: Multi-tenancy schema-per-tenant, HPA scaling, High Availability
- **ARC-001** đến **ARC-006**: RBAC 4 vai trò, JWT authentication, CORS whitelist, Log scrubbing
- **DOC-001**: Tài liệu vận hành đầy đủ, traceability mapping, CI/CD pipeline

Tất cả các lệnh và cấu hình trong tài liệu này phải được test trong môi trường staging trước khi áp dụng vào production. Lưu ý luôn biến môi trường `SPRING_PROFILES_ACTIVE=docker` khi chạy local và `SPRING_PROFILES_ACTIVE=prod` khi deploy qua CI/CD pipeline.

*Runbook phiên bản 1.0 - Cập nhật lần cuối: 2026/08/31 15:13:55*
*Tác giả: Enterprise System Architect*
*Tag truy vết: [DOC-001]*
```