package com.neuramatch.job.repository;

import com.neuramatch.job.entity.JobSkill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    // Find by job
    List<JobSkill> findByJobId(Long jobId);

    // Find required skills for a job
    List<JobSkill> findByJobIdAndIsRequiredTrue(Long jobId);

    // Find preferred skills for a job
    List<JobSkill> findByJobIdAndIsRequiredFalse(Long jobId);

    // Find by skill name
    List<JobSkill> findBySkillNameIgnoreCase(String skillName);

    // Find by category
    List<JobSkill> findByCategory(JobSkill.SkillCategory category);

    // Find by proficiency level
    List<JobSkill> findByRequiredLevel(JobSkill.ProficiencyLevel level);

    // Find most demanded skills
    @Query("SELECT js.skillName, COUNT(js) as cnt FROM JobSkill js " +
           "WHERE js.isRequired = true " +
           "GROUP BY js.skillName ORDER BY cnt DESC")
    List<Object[]> findMostDemandedSkills(Pageable pageable);

    // Find skills by importance
    @Query("SELECT js FROM JobSkill js WHERE js.importance >= :minImportance ORDER BY js.importance DESC")
    List<JobSkill> findByMinimumImportance(@Param("minImportance") Integer minImportance);

    // Count skills by category for a job
    @Query("SELECT js.category, COUNT(js) FROM JobSkill js WHERE js.job.id = :jobId GROUP BY js.category")
    List<Object[]> countSkillsByCategoryForJob(@Param("jobId") Long jobId);

    // Find trending skills (most mentioned in jobs - top demanded skills)
    @Query("SELECT js.skillName, COUNT(js) as cnt FROM JobSkill js " +
           "WHERE js.job.status = 'ACTIVE' " +
           "GROUP BY js.skillName ORDER BY cnt DESC")
    List<Object[]> findTrendingSkills(Pageable pageable);
}
