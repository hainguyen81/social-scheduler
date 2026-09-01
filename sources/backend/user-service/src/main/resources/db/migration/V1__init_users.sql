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