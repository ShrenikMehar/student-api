# Student REST API

## Overview

This project implements a REST API for managing student records.

The service is built using **Kotlin** and the **Micronaut** framework and follows good backend engineering practices such as:

* layered architecture (controller → service → repository)
* database migrations using Flyway
* automated linting and formatting
* unit testing
* CI pipeline with GitHub Actions
* containerized deployment using Docker
* infrastructure provisioning using Vagrant
* Kubernetes orchestration using Minikube
* Helm charts for deployment management
* Secrets management using Vault and External Secrets Operator
* GitOps-based deployments using ArgoCD
* Observability stack with Prometheus, Loki, Grafana, and Promtail
* Grafana dashboards and Prometheus alert rules

Student data is stored in **PostgreSQL**.

---

## Features

The API supports:

* Create a student
* Retrieve all students
* Retrieve a student by ID
* Update student information
* Delete a student record
* Health check endpoint

---

## API Base Path

```
/api/v1/students
```

---

## API Endpoints

| Method | Endpoint              | Description                 |
| ------ | --------------------- | --------------------------- |
| POST   | /api/v1/students      | Create a new student        |
| GET    | /api/v1/students      | Retrieve all students       |
| GET    | /api/v1/students/{id} | Retrieve a specific student |
| PUT    | /api/v1/students/{id} | Update a student            |
| DELETE | /api/v1/students/{id} | Delete a student            |
| GET    | /healthcheck          | Service health status       |

---

## Tech Stack

* **Language:** Kotlin
* **Framework:** Micronaut
* **Database:** PostgreSQL
* **Database Migration:** Flyway
* **Build Tool:** Gradle
* **Testing:** JUnit 5
* **Logging:** Logback
* **Linting:** Detekt
* **Formatting:** ktlint
* **CI/CD:** GitHub Actions
* **Containerization:** Docker
* **Infrastructure:** Vagrant
* **Container Orchestration:** Kubernetes (Minikube)
* **Deployment Management:** Helm
* **Secrets Management:** Hashicorp Vault + External Secrets Operator
* **GitOps:** ArgoCD
* **Metrics:** Prometheus + kube-state-metrics + node-exporter
* **Logs:** Loki + Promtail
* **Visualization:** Grafana
* **Endpoint Monitoring:** Blackbox Exporter
* **DB Monitoring:** Postgres Exporter

---

# Running the Application

## Prerequisites

### Basic Tools

Install the following tools:

* Docker
* Make
* Java 21 (only required for local development)

Verify installation:

```
docker --version
make --version
java -version
```

### Kubernetes Tools (required for K8s deployment)

```
brew install kubectl
brew install minikube
brew install helm
brew install gettext
```

`gettext` provides `envsubst` which is required for applying ArgoCD manifests with environment variable substitution.

Verify installation:

```
kubectl version --client
minikube version
helm version
envsubst --version
```

### Colima Resource Requirements

This project runs a 4-node Minikube cluster with multiple services. Colima must be configured with sufficient resources:

```
colima start --cpu 4 --memory 8
```

Verify:

```
docker info | grep -E "CPUs|Memory"
```

---

## Run Using Docker (Recommended)

The easiest way to run the project locally is using Docker.

### Build the image

```
make build
```

### Start the application

```
make run
```

This starts:

* PostgreSQL database
* Student REST API

The API will be available at:

```
http://localhost:8080
```

Health check endpoint:

```
http://localhost:8080/healthcheck
```

### Stop the application

```
make stop
```

### View logs

```
make logs
```

---

## Local Development (Without Docker)

Start PostgreSQL:

```
make db-up
```

Run the application:

```
make local-run
```

Stop PostgreSQL:

```
make db-down
```

---

# Deployment Using Vagrant

The project can also run inside a **virtual machine** using Vagrant.
This simulates a production-like deployment environment where services run inside containers.

## Architecture

```
Client
   │
   ▼
Nginx
   │
   ├── Student API (instance 1)
   └── Student API (instance 2)
          │
          ▼
        PostgreSQL
```

---

## Infrastructure Layout

```
infra/
 ├── provision.sh
 ├── docker-compose.yml
 └── nginx/
     └── nginx.conf

Vagrantfile
```

---

# Installing Vagrant

## macOS (Using Homebrew)

```
brew install vagrant
```

Verify installation:

```
vagrant --version
```

---

## Apple Silicon (M1/M2/M3/M4) Setup

VirtualBox does **not support Apple Silicon processors**.

This project uses **UTM as the Vagrant provider** instead.

### Install UTM

Download from:

```
https://mac.getutm.app/
```

### Install the UTM provider plugin

```
vagrant plugin install vagrant_utm
```

Verify installation:

```
vagrant plugin list
```

Expected output:

```
vagrant_utm
```

---

## Vagrant Box Used

The project uses an ARM-compatible box:

```
utm/bookworm
```

This provides a **Debian 12 ARM64 environment** compatible with Apple Silicon.

---

# Running the Application in the VM

Start the VM:

```
vagrant up
```

SSH into the VM:

```
vagrant ssh
```

Start the services:

```
cd /vagrant/infra
docker-compose up -d
```

This will start:

* PostgreSQL container
* Student API containers
* Nginx reverse proxy

---

## Verify the API

```
curl http://localhost:8080/healthcheck
```

Expected response:

```
OK
```

---

## Stop Services

Inside the VM:

```
docker-compose down
```

Stop the VM:

```
vagrant halt
```

Destroy the VM:

```
vagrant destroy
```

---

# Deployment Using Kubernetes, Helm, ArgoCD and Observability

The project can be deployed on a local Kubernetes cluster using Minikube, Helm, and ArgoCD, with a full observability stack.

## Architecture

```
Client
   │
   ▼
K8s Service (NodePort)
   │
   ▼
Student API pods (minikube-m02)
   │
   ▼
PostgreSQL pod (minikube-m03)

Vault + ESO + ArgoCD + Observability (minikube-m04)
   │
   ├── Vault → stores DB credentials
   ├── ESO → injects credentials as K8s Secrets
   ├── ArgoCD → watches Git and syncs deployments
   └── Observability
       ├── Prometheus → scrapes metrics from all services
       ├── Grafana → visualizes metrics and logs
       ├── Loki → stores logs
       ├── Promtail → ships student-api logs to Loki
       ├── Postgres Exporter → exposes DB metrics to Prometheus
       └── Blackbox Exporter → monitors endpoint health
```

## Infrastructure Layout

```
infra/
 ├── helm/
 │   ├── student-api/       → custom chart for the API
 │   ├── postgres/          → custom chart for PostgreSQL
 │   ├── vault/             → Hashicorp Vault chart
 │   ├── external-secrets/  → External Secrets Operator chart
 │   ├── argocd/            → ArgoCD chart
 │   └── observability/     → observability stack values and configs
 │       ├── prometheus-values.yaml
 │       ├── loki-values.yaml
 │       ├── promtail-values.yaml
 │       ├── postgres-exporter-values.yaml
 │       ├── blackbox-exporter-values.yaml
 │       └── alerts/
 │           └── alert-rules.yaml
 └── argocd/
     ├── repository-secret.yaml
     └── applications/
         ├── student-api.yaml
         └── postgres.yaml
```

---

## Prerequisites

### GitHub Actions Secrets

Add the following secrets to your GitHub repository:

```
GitHub repo → Settings → Secrets and variables → Actions → New repository secret
```

Required secrets:

| Secret | Description |
| ------ | ----------- |
| DOCKER_USERNAME | DockerHub username |
| DOCKER_PASSWORD | DockerHub password |
| GITHUB_USERNAME | GitHub username for ArgoCD repo access |
| GITHUB_TOKEN | GitHub Personal Access Token with repo scope |

### GitHub Actions Permissions

Enable write permissions for GitHub Actions:

```
GitHub repo → Settings → Actions → General → Workflow permissions → Read and write permissions → Save
```

This is required for the CI pipeline to commit the updated image tag back to the repository.

### Environment File

Copy `.env.example` to `.env` and fill in the values:

```
cp .env.example .env
```

Required values:

```
DB_HOST=postgres
DB_PORT=5432
DB_NAME=students
DB_USER=postgres
DB_PASSWORD=postgres
VAULT_TOKEN=root
GITHUB_USERNAME=your_github_username
GITHUB_TOKEN=your_github_pat
```

---

## Start the Cluster

```
source .env && make k8s-up
```

This will:

* Start Minikube with 4 nodes
* Label nodes by role (application, database, dependent_services)
* Add required Helm repositories
* Deploy Vault, ESO, PostgreSQL, Student API, ArgoCD, and full observability stack
* Store DB credentials in Vault
* Apply ArgoCD repository secret and application manifests
* Apply Prometheus alert rules
* ArgoCD will automatically sync and manage deployments

---

## Access the API

```
make k8s-run
```

Keep the terminal open and use the returned URL to make requests:

```
http://127.0.0.1:<port>/healthcheck
```

---

## Access the ArgoCD UI

Get the ArgoCD UI URL:

```
make k8s-argocd-ui
```

Get the initial admin password:

```
make k8s-argocd-password
```

Login credentials:

```
username: admin
password: <output from above command>
```

---

## Access Grafana

Get the Grafana UI URL:

```
make k8s-grafana-ui
```

Login credentials:

```
username: admin
password: admin
```

### Pre-configured dashboards

Grafana comes with the following dashboards pre-loaded:

* **Node Exporter Full** — CPU, memory, disk and network per node
* **Kubernetes Cluster Overview** — pod restarts, deployments, resource usage
* **PostgreSQL Database** — connections, query stats, cache hit ratio
* **Blackbox Exporter** — endpoint uptime and response time for student-api, Vault, ArgoCD

### Pre-configured data sources

* **Prometheus** — for metrics
* **Loki** — for logs

### Useful Grafana queries

View student-api logs in Explore with Loki:

```
{job="student-api"}
```

View endpoint health in Explore with Prometheus:

```
probe_success
```

View Postgres up status in Explore with Prometheus:

```
pg_up
```

---

## Alert Rules

The following Prometheus alert rules are configured:

| Alert | Condition | Severity |
| ----- | --------- | -------- |
| HighCPUUsage | CPU > 80% for 5 min | warning |
| HighDiskUsage | Disk > 80% for 5 min | warning |
| HighErrorRate | 5xx errors > 0.1 req/s for 1 min | critical |
| HighRequestCount | Request rate > 100 req/s for 5 min | warning |
| HighP90Latency | p90 latency > 1s for 5 min | warning |
| HighP95Latency | p95 latency > 2s for 5 min | warning |
| HighP99Latency | p99 latency > 5s for 5 min | critical |
| PostgresRestarted | Postgres container restart count > 0 | critical |
| VaultRestarted | Vault container restart count > 0 | critical |
| ArgoCDRestarted | ArgoCD server restart count > 0 | critical |

View active alerts in Grafana → Alerting → Alert rules.

---

## Pause and Resume

Pause the cluster without losing any data or configuration:

```
make k8s-stop
```

Resume:

```
make k8s-start
```

After resuming, re-expose the services by running:

```
make k8s-run
make k8s-argocd-ui
make k8s-grafana-ui
```

---

## Full Teardown

```
make k8s-down
```

This deletes the entire Minikube cluster and all resources.

---

# Tested Environment

The infrastructure setup has been tested on:

```
MacBook (Apple Silicon M4)
macOS
Colima (4 CPU, 8GB RAM)
UTM
Vagrant
Docker
Minikube
Helm
ArgoCD
Prometheus + Grafana + Loki
```

---

# Running Tests

```
make local-test
```

---

# Code Quality

Run lint checks:

```
make lint
```

Format code automatically:

```
make format
```

---

# Continuous Integration

GitHub Actions runs the following on every push to `src/**`:

* Build
* Lint checks
* Formatting validation
* Unit tests
* Docker image build and push to DockerHub using Git commit SHA as image tag
* Update image tag in `infra/helm/student-api/values.yaml` and commit back to Git
* ArgoCD detects the updated tag in Git and automatically deploys the new version to the cluster

---

# Postman Collection

API requests can be tested using the provided Postman collection.

Location:

```
postman/student-api.postman_collection.json
```

Import the collection into Postman and run requests against the URL returned by:

```
make k8s-run
```

---

# Project Structure

```
src/main/kotlin
  controller     → REST controllers
  service        → business logic
  repository     → database access
  entity         → database entities
  dto            → request/response models

src/main/resources
  db/migration   → Flyway migrations

src/test/kotlin  → unit tests

infra
  docker-compose.yml   → local Docker setup
  provision.sh         → Vagrant VM provisioning
  nginx/               → Nginx load balancer config
  helm/                → Helm charts and values for Kubernetes deployment
    student-api/       → custom chart for the API
    postgres/          → custom chart for PostgreSQL
    vault-values.yaml  → Hashicorp Vault values
    external-secrets-values.yaml  → External Secrets Operator values
    argocd-values.yaml → ArgoCD values
    observability/     → observability stack values files and alert rules
  argocd/              → ArgoCD declarative configuration
    repository-secret.yaml
    applications/
      student-api.yaml
      postgres.yaml

scripts          → developer setup scripts
postman          → API testing collection
```

---

# Future Improvements

* Dockerfile optimisations to reduce build time
* Think of Multiple environment support
