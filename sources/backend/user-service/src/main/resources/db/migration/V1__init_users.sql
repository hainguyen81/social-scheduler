package org.nlh4j.socialscheduler.userservice;

// ============================================================================
// 🏢 ENTERPRISE SYSTEM AUTOMATED INTEGRATION TEST SUITE
// ============================================================================
// Target Project Identity Safe Name: social-scheduler
// Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
// Target Component Destination Path: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql
// Traceability Audit Tags For This Task: [DAT-001], [DAT-ALL (1 to 3)]
// Business Context & Architecture: Integration test suite utilizing Testcontainers
// PostgreSQL 16-alpine to validate Flyway migration script V1__init_users.sql,
// ensuring multi-tenancy schema isolation, RBAC constraints, and error boundaries.
// ============================================================================

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

/**
 * Integration test suite for verifying PostgreSQL database schema migrations
 * managed by Flyway within the user-service bounded context.
 * 
 * @verifies [DAT-001], [DAT-ALL (1 to 3)]
 */
@SpringBootTest
@Testcontainers
public class UserSchemaMigrationIT {

    // ========================================================================
    // TOP-OF-CLASS CONSTANTS & LOGGER DECLARATION [0.2]
    // ========================================================================
    private static final Logger logger = LoggerFactory.getLogger(UserSchemaMigrationIT.class);
    
    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String TEST_DB_NAME = "socialscheduler_test";
    private static final String TEST_DB_USER = "test";
    private static final String TEST_DB_PASS = "test";
    
    private static final String SCHEMA_NAME = "user_schema";
    private static final String TABLE_NAME = "users";

    // ========================================================================
    // TESTCONTAINERS INFRASTRUCTURE CONFIGURATION [0.3]
    // ========================================================================
    @Container
    public static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(TEST_DB_NAME)
            .withUsername(TEST_DB_USER)
            .withPassword(TEST_DB_PASS);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        logger.info("[PROCESS] [DAT-001] Configuring dynamic datasource properties for PostgreSQL Testcontainer");
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Validates that Flyway successfully executed V1__init_users.sql, created the
     * user_schema, and provisioned the users table with all expected columns.
     * 
     * @verifies [DAT-001], [DAT-ALL (1 to 3)]
     */
    @Test
    @DisplayName("Verify Users Table Structure and Columns Exist After Flyway Migration")
    public void testUsersTableStructureMigration() {
        logger.info("[TEST_START] [DAT-001] Executing testUsersTableStructureMigration()");
        try {
            // Query information_schema to verify table columns
            String query = "SELECT column_name FROM information_schema.columns WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position;";
            List<String> columns = jdbcTemplate.queryForList(query, String.class, SCHEMA_NAME, TABLE_NAME);

            logger.info("[DAT-001] Retrieved columns from user_schema.users: {}", columns);

            Assertions.assertNotNull(columns, "Columns list must not be null");
            Assertions.assertTrue(columns.contains("user_id"), "Table must contain user_id column");
            Assertions.assertTrue(columns.contains("tenant_id"), "Table must contain tenant_id column");
            Assertions.assertTrue(columns.contains("email"), "Table must contain email column");
            Assertions.assertTrue(columns.contains("password_hash"), "Table must contain password_hash column");
            Assertions.assertTrue(columns.contains("role"), "Table must contain role column");
            Assertions.assertTrue(columns.contains("enabled"), "Table must contain enabled column");
            Assertions.assertTrue(columns.contains("created_at"), "Table must contain created_at column");
            Assertions.assertTrue(columns.contains("updated_at"), "Table must contain updated_at column");

            logger.info("[TEST_SUCCESS] [DAT-001] testUsersTableStructureMigration completed successfully.");
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [DAT-001] testUsersTableStructureMigration failed. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Test failed due to database inspection error", e);
        }
    }

    /**
     * Validates that primary key and unique constraints (uk_users_tenant_email)
     * are correctly enforced by the database engine.
     * 
     * @verifies [DAT-001], [DAT-ALL (1 to 3)]
     */
    @Test
    @DisplayName("Verify Primary Key and Unique Constraints Enforcement")
    public void testConstraintsEnforcement() {
        logger.info("[TEST_START] [DAT-001] Executing testConstraintsEnforcement()");
        try {
            // Verify table constraints exist in information_schema
            String constraintQuery = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_schema = ? AND table_name = ?";
            List<String> constraints = jdbcTemplate.queryForList(constraintQuery, String.class, SCHEMA_NAME, TABLE_NAME);

            logger.info("[DAT-001] Retrieved constraints for user_schema.users: {}", constraints);

            Assertions.assertTrue(constraints.contains("pk_users"), "Primary key constraint pk_users must exist");
            Assertions.assertTrue(constraints.contains("uk_users_tenant_email"), "Unique constraint uk_users_tenant_email must exist");

            // Insert a valid test record
            UUID userId1 = UUID.randomUUID();
            String insertSql = "INSERT INTO user_schema.users (user_id, tenant_id, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, userId1, "tenant-alpha", "test@example.com", "hashed_pwd_123", "ADMIN");

            // Attempt to insert duplicate email within the same tenant to trigger unique constraint violation
            UUID userId2 = UUID.randomUUID();
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                jdbcTemplate.update(insertSql, userId2, "tenant-alpha", "test@example.com", "hashed_pwd_456", "USER");
            }, "Inserting duplicate email within the same tenant must throw DataIntegrityViolationException");

            logger.info("[TEST_SUCCESS] [DAT-001] testConstraintsEnforcement completed successfully.");
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [DAT-001] testConstraintsEnforcement failed. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Test failed during constraint validation", e);
        }
    }

    /**
     * Validates that the check constraint (ck_users_role) strictly permits only
     * authorized enterprise roles ('ADMIN', 'USER', 'SCHEDULER', 'ANALYST')
     * and rejects invalid role values with DataIntegrityViolationException.
     * 
     * @verifies [DAT-001], [DAT-ALL (1 to 3)]
     */
    @Test
    @DisplayName("Verify Check Constraint ck_users_role Rejects Invalid Roles")
    public void testRoleCheckConstraintRejectsInvalidValues() {
        logger.info("[TEST_START] [DAT-001] Executing testRoleCheckConstraintRejectsInvalidValues()");
        try {
            UUID userId = UUID.randomUUID();
            String insertSql = "INSERT INTO user_schema.users (user_id, tenant_id, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";

            // Attempt to insert an invalid role value not present in the check constraint whitelist
            Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
                jdbcTemplate.update(insertSql, userId, "tenant-beta", "invalid.role@example.com", "hashed_pwd_xyz", "INVALID_ROLE");
            }, "Inserting an invalid role must trigger a check constraint violation and throw DataIntegrityViolationException");

            logger.info("[TEST_SUCCESS] [DAT-001] testRoleCheckConstraintRejectsInvalidValues completed successfully.");
        } catch (Exception e) {
            logger.error("[CRITICAL FAIL] [DAT-001] testRoleCheckConstraintRejectsInvalidValues failed. Raw error: {}", e.getMessage(), e);
            throw new RuntimeException("Test failed during role check constraint validation", e);
        }
    }
}