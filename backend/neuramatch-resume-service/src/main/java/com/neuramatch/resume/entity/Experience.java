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
@Table(name = "experiences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String companyName;

    private String location;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate; // null if current

    private Boolean isCurrentRole = false;

    @Column(length = 3000)
    private String description;

    @Column(length = 2000)
    private String achievements; // Bullet points of achievements

    // Extracted contextual information
    private Integer teamSize; // "Led team of 10"

    private String leadershipRole; // "Tech Lead", "Manager", "Senior"

    @Column(length = 500)
    private String impactMetrics; // "Reduced latency by 40%"

    @Column(length = 500)
    private String scaleIndicators; // "Processed 1M requests/day"

    @Column(length = 500)
    private String domainExpertise; // "HIPAA compliance", "Financial systems"

    @Column(length = 1000)
    private String technologiesUsed; // Comma-separated

    // Calculated fields
    private Integer durationInMonths;

    private Double relevanceScore; // 0.0 to 1.0

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // Helper method to calculate duration
    @PrePersist
    @PreUpdate
    public void calculateDuration() {
        if (startDate != null) {
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            this.durationInMonths = (int) java.time.temporal.ChronoUnit.MONTHS.between(startDate, end);
        }
    }

    public boolean isRecent() {
        if (endDate == null) return true; // Current role
        return endDate.isAfter(LocalDate.now().minusYears(2));
    }

    public int getTotalYears() {
        return durationInMonths != null ? durationInMonths / 12 : 0;
    }
}
