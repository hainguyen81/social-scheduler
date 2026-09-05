```markdown
# Social Scheduler - CI/CD Pipeline Documentation

**Document Version:** 1.3  
**Last Updated:** 2026-08-31  
**Author:** Enterprise System Architect (SA Agent) & Senior Technical Writer  
**Approval Status:** Approved for Technical Review  
**Target Tag IDs:** [DOC-001], [NFR-001], [NFR-002], [NFR-003], [REQ-001], [REQ-002], [REQ-003]

---

## 1. Overview

This document provides a comprehensive specification of the Continuous Integration and Continuous Deployment (CI/CD) pipeline for the `social-scheduler` microservices platform. The pipeline is implemented using **GitHub Actions** and orchestrates nine sequential stages from code validation to production deployment. The pipeline enforces strict quality gates, security scanning, and approval workflows to ensure enterprise-grade delivery standards.

**Traceability Matrix Reference:** All pipeline stages map to non-functional requirement **[NFR-001]** (Performance & Observability), **[NFR-002]** (Security & Compliance), **[NFR-003]** (Scalability & High Availability), functional requirements **[REQ-001]**, **[REQ-002]**, **[REQ-003]**, and documentation requirement **[DOC-001]**.

---

## 2. Pipeline Architecture

### 2.1 Pipeline Stages Overview

| Stage | Name | Purpose | Quality Gate | Target Tag IDs |
|-------|------|---------|--------------|----------------|
| 1 | `lint` | Static code analysis (Checkstyle, SpotBugs, ESLint) | Zero violations | [NFR-002], [DOC-001] |
| 2 | `unit-test` | Unit test execution (JUnit 5, Mockito, Jest) | Coverage ≥ 85% | [NFR-001], [DOC-001] |
| 3 | `integration-test` | Integration tests with Testcontainers | All tests pass | [NFR-001], [NFR-003], [DOC-001] |
| 4 | `build-image` | Multi-stage Docker image build | Successful build | [NFR-001], [NFR-003], [DOC-001] |
| 5 | `push-image` | Push images to Google Artifact Registry | Images pushed | [NFR-003], [DOC-001] |
| 6 | `deploy-staging` | Deploy to GKE staging namespace | Pods ready | [NFR-003], [DOC-001] |
| 7 | `smoke-test` | Health checks and metrics validation | All endpoints healthy | [NFR-001], [NFR-003], [DOC-001] |
| 8 | `approval` | Manual approval gate (Technical Lead) | Approval granted | [NFR-002], [DOC-001] |
| 9 | `deploy-prod` | Production deployment with rolling update | Rollout complete | [NFR-001], [NFR-003], [DOC-001] |

### 2.2 Pipeline Flow Diagram

```mermaid
flowchart TD
    A[Push/PR to develop/main] --> B[Stage 1: lint]
    B -->|Checkstyle + SpotBugs + ESLint| C[Stage 2: unit-test]
    C -->|JUnit 5 + Mockito + Jest<br/>Coverage ≥ 85%| D[Stage 3: integration-test]
    D -->|Testcontainers: PostgreSQL, Redis, Kafka| E[Stage 4: build-image]
    E -->|Multi-stage Docker Build<br/>4 Services| F[Stage 5: push-image]
    F -->|Push to Artifact Registry<br/>asia-southeast1-docker.pkg.dev| G[Stage 6: deploy-staging]
    G -->|kubectl apply -k staging| H[Stage 7: smoke-test]
    H -->|/actuator/health + Metrics| I[Stage 8: approval]
    I -->|GitHub Environment<br/>Technical Lead Approval| J[Stage 9: deploy-prod]
    J -->|Rolling Update Strategy| K[Production Live]

    style B fill:#e1f5fe,stroke:#01579b
    style C fill:#e8f5e9,stroke:#1b5e20
    style D fill:#fff3e0,stroke:#e65100
    style E fill:#f3e5f5,stroke:#4a148c
    style F fill:#fce4ec,stroke:#880e4f
    style G fill:#e0f2f1,stroke:#004d40
    style H fill:#f1f8e9,stroke:#33691e
    style I fill:#fff8e1,stroke:#f57f17
    style J fill:#e8eaf6,stroke:#1a237e