package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

/**
 * Represents interchangeable skills
 * Example: PostgreSQL ALTERNATIVE_TO MySQL
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativeToRelationship {

    @RelationshipId
    private Long id;

    /**
     * How similar these skills are (0.0 to 1.0)
     */
    @Property("similarity")
    private Double similarity;

    /**
     * How easily skills transfer between these technologies (0.0 to 1.0)
     */
    @Property("transferability")
    private Double transferability;

    @TargetNode
    private SkillNode targetSkill;
}
