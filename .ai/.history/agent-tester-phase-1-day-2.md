# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Test Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql` (Must map to sources/backend/ or sources/frontend/)


### ENTERPRISE AUTOMATED TESTING RECOVERY WORKSPACE
* **Target Test File Disk Status:** PROCOVERY_TEST_MAINTENANCE
* **Verification Scope:** INTEGRATION_SCOPE
* **Current Living Test Suite Content:**
<EXISTING_TEST_SUITE_CODE>
```sql
-- ============================================================================
-- 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
-- ============================================================================
-- Target Project Identity Safe Name: social-scheduler
-- Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
-- Target Component Destination Path: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql
-- Traceability Audit Tags For This Task: [DAT-001], [DAT-ALL (1 to 3)]
-- Business Context & Architecture: Initial database schema migration script for user-service
-- implementing strict multi-tenancy schema isolation, secure password storage constraints,
-- RBAC role validation, and OWASP A03 compliant UUID primary keys.
-- ============================================================================

-- [DAT-001] [DAT-ALL (1 to 3)] Create dedicated schema for user-service boundary context
-- to enforce schema-per-tenant architectural data isolation and security hardening.
CREATE SCHEMA IF NOT EXISTS user_schema;

-- [DAT-001] [DAT-ALL (1 to 3)] Set default search path to user_schema for subsequent DDL operations
SET search_path TO user_schema, public;

-- ============================================================================
-- TABLE: users
-- ============================================================================
-- [DAT-001] [DAT-ALL (1 to 3)] Stores enterprise user credentials, tenant mapping,
-- and RBAC authorization metadata. Uses UUID primary keys to neutralize sequential
-- enumeration vectors in strict alignment with OWASP A03 security guidelines.
-- ============================================================================
CREATE TABLE user_schema.users (
    -- Unique identifier for the user entity, generated as a cryptographically secure UUID
    user_id UUID NOT NULL,
    
    -- Tenant identifier string for multi-tenancy data partitioning and logical isolation
    tenant_id VARCHAR(64) NOT NULL,
    
    -- User email address used as the primary login principal
    email VARCHAR(255) NOT NULL,
    
    -- Bcrypt/Argon2 hashed password string; cleartext passwords are strictly banned
    password_hash VARCHAR(255) NOT NULL,
    
    -- Role-based access control (RBAC) permission level assigned to the user principal
    role VARCHAR(32) NOT NULL,
    
    -- Account status flag indicating whether the user is permitted to authenticate
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamp recording the exact moment of initial record creation
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Timestamp recording the last modification applied to the user record
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- [DAT-001] Primary key constraint enforcing entity uniqueness via UUID
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    
    -- [DAT-001] Composite unique constraint ensuring email addresses are strictly unique within a single tenant boundary
    CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email),
    
    -- [DAT-001] [DAT-ALL (1 to 3)] Check constraint enforcing strict RBAC role membership per enterprise specification
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER', 'SCHEDULER', 'ANALYST'))
);

-- ============================================================================
-- INDEXES & PERFORMANCE OPTIMIZATION
-- ============================================================================
-- [DAT-001] [DAT-ALL (1 to 3)] Create b-tree index on tenant_id to accelerate multi-tenant
-- query filtering, tenant scoping, and authorization lookups across partitioned partitions.
CREATE INDEX idx_users_tenant ON user_schema.users(tenant_id);

-- [DAT-001] [DAT-ALL (1 to 3)] Create b-tree index on email to optimize authentication token resolution
CREATE INDEX idx_users_email ON user_schema.users(email);
```
</EXISTING_TEST_SUITE_CODE>



### 🚀 SYSTEM INTEGRATION TESTING CONTEXT (E2E PIPELINE)
INTEGRATION_SCOPE: Multi-component workflow validation required for target destination: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql. 
[INSTRUCTION FOR AI: This is a system integration/E2E test suite. No single class code context is provided. You MUST write the test to bootstrap the full runtime infrastructure context, handle live network APIs, or database relational calculation states.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY TESTER AGENT
["Sinh lớp kiểm thử tích hợp tại ./sources/backend/user-service/src/test/java/org/nlh4j/socialscheduler/userservice/UserSchemaMigrationIT.java sử dụng Testcontainers PostgreSQL phiên bản 16-alpine. Lớp kiểm thử phải được đánh dấu annotation @SpringBootTest và @Testcontainers để Spring tự động quản lý vòng đời container. Khai báo container PostgreSQL với phiên bản postgres:16-alpine và cấu hình container với biến môi trường POSTGRES_DB=socialscheduler_test, POSTGRES_USER=test, POSTGRES_PASSWORD=test. Lớp kiểm thử phải trỏ Flyway vào script ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql thông qua annotation @Sql hoặc cấu hình datasource động. Xác minh các bảng users được tạo với đầy đủ cột thông qua câu truy vấn SELECT column_name FROM information_schema.columns WHERE table_schema = 'user_schema' AND table_name = 'users'. Xác minh ràng buộc khóa chính tồn tại thông qua truy vấn information_schema.table_constraints. Xác minh ràng buộc khóa duy nhất uk_users_tenant_email. Xác minh ràng buộc kiểm tra ck_users_role hoạt động đúng bằng cách thử chèn giá trị role = 'INVALID_ROLE' và khẳng định ngoại lệ DataIntegrityViolationException được ném ra. Đảm bảo kiểm thử thất bại khi chèn giá trị role không thuộc tập cho phép."]

---

### ⚙️ TEST ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided away. Automatically evaluate the extension profile of the target test component path (from `**Verification Scope**`), you MUST perform an AST-level incremental insertion of the new test methods into the current file text block. You ARE CRITICALLY BANNED from dropping or shrinking old test cases.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` structure. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-003]', '[DAT-ALL (1 to 3)]']` variable, and permanently burn those codes into the Javadoc metadata blocks (for Java) or the test case description strings (for TypeScript/JavaScript). Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```java
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
```

