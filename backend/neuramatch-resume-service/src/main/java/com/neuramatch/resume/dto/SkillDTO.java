package com.neuramatch.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDTO {

    private Long id;
    private String skillName;
    private String category;
    private String proficiencyLevel;
    private LocalDate lastUsedDate;
    private Integer yearsOfExperience;
    private Double confidenceScore;
    private Boolean isVerified;
}
