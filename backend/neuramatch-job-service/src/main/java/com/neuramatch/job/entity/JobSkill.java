package com.neuramatch.job.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel requiredLevel;

    private Integer minYearsExperience;

    private Boolean isRequired = true; // Required vs Preferred

    private Integer importance; // 1-10, how critical is this skill

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Enums
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
    }
}
