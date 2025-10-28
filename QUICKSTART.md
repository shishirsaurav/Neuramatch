# NeuraMatch Quick Start 🚀

Since you already have your Gemini API key, follow these 4 simple steps:

---

## Step 1: Configure Environment (30 seconds)

```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch

# Copy the example file
cp .env.example .env

# Edit .env and add your Gemini API key
nano .env
# Or use your favorite editor:
# code .env
# vim .env
```

Replace this line:
```bash
GEMINI_API_KEY=your_gemini_api_key_here
```

With your actual key:
```bash
GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXX
```

Save and close.

---

## Step 2: Start Infrastructure (2 minutes)

```bash
# Start all Docker services
docker-compose up -d

# Wait for services to be healthy (takes ~30 seconds)
echo "Waiting for services to start..."
sleep 30

# Verify all services are running
docker-compose ps
```

You should see all services as "healthy" or "running":
- ✅ neuramatch-postgres
- ✅ neuramatch-neo4j
- ✅ neuramatch-redis
- ✅ neuramatch-kafka
- ✅ neuramatch-zookeeper
- ✅ neuramatch-nlp-service
- ✅ neuramatch-prometheus
- ✅ neuramatch-grafana

---

## Step 3: Build Backend (one-time, ~2 minutes)

```bash
cd backend

# Build all services with Java 17
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean install -DskipTests
```

Wait for: `[INFO] BUILD SUCCESS`

---

## Step 4: Start Microservices

Open **4 separate terminal windows/tabs**:

### Terminal 1 - Resume Service (Port 8081)
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-resume-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```
Wait for: `Started ResumeServiceApplication`

### Terminal 2 - Job Service (Port 8082)
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-job-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```
Wait for: `Started JobServiceApplication`

### Terminal 3 - Matching Service (Port 8083)
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-matching-service
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```
Wait for: `Started MatchingServiceApplication`

### Terminal 4 - API Gateway (Port 8080)
```bash
cd /Users/shishirsaurav/Desktop/NeuraMatch/backend/neuramatch-api-gateway
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```
Wait for: `Started ApiGatewayApplication`

---

## Step 5: Verify Everything Works ✅

Open a new terminal and run:

```bash
# Check all services
echo "Resume Service:" && curl -s http://localhost:8081/actuator/health
echo "Job Service:" && curl -s http://localhost:8082/actuator/health
echo "Matching Service:" && curl -s http://localhost:8083/actuator/health
echo "API Gateway:" && curl -s http://localhost:8080/actuator/health
echo "NLP Service:" && curl -s http://localhost:5000/health
```

All should return: `{"status":"UP"}`

---

## 🎉 You're Ready!

### Access Points:

| Service | URL | Description |
|---------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Main entry point |
| **Swagger UI (Resume)** | http://localhost:8081/swagger-ui.html | Resume API docs |
| **Swagger UI (Job)** | http://localhost:8082/swagger-ui.html | Job API docs |
| **Swagger UI (Matching)** | http://localhost:8083/swagger-ui.html | Matching API docs |
| **Neo4j Browser** | http://localhost:7474 | Skill graph (neo4j/password) |
| **Grafana** | http://localhost:3000 | Monitoring (admin/admin) |
| **Prometheus** | http://localhost:9090 | Metrics |

---

## Test the System 🧪

### 1. Create a Resume:
```bash
curl -X POST http://localhost:8081/api/v1/resumes \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Developer",
    "email": "jane@example.com",
    "phone": "555-1234",
    "summary": "Senior Java developer with 7 years experience in Spring Boot microservices",
    "experiences": [{
      "title": "Senior Software Engineer",
      "company": "Tech Corp",
      "startDate": "2018-01-01",
      "endDate": "2024-12-01",
      "description": "Led development of microservices architecture"
    }],
    "skills": [
      {"skillName": "Java", "proficiency": "EXPERT", "yearsOfExperience": 7},
      {"skillName": "Spring Boot", "proficiency": "ADVANCED", "yearsOfExperience": 6},
      {"skillName": "Microservices", "proficiency": "ADVANCED", "yearsOfExperience": 5},
      {"skillName": "PostgreSQL", "proficiency": "INTERMEDIATE", "yearsOfExperience": 4}
    ],
    "education": [{
      "degree": "Bachelor of Science",
      "fieldOfStudy": "Computer Science",
      "institution": "State University",
      "graduationYear": 2017
    }]
  }'
```

### 2. Create a Job:
```bash
curl -X POST http://localhost:8082/api/v1/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "jobTitle": "Senior Java Engineer",
    "companyName": "Awesome Startup",
    "jobDescription": "We are looking for an experienced Java developer to join our team",
    "responsibilities": "Design and develop microservices, mentor junior developers",
    "qualifications": "5+ years Java experience, strong Spring Boot knowledge",
    "requiredSkills": ["Java", "Spring Boot", "Microservices", "REST APIs"],
    "minYearsExperience": 5,
    "maxYearsExperience": 10,
    "location": "San Francisco, CA",
    "remoteType": "HYBRID",
    "employmentType": "FULL_TIME",
    "minSalary": 120000,
    "maxSalary": 180000
  }'
```

### 3. Find Matching Jobs:
```bash
# Get matches for resume ID 1
curl http://localhost:8083/api/v1/matching/resume/1/jobs?limit=10
```

### 4. Get Match Explanation:
```bash
# Explain why resume 1 matches job 1
curl http://localhost:8083/api/v1/explainability/match/1/1
```

---

## Stop Everything 🛑

When you're done:

```bash
# Stop Spring Boot services: Press Ctrl+C in each terminal

# Stop Docker services:
docker-compose down

# Stop and remove all data (CAUTION - deletes database):
# docker-compose down -v
```

---

## Troubleshooting 🔧

### "Cannot connect to database"
```bash
# Check Docker services
docker-compose ps

# Restart if needed
docker-compose restart postgres
```

### "Port already in use"
```bash
# Find what's using the port
lsof -i :8081

# Kill it
kill -9 <PID>
```

### "Java version error"
```bash
# Make sure you're using Java 17
java -version

# Should show: openjdk version "17.x.x"
```

### "Gemini API error"
- Check your API key in `.env` file
- Verify no extra spaces or quotes
- Check quota: https://makersuite.google.com/app/apikey

---

## Daily Development Workflow

```bash
# Morning: Start infrastructure
docker-compose up -d

# Start your microservices (4 terminals)
# ... run mvn spring-boot:run in each service

# Develop and test...

# Evening: Stop everything
# Ctrl+C in each terminal
docker-compose stop
```

---

## Next Steps 📚

1. **Load Test Data**: Import sample resumes and jobs
2. **Explore Neo4j**: View skill relationships at http://localhost:7474
3. **Monitor**: Check Grafana dashboards at http://localhost:3000
4. **API Testing**: Use Swagger UI or Postman
5. **Run Tests**: `mvn test` in each service

---

**Need help?** Check:
- `SETUP_GUIDE.md` - Detailed setup instructions
- `COMPLETE_PROJECT_STATUS.md` - Project overview
- `FINAL_COMPILATION_FIXES.md` - Technical details

**Ready to code!** 🎉
