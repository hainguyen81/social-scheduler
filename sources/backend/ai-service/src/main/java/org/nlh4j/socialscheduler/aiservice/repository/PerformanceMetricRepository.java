package org.nlh4j.socialscheduler.aiservice.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.nlh4j.socialscheduler.aiservice.entity.PerformanceMetricEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA Repository for {@link PerformanceMetricEntity}.
 * Defines the optimized native query for fetching top-performing posts.
 *
 * <p><b>Query Design:</b> Uses native SQL for maximum performance on the engagement
 * computation {@code (likes + comments + shares)}. All parameters are bound via
 * {@code @Param} to prevent SQL injection (OWASP A03 compliance).</p>
 *
 * @traceability [REQ-002], [DAT-002]
 */
public interface PerformanceMetricRepository extends org.springframework.data.jpa.repository.JpaRepository<PerformanceMetricEntity, UUID> {

    /**
     * Native query joining performance_metrics with schedules to filter by platform
     * and compute engagement score. Enforces tenant isolation via tenant_id.
     *
     * @param tenantId  current tenant identifier (from SecurityContext)
     * @param platform  target platform (FACEBOOK, INSTAGRAM, TIKTOK)
     * @param sinceDate lookback window start (30 days ago)
     * @param limit     maximum rows to return
     * @return list of performance metrics ordered by engagement descending
     */
    @Query(
        value = """
            SELECT pm.*
            FROM ai_schema.performance_metrics pm
            JOIN schedule_schema.schedules s ON pm.post_id = s.schedule_id
            WHERE pm.tenant_id = :tenantId
              AND s.platform = :platform
              AND pm.collected_at >= :sinceDate
            ORDER BY (pm.likes + pm.comments + pm.shares) DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<PerformanceMetricEntity> findTopPerformingPostsByTenantAndPlatform(
            @Param("tenantId") String tenantId,
            @Param("platform") String platform,
            @Param("sinceDate") OffsetDateTime sinceDate,
            @Param("limit") int limit
    );
}
