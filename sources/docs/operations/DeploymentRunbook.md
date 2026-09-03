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

> 🧩 This command applies Deployments, Services, HPAs