-- =========================================================================================
-- Blueprint ID: ARCH-20260831151355
-- Project Name: social-scheduler
-- Target Component: ./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql
-- Traceability Tags: [DAT-001], [DAT-002], [DAT-003], [DAT-ALL (1 to 3)]
-- Enterprise Compliance: Multi-Tenant Schema Isolation, OWASP Data Integrity & Security Standards
-- =========================================================================================

CREATE SCHEMA IF NOT EXISTS schedule_schema;

SET search_type TO schedule_schema, public;

CREATE TABLE schedule_schema.schedules (
    schedule_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,
    actual_sent_time TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_schedules PRIMARY KEY (schedule_id, tenant_id),
    CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id) ON DELETE CASCADE,
    CONSTRAINT ck_schedules_platform CHECK (platform IN ('FACEBOOK', 'INSTAGRAM', 'TIKTOK')),
    CONSTRAINT ck_schedules_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_schedules_retry_count CHECK (retry_count >= 0)
);

-- Indexes for multi-tenant isolation, performance optimization, and schedule querying
CREATE INDEX idx_schedules_tenant_id ON schedule_schema.schedules(tenant_id);
CREATE INDEX idx_schedules_user_status ON schedule_schema.schedules(user_id, status);
CREATE INDEX idx_schedules_tenant_time ON schedule_schema.schedules(tenant_id, scheduled_time);
CREATE INDEX idx_schedules_platform_status ON schedule_schema.schedules(platform, status);