package com.neuramatch.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long id;
    private String jobTitle;
    private String jobDescription;
    private String responsibilities;
    private String qualifications;

    // Company info
    private Long companyId;
    private String companyName;

    // Location
    private String location;
    private Boolean isRemote;
    private String workMode; // ONSITE, REMOTE, HYBRID

    // Experience
    private Integer minYearsExperience;
    private Integer maxYearsExperience;
    private String experienceLevel; // ENTRY, JUNIOR, MID, SENIOR, LEAD, PRINCIPAL

    // Salary
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private String salaryCurrency;
    private String salaryPeriod; // HOURLY, MONTHLY, YEARLY

    // Job details
    private String jobType; // FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
    private String industry;

    // Application
    private String applicationUrl;
    private LocalDate applicationDeadline;

    // Quality metrics
    private Integer qualityScore;
    private List<String> qualitySuggestions;
    private Integer biasScore;
    private List<BiasIssueDTO> biasIssues;

    // Skills
    private List<JobSkillDTO> requiredSkills;
    private List<JobSkillDTO> preferredSkills;

    // Status
    private String status; // DRAFT, ACTIVE, CLOSED, FILLED, CANCELLED, EXPIRED

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate expiresAt;
}
