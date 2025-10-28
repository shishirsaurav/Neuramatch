package com.neuramatch.resume.repository;

import com.neuramatch.resume.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    // Find by resume
    List<Education> findByResumeId(Long resumeId);

    List<Education> findByResumeIdOrderByEndDateDesc(Long resumeId);

    // Find by institution
    List<Education> findByInstitutionNameIgnoreCase(String institutionName);

    // Find by degree
    List<Education> findByDegreeContainingIgnoreCase(String degree);

    // Find by field of study
    List<Education> findByFieldOfStudyContainingIgnoreCase(String fieldOfStudy);

    // Find by education level
    List<Education> findByEducationLevel(Education.EducationLevel level);

    // Find verified education
    List<Education> findByIsVerifiedTrue();

    // Find by minimum GPA
    @Query("SELECT e FROM Education e WHERE e.gpa >= :minGpa")
    List<Education> findByMinimumGpa(@Param("minGpa") Double minGpa);

    // Find by graduation year range
    @Query("SELECT e FROM Education e WHERE YEAR(e.endDate) BETWEEN :startYear AND :endYear")
    List<Education> findByGraduationYearRange(@Param("startYear") int startYear,
                                               @Param("endYear") int endYear);

    // Find by location
    List<Education> findByLocationContainingIgnoreCase(String location);

    // Find highest education level for a resume
    @Query("SELECT e FROM Education e WHERE e.resume.id = :resumeId ORDER BY " +
           "CASE e.educationLevel " +
           "WHEN 'PHD' THEN 1 " +
           "WHEN 'MASTERS' THEN 2 " +
           "WHEN 'BACHELORS' THEN 3 " +
           "WHEN 'ASSOCIATE' THEN 4 " +
           "ELSE 5 END")
    List<Education> findHighestEducationForResume(@Param("resumeId") Long resumeId);
}
