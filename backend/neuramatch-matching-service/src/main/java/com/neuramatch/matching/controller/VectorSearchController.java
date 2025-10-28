package com.neuramatch.matching.controller;

import com.neuramatch.matching.search.ResumeJobMatchingService;
import com.neuramatch.matching.search.SemanticSearchService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.ResumeVector;
import com.neuramatch.matching.vector.VectorIndexingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for vector search and matching operations
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class VectorSearchController {

    private final SemanticSearchService searchService;
    private final ResumeJobMatchingService matchingService;
    private final VectorIndexingService indexingService;

    /**
     * Search for resumes using natural language query
     */
    @PostMapping("/resumes")
    public ResponseEntity<List<SemanticSearchService.SearchResult<ResumeVector>>> searchResumes(
            @RequestBody SearchRequest request) {

        log.info("POST /api/search/resumes - query: '{}', limit: {}", request.getQuery(), request.getLimit());

        SemanticSearchService.SearchFilters filters = SemanticSearchService.SearchFilters.builder()
            .minYearsExperience(request.getMinYearsExperience())
            .maxYearsExperience(request.getMaxYearsExperience())
            .location(request.getLocation())
            .remoteType(request.getRemoteType())
            .minQualityScore(request.getMinQualityScore())
            .build();

        List<SemanticSearchService.SearchResult<ResumeVector>> results =
            searchService.searchResumes(request.getQuery(), filters, request.getLimit());

        return ResponseEntity.ok(results);
    }

    /**
     * Search for jobs using natural language query
     */
    @PostMapping("/jobs")
    public ResponseEntity<List<SemanticSearchService.SearchResult<JobVector>>> searchJobs(
            @RequestBody SearchRequest request) {

        log.info("POST /api/search/jobs - query: '{}', limit: {}", request.getQuery(), request.getLimit());

        SemanticSearchService.SearchFilters filters = SemanticSearchService.SearchFilters.builder()
            .minYearsExperience(request.getMinYearsExperience())
            .maxYearsExperience(request.getMaxYearsExperience())
            .location(request.getLocation())
            .remoteType(request.getRemoteType())
            .employmentType(request.getEmploymentType())
            .build();

        List<SemanticSearchService.SearchResult<JobVector>> results =
            searchService.searchJobs(request.getQuery(), filters, request.getLimit());

        return ResponseEntity.ok(results);
    }

    /**
     * Find similar resumes to a given resume
     */
    @GetMapping("/resumes/{resumeId}/similar")
    public ResponseEntity<List<SemanticSearchService.SearchResult<ResumeVector>>> findSimilarResumes(
            @PathVariable Long resumeId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/search/resumes/{}/similar - limit: {}", resumeId, limit);

        List<SemanticSearchService.SearchResult<ResumeVector>> results =
            searchService.findSimilarResumes(resumeId, limit);

        return ResponseEntity.ok(results);
    }

    /**
     * Find similar jobs to a given job
     */
    @GetMapping("/jobs/{jobId}/similar")
    public ResponseEntity<List<SemanticSearchService.SearchResult<JobVector>>> findSimilarJobs(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("GET /api/search/jobs/{}/similar - limit: {}", jobId, limit);

        List<SemanticSearchService.SearchResult<JobVector>> results =
            searchService.findSimilarJobs(jobId, limit);

        return ResponseEntity.ok(results);
    }

    /**
     * Find matching jobs for a resume
     */
    @GetMapping("/resumes/{resumeId}/matches")
    public ResponseEntity<List<ResumeJobMatchingService.JobMatch>> getJobMatchesForResume(
            @PathVariable Long resumeId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String remoteType,
            @RequestParam(required = false) String employmentType,
            @RequestParam(defaultValue = "50") int limit) {

        log.info("GET /api/search/resumes/{}/matches - limit: {}", resumeId, limit);

        ResumeJobMatchingService.MatchingCriteria criteria =
            ResumeJobMatchingService.MatchingCriteria.builder()
                .limit(limit)
                .location(location)
                .remoteType(remoteType)
                .employmentType(employmentType)
                .build();

        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(resumeId, criteria);

        return ResponseEntity.ok(matches);
    }

    /**
     * Find matching candidates for a job
     */
    @GetMapping("/jobs/{jobId}/candidates")
    public ResponseEntity<List<ResumeJobMatchingService.CandidateMatch>> getCandidateMatchesForJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String remoteType,
            @RequestParam(required = false) Integer minQualityScore,
            @RequestParam(defaultValue = "100") int limit) {

        log.info("GET /api/search/jobs/{}/candidates - limit: {}", jobId, limit);

        ResumeJobMatchingService.MatchingCriteria criteria =
            ResumeJobMatchingService.MatchingCriteria.builder()
                .limit(limit)
                .location(location)
                .remoteType(remoteType)
                .minQualityScore(minQualityScore)
                .build();

        List<ResumeJobMatchingService.CandidateMatch> matches =
            matchingService.findMatchingCandidatesForJob(jobId, criteria);

        return ResponseEntity.ok(matches);
    }

    /**
     * Search resumes by skills
     */
    @PostMapping("/resumes/by-skills")
    public ResponseEntity<List<SemanticSearchService.SearchResult<ResumeVector>>> searchResumesBySkills(
            @RequestBody SkillSearchRequest request) {

        log.info("POST /api/search/resumes/by-skills - skills: {}, limit: {}",
            request.getSkills(), request.getLimit());

        List<SemanticSearchService.SearchResult<ResumeVector>> results =
            searchService.searchResumesBySkills(request.getSkills(), request.getLimit());

        return ResponseEntity.ok(results);
    }

    /**
     * Get index statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<VectorIndexingService.IndexStats> getIndexStats() {
        log.info("GET /api/search/stats");

        VectorIndexingService.IndexStats stats = indexingService.getIndexStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Index a resume (admin operation)
     */
    @PostMapping("/admin/index/resume")
    public ResponseEntity<Map<String, Object>> indexResume(@RequestBody VectorIndexingService.ResumeIndexRequest request) {
        log.info("POST /api/search/admin/index/resume - resumeId: {}", request.getResumeId());

        ResumeVector indexed = indexingService.indexResume(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "resumeId", request.getResumeId(),
            "vectorId", indexed.getId()
        ));
    }

    /**
     * Index a job (admin operation)
     */
    @PostMapping("/admin/index/job")
    public ResponseEntity<Map<String, Object>> indexJob(@RequestBody VectorIndexingService.JobIndexRequest request) {
        log.info("POST /api/search/admin/index/job - jobId: {}", request.getJobId());

        JobVector indexed = indexingService.indexJob(request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "jobId", request.getJobId(),
            "vectorId", indexed.getId()
        ));
    }

    // ========== Request DTOs ==========

    @lombok.Data
    public static class SearchRequest {
        private String query;
        private Integer limit = 20;
        private Integer minYearsExperience;
        private Integer maxYearsExperience;
        private String location;
        private String remoteType;
        private String employmentType;
        private Integer minQualityScore;
    }

    @lombok.Data
    public static class SkillSearchRequest {
        private List<String> skills;
        private Integer limit = 20;
    }
}
