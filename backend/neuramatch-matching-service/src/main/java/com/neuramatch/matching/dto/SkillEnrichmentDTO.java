package com.neuramatch.matching.dto;

import lombok.*;

import java.util.List;

/**
 * DTO containing enriched skill information from the knowledge graph
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillEnrichmentDTO {

    private String skillName;
    private String canonicalName; // Resolved from synonyms
    private String displayName;
    private String category;
    private Double popularity;
    private Double trendScore;
    private Integer avgSalaryImpact;
    private String difficultyLevel;

    // Relationships
    private List<RelatedSkillDTO> prerequisites;
    private List<RelatedSkillDTO> complementarySkills;
    private List<RelatedSkillDTO> alternatives;
    private List<String> synonyms;
    private List<String> partOfEcosystem;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RelatedSkillDTO {
        private String skillName;
        private String displayName;
        private Double relationshipStrength;
        private String relationshipType;
    }
}
