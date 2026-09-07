package org.nlh4j.socialscheduler.scheduleservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.nlh4j.socialscheduler.common.Platform;
import org.nlh4j.socialscheduler.scheduleservice.dto.ScheduleStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
import lombok.Data;

/**
 * 🏛️ ScheduleEntity represents the core database persistence entity mapped to the
 * 'schedules' table within the 'schedule_schema' multi-tenant Postgres partition.
 * 
 * @traceability [REQ-001]
 * @author Enterprise Architecture Team
 * @since 1.0.0
 */
@Data
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
    private Platform platform;

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
    private ScheduleStatus status = ScheduleStatus.PENDING;

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
    }

    /**
     * Parameterized constructor for high-speed programmatic entity instantiation.
     */
    public ScheduleEntity(UUID scheduleId, UUID userId, String tenantId, Platform platform, 
                          String content, LocalDateTime scheduledTime) {
        this.scheduleId = scheduleId != null ? scheduleId : UUID.randomUUID();
        this.userId = userId;
        this.tenantId = tenantId;
        this.platform = platform;
        this.content = content;
        this.scheduledTime = scheduledTime;
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
        this.status = Objects.requireNonNullElse(this.status, ScheduleStatus.PENDING);
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