package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchExplanationDTO {
    private Long jobId;
    private Long resumeId;
    private String candidateName;
    private String jobTitle;
    private String companyName;

    // Overall scoring
    private Double overallScore; // 0-100
    private String matchLevel; // EXCELLENT, GOOD, FAIR, POOR
    private String confidence; // HIGH, MEDIUM, LOW

    // Detailed breakdown
    private Map<String, Double> scoreBreakdown;

    // Key strengths (top reasons for match)
    private List<String> keyStrengths;

    // Areas of concern
    private List<String> concerns;

    // Skill analysis
    private SkillGapAnalysisDTO skillAnalysis;

    // Experience analysis
    private ExperienceAnalysisDTO experienceAnalysis;

    // Recommendations
    private List<String> recommendations;

    // Score components (detailed)
    private Double technicalSkillScore;
    private Double experienceScore;
    private Double educationScore;
    private Double domainExpertiseScore;
    private Double culturalFitScore;
    private Double recencyScore;
}
