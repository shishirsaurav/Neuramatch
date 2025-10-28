package com.neuramatch.matching.search;

import com.neuramatch.matching.embedding.GeminiEmbeddingService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.JobVectorRepository;
import com.neuramatch.matching.vector.ResumeVector;
import com.neuramatch.matching.vector.ResumeVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for semantic similarity search using vector embeddings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchService {

    private final ResumeVectorRepository resumeVectorRepository;
    private final JobVectorRepository jobVectorRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;

    /**
     * Search for similar resumes using text query
     */
    public List<SearchResult<ResumeVector>> searchResumes(String query, SearchFilters filters, int limit) {
        log.debug("Searching resumes with query: '{}'", query);

        // Generate embedding for search query
        List<Double> queryEmbedding = geminiEmbeddingService.generateEmbedding(query);
        String embeddingStr = vectorToString(queryEmbedding);

        // Execute vector similarity search with filters
        List<ResumeVector> results;

        if (filters == null || filters.isEmpty()) {
            results = resumeVectorRepository.findSimilarResumes(embeddingStr, -1L, limit);
        } else {
            results = resumeVectorRepository.findSimilarResumesWithFilters(
                embeddingStr,
                filters.getMinYearsExperience(),
                filters.getMaxYearsExperience(),
                filters.getLocation(),
                filters.getRemoteType(),
                filters.getMinQualityScore(),
                limit
            );
        }

        // Convert to search results with similarity scores
        return convertToSearchResults(results, queryEmbedding);
    }

    /**
     * Search for similar jobs using text query
     */
    public List<SearchResult<JobVector>> searchJobs(String query, SearchFilters filters, int limit) {
        log.debug("Searching jobs with query: '{}'", query);

        // Generate embedding for search query
        List<Double> queryEmbedding = geminiEmbeddingService.generateEmbedding(query);
        String embeddingStr = vectorToString(queryEmbedding);

        // Execute vector similarity search with filters
        List<JobVector> results;

        if (filters == null || filters.isEmpty()) {
            results = jobVectorRepository.findMatchingJobs(embeddingStr, limit);
        } else {
            results = jobVectorRepository.findMatchingJobsWithFilters(
                embeddingStr,
                filters.getMinYearsExperience(),
                filters.getMaxYearsExperience(),
                filters.getLocation(),
                filters.getRemoteType(),
                filters.getEmploymentType(),
                limit
            );
        }

        // Convert to search results with similarity scores
        return convertToSearchResults(results, queryEmbedding);
    }

    /**
     * Find similar resumes to a given resume
     */
    public List<SearchResult<ResumeVector>> findSimilarResumes(Long resumeId, int limit) {
        log.debug("Finding similar resumes to ID: {}", resumeId);

        ResumeVector sourceResume = resumeVectorRepository.findByResumeId(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        List<Double> embedding = sourceResume.getEmbeddingAsList();
        String embeddingStr = vectorToString(embedding);

        List<ResumeVector> results = resumeVectorRepository.findSimilarResumes(
            embeddingStr,
            resumeId,
            limit
        );

        return convertToSearchResults(results, embedding);
    }

    /**
     * Find similar jobs to a given job
     */
    public List<SearchResult<JobVector>> findSimilarJobs(Long jobId, int limit) {
        log.debug("Finding similar jobs to ID: {}", jobId);

        JobVector sourceJob = jobVectorRepository.findByJobId(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        List<Double> embedding = sourceJob.getEmbeddingAsList();
        String embeddingStr = vectorToString(embedding);

        List<JobVector> results = jobVectorRepository.findSimilarJobs(
            embeddingStr,
            jobId,
            limit
        );

        return convertToSearchResults(results, embedding);
    }

    /**
     * Search resumes with skill filter
     */
    public List<SearchResult<ResumeVector>> searchResumesBySkills(List<String> skills, int limit) {
        log.debug("Searching resumes with skills: {}", skills);

        // Create query from skills
        String query = String.join(", ", skills);
        List<Double> queryEmbedding = geminiEmbeddingService.generateEmbedding(query);
        String embeddingStr = vectorToString(queryEmbedding);

        // PostgreSQL array format: '{skill1,skill2,skill3}'
        String skillsArrayStr = "{" + String.join(",", skills) + "}";

        List<ResumeVector> results = resumeVectorRepository.findSimilarResumesWithSkills(
            embeddingStr,
            skillsArrayStr,
            limit
        );

        return convertToSearchResults(results, queryEmbedding);
    }

    /**
     * Convert raw results to SearchResult with similarity scores
     */
    private <T> List<SearchResult<T>> convertToSearchResults(List<T> entities, List<Double> queryEmbedding) {
        List<SearchResult<T>> results = new ArrayList<>();

        for (T entity : entities) {
            List<Double> entityEmbedding;

            if (entity instanceof ResumeVector) {
                entityEmbedding = ((ResumeVector) entity).getEmbeddingAsList();
            } else if (entity instanceof JobVector) {
                entityEmbedding = ((JobVector) entity).getEmbeddingAsList();
            } else {
                continue;
            }

            double similarity = geminiEmbeddingService.cosineSimilarity(queryEmbedding, entityEmbedding);

            results.add(SearchResult.<T>builder()
                .entity(entity)
                .similarityScore(similarity)
                .distance(1.0 - similarity) // Cosine distance
                .build());
        }

        return results;
    }

    /**
     * Convert vector to PostgreSQL format string
     * Format: [0.1,0.2,0.3,...]
     */
    private String vectorToString(List<Double> vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(vector.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    // ========== DTOs ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchFilters {
        private Integer minYearsExperience;
        private Integer maxYearsExperience;
        private String location;
        private String remoteType;
        private String employmentType;
        private Integer minQualityScore;

        public boolean isEmpty() {
            return minYearsExperience == null &&
                   maxYearsExperience == null &&
                   location == null &&
                   remoteType == null &&
                   employmentType == null &&
                   minQualityScore == null;
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SearchResult<T> {
        private T entity;
        private double similarityScore; // 0.0 to 1.0 (higher = more similar)
        private double distance; // 0.0 to 2.0 (lower = more similar)
    }
}
