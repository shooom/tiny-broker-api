### 📈 Broker API – Development Roadmap

#### 🎯 Goal
Build an event-driven microservice-based broker system, deployable to Azure Kubernetes Service (AKS), with infrastructure managed via Terraform and CI/CD pipelines using GitHub Actions.

#### ✅ Stage 1: Local Monolith Setup
- [ ]	Upgrade Spring Boot to the latest stable version
- [ ]	GitHub Actions:
- [ ]	CI pipeline for build and tests (unit/integration)
- [ ]	Update README.md with local setup instructions

#### 🚀 Stage 2: Monolith Deployment to AWS
- [ ]	Build and push Docker image to Amazon Elastic Container Registry (ECR)
- [ ]	Terraform:
  - [ ]	Create ACR
  - [ ]	Deploy monolith to Azure App Service or directly to AKS
- [ ]	GitHub Actions:
  - [ ]	CD pipeline: push image and deploy to Azure

#### 🔨 Stage 3: Transition to Microservices
- [ ]	Set up Maven multi-module structure (pom.xml with <packaging>pom</packaging>)
- [ ]	Split business logic into individual services (order-service, user-service, etc.)
- [ ]	Create docker-compose.yml with RabbitMQ and PostgreSQL
- [ ]	Implement event-driven communication via RabbitMQ
- [ ]	Use the Outbox Pattern and prepare for Saga orchestration

#### 🧪 Stage 4: Local Microservices Testing
- [ ]	Create a docker-compose setup with all microservices
- [ ]	Implement integration tests using Testcontainers
- [ ]	Add/activate observability stack: Prometheus, Grafana, Jaeger

#### 🧪 Stage 5: Kubernetes (Local, via OrbStack)
- [ ]	Install and configure local Kubernetes cluster via OrbStack
- [ ]	Create Helm charts for each microservice
- [ ]	Deploy services locally with Helm
- [ ]	Validate:
- [ ]	Service discovery and communication
- [ ]	RabbitMQ in-cluster setup
- [ ]	Monitoring stack (Prometheus, Grafana, Jaeger)
- [ ]	Update local deployment docs and Helm values

#### ☁️ Stage 6: Microservices Deployment to Kubernetes (EKS)
- [ ]	Provision Amazon EKS cluster via Terraform
- [ ]	Create Helm charts for each microservice
- [ ]	Set up CI/CD pipelines:
  - [ ]	Build and push Docker images
  - [ ]	Deploy and upgrade via Helm
- [ ]	Deploy RabbitMQ in Kubernetes using Helm
