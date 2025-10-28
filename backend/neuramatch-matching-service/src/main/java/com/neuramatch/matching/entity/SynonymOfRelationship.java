package com.neuramatch.matching.entity;

import org.springframework.data.neo4j.core.schema.*;
import lombok.*;

/**
 * Represents alternative names for the same skill
 * Example: K8s SYNONYM_OF Kubernetes
 */
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SynonymOfRelationship {

    @RelationshipId
    private Long id;

    /**
     * How commonly this synonym is used (0.0 to 1.0)
     */
    @Property("commonUsage")
    private Double commonUsage;

    @TargetNode
    private SkillNode targetSkill;
}
