package org.nlh4j.socialscheduler.scheduleservice.entity;

// [REQ-001] Traceability Tag ID compliance mapping for entity schema definition

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 🏛️ ScheduleEntity represents the core database persistence entity mapped to the
 * 'schedules' table within the 'schedule_schema' multi-tenant Postgres partition.
 * 
 * @traceability [REQ-001]
 * @author Enterprise Architecture Team
 * @since 1.0.0
 */
@Entity
@Table(name = "schedules", schema = "schedule_schema")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleEntity implements Serializable {

    // =========================================================================
    // 🛡️ CONSTANT DEFINITIONS (Anti-Magic-Numbers & Configuration Isolation)
    // =========================================================================
    
    private static final long serialVersionUID = 1L;
    
    public static final String SCHEMA_NAME = "schedule_schema";
    public static final String TABLE_NAME = "schedules";
    
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    
    public static final String PLATFORM_FACEBOOK = "FACEBOOK";
    public static final String PLATFORM_INSTAGRAM = "INSTAGRAM";
    public static final String PLATFORM_TIKTOK = "TIKTOK";

    // =========================================================================
    // 📊 PERSISTENT ATTRIBUTES & COLUMN MAPPINGS
    // =========================================================================

    /**
     * Unique primary key identifier for the scheduled publication item.
     * Mapped as non-updatable to guarantee immutable ledger lineage.
     */
    @Id
    @Column(name = "schedule_id", nullable = false, updatable = false)
    private UUID scheduleId;

    /**
     * Foreign key reference identifying the owner user of the schedule.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /**
     * Multi-tenancy isolation discriminator key mapping to the tenant partition.
     */
    @Column(name = "tenant_id", nullable = false, length = 64, updatable = false)
    private String tenantId;

    /**
     * Target social media network platform (FACEBOOK, INSTAGRAM, TIKTOK).
     * Validated against explicit architectural whitelist constraints.
     */
    @Column(name = "platform", nullable = false, length = 32)
    private String platform;

    /**
     * Plain text or formatted social content payload to be published.
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp indicating when the publication job is scheduled to execute.
     */
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * Current lifecycle status of the schedule (PENDING, SENT, FAILED, CANCELLED).
     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * Exact timestamp when the publication job was successfully dispatched.
     */
    @Column(name = "actual_sent_time")
    private LocalDateTime actualSentTime;

    /**
     * Cumulative count of delivery retry attempts performed by worker nodes.
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * System audit timestamp marking the exact instant of entity creation.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * System audit timestamp marking the last modification update instant.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================================
    // 🔗 RELATIONSHIP MAPPINGS (Lazy Loading Enforcement)
    // =========================================================================

    /**
     * Many-to-one logical association back to the parent user entity.
     * Configured with LAZY fetch to prevent accidental eager N+1 loading queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private Object userEntityRef; // Placeholder or explicit UserEntity mapping handle

    // =========================================================================
    // 🏗️ CONSTRUCTORS
    // =========================================================================

    /**
     * Default protected no-args constructor required by JPA specification.
     */
    public ScheduleEntity() {
        this.retryCount = 0;
        this.status = STATUS_PENDING;
    }

    /**
     * Parameterized constructor for high-speed programmatic entity instantiation.
     */
    public ScheduleEntity(UUID scheduleId, UUID userId, String tenantId, String platform, 
                          String content, LocalDateTime scheduledTime) {
        this.scheduleId = scheduleId != null ? scheduleId : UUID.randomUUID();
        this.userId = userId;
        this.tenantId = tenantId;
        this.platform = platform;
        this.content = content;
        this.scheduledTime = scheduledTime;
        this.status = STATUS_PENDING;
        this.retryCount = 0;
    }

    // =========================================================================
    // 🔄 LIFECYCLE CALLBACK AUDITING HOOKS
    // =========================================================================

    /**
     * Pre-persist lifecycle callback method executed prior to database insertion.
     * Automatically assigns default IDs, audit timestamps, and initial states.
     */
    @PrePersist
    public void prePersist() {
        if (this.scheduleId == null) {
            this.scheduleId = UUID.randomUUID();
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.status == null) {
            this.status = STATUS_PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    /**
     * Pre-update lifecycle callback method executed prior to database updates.
     * Automatically refreshes the modification audit timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================================================================
    // 📈 GETTERS AND SETTERS
    // =========================================================================

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getActualSentTime() {
        return actualSentTime;
    }

    public void setActualSentTime(LocalDateTime actualSentTime) {
        this.actualSentTime = actualSentTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================================
    // 🧮 HASHCODE, EQUALS & TOSTRING CONTRACTS
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEntity that = (ScheduleEntity) o;
        return Objects.equals(scheduleId, that.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId);
    }

    @Override
    public String toString() {
        return "ScheduleEntity{" +
                "scheduleId=" + scheduleId +
                ", userId=" + userId +
                ", tenantId='" + tenantId + '\'' +
                ", platform='" + platform + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}