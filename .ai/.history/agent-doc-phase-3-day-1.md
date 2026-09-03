# Day 1: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/docs/api/RecommendationApiContract.yaml
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DOCUMENT MATRIX INJECTION
*   Target Project Identity Safe Name: 
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Documentation Destination Path: `./sources/docs/api/RecommendationApiContract.yaml`




*   Documentation Context: Conceptual Init (Synthesize the architecture, guidelines, or specs based purely on the execution sub-tasks blueprint.)


### 📋 EXECUTION SUB-TASKS & DOCUMENT CONTENT TO WRITE
['Tạo tệp OpenAPI 3.0 chuẩn YAML tại ./sources/docs/api/RecommendationApiContract.yaml mô tả đầy đủ hai endpoint POST /api/v1/ai/recommendations và GET /api/v1/ai/recommendations/health. Khai báo thông tin metadata: openapi: 3.0.3, info: {title: "Social Scheduler - AI Recommendation API Contract", version: "1.0.0", description: "Microservice cung cấp nội dung bài đăng được cá nhân hóa thông qua OpenAI Completion API kết hợp phân tích hiệu suất lịch sử"}. Định nghĩa components.securitySchemes.bearerAuth với type: http, scheme: bearer, bearerFormat: JWT, kèm description: "JWT Bearer token chứa claim \'roles\' để phân quyền RBAC 4 vai trò". Khai báo components.schemas cho các schema: RecommendationRequest (userId UUID required, platform enum required, topic string 1-500 required, tone enum optional default PROFESSIONAL, maxLength integer 100-3000 optional), RecommendationResponse (recommendationId UUID, userId UUID, platform string, content string, confidenceScore number 0.0-1.0, isFallback boolean, generatedAt date-time), ErrorResponse (errorCode string, message string, timestamp date-time, correlationId UUID), ValidationErrorResponse (errorCode string, message string, fieldErrors array of objects), PlatformEnum (FACEBOOK, INSTAGRAM, TIKTOK), ToneEnum (PROFESSIONAL, CASUAL, HUMOROUS, INSPIRATIONAL). Định nghĩa paths./api/v1/ai/recommendations.post với: summary: "Tạo đề xuất nội dung bài đăng", tags: ["AI Recommendations"], security: [{bearerAuth: []}], requestBody tham chiếu RecommendationRequest, responses bao gồm 200 (Success), 401 (Unauthorized - TOKEN_EXPIRED), 403 (Forbidden - INSUFFICIENT_ROLE), 422 (Unprocessable Entity - VALIDATION_FAILED), 429 (Too Many Requests - RATE_LIMIT_EXCEEDED), 500 (Internal Server Error), 503 (Service Unavailable - AI_SERVICE_UNAVAILABLE). Định nghĩa paths./api/v1/ai/recommendations/health.get với summary: "Kiểm tra tình trạng dịch vụ AI" trả về 200 với schema chứa status, service, version. Bổ sung examples cho mỗi trường hợp request và response thành công/thất bại. Sử dụng $ref để tái sử dụng schema giữa các endpoint. Tham chiếu ma trận phân quyền RBAC trong info.description hoặc extension field x-rbac-matrix. Đảm bảo tài liệu tuân thủ cấu trúc YAML chuẩn OpenAPI 3.0.3 và sử dụng nullable: true cho các trường optional.']

---

### ⚙️ TECHNICAL WRITER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder guardrails, and detailed sub-task document specifications provided above.


Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[DOC-001]']` variable, and permanently burn those codes into the generated documentation layout (inside the table columns or the dedicated Traceability Matrix Reference section). Emit the final production-ready comprehensive documentation inside a single valid markdown code block (` ```markdown `) now.
* **📝 Response**:
```markdown
# 📋 **Enterprise System Documentation – Social Scheduler Blueprint**

## 📊 **Document Control**

| Item | Details |
| :--- | :--- |
| **Blueprint ID** | ARCH-20260831151355 |
| **Project Name** | social-scheduler |
| **Version** | 1 .0 (Cơ sở) |
| **Date Time** | 2026/08/31 15:13:55 |
| **Author** | Enterprise System Architect (SA Agent) |
| **Approval** | Chờ phê duyệt quản trị kỹ thuật |

## 📊 **1. TỔNG QUAN HỆ THỐNG & MÔ HÌNH KIẾN TRÚC CỐT LÕI**

### ⚙️ **1.1. Mô hình hệ thống cốt lõi & Kiến trúc điều phối**

- Kiến trúc tổng thể theo mô hình **Microservices hướng sự kiện (Event-Driven)** kết hợp **CQRS** trên nền **Reactive** để xử lý khối lượng đăng bài lớn theo thời gian thực.
- Hệ thống được phân tách thành các bounded context nghiệp vụ độc lập: `user-service`, `scheduler-service`, `content-service`, `integration-service`, `analytics-service`, `notification-service`, `auth-service` và một API Gateway trung tâm.
- Lớp truy cập dữ liệu sử dụng **PostgreSQL** phân vùng theo schema, tích hợp **Redis** cho caching phiên và giới hạn tỷ lệ; giao tiếp bất đồng bộ qua **Apache Kafka** với các topic chuyên dụng theo miền nghiệp vụ.
- Mô hình AI/ML được đóng gói như một microservice độc lập (`ai-recommender-service`) giao tiếp qua Kafka, cho phép tách biệt hoàn toàn giữa workload

