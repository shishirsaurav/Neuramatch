package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTrendDTO {
    private String skillName;
    private String category;
    private Integer currentDemand;
    private Integer previousDemand;
    private Double growthRate; // Percentage
    private Integer averageSalary;
    private String trendDirection; // UP, DOWN, STABLE
}
