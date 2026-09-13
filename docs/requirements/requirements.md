# TÀI LIỆU YÊU CẦU PHẦN MỀM: social-scheduler

## 📊 Quản lý tài liệu

| Mục | Chi tiết |
| :--- | :--- |
| **Mã SRS** | SRS-20260913144443 |
| **Tên dự án** | social-scheduler |
| **Phiên bản** | 1.0 (Cơ sở) |
| **Ngày giờ** | 2026/09/13 14:44:43 |
| **Tác giả** | Chuyên viên phân tích nghiệp vụ (BA) / Chiến lược sản phẩm (BA Agent) |
| **Duyệt** | Đang chờ xem xét quản trị kỹ thuật |

## 1. TỔNG QUAN DỰ ÁN & KIẾN TRÚC TOÀN CẦU

### 1.1 Mục tiêu sản phẩm & Giá trị cốt lõi
- Tự động hóa lịch đăng bài trên mạng xã hội cho doanh nghiệp nhỏ
- Đề xuất nội dung bài đăng thông minh bằng AI
- Xuất bản đa nền tảng (Facebook, Instagram, TikTok)

### 1.2 Đối tượng mục tiêu
- Chủ doanh nghiệp nhỏ
- Quản lý tiếp thị
- Chuyên gia marketing tự do

### 1.3 Ma trận kiểm soát truy cập dựa trên vai trò (RBAC) toàn cầu
- **[ARC-001]** Chủ doanh nghiệp: Quản lý tài khoản, xem báo cáo hiệu suất
- **[ARC-002]** Quản lý tiếp thị: Tạo và quản lý lịch đăng bài, xem báo cáo
- **[ARC-003]** Chuyên gia marketing: Tạo nội dung, xem báo cáo

### 1.4 Ràng buộc công nghệ & Sơ đồ cơ sở hạ tầng toàn cầu
- **[ARC-004]** Sử dụng Quarkus cho backend (Java)
- **[ARC-005]** Sử dụng Next.js cho giao diện quản trị
- **[ARC-006]** Sử dụng React Native cho ứng dụng di động
- **[ARC-007]** Sử dụng PostgreSQL cho cơ sở dữ liệu
- **[ARC-008]** Sử dụng Apache Kafka cho xử lý sự kiện thời gian thực
- **[ARC-009]** Sử dụng Firebase Hosting cho triển khai frontend

## 2. CÁC MÔ-ĐUN CHÍNH MỞ RỘNG

### 2.1 Tích hợp API lịch đăng bài tự động

#### 2.1.1 Yêu cầu chức năng cốt lõi
- **[REQ-001]** Tích hợp API lịch đăng bài tự động cho Facebook, Instagram và TikTok

#### 2.1.2 Tiêu chí chấp nhận & Tương tác
- **Cho [REQ-001]**
  - **Cho** một người dùng đã xác thực
  - **Khi** họ chọn nền tảng và nhập nội dung
  - **Thì** hệ thống sẽ lưu lịch đăng bài và lên lịch xuất bản

#### 2.1.3 Luồng ngoại lệ mô-đun
- **[EXC-001]** Xử lý ngoại lệ khi API bên thứ ba trả về lỗi; ghi lại và thử lại sau
- **[EXC-002]** Xác thực quyền truy cập người dùng và xử lý trường hợp token hết hạn
- **[EXC-003]** Bảo vệ chống lại việc spam lịch đăng bài và tấn công flood comment

#### 2.1.4 Từ điển dữ liệu mô-đun
- **[DAT-001]** Tạo bảng lưu trữ lịch đăng bài: id, user_id, platform, content, scheduled_time, status

### 2.2 Triển khai mô hình học máy để đề xuất nội dung

#### 2.2.1 Yêu cầu chức năng cốt lõi
- **[REQ-002]** Triển khai mô hình học máy để đề xuất nội dung bài đăng dựa trên hiệu suất trước đó

#### 2.2.2 Tiêu chí chấp nhận & Tương tác
- **Cho [REQ-002]**
  - **Cho** một người dùng đã đăng bài trước đó
  - **Khi** họ yêu cầu đề xuất nội dung mới
  - **Thì** hệ thống sẽ phân tích hiệu suất bài đăng trước đó và đề xuất nội dung mới

#### 2.2.3 Luồng ngoại lệ mô-đun
- **[EXC-004]** Xử lý ngoại lệ khi mô hình học máy không có đủ dữ liệu để đề xuất
- **[EXC-005]** Xác thực đầu vào dữ liệu và kiểm tra giới hạn tỷ lệ cho từng người dùng

#### 2.2.4 Từ điển dữ liệu mô-đun
- **[DAT-002]** Tạo bảng hiệu suất bài đăng: id, post_id, metric_likes, metric_comments, metric_shares, collected_at

## 3. YÊU CẦU KHÔNG CHỨC NĂNG TOÀN CẦU

- **[NFR-001]** Hiệu suất: Thời gian phản hồi dưới 2 giây cho các yêu cầu API
- **[NFR-002]** Bảo mật: Mã hóa dữ liệu sử dụng AES-256, xác thực JWT/OAuth2
- **[NFR-003]** Khả năng mở rộng: Hỗ trợ 10,000 người dùng đồng thời
- **[NFR-004]** Sẵn sàng cao: 99.9% thời gian hoạt động
- **[NFR-005]** Độc lập dữ liệu: Bảo vệ dữ liệu người dùng bằng cách sử dụng phân vùng cơ sở dữ liệu