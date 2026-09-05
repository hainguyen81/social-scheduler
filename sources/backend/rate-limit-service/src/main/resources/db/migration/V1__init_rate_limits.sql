-- ============================================================================
-- Enterprise Database Migration Script: V1__init_rate_limits.sql
-- Project Identity: social-scheduler
-- Target Component: ./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql
-- Traceability Audit Tags: [DAT-003], [DAT-ALL (1 to 3)]
-- Business Logic Context: Initial Flyway DDL migration script for rate-limit-service.
--                         Creates isolated schema, rate_limits table, composite primary key,
--                         foreign key reference to user schema, check constraints, and performance index.
-- Security & Compliance: OWASP Top 10 compliance for data integrity, multi-tenancy isolation,
--                        and strict endpoint whitelisting to prevent security misconfigurations.
-- ============================================================================

-- [DAT-003], [DAT-ALL (1 to 3)] - Create dedicated schema for rate limit management if not exists
-- Enforces logical separation of concerns within the multi-tenant PostgreSQL database cluster
CREATE SCHEMA IF NOT EXISTS rate_limit_schema;

-- [DAT-003], [DAT-ALL (1 to 3)] - Drop table if exists to ensure idempotent initial migration execution during development
DROP TABLE IF EXISTS rate_limit_schema.rate_limits CASCADE;

-- [DAT-003], [DAT-ALL (1 to 3)] - Create the rate_limits table storing request counters and sliding/fixed window metadata
CREATE TABLE rate_limit_schema.rate_limits (
    -- Unique identifier for the rate limit tracking record (UUID v4)
    rate_limit_id UUID NOT NULL,
    
    -- Foreign reference linking the rate limit counter to the specific user entity
    user_id UUID NOT NULL,
    
    -- Tenant identifier string ensuring strict multi-tenancy data segregation [NFR-003]
    tenant_id VARCHAR(64) NOT NULL,
    
    -- Target API endpoint path being monitored for rate limiting [REQ-003]
    endpoint VARCHAR(255) NOT NULL,
    
    -- Accumulative request counter within the active time window
    request_count INTEGER NOT NULL,
    
    -- Timestamp marking the beginning of the rate limit sliding/fixed window
    window_start TIMESTAMP NOT NULL,
    
    -- Timestamp marking the expiration of the active rate limit window
    window_end TIMESTAMP NOT NULL,
    
    -- Composite primary key enforcing uniqueness across rate limit records per endpoint and window start [DAT-003]
    CONSTRAINT pk_rate_limits PRIMARY KEY (rate_limit_id, endpoint, window_start),
    
    -- Foreign key constraint ensuring referential integrity with the users table in user_schema [DAT-001, DAT-003]
    CONSTRAINT fk_rate_limits_user FOREIGN KEY (user_id) 
        REFERENCES user_schema.users(user_id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- Check constraint restricting endpoint values to a strict security whitelist, preventing unauthorized endpoint injection [REQ-003, NFR-002]
    CONSTRAINT ck_rate_limits_endpoint CHECK (
        endpoint IN (
            '/api/v1/schedules', 
            '/api/v1/recommendations', 
            '/api/v1/rate-limits', 
            '/api/v1/users'
        )
    ),
    
    -- Check constraint ensuring request count is never negative, maintaining data integrity [DAT-003]
    CONSTRAINT ck_rate_limits_count CHECK (request_count >= 0)
);

-- ============================================================================
-- INDEXING STRATEGY & PERFORMANCE OPTIMIZATION
-- ============================================================================

-- [DAT-003], [DAT-ALL (1 to 3)] - Create high-performance composite B-Tree index on (user_id, endpoint, window_start)
-- Optimizes sliding window and token bucket evaluation queries executed frequently by rate-limit-service [REQ-003]
CREATE INDEX idx_rate_limits_window 
    ON rate_limit_schema.rate_limits(user_id, endpoint, window_start);

-- [DAT-003], [DAT-ALL (1 to 3)] - Create secondary index on tenant_id to support multi-tenant query filtration and auditing
CREATE INDEX idx_rate_limits_tenant 
    ON rate_limit_schema.rate_limits(tenant_id);

-- ============================================================================
-- END OF MIGRATION SCRIPT: V1__init_rate_limits.sql
-- ============================================================================