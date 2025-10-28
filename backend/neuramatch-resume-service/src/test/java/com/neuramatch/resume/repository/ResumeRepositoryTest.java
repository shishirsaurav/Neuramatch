package com.neuramatch.resume.repository;

import com.neuramatch.resume.entity.Resume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ResumeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ResumeRepository resumeRepository;

    private Resume testResume;

    @BeforeEach
    void setUp() {
        testResume = Resume.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .summary("Experienced Software Engineer")
                .location("San Francisco, CA")
                .qualityScore(85)
                .status(Resume.ResumeStatus.ACTIVE)
                .build();
    }

    @Test
    void testSaveResume() {
        // When
        Resume saved = resumeRepository.save(testResume);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("John Doe");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void testFindByEmail() {
        // Given
        entityManager.persist(testResume);
        entityManager.flush();

        // When
        Optional<Resume> found = resumeRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testExistsByEmail() {
        // Given
        entityManager.persist(testResume);
        entityManager.flush();

        // When & Then
        assertThat(resumeRepository.existsByEmail("john.doe@example.com")).isTrue();
        assertThat(resumeRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void testFindByStatus() {
        // Given
        entityManager.persist(testResume);

        Resume inactiveResume = Resume.builder()
                .fullName("Jane Smith")
                .email("jane.smith@example.com")
                .status(Resume.ResumeStatus.INACTIVE)
                .build();
        entityManager.persist(inactiveResume);
        entityManager.flush();

        // When
        List<Resume> activeResumes = resumeRepository.findByStatus(Resume.ResumeStatus.ACTIVE);

        // Then
        assertThat(activeResumes).hasSize(1);
        assertThat(activeResumes.get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByMinimumQualityScore() {
        // Given
        entityManager.persist(testResume);

        Resume lowQualityResume = Resume.builder()
                .fullName("Low Quality")
                .email("low@example.com")
                .qualityScore(50)
                .status(Resume.ResumeStatus.ACTIVE)
                .build();
        entityManager.persist(lowQualityResume);
        entityManager.flush();

        // When
        List<Resume> highQualityResumes = resumeRepository.findByMinimumQualityScore(80);

        // Then
        assertThat(highQualityResumes).hasSize(1);
        assertThat(highQualityResumes.get(0).getQualityScore()).isGreaterThanOrEqualTo(80);
    }

    @Test
    void testFindByFullNameContaining() {
        // Given
        entityManager.persist(testResume);
        entityManager.flush();

        // When
        List<Resume> found = resumeRepository.findByFullNameContainingIgnoreCase("john");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFullName()).contains("John");
    }

    @Test
    void testFindActiveResumesWithPagination() {
        // Given
        for (int i = 0; i < 15; i++) {
            Resume resume = Resume.builder()
                    .fullName("User " + i)
                    .email("user" + i + "@example.com")
                    .status(Resume.ResumeStatus.ACTIVE)
                    .build();
            entityManager.persist(resume);
        }
        entityManager.flush();

        // When
        Page<Resume> page = resumeRepository.findActiveResumes(PageRequest.of(0, 10));

        // Then
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void testCountByStatus() {
        // Given
        entityManager.persist(testResume);

        Resume inactiveResume = Resume.builder()
                .fullName("Inactive User")
                .email("inactive@example.com")
                .status(Resume.ResumeStatus.INACTIVE)
                .build();
        entityManager.persist(inactiveResume);
        entityManager.flush();

        // When
        long activeCount = resumeRepository.countByStatus(Resume.ResumeStatus.ACTIVE);
        long inactiveCount = resumeRepository.countByStatus(Resume.ResumeStatus.INACTIVE);

        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(inactiveCount).isEqualTo(1);
    }

    @Test
    void testFindRecentResumes() {
        // Given
        entityManager.persist(testResume);
        entityManager.flush();

        // When
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Resume> recentResumes = resumeRepository.findRecentResumes(oneDayAgo);

        // Then
        assertThat(recentResumes).isNotEmpty();
        assertThat(recentResumes.get(0).getCreatedAt()).isAfter(oneDayAgo);
    }

    @Test
    void testSearchByEmailOrName() {
        // Given
        entityManager.persist(testResume);
        entityManager.flush();

        // When
        Page<Resume> foundByEmail = resumeRepository.searchByEmailOrName("john.doe", PageRequest.of(0, 10));
        Page<Resume> foundByName = resumeRepository.searchByEmailOrName("john", PageRequest.of(0, 10));

        // Then
        assertThat(foundByEmail.getContent()).hasSize(1);
        assertThat(foundByName.getContent()).hasSize(1);
    }
}
