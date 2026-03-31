SHELL := /bin/bash

.PHONY: build run stop logs db-up db-down local-build local-run local-test lint format \
	k8s-up k8s-cluster k8s-core k8s-apps k8s-gitops k8s-observability \
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
K8S_SCRIPT := ./scripts/k8s.sh

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

k8s-up:
	$(K8S_SCRIPT) up

k8s-cluster:
	$(K8S_SCRIPT) cluster

k8s-core:
	$(K8S_SCRIPT) core

k8s-apps:
	$(K8S_SCRIPT) apps

k8s-gitops:
	$(K8S_SCRIPT) gitops

k8s-observability:
	$(K8S_SCRIPT) observability

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
