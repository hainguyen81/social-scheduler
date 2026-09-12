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