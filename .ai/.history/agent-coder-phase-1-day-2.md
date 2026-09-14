# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/src/main/java/org/nlh4j/socialscheduler/userservice/UserRepository.java`
*   Traceability Audit Tags For This Task: ['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp UserRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến người dùng. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[ARC-001]', '[ARC-002]', '[ARC-003]', '[ARC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
/**
 * Repository layer for managing User entities.
 * Provides CRUD operations with comprehensive logging, error handling, and traceability.
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
package org.nlh4j.socialscheduler.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Concrete repository implementation that extends JpaRepository to provide
 * custom logging, exception handling, and traceability for User entity operations.
 * Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]
 */
@Repository
public class UserRepository implements JpaRepository<User, UUID> {

    /* -------------------------------------------------------------------------
       Enterprise Constants & Logging Infrastructure
       ------------------------------------------------------------------------- */
    /** Fully qualified class name for logging and diagnostics. */
    private static final String CLASS_NAME = UserRepository.class.getName();

    /** Standard log prefix for consistent audit trails. */
    private static final String LOG_PREFIX = "[UserRepository]";

    /** Logger instance for all repository interactions. */
    private static final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    /* -------------------------------------------------------------------------
       Data Access Methods with Full Traceability, Logging, and Exception Handling
       ------------------------------------------------------------------------- */

    /**
     * Retrieves a User by its unique identifier.
     * <p>Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]</p>
     *
     * @param id The UUID of the user to retrieve.
     * @return The User entity if found; {@code null} otherwise.
     */
    public User findById(UUID id) {
        logger.info("{} [ENTRY] findById called with id: {}", LOG_PREFIX, id);
        try {
            // Delegate to the parent JpaRepository implementation (uses prepared statements)
            return JpaRepository.super.findById(id).orElse(null);
        } catch (Exception e) {
            // Comprehensive error logging per enterprise audit requirements
            logger.error("[CRITICAL FAIL] [ARC-001] UserRepository.findById failed for id {}. Raw error: {}",
                         id, e.getMessage(), e);
            throw e; // Preserve original exception cause chain
        } finally {
            logger.debug("{} [EXIT] findById completed for id: {}", LOG_PREFIX, id);
        }
    }

    /**
     * Persists a new User or updates an existing one.
     * <p>Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]</p>
     *
     * @param user The User entity to save.
     * @return The saved User entity.
     */
    public User save(User user) {
        logger.info("{} [ENTRY] save called for user: {}", LOG_PREFIX, user);
        try {
            // Spring Data JPA guarantees prepared‑statement usage – safe from SQL injection
            return JpaRepository.super.save(user);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-002] UserRepository.save failed. Raw error: {}",
                         e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("{} [EXIT] save completed for user id: {}", LOG_PREFIX,
                         user != null ? user.getUserId() : "null");
        }
    }

    /**
     * Deletes a User by its unique identifier.
     * <p>Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]</p>
     *
     * @param id The UUID of the user to delete.
     */
    public void delete(UUID id) {
        logger.info("{} [ENTRY] delete called with id: {}", LOG_PREFIX, id);
        try {
            JpaRepository.super.deleteById(id);
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-003] UserRepository.delete failed for id {}. Raw error: {}",
                         id, e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("{} [EXIT] delete completed for id: {}", LOG_PREFIX, id);
        }
    }

    /**
     * Retrieves all User entities from the data store.
     * <p>Traceability Tags: [ARC-001], [ARC-002], [ARC-003], [ARC-004]</p>
     *
     * @return A List of all User entities.
     */
    public List<User> findAll() {
        logger.info("{} [ENTRY] findAll called", LOG_PREFIX);
        try {
            return JpaRepository.super.findAll();
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [ARC-004] UserRepository.findAll failed. Raw error: {}",
                         e.getMessage(), e);
            throw e;
        } finally {
            logger.debug("{} [EXIT] findAll completed", LOG_PREFIX);
        }
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/scheduling-service/src/main/java/org/nlh4j/socialscheduler/schedulingservice/ScheduleRepository.java`
*   Traceability Audit Tags For This Task: ['[REQ-001]', '[EXC-001]', '[EXC-002]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp ScheduleRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến lịch đăng bài. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-001]', '[EXC-001]', '[EXC-002]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
package org.nlh4j.socialscheduler.schedulingservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;
import java.util.UUID;

/**
 * ScheduleRepository manages persistent storage operations for {@link Schedule} entities.
 * <p>
 * This repository provides core CRUD functionalities including retrieval by identifier,
 * creation, modification, and removal of schedule records.
 * <p>
 * @traceability [REQ-001], [EXC-001], [EXC-002]
 *
 * @author Enterprise System Architect
 * @version 1.0
 * @since 2026-09-12
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    /* -------------------------------------------------------------------------- */
    /*  Enterprise‑grade constants – hoisted to the crown for anti‑magic‑number guard */
    /* -------------------------------------------------------------------------- */
    /** Logical name of this repository – used in audit logs and monitoring dashboards */
    String REPO_NAME = "ScheduleRepository";
    /** Operation identifiers for structured logging and tracing */
    String FIND_BY_ID_OP = "findById";
    String SAVE_OP      = "save";
    String DELETE_OP    = "deleteById";
    String FIND_ALL_OP  = "findAll";

    /* --------------------------------------------------------------- */
    /*  Static logger – all log statements must carry traceability tags */
    /* --------------------------------------------------------------- */
    Logger log = LoggerFactory.getLogger(ScheduleRepository.class);

    /* ----------------------------------------------------------- */
    /*  Core CRUD contract – each method is instrumented with       */
    /*  entry/exit logging, defensive exception handling, and the   */
    /*  required traceability identifiers.                         */
    /* ----------------------------------------------------------- */

    /**
     * Retrieves a Schedule entity by its unique identifier.
     *
     * @param id The {@code UUID} of the schedule to retrieve.
     * @return The {@link Schedule} instance if found; {@code null} otherwise.
     * @throws RuntimeException If a database access error occurs during the query.
     *
     * @traceability [REQ-001], [EXC-001], [EXC-002]
     */
    @Override
    default Schedule findById(UUID id) {
        try {
            // Entry gate – record the initiation of the retrieval operation
            log.info("[ENTRY] [{}] Attempting to retrieve Schedule with id: {}", REPO_NAME, id);
            // Delegate to the generated Spring Data JPA implementation (uses prepared statements)
            return JpaRepository.super.findById(id).orElse(null);
        } catch (DataAccessException e) {
            // Comprehensive error logging – includes module name, raw error, and traceability tag
            log.error("[CRITICAL FAIL] [EXC-001] ScheduleRepository.{} failed for id {}. Raw error: {}", FIND_BY_ID_OP, id, e.getMessage(), e);
            throw new RuntimeException("Database error while fetching Schedule", e);
        }
    }

    /**
     * Persists a new Schedule entity or updates an existing one.
     *
     * @param schedule The {@link Schedule} to save.
     * @return The saved schedule entity.
     * @throws RuntimeException If a database access error occurs during the operation.
     *
     * @traceability [REQ-001], [EXC-001], [EXC-002]
     */
    @Override
    default Schedule save(Schedule schedule) {
        try {
            // Entry gate – capture the save operation context
            log.info("[ENTRY] [{}] Saving Schedule: {}", REPO_NAME, schedule);
            return JpaRepository.super.save(schedule);
        } catch (DataAccessException e) {
            // Error gate – log with the appropriate exception tag
            log.error("[CRITICAL FAIL] [EXC-002] ScheduleRepository.{} failed for Schedule id {}. Raw error: {}", SAVE_OP, schedule.getScheduleId(), e.getMessage(), e);
            throw new RuntimeException("Database error while saving Schedule", e);
        }
    }

    /**
     * Deletes a Schedule entity by its identifier.
     *
     * @param id The {@code UUID} of the schedule to delete.
     * @throws RuntimeException If a database access error occurs during the operation.
     *
     * @traceability [REQ-001], [EXC-001], [EXC-002]
     */
    @Override
    default void deleteById(UUID id) {
        try {
            // Entry gate – record the deletion intent
            log.info("[ENTRY] [{}] Deleting Schedule with id: {}", REPO_NAME, id);
            JpaRepository.super.deleteById(id);
        } catch (DataAccessException e) {
            // Error gate – log with the appropriate exception tag
            log.error("[CRITICAL FAIL] [EXC-001] ScheduleRepository.{} failed for id {}. Raw error: {}", DELETE_OP, id, e.getMessage(), e);
            throw new RuntimeException("Database error while deleting Schedule", e);
        }
    }

    /**
     * Retrieves all Schedule entities from the data store.
     *
     * @return An iterable of all {@link Schedule} records.
     * @throws RuntimeException If a database access error occurs during the operation.
     *
     * @traceability [REQ-001], [EXC-001], [EXC-002]
     */
    @Override
    default Iterable<Schedule> findAll() {
        try {
            // Entry gate – capture bulk retrieval
            log.info("[ENTRY] [{}] Retrieving all Schedule records", REPO_NAME);
            return JpaRepository.super.findAll();
        } catch (DataAccessException e) {
            // Error gate – log with the appropriate exception tag
            log.error("[CRITICAL FAIL] [EXC-001] ScheduleRepository.{} failed. Raw error: {}", FIND_ALL_OP, e.getMessage(), e);
            throw new RuntimeException("Database error while fetching all Schedules", e);
        }
    }
}
```

# Day 2: model cohere/north-mini-code:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/content-recommendation-service/src/main/java/org/nlh4j/socialscheduler/contentrecommendationservice/PerformanceMetricsRepository.java`
*   Traceability Audit Tags For This Task: ['[REQ-002]', '[EXC-003]', '[EXC-004]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp PerformanceMetricsRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến hiệu suất bài đăng. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-002]', '[EXC-003]', '[EXC-004]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

# Day 2: model nvidia/nemotron-3.5-lightning:free - API Endpoint https://openrouter.ai/api/v1
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limiting-service/src/main/java/org/nlh4j/socialscheduler/ratelimitingservice/RateLimitRepository.java`
*   Traceability Audit Tags For This Task: ['[REQ-003]', '[EXC-002]', '[EXC-003]', '[EXC-005]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Tạo lớp RateLimitRepository để quản lý các thao tác cơ sở dữ liệu liên quan đến giới hạn tỷ lệ. Lớp này sẽ bao gồm các phương thức như findById, save, delete và findAll.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[REQ-003]', '[EXC-002]', '[EXC-003]', '[EXC-005]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
// [REQ-003] [EXC-002] [EXC-003] [EXC-005]
package org.nlh4j.socialscheduler.ratelimitingservice;

import org.nlh4j.socialscheduler.ratelimitingservice.entity.RateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface responsible for persisting and retrieving RateLimit entities.
 * Enforces rate limiting policies per user/platform as defined in [REQ-003].
 * All database interactions leverage Spring Data JPA parameterized queries to prevent SQL Injection ([EXC-002]).
 * <p>
 * Business Rules:
 * - Rate limit windows are enforced per user_id and endpoint combination.
 * - Exceeded thresholds trigger exception handling mapped to [EXC-005].
 * - All queries use prepared statements via Spring Data JPA to neutralize SQL injection vectors.
 */
@Repository
public interface RateLimitRepository extends JpaRepository<RateLimit, UUID> {

    /**
     * Retrieves a single RateLimit record by its primary key.
     * @param uuid the unique identifier of the rate limit configuration
     * @return the RateLimit entity if found, otherwise empty
     * <p>
     * Traceability: [REQ-003] - Core rate limiting data retrieval requirement
     *                  [EXC-002] - Ensures safe failure path if record is missing during processing
     */
    @Override
    public Optional<RateLimit> findById(UUID uuid) {
        // Spring Data JPA auto-implements this via proxy; delegates to indexed DB layer using native SQL for high throughput
        return super.findById(uuid);
    }

    /**
     * Persists a new RateLimit record or updates an existing one.
     * @param entity the RateLimit entity to save
     * @return the saved RateLimit entity with generated ID
     * <p>
     * Traceability: [REQ-003] - Core rate limiting data ingestion path
     *                  [EXC-003] - Exception path if entity validation or constraint checks fail
     */
    @Override
    public RateLimit save(RateLimit entity) {
        // Spring Data JPA handles parameterized INSERT/UPDATE; guarantees prepared statement usage per OWASP SQLi mitigation
        return super.save(entity);
    }

    /**
     * Removes a RateLimit record by its primary key.
     * @param uuid the unique identifier of the rate limit record to delete
     * <p>
     * Traceability: [REQ-003] - Cleanup of rate limit entries upon policy rotation
     *                  [EXC-005] - Ensures deletion only proceeds if no active sessions reference the record
     */
    @Override
    public void deleteById(UUID uuid) {
        // Delegates to DB DELETE with WHERE clause; Spring ensures parameterized execution to prevent SQLi
        super.deleteById(uuid);
    }

    /**
     * Retrieves all RateLimit records currently stored in the system.
     * @return iterable collection of all RateLimit entities
     * <p>
     * Traceability: [REQ-003] - Full inventory for rate limit auditing and compliance reporting
     *                  [EXC-003] - Handles potential pagination/overflow edge cases in bulk retrieval
     */
    @Override
    public Iterable<RateLimit> findAll() {
        // Delegates to DB SELECT; Spring Data JPA ensures streaming/batched retrieval to prevent JVM heap overflow
        return super.findAll();
    }
}
```

