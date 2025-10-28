package com.neuramatch.matching.dto;

import lombok.*;

import java.util.List;

/**
 * DTO for skill recommendations and suggestions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRecommendationDTO {

    private String skillName;
    private String displayName;
    private String category;
    private Double popularity;
    private Double trendScore;
    private Integer avgSalaryImpact;
    private String difficultyLevel;
    private String description;

    private RecommendationType recommendationType;
    private Double recommendationScore; // 0.0 to 1.0
    private String reason;

    private List<String> prerequisites;
    private List<String> complements;
    private List<String> alternatives;

    // Additional fields for RecommendationService compatibility
    private String difficulty;        // EASY, MEDIUM, HARD
    private String learningTime;      // "2-3 weeks", "1-2 months", etc.
    private String salaryImpact;      // "+$5,000", "+$15,000", etc.
    private String demandTrend;       // HIGH, VERY_HIGH, MEDIUM, LOW
    private Integer priority;         // 0-100 priority score for sorting
    private List<String> relatedSkills; // Related/complementary skills

    public enum RecommendationType {
        PREREQUISITE,      // Missing prerequisite
        COMPLEMENTARY,     // Works well with existing skills
        TRENDING,          // High trend score
        HIGH_SALARY_IMPACT, // High salary impact
        SKILL_GAP,         // Fills a gap in skillset
        CAREER_GROWTH      // Recommended for career advancement
    }
}
