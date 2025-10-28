package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.*;
import com.neuramatch.matching.search.ResumeJobMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExplainabilityService {

    private final SkillGraphService skillGraphService;
    private final SkillEnrichmentService skillEnrichmentService;

    public MatchExplanationDTO explainMatch(
            ResumeJobMatchingService.JobMatch jobMatch,
            List<String> candidateSkills,
            List<String> requiredSkills,
            Integer candidateExperience,
            Integer minRequiredExperience) {

        log.debug("Generating match explanation for job={}, resume={}",
                jobMatch.getJobId(), jobMatch.getResumeId());

        // Calculate detailed score breakdown
        Map<String, Double> breakdown = calculateScoreBreakdown(jobMatch);

        // Generate key strengths
        List<String> keyStrengths = generateKeyStrengths(jobMatch, breakdown);

        // Identify concerns
        List<String> concerns = identifyConcerns(jobMatch, breakdown);

        // Analyze skill gaps
        SkillGapAnalysisDTO skillAnalysis = analyzeSkillGaps(
                candidateSkills, requiredSkills);

        // Analyze experience
        ExperienceAnalysisDTO experienceAnalysis = analyzeExperience(
                candidateExperience, minRequiredExperience, jobMatch);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(
                jobMatch, skillAnalysis, experienceAnalysis);

        // Determine confidence level
        String confidence = calculateConfidence(jobMatch.getOverallScore(), breakdown);

        // Determine match level
        String matchLevel = getMatchLevel(jobMatch.getOverallScore());

        return MatchExplanationDTO.builder()
                .jobId(jobMatch.getJobId())
                .resumeId(jobMatch.getResumeId())
                .candidateName(jobMatch.getCandidateName())
                .jobTitle(jobMatch.getJobTitle())
                .companyName(jobMatch.getCompanyName())
                .overallScore(jobMatch.getOverallScore())
                .matchLevel(matchLevel)
                .confidence(confidence)
                .scoreBreakdown(breakdown)
                .keyStrengths(keyStrengths)
                .concerns(concerns)
                .skillAnalysis(skillAnalysis)
                .experienceAnalysis(experienceAnalysis)
                .recommendations(recommendations)
                .technicalSkillScore(jobMatch.getSkillMatchScore() * 100)
                .experienceScore(jobMatch.getExperienceMatchScore() * 100)
                .educationScore(calculateEducationScore(jobMatch))
                .domainExpertiseScore(calculateDomainScore(jobMatch))
                .culturalFitScore(jobMatch.getLocationMatchScore() * 100)
                .recencyScore(calculateRecencyScore(jobMatch))
                .build();
    }

    private Map<String, Double> calculateScoreBreakdown(ResumeJobMatchingService.JobMatch match) {
        Map<String, Double> breakdown = new LinkedHashMap<>();

        breakdown.put("technical_skills", match.getSkillMatchScore() * 100);
        breakdown.put("experience_level", match.getExperienceMatchScore() * 100);
        breakdown.put("education", calculateEducationScore(match));
        breakdown.put("domain_expertise", calculateDomainScore(match));
        breakdown.put("cultural_fit", match.getLocationMatchScore() * 100);
        breakdown.put("recency", calculateRecencyScore(match));

        return breakdown;
    }

    private List<String> generateKeyStrengths(
            ResumeJobMatchingService.JobMatch match,
            Map<String, Double> breakdown) {

        List<String> strengths = new ArrayList<>();

        // Technical skills strength
        if (match.getSkillMatchScore() >= 0.9) {
            strengths.add(String.format(
                    "Exceptional skill match (%.0f%% coverage) - candidate has all required technical skills",
                    match.getSkillMatchScore() * 100));
        } else if (match.getSkillMatchScore() >= 0.75) {
            strengths.add(String.format(
                    "Strong technical skills (%.0f%% match) with core competencies covered",
                    match.getSkillMatchScore() * 100));
        }

        // Experience strength
        if (match.getExperienceMatchScore() >= 0.95) {
            strengths.add(String.format(
                    "Perfect experience match - %d years aligns ideally with requirements",
                    match.getYearsOfExperience()));
        } else if (match.getExperienceMatchScore() >= 0.8) {
            strengths.add(String.format(
                    "Strong experience background with %d years in relevant roles",
                    match.getYearsOfExperience()));
        }

        // Semantic similarity strength
        if (match.getSemanticSimilarity() >= 0.85) {
            strengths.add(String.format(
                    "Excellent overall profile match (%.0f%% semantic similarity) - candidate's background aligns closely with job requirements",
                    match.getSemanticSimilarity() * 100));
        }

        // Location/Remote strength
        if (match.getLocationMatchScore() >= 0.9) {
            strengths.add("Location preferences perfectly aligned - no relocation or remote work concerns");
        }

        // Education if available
        double eduScore = calculateEducationScore(match);
        if (eduScore >= 90) {
            strengths.add("Education background exceeds requirements");
        }

        return strengths.stream().limit(5).collect(Collectors.toList());
    }

    private List<String> identifyConcerns(
            ResumeJobMatchingService.JobMatch match,
            Map<String, Double> breakdown) {

        List<String> concerns = new ArrayList<>();

        // Skill gaps
        if (match.getSkillMatchScore() < 0.7) {
            concerns.add(String.format(
                    "Significant skill gaps (%.0f%% coverage) - multiple required skills missing",
                    match.getSkillMatchScore() * 100));
        } else if (match.getSkillMatchScore() < 0.8) {
            concerns.add("Some key technical skills may need development");
        }

        // Experience concerns
        if (match.getExperienceMatchScore() < 0.7) {
            if (match.getYearsOfExperience() < match.getMinYearsRequired()) {
                concerns.add(String.format(
                        "Under-qualified: %d years experience vs %d years required",
                        match.getYearsOfExperience(), match.getMinYearsRequired()));
            } else {
                concerns.add("May be over-qualified for this position");
            }
        }

        // Location concerns
        if (match.getLocationMatchScore() < 0.6) {
            concerns.add("Location preferences may pose challenges - discuss remote/relocation options");
        }

        // Overall score concern
        if (match.getOverallScore() < 70) {
            concerns.add("Overall match score is below recommended threshold - carefully evaluate fit");
        }

        return concerns;
    }

    private SkillGapAnalysisDTO analyzeSkillGaps(
            List<String> candidateSkills,
            List<String> requiredSkills) {

        Set<String> candidateSet = new HashSet<>(candidateSkills);
        List<SkillGapDTO> criticalGaps = new ArrayList<>();
        List<SkillGapDTO> transferableSkills = new ArrayList<>();
        List<String> perfectMatches = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {
            if (candidateSet.contains(requiredSkill)) {
                perfectMatches.add(requiredSkill);
            } else {
                // Check for alternatives
                List<String> alternatives = skillGraphService
                        .findAlternatives(requiredSkill, 0.75);

                boolean hasAlternative = candidateSkills.stream()
                        .anyMatch(alternatives::contains);

                if (hasAlternative) {
                    transferableSkills.add(SkillGapDTO.builder()
                            .skillName(requiredSkill)
                            .isRequired(true)
                            .hasAlternative(true)
                            .alternativeSkills(alternatives.stream()
                                    .filter(candidateSet::contains)
                                    .collect(Collectors.toList()))
                            .transferability(0.8)
                            .impactOnScore(-2.0)
                            .severity("LOW")
                            .estimatedLearningTimeMonths(1)
                            .learningPath("Quick upskilling from similar technology")
                            .build());
                } else {
                    criticalGaps.add(SkillGapDTO.builder()
                            .skillName(requiredSkill)
                            .isRequired(true)
                            .hasAlternative(false)
                            .impactOnScore(-10.0)
                            .severity("HIGH")
                            .estimatedLearningTimeMonths(3)
                            .learningPath("Requires dedicated learning period")
                            .recommendedResources(getRecommendedResources(requiredSkill))
                            .build());
                }
            }
        }

        int totalRequired = requiredSkills.size();
        int matched = perfectMatches.size() + transferableSkills.size();
        double coverage = totalRequired > 0 ? (double) matched / totalRequired * 100 : 0;

        return SkillGapAnalysisDTO.builder()
                .criticalGaps(criticalGaps)
                .transferableSkills(transferableSkills)
                .perfectMatches(perfectMatches)
                .totalRequiredSkills(totalRequired)
                .matchedRequiredSkills(matched)
                .coveragePercentage(coverage)
                .estimatedLearningTimeMonths(criticalGaps.size() * 3)
                .impactOnScore(criticalGaps.stream()
                        .mapToDouble(SkillGapDTO::getImpactOnScore)
                        .sum())
                .build();
    }

    private ExperienceAnalysisDTO analyzeExperience(
            Integer candidateYears,
            Integer minRequired,
            ResumeJobMatchingService.JobMatch match) {

        String experienceLevel;
        String explanation;

        if (candidateYears < minRequired) {
            experienceLevel = "UNDER_QUALIFIED";
            explanation = String.format(
                    "Candidate has %d years but position requires minimum %d years. " +
                    "However, strong skills may compensate for experience gap.",
                    candidateYears, minRequired);
        } else if (candidateYears <= minRequired + 3) {
            experienceLevel = "PERFECTLY_MATCHED";
            explanation = String.format(
                    "Candidate's %d years of experience aligns well with the %d-year requirement. " +
                    "Strong fit for career progression.",
                    candidateYears, minRequired);
        } else {
            experienceLevel = "OVER_QUALIFIED";
            explanation = String.format(
                    "Candidate's %d years exceeds requirement by %d years. " +
                    "Excellent experience but verify role meets career expectations.",
                    candidateYears, candidateYears - minRequired);
        }

        return ExperienceAnalysisDTO.builder()
                .requiredMinYears(minRequired)
                .candidateYears(candidateYears)
                .experienceLevel(experienceLevel)
                .experienceScore(match.getExperienceMatchScore() * 100)
                .explanation(explanation)
                .isGoodFit(match.getExperienceMatchScore() >= 0.7)
                .build();
    }

    private List<String> generateRecommendations(
            ResumeJobMatchingService.JobMatch match,
            SkillGapAnalysisDTO skillAnalysis,
            ExperienceAnalysisDTO experienceAnalysis) {

        List<String> recommendations = new ArrayList<>();

        if (match.getOverallScore() >= 85) {
            recommendations.add("HIGHLY RECOMMENDED: Proceed with interview immediately");
        } else if (match.getOverallScore() >= 70) {
            recommendations.add("RECOMMENDED: Strong candidate, schedule interview to assess fit");
        } else if (match.getOverallScore() >= 60) {
            recommendations.add("CONSIDER: Review gaps carefully before proceeding");
        } else {
            recommendations.add("NOT RECOMMENDED: Significant gaps in requirements");
        }

        // Skill-based recommendations
        if (!skillAnalysis.getCriticalGaps().isEmpty()) {
            recommendations.add(String.format(
                    "Address %d critical skill gaps in initial screening",
                    skillAnalysis.getCriticalGaps().size()));
        }

        if (!skillAnalysis.getTransferableSkills().isEmpty()) {
            recommendations.add(String.format(
                    "Candidate has %d transferable skills - discuss learning timeline",
                    skillAnalysis.getTransferableSkills().size()));
        }

        // Experience-based recommendations
        if ("UNDER_QUALIFIED".equals(experienceAnalysis.getExperienceLevel())) {
            recommendations.add("Consider for junior/mid-level variant of role if available");
        } else if ("OVER_QUALIFIED".equals(experienceAnalysis.getExperienceLevel())) {
            recommendations.add("Discuss career goals and growth opportunities to ensure long-term fit");
        }

        return recommendations;
    }

    private String calculateConfidence(Double overallScore, Map<String, Double> breakdown) {
        // High confidence if score is clear-cut and consistent across factors
        double variance = calculateVariance(breakdown.values());

        if (overallScore >= 85 && variance < 100) {
            return "HIGH";
        } else if (overallScore >= 70 && variance < 200) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private String getMatchLevel(Double score) {
        if (score >= 85) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 60) return "FAIR";
        return "POOR";
    }

    private double calculateEducationScore(ResumeJobMatchingService.JobMatch match) {
        // Placeholder - would integrate with education matching logic
        return 80.0;
    }

    private double calculateDomainScore(ResumeJobMatchingService.JobMatch match) {
        // Placeholder - would analyze industry/domain alignment
        return 75.0;
    }

    private double calculateRecencyScore(ResumeJobMatchingService.JobMatch match) {
        // Placeholder - would check how recent skills are used
        return 85.0;
    }

    private double calculateVariance(Collection<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
    }

    private List<String> getRecommendedResources(String skill) {
        // Placeholder - would provide actual learning resources
        return Arrays.asList(
                "Official " + skill + " documentation",
                "Udemy/Coursera courses on " + skill,
                "Hands-on projects and tutorials"
        );
    }
}
