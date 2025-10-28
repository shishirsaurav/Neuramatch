package com.neuramatch.job.repository;

import com.neuramatch.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    // Find by status
    List<Job> findByStatus(Job.JobStatus status);

    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

    // Find active jobs
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(j.applicationDeadline IS NULL OR j.applicationDeadline > CURRENT_TIMESTAMP)")
    Page<Job> findActiveJobs(Pageable pageable);

    // Find by company
    List<Job> findByCompanyId(Long companyId);

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    // Find by job title
    List<Job> findByJobTitleContainingIgnoreCase(String jobTitle);

    // Find by location
    List<Job> findByLocationContainingIgnoreCase(String location);

    // Find remote jobs
    List<Job> findByIsRemoteTrue();

    Page<Job> findByIsRemoteTrue(Pageable pageable);

    // Find by work mode
    List<Job> findByWorkMode(Job.WorkMode workMode);

    // Find by job type
    List<Job> findByJobType(Job.JobType jobType);

    Page<Job> findByJobType(Job.JobType jobType, Pageable pageable);

    // Find by experience level
    List<Job> findByExperienceLevel(Job.ExperienceLevel experienceLevel);

    // Find by salary range
    @Query("SELECT j FROM Job j WHERE j.minSalary >= :minSalary AND j.maxSalary <= :maxSalary")
    List<Job> findBySalaryRange(@Param("minSalary") BigDecimal minSalary,
                                 @Param("maxSalary") BigDecimal maxSalary);

    // Find by minimum quality score
    @Query("SELECT j FROM Job j WHERE j.qualityScore >= :minScore")
    List<Job> findByMinimumQualityScore(@Param("minScore") Integer minScore);

    // Find by industry
    List<Job> findByIndustryIgnoreCase(String industry);

    // Find recent jobs
    @Query("SELECT j FROM Job j WHERE j.publishedAt >= :since ORDER BY j.publishedAt DESC")
    List<Job> findRecentJobs(@Param("since") LocalDateTime since);

    // Find with required skills
    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN FETCH j.jobSkills WHERE j.id = :id")
    Optional<Job> findByIdWithRequiredSkills(@Param("id") Long id);

    // Find with all relationships
    @Query("SELECT DISTINCT j FROM Job j " +
           "LEFT JOIN FETCH j.jobSkills " +
           "LEFT JOIN FETCH j.company " +
           "WHERE j.id = :id")
    Optional<Job> findByIdWithAllRelations(@Param("id") Long id);

    // Search jobs
    @Query("SELECT j FROM Job j WHERE " +
           "LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(j.jobDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Job> searchJobs(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count by status
    long countByStatus(Job.JobStatus status);

    // Count by company
    long countByCompanyId(Long companyId);

    // Find expiring soon
    @Query("SELECT j FROM Job j WHERE j.applicationDeadline BETWEEN CURRENT_TIMESTAMP AND :deadline " +
           "AND j.status = 'ACTIVE'")
    List<Job> findExpiringSoon(@Param("deadline") LocalDateTime deadline);

    // Find by company and status
    List<Job> findByCompanyIdAndStatus(Long companyId, Job.JobStatus status);
}
