# Day 2: model models/gemini-flash-lite-latest - API Endpoint https://generativelanguage.googleapis.com/v1beta/openai
* **Production source codebase at SOURCE destination**: INTEGRATION_SCOPE
* **Production source codebase generated at TARGET destination**: ./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql
* **📝 Prompt / Tasks / Data**:
### 🏢 ENTERPRISE SYSTEM DATA LAYER INJECTION
*   Target Project Identity Safe Name: social-scheduler
*   Enforced Java Package Prefix Base: org.nlh4j.socialscheduler
*   Target Component Destination Path: `./sources/backend/schedule-service/src/main/resources/db/migration/V1__init_schedules.sql` (Must map to sources/backend/ or sources/frontend/)
*   Context Module Context Reference Path: `INTEGRATION_SCOPE`

### SOURCE CODE UNDER AUDIT (VERIFICATION TARGET)
* **Target Code Component Payload For Comprehensive Review:**
<EXISTING_CODE_UNDER_AUDIT>

</EXISTING_CODE_UNDER_AUDIT>


### ❌ REAL RAW COMPILER ERROR LOGS (CRITICAL FIX TARGET)
The codebase above triggered the following compiler or runtime exceptions. You MUST analyze this stack trace or log error text to pinpoint and auto-patch the root cause:
```text
True
```
*   Operational Modality Activated: COMPILER_FIXER_MODE


### 📋 EXECUTION SUB-TASKS TO ENFORCE
['Rà soát toàn bộ bốn tệp Flyway DDL đã được sinh ra tại các tác vụ con 2.1 đến 2.4 nhằm đảm bảo tính toàn vẹn tham chiếu giữa các schema. Xác minh ràng buộc khóa ngoại fk_schedules_user tham chiếu chính xác cột user_id của schema user_schema.users. Xác minh ràng buộc khóa ngoại fk_performance_schedule tham chiếu chính xác cột schedule_id của schema schedule_schema.schedules. Xác minh ràng buộc khóa ngoại fk_rate_limits_user tham chiếu chính xác cột user_id của schema user_schema.users. Xác minh mọi cột tenant_id đều có chỉ mục phụ trợ nhằm đảm bảo hiệu năng truy vấn đa tenant. Đánh giá chiến lược schema-per-tenant và đề xuất bổ sung cột tenant_id vào khóa chính phức hợp của bảng schedules và performance_metrics nếu cần thiết. Kiểm tra tính nhất quán của tập giá trị enum trong các ràng buộc CHECK giữa schema và logic nghiệp vụ dự kiến. Rà soát việc sử dụng kiểu UUID cho khóa chính nhằm đảm bảo chống enumeration attack theo OWASP A07. Lập báo cáo review ghi nhận các phát hiện và đề xuất cải tiến.']

---

### ⚙️ LEAD REVIEWER/FIXER EXECUTION INSTRUCTION:
Please read the comprehensive architectural constraints, workspace folder routing rules, sub-task specifications, and potential compiler log streams provided above. Automatically evaluate the error signatures or static code anomalies to construct either a fully patched backend Java component or a frontend TypeScript/Next.js asset. 

Ensure that you read the exact Tag IDs from the `['[DAT-001]', '[DAT-002]', '[DAT-003]', '[DAT-ALL (1 to 3)]']` variable, merge them seamlessly with any pre-existing tags inside the source payload, and permanently inject/preserve those exact Tag IDs within the generated code's Javadoc documentation or file header comments. The final code must be completely self-contained and compiler-ready. Emit the production source code inside a single valid markdown code block now.
* **📝 Response**:
```sql
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
```

