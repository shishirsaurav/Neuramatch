package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

/**
 * Represents skills that work well together
 * Example: React COMPLEMENTS TypeScript
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplementsRelationship {

    @RelationshipId
    private Long id;

    /**
     * How well these skills complement each other (0.0 to 1.0)
     */
    @Property("strength")
    private Double strength;

    /**
     * How commonly these skills are used together (0.0 to 1.0)
     */
    @Property("commonality")
    private Double commonality;

    @TargetNode
    private SkillNode targetSkill;
}
