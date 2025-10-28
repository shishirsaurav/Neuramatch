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
public class SkillGapAnalysisDTO {
    // Required skills missing
    private List<SkillGapDTO> criticalGaps;

    // Preferred skills missing
    private List<SkillGapDTO> niceToHaveGaps;

    // Skills with transferable alternatives
    private List<SkillGapDTO> transferableSkills;

    // Perfectly matched skills
    private List<String> perfectMatches;

    // Over-qualified skills
    private List<String> bonusSkills;

    // Summary metrics
    private Integer totalRequiredSkills;
    private Integer matchedRequiredSkills;
    private Double coveragePercentage;
    private Integer estimatedLearningTimeMonths;
    private Double impactOnScore; // How gaps affect overall score
}
