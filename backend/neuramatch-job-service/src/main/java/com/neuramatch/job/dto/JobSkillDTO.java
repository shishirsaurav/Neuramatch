package com.neuramatch.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkillDTO {
    private Long id;
    private String skillName;
    private String category;
    private String requiredLevel; // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    private Integer minYearsExperience;
    private Boolean isRequired;
    private Integer importance; // 1-10
}
