package com.neuramatch.matching.vector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for vector similarity search on resumes
 */
@Repository
public interface ResumeVectorRepository extends JpaRepository<ResumeVector, Long> {

    /**
     * Find resume vector by resume ID
     */
    Optional<ResumeVector> findByResumeId(Long resumeId);

    /**
     * Find similar resumes using cosine distance
     * Lower distance = more similar (0 = identical, 2 = opposite)
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.resume_id != :excludeResumeId
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumes(
        @Param("embedding") String embedding,
        @Param("excludeResumeId") Long excludeResumeId,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with minimum years of experience filter
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.years_of_experience >= :minYears
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesWithExperience(
        @Param("embedding") String embedding,
        @Param("minYears") int minYears,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with location filter
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.location = :location
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesByLocation(
        @Param("embedding") String embedding,
        @Param("location") String location,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with salary range filter
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.min_salary <= :maxSalary
        AND rv.max_salary >= :minSalary
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesBySalary(
        @Param("embedding") String embedding,
        @Param("minSalary") int minSalary,
        @Param("maxSalary") int maxSalary,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with remote preference filter
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.remote_preference = :remoteType
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesByRemoteType(
        @Param("embedding") String embedding,
        @Param("remoteType") String remoteType,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with skill overlap filter
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND rv.top_skills && CAST(:skills AS text[])
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesWithSkills(
        @Param("embedding") String embedding,
        @Param("skills") String skills,
        @Param("limit") int limit
    );

    /**
     * Find similar resumes with multiple filters
     */
    @Query(value = """
        SELECT rv.*,
               (rv.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM resume_vectors rv
        WHERE rv.is_active = true
        AND (:minYears IS NULL OR rv.years_of_experience >= :minYears)
        AND (:maxYears IS NULL OR rv.years_of_experience <= :maxYears)
        AND (:location IS NULL OR rv.location = :location)
        AND (:remoteType IS NULL OR rv.remote_preference = :remoteType)
        AND (:minQuality IS NULL OR rv.quality_score >= :minQuality)
        ORDER BY rv.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ResumeVector> findSimilarResumesWithFilters(
        @Param("embedding") String embedding,
        @Param("minYears") Integer minYears,
        @Param("maxYears") Integer maxYears,
        @Param("location") String location,
        @Param("remoteType") String remoteType,
        @Param("minQuality") Integer minQuality,
        @Param("limit") int limit
    );

    /**
     * Count active resume vectors
     */
    long countByIsActiveTrue();

    /**
     * Find all active resume vectors
     */
    List<ResumeVector> findByIsActiveTrue();

    /**
     * Delete by resume ID
     */
    void deleteByResumeId(Long resumeId);
}
