```markdown
# Social Scheduler - Production Deployment Runbook

**Document ID:** DOC-001  
**Target Environment:** Google Cloud Platform (GCP) — Production  
**Last Updated:** 2026-08-31  
**Author:** Enterprise System Architect (SA Agent)  
**Status:** Approved  

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)  
2. [Infrastructure Provisioning via Terraform](#2-infrastructure-provisioning-via-terraform)  
3. [Application Deployment to GKE](#3-application-deployment-to-gke)  
4. [Rollback Procedures](#4-rollback-procedures)  
5. [Post-Deployment Validation Checklist](#5-post-deployment-validation-checklist)  
6. [Emergency Troubleshooting Guide](#6-emergency-troubleshooting-guide)  
7. [Traceability Matrix Reference](#7-traceability-matrix-reference)

---

## 1. Prerequisites

Before initiating any deployment activities, ensure the following tools and permissions are available: [DOC-001]

### Required Tools

| Tool | Minimum Version | Purpose | Targeted Tag IDs |
| :--- | :--- | :--- | :--- |
| **Google Cloud CLI (`gcloud`)** | `450.0.0` | Authenticate and interact with GCP services | [DOC-001] |
| **Kubernetes CLI (`kubectl`)** | `1.28` | Manage GKE cluster resources | [DOC-001] |
| **Terraform CLI** | `1.6.0` | Provision infrastructure as code | [NFR-002], [DOC-001] |
| **Docker** | `24.0.x` | Build and push container images | [NFR-001], [DOC-001] |
| **Helm** | `3.12.x` | Install observability stack (Prometheus, Grafana) | [NFR-001], [DOC-001] |

### IAM Permissions

The deploying user or service account must hold the following roles: [NFR-002]

| Role | Scope | Justification | Targeted Tag IDs |
| :--- | :--- | :--- | :--- |
| `roles/owner` | Project-wide | Full administrative access for initial setup | [NFR-002] |
| `roles/container.admin` | GKE cluster | Deploy and manage Kubernetes workloads | [NFR-003] |
| `roles/cloudsql.admin` | Cloud SQL instances | Manage database lifecycle | [DAT-001] |
| `roles/storage.admin` | GCS buckets | Access Terraform state backend | [DOC-001] |
| `roles/artifactregistry.writer` | Artifact Registry | Push container images | [NFR-001] |
| `roles/monitoring.viewer` | Monitoring | View metrics and dashboards | [NFR-001] |
| `roles/logging.viewer` | Logging | Access logs for troubleshooting | [NFR-001] |

> 🔒 **Security Note:** In production environments, replace `roles/owner` with least-privilege custom roles scoped to specific resources [NFR-002].

---

## 2. Infrastructure Provisioning via Terraform

This section outlines the process for provisioning the foundational GCP infrastructure using Terraform modules located at `./sources/infra/terraform/gcp/` [NFR-002].

### Step-by-Step Instructions

#### 2.1 Authenticate with GCP

```bash
gcloud auth login
gcloud config set project social-scheduler-prod
```

#### 2.2 Initialize Terraform Backend

Navigate to the Terraform root directory `./sources/infra/terraform/gcp` and initialize the backend: [DOC-001]

```bash
cd ./sources/infra/terraform/gcp
terraform init
```

> ✅ This step initializes the GCS backend (`socialscheduler-tfstate`) and downloads required providers (`google`, `google-beta`).

#### 2.3 Preview Changes

Generate an execution plan to preview all infrastructure changes: [DOC-001]

```bash
terraform plan -out=tfplan
```

> 📋 Review the output carefully. Confirm that VPC networks, subnets, GKE clusters, Cloud SQL instances, and Memorystore Redis instances are correctly defined.

#### 2.4 Apply Infrastructure

Apply the planned changes to provision the infrastructure [NFR-002]:

```bash
terraform apply tfplan
```

> ⏱️ This operation may take 10–15 minutes depending on resource complexity.

### Provisioned Resources Summary

| Resource | Module File Path | Targeted Tag IDs |
| :--- | :--- | :--- |
| Custom VPC Network | `./sources/infra/terraform/gcp/vpc.tf` | [NFR-002] |
| GKE Autopilot Cluster | `./sources/infra/terraform/gcp/gke.tf` | [NFR-002], [NFR-003] |
| Cloud SQL Instance | `./sources/infra/terraform/gcp/cloudsql.tf` | [NFR-002], [DAT-001] |
| Memorystore Redis | `./sources/infra/terraform/gcp/memorystore.tf` | [NFR-002], [REQ-003] |

---

## 3. Application Deployment to GKE

Once the infrastructure is provisioned, deploy the microservices to the GKE cluster using Kubernetes manifests stored under `./sources/infra/kubernetes/socialscheduler/` [NFR-003].

### Step-by-Step Instructions

#### 3.1 Configure Kubeconfig

Authenticate `kubectl` with the newly created GKE cluster: [NFR-003]

```bash
gcloud container clusters get-credentials socialscheduler-gke --region asia-southeast1
```

#### 3.2 Create Namespace

Create a dedicated namespace for the application: [NFR-003]

```bash
kubectl create namespace socialscheduler
```

#### 3.3 Apply Kubernetes Manifests

Deploy all base manifests and overlays using Kustomize: [NFR-003]

```bash
kubectl apply -k ./sources/infra/kubernetes/socialscheduler/overlays/prod
```

> 🧩 This command applies Deployments, Services, HPAs, Ingress, and ConfigMaps from `./sources/infra/kubernetes/socialscheduler/base/`.

#### 3.4 Verify Deployment Rollout Status

Monitor the rollout status of core services to ensure successful pod initialization [NFR-003]:

```bash
kubectl rollout status deployment/schedule-service -n socialscheduler
kubectl rollout status deployment/user-service -n socialscheduler
kubectl rollout status deployment/ai-service -n socialscheduler
kubectl rollout status deployment/rate-limit-service -n socialscheduler
```

---

## 4. Rollback Procedures

If a newly deployed version exhibits critical failures, execute the rollback procedure immediately to restore service stability [NFR-003], [EXC-003].

### 4.1 Rollback Deployment via Kubectl

Roll back the deployment to the previous stable revision:

```bash
kubectl rollout undo deployment/schedule-service -n socialscheduler
kubectl rollout undo deployment/user-service -n socialscheduler
kubectl rollout undo deployment/ai-service -n socialscheduler
kubectl rollout undo deployment/rate-limit-service -n socialscheduler
```

### 4.2 Verify Rollback Status

Confirm that all pods have successfully reverted to the stable image version:

```bash
kubectl get pods -n socialscheduler -o wide
```

---

## 5. Post-Deployment Validation Checklist

After completing the deployment to GKE, execute the following validation steps to ensure system health and performance compliance [NFR-001], [DOC-001]:

### 5.1 Smoke Test Health Endpoints

Perform HTTP GET requests to verify that all service health endpoints return HTTP 200 UP status:

```bash
curl -i https://api.socialscheduler.local/api/v1/schedules/health
curl -i https://api.socialscheduler.local/api/v1/ai/recommendations/health
```

### 5.2 Validate Prometheus Metrics Collection

Query Prometheus via the API to verify metric collection across all microservices [NFR-001]:

```bash
curl -G "http://prometheus.observability.svc.cluster.local:9090/api/v1/query" --data-urlencode "query=up"
```

> ✅ Expected output: A list of active targets with value `1` for all deployed services.

### 5.3 Grafana Dashboard Verification

Access the Grafana web console and load the `socialscheduler-overview.json` dashboard located at `./sources/infra/observability/grafana-dashboard.json`. Verify that:
- P95 Latency remains below 200ms [NFR-001].
- HTTP 429 Rate Limit error counts are within normal operating bounds [REQ-003].
- CPU and Memory utilizations per pod are stable and well within HPA thresholds [NFR-003].

---

## 6. Emergency Troubleshooting Guide

This section provides operational runbooks for resolving critical production incidents.

### 6.1 Scenario A: HTTP 429 Rate Limit Flooding

* **Symptom:** Clients report widespread HTTP 429 Too Many Requests errors; rate-limit service metrics spike [EXC-005], [REQ-003].
* **Root Cause:** Traffic surge exceeding the Redis Token Bucket capacity or potential DDoS attack.
* **Resolution Steps:**
  1. Inspect Redis memory and connection pool status in Memorystore.
  2. Temporarily increase token bucket capacity in `./sources/backend/rate-limit-service/src/main/resources/application.yml` or via ConfigMap update:
     ```bash
     kubectl set env deployment/rate-limit-service RATE_LIMIT_CAPACITY=200 -n socialscheduler
     ```
  3. If traffic is malicious, enable Google Cloud Armor security policy on the Ingress gateway.

### 6.2 Scenario B: Kafka Consumer Lag Spike

* **Symptom:** Scheduled posts are not published on time; Kafka consumer lag exceeds 1000 messages [NFR-001].
* **Root Cause:** Upstream third-party social platform API (Facebook, Instagram, TikTok) throttling or downtime [EXC-001].
* **Resolution Steps:**
  1. Check Kafka consumer group status:
     ```bash
     kubectl exec -it kafka-broker-0 -n kafka -- kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group schedule-consumer-group
     ```
  2. Inspect application logs for `SocialPlatformException` errors:
     ```bash
     kubectl logs -l app=schedule-service -n socialscheduler --tail=100 | grep "SocialPlatformException"
     ```
  3. Scale up `schedule-service` replicas to handle backlog processing:
     ```bash
     kubectl scale deployment/schedule-service --replicas=10 -n socialscheduler
     ```

### 6.3 Scenario C: Cloud SQL Database Storage Exhaustion

* **Symptom:** Database connections fail; application logs report SQL connection timeouts or disk full errors [DAT-001], [EXC-003].
* **Root Cause:** Unbounded growth of `performance_metrics` or audit logs.
* **Resolution Steps:**
  1. Check Cloud SQL instance disk utilization in GCP Console.
  2. Resize storage interactively via Terraform (`./sources/infra/terraform/gcp/cloudsql.tf`) or GCP Console.
  3. Execute data cleanup script for expired rate limit windows or old performance metrics:
     ```sql
     DELETE FROM rate_limit_schema.rate_limits WHERE window_end < NOW() - INTERVAL '7 days';
     ```

---

## 7. Traceability Matrix Reference

| Requirement ID | Architectural Domain | Description / Compliance Target | Document Section |
| :--- | :--- | :--- | :--- |
| **[DOC-001]** | Enterprise Documentation | Production deployment runbook and operational guidelines | Sections 1, 2, 3, 4, 5, 6 |
| **[NFR-001]** | Performance & Observability | P95 latency < 200ms, Prometheus/Grafana monitoring | Sections 1, 5 |
| **[NFR-002]** | Security & Compliance | Terraform GCP provisioning, least-privilege IAM, OWASP Top 10 | Sections 1, 2 |
| **[NFR-003]** | Cloud Architecture | GKE Autopilot, Kubernetes manifests, HPA, horizontal scaling | Sections 2, 3, 4 |
| **[REQ-001]** | Core Business Logic | Multi-platform scheduling (Facebook, Instagram, TikTok) | Sections 5, 6 |
| **[REQ-003]** | Rate Limiting | Redis Token Bucket rate limiter, HTTP 429 response handling | Sections 5, 6 |
| **[DAT-001]** | Database & Persistence | Schema-per-tenant, Cloud SQL PostgreSQL persistence | Sections 1, 6 |
| **[EXC-001]** | Fault Tolerance | Handling third-party SDK and network failures | Section 6 |
| **[EXC-005]** | Rate Limit Exception | Proper HTTP 429 formatting and `Retry-After` header | Section 6 |
```