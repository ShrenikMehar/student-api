# SRE Bootcamp — Project Context Document

## Purpose
This document provides full context for:
1. Refactoring and improving the codebase and infrastructure
2. Suggesting improvements to the One2N SRE Bootcamp curriculum

---

## Project Overview

**Repository:** https://github.com/ShrenikMehar/StudentManagement

**Goal:** Build a production-style Student Management REST API, progressively deploying and operating it through a series of SRE exercises covering containerization, CI/CD, Kubernetes, GitOps, and observability.

**Developer:** Shrenik Mehar, Software Engineer at One2N Consulting, Pune
**Background:** Strong Kotlin/Micronaut backend experience, minimal prior K8s/infra experience before this bootcamp.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Micronaut |
| Build | Gradle |
| Database | PostgreSQL |
| Migrations | Flyway (embedded in app) |
| Testing | JUnit 5 + MockK |
| Logging | SLF4J + Logback |
| Code Quality | Detekt + ktlint |
| Containerization | Docker (multi-stage) |
| Local Dev | Docker Compose |
| CI/CD | GitHub Actions (self-hosted runner) |
| VM Infra | Vagrant + UTM (Apple Silicon M4) |
| Orchestration | Kubernetes (Minikube, 4 nodes) |
| Package Manager | Helm |
| Secrets | Hashicorp Vault (dev mode) + ESO |
| GitOps | ArgoCD |
| Metrics | Prometheus + kube-prometheus-stack |
| Logs | Loki + Promtail |
| Visualization | Grafana |
| Endpoint Monitoring | Blackbox Exporter |
| DB Monitoring | Postgres Exporter |

---

## API Design

**Base path:** `/api/v1/students`

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/students | Create student |
| GET | /api/v1/students | Get all students |
| GET | /api/v1/students/{id} | Get student by ID |
| PUT | /api/v1/students/{id} | Update student |
| DELETE | /api/v1/students/{id} | Delete student |
| GET | /healthcheck | Health check |

**Student model:** `{ id: UUID, name: String, age: Int, email: String }`

**Architecture:** Controller → Service → Repository → Database (Micronaut Data JDBC)

---

## Kubernetes Cluster Setup

**4-node Minikube cluster** (Docker driver, Apple Silicon M4 with Colima 4CPU/8GB):

| Node | Role | Label |
|---|---|---|
| minikube | Control plane | (no workloads) |
| minikube-m02 | Application | type=application |
| minikube-m03 | Database | type=database |
| minikube-m04 | Dependent services | type=dependent_services |

**Namespaces:**
- `student-api` — student API + postgres
- `vault` — Hashicorp Vault
- `external-secrets` — ESO
- `argocd` — ArgoCD
- `observability` — Prometheus, Grafana, Loki, Promtail, exporters

---

## Directory Structure

```
StudentManagement/
├── src/
│   ├── main/kotlin/org/one2n/
│   │   ├── controller/         # StudentController, HealthController
│   │   ├── service/            # StudentService
│   │   ├── repository/         # StudentRepository (Micronaut Data JDBC)
│   │   ├── entity/             # Student
│   │   └── dto/                # StudentRequest, StudentResponse
│   ├── main/resources/
│   │   ├── application.yml     # Micronaut config with Flyway
│   │   └── db/migration/       # V1__create_students_table.sql
│   └── test/kotlin/org/one2n/
│       ├── controller/         # StudentControllerTest, HealthControllerTest
│       ├── service/            # StudentServiceTest
│       └── util/               # StudentTestData
├── infra/
│   ├── docker-compose.yml      # Vagrant deployment (hardcoded values)
│   ├── provision.sh            # Vagrant VM provisioning script
│   ├── nginx/
│   │   └── nginx.conf          # Load balancer config
│   ├── helm/
│   │   ├── student-api/        # Custom Helm chart
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml     # Empty defaults, values injected via --set from .env
│   │   │   └── templates/
│   │   │       ├── deployment.yaml
│   │   │       ├── service.yaml
│   │   │       ├── configmap.yaml
│   │   │       ├── secret-store.yaml      # ESO SecretStore
│   │   │       ├── external-secret.yaml   # ESO ExternalSecret
│   │   │       └── vault-token-secret.yaml
│   │   ├── postgres/           # Custom Helm chart
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml
│   │   │   └── templates/
│   │   │       ├── deployment.yaml
│   │   │       └── service.yaml
│   │   │       # Note: secret.yaml removed — ESO manages postgres-secret
│   │   ├── vault-values.yaml              # Direct values for hashicorp/vault
│   │   ├── external-secrets-values.yaml   # Direct values for ESO
│   │   ├── argocd-values.yaml             # Direct values for ArgoCD
│   │   └── observability/                 # Values files only (direct helm install)
│   │       ├── prometheus-values.yaml      # kube-prometheus-stack config + dashboards
│   │       ├── loki-values.yaml
│   │       ├── promtail-values.yaml        # static_configs targeting student-api logs
│   │       ├── postgres-exporter-values.yaml
│   │       ├── blackbox-exporter-values.yaml
│   │       └── alerts/
│   │           └── alert-rules.yaml        # PrometheusRule manifest
│   └── argocd/
│       ├── repository-secret.yaml          # GitHub repo access (uses ${GITHUB_TOKEN})
│       └── applications/
│           ├── student-api.yaml            # ArgoCD Application
│           └── postgres.yaml              # ArgoCD Application
├── .github/
│   └── workflows/
│       └── ci.yml              # Build → lint → test → docker push → update values.yaml tag
├── postman/
│   └── student-api.postman_collection.json
├── scripts/                    # Developer setup scripts
├── Dockerfile                  # Multi-stage: gradle:8.7-jdk21 → eclipse-temurin:21-jre-alpine
├── docker-compose.yml          # Local dev (uses .env file)
├── Vagrantfile                 # UTM provider, utm/bookworm box, ARM64
├── Makefile                    # All commands (local + K8s)
├── .env                        # Local secrets (gitignored)
├── .env.example                # Template with placeholder values
├── detekt.yml                  # Detekt config
└── build.gradle.kts

---

## Environment Variables (.env)

```
DB_HOST=postgres
DB_PORT=5432
DB_NAME=students
DB_USER=postgres
DB_PASSWORD=postgres
VAULT_TOKEN=root
GITHUB_USERNAME=ShrenikMehar
GITHUB_TOKEN=<github_pat>
```

---

## Makefile Targets

### Local Development
```
make build          # docker build
make run            # docker-compose up (uses .env)
make stop           # docker-compose down
make logs           # docker-compose logs -f
make db-up          # postgres only
make db-down        # stop postgres
make local-build    # gradle build
make local-run      # gradle run
make local-test     # gradle test
make lint           # ktlintCheck + detekt
make format         # ktlintFormat
```

### Kubernetes
```
source .env && make k8s-up          # Full cluster bootstrap
make k8s-down                       # minikube delete
make k8s-stop                       # minikube stop
make k8s-start                      # minikube start
make k8s-run                        # Get student API URL
make k8s-argocd-ui                  # Get ArgoCD URL
make k8s-argocd-password            # Get ArgoCD admin password
make k8s-grafana-ui                 # Get Grafana URL
```

### k8s-up does (in order):
1. minikube start --nodes 4 --driver=docker
2. Label nodes (application, database, dependent_services)
3. Create namespaces (student-api, vault, external-secrets, argocd, observability)
4. Add Helm repos (hashicorp, external-secrets, argo, prometheus-community, grafana)
5. helm upgrade --install vault with infra/helm/vault-values.yaml → kubectl wait → vault login → vault kv put credentials
6. helm upgrade --install external-secrets with infra/helm/external-secrets-values.yaml → kubectl wait
7. helm upgrade --install postgres (with --set from .env)
8. helm upgrade --install student-api (with --set from .env)
9. helm upgrade --install argocd with infra/helm/argocd-values.yaml → kubectl wait → envsubst apply repository-secret → kubectl apply applications
10. helm upgrade --install prometheus (kube-prometheus-stack)
11. helm upgrade --install loki
12. helm upgrade --install promtail
13. helm upgrade --install postgres-exporter
14. helm upgrade --install blackbox-exporter
15. kubectl apply alert-rules.yaml

---

## CI Pipeline (.github/workflows/ci.yml)

**Triggers:** push to `src/**` or manual workflow_dispatch
**Runner:** self-hosted (MacBook M4)

**Steps:**
1. Checkout (with write token for push back)
2. Setup Java 21
3. make local-build
4. make lint
5. make local-test
6. docker build with VERSION=${{ github.sha }}
7. docker login to DockerHub
8. docker tag + push shrenikmehar/student-api:<sha>
9. sed update image tag in infra/helm/student-api/values.yaml
10. git commit + push → "ci: update image tag to <sha>"

**ArgoCD then detects the values.yaml change and auto-deploys.**

---

## Key Design Decisions & Trade-offs

### What was done well
- Clean layered architecture (controller → service → repository)
- Proper DTO/entity separation
- MockK for unit tests, good coverage of happy path + not-found cases
- `findStudentOrThrow` private helper avoids repetition
- Extension functions `toResponse()` / `toEntity()` for clean mapping
- Multi-stage Docker build reduces image size
- `StudentTestData` factory pattern for test data

### Known gaps / things to improve
- **Flyway runs in app on startup** — exercise required init container for migrations, not implemented
- **No PersistentVolumeClaim** for Postgres — data lost on pod restart
- **Vault dev mode** — data wiped on every cluster teardown, credentials must be re-seeded
- **Bootstrap defaults are split across `.env`, Helm values, and scripts** — local Minikube defaults work, but config is still spread across multiple places
- **No integration tests** — only unit tests with mocked repos, no Testcontainers
- **No request validation tests** — validation annotations on DTOs not tested
- **No verify() calls in controller tests** — only response assertions, not service call verification
- **DB_PORT in ConfigMap** — should be quoted string, caused issues
- **k8s-up is not idempotent** — fails if cluster already running (minikube start fails, namespace already exists, etc.)
- **Promtail uses static_configs** not kubernetes_sd_configs — acceptable for this Minikube setup but not scalable
- **Loki chunks-cache pending** — known issue with Loki on Minikube, non-critical
- **Application error logs dashboard** — skipped (Loki LogQL not compatible with PrometheusRule)
- **Slack alerts** — skipped from Exercise 11
- **No Hadolint** — Dockerfile not linted (mentioned in Exercise 2 further reading)
- **CI doesn't trigger on infra changes** — only `src/**` triggers pipeline

---

## Exercises — Original Requirements vs What Was Built

### Exercise 1 — REST API ✅
**Required:** CRUD API, healthcheck, versioning, Flyway, env config, logs, unit tests, Postman, Makefile, README
**Built:** All requirements met. Used Kotlin + Micronaut (not in the suggested list but approved).

### Exercise 2 — Containerize ✅
**Required:** Multi-stage Dockerfile, semver tagging, small image, env injection
**Built:** Met. Used gradle:8.7-jdk21 builder + eclipse-temurin:21-jre-alpine runtime.
**Gap:** Hadolint not added.

### Exercise 3 — One Click Setup ✅
**Required:** docker-compose, make targets for DB start + migration + build + run
**Built:** Met. `make run` starts everything. DB migrations run via Flyway on app startup.
**Gap:** Exercise asked for explicit migration make target — migrations are implicit via app startup.

### Exercise 4 — CI Pipeline ✅
**Required:** Build, test, lint, docker login, build + push. Self-hosted runner. Manual trigger. Only on code changes.
**Built:** All met.

### Exercise 5 — Bare Metal ✅
**Required:** Vagrant box, provision script, docker-compose, 2 API + 1 DB + 1 Nginx, Nginx load balancing on port 8080
**Built:** All met. Used UTM + vagrant_utm for Apple Silicon.

### Exercise 6 — K8s Cluster ✅
**Required:** 3-node Minikube cluster (exercise said 3, we used 4 to separate control plane from workloads), node labels
**Built:** 4 nodes (1 control plane + 3 workers). Labels correct.

### Exercise 7 — K8s Deployment ⚠️
**Required:** K8s manifests, single file per component, init container for migrations, ConfigMaps, ESO + Vault, expose API
**Built:** Helm-based deployment with ESO + Vault working. Init container NOT implemented.
**Gap:** Init container for migrations missing.

### Exercise 8 — Helm Charts ✅
**Required:** Helm charts committed, community charts for DB/Vault recommended, entire stack via Helm
**Built:** Custom charts for student-api + postgres. Direct Helm installs with repo-pinned values files for Vault, ESO, ArgoCD, and observability.

### Exercise 9 — ArgoCD ✅
**Required:** ArgoCD in argocd namespace on dependent_services node, declarative manifests, Helm as source of truth, CI job to update image tag, self-hosted runner, auto-sync
**Built:** All met. CI updates values.yaml and ArgoCD auto-syncs.

### Exercise 10 — Observability Stack ✅
**Required:** Prometheus + Loki + Grafana in observability namespace, Promtail for app logs only, DB exporter, Blackbox exporter, kube-state + node metrics, two Grafana data sources
**Built:** All met. Promtail ships only student-api logs using static_configs.

### Exercise 11 — Dashboards & Alerts ⚠️
**Required:** 5 dashboards (DB, error logs, node, kube-state, blackbox), 5 alert scenarios, Slack notifications
**Built:** 4 community dashboards (DB, node, kube-state, blackbox). Error logs dashboard skipped. All 5 alert scenarios implemented as PrometheusRule. Slack skipped.
**Gap:** Application error logs dashboard and Slack integration missing.

---

## Areas for Refactoring (Priority Order)

### High Priority
1. **Make k8s-up idempotent** — add checks before each step (minikube already running? namespace exists? helm release exists?)
2. **Add PersistentVolumeClaim to postgres Helm chart** — data survives pod restarts
3. **Fix Vault dev mode seeding** — add a make target or script that checks if credentials exist before seeding
4. **Centralize bootstrap configuration further** — reduce duplication across `.env`, chart values, and scripts

### Medium Priority
6. **Add integration tests with Testcontainers** — test actual DB operations
7. **Add verify() calls to controller tests** — ensure service methods are actually called
8. **Add validation failure tests** — test blank name, invalid email, age=0
9. **Fix DB_PORT quoting** — should consistently be a string in ConfigMap
10. **Implement init container for Flyway migrations** — complete Exercise 7 requirement

### Low Priority
11. **Add Hadolint to CI** — Dockerfile linting
12. **Add CI trigger for infra/** — currently only src/** triggers pipeline
13. **Implement application error logs dashboard** — using Loki + Grafana
14. **Add Slack contact point** — complete Exercise 11 requirement
15. **Add pagination to GET /api/v1/students** — mentioned in future improvements
16. **Add OpenAPI/Swagger documentation** — mentioned in future improvements

---

## Suggestions for Bootcamp Improvement

### Exercise 3
- The requirement says "run DB DML migrations" as a make target, but doesn't clarify whether this means a separate step or embedded in app startup. Flyway-on-startup is the Micronaut idiomatic way — the exercise should clarify this distinction.
- The bash functions to install tools were mentioned but not built — either make this a clear requirement or remove it.

### Exercise 6
- Exercise says "3-node cluster" but doesn't account for the control plane node. A developer following literally will label the control plane as an application node. Should say "3-worker-node cluster" or "4-node cluster with control plane".

### Exercise 7
- Init container requirement conflicts with Flyway-on-startup from Exercise 1. The exercise should either note this conflict explicitly and require disabling Flyway in app config, or suggest a different migration approach from the start.
- The order of exercises means students write code with embedded Flyway (Exercise 1) then have to undo that for K8s (Exercise 7). Consider noting this dependency earlier.

### Exercise 8
- "Community-managed charts, but it is recommended that you also add these charts inside the helm directory" is ambiguous. Does this mean wrapper charts or just values files? Senior engineers at One2N clarified that values files are sufficient — this should be stated clearly in the exercise.
- No guidance on secrets management in Helm values — students naturally hardcode credentials in values.yaml. Should mention --set pattern or .env approach.

### Exercise 9
- The exercise doesn't mention that `GITHUB_TOKEN` built-in needs write permissions enabled in GitHub repo settings. This is a non-obvious step that causes cryptic 403 errors.
- No guidance on the chicken-and-egg problem: ArgoCD needs to deploy the app, but the app needs to already exist for ESO secrets to work. The dependency order needs documentation.

### Exercise 10
- Promtail is deprecated as of February 2026 — the exercise should mention Grafana Alloy as the modern replacement and let students choose.
- No mention of Colima resource requirements. Running a 4-node Minikube cluster + full observability stack on default Docker resources (2CPU/2GB) is impossible. Should add a prerequisites section for local resource requirements.
- Loki has significant filesystem permission issues on Apple Silicon Minikube. Should document the `runAsUser: 0` workaround or suggest an alternative storage backend.

### Exercise 11
- The exercise asks for Slack integration but this requires creating a Slack workspace/app which some students may not have. Should offer an alternative (email, webhook to a test endpoint, etc.).
- "Application error logs" dashboard requires LogQL → Prometheus metric conversion (recording rules) which is non-trivial. Should provide more guidance or simplify the requirement to "view error logs in Loki Explore".
- The distinction between PrometheusRule (server-side alerts) and Grafana alerts (UI alerts) is not explained. Students may try to configure one when the exercise expects the other.

### General
- Resource requirements for the entire bootcamp (Colima/Docker Desktop CPU/RAM) should be documented upfront at the start of Part One.
- A troubleshooting guide for common Apple Silicon issues (VirtualBox not supported, permission errors, TLS timeouts on resource-constrained clusters) would save significant debugging time.
- The bootcamp would benefit from a reference implementation repository that students can compare against after completing each exercise.
