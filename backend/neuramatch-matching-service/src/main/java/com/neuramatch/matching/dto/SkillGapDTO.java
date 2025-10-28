package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapDTO {
    private String skillName;
    private String category;
    private String requiredLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private Boolean isRequired;
    private Integer importance; // 1-10

    // Alternative skills candidate has
    private Boolean hasAlternative;
    private List<String> alternativeSkills;
    private Double transferability; // 0.0-1.0

    // Learning information
    private String learningPath;
    private Integer estimatedLearningTimeMonths;
    private List<String> recommendedResources;

    // Impact analysis
    private Double impactOnScore; // How much this gap affects the match score
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW
}
