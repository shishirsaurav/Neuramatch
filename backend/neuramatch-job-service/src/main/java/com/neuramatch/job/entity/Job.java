package com.neuramatch.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobTitle;

    @Column(length = 5000, nullable = false)
    private String jobDescription;

    @Column(length = 2000)
    private String responsibilities;

    @Column(length = 2000)
    private String qualifications;

    // Location
    private String location;

    private Boolean isRemote = false;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode = WorkMode.HYBRID;

    // Experience requirements
    private Integer minYearsExperience;

    private Integer maxYearsExperience;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    // Salary
    private BigDecimal minSalary;

    private BigDecimal maxSalary;

    @Column(length = 10)
    private String salaryCurrency = "USD";

    @Enumerated(EnumType.STRING)
    private SalaryPeriod salaryPeriod = SalaryPeriod.ANNUAL;

    private Boolean isSalaryPublic = false;

    // Job details
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.DRAFT;

    private String industry;

    private String department;

    // Quality metrics
    private Integer qualityScore; // 0-100

    @Column(length = 2000)
    private String qualitySuggestions;

    private Integer biasScore; // 0-100

    // Application details
    private String applicationUrl;

    private String applicationEmail;

    private LocalDate applicationDeadline;

    private LocalDate expiresAt;

    private Integer numberOfPositions = 1;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private LocalDateTime closedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobSkill> jobSkills = new ArrayList<>();

    // Helper methods
    public void addJobSkill(JobSkill skill) {
        jobSkills.add(skill);
        skill.setJob(this);
    }

    public void addRequiredSkill(JobSkill skill) {
        skill.setIsRequired(true);
        addJobSkill(skill);
    }

    public void addPreferredSkill(JobSkill skill) {
        skill.setIsRequired(false);
        addJobSkill(skill);
    }

    public boolean isActive() {
        return status == JobStatus.ACTIVE;
    }

    public boolean isExpired() {
        if (expiresAt != null && expiresAt.isBefore(LocalDate.now())) {
            return true;
        }
        return applicationDeadline != null && applicationDeadline.isBefore(LocalDate.now());
    }

    // Enums
    public enum JobType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        INTERNSHIP,
        TEMPORARY,
        FREELANCE
    }

    public enum JobStatus {
        DRAFT,
        ACTIVE,
        CLOSED,
        FILLED,
        CANCELLED,
        EXPIRED
    }

    public enum WorkMode {
        ONSITE,
        REMOTE,
        HYBRID
    }

    public enum ExperienceLevel {
        ENTRY,
        JUNIOR,
        MID,
        SENIOR,
        LEAD,
        PRINCIPAL,
        EXECUTIVE
    }

    public enum SalaryPeriod {
        HOURLY,
        MONTHLY,
        ANNUAL
    }
}
