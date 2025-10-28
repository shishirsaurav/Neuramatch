package com.neuramatch.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 5, max = 200, message = "Job title must be between 5 and 200 characters")
    private String jobTitle;

    @NotBlank(message = "Job description is required")
    @Size(min = 50, message = "Job description must be at least 50 characters")
    private String jobDescription;

    private String responsibilities;
    private String qualifications;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Location is required")
    private String location;

    private Boolean isRemote;
    private String workMode; // ONSITE, REMOTE, HYBRID

    @Min(value = 0, message = "Minimum years of experience cannot be negative")
    private Integer minYearsExperience;

    @Min(value = 0, message = "Maximum years of experience cannot be negative")
    private Integer maxYearsExperience;

    private String experienceLevel;

    @DecimalMin(value = "0.0", message = "Minimum salary must be positive")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", message = "Maximum salary must be positive")
    private BigDecimal maxSalary;

    private String salaryCurrency;
    private String salaryPeriod;

    @NotBlank(message = "Job type is required")
    private String jobType;

    private String industry;
    private String applicationUrl;
    private LocalDate applicationDeadline;

    @NotEmpty(message = "At least one required skill must be specified")
    private List<JobSkillDTO> requiredSkills;

    private List<JobSkillDTO> preferredSkills;

    private LocalDate expiresAt;
}
