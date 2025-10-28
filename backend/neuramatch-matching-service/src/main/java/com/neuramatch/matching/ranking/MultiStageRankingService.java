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
 * Multi-stage ranking pipeline for progressive refinement of search results
 *
 * Stage 1: Vector Search - Fast recall (top 500)
 * Stage 2: Hybrid Scoring - Combine semantic + lexical (top 100)
 * Stage 3: Feature-based Re-ranking - Detailed scoring (top 20)
 * Stage 4: Diversity Re-ranking - Ensure diverse results (final top N)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MultiStageRankingService {

    private final HybridScoringService hybridScoringService;
    private final FeatureBasedRankingService featureRankingService;
    private final DiversityRankingService diversityRankingService;

    /**
     * Execute multi-stage ranking pipeline for job matches
     */
    public List<RankedMatch<ResumeJobMatchingService.CandidateMatch>> rankCandidates(
            List<ResumeJobMatchingService.CandidateMatch> candidates,
            JobVector job,
            RankingConfig config) {

        log.info("Starting multi-stage ranking for {} candidates", candidates.size());

        // Stage 1: Already done - vector search returned top 500

        // Stage 2: Hybrid scoring (semantic + lexical BM25)
        List<RankedMatch<ResumeJobMatchingService.CandidateMatch>> stage2Results =
            hybridScoringService.scoreCandidatesWithHybrid(
                candidates,
                job,
                config.getStage2Limit()
            );

        log.debug("Stage 2 (Hybrid): {} candidates", stage2Results.size());

        // Stage 3: Feature-based re-ranking
        List<RankedMatch<ResumeJobMatchingService.CandidateMatch>> stage3Results =
            featureRankingService.rerankCandidates(
                stage2Results,
                job,
                config.getStage3Limit()
            );

        log.debug("Stage 3 (Feature): {} candidates", stage3Results.size());

        // Stage 4: Diversity re-ranking (optional)
        List<RankedMatch<ResumeJobMatchingService.CandidateMatch>> finalResults;
        if (config.isEnableDiversity()) {
            finalResults = diversityRankingService.diversifyResults(
                stage3Results,
                config.getDiversityConfig(),
                config.getFinalLimit()
            );
            log.debug("Stage 4 (Diversity): {} candidates", finalResults.size());
        } else {
            finalResults = stage3Results.stream()
                .limit(config.getFinalLimit())
                .collect(Collectors.toList());
        }

        log.info("Multi-stage ranking complete: {} final candidates", finalResults.size());

        return finalResults;
    }

    /**
     * Execute multi-stage ranking pipeline for resume matches (jobs for a candidate)
     */
    public List<RankedMatch<ResumeJobMatchingService.JobMatch>> rankJobs(
            List<ResumeJobMatchingService.JobMatch> jobs,
            ResumeVector resume,
            RankingConfig config) {

        log.info("Starting multi-stage ranking for {} jobs", jobs.size());

        // Stage 2: Hybrid scoring
        List<RankedMatch<ResumeJobMatchingService.JobMatch>> stage2Results =
            hybridScoringService.scoreJobsWithHybrid(
                jobs,
                resume,
                config.getStage2Limit()
            );

        log.debug("Stage 2 (Hybrid): {} jobs", stage2Results.size());

        // Stage 3: Feature-based re-ranking
        List<RankedMatch<ResumeJobMatchingService.JobMatch>> stage3Results =
            featureRankingService.rerankJobs(
                stage2Results,
                resume,
                config.getStage3Limit()
            );

        log.debug("Stage 3 (Feature): {} jobs", stage3Results.size());

        // Stage 4: Diversity re-ranking
        List<RankedMatch<ResumeJobMatchingService.JobMatch>> finalResults;
        if (config.isEnableDiversity()) {
            finalResults = diversityRankingService.diversifyJobResults(
                stage3Results,
                config.getDiversityConfig(),
                config.getFinalLimit()
            );
            log.debug("Stage 4 (Diversity): {} jobs", finalResults.size());
        } else {
            finalResults = stage3Results.stream()
                .limit(config.getFinalLimit())
                .collect(Collectors.toList());
        }

        log.info("Multi-stage ranking complete: {} final jobs", finalResults.size());

        return finalResults;
    }

    /**
     * Get default ranking configuration
     */
    public static RankingConfig getDefaultConfig() {
        return RankingConfig.builder()
            .stage2Limit(100)
            .stage3Limit(20)
            .finalLimit(10)
            .enableDiversity(true)
            .diversityConfig(DiversityConfig.builder()
                .diversityWeight(0.3)
                .companyDiversityWeight(0.5)
                .locationDiversityWeight(0.3)
                .skillDiversityWeight(0.2)
                .build())
            .build();
    }

    // ========== DTOs ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RankingConfig {
        private int stage2Limit;      // Top N for hybrid scoring
        private int stage3Limit;      // Top N for feature ranking
        private int finalLimit;       // Final top N results
        private boolean enableDiversity;
        private DiversityConfig diversityConfig;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DiversityConfig {
        private double diversityWeight;        // Overall diversity importance (0-1)
        private double companyDiversityWeight; // Company diversity weight
        private double locationDiversityWeight; // Location diversity weight
        private double skillDiversityWeight;   // Skill diversity weight
    }

    @lombok.Data
    @lombok.Builder
    public static class RankedMatch<T> {
        private T match;
        private double finalScore;
        private Map<String, Double> scoreBreakdown;
        private int rank;

        // Stage scores for analysis
        private double vectorScore;
        private double hybridScore;
        private double featureScore;
        private double diversityScore;
    }
}
