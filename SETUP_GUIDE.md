# NeuraMatch Setup Guide - Complete Connection Instructions

## What You Need to Connect

NeuraMatch requires **2 types of connections**:

### 1. Local Infrastructure (Automated via Docker) ✅
- PostgreSQL with pgvector
- Neo4j Graph Database
- Redis Cache
- Kafka Message Broker
- Prometheus & Grafana (Monitoring)
- NLP Service (Python/Flask)

### 2. External API Keys (Manual Setup Required) 🔑
- **Google Gemini API** (Primary - REQUIRED)
- OpenAI API (Optional - not used by default)
- Pinecone (Optional - PostgreSQL pgvector is used instead)

---

## Step-by-Step Setup Instructions

### STEP 1: Start Local Infrastructure (Docker)

All local services are pre-configured in `docker-compose.yml`. Simply run:

```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch

# Start all infrastructure services
docker-compose up -d

# Verify all services are running
docker-compose ps
```

**Services Started:**
- ✅ PostgreSQL (port 5432) - Main database with pgvector extension
- ✅ Neo4j (ports 7474, 7687) - Skill graph database
- ✅ Redis (port 6379) - Caching layer
- ✅ Kafka (port 9092) - Event streaming
- ✅ Zookeeper (port 2181) - Kafka coordinator
- ✅ Prometheus (port 9090) - Metrics collection
- ✅ Grafana (port 3000) - Monitoring dashboards
- ✅ NLP Service (port 5000) - Skill extraction service

**Health Check:**
```bash
# Check if all services are healthy
docker-compose ps

# Should show all services as "healthy" or "running"
```

---

### STEP 2: Get Google Gemini API Key (REQUIRED) 🔑

NeuraMatch uses **Google Gemini** for:
- Generating 768-dimensional embeddings (text-embedding-004)
- Resume/Job description analysis
- Semantic matching

**How to Get Gemini API Key:**

1. Go to **Google AI Studio**: https://makersuite.google.com/app/apikey
2. Sign in with your Google account
3. Click **"Get API Key"** or **"Create API Key"**
4. Copy the API key (starts with `AIza...`)

**Cost:** FREE tier includes:
- 1,500 requests per day
- 1 million tokens per month
- Perfect for development and testing

---

### STEP 3: Configure Environment Variables

Create a `.env` file from the example:

```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch
cp .env.example .env
```

Edit `.env` and add your Gemini API key:

```bash
# Required - Replace with your actual Gemini API key
GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

# Database Configuration (default values work with docker-compose)
DB_HOST=localhost
DB_USER=postgres
DB_PASSWORD=postgres

# Neo4j Configuration (default values work with docker-compose)
NEO4J_HOST=localhost
NEO4J_USER=neo4j
NEO4J_PASSWORD=password

# Redis Configuration (default works with docker-compose)
REDIS_HOST=localhost

# Kafka Configuration (default works with docker-compose)
KAFKA_BROKERS=localhost:9092

# Gemini Configuration
GEMINI_API_KEY=YOUR_ACTUAL_API_KEY_HERE  # ← CHANGE THIS!
GEMINI_MODEL=gemini-pro
GEMINI_EMBEDDING_MODEL=models/text-embedding-004

# Application Configuration
APP_ENV=development
LOG_LEVEL=DEBUG
```

---

### STEP 4: Initialize Databases

#### 4a. Initialize PostgreSQL

The database schema is automatically created on first startup via `init.sql`. To verify:

```bash
# Connect to PostgreSQL
docker exec -it neuramatch-postgres psql -U postgres -d neuramatch

# Check if tables exist
\dt

# Should show: resumes, jobs, skills, experiences, education, job_skills, etc.
\q
```

If tables aren't created, run manually:
```bash
docker exec -i neuramatch-postgres psql -U postgres -d neuramatch < sql/init.sql
```

#### 4b. Initialize Neo4j Skill Graph

```bash
# Connect to Neo4j browser
open http://localhost:7474

# Login with:
# Username: neo4j
# Password: password

# Run the skill graph initialization scripts
cd neo4j-scripts
# Copy and paste the Cypher queries from the scripts into Neo4j browser
```

Or use the automated script (if available):
```bash
docker exec -i neuramatch-neo4j cypher-shell -u neo4j -p password < neo4j-scripts/init-skills.cypher
```

---

### STEP 5: Build Backend Services

Set Java 17 and build all services:

```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend

# Build with Java 17
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean install -DskipTests

# Or if Java 17 is default:
mvn clean install -DskipTests
```

**Build should complete with:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~30 seconds
```

---

### STEP 6: Start Backend Microservices

Open **4 separate terminals** and start each service:

**Terminal 1 - Resume Service (Port 8081):**
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-resume-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Terminal 2 - Job Service (Port 8082):**
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-job-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Terminal 3 - Matching Service (Port 8083):**
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-matching-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Terminal 4 - API Gateway (Port 8080):**
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-api-gateway
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

**Wait for all services to start** - Look for:
```
Started ResumeServiceApplication in X seconds
Started JobServiceApplication in X seconds
Started MatchingServiceApplication in X seconds
Started ApiGatewayApplication in X seconds
```

---

### STEP 7: Verify Connections

#### Health Check Endpoints:

```bash
# Resume Service
curl http://localhost:8081/actuator/health

# Job Service
curl http://localhost:8082/actuator/health

# Matching Service
curl http://localhost:8083/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# NLP Service
curl http://localhost:5000/health
```

All should return: `{"status":"UP"}`

#### Check Database Connections:

```bash
# PostgreSQL
docker exec neuramatch-postgres psql -U postgres -d neuramatch -c "SELECT version();"

# Neo4j
docker exec neuramatch-neo4j cypher-shell -u neo4j -p password "RETURN 'Neo4j Connected' as status"

# Redis
docker exec neuramatch-redis redis-cli ping
# Should return: PONG

# Kafka
docker exec neuramatch-kafka kafka-topics --bootstrap-server localhost:9092 --list
# Should list topics (or empty if no topics yet)
```

---

### STEP 8: Access Services

#### Web Interfaces:

| Service | URL | Credentials |
|---------|-----|-------------|
| Neo4j Browser | http://localhost:7474 | neo4j / password |
| Grafana Dashboard | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | None |
| Swagger UI (Resume) | http://localhost:8081/swagger-ui.html | None |
| Swagger UI (Job) | http://localhost:8082/swagger-ui.html | None |
| Swagger UI (Matching) | http://localhost:8083/swagger-ui.html | None |

#### API Endpoints:

```bash
# Example: Upload a resume
curl -X POST http://localhost:8081/api/v1/resumes \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "phone": "555-0123",
    "summary": "Experienced Java developer with 5 years in Spring Boot",
    "skills": [
      {"skillName": "Java", "proficiency": "EXPERT", "yearsOfExperience": 5},
      {"skillName": "Spring Boot", "proficiency": "ADVANCED", "yearsOfExperience": 4}
    ]
  }'

# Example: Create a job posting
curl -X POST http://localhost:8082/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "jobTitle": "Senior Java Developer",
    "companyName": "Tech Corp",
    "jobDescription": "Looking for experienced Java developer",
    "requiredSkills": ["Java", "Spring Boot", "Microservices"],
    "minYearsExperience": 3,
    "maxYearsExperience": 8
  }'

# Example: Find matching jobs for a resume
curl http://localhost:8083/api/v1/matching/resume/1/jobs?limit=10
```

---

## Quick Start Script

Save this as `start-neuramatch.sh`:

```bash
#!/bin/bash

echo "🚀 Starting NeuraMatch..."

# 1. Start Docker services
echo "📦 Starting infrastructure..."
docker-compose up -d
sleep 10

# 2. Wait for services to be healthy
echo "⏳ Waiting for services..."
sleep 20

# 3. Verify infrastructure
echo "✅ Checking infrastructure..."
docker-compose ps

# 4. Start Spring Boot services (requires separate terminals)
echo "🔧 Start backend services in separate terminals:"
echo ""
echo "Terminal 1: cd backend/neuramatch-resume-service && JAVA_HOME=\$(/usr/libexec/java_home -v 17) mvn spring-boot:run"
echo "Terminal 2: cd backend/neuramatch-job-service && JAVA_HOME=\$(/usr/libexec/java_home -v 17) mvn spring-boot:run"
echo "Terminal 3: cd backend/neuramatch-matching-service && JAVA_HOME=\$(/usr/libexec/java_home -v 17) mvn spring-boot:run"
echo "Terminal 4: cd backend/neuramatch-api-gateway && JAVA_HOME=\$(/usr/libexec/java_home -v 17) mvn spring-boot:run"
echo ""
echo "✅ Infrastructure ready!"
```

Make it executable:
```bash
chmod +x start-neuramatch.sh
./start-neuramatch.sh
```

---

## Troubleshooting

### Issue: "Cannot connect to PostgreSQL"
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs neuramatch-postgres

# Restart PostgreSQL
docker restart neuramatch-postgres
```

### Issue: "Neo4j authentication failed"
```bash
# Reset Neo4j password
docker exec -it neuramatch-neo4j cypher-shell -u neo4j -p neo4j
# Then change password to 'password'
```

### Issue: "Gemini API key invalid"
- Verify key starts with `AIza`
- Check for extra spaces or quotes in .env file
- Generate new key at https://makersuite.google.com/app/apikey
- Ensure you're not exceeding free tier limits

### Issue: "Java version mismatch"
```bash
# Always use Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
java -version  # Should show version 17
```

### Issue: "Port already in use"
```bash
# Find process using port (e.g., 8081)
lsof -i :8081

# Kill process
kill -9 <PID>
```

---

## Optional: Frontend Setup

If you want to run the frontend (React):

```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/frontend

# Install dependencies
npm install

# Start development server
npm start

# Access at http://localhost:3000
```

---

## Connection Summary

### Required (Must Connect):
- ✅ PostgreSQL (via Docker)
- ✅ Neo4j (via Docker)
- ✅ Redis (via Docker)
- ✅ Kafka (via Docker)
- ✅ NLP Service (via Docker)
- 🔑 **Google Gemini API Key** (Get from Google AI Studio)

### Optional:
- ⭕ OpenAI API (Alternative to Gemini - not needed)
- ⭕ Pinecone (Alternative to pgvector - not needed)

---

## System Requirements

- **Java**: JDK 17
- **Maven**: 3.8+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Memory**: 8GB RAM minimum (16GB recommended)
- **Disk**: 10GB free space

---

## Next Steps After Setup

1. **Load Sample Data**: Import sample resumes and jobs
2. **Test Matching**: Verify semantic search works
3. **Monitor Performance**: Check Grafana dashboards
4. **API Testing**: Use Postman/Swagger to test all endpoints
5. **Integration Tests**: Run `mvn test` in each service

---

**Need Help?** Check the logs:
```bash
# Docker services
docker-compose logs -f [service-name]

# Spring Boot services
# Check terminal output where mvn spring-boot:run is running
```

---

**Generated**: 2025-10-24
**Status**: Ready for development and testing 🎉
