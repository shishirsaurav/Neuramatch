# NeuraMatch - Setup Guide

## Sprint 1 Completed ✅

### What's Been Set Up

#### 1. **Project Structure**
```
NeuraMatch/
├── backend/
│   ├── pom.xml (parent)
│   ├── neuramatch-common/
│   ├── neuramatch-resume-service/
│   ├── neuramatch-job-service/
│   ├── neuramatch-matching-service/
│   └── neuramatch-api-gateway/
├── frontend/ (placeholder)
├── sql/
├── monitoring/
└── docker-compose.yml
```

#### 2. **Microservices**
- **API Gateway** (port 8080) - Routes and rate limiting
- **Resume Service** (port 8081) - Resume upload and parsing
- **Job Service** (port 8082) - Job posting management
- **Matching Service** (port 8083) - AI-powered matching

#### 3. **Infrastructure (Docker Compose)**
- PostgreSQL 15+ with pgvector extension
- Neo4j 5.x for knowledge graph
- Redis 7.x for caching
- Apache Kafka 3.0+ with Zookeeper
- Prometheus for metrics
- Grafana for dashboards

#### 4. **Configuration**
- Application properties for all services
- Logback logging framework
- Health check endpoints
- Environment variables (.env.example)

#### 5. **Git Repository**
- Initialized with comprehensive .gitignore
- All files staged and ready to commit

---

## Quick Start

### Prerequisites
- Java 17+ ✅
- Maven 3.8+ ✅
- Docker & Docker Compose

### 1. Start Infrastructure Services
```bash
# Start all databases and services
docker-compose up -d

# Check if all services are running
docker-compose ps

# View logs
docker-compose logs -f
```

### 2. Copy Environment Variables
```bash
cp .env.example .env
# Edit .env with your API keys
```

### 3. Build the Project
```bash
cd backend
mvn clean install
```

### 4. Run Services

**Option A: Run all services individually**
```bash
# Terminal 1: API Gateway
cd backend/neuramatch-api-gateway
mvn spring-boot:run

# Terminal 2: Resume Service
cd backend/neuramatch-resume-service
mvn spring-boot:run

# Terminal 3: Job Service
cd backend/neuramatch-job-service
mvn spring-boot:run

# Terminal 4: Matching Service
cd backend/neuramatch-matching-service
mvn spring-boot:run
```

**Option B: Build JARs and run**
```bash
cd backend
mvn clean package
java -jar neuramatch-api-gateway/target/neuramatch-api-gateway-1.0.0-SNAPSHOT.jar
java -jar neuramatch-resume-service/target/neuramatch-resume-service-1.0.0-SNAPSHOT.jar
# ... etc
```

### 5. Verify Services

**Check Health Endpoints:**
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Resume Service
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/v1/health

# Job Service
curl http://localhost:8082/actuator/health

# Matching Service
curl http://localhost:8083/actuator/health
```

**Access UIs:**
- Neo4j Browser: http://localhost:7474 (neo4j/password)
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

---

## Database Setup

### PostgreSQL
```bash
# Connect to database
docker exec -it neuramatch-postgres psql -U postgres -d neuramatch

# Verify pgvector extension
\dx

# Exit
\q
```

### Neo4j
```bash
# Access Neo4j Browser
open http://localhost:7474

# Login with: neo4j/password
# Run test query:
RETURN "Neo4j is ready!" AS status
```

### Redis
```bash
# Test Redis
docker exec -it neuramatch-redis redis-cli ping
# Should return: PONG
```

### Kafka
```bash
# List topics
docker exec -it neuramatch-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Create test topic
docker exec -it neuramatch-kafka kafka-topics --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

---

## Development Workflow

### Making Code Changes
1. Make changes in your IDE
2. Maven will auto-compile (if using `mvn spring-boot:run`)
3. Or rebuild: `mvn clean package`

### Adding Dependencies
Edit the appropriate `pom.xml` file and run:
```bash
mvn clean install
```

### Viewing Logs
```bash
# Application logs
tail -f backend/neuramatch-resume-service/logs/neuramatch-resume-service.log

# Docker service logs
docker-compose logs -f postgres
docker-compose logs -f kafka
```

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port
lsof -i :8080
# Kill process
kill -9 <PID>
```

### Docker Issues
```bash
# Stop all containers
docker-compose down

# Remove volumes (careful: deletes data!)
docker-compose down -v

# Rebuild containers
docker-compose up -d --build
```

### Maven Build Issues
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Skip tests
mvn clean install -DskipTests
```

### Database Connection Issues
1. Check if PostgreSQL is running: `docker ps | grep postgres`
2. Verify credentials in application.yml
3. Check network connectivity: `docker network ls`

---

## Next Steps (Sprint 2)

- [ ] Create domain entities (Resume, Job, Skill, Experience)
- [ ] Implement JPA repositories
- [ ] Add database migrations (Flyway/Liquibase)
- [ ] Write unit tests

---

## API Endpoints (Current)

| Service | Endpoint | Description |
|---------|----------|-------------|
| Gateway | `GET /actuator/health` | Health check |
| Resume  | `GET /api/v1/health` | Custom health check |
| Job     | `GET /actuator/health` | Health check |
| Matching| `GET /actuator/health` | Health check |

---

## Useful Commands

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# List running Docker containers
docker ps

# View Docker logs
docker logs <container-name>

# Restart a service
docker-compose restart postgres

# Stop all services
docker-compose down

# Start services in background
docker-compose up -d

# View resource usage
docker stats
```

---

## Environment Variables

Required for production (optional for local development):

```bash
# Database
DB_HOST=localhost
DB_USER=postgres
DB_PASSWORD=postgres

# Neo4j
NEO4J_HOST=localhost
NEO4J_PASSWORD=password

# Redis
REDIS_HOST=localhost

# Kafka
KAFKA_BROKERS=localhost:9092

# AI APIs (for future sprints)
OPENAI_API_KEY=sk-...
GEMINI_API_KEY=...
PINECONE_API_KEY=...
```

---

## Git Commands

```bash
# Initial commit
git commit -m "Sprint 1: Project foundation and infrastructure setup"

# Create development branch
git checkout -b develop

# Push to remote
git remote add origin <your-repo-url>
git push -u origin master
```

---

**Sprint 1 Status: ✅ COMPLETED**

All infrastructure is ready. You can now proceed to Sprint 2 for domain modeling!
