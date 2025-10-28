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
public class ExperienceAnalysisDTO {
    // Experience requirements
    private Integer requiredMinYears;
    private Integer requiredMaxYears;
    private Integer candidateYears;

    // Analysis
    private String experienceLevel; // UNDER_QUALIFIED, PERFECTLY_MATCHED, OVER_QUALIFIED
    private Double experienceScore; // 0-100
    private String explanation;

    // Relevant experience
    private List<String> relevantRoles;
    private Boolean hasLeadershipExperience;
    private Boolean hasScaleExperience;
    private Boolean hasRecentExperience;

    // Domain expertise
    private List<String> relevantDomains;
    private Double domainMatchScore;

    // Career trajectory
    private String careerGrowth; // ASCENDING, STABLE, TRANSITIONING
    private Boolean isGoodFit;
}
