package com.neuramatch.matching.search;

import com.neuramatch.matching.embedding.GeminiEmbeddingService;
import com.neuramatch.matching.service.SkillEnrichmentService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.JobVectorRepository;
import com.neuramatch.matching.vector.ResumeVector;
import com.neuramatch.matching.vector.ResumeVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for matching resumes to jobs and vice versa
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeJobMatchingService {

    private final ResumeVectorRepository resumeVectorRepository;
    private final JobVectorRepository jobVectorRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final SkillEnrichmentService skillEnrichmentService;

    /**
     * Find best matching jobs for a resume
     */
    public List<JobMatch> findMatchingJobsForResume(Long resumeId, MatchingCriteria criteria) {
        log.info("Finding matching jobs for resume ID: {}", resumeId);

        ResumeVector resume = resumeVectorRepository.findByResumeId(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        List<Double> resumeEmbedding = resume.getEmbeddingAsList();
        String embeddingStr = vectorToString(resumeEmbedding);

        int limit = criteria != null && criteria.getLimit() != null ? criteria.getLimit() : 50;

        // Find jobs using vector similarity
        List<JobVector> jobs;
        if (criteria != null && criteria.hasFilters()) {
            jobs = jobVectorRepository.findMatchingJobsWithFilters(
                embeddingStr,
                resume.getYearsOfExperience(),
                resume.getYearsOfExperience(),
                criteria.getLocation(),
                criteria.getRemoteType(),
                criteria.getEmploymentType(),
                limit
            );
        } else {
            jobs = jobVectorRepository.findMatchingJobsByExperience(
                embeddingStr,
                resume.getYearsOfExperience() != null ? resume.getYearsOfExperience() : 0,
                limit
            );
        }

        // Calculate match scores
        return jobs.stream()
            .map(job -> calculateJobMatch(resume, job))
            .sorted((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()))
            .collect(Collectors.toList());
    }

    /**
     * Find best matching candidates for a job
     */
    public List<CandidateMatch> findMatchingCandidatesForJob(Long jobId, MatchingCriteria criteria) {
        log.info("Finding matching candidates for job ID: {}", jobId);

        JobVector job = jobVectorRepository.findByJobId(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        List<Double> jobEmbedding = job.getEmbeddingAsList();
        String embeddingStr = vectorToString(jobEmbedding);

        int limit = criteria != null && criteria.getLimit() != null ? criteria.getLimit() : 100;

        // Find resumes using vector similarity
        List<ResumeVector> resumes;
        if (criteria != null && criteria.hasFilters()) {
            resumes = resumeVectorRepository.findSimilarResumesWithFilters(
                embeddingStr,
                job.getMinYearsExperience(),
                job.getMaxYearsExperience(),
                criteria.getLocation(),
                criteria.getRemoteType(),
                criteria.getMinQualityScore(),
                limit
            );
        } else {
            resumes = resumeVectorRepository.findSimilarResumesWithExperience(
                embeddingStr,
                job.getMinYearsExperience() != null ? job.getMinYearsExperience() : 0,
                limit
            );
        }

        // Calculate match scores
        return resumes.stream()
            .map(resume -> calculateCandidateMatch(resume, job))
            .sorted((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()))
            .collect(Collectors.toList());
    }

    /**
     * Calculate job match score for a resume
     */
    private JobMatch calculateJobMatch(ResumeVector resume, JobVector job) {
        // 1. Semantic similarity (40%)
        double semanticScore = geminiEmbeddingService.cosineSimilarity(
            resume.getEmbeddingAsList(),
            job.getEmbeddingAsList()
        );

        // 2. Skills match (30%)
        double skillScore = calculateSkillMatch(
            resume.getTopSkills() != null ? Arrays.asList(resume.getTopSkills()) : List.of(),
            job.getRequiredSkills() != null ? Arrays.asList(job.getRequiredSkills()) : List.of()
        );

        // 3. Experience match (20%)
        double experienceScore = calculateExperienceMatch(
            resume.getYearsOfExperience(),
            job.getMinYearsExperience(),
            job.getMaxYearsExperience()
        );

        // 4. Location/Remote match (10%)
        double locationScore = calculateLocationMatch(
            resume.getLocation(),
            resume.getRemotePreference(),
            job.getLocation(),
            job.getRemoteType()
        );

        // Weighted overall score
        double overallScore = (semanticScore * 0.4) +
                             (skillScore * 0.3) +
                             (experienceScore * 0.2) +
                             (locationScore * 0.1);

        return JobMatch.builder()
            // Job information
            .jobId(job.getJobId())
            .jobTitle(job.getTitle())
            .companyName(job.getCompanyName())
            .location(job.getLocation())
            .remoteType(job.getRemoteType())
            .salaryRange(job.getMinSalary() != null && job.getMaxSalary() != null ?
                String.format("$%,d - $%,d", job.getMinSalary(), job.getMaxSalary()) : null)
            // Resume information
            .resumeId(resume.getResumeId())
            .candidateName(resume.getFullName())
            .yearsOfExperience(resume.getYearsOfExperience())
            .minYearsRequired(job.getMinYearsExperience())
            // Match scores
            .overallScore(overallScore * 100) // Convert to 0-100
            .semanticSimilarity(semanticScore)
            .skillMatchScore(skillScore)
            .experienceMatchScore(experienceScore)
            .locationMatchScore(locationScore)
            .build();
    }

    /**
     * Calculate candidate match score for a job
     */
    private CandidateMatch calculateCandidateMatch(ResumeVector resume, JobVector job) {
        // Same scoring logic as job match
        double semanticScore = geminiEmbeddingService.cosineSimilarity(
            resume.getEmbeddingAsList(),
            job.getEmbeddingAsList()
        );

        double skillScore = calculateSkillMatch(
            resume.getTopSkills() != null ? Arrays.asList(resume.getTopSkills()) : List.of(),
            job.getRequiredSkills() != null ? Arrays.asList(job.getRequiredSkills()) : List.of()
        );

        double experienceScore = calculateExperienceMatch(
            resume.getYearsOfExperience(),
            job.getMinYearsExperience(),
            job.getMaxYearsExperience()
        );

        double locationScore = calculateLocationMatch(
            resume.getLocation(),
            resume.getRemotePreference(),
            job.getLocation(),
            job.getRemoteType()
        );

        double overallScore = (semanticScore * 0.4) +
                             (skillScore * 0.3) +
                             (experienceScore * 0.2) +
                             (locationScore * 0.1);

        return CandidateMatch.builder()
            .resumeId(resume.getResumeId())
            .fullName(resume.getFullName())
            .yearsOfExperience(resume.getYearsOfExperience())
            .location(resume.getLocation())
            .remotePreference(resume.getRemotePreference())
            .overallScore(overallScore * 100)
            .semanticSimilarity(semanticScore)
            .skillMatchScore(skillScore)
            .experienceMatchScore(experienceScore)
            .locationMatchScore(locationScore)
            .qualityScore(resume.getQualityScore())
            .build();
    }

    /**
     * Calculate skill match score with alternative skills considered
     */
    private double calculateSkillMatch(List<String> resumeSkills, List<String> jobSkills) {
        if (jobSkills.isEmpty()) {
            return 1.0; // No skills required
        }

        if (resumeSkills.isEmpty()) {
            return 0.0; // No skills on resume
        }

        // Use skill enrichment service to calculate coverage with alternatives
        double coverage = skillEnrichmentService.calculateSkillCoverage(
            resumeSkills,
            jobSkills,
            true // Use alternatives
        );

        return coverage;
    }

    /**
     * Calculate experience match score
     */
    private double calculateExperienceMatch(Integer candidateYears, Integer minRequired, Integer maxRequired) {
        if (candidateYears == null) {
            return 0.5; // Unknown experience
        }

        if (minRequired == null && maxRequired == null) {
            return 1.0; // No experience requirement
        }

        int min = minRequired != null ? minRequired : 0;
        int max = maxRequired != null ? maxRequired : 50;

        if (candidateYears >= min && candidateYears <= max) {
            return 1.0; // Perfect match
        } else if (candidateYears < min) {
            // Underqualified - penalize based on gap
            int gap = min - candidateYears;
            return Math.max(0.0, 1.0 - (gap * 0.1)); // -10% per year under
        } else {
            // Overqualified - slight penalty
            int gap = candidateYears - max;
            return Math.max(0.7, 1.0 - (gap * 0.05)); // -5% per year over, min 0.7
        }
    }

    /**
     * Calculate location/remote match score
     */
    private double calculateLocationMatch(String resumeLocation, String resumeRemote,
                                         String jobLocation, String jobRemote) {
        // Both remote
        if ("REMOTE".equals(resumeRemote) && "REMOTE".equals(jobRemote)) {
            return 1.0;
        }

        // Job is remote, candidate prefers remote
        if ("REMOTE".equals(jobRemote)) {
            return "REMOTE".equals(resumeRemote) ? 1.0 : 0.8;
        }

        // Location match
        if (resumeLocation != null && jobLocation != null) {
            if (resumeLocation.equalsIgnoreCase(jobLocation)) {
                return 1.0;
            }
            // Same state/region could be implemented here
        }

        // Hybrid is acceptable for both
        if ("HYBRID".equals(resumeRemote) || "HYBRID".equals(jobRemote)) {
            return 0.7;
        }

        // Default to moderate match
        return 0.5;
    }

    /**
     * Convert vector to PostgreSQL format
     */
    private String vectorToString(List<Double> vector) {
        return "[" + vector.stream()
            .map(Object::toString)
            .collect(Collectors.joining(",")) + "]";
    }

    // ========== DTOs ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MatchingCriteria {
        private Integer limit;
        private String location;
        private String remoteType;
        private String employmentType;
        private Integer minQualityScore;

        public boolean hasFilters() {
            return location != null ||
                   remoteType != null ||
                   employmentType != null ||
                   minQualityScore != null;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class JobMatch {
        // Job information
        private Long jobId;
        private String jobTitle;
        private String companyName;
        private String location;
        private String remoteType;
        private String salaryRange;

        // Resume information (for explainability)
        private Long resumeId;
        private String candidateName;
        private Integer yearsOfExperience;
        private Integer minYearsRequired;

        // Match scores
        private double overallScore; // 0-100
        private double semanticSimilarity; // 0-1
        private double skillMatchScore; // 0-1
        private double experienceMatchScore; // 0-1
        private double locationMatchScore; // 0-1
    }

    @lombok.Data
    @lombok.Builder
    public static class CandidateMatch {
        private Long resumeId;
        private String fullName;
        private Integer yearsOfExperience;
        private String location;
        private String remotePreference;
        private Integer qualityScore;

        private double overallScore; // 0-100
        private double semanticSimilarity; // 0-1
        private double skillMatchScore; // 0-1
        private double experienceMatchScore; // 0-1
        private double locationMatchScore; // 0-1
    }
}
