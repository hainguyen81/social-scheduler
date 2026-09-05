# Social Scheduler Enterprise Architecture Blueprint
## 🏛️ System Architecture & Engineering Specifications
- **Project Identity:** `social-scheduler` [DOC-001]
- **Base Package Path:** `org.nlh4j.socialscheduler` [DOC-001]
- **Target Documentation Path:** `./sources/docs/architecture/SocialSchedulerBlueprint.md` [DOC-001]
- **Blueprint Version:** 1.0.0 [DOC-001]
- **Traceability Baseline:** [REQ-001], [REQ-002], [REQ-003], [DAT-001], [DAT-002], [DAT-003], [EXC-001], [EXC-002], [EXC-003], [EXC-004], [EXC-005], [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006], [NFR-001], [NFR-002], [NFR-003], [DOC-001]

---

## 📑 Mục Lục (Table of Contents)
1. [Sơ đồ Ngữ cảnh Hệ thống (System Context)](#1-sơ-đồ-ngữ-cảnh-hệ-thống-system-context) [DOC-001]
2. [Sơ đồ Container (Container Diagram)](#2-sơ-đồ-container-container-diagram) [DOC-001]
3. [Sơ đồ Thành phần (Component Diagram - Schedule Service)](#3-sơ-đồ-thành-phần-component-diagram---schedule-service) [DOC-001]
4. [Sơ đồ Tuần tự Nghiệp vụ (Sequence Diagrams)](#4-sơ-đồ-tuần-tự-nghiệp-vụ-sequence-diagrams) [DOC-001]
5. [Ma trận Phân quyền RBAC & Bảo mật OWASP](#5-ma-trận-phân-quyền-rbac--bảo-mật-owasp) [DOC-001]
6. [Chỉ tiêu Hiệu năng, Phi chức năng & Ma trận Truy vết Tag ID](#6-chỉ-tiêu-hiệu-năng-phi-chức-năng--ma-trận-truy-vết-tag-id) [DOC-001]

---

## 1. Sơ đồ Ngữ cảnh Hệ thống (System Context)

Hệ thống **social-scheduler** được thiết kế theo kiến trúc Microservices hướng sự kiện (Event-Driven Microservices), tách biệt hoàn toàn giữa các bounded context nghiệp vụ nhằm đảm bảo khả năng mở rộng ngang (horizontal scaling), độ cô lập cao và khả năng chịu lỗi tối ưu [ARC-000], [NFR-003].

### 🌐 Mô hình Tương tác Ngữ cảnh Cấp cao (System Context Flowchart) [DOC-001]

Người dùng doanh nghiệp (`User`) và quản trị viên (`Admin`) tương tác với hệ thống thông qua giao thức an toàn TLS 1.3 tới API Gateway trung tâm. API Gateway chịu trách nhiệm giải mã token JWT, phân quyền RBAC, kiểm tra hạn mức qua `rate-limit-service`, sau đó định tuyến lưu lượng vào các microservice chuyên trách. Dịch vụ lập lịch `schedule-service` giao tiếp bất đồng bộ qua Kafka broker, tích hợp trực tiếp với SDK Facebook, Instagram, TikTok. Dịch vụ đề xuất `ai-service` tiêu thụ số liệu hiệu suất lịch sử và tích hợp mô hình OpenAI để cung cấp nội dung thông minh.