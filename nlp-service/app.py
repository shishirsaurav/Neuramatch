from flask import Flask, request, jsonify
import spacy
import re
from datetime import datetime
from dateutil import parser as date_parser
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load SpaCy model
try:
    nlp = spacy.load("en_core_web_sm")
    logger.info("SpaCy model loaded successfully")
except:
    logger.error("SpaCy model not found. Run: python -m spacy download en_core_web_sm")
    nlp = None

# Skill keywords database (expandable)
PROGRAMMING_LANGUAGES = {
    'java', 'python', 'javascript', 'typescript', 'c++', 'c#', 'ruby', 'go', 'rust',
    'php', 'swift', 'kotlin', 'scala', 'r', 'matlab', 'perl', 'bash', 'sql'
}

FRAMEWORKS = {
    'spring', 'spring boot', 'react', 'angular', 'vue', 'django', 'flask', 'express',
    'nodejs', 'node.js', '.net', 'asp.net', 'laravel', 'rails', 'fastapi'
}

DATABASES = {
    'postgresql', 'mysql', 'mongodb', 'redis', 'cassandra', 'elasticsearch',
    'dynamodb', 'oracle', 'sql server', 'sqlite', 'neo4j'
}

CLOUD_PLATFORMS = {
    'aws', 'azure', 'gcp', 'google cloud', 'heroku', 'digitalocean', 'kubernetes',
    'docker', 'openshift'
}

TOOLS = {
    'git', 'jenkins', 'gitlab', 'github', 'jira', 'confluence', 'maven', 'gradle',
    'terraform', 'ansible', 'kafka', 'rabbitmq', 'nginx'
}

ALL_SKILLS = PROGRAMMING_LANGUAGES | FRAMEWORKS | DATABASES | CLOUD_PLATFORMS | TOOLS

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy", "service": "nlp-service"})

@app.route('/extract', methods=['POST'])
def extract_entities():
    """Extract all entities from resume text"""
    if not nlp:
        return jsonify({"error": "NLP model not loaded"}), 500

    data = request.get_json()
    text = data.get('text', '')

    if not text:
        return jsonify({"error": "No text provided"}), 400

    doc = nlp(text)

    result = {
        "skills": extract_skills(text, doc),
        "experiences": extract_experiences(text, doc),
        "education": extract_education(text, doc),
        "contact": extract_contact_info(text, doc)
    }

    logger.info(f"Extracted entities from {len(text)} chars")
    return jsonify(result)

def extract_skills(text, doc):
    """Extract technical skills from text"""
    skills = []
    text_lower = text.lower()

    # Extract skills from predefined list
    for skill in ALL_SKILLS:
        if re.search(r'\b' + re.escape(skill) + r'\b', text_lower):
            category = categorize_skill(skill)
            proficiency = infer_proficiency(text_lower, skill)

            skills.append({
                "skillName": skill.title(),
                "category": category,
                "proficiencyLevel": proficiency,
                "confidenceScore": 0.85
            })

    # Extract technology entities
    for ent in doc.ents:
        if ent.label_ in ['PRODUCT', 'ORG']:
            skill_name = ent.text.strip()
            if len(skill_name) > 2 and skill_name.lower() not in [s['skillName'].lower() for s in skills]:
                skills.append({
                    "skillName": skill_name,
                    "category": "TOOL",
                    "proficiencyLevel": "INTERMEDIATE",
                    "confidenceScore": 0.6
                })

    return skills

def categorize_skill(skill):
    """Categorize a skill"""
    if skill in PROGRAMMING_LANGUAGES:
        return "PROGRAMMING_LANGUAGE"
    elif skill in FRAMEWORKS:
        return "FRAMEWORK"
    elif skill in DATABASES:
        return "DATABASE"
    elif skill in CLOUD_PLATFORMS:
        return "CLOUD_PLATFORM"
    elif skill in TOOLS:
        return "DEVOPS_TOOL"
    else:
        return "OTHER"

def infer_proficiency(text, skill):
    """Infer proficiency level from context"""
    skill_context = re.search(r'.{0,100}\b' + re.escape(skill) + r'\b.{0,100}', text, re.IGNORECASE)

    if not skill_context:
        return "INTERMEDIATE"

    context = skill_context.group().lower()

    if any(word in context for word in ['expert', 'advanced', 'proficient', 'senior', 'lead']):
        return "EXPERT"
    elif any(word in context for word in ['experienced', 'strong', 'solid']):
        return "ADVANCED"
    elif any(word in context for word in ['basic', 'beginner', 'learning', 'familiar']):
        return "BEGINNER"
    else:
        return "INTERMEDIATE"

def extract_experiences(text, doc):
    """Extract work experience from text"""
    experiences = []

    # Split by common section headers
    exp_section = extract_section(text, ['experience', 'work history', 'employment'])

    if not exp_section:
        return experiences

    # Split into individual experiences (by newlines or job titles)
    exp_blocks = re.split(r'\n{2,}', exp_section)

    for block in exp_blocks:
        if len(block.strip()) < 20:
            continue

        exp = parse_experience_block(block, nlp)
        if exp:
            experiences.append(exp)

    return experiences

def parse_experience_block(text, nlp_model):
    """Parse a single experience block"""
    doc = nlp_model(text)

    # Extract company name (usually ORG entity)
    companies = [ent.text for ent in doc.ents if ent.label_ == 'ORG']

    # Extract dates
    dates = extract_dates(text)

    # Extract job title (first line or before company)
    lines = text.split('\n')
    job_title = lines[0].strip() if lines else "Unknown"

    # Check if current role
    is_current = 'present' in text.lower() or 'current' in text.lower()

    # Extract leadership indicators
    team_size = extract_team_size(text)
    leadership_role = extract_leadership_role(text)

    # Extract impact metrics
    impact_metrics = extract_impact_metrics(text)

    return {
        "jobTitle": job_title[:100],
        "companyName": companies[0] if companies else "Unknown",
        "startDate": dates[0] if len(dates) > 0 else None,
        "endDate": dates[1] if len(dates) > 1 and not is_current else None,
        "isCurrentRole": is_current,
        "description": text[:500],
        "teamSize": team_size,
        "leadershipRole": leadership_role,
        "impactMetrics": impact_metrics
    }

def extract_education(text, doc):
    """Extract education information"""
    educations = []

    edu_section = extract_section(text, ['education', 'academic', 'qualification'])

    if not edu_section:
        return educations

    # Common degree patterns
    degree_patterns = [
        r"(Bachelor|Master|PhD|M\.?S\.?|B\.?S\.?|B\.?A\.?|M\.?A\.?|MBA|Ph\.?D\.?).*?(?:in|of)\s+([A-Z][a-zA-Z\s]+)",
        r"(Bachelor|Master|PhD|Doctorate).*?([A-Z][a-zA-Z\s]+)"
    ]

    for pattern in degree_patterns:
        matches = re.finditer(pattern, edu_section, re.IGNORECASE)
        for match in matches:
            degree = match.group(1)
            field = match.group(2).strip() if len(match.groups()) > 1 else "Unknown"

            # Extract institution
            doc_section = nlp(edu_section)
            institutions = [ent.text for ent in doc_section.ents if ent.label_ == 'ORG']

            # Extract GPA
            gpa = extract_gpa(edu_section)

            # Extract dates
            dates = extract_dates(edu_section)

            educations.append({
                "degree": degree,
                "fieldOfStudy": field[:100],
                "institutionName": institutions[0] if institutions else "Unknown",
                "gpa": gpa,
                "startDate": dates[0] if len(dates) > 0 else None,
                "endDate": dates[1] if len(dates) > 1 else None,
                "educationLevel": map_education_level(degree)
            })

    return educations[:5]  # Limit to 5 education entries

def extract_contact_info(text, doc):
    """Extract contact information"""
    # Extract email
    email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
    emails = re.findall(email_pattern, text)

    # Extract phone
    phone_pattern = r'\+?1?\s*\(?([0-9]{3})\)?[-.\s]?([0-9]{3})[-.\s]?([0-9]{4})'
    phones = re.findall(phone_pattern, text)

    # Extract LinkedIn
    linkedin_pattern = r'linkedin\.com/in/[\w-]+'
    linkedin = re.findall(linkedin_pattern, text.lower())

    # Extract GitHub
    github_pattern = r'github\.com/[\w-]+'
    github = re.findall(github_pattern, text.lower())

    # Extract name (first PERSON entity)
    names = [ent.text for ent in doc.ents if ent.label_ == 'PERSON']

    return {
        "fullName": names[0] if names else None,
        "email": emails[0] if emails else None,
        "phone": '-'.join(phones[0]) if phones else None,
        "linkedinUrl": 'https://' + linkedin[0] if linkedin else None,
        "githubUrl": 'https://' + github[0] if github else None
    }

def extract_section(text, headers):
    """Extract a section from text based on headers"""
    text_lower = text.lower()
    for header in headers:
        pattern = r'(?:^|\n)\s*' + header + r'\s*[:\n](.+?)(?=\n\s*(?:education|experience|skills|projects|certifications|$))'
        match = re.search(pattern, text_lower, re.DOTALL | re.IGNORECASE)
        if match:
            return match.group(1)
    return None

def extract_dates(text):
    """Extract dates from text"""
    dates = []

    # Common date patterns
    date_patterns = [
        r'(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{4}',
        r'\d{1,2}/\d{4}',
        r'\d{4}'
    ]

    for pattern in date_patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        for match in matches:
            try:
                date = date_parser.parse(match, fuzzy=True)
                dates.append(date.strftime('%Y-%m-%d'))
            except:
                pass

    return dates[:2]  # Return max 2 dates (start and end)

def extract_team_size(text):
    """Extract team size from text"""
    patterns = [
        r'team of (\d+)',
        r'led (\d+)',
        r'managed (\d+)',
        r'(\d+)[- ]person team'
    ]

    for pattern in patterns:
        match = re.search(pattern, text.lower())
        if match:
            return int(match.group(1))

    return None

def extract_leadership_role(text):
    """Extract leadership role"""
    leadership_keywords = ['lead', 'manager', 'director', 'head', 'senior', 'principal', 'architect', 'chief']

    for keyword in leadership_keywords:
        if keyword in text.lower():
            return keyword.title()

    return None

def extract_impact_metrics(text):
    """Extract impact metrics (percentages, numbers)"""
    patterns = [
        r'(\d+%)',
        r'reduced.*?by (\d+%)',
        r'increased.*?by (\d+%)',
        r'improved.*?by (\d+%)'
    ]

    metrics = []
    for pattern in patterns:
        matches = re.findall(pattern, text.lower())
        metrics.extend(matches)

    return ', '.join(metrics[:3]) if metrics else None

def extract_gpa(text):
    """Extract GPA from text"""
    gpa_pattern = r'GPA:?\s*(\d+\.?\d*)\s*/?\s*(\d+\.?\d*)?'
    match = re.search(gpa_pattern, text, re.IGNORECASE)

    if match:
        gpa = float(match.group(1))
        max_gpa = float(match.group(2)) if match.group(2) else 4.0
        return gpa

    return None

def map_education_level(degree):
    """Map degree to education level"""
    degree_lower = degree.lower()

    if 'phd' in degree_lower or 'doctorate' in degree_lower:
        return "PHD"
    elif 'master' in degree_lower or 'ms' in degree_lower or 'ma' in degree_lower or 'mba' in degree_lower:
        return "MASTERS"
    elif 'bachelor' in degree_lower or 'bs' in degree_lower or 'ba' in degree_lower:
        return "BACHELORS"
    else:
        return "OTHER"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
