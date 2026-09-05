# Security Compliance Matrix - Social Scheduler Platform
**Document Control:** `./sources/docs/architecture/SecurityComplianceMatrix.md`
**Project:** social-scheduler
**Version:** 1.0
**Classification:** Enterprise Confidential
**Targeted Tag IDs:** [DOC-001], [NFR-002]

---

## 1. Executive Summary

This document establishes the formal security compliance matrix for the `social-scheduler` microservices platform, mapping each applicable OWASP Top 10 (2021) category to concrete, auditable mitigation controls implemented across the system architecture. The matrix serves as the authoritative reference for security auditors, compliance officers, and engineering teams to verify that the platform meets enterprise-grade security standards.

**Scope:** All backend microservices (`user-service`, `schedule-service`, `ai-service`, `rate-limit-service`, `api-gateway`), infrastructure layer (GCP, GKE, Cloud SQL, Memorystore), and data pipelines (Kafka, Redis).

**Compliance Baseline:** OWASP Top 10 2021 + NIST 800-53 Rev.5 controls mapping.

---

## 2. Traceability Matrix Reference

| OWASP Category | OWASP ID | Mitigation Control | Implementation Location | Tag IDs |
| :--- | :--- | :--- | :--- | :--- |
| Broken Access Control | A01:2021 | RBAC 4-Role Enforcement (Admin, User, Scheduler, Analyst) | `api-gateway` SecurityConfig, `RbacPredicate`, Spring Security | [ARC-001], [ARC-002], [ARC-003], [ARC-004], [ARC-005], [ARC-006] |
| Cryptographic Failures | A02:2021 | TLS 1.3 End-to-End, JWT RS256, Key Rotation 90-day | `api-gateway` JwtDecoder, GCP Load Balancer, Cloud KMS | [NFR-002], [ARC-005] |
| Injection | A03:2021 | JPA Parameter Binding, Hibernate PreparedStatement, Whitelist Validation, HTML Sanitizer | `schedule-service` SchedulePayloadValidator, `user-service` Repository, `ai-service` OpenAIClient | [DAT-001], [DAT-002], [DAT-003], [REQ-003] |
| Insecure Design | A04:2021 | Rate Limiter Redis Token Bucket, Defense-in-Depth Gateway Filter | `rate-limit-service` RedisTokenBucketStrategy, `api-gateway` RateLimitGatewayFilter | [REQ-003], [EXC-005], [ARC-006] |
| Security Misconfiguration | A05:2021 | CORS Whitelist (No Wildcard), Security Headers (CSP, HSTS, X-Content-Type-Options) | `api-gateway` CorsFilter, Nginx Ingress ConfigMap, Spring Boot Actuator | [NFR-002], [NFR-003], [ARC-006] |
| Identification & Authentication Failures | A07:2021 | OAuth2 Resource Server, JWT Decoder, Token Expiry Handling (HTTP 401) | `api-gateway` SecurityConfig, JwtAuthFilter, GlobalExceptionHandler | [ARC-005], [EXC-002], [NFR-002] |
| Security Logging & Monitoring Failures | A09:2021 | Prometheus + Grafana, Structured Logging with Correlation ID, LogScrubbingInterceptor | `observability` Prometheus ConfigMap, Grafana Dashboard, SLF4J Logback | [NFR-001], [ARC-006], [EXC-001], [EXC-003] |

---

## 3. Detailed Control Mapping

### 3.1 A01:2021 — Broken Access Control → RBAC 4-Role Enforcement

**Threat Model:** Unauthorized access to administrative functions, cross-tenant data leakage, privilege escalation via API endpoint manipulation.

**Mitigation Architecture:**
The platform enforces a strict Role-Based Access Control (RBAC) model across four distinct organizational tiers:
- **[ARC-001] ADMIN:** Full administrative privileges across all multi-tenant boundaries, system configuration, and rate-limit resets.
- **[ARC-002] USER:** Standard tenant user privileges for creating, updating, and executing social media schedules.
- **[ARC-003] SCHEDULER:** Automated worker role with permissions to dispatch posts to external APIs (Facebook, Instagram, TikTok).
- **[ARC-004] ANALYST:** Read-only access to performance metrics and AI recommendation endpoints for historical analysis.

These roles are embedded directly within the JWT claims issued by `auth-service` and validated at the `api-gateway` via Spring Security's `RbacPredicate` and `@PreAuthorize` annotations on all microservice controllers. Multi-tenant isolation is enforced at the persistence layer using `tenant_id` column filters and schema-per-tenant isolation [DAT-001].

---

### 3.2 A02:2021 — Cryptographic Failures → TLS 1.3 & JWT RS256

**Threat Model:** Eavesdropping on transit data, token forgery, session hijacking, compromise of cryptographic secrets.

**Mitigation Architecture:**
- **[NFR-002] Transport Encryption:** All incoming and inter-service communications mandate TLS 1.3 with Perfect Forward Secrecy (PFS) terminated at the Google Cloud HTTPS Load Balancer.
- **Token Signing & Rotation:** JWT tokens are cryptographically signed using RS256 with 2048-bit RSA keys managed via GCP Cloud KMS. Cryptographic keys undergo an automated 90-day rotation cycle without service downtime.
- **Data at Rest:** All PostgreSQL databases in Cloud SQL and Redis caches in Memorystore are encrypted using Google-managed encryption keys (GMEK) at the storage block level.

---

### 3.3 A03:2021 — Injection → JPA Parameter Binding & HTML Sanitizer

**Threat Model:** SQL Injection (SQLi) via malicious payloads in schedule content or search parameters, Cross-Site Scripting (XSS) via injected JavaScript in social posts.

**Mitigation Architecture:**
- **SQL Injection Prevention:** All database operations utilize Spring Data JPA Repositories and Hibernate `PreparedStatement` parameter binding. Native queries are strictly forbidden unless vetted through `SafeSqlScanner`. Dynamic sorting is restricted via `ALLOWED_SORT_FIELDS` whitelists [DAT-001], [DAT-002], [DAT-003].
- **Input Sanitization:** User-supplied post content (`content` field) passes through the `HtmlSanitizer` (OWASP Java HTML Sanitizer) within `SchedulePayloadValidator` to strip malicious script tags, event handlers, and unauthorized markup before persistence [REQ-003].

---

### 3.4 A04:2021 — Insecure Design → Redis Token Bucket Rate Limiter

**Threat Model:** Denial of Service (DoS), API abuse, brute-force attacks on AI content generation endpoints.

**Mitigation Architecture:**
- **[REQ-003] Rate Limiting:** The `rate-limit-service` implements the Redis Token Bucket algorithm (`RedisTokenBucketStrategy`) maintaining atomicity via Lua scripts. Requests exceeding default thresholds (e.g., 100 tokens/minute per user) immediately trigger HTTP 429 (`RATE_LIMIT_EXCEEDED`) with a `Retry-After` header [EXC-005].
- **Defense-in-Depth:** The `RateLimitGatewayFilter` intercepts requests at the API Gateway layer, blocking unauthorized traffic before reaching downstream business microservices [ARC-006].

---

### 3.5 A05:2021 — Security Misconfiguration → CORS & Security Headers

**Threat Model:** Cross-Site Request Forgery (CSRF), MIME-sniffing, clickjacking, unauthorized cross-origin resource sharing.

**Mitigation Architecture:**
- **Strict CORS Policy:** Wildcard origins (`*`) are prohibited. `CorsFilter` dynamically validates incoming `Origin` headers against the tenant-specific whitelist stored in the `TENANT_ORIGINS` registry, enforcing exact matching and `Vary: Origin` response headers [NFR-003].
- **Security Headers:** Nginx Ingress and Spring Security enforce strict HTTP response headers across all routes:
  - `Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none';`
  - `X-Content-Type-Options: nosniff`
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains` [ARC-006].

---

### 3.6 A07:2021 — Identification & Authentication Failures → OAuth2 & JWT

**Threat Model:** Replay attacks, expired token abuse, stolen session exploitation, improper credential handling.

**Mitigation Architecture:**
- **OAuth2 Resource Server:** All microservices operate as stateless OAuth2 Resource Servers utilizing Spring Security 6 `JwtDecoder` to validate incoming Bearer tokens [ARC-005].
- **Token Expiry & Invalidation:** When a token is expired or structurally malformed, `JwtAuthFilter` and `GlobalExceptionHandler` intercept the exception and immediately return HTTP 401 (`TOKEN_EXPIRED`) with a structured error payload and correlation ID, prompting client-side re-authentication [EXC-002].

---

### 3.7 A09:2021 — Security Logging & Monitoring Failures → Observability & PII Scrubbing

**Threat Model:** Inadequate audit trails during security incidents, accidental exposure of PII in log aggregators, unmonitored upstream failures.

**Mitigation Architecture:**
- **[NFR-001] Observability Stack:** Prometheus and Grafana continuously monitor microservice health, P95 latencies, and HTTP 429 rate-limiting events.
- **Structured Logging & Correlation:** All SLF4J/Logback emissions include a unique `correlationId` injected into MDC for distributed tracing across Kafka event brokers and REST calls [EXC-001], [EXC-003].
- **PII Scrubbing:** The `LogScrubbingInterceptor` scans all log messages and API responses via regex patterns, automatically masking sensitive PII (emails, tokens, passwords) before emission to prevent leakage into log aggregators (Loki/Cloud Logging) [ARC-006].

---
*End of Security Compliance Matrix — [DOC-001], [NFR-002]*