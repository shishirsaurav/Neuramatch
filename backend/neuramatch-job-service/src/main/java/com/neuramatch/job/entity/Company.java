package com.neuramatch.job.entity;

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
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String companyName;

    @Column(length = 2000)
    private String description;

    private String industry;

    @Enumerated(EnumType.STRING)
    private CompanySize companySize;

    private String website;

    private String logoUrl;

    // Location
    private String headquarters;

    private String country;

    // Social media
    private String linkedinUrl;

    private String twitterUrl;

    private String facebookUrl;

    // Contact
    private String contactEmail;

    private String contactPhone;

    // Founded
    private Integer foundedYear;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    private Boolean isVerified = false;

    // Timestamps
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Job> jobs = new ArrayList<>();

    // Helper methods
    public void addJob(Job job) {
        jobs.add(job);
        job.setCompany(this);
    }

    public void removeJob(Job job) {
        jobs.remove(job);
        job.setCompany(null);
    }

    public int getActiveJobsCount() {
        return (int) jobs.stream()
                .filter(Job::isActive)
                .count();
    }

    // Enums
    public enum CompanySize {
        STARTUP("1-10"),
        SMALL("11-50"),
        MEDIUM("51-200"),
        LARGE("201-1000"),
        ENTERPRISE("1000+");

        private final String range;

        CompanySize(String range) {
            this.range = range;
        }

        public String getRange() {
            return range;
        }
    }

    public enum CompanyStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}
