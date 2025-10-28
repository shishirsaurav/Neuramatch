package com.neuramatch.resume.repository;

import com.neuramatch.resume.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Find by resume
    List<Skill> findByResumeId(Long resumeId);

    // Find by skill name
    List<Skill> findBySkillNameIgnoreCase(String skillName);

    // Find by category
    List<Skill> findByCategory(Skill.SkillCategory category);

    Page<Skill> findByCategory(Skill.SkillCategory category, Pageable pageable);

    // Find by proficiency level
    List<Skill> findByProficiencyLevel(Skill.ProficiencyLevel level);

    // Find verified skills
    List<Skill> findByIsVerifiedTrue();

    // Find by resume and category
    List<Skill> findByResumeIdAndCategory(Long resumeId, Skill.SkillCategory category);

    // Find by minimum years of experience
    @Query("SELECT s FROM Skill s WHERE s.yearsOfExperience >= :minYears")
    List<Skill> findByMinimumExperience(@Param("minYears") Integer minYears);

    // Find by skill name pattern
    @Query("SELECT s FROM Skill s WHERE LOWER(s.skillName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<Skill> searchBySkillName(@Param("pattern") String pattern);

    // Count skills by category for a resume
    @Query("SELECT s.category, COUNT(s) FROM Skill s WHERE s.resume.id = :resumeId GROUP BY s.category")
    List<Object[]> countSkillsByCategory(@Param("resumeId") Long resumeId);

    // Find most common skills across all resumes
    @Query("SELECT s.skillName, COUNT(s) as cnt FROM Skill s GROUP BY s.skillName ORDER BY cnt DESC")
    List<Object[]> findMostCommonSkills(Pageable pageable);

    // Find skills with high confidence
    @Query("SELECT s FROM Skill s WHERE s.confidenceScore >= :minConfidence")
    List<Skill> findByMinimumConfidence(@Param("minConfidence") Double minConfidence);
}
