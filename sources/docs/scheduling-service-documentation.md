# 📄 Technical Documentation – Scheduling Service
**File Path:** `./sources/docs/scheduling-service-documentation.md`  
**Version:** 1.0 (Cơ sở)  
**Date:** 2026/09/12 13:29:15  
**Author:** Enterprise System Architect (SA Agent)  
**Approved:** Chờ phê duyệt quản trị kỹ thuật  

## 🎯 1. Tổng quan về Dịch vụ

### 1.1 Mục đích & Phạm vi
Dịch vụ lên lịch (`scheduling-service`) cung cấp các chức năng cốt lõi để quản lý các công việc đăng bài trên các nền tảng mạng xã hội (Facebook, Instagram, TikTok). Nó tuân thủ các yêu cầu nghiệp vụ chính sau:

- **[REQ-001]** Tích hợp lịch đăng bài tự động cho Facebook, Instagram và TikTok.  
- **[EXC-001]** Xử lý các ngoại lệ liên quan đến lỗi định dạng lịch hoặc xung đột thời gian.  
- **[EXC-002]** Quản lý các lỗi liên quan đến quyền truy cập người dùng hoặc xác thực nền tảng.  

### 1.2 Kiến trúc Hệ thống
Dịch vụ này là một phần của kiến trúc **microservices** đa tenant, sử dụng **Apache Kafka** để phát sóng các sự kiện như `SCHEDULE_CREATED`, `SCHEDULE_UPDATED`, `SCHEDULE_PROCESSED`. Nó tuân thủ mô hình **CQRS** và **Event-Driven** với các thành phần chính sau:

- **Entity:** `Schedule` (lịch đăng bài) – được lưu trữ trong PostgreSQL.  
- **Service Layer:** `ScheduleService` – chứa logic nghiệp vụ và xử lý sự kiện.  
- **API Layer:** `ScheduleController` – các điểm cuối REST tuân thủ chuẩn **OpenAPI 3.0**.  
- **Lớp xử lý ngoại lệ:** `ScheduleExceptionHandler` – chuẩn hóa lỗi và ghi log với các Tag ID theo dõi.  

### 1.3 Ma trận Theo dõi
| Module | TagID Mục tiêu |
|--------|--------------|
| Lớp Schedule (Domain Model) | `[REQ-001]` |
| Lớp ScheduleService (Logic Nghiệp vụ) | `[REQ-001], [EXC-001], [EXC-002]` |
| Lớp ScheduleController (API) | `[REQ-001]` |
| Lớp ScheduleExceptionHandler (Xử lý Ngoại lệ) | `[EXC-001], [EXC-002]` |

---

## 🗺️ 2. Sơ đồ Kiến trúc (Mermaid)