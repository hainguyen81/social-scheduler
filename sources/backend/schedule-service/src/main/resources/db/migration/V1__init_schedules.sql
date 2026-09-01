-- ============================================================================
-- 🏛️ MASTER ENTERPRISE GOVERNANCE GUARDRAILS & TRACEABILITY METADATA
-- ============================================================================
-- Traceability Tag IDs: [DAT-001], [DAT-ALL (1 to 3)]
-- Project Identity: social-scheduler
-- Target Component: ./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql
-- Business Context: Enterprise multi-tenant scheduling database migration script.
-- Architectural Standard: Schema-per-tenant isolation, strict foreign key constraints,
--                         OWASP A01 compliance via tenant segregation, indexing.
-- ============================================================================

-- [REQ-DAT-001] [DAT-ALL (1 to 3)] Ensure isolated bounded schema exists for the scheduler domain
CREATE SCHEMA IF NOT EXISTS schedule_schema;

-- [REQ-DAT-001] [DAT-ALL (1 to 3)] Create the primary schedules table with strict columnar types and constraints
-- Business Rule: Enforces data integrity, multi-tenancy isolation via tenant_id, and platform governance.
CREATE TABLE schedule_schema.schedules (
    -- Unique identifier for each scheduled post entry
    schedule_id UUID NOT NULL,
    
    -- Identifier of the user who owns or created the schedule
    user_id UUID NOT NULL,
    
    -- Tenant identifier for strict multi-tenant data isolation [OWASP A01 / NFR-003]
    tenant_id VARCHAR(64) NOT NULL,
    
    -- Target social media platform for publication
    platform VARCHAR(32) NOT NULL,
    
    -- Textual payload content of the post to be published
    content TEXT NOT NULL,
    
    -- Exact scheduled execution timestamp for publication
    scheduled_time TIMESTAMP NOT NULL,
    
    -- Current lifecycle status of the schedule entry
    status VARCHAR(16) NOT NULL,
    
    -- Timestamp when the post was actually sent to the external platform (nullable)
    actual_sent_time TIMESTAMP,
    
    -- Counter tracking publication retry attempts upon upstream failure
    retry_count INTEGER NOT NULL DEFAULT 0,
    
    -- Timestamp when the record was initially created
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Timestamp when the record was last modified
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- [DAT-001] Composite Primary Key enforcing uniqueness across schedule, user, platform, and execution time
    CONSTRAINT pk_schedules PRIMARY KEY (schedule_id, user_id, platform, scheduled_time),
    
    -- [DAT-ALL (1 to 3)] Foreign Key constraint linking user_id to the core user-service schema table
    CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id),
    
    -- [DAT-001] Check constraint ensuring platform strictly matches authorized enterprise channels
    CONSTRAINT ck_schedules_platform CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'TIKTOK')),
    
    -- [DAT-001] Check constraint validating lifecycle state transitions
    CONSTRAINT ck_schedules_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

-- ============================================================================
-- DATABASE PERFORMANCE & QUERY OPTIMIZATION INDEXES
-- ============================================================================

-- [DAT-003] Auxiliary index optimizing queries filtering by user and current lifecycle status
CREATE INDEX idx_schedules_user_status ON schedule_schema.schedules(user_id, status);

-- [DAT-003] Auxiliary index optimizing time-window polling queries partitioned by tenant
CREATE INDEX idx_schedules_tenant_time ON schedule_schema.schedules(tenant_id, scheduled_time);