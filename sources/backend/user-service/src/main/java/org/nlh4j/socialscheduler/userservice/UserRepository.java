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