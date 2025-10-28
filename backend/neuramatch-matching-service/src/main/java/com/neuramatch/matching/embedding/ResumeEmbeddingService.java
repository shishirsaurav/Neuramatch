package com.neuramatch.matching.embedding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for generating embeddings from resume data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeEmbeddingService {

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final EmbeddingCacheService cacheService;

    /**
     * Generate embedding for a resume
     * Combines skills, experience, and education into a single embedding
     */
    public List<Double> generateResumeEmbedding(ResumeEmbeddingRequest request) {
        log.debug("Generating embedding for resume ID: {}", request.getResumeId());

        String resumeText = constructResumeText(request);

        // Generate embedding (cache is handled inside GeminiEmbeddingService)
        List<Double> embedding = geminiEmbeddingService.generateEmbedding(resumeText);

        return embedding;
    }

    /**
     * Generate embeddings for multiple resumes in batch
     */
    public List<List<Double>> generateResumeEmbeddings(List<ResumeEmbeddingRequest> requests) {
        log.info("Generating embeddings for {} resumes", requests.size());

        List<String> resumeTexts = requests.stream()
            .map(this::constructResumeText)
            .toList();

        // Generate embeddings (cache is handled inside GeminiEmbeddingService)
        return geminiEmbeddingService.generateEmbeddings(resumeTexts);
    }

    /**
     * Construct resume text from structured data
     * Format optimized for semantic understanding
     */
    private String constructResumeText(ResumeEmbeddingRequest request) {
        StringBuilder text = new StringBuilder();

        // Profile summary
        if (request.getFullName() != null) {
            text.append("Candidate: ").append(request.getFullName()).append("\n\n");
        }

        // Skills section - most important for matching
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            text.append("Technical Skills:\n");
            request.getSkills().forEach(skill -> {
                text.append("- ").append(skill.getSkillName());
                if (skill.getProficiency() != null) {
                    text.append(" (").append(skill.getProficiency()).append(")");
                }
                if (skill.getYearsOfExperience() != null) {
                    text.append(" - ").append(skill.getYearsOfExperience()).append(" years");
                }
                text.append("\n");
            });
            text.append("\n");
        }

        // Experience section
        if (request.getExperiences() != null && !request.getExperiences().isEmpty()) {
            text.append("Professional Experience:\n");
            request.getExperiences().forEach(exp -> {
                text.append("- ").append(exp.getJobTitle());
                if (exp.getCompanyName() != null) {
                    text.append(" at ").append(exp.getCompanyName());
                }
                if (exp.getDurationInMonths() != null) {
                    text.append(" (").append(exp.getDurationInMonths()).append(" months)");
                }
                if (exp.getDescription() != null) {
                    text.append(": ").append(exp.getDescription());
                }
                text.append("\n");
            });
            text.append("\n");
        }

        // Education section
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            text.append("Education:\n");
            request.getEducations().forEach(edu -> {
                text.append("- ").append(edu.getDegree());
                if (edu.getFieldOfStudy() != null) {
                    text.append(" in ").append(edu.getFieldOfStudy());
                }
                if (edu.getInstitutionName() != null) {
                    text.append(" from ").append(edu.getInstitutionName());
                }
                text.append("\n");
            });
            text.append("\n");
        }

        // Additional context
        if (request.getSummary() != null) {
            text.append("Professional Summary:\n").append(request.getSummary()).append("\n");
        }

        return text.toString().trim();
    }

    /**
     * DTO for resume embedding request
     */
    @lombok.Data
    @lombok.Builder
    public static class ResumeEmbeddingRequest {
        private Long resumeId;
        private String fullName;
        private String summary;
        private List<SkillDTO> skills;
        private List<ExperienceDTO> experiences;
        private List<EducationDTO> educations;
    }

    @lombok.Data
    @lombok.Builder
    public static class SkillDTO {
        private String skillName;
        private String proficiency;
        private Integer yearsOfExperience;
    }

    @lombok.Data
    @lombok.Builder
    public static class ExperienceDTO {
        private String jobTitle;
        private String companyName;
        private Integer durationInMonths;
        private String description;
    }

    @lombok.Data
    @lombok.Builder
    public static class EducationDTO {
        private String degree;
        private String fieldOfStudy;
        private String institutionName;
    }
}
