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
@Table(name = "educations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String degree; // Bachelor's, Master's, PhD, etc.

    private String fieldOfStudy; // Computer Science, Engineering, etc.

    private LocalDate startDate;

    private LocalDate endDate;

    private String location;

    private Double gpa; // Grade Point Average

    private String maxGpa; // "4.0", "10.0", etc.

    @Column(length = 1000)
    private String achievements; // Awards, honors, dean's list

    @Column(length = 1000)
    private String relevantCoursework;

    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    private Boolean isVerified = false;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    public enum EducationLevel {
        HIGH_SCHOOL,
        ASSOCIATE,
        BACHELORS,
        MASTERS,
        PHD,
        CERTIFICATE,
        BOOTCAMP,
        OTHER
    }

    public int getYearOfGraduation() {
        return endDate != null ? endDate.getYear() : 0;
    }
}
