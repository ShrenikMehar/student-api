#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

MINIKUBE_NODES="${MINIKUBE_NODES:-4}"
MINIKUBE_DRIVER="${MINIKUBE_DRIVER:-docker}"
VAULT_NAMESPACE="${VAULT_NAMESPACE:-vault}"
EXTERNAL_SECRETS_NAMESPACE="${EXTERNAL_SECRETS_NAMESPACE:-external-secrets}"
APP_NAMESPACE="${APP_NAMESPACE:-student-api}"
ARGOCD_NAMESPACE="${ARGOCD_NAMESPACE:-argocd}"
OBSERVABILITY_NAMESPACE="${OBSERVABILITY_NAMESPACE:-observability}"
DB_HOST="${DB_HOST:-postgres}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-students}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"
VAULT_TOKEN="${VAULT_TOKEN:-root}"

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required environment variable: $name" >&2
    exit 1
  fi
}

log() {
  echo "==> $1"
}

ensure_minikube_running() {
  local host_status
  host_status="$(minikube status --format='{{.Host}}' 2>/dev/null || true)"

  if [[ "$host_status" == "Running" ]]; then
    log "Minikube cluster already running, skipping start"
    return
  fi

  log "Starting Minikube cluster"
  minikube start --nodes "$MINIKUBE_NODES" --driver="$MINIKUBE_DRIVER"
}

label_node() {
  local node="$1"
  local role="$2"
  local current

  current="$(kubectl get node "$node" -o jsonpath='{.metadata.labels.type}' 2>/dev/null || true)"
  if [[ "$current" == "$role" ]]; then
    log "Node $node already labeled type=$role"
    return
  fi

  log "Labeling node $node as type=$role"
  kubectl label node "$node" "type=$role" --overwrite
}

ensure_node_labels() {
  label_node minikube-m02 application
  label_node minikube-m03 database
  label_node minikube-m04 dependent_services
}

ensure_namespace() {
  local namespace="$1"

  if kubectl get namespace "$namespace" >/dev/null 2>&1; then
    log "Namespace $namespace already exists"
    return
  fi

  log "Creating namespace $namespace"
  kubectl create namespace "$namespace"
}

ensure_namespaces() {
  ensure_namespace "$APP_NAMESPACE"
  ensure_namespace "$VAULT_NAMESPACE"
  ensure_namespace "$EXTERNAL_SECRETS_NAMESPACE"
  ensure_namespace "$ARGOCD_NAMESPACE"
  ensure_namespace "$OBSERVABILITY_NAMESPACE"
}

ensure_helm_repo() {
  local name="$1"
  local url="$2"

  if helm repo list | awk '{print $1}' | grep -qx "$name"; then
    log "Helm repo $name already configured"
    return
  fi

  log "Adding Helm repo $name"
  helm repo add "$name" "$url"
}

ensure_helm_repos() {
  ensure_helm_repo hashicorp https://helm.releases.hashicorp.com
  ensure_helm_repo external-secrets https://charts.external-secrets.io
  ensure_helm_repo argo https://argoproj.github.io/argo-helm
  ensure_helm_repo prometheus-community https://prometheus-community.github.io/helm-charts
  ensure_helm_repo grafana https://grafana.github.io/helm-charts

  log "Updating Helm repos"
  helm repo update
}

wait_for_eso_crds() {
  log "Waiting for External Secrets CRDs"
  kubectl wait --for=condition=established crd/externalsecrets.external-secrets.io --timeout=300s
  kubectl wait --for=condition=established crd/secretstores.external-secrets.io --timeout=300s
}

seed_vault_secret() {
  log "Checking Vault seed data"
  if kubectl exec vault-0 -n "$VAULT_NAMESPACE" -- vault kv get secret/student-api/db >/dev/null 2>&1; then
    log "Vault secret student-api/db already exists, skipping seed"
    return
  fi

  log "Seeding Vault secret student-api/db"
  kubectl exec vault-0 -n "$VAULT_NAMESPACE" -- vault login "$VAULT_TOKEN" >/dev/null
  kubectl exec vault-0 -n "$VAULT_NAMESPACE" -- vault kv put secret/student-api/db username="$DB_USER" password="$DB_PASSWORD"
}

deploy_core() {
  ensure_namespaces
  ensure_helm_repos

  log "Deploying Vault"
  helm upgrade --install vault hashicorp/vault \
    --namespace "$VAULT_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/vault-values.yaml"
  kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=vault -n "$VAULT_NAMESPACE" --timeout=300s
  seed_vault_secret

  log "Deploying External Secrets"
  helm upgrade --install external-secrets external-secrets/external-secrets \
    --namespace "$EXTERNAL_SECRETS_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/external-secrets-values.yaml"
  kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=external-secrets -n "$EXTERNAL_SECRETS_NAMESPACE" --timeout=300s
  wait_for_eso_crds
}

deploy_apps() {
  deploy_core

  log "Deploying Postgres"
  helm upgrade --install postgres "$ROOT_DIR/infra/helm/postgres" \
    --namespace "$APP_NAMESPACE"

  log "Deploying student-api"
  helm upgrade --install student-api "$ROOT_DIR/infra/helm/student-api" \
    --namespace "$APP_NAMESPACE"
}

deploy_gitops() {
  require_env GITHUB_USERNAME
  require_env GITHUB_TOKEN

  ensure_namespaces
  ensure_helm_repos

  log "Deploying ArgoCD"
  helm upgrade --install argocd argo/argo-cd \
    --namespace "$ARGOCD_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/argocd-values.yaml"
  kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n "$ARGOCD_NAMESPACE" --timeout=300s

  log "Applying ArgoCD repository and applications"
  envsubst < "$ROOT_DIR/infra/argocd/repository-secret.yaml" | kubectl apply -f -
  kubectl apply -f "$ROOT_DIR/infra/argocd/applications/postgres.yaml"
  kubectl apply -f "$ROOT_DIR/infra/argocd/applications/student-api.yaml"
}

deploy_observability() {
  ensure_namespaces
  ensure_helm_repos

  log "Deploying observability stack"
  helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
    --namespace "$OBSERVABILITY_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/observability/prometheus-values.yaml"
  helm upgrade --install loki grafana/loki \
    --namespace "$OBSERVABILITY_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/observability/loki-values.yaml"
  helm upgrade --install promtail grafana/promtail \
    --namespace "$OBSERVABILITY_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/observability/promtail-values.yaml"
  helm upgrade --install postgres-exporter prometheus-community/prometheus-postgres-exporter \
    --namespace "$OBSERVABILITY_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/observability/postgres-exporter-values.yaml"
  helm upgrade --install blackbox-exporter prometheus-community/prometheus-blackbox-exporter \
    --namespace "$OBSERVABILITY_NAMESPACE" \
    -f "$ROOT_DIR/infra/helm/observability/blackbox-exporter-values.yaml"

  log "Applying observability alert rules"
  kubectl apply -f "$ROOT_DIR/infra/helm/observability/alerts/alert-rules.yaml"
}

deploy_cluster() {
  ensure_minikube_running
  ensure_node_labels
}

deploy_all() {
  deploy_cluster
  deploy_apps
  deploy_gitops
  deploy_observability
  log "Kubernetes bootstrap complete. Run 'make k8s-run' to get the API URL."
}

main() {
  local command="${1:-up}"

  case "$command" in
    up)
      deploy_all
      ;;
    cluster)
      deploy_cluster
      ;;
    core)
      deploy_core
      ;;
    apps)
      deploy_apps
      ;;
    gitops)
      deploy_gitops
      ;;
    observability)
      deploy_observability
      ;;
    *)
      echo "Unknown command: $command" >&2
      echo "Usage: $0 {up|cluster|core|apps|gitops|observability}" >&2
      exit 1
      ;;
  esac
}

main "$@"
