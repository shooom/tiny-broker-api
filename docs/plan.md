### 📈 Broker API – Development Roadmap

#### 🎯 Goal
Build an event-driven microservice-based broker system, deployable to Azure Kubernetes Service (AKS), with infrastructure managed via Terraform and CI/CD pipelines using GitHub Actions.

#### ✅ Stage 1: Local Monolith Setup
- [ ]	Prepare Terraform setup:
- [ ]	Use null_resource to run docker-compose locally
- [ ]	Launch monitoring stack (Prometheus, Grafana) via docker-compose
- [ ]	GitHub Actions:
- [ ]	CI pipeline for build and tests (unit/integration)
- [ ]	Update README.md with local setup instructions

#### 🚀 Stage 2: Monolith Deployment to Azure
- [ ]	Build and push Docker image to Azure Container Registry (ACR)
- [ ]	Terraform:
- [ ]	Create ACR
- [ ]	Deploy monolith to Azure App Service or directly to AKS
- [ ]	GitHub Actions:
- [ ]	CD pipeline: push image and deploy to Azure

#### 🔨 Stage 3: Transition to Microservices
- [ ]	Move current codebase to apps/monolith
- [ ]	Set up Maven multi-module structure (pom.xml with <packaging>pom</packaging>)
- [ ]	Split business logic into individual services (order-service, user-service, etc.)
- [ ]	Create docker-compose.yml with RabbitMQ and PostgreSQL
- [ ]	Implement event-driven communication via RabbitMQ
- [ ]	Use the Outbox Pattern and prepare for Saga orchestration

#### 🧪 Stage 4: Local Microservices Testing
- [ ]	Create a docker-compose setup with all microservices
- [ ]	Implement integration tests using Testcontainers
- [ ]	Add observability stack: Prometheus, Grafana, Jaeger

#### ☁️ Stage 5: Microservices Deployment to Kubernetes (AKS)
- [ ]	Provision AKS cluster using Terraform
- [ ]	Create Helm charts for each microservice
- [ ]	Set up CI/CD pipelines:
- [ ]	Build and push Docker images
- [ ]	Deploy and upgrade via Helm
- [ ]	Deploy RabbitMQ in Kubernetes using Helm
