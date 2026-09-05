```markdown
# Deployment Runbook - Social Scheduler Production GCP

## 📋 TRACEABILITY MATRIX REFERENCE
| Section | Targeted Tag IDs | Description |
| :--- | :--- | :--- |
| Prerequisites & Tooling | [DOC-001] | Software dependencies, versions, and IAM role requirements |
| Infrastructure Provisioning (Terraform) | [NFR-002], [DOC-001] | GCP infrastructure deployment via Terraform (VPC, GKE, Cloud SQL, Memorystore) |
| Application Deployment (Kubernetes) | [NFR-003], [DOC-001] | Application manifests deployment, namespace, and rollout verification |
| Rollback & Post-Deployment Verification | [NFR-003], [DOC-001] | Rollback procedures, smoke testing, Prometheus metrics, and Grafana verification |
| Emergency Commands & Incident Response | [NFR-001], [NFR-002], [EXC-001], [EXC-002], [EXC-003], [EXC-005], [DOC-001] | Incident response for HTTP 429 rate limit spikes, Kafka failures, DB pool exhaustion, and storage expansion |
| Deployment Workflow Sequence | [NFR-001], [NFR-002], [NFR-003], [DOC-001] | End-to-end deployment lifecycle sequence diagram |

---

## 🔄 DEPLOYMENT LIFECYCLE SEQUENCE DIAGRAM [NFR-001], [NFR-002], [NFR-003], [DOC-001]

```mermaid
sequenceDiagram
    autonumber
    actor Ops as DevOps Engineer
    participant GCP as Google Cloud Platform
    participant GKE as GKE Cluster
    participant Prom as Prometheus
    participant Graf as Grafana

    Ops->>GCP: gcloud auth login & gcloud config set project
    Ops->>GCP: cd ./sources/infra/terraform/gcp && terraform init/plan/apply
    GCP-->>Ops: VPC, GKE, Cloud SQL, Memorystore Provisioned
    Ops->>GKE: gcloud container clusters get-credentials socialscheduler-gke
    Ops->>GKE: kubectl create namespace socialscheduler
    Ops->>GKE: kubectl apply -k ./sources/infra/kubernetes/socialscheduler/overlays/prod
    GKE-->>Ops: Deployments, Services, HPA, Ingress & ConfigMaps Applied
    Ops->>GKE: kubectl rollout status deployment/schedule-service -n socialscheduler
    GKE-->>Ops: Deployment successfully rolled out
    Ops->>Prom: GET /api/v1/query?query=up
    Prom-->>Ops: 200 OK (All metrics scraping targets active)
    Ops->>Graf: Import & verify dashboard socialscheduler-overview.json
    Graf-->>Ops: Verification complete (Latency P95 < 200ms)
```

---

## 🏗️ PART 1: PREREQUISITES & TOOLING [DOC-001]

### 1.1. Required tool versions
- **gcloud CLI**: Version `450.0.0` or later. Install and authenticate: `gcloud auth login`.
- **kubectl**: Version `1.28` or later. Must match the GKE cluster version.
- **Terraform**: Version `1.6.0` or later. Download from HashiCorp releases.
- **IAM roles**: The executing account needs:
  - `roles/owner` (or `roles/container.admin` for GKE operations)
  - `roles/cloudsql.admin` (manage Cloud SQL instances)
  - `roles/redis.admin` (manage Memorystore instances)
  - `roles/iam.serviceAccountUser` (assign service accounts to workloads)

### 1.2. Environment verification
```bash
# Verify gcloud version
gcloud version

# Verify kubectl client version
kubectl version --client

# Verify terraform version
terraform version
```

### 1.3. Authentication steps
```bash
# Login to Google Cloud
gcloud auth login

# Set default project (replace with actual project ID)
gcloud config set project social-scheduler-prod

# Set default region for Asia‑Southeast1
gcloud config set region asia-southeast1
```

---

## 🏗️ PART 2: INFRASTRUCTURE PROVISIONING (TERRAFORM) [NFR-002], [DOC-001]

### 2.1. Initialize Terraform backend (GCS)
```bash
# Navigate to the Terraform GCP directory
cd ./sources/infra/terraform/gcp

# Initialize backend for state management in GCS bucket
terraform init \
  -backend-config="bucket=socialscheduler-tfstate" \
  -backend-config="prefix=terraform/state/prod"
```

### 2.2. Create an execution plan (plan)
```bash
# Generate an execution plan file
terraform plan -out=tfplan

# Review the planned resources
terraform show tfplan
```

### 2.3. Apply the infrastructure (apply)
```bash