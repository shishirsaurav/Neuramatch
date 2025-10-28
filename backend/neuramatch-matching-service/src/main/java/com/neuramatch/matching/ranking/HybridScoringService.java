package com.neuramatch.matching.ranking;

import com.neuramatch.matching.search.ResumeJobMatchingService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.ResumeVector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid scoring combining semantic (vector) similarity with lexical (BM25) matching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HybridScoringService {

    private static final double SEMANTIC_WEIGHT = 0.7;  // Weight for vector similarity
    private static final double LEXICAL_WEIGHT = 0.3;   // Weight for BM25 score

    /**
     * Score candidates with hybrid approach
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>>
            scoreCandidatesWithHybrid(
                List<ResumeJobMatchingService.CandidateMatch> candidates,
                JobVector job,
                int limit) {

        log.debug("Hybrid scoring for {} candidates", candidates.size());

        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> rankedMatches =
            new ArrayList<>();

        // Extract job keywords for BM25
        Set<String> jobKeywords = extractKeywords(job);

        for (ResumeJobMatchingService.CandidateMatch candidate : candidates) {
            // Semantic score (already calculated)
            double semanticScore = candidate.getSemanticSimilarity();

            // Lexical score (BM25-like)
            double lexicalScore = calculateBM25Score(candidate, jobKeywords);

            // Hybrid score
            double hybridScore = (semanticScore * SEMANTIC_WEIGHT) + (lexicalScore * LEXICAL_WEIGHT);

            Map<String, Double> breakdown = new HashMap<>();
            breakdown.put("semantic", semanticScore);
            breakdown.put("lexical", lexicalScore);
            breakdown.put("hybrid", hybridScore);

            rankedMatches.add(MultiStageRankingService.RankedMatch.<ResumeJobMatchingService.CandidateMatch>builder()
                .match(candidate)
                .finalScore(hybridScore)
                .scoreBreakdown(breakdown)
                .vectorScore(semanticScore)
                .hybridScore(hybridScore)
                .build());
        }

        // Sort by hybrid score and limit
        return rankedMatches.stream()
            .sorted(Comparator.comparingDouble((MultiStageRankingService.RankedMatch<?> rm) -> rm.getFinalScore()).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Score jobs with hybrid approach
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>>
            scoreJobsWithHybrid(
                List<ResumeJobMatchingService.JobMatch> jobs,
                ResumeVector resume,
                int limit) {

        log.debug("Hybrid scoring for {} jobs", jobs.size());

        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> rankedMatches =
            new ArrayList<>();

        // Extract resume keywords for BM25
        Set<String> resumeKeywords = extractKeywords(resume);

        for (ResumeJobMatchingService.JobMatch job : jobs) {
            double semanticScore = job.getSemanticSimilarity();
            double lexicalScore = calculateBM25Score(job, resumeKeywords);
            double hybridScore = (semanticScore * SEMANTIC_WEIGHT) + (lexicalScore * LEXICAL_WEIGHT);

            Map<String, Double> breakdown = new HashMap<>();
            breakdown.put("semantic", semanticScore);
            breakdown.put("lexical", lexicalScore);
            breakdown.put("hybrid", hybridScore);

            rankedMatches.add(MultiStageRankingService.RankedMatch.<ResumeJobMatchingService.JobMatch>builder()
                .match(job)
                .finalScore(hybridScore)
                .scoreBreakdown(breakdown)
                .vectorScore(semanticScore)
                .hybridScore(hybridScore)
                .build());
        }

        return rankedMatches.stream()
            .sorted(Comparator.comparingDouble((MultiStageRankingService.RankedMatch<?> rm) -> rm.getFinalScore()).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Calculate BM25-like score for candidate
     */
    private double calculateBM25Score(ResumeJobMatchingService.CandidateMatch candidate, Set<String> queryKeywords) {
        if (queryKeywords.isEmpty()) {
            return 0.5; // Neutral score
        }

        // Extract candidate keywords
        Set<String> candidateKeywords = new HashSet<>();
        if (candidate.getFullName() != null) {
            Arrays.stream(candidate.getFullName().toLowerCase().split("\\s+"))
                .forEach(candidateKeywords::add);
        }
        if (candidate.getLocation() != null) {
            Arrays.stream(candidate.getLocation().toLowerCase().split("\\s+"))
                .forEach(candidateKeywords::add);
        }

        // Calculate keyword overlap
        long matchCount = queryKeywords.stream()
            .filter(candidateKeywords::contains)
            .count();

        // BM25-like scoring (simplified)
        double k1 = 1.5;  // Term frequency saturation parameter
        double b = 0.75;  // Length normalization parameter

        double avgDocLength = 10.0;  // Average document length
        double docLength = candidateKeywords.size();

        double score = 0.0;
        for (String keyword : queryKeywords) {
            if (candidateKeywords.contains(keyword)) {
                double tf = 1.0;  // Term frequency (simplified)
                double idf = Math.log((1000.0 + 1) / (10.0 + 0.5)); // Simplified IDF

                double numerator = tf * (k1 + 1);
                double denominator = tf + k1 * (1 - b + b * (docLength / avgDocLength));

                score += idf * (numerator / denominator);
            }
        }

        // Normalize to 0-1 range
        return Math.min(1.0, score / queryKeywords.size());
    }

    /**
     * Calculate BM25-like score for job
     */
    private double calculateBM25Score(ResumeJobMatchingService.JobMatch job, Set<String> queryKeywords) {
        if (queryKeywords.isEmpty()) {
            return 0.5;
        }

        Set<String> jobKeywords = new HashSet<>();
        if (job.getJobTitle() != null) {
            Arrays.stream(job.getJobTitle().toLowerCase().split("\\s+"))
                .forEach(jobKeywords::add);
        }
        if (job.getCompanyName() != null) {
            Arrays.stream(job.getCompanyName().toLowerCase().split("\\s+"))
                .forEach(jobKeywords::add);
        }
        if (job.getLocation() != null) {
            Arrays.stream(job.getLocation().toLowerCase().split("\\s+"))
                .forEach(jobKeywords::add);
        }

        long matchCount = queryKeywords.stream()
            .filter(jobKeywords::contains)
            .count();

        if (matchCount == 0) {
            return 0.3; // Low score for no keyword matches
        }

        // Simplified BM25 calculation
        return Math.min(1.0, (double) matchCount / queryKeywords.size());
    }

    /**
     * Extract keywords from job vector
     */
    private Set<String> extractKeywords(JobVector job) {
        Set<String> keywords = new HashSet<>();

        if (job.getTitle() != null) {
            Arrays.stream(job.getTitle().toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2)
                .forEach(keywords::add);
        }

        if (job.getRequiredSkills() != null) {
            Arrays.stream(job.getRequiredSkills())
                .map(String::toLowerCase)
                .forEach(keywords::add);
        }

        if (job.getLocation() != null) {
            Arrays.stream(job.getLocation().toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2)
                .forEach(keywords::add);
        }

        return keywords;
    }

    /**
     * Extract keywords from resume vector
     */
    private Set<String> extractKeywords(ResumeVector resume) {
        Set<String> keywords = new HashSet<>();

        if (resume.getTopSkills() != null) {
            Arrays.stream(resume.getTopSkills())
                .map(String::toLowerCase)
                .forEach(keywords::add);
        }

        if (resume.getLocation() != null) {
            Arrays.stream(resume.getLocation().toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2)
                .forEach(keywords::add);
        }

        return keywords;
    }
}
