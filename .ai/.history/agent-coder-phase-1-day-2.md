# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-ALL (1 to 3)]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Sinh script Flyway tại ./sources/backend/user-service/src/main/resources/db/migration/V1__init_users.sql thực thi di trú schema ban đầu cho bảng users. Đầu tiên tạo schema user_schema thông qua CREATE SCHEMA IF NOT EXISTS user_schema. Tạo bảng users với các cột theo đúng thứ tự: user_id UUID NOT NULL, tenant_id VARCHAR(64) NOT NULL, email VARCHAR(255) NOT NULL, password_hash VARCHAR(255) NOT NULL, role VARCHAR(32) NOT NULL, enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP. Khai báo khóa chính pk_users trên cột user_id thông qua CONSTRAINT pk_users PRIMARY KEY (user_id). Khai báo khóa duy nhất uk_users_tenant_email trên cặp cột (tenant_id, email) để đảm bảo email là duy nhất trong phạm vi tenant. Khai báo ràng buộc kiểm tra ck_users_role với tập giá trị cho phép ('ADMIN', 'USER', 'SCHEDULER', 'ANALYST') thông qua CHECK (role IN (...)). Tạo chỉ mục phụ trợ idx_users_tenant trên cột tenant_id để tối ưu truy vấn đa-tenant. Đảm bảo bảng được tạo trong schema user_schema thông qua tiền tố schema trước tên bảng. Tuân thủ nguyên tắc OWASP A03 bằng cách sử dụng kiểu UUID cho khóa chính thay vì SERIAL."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-ALL (1 to 3)]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
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

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql`
*   Traceability Audit Tags For This Task: ['[DAT-001]', '[DAT-ALL (1 to 3)]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Sinh script Flyway tại ./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql thực thi di trú schema ban đầu cho bảng schedules. Đầu tiên tạo schema schedule_schema thông qua CREATE SCHEMA IF NOT EXISTS schedule_schema. Tạo bảng schedules với các cột theo đúng thứ tự: schedule_id UUID NOT NULL, user_id UUID NOT NULL, tenant_id VARCHAR(64) NOT NULL, platform VARCHAR(32) NOT NULL, content TEXT NOT NULL, scheduled_time TIMESTAMP NOT NULL, status VARCHAR(16) NOT NULL, actual_sent_time TIMESTAMP, retry_count INTEGER NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP. Khai báo khóa chính phức hợp pk_schedules trên bộ bốn cột (schedule_id, user_id, platform, scheduled_time). Khai báo khóa ngoại fk_schedules_user tham chiếu cột user_id của schema user_schema.users thông qua FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id). Khai báo ràng buộc kiểm tra ck_schedules_platform với tập giá trị cho phép ('FACEBOOK', 'INSTAGRAM', 'TIKTOK'). Khai báo ràng buộc kiểm tra ck_schedules_status với tập giá trị cho phép ('PENDING', 'SENT', 'FAILED', 'CANCELLED'). Tạo chỉ mục phụ trợ idx_schedules_user_status trên cặp cột (user_id, status) và chỉ mục idx_schedules_tenant_time trên cặp cột (tenant_id, scheduled_time) để tối ưu hiệu năng truy vấn lịch đăng bài theo tenant và theo khoảng thời gian. Tuân thủ nguyên tắc schema-per-tenant và OWASP A01 thông qua cột tenant_id trong mọi câu truy vấn."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-ALL (1 to 3)]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql`
*   Traceability Audit Tags For This Task: ['[DAT-002]', '[DAT-ALL (1 to 3)]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
['Sinh script Flyway tại ./sources/backend/ai-service/src/main/resources/db/migration/V1__init_performance_metrics.sql thực thi di trú schema ban đầu cho bảng performance_metrics. Đầu tiên tạo schema ai_schema thông qua CREATE SCHEMA IF NOT EXISTS ai_schema. Tạo bảng performance_metrics với các cột theo đúng thứ tự: performance_id UUID NOT NULL, post_id UUID NOT NULL, tenant_id VARCHAR(64) NOT NULL, likes INTEGER NOT NULL DEFAULT 0, comments INTEGER NOT NULL DEFAULT 0, shares INTEGER NOT NULL DEFAULT 0, collected_at TIMESTAMP NOT NULL. Khai báo khóa chính phức hợp pk_performance trên bộ ba cột (performance_id, post_id, collected_at). Khai báo khóa ngoại fk_performance_schedule tham chiếu cột schedule_id của schema schedule_schema.schedules thông qua FOREIGN KEY (post_id) REFERENCES schedule_schema.schedules(schedule_id). Khai báo ba ràng buộc kiểm tra ck_performance_likes, ck_performance_comments, ck_performance_shares đảm bảo giá trị số nguyên không âm >= 0. Tạo chỉ mục phụ trợ idx_performance_post trên cột post_id để tối ưu truy vấn theo bài đăng. Tuân thủ nguyên tắc OWASP A03 bằng cách sử dụng kiểu UUID cho mọi khóa chính và khóa ngoại.']

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-002]', '[DAT-ALL (1 to 3)]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql`
*   Traceability Audit Tags For This Task: ['[DAT-003]', '[DAT-ALL (1 to 3)]']

### 📁 BASELINE LAYER / REFERENCE SPECIFICATION

[INSTRUCTION FOR AI: No reference source component or baseline interface is provided. You are tasked with architecting and writing this component completely from scratch, aligning perfectly with the target path file extension.]


### 📋 EXECUTION SUB-TASKS TO IMPLEMENT BY CODER AGENT
["Sinh script Flyway tại ./sources/backend/rate-limit-service/src/main/resources/db/migration/V1__init_rate_limits.sql thực thi di trú schema ban đầu cho bảng rate_limits. Đầu tiên tạo schema rate_limit_schema thông qua CREATE SCHEMA IF NOT EXISTS rate_limit_schema. Tạo bảng rate_limits với các cột theo đúng thứ tự: rate_limit_id UUID NOT NULL, user_id UUID NOT NULL, tenant_id VARCHAR(64) NOT NULL, endpoint VARCHAR(255) NOT NULL, request_count INTEGER NOT NULL, window_start TIMESTAMP NOT NULL, window_end TIMESTAMP NOT NULL. Khai báo khóa chính phức hợp pk_rate_limits trên bộ ba cột (rate_limit_id, endpoint, window_start). Khai báo khóa ngoại fk_rate_limits_user tham chiếu cột user_id của schema user_schema.users thông qua FOREIGN KEY (user_id) REFERENCES user_schema.users(user_id). Khai báo ràng buộc kiểm tra ck_rate_limits_endpoint với tập giá trị cho phép ('/api/v1/schedules', '/api/v1/recommendations', '/api/v1/rate-limits', '/api/v1/users') để ngăn chặn ghi log endpoint ngoài whitelist. Khai báo ràng buộc kiểm tra ck_rate_limits_count đảm bảo request_count >= 0. Tạo chỉ mục phụ trợ idx_rate_limits_window trên bộ ba cột (user_id, endpoint, window_start) để tối ưu truy vấn cửa sổ giới hạn tỷ lệ."]

---

### ⚙️ CORE SOFTWARE ENGINEER EXECUTION INSTRUCTION:

Please read the comprehensive architectural constraints, workspace folder routing rules, and sub-task specifications provided above. Automatically evaluate the extension profile of the target component path to implement either a backend Java component from scratch or a frontend TypeScript/Next.js/React asset from scratch.


Verify that any Java file generated strictly begins with the required `org.nlh4j.socialscheduler` package layout. Ensure that you read the exact Tag IDs from the `['[DAT-003]', '[DAT-ALL (1 to 3)]']` variable, and permanently burn those codes into the top-level comment block or Javadoc of the generated source code file. Emit the final compiler-ready production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

