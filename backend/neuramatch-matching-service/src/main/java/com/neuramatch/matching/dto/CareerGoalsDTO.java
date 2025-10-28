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
public class CareerGoalsDTO {
    private List<String> desiredRoles;
    private List<String> desiredIndustries;
    private String careerStage; // ENTRY, MID_LEVEL, SENIOR, EXECUTIVE
    private String preferredCompanySize; // STARTUP, SMALL, MEDIUM, LARGE, ENTERPRISE
    private String remotePreference; // REMOTE, HYBRID, ONSITE, FLEXIBLE
    private List<String> prioritiesRanked; // growth, compensation, work_life_balance, impact, etc.
    private List<String> willingToLearn; // Skills candidate wants to learn
    private List<String> avoidSkills; // Technologies to avoid
    private Integer targetSalaryMin;
    private Integer targetSalaryMax;
}
