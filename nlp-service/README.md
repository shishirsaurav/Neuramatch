# NLP Service - Resume Entity Extraction

Python-based NLP service using SpaCy for extracting structured information from resumes.

## Features

- **Named Entity Recognition (NER)** using SpaCy
- **Skill Extraction** from predefined technology database
- **Experience Extraction** with dates, companies, and job titles
- **Education Extraction** with degrees and institutions
- **Contact Information Extraction** (email, phone, LinkedIn, GitHub)

## Tech Stack

- Python 3.11
- Flask (REST API)
- SpaCy (NLP/NER)
- python-dateutil (date parsing)
- Gunicorn (production server)

## Installation

### Local Development

```bash
# Create virtual environment
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Download SpaCy model
python -m spacy download en_core_web_sm

# Run the service
python app.py
```

The service will be available at `http://localhost:5000`

### Docker

```bash
# Build image
docker build -t neuramatch-nlp-service .

# Run container
docker run -p 5000:5000 neuramatch-nlp-service
```

### Docker Compose

```bash
# From project root
docker-compose up nlp-service
```

## API Endpoints

### Health Check

```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "nlp-service"
}
```

### Extract Entities

```http
POST /extract
Content-Type: application/json

{
  "text": "Resume text content..."
}
```

**Response:**
```json
{
  "contact": {
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "123-456-7890",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "githubUrl": "https://github.com/johndoe"
  },
  "skills": [
    {
      "skillName": "Java",
      "category": "PROGRAMMING_LANGUAGE",
      "proficiencyLevel": "EXPERT",
      "confidenceScore": 0.9
    }
  ],
  "experiences": [
    {
      "jobTitle": "Senior Software Engineer",
      "companyName": "Tech Corp",
      "startDate": "2020-01-01",
      "endDate": "2023-12-31",
      "isCurrentRole": false,
      "teamSize": 5,
      "leadershipRole": "Lead",
      "impactMetrics": "40%"
    }
  ],
  "education": [
    {
      "degree": "Bachelor of Science",
      "fieldOfStudy": "Computer Science",
      "institutionName": "State University",
      "gpa": 3.8,
      "educationLevel": "BACHELORS"
    }
  ]
}
```

## Skill Categories

The service recognizes skills in the following categories:

- **PROGRAMMING_LANGUAGE**: Java, Python, JavaScript, etc.
- **FRAMEWORK**: Spring Boot, React, Django, etc.
- **DATABASE**: PostgreSQL, MongoDB, Redis, etc.
- **CLOUD_PLATFORM**: AWS, Azure, GCP, Kubernetes, etc.
- **DEVOPS_TOOL**: Git, Jenkins, Docker, etc.

## Proficiency Levels

Inferred from context:
- **EXPERT**: "expert", "advanced", "proficient", "senior", "lead"
- **ADVANCED**: "experienced", "strong", "solid"
- **BEGINNER**: "basic", "beginner", "learning", "familiar"
- **INTERMEDIATE**: Default if no context found

## Extraction Features

### Contact Information
- Email addresses (regex pattern)
- Phone numbers (US format)
- LinkedIn profiles
- GitHub profiles
- Full name (first PERSON entity)

### Skills
- Matches against 100+ technology keywords
- Categorizes automatically
- Infers proficiency from context
- Confidence scoring

### Experience
- Job titles and companies
- Start/end dates
- Current role detection
- Team size extraction
- Leadership role identification
- Impact metrics (percentages)

### Education
- Degree types (Bachelor, Master, PhD)
- Field of study
- Institutions (ORG entities)
- GPA extraction
- Date ranges

## Testing

```bash
# Test health endpoint
curl http://localhost:5000/health

# Test extraction
curl -X POST http://localhost:5000/extract \
  -H "Content-Type: application/json" \
  -d '{"text": "Sample resume text..."}'
```

## Production Deployment

The service uses Gunicorn for production:

```bash
gunicorn --bind 0.0.0.0:5000 --workers 2 --timeout 120 app:app
```

Configuration:
- 2 worker processes
- 120-second timeout (for large resumes)
- Bind to all interfaces

## Error Handling

- Returns 400 for missing text
- Returns 500 if SpaCy model not loaded
- Logs all extraction attempts
- Graceful fallback for parsing errors

## Performance

- Average extraction time: 1-3 seconds
- Handles resumes up to 10 pages
- Concurrent request support via Gunicorn workers

## Future Enhancements

- Custom NER model training
- Soft skills extraction
- Certification detection
- Multi-language support
- Achievement/impact parsing
- Project extraction
