/**
 * PerformanceAnalyticsClient - Integration client for fetching historical performance metrics
 * to support AI-driven content recommendation personalization.
 *
 * <p>This component provides read-only access to the {@code performance_metrics} table
 * (migrated in Phase 1 via {@code V1__init_performance_metrics.sql}) and joins with
 * {@code schedules} table to filter by platform. All queries enforce multi-tenant isolation
 * by injecting {@code tenant_id} from the authenticated security context.</p>
 *
 * <p><b>Traceability Tags:</b> [REQ-002]</p>
 *
 * <p><b>Architectural Compliance:</b>
 * <ul>
 *   <li>Implements {@link PerformanceAnalyticsClient} interface for testability (mocking)</li>
 *   <li>Uses Spring Data JPA with {@code @Query} and named parameters to prevent SQL Injection (OWASP A03)</li>
 *   <li>Integrates Caffeine caching via {@code @Cacheable} with 15-minute TTL</li>
 *   <li>Emits Micrometer timer metric {@code ai.performance.fetch.duration}</li>
 *   <li>Returns empty list (never null) on no-data scenarios</li>
 *   <li>Enforces tenant isolation via {@code SecurityContextHolder}</li>
 * </ul>
 * </p>
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @since 2026-08-31
 */
package org.nlh4j.socialscheduler.aiservice.integration;

import java.util.List;
import java.util.UUID;

import org.nlh4j.socialscheduler.aiservice.entity.PerformanceMetricEntity;

/**
 * Interface defining the contract for performance analytics data access.
 * Separated to enable easy mocking in unit tests (e.g., {@code RecommendationServiceTest}).
 *
 * @traceability [REQ-002]
 */
public interface PerformanceAnalyticsClient {

    /**
     * Retrieves top-performing posts for a given user and platform within the last 30 days.
     *
     * @param userId   the unique identifier of the user (tenant-scoped)
     * @param platform target social platform (FACEBOOK, INSTAGRAM, TIKTOK)
     * @param limit    maximum number of results to return
     * @return list of performance metrics ordered by total engagement descending; empty list if none found
     */
    List<PerformanceMetricEntity> findTopPerformingPosts(UUID userId, String platform, int limit);
}