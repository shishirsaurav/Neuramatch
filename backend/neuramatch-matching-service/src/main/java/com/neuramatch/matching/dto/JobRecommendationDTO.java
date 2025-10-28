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
public class JobRecommendationDTO {
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String location;
    private Double matchScore;
    private String reason; // CURRENT_SKILLS, GROWTH_OPPORTUNITY, TRENDING, etc.
    private Double skillMatch;
    private Double experienceMatch;
    private Double score; // Overall recommendation score
    private List<String> missingSkills;
    private String growthPotential; // HIGH, MEDIUM, LOW
}
