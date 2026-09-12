package org.nlh4j.socialscheduler.contentrecommendationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository class for managing {@link PerformanceMetrics} entities.
 * This component provides core CRUD operations with comprehensive logging,
 * error handling, and traceability tagging as per enterprise governance.
 *
 * @traceability [REQ-002], [EXC-003], [EXC-004]
 */
@Repository
public class PerformanceMetricsRepository {

    /* -------------------------------------------------------------------------
       Enterprise‑wide constants – hoisted to the class crown to satisfy the
       Anti‑Magic‑Numbers policy and enable centralized configuration.
       ------------------------------------------------------------------------- */
    /** Maximum allowed execution timeout for any database operation (milliseconds). */
    public static final int DEFAULT_OPERATION_TIMEOUT_MS = 5_000;
    /** Number of retry attempts for transient database failures. */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    /** Logical name of the managed entity – used in audit logs and error messages. */
    public static final String ENTITY_NAME = "PerformanceMetrics";

    /** Standard SLF4J logger – required by the Logging Audit Law. */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsRepository.class);

    /**
     * Underlying Spring Data JPA repository that provides the actual persistence
     * operations. Composition is used here to keep the concrete class focused on
     * cross‑cutting concerns (logging, exception wrapping) while delegating the
     * data‑access logic to the auto‑generated JPA infrastructure.
     */
    private final JpaRepository<PerformanceMetrics, UUID> jpaRepository;

    public PerformanceMetricsRepository(JpaRepository<PerformanceMetrics, UUID> jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Retrieve a {@link PerformanceMetrics} record by its unique identifier.
     *
     * @param id The UUID of the performance metrics record.
     * @return {@link Optional} containing the found entity, or empty if not found.
     * @traceability [REQ-002]
     */
    @Transactional(readOnly = true)
    public Optional<PerformanceMetrics> findById(UUID id) {
        logger.info("[ENTRY] findById invoked for id: {}", id);
        try {
            // Spring Data JPA automatically translates this into a safe, parameterized query.
            Optional<PerformanceMetrics> result = jpaRepository.findById(id);
            logger.debug("[EXIT] findById returned: {}", result.isPresent() ? "Entity found" : "No entity");
            return result;
        } catch (DataAccessException e) {
            // Comprehensive error logging with traceability tag for audit.
            logger.error("[CRITICAL FAIL] [EXC-003] PerformanceMetricsRepository.findById failed due to database access error. Raw error: {}", e.getMessage(), e);
            // Preserve the original cause chain as required by enterprise exception policy.
            throw new PerformanceMetricsRepositoryException("Failed to retrieve PerformanceMetrics with id " + id, e);
        }
    }

    /**
     * Persist a new {@link PerformanceMetrics} entity or update an existing one.
     *
     * @param entity The {@link PerformanceMetrics} instance to save.
     * @return The saved entity.
     * @traceability [REQ-002], [EXC-004]
     */
    @Transactional
    public PerformanceMetrics save(PerformanceMetrics entity) {
        logger.info("[ENTRY] save invoked for entity: {}", entity);
        try {
            // Spring Data JPA's save method uses prepared statements, neutralizing SQL injection risks.
            PerformanceMetrics saved = jpaRepository.save(entity);
            logger.debug("[EXIT] save completed with id: {}", saved.getPerformanceId());
            return saved;
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-004] PerformanceMetricsRepository.save encountered a data integrity violation. Raw error: {}", e.getMessage(), e);
            throw new PerformanceMetricsRepositoryException("Failed to save PerformanceMetrics entity", e);
        }
    }

    /**
     * Remove a {@link PerformanceMetrics} record by its identifier.
     *
     * @param id The UUID of the record to delete.
     * @traceability [REQ-002], [EXC-003]
     */
    @Transactional
    public void deleteById(UUID id) {
        logger.info("[ENTRY] deleteById invoked for id: {}", id);
        try {
            jpaRepository.deleteById(id);
            logger.debug("[EXIT] deleteById completed for id: {}", id);
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-003] PerformanceMetricsRepository.deleteById failed during deletion. Raw error: {}", e.getMessage(), e);
            throw new PerformanceMetricsRepositoryException("Failed to delete PerformanceMetrics with id " + id, e);
        }
    }

    /**
     * Retrieve all {@link PerformanceMetrics} records from the datastore.
     *
     * @return List of all entities.
     * @traceability [REQ-002]
     */
    @Transactional(readOnly = true)
    public List<PerformanceMetrics> findAll() {
        logger.info("[ENTRY] findAll invoked");
        try {
            List<PerformanceMetrics> results = jpaRepository.findAll();
            logger.debug("[EXIT] findAll returned {} records", results.size());
            return results;
        } catch (DataAccessException e) {
            logger.error("[CRITICAL FAIL] [EXC-003] PerformanceMetricsRepository.findAll encountered an unexpected database error. Raw error: {}", e.getMessage(), e);
            throw new PerformanceMetricsRepositoryException("Failed to retrieve all PerformanceMetrics records", e);
        }
    }

    /**
     * Custom enterprise exception wrapper that preserves the causal chain.
     * This satisfies the Exception Cause Chain Preservation Law.
     */
    @SuppressWarnings("serial")
    public static class PerformanceMetricsRepositoryException extends RuntimeException {
        public PerformanceMetricsRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}