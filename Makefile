.PHONY: build run stop logs db-up db-down local-build local-run local-test lint format \
        k8s-up k8s-run k8s-argocd-password k8s-argocd-ui k8s-grafana-ui k8s-stop k8s-start k8s-down

-include .env
export

IMAGE_NAME=student-api
VERSION ?= 1.0.0

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
	# After this completes and all pods are running, run: make k8s-run to get the API url
	minikube start --nodes 4 --driver=docker
	kubectl label node minikube-m02 type=application
	kubectl label node minikube-m03 type=database
	kubectl label node minikube-m04 type=dependent_services
	kubectl create namespace student-api
	kubectl create namespace vault
	kubectl create namespace external-secrets
	helm repo add hashicorp https://helm.releases.hashicorp.com
	helm repo add external-secrets https://charts.external-secrets.io
	helm repo update
	helm dependency build infra/helm/vault
	helm dependency build infra/helm/external-secrets
	helm install vault infra/helm/vault --namespace vault
	kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=vault -n vault --timeout=300s
	kubectl exec -it vault-0 -n vault -- vault login $(VAULT_TOKEN)
	kubectl exec -it vault-0 -n vault -- vault kv put secret/student-api/db username=$(DB_USER) password=$(DB_PASSWORD)
	helm install external-secrets infra/helm/external-secrets --namespace external-secrets
	kubectl wait --for=condition=ready pod -l 'app.kubernetes.io/instance=external-secrets' -n external-secrets --timeout=300s
	helm install postgres infra/helm/postgres \
    		--namespace student-api \
    		--set auth.username=$(DB_USER) \
    		--set auth.password=$(DB_PASSWORD) \
    		--set auth.database=$(DB_NAME)
	helm install student-api infra/helm/student-api \
		--namespace student-api \
		--set config.dbHost=$(DB_HOST) \
		--set config.dbPort=$(DB_PORT) \
		--set config.dbName=$(DB_NAME) \
		--set secret.dbUser=$(DB_USER) \
		--set secret.dbPassword=$(DB_PASSWORD) \
		--set vaultToken=$(VAULT_TOKEN)
	kubectl create namespace argocd
	helm install argocd infra/helm/argocd --namespace argocd
	kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
	envsubst < infra/argocd/repository-secret.yaml | kubectl apply -f -
	kubectl apply -f infra/argocd/applications/postgres.yaml
	kubectl apply -f infra/argocd/applications/student-api.yaml
	kubectl create namespace observability
	helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
	helm repo add grafana https://grafana.github.io/helm-charts
	helm repo update
	helm install prometheus prometheus-community/kube-prometheus-stack \
		--namespace observability \
		-f infra/helm/observability/prometheus-values.yaml
	helm install loki grafana/loki \
		--namespace observability \
		-f infra/helm/observability/loki-values.yaml
	helm install promtail grafana/promtail \
		--namespace observability \
		-f infra/helm/observability/promtail-values.yaml
	helm install postgres-exporter prometheus-community/prometheus-postgres-exporter \
		--namespace observability \
		-f infra/helm/observability/postgres-exporter-values.yaml
	helm install blackbox-exporter prometheus-community/prometheus-blackbox-exporter \
		--namespace observability \
		-f infra/helm/observability/blackbox-exporter-values.yaml

k8s-run:
	minikube service student-api -n student-api --url

k8s-argocd-password:
	@kubectl get secret argocd-initial-admin-secret -n argocd -o jsonpath="{.data.password}" | base64 -d && echo

k8s-argocd-ui:
	minikube service argocd-server -n argocd --url

k8s-grafana-ui:
	minikube service prometheus-grafana -n observability --url

k8s-stop:
	minikube stop

k8s-start:
	minikube start

k8s-down:
	minikube delete
