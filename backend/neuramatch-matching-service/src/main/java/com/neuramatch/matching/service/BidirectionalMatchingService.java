package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.BidirectionalScoreDTO;
import com.neuramatch.matching.dto.CareerGoalsDTO;
import com.neuramatch.matching.search.ResumeJobMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BidirectionalMatchingService {

    private final ResumeJobMatchingService matchingService;
    private final TemporalDecayService temporalDecayService;

    /**
     * Calculate bidirectional match score (both job→resume AND resume→job)
     */
    public BidirectionalScoreDTO calculateBidirectionalScore(
            Long jobId,
            Long resumeId) {

        log.debug("Calculating bidirectional score for job={}, resume={}", jobId, resumeId);

        // Direction 1: Job → Resume (Does candidate meet job requirements?)
        double jobToResumeScore = calculateJobToResumeScore(jobId, resumeId);

        // Direction 2: Resume → Job (Does job match candidate's goals?)
        double resumeToJobScore = calculateResumeToJobScore(resumeId, jobId);

        // Calculate harmonic mean (penalizes imbalanced matches)
        double harmonicMean = 0.0;
        if (jobToResumeScore > 0 && resumeToJobScore > 0) {
            harmonicMean = 2.0 * (jobToResumeScore * resumeToJobScore) /
                          (jobToResumeScore + resumeToJobScore);
        }

        // Weighted combination (configurable weights)
        double weightedScore = (0.6 * jobToResumeScore) + (0.4 * resumeToJobScore);

        // Apply temporal decay to skills (recency matters)
        double temporalAdjusted = applyTemporalAdjustment(weightedScore, resumeId);

        return BidirectionalScoreDTO.builder()
                .jobId(jobId)
                .resumeId(resumeId)
                .jobToResumeScore(jobToResumeScore)
                .resumeToJobScore(resumeToJobScore)
                .harmonicMean(harmonicMean)
                .weightedScore(weightedScore)
                .temporalAdjustedScore(temporalAdjusted)
                .finalScore(temporalAdjusted)
                .matchQuality(getMatchQuality(temporalAdjusted))
                .isBalanced(isBalancedMatch(jobToResumeScore, resumeToJobScore))
                .build();
    }

    /**
     * Traditional direction: Job requirements → Candidate qualifications
     */
    private double calculateJobToResumeScore(Long jobId, Long resumeId) {
        // Use existing matching service
        ResumeJobMatchingService.MatchingCriteria criteria =
            ResumeJobMatchingService.MatchingCriteria.builder()
                .limit(1)
                .build();

        List<ResumeJobMatchingService.JobMatch> matches =
            matchingService.findMatchingJobsForResume(resumeId, criteria);

        if (matches.isEmpty()) {
            return 0.0;
        }

        // Get overall score (already considers skills, experience, etc.)
        return matches.get(0).getOverallScore() / 100.0; // Normalize to 0-1
    }

    /**
     * Reverse direction: Candidate goals → Job characteristics
     */
    private double calculateResumeToJobScore(Long resumeId, Long jobId) {
        // Extract career goals from resume
        CareerGoalsDTO goals = extractCareerGoals(resumeId);

        // Score components
        double goalAlignment = scoreGoalAlignment(goals, jobId);
        double growthPotential = scoreGrowthPotential(resumeId, jobId);
        double skillInterest = scoreSkillInterest(resumeId, jobId);
        double cultureMatch = scoreCultureMatch(goals, jobId);

        // Weighted combination
        return (goalAlignment * 0.4) +
               (growthPotential * 0.3) +
               (skillInterest * 0.2) +
               (cultureMatch * 0.1);
    }

    /**
     * Extract career goals from resume (from summary, objectives, etc.)
     */
    private CareerGoalsDTO extractCareerGoals(Long resumeId) {
        // TODO: Integrate with NLP service to extract from resume text
        // For now, return reasonable defaults based on experience level

        return CareerGoalsDTO.builder()
                .desiredRoles(List.of("Software Engineer", "Senior Developer"))
                .desiredIndustries(List.of("Technology", "Finance"))
                .careerStage("MID_LEVEL") // ENTRY, MID, SENIOR, EXECUTIVE
                .preferredCompanySize("MEDIUM") // STARTUP, SMALL, MEDIUM, LARGE
                .remotePreference("HYBRID")
                .prioritiesRanked(List.of("growth", "compensation", "work_life_balance"))
                .willingToLearn(List.of("Kubernetes", "React"))
                .avoidSkills(List.of()) // Technologies to avoid
                .build();
    }

    /**
     * Score how well job aligns with candidate's stated goals
     */
    private double scoreGoalAlignment(CareerGoalsDTO goals, Long jobId) {
        double score = 0.7; // Base score

        // TODO: Check if job title matches desired roles
        // TODO: Check if industry matches
        // TODO: Check if company size matches preference
        // TODO: Check if remote policy matches

        return score;
    }

    /**
     * Assess growth potential this job offers the candidate
     */
    private double scoreGrowthPotential(Long resumeId, Long jobId) {
        double score = 0.75; // Base score

        // TODO: Compare candidate's current level to job level
        // TODO: Check for stretch opportunities
        // TODO: Evaluate learning opportunities (new technologies)
        // TODO: Consider career progression path

        return score;
    }

    /**
     * Measure candidate's interest in job's skill stack
     */
    private double scoreSkillInterest(Long resumeId, Long jobId) {
        double score = 0.8; // Base score

        // TODO: Check recency of skill usage (recent = higher interest)
        // TODO: Identify skills candidate is actively learning
        // TODO: Compare with willingness to learn new skills

        return score;
    }

    /**
     * Evaluate culture fit based on preferences
     */
    private double scoreCultureMatch(CareerGoalsDTO goals, Long jobId) {
        double score = 0.7; // Base score

        // TODO: Match work style preferences
        // TODO: Match team size preferences
        // TODO: Match pace preferences (fast-paced vs stable)

        return score;
    }

    /**
     * Apply temporal decay to account for skill recency
     */
    private double applyTemporalAdjustment(double baseScore, Long resumeId) {
        // TODO: Get candidate's skills with last used dates
        // For now, apply modest decay for demonstration
        double decayFactor = 0.95; // 5% decay for average staleness
        return baseScore * decayFactor;
    }

    /**
     * Check if match is balanced in both directions
     */
    private boolean isBalancedMatch(double jobToResume, double resumeToJob) {
        double difference = Math.abs(jobToResume - resumeToJob);
        return difference < 0.2; // Within 20% is considered balanced
    }

    /**
     * Get qualitative match quality
     */
    private String getMatchQuality(double score) {
        if (score >= 0.85) return "EXCELLENT";
        if (score >= 0.70) return "GOOD";
        if (score >= 0.55) return "FAIR";
        return "POOR";
    }

    /**
     * Get detailed match insights
     */
    public Map<String, Object> getMatchInsights(Long jobId, Long resumeId) {
        BidirectionalScoreDTO score = calculateBidirectionalScore(jobId, resumeId);

        Map<String, Object> insights = new HashMap<>();
        insights.put("bidirectionalScore", score);
        insights.put("recommendation", generateRecommendation(score));
        insights.put("concerns", identifyConcerns(score));
        insights.put("strengths", identifyStrengths(score));

        return insights;
    }

    private String generateRecommendation(BidirectionalScoreDTO score) {
        if (score.getFinalScore() >= 0.85) {
            return "HIGHLY RECOMMENDED: Strong mutual fit - proceed with interview";
        } else if (score.getFinalScore() >= 0.70) {
            return "RECOMMENDED: Good match - candidate meets requirements and job aligns with goals";
        } else if (!score.getIsBalanced()) {
            if (score.getJobToResumeScore() > score.getResumeToJobScore()) {
                return "CAUTION: Candidate qualifies but job may not match their career goals";
            } else {
                return "CAUTION: Job aligns with goals but candidate may be under-qualified";
            }
        } else {
            return "NOT RECOMMENDED: Weak match in both directions";
        }
    }

    private List<String> identifyConcerns(BidirectionalScoreDTO score) {
        List<String> concerns = new java.util.ArrayList<>();

        if (score.getJobToResumeScore() < 0.6) {
            concerns.add("Candidate may not fully meet job requirements");
        }

        if (score.getResumeToJobScore() < 0.6) {
            concerns.add("Job may not align with candidate's career goals");
        }

        if (!score.getIsBalanced()) {
            concerns.add("Imbalanced match - one direction much stronger than other");
        }

        if (score.getTemporalAdjustedScore() < score.getWeightedScore() * 0.9) {
            concerns.add("Some skills may be outdated - candidate hasn't used them recently");
        }

        return concerns;
    }

    private List<String> identifyStrengths(BidirectionalScoreDTO score) {
        List<String> strengths = new java.util.ArrayList<>();

        if (score.getJobToResumeScore() >= 0.8) {
            strengths.add("Candidate strongly meets job requirements");
        }

        if (score.getResumeToJobScore() >= 0.8) {
            strengths.add("Job aligns well with candidate's career goals");
        }

        if (score.getIsBalanced()) {
            strengths.add("Balanced match - mutual fit in both directions");
        }

        if (score.getTemporalAdjustedScore() >= score.getWeightedScore() * 0.95) {
            strengths.add("Candidate has recent, relevant experience");
        }

        return strengths;
    }
}
