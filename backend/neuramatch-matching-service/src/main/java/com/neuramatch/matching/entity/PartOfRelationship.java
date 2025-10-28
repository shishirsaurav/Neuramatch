package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

/**
 * Represents hierarchical relationship between skills
 * Example: Spring Boot PART_OF Spring Ecosystem
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartOfRelationship {

    @RelationshipId
    private Long id;

    /**
     * Scope of the relationship (framework, ecosystem, platform, etc.)
     */
    @Property("scope")
    private String scope;

    @TargetNode
    private SkillNode targetSkill;
}
