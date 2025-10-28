package com.neuramatch.resume.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(length = 1000)
    private String summary;

    private String location;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    @Column(length = 500)
    private String careerObjective;

    // File metadata
    private String originalFileName;

    private String fileStoragePath;

    @Column(length = 50)
    private String fileType; // PDF, DOCX, etc.

    // Quality metrics
    private Integer qualityScore; // 0-100

    @Column(length = 2000)
    private String qualitySuggestions;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeStatus status = ResumeStatus.ACTIVE;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastModifiedDate; // From resume content

    // Relationships
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    // Helper methods
    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setResume(this);
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
        skill.setResume(null);
    }

    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setResume(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setResume(null);
    }

    public void addEducation(Education education) {
        educations.add(education);
        education.setResume(this);
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        education.setResume(null);
    }

    public enum ResumeStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED,
        DRAFT
    }
}
