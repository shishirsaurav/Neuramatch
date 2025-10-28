package com.neuramatch.matching.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for generating embeddings from job posting data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobEmbeddingService {

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final EmbeddingCacheService cacheService;

    /**
     * Generate embedding for a job posting
     */
    public List<Double> generateJobEmbedding(JobEmbeddingRequest request) {
        log.debug("Generating embedding for job ID: {}", request.getJobId());

        String jobText = constructJobText(request);

        // Generate embedding (cache is handled inside GeminiEmbeddingService)
        List<Double> embedding = geminiEmbeddingService.generateEmbedding(jobText);

        return embedding;
    }

    /**
     * Generate embeddings for multiple jobs in batch
     */
    public List<List<Double>> generateJobEmbeddings(List<JobEmbeddingRequest> requests) {
        log.info("Generating embeddings for {} jobs", requests.size());

        List<String> jobTexts = requests.stream()
            .map(this::constructJobText)
            .toList();

        // Generate embeddings (cache is handled inside GeminiEmbeddingService)
        return geminiEmbeddingService.generateEmbeddings(jobTexts);
    }

    /**
     * Construct job text from structured data
     * Format optimized for semantic understanding
     */
    private String constructJobText(JobEmbeddingRequest request) {
        StringBuilder text = new StringBuilder();

        // Job header
        if (request.getTitle() != null) {
            text.append("Job Title: ").append(request.getTitle()).append("\n\n");
        }

        if (request.getCompanyName() != null) {
            text.append("Company: ").append(request.getCompanyName()).append("\n");
        }

        if (request.getLocation() != null) {
            text.append("Location: ").append(request.getLocation()).append("\n\n");
        }

        // Job description
        if (request.getDescription() != null) {
            text.append("Job Description:\n").append(request.getDescription()).append("\n\n");
        }

        // Required skills - most important for matching
        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            text.append("Required Skills:\n");
            request.getRequiredSkills().forEach(skill -> {
                text.append("- ").append(skill.getSkillName());
                if (skill.getMinYearsRequired() != null) {
                    text.append(" (").append(skill.getMinYearsRequired()).append("+ years)");
                }
                if (skill.getPriority() != null) {
                    text.append(" [").append(skill.getPriority()).append(" priority]");
                }
                text.append("\n");
            });
            text.append("\n");
        }

        // Preferred skills
        if (request.getPreferredSkills() != null && !request.getPreferredSkills().isEmpty()) {
            text.append("Preferred Skills:\n");
            request.getPreferredSkills().forEach(skill ->
                text.append("- ").append(skill).append("\n")
            );
            text.append("\n");
        }

        // Requirements
        if (request.getMinYearsExperience() != null) {
            text.append("Experience Required: ").append(request.getMinYearsExperience()).append("+ years\n");
        }

        if (request.getEducationLevel() != null) {
            text.append("Education: ").append(request.getEducationLevel()).append("\n");
        }

        // Compensation
        if (request.getMinSalary() != null || request.getMaxSalary() != null) {
            text.append("Salary Range: ");
            if (request.getMinSalary() != null) {
                text.append("$").append(request.getMinSalary());
            }
            if (request.getMaxSalary() != null) {
                text.append(" - $").append(request.getMaxSalary());
            }
            text.append("\n");
        }

        // Additional details
        if (request.getEmploymentType() != null) {
            text.append("Employment Type: ").append(request.getEmploymentType()).append("\n");
        }

        if (request.getRemoteType() != null) {
            text.append("Remote: ").append(request.getRemoteType()).append("\n");
        }

        return text.toString().trim();
    }

    /**
     * DTO for job embedding request
     */
    @lombok.Data
    @lombok.Builder
    public static class JobEmbeddingRequest {
        private Long jobId;
        private String title;
        private String companyName;
        private String location;
        private String description;
        private List<RequiredSkillDTO> requiredSkills;
        private List<String> preferredSkills;
        private Integer minYearsExperience;
        private Integer maxYearsExperience;
        private String educationLevel;
        private Integer minSalary;
        private Integer maxSalary;
        private String employmentType;
        private String remoteType;
    }

    @lombok.Data
    @lombok.Builder
    public static class RequiredSkillDTO {
        private String skillName;
        private Integer minYearsRequired;
        private String priority; // MUST_HAVE, NICE_TO_HAVE
    }
}
