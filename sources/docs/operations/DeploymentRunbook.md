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

Before initiating any deployment activities, ensure the following tools and permissions are available:

### Required Tools

| Tool | Minimum Version | Purpose |
| :--- | :--- | :--- |
| **Google Cloud CLI (`gcloud`)** | `450.0.0` | Authenticate and interact with GCP services |
| **Kubernetes CLI (`kubectl`)** | `1.28` | Manage GKE cluster resources |
| **Terraform CLI** | `1.6.0` | Provision infrastructure as code |
| **Docker** | `24.0.x` | Build and push container images |
| **Helm** | `3.12.x` | Install observability stack (Prometheus, Grafana) |

### IAM Permissions

The deploying user or service account must hold the following roles:

| Role | Scope | Justification |
| :--- | :--- | :--- |
| `roles/owner` | Project-wide | Full administrative access for initial setup |
| `roles/container.admin` | GKE cluster | Deploy and manage Kubernetes workloads |
| `roles/cloudsql.admin` | Cloud SQL instances | Manage database lifecycle |
| `roles/storage.admin` | GCS buckets | Access Terraform state backend |
| `roles/artifactregistry.writer` | Artifact Registry | Push container images |
| `roles/monitoring.viewer` | Monitoring | View metrics and dashboards |
| `roles/logging.viewer` | Logging | Access logs for troubleshooting |

> 🔒 **Security Note:** In production environments, replace `roles/owner` with least-privilege custom roles scoped to specific resources.

---

## 2. Infrastructure Provisioning via Terraform

This section outlines the process for provisioning the foundational GCP infrastructure using Terraform modules located at `./sources/infra/terraform/gcp/`.

### Step-by-Step Instructions

#### 2.1 Authenticate with GCP

```bash
gcloud auth login
gcloud config set project social-scheduler-prod
```

#### 2.2 Initialize Terraform Backend

Navigate to the Terraform root directory and initialize the backend:

```bash
cd ./sources/infra/terraform/gcp
terraform init
```

> ✅ This step initializes the GCS backend (`socialscheduler-tfstate`) and downloads required providers (`google`, `google-beta`).

#### 2.3 Preview Changes

Generate an execution plan to preview all infrastructure changes:

```bash
terraform plan -out=tfplan
```

> 📋 Review the output carefully. Confirm that VPC networks, subnets, GKE clusters, Cloud SQL instances, and Memorystore Redis instances are correctly defined.

#### 2.4 Apply Infrastructure

Apply the planned changes to provision the infrastructure:

```bash
terraform apply tfplan
```

> ⏱️ This operation may take 10–15 minutes depending on resource complexity.

### Provisioned Resources Summary

| Resource | Module File | Tag ID |
| :--- | :--- | :--- |
| Custom VPC Network | `./sources/infra/terraform/gcp/vpc.tf` | [NFR-002] |
| GKE Autopilot Cluster | `./sources/infra/terraform/gcp/gke.tf` | [NFR-002], [NFR-003] |
| Cloud SQL Instance | `./sources/infra/terraform/gcp/cloudsql.tf` | [NFR-002] |
| Memorystore Redis | `./sources/infra/terraform/gcp/memorystore.tf` | [NFR-002] |

---

## 3. Application Deployment to GKE

Once the infrastructure is provisioned, deploy the microservices to the GKE cluster using Kubernetes manifests stored under `./sources/infra/kubernetes/socialscheduler/`.

### Step-by-Step Instructions

#### 3.1 Configure Kubeconfig

Authenticate `kubectl` with the newly created GKE cluster:

```bash
gcloud container clusters get-credentials socialscheduler-gke --region asia-southeast1
```

#### 3.2 Create Namespace

Create a dedicated namespace for the application:

```bash
kubectl create namespace socialscheduler
```

#### 3.3 Apply Kubernetes Manifests

Deploy all base manifests using Kustomize:

```bash
kubectl apply -k ./sources/infra/kubernetes/socialscheduler/overlays/prod
```

> 🧩 This command applies Deployments, Services, HPAs, Ingresses, ConfigMaps, and Secrets defined in the `base` directory, customized for the `prod` overlay.

#### 3.4 Monitor Rollout Status

Verify that each deployment completes successfully:

```bash
kubectl rollout status deployment/schedule-service -n socialscheduler
kubectl rollout status deployment/user-service -n socialscheduler
kubectl rollout status deployment/ai-service -n socialscheduler
kubectl rollout status deployment/rate-limit-service -n socialscheduler
```

> ✅ Wait until all deployments report `successfully rolled out`.

---

## 4. Rollback Procedures

In case of failed deployments or critical issues post-deployment, follow these rollback steps.

### 4.1 Rollback a Specific Deployment

To revert a deployment to its previous revision:

```bash
kubectl rollout undo deployment/schedule-service -n socialscheduler
```

Repeat for other affected services as needed.

### 4.2 Rollback All Services

If multiple services require rollback:

```bash
kubectl rollout undo deployment/user-service -n socialscheduler
kubectl rollout undo deployment/schedule-service -n socialscheduler
kubectl rollout undo deployment/ai-service -n socialscheduler
kubectl rollout undo deployment/rate-limit-service -n socialscheduler
```

### 4.3 Verify Rollback Completion

Confirm that the rollback was successful:

```bash
kubectl rollout status deployment/schedule-service -n socialscheduler
```

---

## 5. Post-Deployment Validation Checklist

After deployment, perform the following checks to validate system health and functionality.

### 5.1 Smoke Test Endpoints

Ensure all services respond to health checks:

```bash
curl -H "Authorization: Bearer <valid_jwt_token>" \
  https://api.socialscheduler.local/api/v1/schedules/health

curl -H "Authorization: Bearer <valid_jwt_token>" \
  https://api.socialscheduler.local/api/v1/users/health

curl -H "Authorization: Bearer <valid_jwt_token>" \
  https://api.socialscheduler.local/api/v1/ai/recommendations/health

curl -H "Authorization: Bearer <valid_jwt_token>" \
  https://api.socialscheduler.local/api/v1/rate-limits/health
```

Expected Response:
```json
{"status": "UP"}
```

### 5.2 Validate Prometheus Metrics

Query Prometheus to confirm service availability:

```bash
kubectl port-forward svc/prometheus-server -n observability 9090 &
curl "http://localhost:9090/api/v1/query?query=up"
```

Expected Output:
```json
{
  "status": "success",
  "data": {
    "resultType": "vector",
    "result": [
      {
        "metric": {"job": "socialscheduler-services"},
        "value": ["<timestamp>", "1"]
      }
    ]
  }
}
```

### 5.3 Check Grafana Dashboard

Access the Grafana dashboard via port-forward:

```bash
kubectl port-forward svc/grafana -n observability 3000:3000 &
open http://localhost:3000
```

Import the pre-configured dashboard JSON from:
`./sources/infra/observability/grafana-dashboard.json`

Verify panels show:
- HTTP Request Latency P95 < 200ms ([NFR-001])
- Rate Limited Requests (HTTP 429) within acceptable thresholds ([REQ-003])
- CPU/Memory utilization per pod within limits ([NFR-003])

---

## 6. Emergency Troubleshooting Guide

### 6.1 HTTP 429 Flood – Rate Limit Exhaustion

**Symptom:** Clients receive excessive `429 Too Many Requests` responses.

**Resolution Steps:**

1. Identify top offending users/IPs:
   ```bash
   kubectl logs -n socialscheduler -l app=rate-limit-service | grep "RATE_LIMIT_EXCEEDED"
   ```

2. Temporarily increase Redis Token Bucket capacity:
   ```bash
   kubectl exec -n socialscheduler deploy/rate-limit-service -- \
     redis-cli CONFIG SET maxmemory-policy ALLKEYS-LRU
   ```

3. Scale up `rate-limit-service` pods:
   ```bash
   kubectl scale deployment/rate-limit-service --replicas=5 -n socialscheduler
   ```

4. Investigate root cause via Prometheus alert:
   ```promql
   sum(rate(http_server_requests_seconds_count{status="429"}[5m])) > 100
   ```

### 6.2 Kafka Consumer Job Failure

**Symptom:** Scheduled posts are not being published to social platforms.

**Resolution Steps:**

1. Check Kafka consumer group lag:
   ```bash
   kubectl exec -n kafka kafka-cluster-0 -- \
     bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
     --describe --group schedule-execution-group
   ```

2. Restart the consumer pod:
   ```bash
   kubectl delete pod -n socialscheduler -l app=schedule-service
   ```

3. Inspect logs for errors:
   ```bash
   kubectl logs -n socialscheduler -l app=schedule-service --tail=100
   ```

4. Manually trigger reprocessing if necessary:
   ```bash
   kubectl exec -n socialscheduler deploy/schedule-service -- \
     java -jar app.jar --spring.profiles.active=manual-replay
   ```

### 6.3 Cloud SQL Disk Space Exhaustion

**Symptom:** Database queries fail with `disk full` or `connection refused`.

**Resolution Steps:**

1. Check disk usage:
   ```bash
   gcloud sql instances list --project social-scheduler-prod
   gcloud sql instances describe socialscheduler-db --project social-scheduler-prod
   ```

2. Increase disk size:
   ```bash
   gcloud sql instances patch socialscheduler-db \
     --project social-scheduler-prod \
     --activation-timeout=10m \
     --database-flags=cloudsql.enable_pgaudit.on=1
   ```

3. Enable automatic storage increase:
   ```bash
   gcloud sql instances patch socialscheduler-db \
     --project social-scheduler-prod \
     --storage-auto-increase
   ```

4. Clean up old logs:
   ```bash
   gcloud sql connect socialscheduler-db --user=postgres --command="
     SELECT pg_logfile('pg_logical');
     TRUNCATE TABLE performance_metrics WHERE collected_at < NOW() - INTERVAL '30 days';
   "
   ```

---

## 7. Traceability Matrix Reference

This section maps key deployment components and procedures back to their originating requirement tags.

| Component / Procedure | File Path | Tag ID(s) |
| :--- | :--- | :--- |
| Terraform VPC Module | `./sources/infra/terraform/gcp/vpc.tf` | [NFR-002] |
| Terraform GKE Module | `./sources/infra/terraform/gcp/gke.tf` | [NFR-002], [NFR-003] |
| Terraform Cloud SQL Module | `./sources/infra/terraform/gcp/cloudsql.tf` | [NFR-002] |
| Terraform Memorystore Module | `./sources/infra/terraform/gcp/memorystore.tf` | [NFR-002] |
| Kubernetes Deployment | `./sources/infra/kubernetes/socialscheduler/base/deployment.yaml` | [NFR-003] |
| Kubernetes Service | `./sources/infra/kubernetes/socialscheduler/base/service.yaml` | [NFR-003] |
| Kubernetes HPA | `./sources/infra/kubernetes/socialscheduler/base/hpa.yaml` | [NFR-003] |
| Kubernetes Ingress | `./sources/infra/kubernetes/socialscheduler/base/ingress.yaml` | [NFR-003] |
| Kubernetes ConfigMap | `./sources/infra/kubernetes/socialscheduler/base/configmap.yaml` | [NFR-003] |
| Prometheus Configuration | `./sources/infra/observability/prometheus.yaml` | [NFR-001] |
| Grafana Dashboard | `./sources/infra/observability/grafana-dashboard.json` | [NFR-001] |
| Smoke Test Endpoint | `/actuator/health` | [NFR-001] |
| Rate Limiter Logic | `RedisTokenBucketStrategy.java` | [REQ-003], [EXC-005] |
| Kafka Consumer Group | `schedule-execution-group` | [EXC-001] |
| Cloud SQL Instance | `socialscheduler-db` | [NFR-002] |
| Multi-Tenant Isolation | Schema-per-tenant model | [NFR-003] |

---

**End of Document**
```