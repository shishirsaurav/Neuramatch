package com.neuramatch.resume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiencyLevel;

    // Temporal information
    private LocalDate lastUsedDate;

    private Integer yearsOfExperience;

    private Integer monthsOfExperience;

    // Confidence and validation
    private Double confidenceScore; // 0.0 to 1.0

    private Boolean isVerified = false;

    @Column(length = 500)
    private String context; // Where/how this skill was mentioned

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    public enum SkillCategory {
        PROGRAMMING_LANGUAGE,
        FRAMEWORK,
        DATABASE,
        CLOUD_PLATFORM,
        DEVOPS_TOOL,
        SOFT_SKILL,
        DOMAIN_KNOWLEDGE,
        TOOL,
        METHODOLOGY,
        CERTIFICATION,
        OTHER
    }

    public enum ProficiencyLevel {
        BEGINNER(1),
        INTERMEDIATE(2),
        ADVANCED(3),
        EXPERT(4);

        private final int level;

        ProficiencyLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public static ProficiencyLevel fromYearsOfExperience(Integer years) {
            if (years == null || years < 1) return BEGINNER;
            if (years < 3) return INTERMEDIATE;
            if (years < 5) return ADVANCED;
            return EXPERT;
        }
    }
}
