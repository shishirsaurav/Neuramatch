package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Neo4j entity representing a skill in the knowledge graph
 */
@Node("Skill")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillNode {

    @Id
    private String name; // Unique identifier (lowercase)

    private String displayName; // Proper display name

    @Property("category")
    private SkillCategory category;

    @Property("popularity")
    private Double popularity; // 0.0 to 1.0

    @Property("trendScore")
    private Double trendScore; // 0.0 to 1.0

    @Property("avgSalaryImpact")
    private Integer avgSalaryImpact; // USD salary impact

    @Property("difficultyLevel")
    private DifficultyLevel difficultyLevel;

    @Property("description")
    private String description;

    // Relationships
    @Relationship(type = "REQUIRES", direction = Relationship.Direction.OUTGOING)
    private Set<RequiresRelationship> prerequisites = new HashSet<>();

    @Relationship(type = "COMPLEMENTS", direction = Relationship.Direction.OUTGOING)
    private Set<ComplementsRelationship> complements = new HashSet<>();

    @Relationship(type = "ALTERNATIVE_TO", direction = Relationship.Direction.OUTGOING)
    private Set<AlternativeToRelationship> alternatives = new HashSet<>();

    @Relationship(type = "SYNONYM_OF", direction = Relationship.Direction.OUTGOING)
    private Set<SynonymOfRelationship> synonyms = new HashSet<>();

    @Relationship(type = "PART_OF", direction = Relationship.Direction.OUTGOING)
    private Set<PartOfRelationship> partOf = new HashSet<>();

    public enum SkillCategory {
        PROGRAMMING_LANGUAGE,
        FRAMEWORK,
        DATABASE,
        CLOUD_PLATFORM,
        DEVOPS_TOOL,
        SOFT_SKILL,
        DOMAIN_KNOWLEDGE,
        TOOL,
        METHODOLOGY,
        CERTIFICATION,
        OTHER
    }

    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}
