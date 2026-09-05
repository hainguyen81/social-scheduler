-- ============================================================================
-- 🏛️ MASTER ENTERPRISE GOVERNANCE GUARDRAILS & TRACEABILITY METADATA
-- ============================================================================
-- Project Name: social-scheduler
-- Target Component: ./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql
-- Traceability Tag IDs: [DAT-002], [DAT-ALL (1 to 3)]
-- Business Context: Database Migration Script for AI Recommendation Service Performance Metrics
-- Architecture Compliance: Schema-per-tenant isolation, OWASP SQLi prevention, Foreign Key integrity
-- ============================================================================

-- [DAT-002], [DAT-ALL (1 to 3)] Ensure schema separation for AI service context to isolate tenant data workloads
CREATE SCHEMA IF NOT EXISTS ai_schema;

-- [DAT-002], [DAT-ALL (1 to 3)] Create performance_metrics table within ai_schema to track historical social media engagement
CREATE TABLE IF NOT EXISTS ai_schema.performance_metrics (
    -- Unique identifier for the performance metric record (UUID v4) [OWASP A03 / SQLi Prevention]
    performance_id UUID NOT NULL,
    
    -- Foreign key reference pointing to the published schedule entity in schedule_schema
    post_id UUID NOT NULL,
    
    -- Tenant identifier string for multi-tenancy isolation and security enforcement [ARC-003, NFR-003]
    tenant_id VARCHAR(64) NOT NULL,
    
    -- Aggregate count of likes captured for the post, defaulted to 0
    likes INTEGER NOT NULL DEFAULT 0,
    
    -- Aggregate count of comments captured for the post, defaulted to 0
    comments INTEGER NOT NULL DEFAULT 0,
    
    -- Aggregate count of shares captured for the post, defaulted to 0
    shares INTEGER NOT NULL DEFAULT 0,
    
    -- Timestamp indicating when the performance metrics were collected from third-party social APIs
    collected_at TIMESTAMP NOT NULL,
    
    -- Composite primary key ensuring uniqueness across performance record, post reference, and collection time
    CONSTRAINT pk_performance PRIMARY KEY (performance_id, post_id, collected_at),
    
    -- Foreign key constraint ensuring referential integrity with schedule_schema.schedules
    CONSTRAINT fk_performance_schedule FOREIGN KEY (post_id) 
        REFERENCES schedule_schema.schedules(schedule_id) 
        ON UPDATE CASCADE 
        ON DELETE CASCADE,
    
    -- Check constraint ensuring likes count cannot be negative [Defensive Data Validation]
    CONSTRAINT ck_performance_likes CHECK (likes >= 0),
    
    -- Check constraint ensuring comments count cannot be negative [Defensive Data Validation]
    CONSTRAINT ck_performance_comments CHECK (comments >= 0),
    
    -- Check constraint ensuring shares count cannot be negative [Defensive Data Validation]
    CONSTRAINT ck_performance_shares CHECK (shares >= 0)
);

-- [DAT-002], [DAT-ALL (1 to 3)] Create secondary index on post_id to optimize analytical read queries and AI recommendations lookup
CREATE INDEX IF NOT EXISTS idx_performance_post 
    ON ai_schema.performance_metrics (post_id);

-- [DAT-002], [DAT-ALL (1 to 3)] Create secondary index on tenant_id and collected_at to optimize multi-tenant reporting queries
CREATE INDEX IF NOT EXISTS idx_performance_tenant_collected 
    ON ai_schema.performance_metrics (tenant_id, collected_at);