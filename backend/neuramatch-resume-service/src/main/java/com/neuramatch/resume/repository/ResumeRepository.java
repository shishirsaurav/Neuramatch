package com.neuramatch.resume.repository;

import com.neuramatch.resume.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // Find by email
    Optional<Resume> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find by status
    List<Resume> findByStatus(Resume.ResumeStatus status);

    Page<Resume> findByStatus(Resume.ResumeStatus status, Pageable pageable);

    // Find by quality score
    @Query("SELECT r FROM Resume r WHERE r.qualityScore >= :minScore")
    List<Resume> findByMinimumQualityScore(@Param("minScore") Integer minScore);

    Page<Resume> findByQualityScoreGreaterThanEqual(Integer minScore, Pageable pageable);

    // Find by name pattern
    List<Resume> findByFullNameContainingIgnoreCase(String name);

    // Find by location
    List<Resume> findByLocationContainingIgnoreCase(String location);

    // Find recent resumes
    @Query("SELECT r FROM Resume r WHERE r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<Resume> findRecentResumes(@Param("since") LocalDateTime since);

    // Find active resumes
    @Query("SELECT r FROM Resume r WHERE r.status = 'ACTIVE' ORDER BY r.updatedAt DESC")
    Page<Resume> findActiveResumes(Pageable pageable);

    // Count by status
    long countByStatus(Resume.ResumeStatus status);

    // Find with skills
    @Query("SELECT DISTINCT r FROM Resume r LEFT JOIN FETCH r.skills WHERE r.id = :id")
    Optional<Resume> findByIdWithSkills(@Param("id") Long id);

    // Find with all relationships
    @Query("SELECT DISTINCT r FROM Resume r " +
           "LEFT JOIN FETCH r.skills " +
           "LEFT JOIN FETCH r.experiences " +
           "LEFT JOIN FETCH r.educations " +
           "WHERE r.id = :id")
    Optional<Resume> findByIdWithAllRelations(@Param("id") Long id);

    // Search by email or name
    @Query("SELECT r FROM Resume r WHERE " +
           "LOWER(r.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Resume> searchByEmailOrName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find by date range
    @Query("SELECT r FROM Resume r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<Resume> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}
