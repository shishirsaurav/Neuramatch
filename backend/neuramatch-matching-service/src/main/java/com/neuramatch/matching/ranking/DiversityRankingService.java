package com.neuramatch.matching.ranking;

import com.neuramatch.matching.search.ResumeJobMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Diversity re-ranking to ensure diverse candidate/job pools
 * Prevents result sets dominated by similar profiles from same companies/locations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiversityRankingService {

    /**
     * Diversify candidate results using Maximal Marginal Relevance (MMR)
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>>
            diversifyResults(
                List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> candidates,
                MultiStageRankingService.DiversityConfig config,
                int limit) {

        if (candidates.size() <= limit) {
            return candidates; // Already diverse enough
        }

        log.debug("Diversifying {} candidates to {} with diversity weight {}",
            candidates.size(), limit, config.getDiversityWeight());

        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> selected =
            new ArrayList<>();
        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> remaining =
            new ArrayList<>(candidates);

        // Select first candidate (highest score)
        if (!remaining.isEmpty()) {
            selected.add(remaining.remove(0));
        }

        // Iteratively select diverse candidates
        while (selected.size() < limit && !remaining.isEmpty()) {
            MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch> best = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch> candidate : remaining) {
                // Calculate MMR score
                double relevance = candidate.getFinalScore();
                double diversity = calculateDiversityScore(candidate, selected, config);

                double mmrScore = (1 - config.getDiversityWeight()) * relevance +
                                 config.getDiversityWeight() * diversity;

                if (mmrScore > bestScore) {
                    bestScore = mmrScore;
                    best = candidate;
                }
            }

            if (best != null) {
                best.setDiversityScore(bestScore);
                best.setFinalScore(bestScore); // Update final score with diversity
                selected.add(best);
                remaining.remove(best);
            } else {
                break;
            }
        }

        // Update ranks
        for (int i = 0; i < selected.size(); i++) {
            selected.get(i).setRank(i + 1);
        }

        log.debug("Selected {} diverse candidates", selected.size());
        return selected;
    }

    /**
     * Diversify job results
     */
    public List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>>
            diversifyJobResults(
                List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> jobs,
                MultiStageRankingService.DiversityConfig config,
                int limit) {

        if (jobs.size() <= limit) {
            return jobs;
        }

        log.debug("Diversifying {} jobs to {} with diversity weight {}",
            jobs.size(), limit, config.getDiversityWeight());

        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> selected = new ArrayList<>();
        List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> remaining = new ArrayList<>(jobs);

        if (!remaining.isEmpty()) {
            selected.add(remaining.remove(0));
        }

        while (selected.size() < limit && !remaining.isEmpty()) {
            MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch> best = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch> job : remaining) {
                double relevance = job.getFinalScore();
                double diversity = calculateJobDiversityScore(job, selected, config);

                double mmrScore = (1 - config.getDiversityWeight()) * relevance +
                                 config.getDiversityWeight() * diversity;

                if (mmrScore > bestScore) {
                    bestScore = mmrScore;
                    best = job;
                }
            }

            if (best != null) {
                best.setDiversityScore(bestScore);
                best.setFinalScore(bestScore);
                selected.add(best);
                remaining.remove(best);
            } else {
                break;
            }
        }

        for (int i = 0; i < selected.size(); i++) {
            selected.get(i).setRank(i + 1);
        }

        log.debug("Selected {} diverse jobs", selected.size());
        return selected;
    }

    /**
     * Calculate diversity score for a candidate relative to already selected candidates
     */
    private double calculateDiversityScore(
            MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch> candidate,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> selected,
            MultiStageRankingService.DiversityConfig config) {

        if (selected.isEmpty()) {
            return 1.0; // First candidate is maximally diverse
        }

        ResumeJobMatchingService.CandidateMatch candidateMatch = candidate.getMatch();

        // Calculate diversity on multiple dimensions
        double locationDiversity = calculateLocationDiversity(candidateMatch, selected);
        double experienceDiversity = calculateExperienceDiversity(candidateMatch, selected);

        // Weighted combination
        return (locationDiversity * config.getLocationDiversityWeight()) +
               (experienceDiversity * (1 - config.getLocationDiversityWeight()));
    }

    /**
     * Calculate diversity score for a job relative to already selected jobs
     */
    private double calculateJobDiversityScore(
            MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch> job,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> selected,
            MultiStageRankingService.DiversityConfig config) {

        if (selected.isEmpty()) {
            return 1.0;
        }

        ResumeJobMatchingService.JobMatch jobMatch = job.getMatch();

        double companyDiversity = calculateCompanyDiversity(jobMatch, selected);
        double locationDiversity = calculateJobLocationDiversity(jobMatch, selected);
        double remoteDiversity = calculateRemoteDiversity(jobMatch, selected);

        return (companyDiversity * config.getCompanyDiversityWeight()) +
               (locationDiversity * config.getLocationDiversityWeight()) +
               (remoteDiversity * (1 - config.getCompanyDiversityWeight() - config.getLocationDiversityWeight()));
    }

    /**
     * Calculate location diversity for candidates
     */
    private double calculateLocationDiversity(
            ResumeJobMatchingService.CandidateMatch candidate,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> selected) {

        String candidateLocation = candidate.getLocation();
        if (candidateLocation == null) {
            return 0.5; // Neutral
        }

        long sameLocationCount = selected.stream()
            .map(r -> r.getMatch().getLocation())
            .filter(loc -> candidateLocation.equalsIgnoreCase(loc))
            .count();

        // Higher diversity if fewer candidates from same location
        return 1.0 - ((double) sameLocationCount / selected.size());
    }

    /**
     * Calculate experience diversity for candidates
     */
    private double calculateExperienceDiversity(
            ResumeJobMatchingService.CandidateMatch candidate,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.CandidateMatch>> selected) {

        Integer candidateExp = candidate.getYearsOfExperience();
        if (candidateExp == null) {
            return 0.5;
        }

        // Calculate average experience of selected candidates
        double avgExperience = selected.stream()
            .map(r -> r.getMatch().getYearsOfExperience())
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(candidateExp);

        // Higher diversity if experience is different from average
        double difference = Math.abs(candidateExp - avgExperience);
        return Math.min(1.0, difference / 10.0); // Normalize
    }

    /**
     * Calculate company diversity for jobs
     */
    private double calculateCompanyDiversity(
            ResumeJobMatchingService.JobMatch job,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> selected) {

        String company = job.getCompanyName();
        if (company == null) {
            return 0.5;
        }

        long sameCompanyCount = selected.stream()
            .map(r -> r.getMatch().getCompanyName())
            .filter(c -> company.equalsIgnoreCase(c))
            .count();

        // Penalize if too many jobs from same company
        if (sameCompanyCount >= 2) {
            return 0.2; // Low diversity
        } else if (sameCompanyCount == 1) {
            return 0.6; // Medium diversity
        } else {
            return 1.0; // High diversity (first from this company)
        }
    }

    /**
     * Calculate location diversity for jobs
     */
    private double calculateJobLocationDiversity(
            ResumeJobMatchingService.JobMatch job,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> selected) {

        String location = job.getLocation();
        if (location == null) {
            return 0.5;
        }

        long sameLocationCount = selected.stream()
            .map(r -> r.getMatch().getLocation())
            .filter(loc -> location.equalsIgnoreCase(loc))
            .count();

        return 1.0 - ((double) sameLocationCount / selected.size());
    }

    /**
     * Calculate remote type diversity for jobs
     */
    private double calculateRemoteDiversity(
            ResumeJobMatchingService.JobMatch job,
            List<MultiStageRankingService.RankedMatch<ResumeJobMatchingService.JobMatch>> selected) {

        String remoteType = job.getRemoteType();
        if (remoteType == null) {
            return 0.5;
        }

        long sameRemoteCount = selected.stream()
            .map(r -> r.getMatch().getRemoteType())
            .filter(r -> remoteType.equalsIgnoreCase(r))
            .count();

        return 1.0 - ((double) sameRemoteCount / selected.size());
    }
}
