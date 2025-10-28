package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

/**
 * Represents a prerequisite relationship between skills
 * Example: Spring Boot REQUIRES Java
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequiresRelationship {

    @RelationshipId
    private Long id;

    /**
     * How strongly this prerequisite is required (0.0 to 1.0)
     */
    @Property("strength")
    private Double strength;

    /**
     * Is this an absolute requirement?
     */
    @Property("required")
    private Boolean required;

    @TargetNode
    private SkillNode targetSkill;
}
