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
public class ExperienceDTO {

    private Long id;
    private String jobTitle;
    private String companyName;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrentRole;
    private String description;
    private String achievements;
    private Integer teamSize;
    private String leadershipRole;
    private String impactMetrics;
    private Integer durationInMonths;
}
