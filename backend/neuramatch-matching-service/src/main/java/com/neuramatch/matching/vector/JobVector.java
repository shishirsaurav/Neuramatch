package com.neuramatch.matching.vector;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing job embeddings with pgvector
 */
@Entity
@Table(name = "job_vectors", indexes = {
    @Index(name = "idx_job_vector_job_id", columnList = "job_id"),
    @Index(name = "idx_job_vector_created", columnList = "created_at"),
    @Index(name = "idx_job_vector_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "embedding", columnDefinition = "vector(768)", nullable = false)
    private PGvector embedding;

    // Metadata for filtering
    @Column(name = "title")
    private String title;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "location")
    private String location;

    @Column(name = "min_years_experience")
    private Integer minYearsExperience;

    @Column(name = "max_years_experience")
    private Integer maxYearsExperience;

    @Column(name = "min_salary")
    private Integer minSalary;

    @Column(name = "max_salary")
    private Integer maxSalary;

    @Column(name = "employment_type")
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT

    @Column(name = "remote_type")
    private String remoteType; // REMOTE, HYBRID, ONSITE

    @Column(name = "required_skills", columnDefinition = "text[]")
    private String[] requiredSkills;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority_score")
    private Integer priorityScore; // For ranking popular jobs higher

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Helper method to set embedding from List<Double>
     */
    public void setEmbeddingFromList(java.util.List<Double> embeddingList) {
        float[] floatArray = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            floatArray[i] = embeddingList.get(i).floatValue();
        }
        this.embedding = new PGvector(floatArray);
    }

    /**
     * Helper method to get embedding as List<Double>
     */
    public java.util.List<Double> getEmbeddingAsList() {
        if (embedding == null) {
            return java.util.Collections.emptyList();
        }
        float[] floatArray = embedding.toArray();
        java.util.List<Double> result = new java.util.ArrayList<>(floatArray.length);
        for (float f : floatArray) {
            result.add((double) f);
        }
        return result;
    }

    /**
     * Check if job is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
