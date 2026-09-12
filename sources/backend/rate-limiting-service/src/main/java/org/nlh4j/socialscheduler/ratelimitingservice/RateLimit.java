package org.nlh4j.socialscheduler.ratelimitingservice;

import java.util.UUID;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lớp đại diện cho bản ghi giới hạn tỷ lệ cho người dùng.
 * <p>
 * Lớp này lưu trữ các thông tin cần thiết để thực hiện kiểm tra giới hạn tỷ lệ dựa trên
 * điểm cuối API, ID người dùng và cửa sổ thời gian. Tất cả các giá trị cấu hình
 * (ví dụ: độ dài cửa sổ mặc định, ngưỡng yêu cầu tối đa) được định nghĩa dưới dạng
 * các hằng số lớp để đảm bảo tính bất biến và dễ bảo trì.
 * </p>
 *
 * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
 */
@Entity
@Table(name = "rate_limits")
public class RateLimit {

    /* ==================== CẤU HÌNH HỆ THỐNG VÀ HẰNG SỐ TOÀN CẦU ==================== */

    /** Ngưỡng cửa sổ mặc định tính bằng giây cho việc làm mới giới hạn tỷ lệ */
    public static final int DEFAULT_WINDOW_SECONDS = 60;

    /** Ngưỡng yêu cầu tối đa mặc định cho mỗi cửa sổ */
    public static final int DEFAULT_MAX_REQUESTS = 100;

    /* ==================== TRƯỜNG DỮ LIỆU CỐT LÕI ==================== */

    /**
     * ID duy nhất của bản ghi giới hạn tỷ lệ.
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rate_limit_id", nullable = false, updatable = false)
    private UUID rateLimitId;

    /**
     * ID người dùng bị áp dụng giới hạn.
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Điểm cuối API bị giới hạn.
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Column(name = "endpoint", nullable = false, length = 255)
    private String endpoint;

    /**
     * Số lần yêu cầu trong cửa sổ hiện tại.
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Column(name = "request_count", nullable = false)
    private int requestCount;

    /**
     * Thời điểm bắt đầu cửa sổ giới hạn (milliseconds UTC).
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Column(name = "window_start", nullable = false)
    private long windowStart;

    /**
     * Thời điểm kết thúc cửa sổ giới hạn (milliseconds UTC).
     *
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    @Column(name = "window_end", nullable = false)
    private long windowEnd;

    /* ==================== LOGGER VÀ HỖ TRỢ DOANH NGHIỆP ==================== */

    /** Logger chuẩn doanh nghiệp cho lớp này – được sử dụng cho mọi tương tác vào/ra. */
    private static final Logger logger = LoggerFactory.getLogger(RateLimit.class);

    /* ==================== HÀM XỬ LÝ NGOẠI LỆ TÙY CHỈNH ==================== */

    /**
     * Ngoại lệ được ném khi số lần yêu cầu vượt quá ngưỡng giới hạn được phép.
     * <p>
     * Ngoại lệ này được đánh dấu với các thẻ truy xuất dấu vết doanh nghiệp liên quan đến
     * các yêu cầu xác thực ([REQ-003]) và các kịch bản lỗi giới hạn tỷ lệ
     * ([EXC-002], [EXC-003], [EXC-005]).
     * </p>
     *
     * @traceability [EXC-002], [EXC-003], [EXC-005]
     */
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class RateLimitExceededException extends RuntimeException {
        private final RateLimit rateLimit;

        public RateLimitExceededException(String message, RateLimit rateLimit) {
            super(message);
            this.rateLimit = rateLimit;
        }

        public RateLimit getRateLimit() {
            return rateLimit;
        }
    }

    /* ==================== HÀM XUẤT NHẬP ==================== */

    // Constructors ---------------------------------------------------------

    public RateLimit() {
        // Khởi tạo mặc định cho JPA; logger được khởi tạo bởi lớp cơ sở
    }

    public RateLimit(UUID userId, String endpoint, int requestCount, long windowStart, long windowEnd) {
        this.userId = userId;
        this.endpoint = endpoint;
        this.requestCount = requestCount;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        logger.info("[INIT] Khởi tạo RateLimit cho userId={} endpoint={}", userId, endpoint);
    }

    // Getter và Setter -----------------------------------------------------

    public UUID getRateLimitId() {
        return rateLimitId;
    }

    public void setRateLimitId(UUID rateLimitId) {
        this.rateLimitId = rateLimitId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(long windowStart) {
        this.windowStart = windowStart;
    }

    public long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(long windowEnd) {
        this.windowEnd = windowEnd;
    }

    /* ==================== LOGIC NGHIỆP VỤ ==================== */

    /**
     * Kiểm tra xem yêu cầu hiện tại có nằm trong ngưỡng giới hạn được phép hay không.
     * <p>
     * Phương thức này thực hiện kiểm tra giới hạn một cách an toàn, ghi log chi tiết
     * cho từng trạng thái (vào, thành công, vi phạm) và ném {@link RateLimitExceededException}
     * khi vượt quá ngưỡng. Tất cả các ngoại lệ được bắt và ghi log với các thẻ
     * dấu vết cần thiết ([EXC-002], [EXC-003], [EXC-005]) để đảm bảo khả năng kiểm toán.
     * </p>
     *
     * @param maxRequests ngưỡng tối đa được phép trong cửa sổ hiện tại
     * @return {@code true} nếu {@code requestCount <= maxRequests}; ngược lại {@code false}
     * @throws RateLimitExceededException khi vượt quá giới hạn được phép
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    public boolean isWithinLimit(int maxRequests) {
        logger.info("[ENTRY] Kiểm tra giới hạn tỷ lệ cho userId={} endpoint={} requestCount={}", userId, endpoint, requestCount);
        try {
            if (requestCount > maxRequests) {
                String violationMsg = String.format("Vượt quá giới hạn tỷ lệ cho endpoint %s (yêu cầu: %d, ngưỡng: %d)",
                        endpoint, requestCount, maxRequests);
                logger.warn("[VIOLATION] {}", violationMsg);
                // Ném ngoại lệ tùy chỉnh để tuân thủ các yêu cầu xác thực và xử lý lỗi
                throw new RateLimitExceededException(violationMsg, this);
            }
            logger.debug("[SUCCESS] Giới hạn tỷ lệ hợp lệ cho userId={} endpoint={}", userId, endpoint);
            return true;
        } catch (RateLimitExceededException e) {
            // Bắt và ghi log ngoại lệ theo yêu cầu [EXC-005] để đảm bảo chuỗi nguyên nhân được giữ nguyên
            logger.error("[CRITICAL FAIL] [EXC-005] RateLimit.isWithinLimit phát sinh lỗi giới hạn tỷ lệ. Nguyên nhân thô: {}", e.getMessage(), e);
            throw e;
        } finally {
            logger.info("[EXIT] Kết thúc kiểm tra giới hạn tỷ lệ cho userId={} endpoint={}", userId, endpoint);
        }
    }

    /**
     * Đặt lại cửa sổ giới hạn tỷ lệ – đặt lại số lần yêu cầu về 0 và tính toán
     * lại thời điểm bắt đầu/kết thúc dựa trên độ dài cửa sổ được cung cấp.
     * <p>
     * Phương thức này bao gồm xử lý ngoại lệ toàn diện để ghi log mọi
     * sự cố không mong muốn với các thẻ dấu vết phù hợp ([EXC-002], [EXC-003], [EXC-005]).
     * </p>
     *
     * @param windowSeconds độ dài cửa sổ tính bằng giây
     * @traceability [REQ-003], [EXC-002], [EXC-003], [EXC-005]
     */
    public void resetWindow(int windowSeconds) {
        logger.info("[ENTRY] Đặt lại cửa sổ giới hạn tỷ lệ cho userId={} endpoint={}", userId, endpoint);
        try {
            this.requestCount = 0;
            long now = System.currentTimeMillis();
            this.windowStart = now;
            this.windowEnd = now + (windowSeconds * 1000L);
            logger.debug("[SUCCESS] Cửa sổ giới hạn tỷ lệ đã được đặt lại cho userId={} endpoint={}", userId, endpoint);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [EXC-005] RateLimit.resetWindow gặp lỗi không mong muốn. Nguyên nhân thô: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể đặt lại cửa sổ giới hạn tỷ lệ", e);
        } finally {
            logger.info("[EXIT] Hoàn tất đặt lại cửa sổ giới hạn tỷ lệ cho userId={} endpoint={}", userId, endpoint);
        }
    }

    /* ==================== HỖ TRỢ DEBUG / TỰ KIỂM TRA ==================== */

    @Override
    public String toString() {
        return "RateLimit{" +
                "rateLimitId=" + rateLimitId +
                ", userId=" + userId +
                ", endpoint='" + endpoint + '\'' +
                ", requestCount=" + requestCount +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                '}';
    }
}