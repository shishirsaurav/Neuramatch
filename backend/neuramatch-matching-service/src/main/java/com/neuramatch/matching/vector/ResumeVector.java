package com.neuramatch.matching.vector;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing resume embeddings with pgvector
 */
@Entity
@Table(name = "resume_vectors", indexes = {
    @Index(name = "idx_resume_vector_resume_id", columnList = "resume_id"),
    @Index(name = "idx_resume_vector_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false, unique = true)
    private Long resumeId;

    @Column(name = "embedding", columnDefinition = "vector(768)", nullable = false)
    private PGvector embedding;

    // Metadata for filtering
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "location")
    private String location;

    @Column(name = "min_salary")
    private Integer minSalary;

    @Column(name = "max_salary")
    private Integer maxSalary;

    @Column(name = "remote_preference")
    private String remotePreference; // REMOTE, HYBRID, ONSITE

    @Column(name = "top_skills", columnDefinition = "text[]")
    private String[] topSkills;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
}
