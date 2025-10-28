// ============================================
// NeuraMatch Skill Knowledge Graph Schema
// ============================================

// Create constraints and indexes
CREATE CONSTRAINT skill_name_unique IF NOT EXISTS FOR (s:Skill) REQUIRE s.name IS UNIQUE;
CREATE INDEX skill_category_index IF NOT EXISTS FOR (s:Skill) ON (s.category);
CREATE INDEX skill_popularity_index IF NOT EXISTS FOR (s:Skill) ON (s.popularity);

// ============================================
// Node Structure
// ============================================

// Skill Node Properties:
// - name: String (unique identifier, lowercase)
// - displayName: String (proper display name)
// - category: String (PROGRAMMING_LANGUAGE, FRAMEWORK, DATABASE, etc.)
// - popularity: Float (0.0 to 1.0, market demand indicator)
// - trendScore: Float (0.0 to 1.0, growing/declining)
// - avgSalaryImpact: Integer (average salary increase in USD)
// - difficultyLevel: String (BEGINNER, INTERMEDIATE, ADVANCED)
// - description: String (optional description)

// ============================================
// Relationship Types
// ============================================

// REQUIRES - Prerequisite relationship
// Properties: strength (0.0-1.0), required (boolean)
// Example: Spring Boot -[:REQUIRES {strength: 0.9}]-> Java

// COMPLEMENTS - Skills that work well together
// Properties: strength (0.0-1.0), commonality (0.0-1.0)
// Example: React -[:COMPLEMENTS {strength: 0.8}]-> TypeScript

// ALTERNATIVE_TO - Interchangeable skills
// Properties: similarity (0.0-1.0), transferability (0.0-1.0)
// Example: PostgreSQL -[:ALTERNATIVE_TO {similarity: 0.85}]-> MySQL

// EVOLVES_TO - Technology evolution
// Properties: timeline (String), adoptionRate (0.0-1.0)
// Example: AngularJS -[:EVOLVES_TO {timeline: "2016"}]-> Angular

// PART_OF - Hierarchical relationship
// Properties: scope (String)
// Example: Spring Boot -[:PART_OF]-> Spring Ecosystem

// SYNONYM_OF - Alternative names
// Properties: commonUsage (0.0-1.0)
// Example: JS -[:SYNONYM_OF]-> JavaScript

// Sample queries to verify schema
// MATCH (s:Skill) RETURN count(s) as totalSkills;
// MATCH (s:Skill)-[r]->() RETURN type(r) as relationType, count(r) as count;
