package org.nlh4j.socialscheduler.aiservice.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity mapping to {@code ai_schema.performance_metrics} table.
 * Created in Phase 1 migration {@code V1__init_performance_metrics.sql}.
 *
 * <p>Composite primary key: {@code (performance_id, post_id, collected_at)}.
 * Foreign key to {@code schedule_schema.schedules(schedule_id)} via {@code post_id}.</p>
 *
 * @traceability [DAT-002], [REQ-002]
 */
@Entity
@Table(name = "performance_metrics", schema = "ai_schema")
@IdClass(PerformanceMetricId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceMetricEntity {

    @Id
    @Column(name = "performance_id", nullable = false, updatable = false)
    private UUID performanceId;

    @Id
    @Column(name = "post_id", nullable = false, updatable = false)
    private UUID postId;

    @Id
    @Column(name = "collected_at", nullable = false, updatable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "likes", nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @Column(name = "comments", nullable = false)
    @Builder.Default
    private Integer comments = 0;

    @Column(name = "shares", nullable = false)
    @Builder.Default
    private Integer shares = 0;

    /**
     * Computed engagement score for sorting (not persisted).
     * @return sum of likes, comments, and shares
     */
    @Transient
    public int getEngagementScore() {
        return likes + comments + shares;
    }
}