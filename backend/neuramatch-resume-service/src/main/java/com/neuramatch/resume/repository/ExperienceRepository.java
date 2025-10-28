package com.neuramatch.resume.repository;

import com.neuramatch.resume.entity.Experience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // Find by resume
    List<Experience> findByResumeId(Long resumeId);

    List<Experience> findByResumeIdOrderByStartDateDesc(Long resumeId);

    // Find by company
    List<Experience> findByCompanyNameIgnoreCase(String companyName);

    // Find current roles
    @Query("SELECT e FROM Experience e WHERE e.isCurrentRole = true")
    List<Experience> findCurrentRoles();

    List<Experience> findByResumeIdAndIsCurrentRoleTrue(Long resumeId);

    // Find by job title
    List<Experience> findByJobTitleContainingIgnoreCase(String jobTitle);

    // Find by location
    List<Experience> findByLocationContainingIgnoreCase(String location);

    // Find by minimum duration
    @Query("SELECT e FROM Experience e WHERE e.durationInMonths >= :minMonths")
    List<Experience> findByMinimumDuration(@Param("minMonths") Integer minMonths);

    // Find recent experiences
    @Query("SELECT e FROM Experience e WHERE e.endDate >= :since OR e.isCurrentRole = true")
    List<Experience> findRecentExperiences(@Param("since") LocalDate since);

    // Find by date range
    @Query("SELECT e FROM Experience e WHERE e.startDate <= :endDate AND (e.endDate >= :startDate OR e.endDate IS NULL)")
    List<Experience> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Find with leadership experience
    @Query("SELECT e FROM Experience e WHERE e.leadershipRole IS NOT NULL OR e.teamSize IS NOT NULL")
    List<Experience> findWithLeadershipExperience();

    // Find by relevance score
    @Query("SELECT e FROM Experience e WHERE e.relevanceScore >= :minScore ORDER BY e.relevanceScore DESC")
    Page<Experience> findByMinimumRelevance(@Param("minScore") Double minScore, Pageable pageable);

    // Count total years of experience for a resume
    @Query("SELECT COALESCE(SUM(e.durationInMonths), 0) / 12 FROM Experience e WHERE e.resume.id = :resumeId")
    Integer calculateTotalYearsOfExperience(@Param("resumeId") Long resumeId);

    // Find experiences with specific technologies
    @Query("SELECT e FROM Experience e WHERE LOWER(e.technologiesUsed) LIKE LOWER(CONCAT('%', :technology, '%'))")
    List<Experience> findByTechnology(@Param("technology") String technology);
}
