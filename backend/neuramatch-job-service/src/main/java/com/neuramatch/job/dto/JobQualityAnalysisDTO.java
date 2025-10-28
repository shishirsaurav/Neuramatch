package com.neuramatch.job.dto;

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
public class JobQualityAnalysisDTO {
    private Integer qualityScore; // 0-100
    private String qualityLevel; // EXCELLENT, GOOD, FAIR, POOR
    private List<String> suggestions;

    // Detailed breakdown
    private Map<String, Integer> breakdown;

    // Metrics
    private Integer completenessScore;
    private Integer specificityScore;
    private Integer realismScore;
    private Integer clarityScore;

    // Analysis details
    private Integer wordCount;
    private Boolean hasSalaryRange;
    private Boolean hasResponsibilities;
    private Boolean hasQualifications;
    private Boolean hasApplicationProcess;
    private Integer experienceRangeSize; // Difference between min and max years
    private Boolean hasRealisticRequirements;
}
