package com.neuramatch.matching.ranking;

import com.neuramatch.matching.search.ResumeJobMatchingService;
import com.neuramatch.matching.vector.JobVector;
import com.neuramatch.matching.vector.ResumeVector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feature-based re-ranking using hand-crafted features for fine-grained scoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureBasedRankingService {

    /**
     * Re-rank candidates using detailed feature scoring
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>>
            rerankCandidates(
                List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> candidates,
                JobVector job,
                int limit) {

        log.debug("Feature-based re-ranking for {} candidates", candidates.size());

        for (MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch> rankedMatch : candidates) {
            ResumeJobMatchingService.CandidateMatch candidate = rankedMatch.getMatch();

            // Calculate feature scores
            double qualityScore = calculateQualityScore(candidate);
            double recencyScore = calculateRecencyScore(candidate);
            double experienceBoost = calculateExperienceBoost(candidate, job);
            double locationBoost = calculateLocationBoost(candidate, job);

            // Previous hybrid score
            double hybridScore = rankedMatch.getHybridScore();

            // Weighted combination
            double featureScore = hybridScore * 0.5 +              // Base hybrid score (50%)
                                 qualityScore * 0.2 +              // Quality (20%)
                                 recencyScore * 0.15 +             // Recency (15%)
                                 experienceBoost * 0.10 +          // Experience boost (10%)
                                 locationBoost * 0.05;             // Location boost (5%)

            // Update scores
            rankedMatch.setFinalScore(featureScore);
            rankedMatch.setFeatureScore(featureScore);

            Map<String, Double> breakdown = rankedMatch.getScoreBreakdown();
            breakdown.put("quality", qualityScore);
            breakdown.put("recency", recencyScore);
            breakdown.put("experienceBoost", experienceBoost);
            breakdown.put("locationBoost", locationBoost);
            breakdown.put("feature", featureScore);
        }

        // Sort by feature score
        return candidates.stream()
            .sorted(Comparator.comparingDouble((MultiStageRankingService.RankedMatch<?> rm) -> rm.getFinalScore()).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Re-rank jobs using detailed feature scoring
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>>
            rerankJobs(
                List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> jobs,
                ResumeVector resume,
                int limit) {

        log.debug("Feature-based re-ranking for {} jobs", jobs.size());

        for (MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch> rankedMatch : jobs) {
            ResumeJobMatchingService.JobMatch job = rankedMatch.getMatch();

            // Calculate feature scores
            double salaryScore = calculateSalaryScore(job, resume);
            double remoteScore = calculateRemoteMatchScore(job, resume);
            double companyScore = calculateCompanyScore(job);

            double hybridScore = rankedMatch.getHybridScore();

            // Weighted combination
            double featureScore = hybridScore * 0.6 +          // Base hybrid score (60%)
                                 salaryScore * 0.2 +           // Salary match (20%)
                                 remoteScore * 0.15 +          // Remote preference (15%)
                                 companyScore * 0.05;          // Company reputation (5%)

            rankedMatch.setFinalScore(featureScore);
            rankedMatch.setFeatureScore(featureScore);

            Map<String, Double> breakdown = rankedMatch.getScoreBreakdown();
            breakdown.put("salary", salaryScore);
            breakdown.put("remote", remoteScore);
            breakdown.put("company", companyScore);
            breakdown.put("feature", featureScore);
        }

        return jobs.stream()
            .sorted(Comparator.comparingDouble((MultiStageRankingService.RankedMatch<?> rm) -> rm.getFinalScore()).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Calculate quality score based on resume quality
     */
    private double calculateQualityScore(ResumeJobMatchingService.CandidateMatch candidate) {
        Integer qualityScore = candidate.getQualityScore();
        if (qualityScore == null) {
            return 0.5; // Neutral
        }

        // Normalize 0-100 to 0-1
        return qualityScore / 100.0;
    }

    /**
     * Calculate recency score (penalize old profiles)
     */
    private double calculateRecencyScore(ResumeJobMatchingService.CandidateMatch candidate) {
        // In a real implementation, we'd check when the resume was last updated
        // For now, return neutral score
        return 0.8;
    }

    /**
     * Calculate experience boost for overqualified/perfect match candidates
     */
    private double calculateExperienceBoost(ResumeJobMatchingService.CandidateMatch candidate, JobVector job) {
        Integer candidateYears = candidate.getYearsOfExperience();
        Integer minRequired = job.getMinYearsExperience();
        Integer maxRequired = job.getMaxYearsExperience();

        if (candidateYears == null || minRequired == null) {
            return 0.5;
        }

        int min = minRequired;
        int max = maxRequired != null ? maxRequired : minRequired + 5;

        if (candidateYears >= min && candidateYears <= max) {
            return 1.0; // Perfect range
        } else if (candidateYears > max) {
            // Overqualified - slight boost for very experienced candidates
            return 0.9;
        } else {
            // Underqualified - penalize
            double gap = (double) (min - candidateYears) / min;
            return Math.max(0.3, 1.0 - gap);
        }
    }

    /**
     * Calculate location boost
     */
    private double calculateLocationBoost(ResumeJobMatchingService.CandidateMatch candidate, JobVector job) {
        String candidateLocation = candidate.getLocation();
        String candidateRemote = candidate.getRemotePreference();
        String jobLocation = job.getLocation();
        String jobRemote = job.getRemoteType();

        if ("REMOTE".equals(jobRemote) && "REMOTE".equals(candidateRemote)) {
            return 1.0; // Perfect match
        }

        if (candidateLocation != null && jobLocation != null &&
            candidateLocation.equalsIgnoreCase(jobLocation)) {
            return 1.0; // Same location
        }

        if ("HYBRID".equals(candidateRemote) || "HYBRID".equals(jobRemote)) {
            return 0.7; // Flexible
        }

        return 0.5; // Neutral
    }

    /**
     * Calculate salary match score
     */
    private double calculateSalaryScore(ResumeJobMatchingService.JobMatch job, ResumeVector resume) {
        Integer resumeMin = resume.getMinSalary();
        Integer resumeMax = resume.getMaxSalary();
        Integer jobMin = extractSalaryMin(job.getSalaryRange());
        Integer jobMax = extractSalaryMax(job.getSalaryRange());

        if (resumeMin == null || jobMin == null) {
            return 0.7; // Neutral when salary not specified
        }

        // Check if ranges overlap
        if (resumeMin <= jobMax && resumeMax >= jobMin) {
            // Calculate overlap percentage
            int overlapMin = Math.max(resumeMin, jobMin);
            int overlapMax = Math.min(resumeMax, jobMax);
            int overlapSize = overlapMax - overlapMin;
            int candidateRangeSize = resumeMax - resumeMin;

            if (candidateRangeSize == 0) {
                return 1.0;
            }

            return Math.min(1.0, (double) overlapSize / candidateRangeSize);
        }

        // No overlap - penalize based on gap
        if (resumeMin > jobMax) {
            // Candidate expects more - significant penalty
            return 0.3;
        } else {
            // Candidate willing to accept less - small penalty
            return 0.6;
        }
    }

    /**
     * Calculate remote work match score
     */
    private double calculateRemoteMatchScore(ResumeJobMatchingService.JobMatch job, ResumeVector resume) {
        String resumeRemote = resume.getRemotePreference();
        String jobRemote = job.getRemoteType();

        if (resumeRemote == null || jobRemote == null) {
            return 0.7;
        }

        if (resumeRemote.equals(jobRemote)) {
            return 1.0; // Perfect match
        }

        if ("REMOTE".equals(jobRemote)) {
            return 0.9; // Most people like remote
        }

        if ("HYBRID".equals(resumeRemote) || "HYBRID".equals(jobRemote)) {
            return 0.8; // Flexible
        }

        return 0.5; // Mismatch
    }

    /**
     * Calculate company reputation score
     */
    private double calculateCompanyScore(ResumeJobMatchingService.JobMatch job) {
        // In a real implementation, this would look up company ratings
        // For now, return neutral score
        return 0.75;
    }

    /**
     * Extract minimum salary from salary range string
     */
    private Integer extractSalaryMin(String salaryRange) {
        if (salaryRange == null) return null;

        try {
            String[] parts = salaryRange.split("-");
            if (parts.length > 0) {
                String minStr = parts[0].replaceAll("[^0-9]", "");
                return Integer.parseInt(minStr);
            }
        } catch (Exception e) {
            log.warn("Failed to parse salary range: {}", salaryRange);
        }

        return null;
    }

    /**
     * Extract maximum salary from salary range string
     */
    private Integer extractSalaryMax(String salaryRange) {
        if (salaryRange == null) return null;

        try {
            String[] parts = salaryRange.split("-");
            if (parts.length > 1) {
                String maxStr = parts[1].replaceAll("[^0-9]", "");
                return Integer.parseInt(maxStr);
            }
        } catch (Exception e) {
            log.warn("Failed to parse salary range: {}", salaryRange);
        }

        return null;
    }
}
