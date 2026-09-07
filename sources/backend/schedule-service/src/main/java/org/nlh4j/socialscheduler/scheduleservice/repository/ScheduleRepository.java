package org.nlh4j.socialscheduler.scheduleservice.repository;

import org.nlh4j.socialscheduler.scheduleservice.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------------
 * Enterprise Data Access Repository: ScheduleRepository
 * ----------------------------------------------------------------------------------
 * Business Context: Manages database persistence operations for the Schedule entity
 * within the schedule-service bounded context. Enforces strict multi-tenancy and 
 * parameterized JPQL query boundaries to neutralize SQL Injection vulnerabilities (OWASP A03).
 * 
 * Traceability Tags: [REQ-001]
 * ----------------------------------------------------------------------------------
 */
@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    /**
     * Retrieves a list of schedules associated with a specific user ID and status.
     * Complies with performance constraints by executing optimized index-backed lookups.
     * 
     * @param userId the unique identifier of the user (UUID)
     * @param status the operational status of the schedule (e.g., PENDING, SENT, FAILED, CANCELLED)
     * @return a list of matching ScheduleEntity records
     * @traceability [REQ-001]
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.userId = :userId AND s.status = :status")
    List<ScheduleEntity> findByUserIdAndStatus(
            @Param("userId") UUID userId, 
            @Param("status") String status
    );

    /**
     * Retrieves a list of schedules belonging to a specific tenant ID whose scheduled execution 
     * time falls within a specified date-time range. Employs parameterized JPQL queries 
     * to eliminate SQL injection vectors.
     * 
     * @param tenantId      the unique enterprise tenant identifier
     * @param startTime     the beginning of the scheduling window
     * @param endTime       the end of the scheduling window
     * @return a list of ScheduleEntity records within the timeframe
     * @traceability [REQ-001]
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.tenantId = :tenantId AND s.scheduledTime BETWEEN :startTime AND :endTime")
    List<ScheduleEntity> findByTenantIdAndScheduledTimeBetween(
            @Param("tenantId") String tenantId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime
    );

}