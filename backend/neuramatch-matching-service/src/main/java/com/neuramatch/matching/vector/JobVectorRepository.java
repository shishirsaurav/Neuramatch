package com.neuramatch.matching.vector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for vector similarity search on jobs
 */
@Repository
public interface JobVectorRepository extends JpaRepository<JobVector, Long> {

    /**
     * Find job vector by job ID
     */
    Optional<JobVector> findByJobId(Long jobId);

    /**
     * Find similar jobs using cosine distance
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND jv.job_id != :excludeJobId
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findSimilarJobs(
        @Param("embedding") String embedding,
        @Param("excludeJobId") Long excludeJobId,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs for a resume embedding
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobs(
        @Param("embedding") String embedding,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs with experience filter
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND (:yearsExp >= jv.min_years_experience OR jv.min_years_experience IS NULL)
        AND (:yearsExp <= jv.max_years_experience OR jv.max_years_experience IS NULL)
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobsByExperience(
        @Param("embedding") String embedding,
        @Param("yearsExp") int yearsExperience,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs with location filter
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND jv.location = :location
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobsByLocation(
        @Param("embedding") String embedding,
        @Param("location") String location,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs with salary filter
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND (:minSalary IS NULL OR jv.max_salary >= :minSalary)
        AND (:maxSalary IS NULL OR jv.min_salary <= :maxSalary)
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobsBySalary(
        @Param("embedding") String embedding,
        @Param("minSalary") Integer minSalary,
        @Param("maxSalary") Integer maxSalary,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs with remote type filter
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND jv.remote_type = :remoteType
        ORDER BY jv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobsByRemoteType(
        @Param("embedding") String embedding,
        @Param("remoteType") String remoteType,
        @Param("limit") int limit
    );

    /**
     * Find matching jobs with multiple filters and weighted ranking
     */
    @Query(value = """
        SELECT jv.*,
               (jv.embedding <=> CAST(:embedding AS vector)) AS distance,
               COALESCE(jv.priority_score, 50) AS priority
        FROM job_vectors jv
        WHERE jv.is_active = true
        AND (jv.expires_at IS NULL OR jv.expires_at > CURRENT_TIMESTAMP)
        AND (:minYears IS NULL OR :minYears >= jv.min_years_experience OR jv.min_years_experience IS NULL)
        AND (:maxYears IS NULL OR :maxYears <= jv.max_years_experience OR jv.max_years_experience IS NULL)
        AND (:location IS NULL OR jv.location = :location)
        AND (:remoteType IS NULL OR jv.remote_type = :remoteType)
        AND (:employmentType IS NULL OR jv.employment_type = :employmentType)
        ORDER BY (jv.embedding <=> CAST(:embedding AS vector)) * (1.0 / GREATEST(COALESCE(jv.priority_score, 50), 1))
        LIMIT :limit
        """, nativeQuery = true)
    List<JobVector> findMatchingJobsWithFilters(
        @Param("embedding") String embedding,
        @Param("minYears") Integer minYears,
        @Param("maxYears") Integer maxYears,
        @Param("location") String location,
        @Param("remoteType") String remoteType,
        @Param("employmentType") String employmentType,
        @Param("limit") int limit
    );

    /**
     * Count active and non-expired job vectors
     */
    @Query(value = """
        SELECT COUNT(*)
        FROM job_vectors
        WHERE is_active = true
        AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
        """, nativeQuery = true)
    long countActiveJobs();

    /**
     * Find expired jobs
     */
    @Query(value = """
        SELECT jv FROM JobVector jv
        WHERE jv.isActive = true
        AND jv.expiresAt IS NOT NULL
        AND jv.expiresAt < :now
        """)
    List<JobVector> findExpiredJobs(@Param("now") LocalDateTime now);

    /**
     * Delete by job ID
     */
    void deleteByJobId(Long jobId);
}
