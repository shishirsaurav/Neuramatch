// ============================================
// NeuraMatch Skill Knowledge Graph - Seed Data
// ============================================

// Clear existing data (use with caution!)
// MATCH (n) DETACH DELETE n;

// ============================================
// Programming Languages
// ============================================

CREATE (java:Skill {
  name: "java",
  displayName: "Java",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.92,
  trendScore: 0.85,
  avgSalaryImpact: 15000,
  difficultyLevel: "INTERMEDIATE",
  description: "Object-oriented programming language"
});

CREATE (python:Skill {
  name: "python",
  displayName: "Python",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.95,
  trendScore: 0.95,
  avgSalaryImpact: 18000,
  difficultyLevel: "BEGINNER",
  description: "High-level programming language"
});

CREATE (javascript:Skill {
  name: "javascript",
  displayName: "JavaScript",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.98,
  trendScore: 0.90,
  avgSalaryImpact: 14000,
  difficultyLevel: "BEGINNER"
});

CREATE (typescript:Skill {
  name: "typescript",
  displayName: "TypeScript",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.88,
  trendScore: 0.92,
  avgSalaryImpact: 16000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (go:Skill {
  name: "go",
  displayName: "Go",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.75,
  trendScore: 0.88,
  avgSalaryImpact: 20000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (rust:Skill {
  name: "rust",
  displayName: "Rust",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.65,
  trendScore: 0.95,
  avgSalaryImpact: 22000,
  difficultyLevel: "ADVANCED"
});

CREATE (csharp:Skill {
  name: "c#",
  displayName: "C#",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.82,
  trendScore: 0.80,
  avgSalaryImpact: 15000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (sql:Skill {
  name: "sql",
  displayName: "SQL",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.90,
  trendScore: 0.75,
  avgSalaryImpact: 10000,
  difficultyLevel: "BEGINNER"
});

// ============================================
// Frameworks
// ============================================

CREATE (spring:Skill {
  name: "spring",
  displayName: "Spring Framework",
  category: "FRAMEWORK",
  popularity: 0.88,
  trendScore: 0.82,
  avgSalaryImpact: 12000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (springboot:Skill {
  name: "spring boot",
  displayName: "Spring Boot",
  category: "FRAMEWORK",
  popularity: 0.90,
  trendScore: 0.90,
  avgSalaryImpact: 14000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (django:Skill {
  name: "django",
  displayName: "Django",
  category: "FRAMEWORK",
  popularity: 0.78,
  trendScore: 0.82,
  avgSalaryImpact: 13000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (flask:Skill {
  name: "flask",
  displayName: "Flask",
  category: "FRAMEWORK",
  popularity: 0.72,
  trendScore: 0.80,
  avgSalaryImpact: 12000,
  difficultyLevel: "BEGINNER"
});

CREATE (react:Skill {
  name: "react",
  displayName: "React",
  category: "FRAMEWORK",
  popularity: 0.95,
  trendScore: 0.92,
  avgSalaryImpact: 15000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (angular:Skill {
  name: "angular",
  displayName: "Angular",
  category: "FRAMEWORK",
  popularity: 0.80,
  trendScore: 0.75,
  avgSalaryImpact: 14000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (vue:Skill {
  name: "vue",
  displayName: "Vue.js",
  category: "FRAMEWORK",
  popularity: 0.75,
  trendScore: 0.85,
  avgSalaryImpact: 13000,
  difficultyLevel: "BEGINNER"
});

CREATE (nodejs:Skill {
  name: "node.js",
  displayName: "Node.js",
  category: "FRAMEWORK",
  popularity: 0.92,
  trendScore: 0.88,
  avgSalaryImpact: 14000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (dotnet:Skill {
  name: ".net",
  displayName: ".NET",
  category: "FRAMEWORK",
  popularity: 0.85,
  trendScore: 0.82,
  avgSalaryImpact: 13000,
  difficultyLevel: "INTERMEDIATE"
});

// ============================================
// Databases
// ============================================

CREATE (postgresql:Skill {
  name: "postgresql",
  displayName: "PostgreSQL",
  category: "DATABASE",
  popularity: 0.88,
  trendScore: 0.90,
  avgSalaryImpact: 12000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (mysql:Skill {
  name: "mysql",
  displayName: "MySQL",
  category: "DATABASE",
  popularity: 0.90,
  trendScore: 0.75,
  avgSalaryImpact: 10000,
  difficultyLevel: "BEGINNER"
});

CREATE (mongodb:Skill {
  name: "mongodb",
  displayName: "MongoDB",
  category: "DATABASE",
  popularity: 0.85,
  trendScore: 0.88,
  avgSalaryImpact: 13000,
  difficultyLevel: "BEGINNER"
});

CREATE (redis:Skill {
  name: "redis",
  displayName: "Redis",
  category: "DATABASE",
  popularity: 0.82,
  trendScore: 0.90,
  avgSalaryImpact: 12000,
  difficultyLevel: "BEGINNER"
});

CREATE (elasticsearch:Skill {
  name: "elasticsearch",
  displayName: "Elasticsearch",
  category: "DATABASE",
  popularity: 0.75,
  trendScore: 0.85,
  avgSalaryImpact: 15000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (cassandra:Skill {
  name: "cassandra",
  displayName: "Cassandra",
  category: "DATABASE",
  popularity: 0.60,
  trendScore: 0.70,
  avgSalaryImpact: 16000,
  difficultyLevel: "ADVANCED"
});

// ============================================
// Cloud Platforms
// ============================================

CREATE (aws:Skill {
  name: "aws",
  displayName: "AWS",
  category: "CLOUD_PLATFORM",
  popularity: 0.95,
  trendScore: 0.92,
  avgSalaryImpact: 20000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (azure:Skill {
  name: "azure",
  displayName: "Microsoft Azure",
  category: "CLOUD_PLATFORM",
  popularity: 0.85,
  trendScore: 0.90,
  avgSalaryImpact: 18000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (gcp:Skill {
  name: "gcp",
  displayName: "Google Cloud Platform",
  category: "CLOUD_PLATFORM",
  popularity: 0.78,
  trendScore: 0.88,
  avgSalaryImpact: 19000,
  difficultyLevel: "INTERMEDIATE"
});

// ============================================
// DevOps Tools
// ============================================

CREATE (docker:Skill {
  name: "docker",
  displayName: "Docker",
  category: "DEVOPS_TOOL",
  popularity: 0.92,
  trendScore: 0.90,
  avgSalaryImpact: 15000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (kubernetes:Skill {
  name: "kubernetes",
  displayName: "Kubernetes",
  category: "DEVOPS_TOOL",
  popularity: 0.88,
  trendScore: 0.95,
  avgSalaryImpact: 22000,
  difficultyLevel: "ADVANCED"
});

CREATE (jenkins:Skill {
  name: "jenkins",
  displayName: "Jenkins",
  category: "DEVOPS_TOOL",
  popularity: 0.80,
  trendScore: 0.70,
  avgSalaryImpact: 10000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (git:Skill {
  name: "git",
  displayName: "Git",
  category: "DEVOPS_TOOL",
  popularity: 0.98,
  trendScore: 0.85,
  avgSalaryImpact: 8000,
  difficultyLevel: "BEGINNER"
});

CREATE (kafka:Skill {
  name: "kafka",
  displayName: "Apache Kafka",
  category: "DEVOPS_TOOL",
  popularity: 0.75,
  trendScore: 0.90,
  avgSalaryImpact: 18000,
  difficultyLevel: "INTERMEDIATE"
});

CREATE (terraform:Skill {
  name: "terraform",
  displayName: "Terraform",
  category: "DEVOPS_TOOL",
  popularity: 0.78,
  trendScore: 0.92,
  avgSalaryImpact: 17000,
  difficultyLevel: "INTERMEDIATE"
});

// ============================================
// Relationships - REQUIRES (Prerequisites)
// ============================================

MATCH (sb:Skill {name: "spring boot"}), (j:Skill {name: "java"})
CREATE (sb)-[:REQUIRES {strength: 0.95, required: true}]->(j);

MATCH (s:Skill {name: "spring"}), (j:Skill {name: "java"})
CREATE (s)-[:REQUIRES {strength: 0.95, required: true}]->(j);

MATCH (d:Skill {name: "django"}), (p:Skill {name: "python"})
CREATE (d)-[:REQUIRES {strength: 0.95, required: true}]->(p);

MATCH (f:Skill {name: "flask"}), (p:Skill {name: "python"})
CREATE (f)-[:REQUIRES {strength: 0.90, required: true}]->(p);

MATCH (r:Skill {name: "react"}), (js:Skill {name: "javascript"})
CREATE (r)-[:REQUIRES {strength: 0.90, required: true}]->(js);

MATCH (a:Skill {name: "angular"}), (ts:Skill {name: "typescript"})
CREATE (a)-[:REQUIRES {strength: 0.85, required: true}]->(ts);

MATCH (v:Skill {name: "vue"}), (js:Skill {name: "javascript"})
CREATE (v)-[:REQUIRES {strength: 0.85, required: true}]->(js);

MATCH (n:Skill {name: "node.js"}), (js:Skill {name: "javascript"})
CREATE (n)-[:REQUIRES {strength: 0.95, required: true}]->(js);

MATCH (k:Skill {name: "kubernetes"}), (d:Skill {name: "docker"})
CREATE (k)-[:REQUIRES {strength: 0.80, required: false}]->(d);

// ============================================
// Relationships - COMPLEMENTS
// ============================================

MATCH (r:Skill {name: "react"}), (ts:Skill {name: "typescript"})
CREATE (r)-[:COMPLEMENTS {strength: 0.85, commonality: 0.90}]->(ts);

MATCH (sb:Skill {name: "spring boot"}), (pg:Skill {name: "postgresql"})
CREATE (sb)-[:COMPLEMENTS {strength: 0.75, commonality: 0.80}]->(pg);

MATCH (d:Skill {name: "django"}), (pg:Skill {name: "postgresql"})
CREATE (d)-[:COMPLEMENTS {strength: 0.80, commonality: 0.85}]->(pg);

MATCH (n:Skill {name: "node.js"}), (m:Skill {name: "mongodb"})
CREATE (n)-[:COMPLEMENTS {strength: 0.85, commonality: 0.88}]->(m);

MATCH (d:Skill {name: "docker"}), (k:Skill {name: "kubernetes"})
CREATE (d)-[:COMPLEMENTS {strength: 0.90, commonality: 0.95}]->(k);

MATCH (k:Skill {name: "kafka"}), (sb:Skill {name: "spring boot"})
CREATE (k)-[:COMPLEMENTS {strength: 0.70, commonality: 0.75}]->(sb);

MATCH (r:Skill {name: "redis"}), (n:Skill {name: "node.js"})
CREATE (r)-[:COMPLEMENTS {strength: 0.70, commonality: 0.75}]->(n);

MATCH (tf:Skill {name: "terraform"}), (aws:Skill {name: "aws"})
CREATE (tf)-[:COMPLEMENTS {strength: 0.85, commonality: 0.90}]->(aws);

// ============================================
// Relationships - ALTERNATIVE_TO
// ============================================

MATCH (pg:Skill {name: "postgresql"}), (my:Skill {name: "mysql"})
CREATE (pg)-[:ALTERNATIVE_TO {similarity: 0.85, transferability: 0.90}]->(my);

MATCH (r:Skill {name: "react"}), (a:Skill {name: "angular"})
CREATE (r)-[:ALTERNATIVE_TO {similarity: 0.70, transferability: 0.75}]->(a);

MATCH (r:Skill {name: "react"}), (v:Skill {name: "vue"})
CREATE (r)-[:ALTERNATIVE_TO {similarity: 0.75, transferability: 0.80}]->(v);

MATCH (aws:Skill {name: "aws"}), (az:Skill {name: "azure"})
CREATE (aws)-[:ALTERNATIVE_TO {similarity: 0.80, transferability: 0.85}]->(az);

MATCH (aws:Skill {name: "aws"}), (gcp:Skill {name: "gcp"})
CREATE (aws)-[:ALTERNATIVE_TO {similarity: 0.82, transferability: 0.87}]->(gcp);

MATCH (d:Skill {name: "django"}), (f:Skill {name: "flask"})
CREATE (d)-[:ALTERNATIVE_TO {similarity: 0.65, transferability: 0.85}]->(f);

MATCH (m:Skill {name: "mongodb"}), (c:Skill {name: "cassandra"})
CREATE (m)-[:ALTERNATIVE_TO {similarity: 0.60, transferability: 0.70}]->(c);

// ============================================
// Relationships - PART_OF
// ============================================

MATCH (sb:Skill {name: "spring boot"}), (s:Skill {name: "spring"})
CREATE (sb)-[:PART_OF {scope: "framework"}]->(s);

MATCH (n:Skill {name: "node.js"}), (js:Skill {name: "javascript"})
CREATE (n)-[:PART_OF {scope: "runtime"}]->(js);

// ============================================
// Relationships - SYNONYM_OF
// ============================================

CREATE (js:Skill {
  name: "js",
  displayName: "JS",
  category: "PROGRAMMING_LANGUAGE",
  popularity: 0.98,
  trendScore: 0.90,
  avgSalaryImpact: 14000,
  difficultyLevel: "BEGINNER"
});

MATCH (js:Skill {name: "js"}), (javascript:Skill {name: "javascript"})
CREATE (js)-[:SYNONYM_OF {commonUsage: 0.95}]->(javascript);

CREATE (k8s:Skill {
  name: "k8s",
  displayName: "K8s",
  category: "DEVOPS_TOOL",
  popularity: 0.88,
  trendScore: 0.95,
  avgSalaryImpact: 22000,
  difficultyLevel: "ADVANCED"
});

MATCH (k8s:Skill {name: "k8s"}), (k:Skill {name: "kubernetes"})
CREATE (k8s)-[:SYNONYM_OF {commonUsage: 0.90}]->(k);

// ============================================
// Verification Queries
// ============================================

// Count total skills
// MATCH (s:Skill) RETURN count(s) as totalSkills;

// Count relationships by type
// MATCH ()-[r]->() RETURN type(r) as relationType, count(r) as count ORDER BY count DESC;

// Find all prerequisites for a skill
// MATCH (s:Skill {name: "spring boot"})-[:REQUIRES]->(req:Skill) RETURN req.displayName;

// Find complementary skills
// MATCH (s:Skill {name: "react"})-[:COMPLEMENTS]->(comp:Skill) RETURN comp.displayName;

// Find alternatives
// MATCH (s:Skill {name: "postgresql"})-[:ALTERNATIVE_TO]->(alt:Skill) RETURN alt.displayName;
