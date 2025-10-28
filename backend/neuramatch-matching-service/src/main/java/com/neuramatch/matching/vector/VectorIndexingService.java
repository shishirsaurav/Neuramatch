package com.neuramatch.matching.vector;

import com.neuramatch.matching.embedding.JobEmbeddingService;
import com.neuramatch.matching.embedding.ResumeEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for indexing resumes and jobs into vector database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorIndexingService {

    private final ResumeVectorRepository resumeVectorRepository;
    private final JobVectorRepository jobVectorRepository;
    private final ResumeEmbeddingService resumeEmbeddingService;
    private final JobEmbeddingService jobEmbeddingService;

    /**
     * Index a resume into vector database
     */
    @Transactional
    public ResumeVector indexResume(ResumeIndexRequest request) {
        log.info("Indexing resume ID: {}", request.getResumeId());

        // Generate embedding
        ResumeEmbeddingService.ResumeEmbeddingRequest embeddingRequest =
            ResumeEmbeddingService.ResumeEmbeddingRequest.builder()
                .resumeId(request.getResumeId())
                .fullName(request.getFullName())
                .summary(request.getSummary())
                .skills(request.getSkills())
                .experiences(request.getExperiences())
                .educations(request.getEducations())
                .build();

        List<Double> embedding = resumeEmbeddingService.generateResumeEmbedding(embeddingRequest);

        // Check if resume vector already exists
        Optional<ResumeVector> existing = resumeVectorRepository.findByResumeId(request.getResumeId());

        ResumeVector resumeVector;
        if (existing.isPresent()) {
            log.debug("Updating existing resume vector for ID: {}", request.getResumeId());
            resumeVector = existing.get();
        } else {
            resumeVector = new ResumeVector();
            resumeVector.setResumeId(request.getResumeId());
        }

        // Set embedding and metadata
        resumeVector.setEmbeddingFromList(embedding);
        resumeVector.setFullName(request.getFullName());
        resumeVector.setYearsOfExperience(request.getYearsOfExperience());
        resumeVector.setLocation(request.getLocation());
        resumeVector.setMinSalary(request.getMinSalary());
        resumeVector.setMaxSalary(request.getMaxSalary());
        resumeVector.setRemotePreference(request.getRemotePreference());
        resumeVector.setTopSkills(request.getTopSkills());
        resumeVector.setEducationLevel(request.getEducationLevel());
        resumeVector.setQualityScore(request.getQualityScore());
        resumeVector.setIsActive(true);

        resumeVector = resumeVectorRepository.save(resumeVector);
        log.info("Successfully indexed resume ID: {} with vector ID: {}", request.getResumeId(), resumeVector.getId());

        return resumeVector;
    }

    /**
     * Index a job into vector database
     */
    @Transactional
    public JobVector indexJob(JobIndexRequest request) {
        log.info("Indexing job ID: {}", request.getJobId());

        // Generate embedding
        JobEmbeddingService.JobEmbeddingRequest embeddingRequest =
            JobEmbeddingService.JobEmbeddingRequest.builder()
                .jobId(request.getJobId())
                .title(request.getTitle())
                .companyName(request.getCompanyName())
                .location(request.getLocation())
                .description(request.getDescription())
                .requiredSkills(request.getRequiredSkills())
                .preferredSkills(request.getPreferredSkills())
                .minYearsExperience(request.getMinYearsExperience())
                .maxYearsExperience(request.getMaxYearsExperience())
                .educationLevel(request.getEducationLevel())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .employmentType(request.getEmploymentType())
                .remoteType(request.getRemoteType())
                .build();

        List<Double> embedding = jobEmbeddingService.generateJobEmbedding(embeddingRequest);

        // Check if job vector already exists
        Optional<JobVector> existing = jobVectorRepository.findByJobId(request.getJobId());

        JobVector jobVector;
        if (existing.isPresent()) {
            log.debug("Updating existing job vector for ID: {}", request.getJobId());
            jobVector = existing.get();
        } else {
            jobVector = new JobVector();
            jobVector.setJobId(request.getJobId());
        }

        // Set embedding and metadata
        jobVector.setEmbeddingFromList(embedding);
        jobVector.setTitle(request.getTitle());
        jobVector.setCompanyName(request.getCompanyName());
        jobVector.setLocation(request.getLocation());
        jobVector.setMinYearsExperience(request.getMinYearsExperience());
        jobVector.setMaxYearsExperience(request.getMaxYearsExperience());
        jobVector.setMinSalary(request.getMinSalary());
        jobVector.setMaxSalary(request.getMaxSalary());
        jobVector.setEmploymentType(request.getEmploymentType());
        jobVector.setRemoteType(request.getRemoteType());
        jobVector.setRequiredSkills(request.getRequiredSkillNames());
        jobVector.setEducationLevel(request.getEducationLevel());
        jobVector.setPriorityScore(request.getPriorityScore());
        jobVector.setExpiresAt(request.getExpiresAt());
        jobVector.setIsActive(true);

        jobVector = jobVectorRepository.save(jobVector);
        log.info("Successfully indexed job ID: {} with vector ID: {}", request.getJobId(), jobVector.getId());

        return jobVector;
    }

    /**
     * Index multiple resumes in batch
     */
    @Transactional
    public List<ResumeVector> indexResumesBatch(List<ResumeIndexRequest> requests) {
        log.info("Batch indexing {} resumes", requests.size());

        return requests.stream()
            .map(this::indexResume)
            .toList();
    }

    /**
     * Index multiple jobs in batch
     */
    @Transactional
    public List<JobVector> indexJobsBatch(List<JobIndexRequest> requests) {
        log.info("Batch indexing {} jobs", requests.size());

        return requests.stream()
            .map(this::indexJob)
            .toList();
    }

    /**
     * Delete resume from vector index
     */
    @Transactional
    public void deleteResumeIndex(Long resumeId) {
        log.info("Deleting resume vector for ID: {}", resumeId);
        resumeVectorRepository.deleteByResumeId(resumeId);
    }

    /**
     * Delete job from vector index
     */
    @Transactional
    public void deleteJobIndex(Long jobId) {
        log.info("Deleting job vector for ID: {}", jobId);
        jobVectorRepository.deleteByJobId(jobId);
    }

    /**
     * Deactivate resume (soft delete)
     */
    @Transactional
    public void deactivateResume(Long resumeId) {
        resumeVectorRepository.findByResumeId(resumeId).ifPresent(rv -> {
            rv.setIsActive(false);
            resumeVectorRepository.save(rv);
            log.info("Deactivated resume vector for ID: {}", resumeId);
        });
    }

    /**
     * Deactivate job (soft delete)
     */
    @Transactional
    public void deactivateJob(Long jobId) {
        jobVectorRepository.findByJobId(jobId).ifPresent(jv -> {
            jv.setIsActive(false);
            jobVectorRepository.save(jv);
            log.info("Deactivated job vector for ID: {}", jobId);
        });
    }

    /**
     * Deactivate expired jobs
     */
    @Transactional
    public int deactivateExpiredJobs() {
        List<JobVector> expiredJobs = jobVectorRepository.findExpiredJobs(LocalDateTime.now());
        expiredJobs.forEach(job -> job.setIsActive(false));
        jobVectorRepository.saveAll(expiredJobs);

        log.info("Deactivated {} expired jobs", expiredJobs.size());
        return expiredJobs.size();
    }

    /**
     * Get index statistics
     */
    public IndexStats getIndexStats() {
        long resumeCount = resumeVectorRepository.countByIsActiveTrue();
        long jobCount = jobVectorRepository.countActiveJobs();

        return IndexStats.builder()
            .totalResumes(resumeCount)
            .totalJobs(jobCount)
            .build();
    }

    // ========== DTOs ==========

    @lombok.Data
    @lombok.Builder
    public static class ResumeIndexRequest {
        private Long resumeId;
        private String fullName;
        private String summary;
        private List<ResumeEmbeddingService.SkillDTO> skills;
        private List<ResumeEmbeddingService.ExperienceDTO> experiences;
        private List<ResumeEmbeddingService.EducationDTO> educations;
        private Integer yearsOfExperience;
        private String location;
        private Integer minSalary;
        private Integer maxSalary;
        private String remotePreference;
        private String[] topSkills;
        private String educationLevel;
        private Integer qualityScore;
    }

    @lombok.Data
    @lombok.Builder
    public static class JobIndexRequest {
        private Long jobId;
        private String title;
        private String companyName;
        private String location;
        private String description;
        private List<JobEmbeddingService.RequiredSkillDTO> requiredSkills;
        private List<String> preferredSkills;
        private Integer minYearsExperience;
        private Integer maxYearsExperience;
        private String educationLevel;
        private Integer minSalary;
        private Integer maxSalary;
        private String employmentType;
        private String remoteType;
        private Integer priorityScore;
        private LocalDateTime expiresAt;

        public String[] getRequiredSkillNames() {
            if (requiredSkills == null) return new String[0];
            return requiredSkills.stream()
                .map(JobEmbeddingService.RequiredSkillDTO::getSkillName)
                .toArray(String[]::new);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class IndexStats {
        private long totalResumes;
        private long totalJobs;
    }
}
