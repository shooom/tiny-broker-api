# 📈 Broker API

A personal project to build an event-driven, microservice-based trading backend.  
Initially developed as a monolithic Java app, this project will evolve into a cloud-native architecture running on Kubernetes (AKS), with full CI/CD and infrastructure as code using Terraform.

---
## 🚀 Features
- Order placement and processing
- Event-driven architecture with RabbitMQ
- Modular design for future microservice extraction
- Docker-based local setup
- Infrastructure automation with Terraform
- CI/CD with GitHub Actions
- Planned deployment to Azure Kubernetes Service (AKS)
  
## 🛠️ Roadmap
  -	Terraform-based local setup
  -	Azure deployment via ACR & App Service
  -	Modularization into services (Inventory, Order, MatchingEngine...)
  -	RabbitMQ event integration
  -	Helm + AKS deployment

📝 Full roadmap: docs/plan.md