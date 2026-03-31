SHELL := /bin/bash

.PHONY: build run stop logs db-up db-down local-build local-run local-test lint format \
	k8s-up k8s-cluster k8s-namespaces k8s-repos k8s-core k8s-apps k8s-gitops k8s-observability \
	k8s-run k8s-argocd-password k8s-argocd-ui k8s-grafana-ui k8s-stop k8s-start k8s-down

-include .env
export

IMAGE_NAME=student-api
VERSION ?= 1.0.0
MINIKUBE_NODES ?= 4
MINIKUBE_DRIVER ?= docker
VAULT_NAMESPACE ?= vault
EXTERNAL_SECRETS_NAMESPACE ?= external-secrets
APP_NAMESPACE ?= student-api
ARGOCD_NAMESPACE ?= argocd
OBSERVABILITY_NAMESPACE ?= observability

# Build docker image
build:
	docker build -t $(IMAGE_NAME):$(VERSION) .

# Run full system (API + Postgres)
run:
	docker-compose up

# Stop containers
stop:
	docker-compose down

# View logs
logs:
	docker-compose logs -f

# Run database only
db-up:
	docker-compose up -d postgres

db-down:
	docker-compose down


# ---- Local development helpers ----

local-build:
	./gradlew build

local-run:
	./gradlew run

local-test:
	./gradlew test

lint:
	./gradlew ktlintCheck detekt

format:
	./gradlew ktlintFormat


# ---- Kubernetes helpers ----

k8s-up: k8s-cluster k8s-core k8s-apps k8s-gitops k8s-observability
	@echo "Kubernetes bootstrap complete. Run 'make k8s-run' to get the API URL."

k8s-cluster:
	minikube start --nodes $(MINIKUBE_NODES) --driver=$(MINIKUBE_DRIVER)
	kubectl label node minikube-m02 type=application --overwrite
	kubectl label node minikube-m03 type=database --overwrite
	kubectl label node minikube-m04 type=dependent_services --overwrite

k8s-namespaces:
	kubectl create namespace $(APP_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl create namespace $(VAULT_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl create namespace $(EXTERNAL_SECRETS_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl create namespace $(ARGOCD_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl create namespace $(OBSERVABILITY_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -

k8s-repos:
	helm repo add hashicorp https://helm.releases.hashicorp.com
	helm repo add external-secrets https://charts.external-secrets.io
	helm repo add argo https://argoproj.github.io/argo-helm
	helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
	helm repo add grafana https://grafana.github.io/helm-charts
	helm repo update

k8s-core: k8s-namespaces k8s-repos
	helm upgrade --install vault hashicorp/vault \
		--namespace $(VAULT_NAMESPACE) \
		-f infra/helm/vault-values.yaml
	kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=vault -n $(VAULT_NAMESPACE) --timeout=300s
	kubectl exec vault-0 -n $(VAULT_NAMESPACE) -- vault login $(VAULT_TOKEN)
	kubectl exec vault-0 -n $(VAULT_NAMESPACE) -- vault kv put secret/student-api/db username=$(DB_USER) password=$(DB_PASSWORD)
	helm upgrade --install external-secrets external-secrets/external-secrets \
		--namespace $(EXTERNAL_SECRETS_NAMESPACE) \
		-f infra/helm/external-secrets-values.yaml
	kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=external-secrets -n $(EXTERNAL_SECRETS_NAMESPACE) --timeout=300s
	kubectl wait --for=condition=established crd/externalsecrets.external-secrets.io --timeout=300s
	kubectl wait --for=condition=established crd/secretstores.external-secrets.io --timeout=300s

k8s-apps: k8s-core
	helm upgrade --install postgres infra/helm/postgres \
		--namespace $(APP_NAMESPACE) \
		--set auth.username=$(DB_USER) \
		--set auth.password=$(DB_PASSWORD) \
		--set auth.database=$(DB_NAME)
	helm upgrade --install student-api infra/helm/student-api \
		--namespace $(APP_NAMESPACE) \
		--set config.dbHost=$(DB_HOST) \
		--set config.dbPort=$(DB_PORT) \
		--set config.dbName=$(DB_NAME) \
		--set secret.dbUser=$(DB_USER) \
		--set secret.dbPassword=$(DB_PASSWORD) \
		--set vaultToken=$(VAULT_TOKEN)

k8s-gitops: k8s-namespaces k8s-repos
	helm upgrade --install argocd argo/argo-cd \
		--namespace $(ARGOCD_NAMESPACE) \
		-f infra/helm/argocd-values.yaml
	kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n $(ARGOCD_NAMESPACE) --timeout=300s
	envsubst < infra/argocd/repository-secret.yaml | kubectl apply -f -
	kubectl apply -f infra/argocd/applications/postgres.yaml
	kubectl apply -f infra/argocd/applications/student-api.yaml

k8s-observability: k8s-namespaces k8s-repos
	helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
		--namespace $(OBSERVABILITY_NAMESPACE) \
		-f infra/helm/observability/prometheus-values.yaml
	helm upgrade --install loki grafana/loki \
		--namespace $(OBSERVABILITY_NAMESPACE) \
		-f infra/helm/observability/loki-values.yaml
	helm upgrade --install promtail grafana/promtail \
		--namespace $(OBSERVABILITY_NAMESPACE) \
		-f infra/helm/observability/promtail-values.yaml
	helm upgrade --install postgres-exporter prometheus-community/prometheus-postgres-exporter \
		--namespace $(OBSERVABILITY_NAMESPACE) \
		-f infra/helm/observability/postgres-exporter-values.yaml
	helm upgrade --install blackbox-exporter prometheus-community/prometheus-blackbox-exporter \
		--namespace $(OBSERVABILITY_NAMESPACE) \
		-f infra/helm/observability/blackbox-exporter-values.yaml
	kubectl apply -f infra/helm/observability/alerts/alert-rules.yaml

k8s-run:
	minikube service student-api -n $(APP_NAMESPACE) --url

k8s-argocd-password:
	@kubectl get secret argocd-initial-admin-secret -n $(ARGOCD_NAMESPACE) -o jsonpath="{.data.password}" | base64 -d && echo

k8s-argocd-ui:
	minikube service argocd-server -n $(ARGOCD_NAMESPACE) --url

k8s-grafana-ui:
	minikube service prometheus-grafana -n $(OBSERVABILITY_NAMESPACE) --url

k8s-stop:
	minikube stop

k8s-start:
	minikube start

k8s-down:
	minikube delete
