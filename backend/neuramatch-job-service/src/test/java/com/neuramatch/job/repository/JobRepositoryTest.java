package com.neuramatch.job.repository;

import com.neuramatch.job.entity.Company;
import com.neuramatch.job.entity.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    private Company testCompany;
    private Job testJob;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .companyName("Tech Corp")
                .industry("Technology")
                .companySize(Company.CompanySize.LARGE)
                .status(Company.CompanyStatus.ACTIVE)
                .build();
        entityManager.persist(testCompany);

        testJob = Job.builder()
                .jobTitle("Senior Java Developer")
                .jobDescription("Looking for experienced Java developer")
                .location("San Francisco, CA")
                .isRemote(false)
                .workMode(Job.WorkMode.HYBRID)
                .minYearsExperience(5)
                .maxYearsExperience(10)
                .experienceLevel(Job.ExperienceLevel.SENIOR)
                .minSalary(BigDecimal.valueOf(120000))
                .maxSalary(BigDecimal.valueOf(180000))
                .jobType(Job.JobType.FULL_TIME)
                .status(Job.JobStatus.ACTIVE)
                .company(testCompany)
                .build();
    }

    @Test
    void testSaveJob() {
        // When
        Job saved = jobRepository.save(testJob);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getJobTitle()).isEqualTo("Senior Java Developer");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCompany()).isEqualTo(testCompany);
    }

    @Test
    void testFindByStatus() {
        // Given
        entityManager.persist(testJob);

        Job closedJob = Job.builder()
                .jobTitle("Closed Position")
                .jobDescription("This position is closed")
                .status(Job.JobStatus.CLOSED)
                .jobType(Job.JobType.FULL_TIME)
                .company(testCompany)
                .build();
        entityManager.persist(closedJob);
        entityManager.flush();

        // When
        List<Job> activeJobs = jobRepository.findByStatus(Job.JobStatus.ACTIVE);

        // Then
        assertThat(activeJobs).hasSize(1);
        assertThat(activeJobs.get(0).getJobTitle()).isEqualTo("Senior Java Developer");
    }

    @Test
    void testFindByCompanyId() {
        // Given
        entityManager.persist(testJob);
        entityManager.flush();

        // When
        List<Job> companyJobs = jobRepository.findByCompanyId(testCompany.getId());

        // Then
        assertThat(companyJobs).hasSize(1);
        assertThat(companyJobs.get(0).getCompany().getCompanyName()).isEqualTo("Tech Corp");
    }

    @Test
    void testFindByJobTitleContaining() {
        // Given
        entityManager.persist(testJob);
        entityManager.flush();

        // When
        List<Job> found = jobRepository.findByJobTitleContainingIgnoreCase("java");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getJobTitle()).contains("Java");
    }

    @Test
    void testFindRemoteJobs() {
        // Given
        Job remoteJob = Job.builder()
                .jobTitle("Remote Developer")
                .jobDescription("Remote position")
                .isRemote(true)
                .status(Job.JobStatus.ACTIVE)
                .jobType(Job.JobType.FULL_TIME)
                .company(testCompany)
                .build();
        entityManager.persist(remoteJob);
        entityManager.persist(testJob);
        entityManager.flush();

        // When
        Page<Job> remoteJobs = jobRepository.findByIsRemoteTrue(PageRequest.of(0, 10));

        // Then
        assertThat(remoteJobs.getContent()).hasSize(1);
        assertThat(remoteJobs.getContent().get(0).getIsRemote()).isTrue();
    }

    @Test
    void testFindByJobType() {
        // Given
        entityManager.persist(testJob);

        Job contractJob = Job.builder()
                .jobTitle("Contract Developer")
                .jobDescription("Contract position")
                .jobType(Job.JobType.CONTRACT)
                .status(Job.JobStatus.ACTIVE)
                .company(testCompany)
                .build();
        entityManager.persist(contractJob);
        entityManager.flush();

        // When
        List<Job> fullTimeJobs = jobRepository.findByJobType(Job.JobType.FULL_TIME);

        // Then
        assertThat(fullTimeJobs).hasSize(1);
        assertThat(fullTimeJobs.get(0).getJobType()).isEqualTo(Job.JobType.FULL_TIME);
    }

    @Test
    void testSearchJobs() {
        // Given
        entityManager.persist(testJob);
        entityManager.flush();

        // When
        Page<Job> found = jobRepository.searchJobs("java", PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getJobTitle()).containsIgnoringCase("java");
    }

    @Test
    void testCountByStatus() {
        // Given
        entityManager.persist(testJob);

        Job draftJob = Job.builder()
                .jobTitle("Draft Job")
                .jobDescription("Draft")
                .status(Job.JobStatus.DRAFT)
                .jobType(Job.JobType.FULL_TIME)
                .company(testCompany)
                .build();
        entityManager.persist(draftJob);
        entityManager.flush();

        // When
        long activeCount = jobRepository.countByStatus(Job.JobStatus.ACTIVE);
        long draftCount = jobRepository.countByStatus(Job.JobStatus.DRAFT);

        // Then
        assertThat(activeCount).isEqualTo(1);
        assertThat(draftCount).isEqualTo(1);
    }

    @Test
    void testFindActiveJobs() {
        // Given
        testJob.setPublishedAt(LocalDateTime.now().minusDays(5));
        entityManager.persist(testJob);

        Job expiredJob = Job.builder()
                .jobTitle("Expired Job")
                .jobDescription("Expired")
                .status(Job.JobStatus.ACTIVE)
                .applicationDeadline(LocalDate.now().minusDays(1))
                .jobType(Job.JobType.FULL_TIME)
                .company(testCompany)
                .build();
        entityManager.persist(expiredJob);
        entityManager.flush();

        // When
        Page<Job> activeJobs = jobRepository.findActiveJobs(PageRequest.of(0, 10));

        // Then
        assertThat(activeJobs.getContent()).hasSize(1);
        assertThat(activeJobs.getContent().get(0).getJobTitle()).isEqualTo("Senior Java Developer");
    }

    @Test
    void testFindByExperienceLevel() {
        // Given
        entityManager.persist(testJob);
        entityManager.flush();

        // When
        List<Job> seniorJobs = jobRepository.findByExperienceLevel(Job.ExperienceLevel.SENIOR);

        // Then
        assertThat(seniorJobs).hasSize(1);
        assertThat(seniorJobs.get(0).getExperienceLevel()).isEqualTo(Job.ExperienceLevel.SENIOR);
    }
}
