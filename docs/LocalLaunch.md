## Broker API – Docker & Monitoring Setup

This document describes how to run your Spring Boot application standalone in Docker, and with full monitoring stack using `docker-compose`.

---

### 🐳 Run Application Only (Standalone)

#### 1. Build the JAR

Make sure your application is built:

```bash
  ./mvnw clean package -DskipTests
```
Resulting JAR file will be in target/your-app.jar.

#### 2. Build the Docker image:
```bash
  docker build -t broker-api .
```
#### 3. Run the Container
```bash
  docker run -p 8080:8080 broker-api
```

### 📈 Run With Monitoring (Grafana, Prometheus, Loki, Promtail)

#### 1. Structure
```
.
├── docker-compose.yml
├── Dockerfile
├── target/your-app.jar
└── monitoring/
    ├── prometheus.yml
    ├── loki-config.yaml
    └── promtail-config.yaml
```
#### 2. Build Your App Image
```bash
  docker build -t broker-api .
```
#### 3. Start Full Stack
```bash
   docker-compose up -d
```
This will run:
- Your app
- Prometheus (http://localhost:9090)
- Grafana (http://localhost:3000)
- Loki + Promtail

Grafana login:
- Username: admin
- Password: admin
- 
#### 4. Configure Grafana
1. Open Grafana at http://localhost:3000
2. Add Prometheus data source: http://prometheus:9090
3. Add Loki data source: http://loki:3100
4. Import or create dashboards

🛑 Stop Everything
```bash
    docker-compose down
```