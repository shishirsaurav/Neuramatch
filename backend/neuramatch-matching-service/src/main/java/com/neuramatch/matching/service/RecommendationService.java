package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.JobRecommendationDTO;
import com.neuramatch.matching.dto.SkillRecommendationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final SkillGraphService skillGraphService;

    /**
     * Get personalized job recommendations for a candidate
     */
    public List<JobRecommendationDTO> getJobRecommendations(
            Long resumeId,
            List<String> candidateSkills,
            Integer experience,
            String location) {

        log.info("Generating job recommendations for resume={}", resumeId);

        List<JobRecommendationDTO> recommendations = new ArrayList<>();

        // 1. Jobs matching current skills (high confidence)
        recommendations.addAll(generateMatchingJobs(candidateSkills, experience, "CURRENT_SKILLS"));

        // 2. Jobs with one skill gap (growth opportunity)
        recommendations.addAll(generateStretchJobs(candidateSkills, experience, "GROWTH_OPPORTUNITY"));

        // 3. Jobs in trending technologies candidate knows
        recommendations.addAll(generateTrendingJobs(candidateSkills, "TRENDING"));

        // Sort by score and return top 10
        return recommendations.stream()
                .sorted(Comparator.comparing(JobRecommendationDTO::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get skill evolution recommendations for career growth
     */
    public List<SkillRecommendationDTO> getSkillEvolutionRecommendations(
            List<String> currentSkills,
            String careerGoal) {

        log.info("Generating skill evolution recommendations for goal: {}", careerGoal);

        List<SkillRecommendationDTO> recommendations = new ArrayList<>();

        // 1. Complementary skills (build on what they know)
        recommendations.addAll(getComplementarySkills(currentSkills));

        // 2. Trending/emerging skills in their domain
        recommendations.addAll(getTrendingSkills(currentSkills));

        // 3. High-value skills (salary impact)
        recommendations.addAll(getHighValueSkills(currentSkills));

        // 4. Next-level skills (for career advancement)
        recommendations.addAll(getAdvancementSkills(currentSkills, careerGoal));

        return recommendations.stream()
                .sorted(Comparator.comparing(SkillRecommendationDTO::getPriority).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get job optimization recommendations for recruiters
     */
    public Map<String, Object> getJobOptimizationRecommendations(
            Long jobId,
            Map<String, Object> jobData) {

        log.info("Generating job optimization recommendations for job={}", jobId);

        Map<String, Object> recommendations = new HashMap<>();

        // Analyze current job performance
        @SuppressWarnings("unchecked")
        List<String> requiredSkills = (List<String>) jobData.get("requiredSkills");
        Integer minExperience = (Integer) jobData.get("minExperience");
        Integer maxExperience = (Integer) jobData.get("maxExperience");

        List<String> optimizations = new ArrayList<>();

        // 1. Skill requirements optimization
        if (requiredSkills.size() > 8) {
            optimizations.add("REDUCE_SKILLS: Job has " + requiredSkills.size() +
                    " required skills. Consider reducing to 5-7 core skills to increase candidate pool.");
        }

        // 2. Experience range optimization
        if (maxExperience != null && minExperience != null) {
            int range = maxExperience - minExperience;
            if (range < 2) {
                optimizations.add("WIDEN_EXPERIENCE_RANGE: Experience range is narrow (" + range +
                        " years). Consider widening to 3-5 years for better candidates.");
            } else if (range > 8) {
                optimizations.add("NARROW_EXPERIENCE_RANGE: Experience range is too wide (" + range +
                        " years). Consider narrowing for better targeting.");
            }
        }

        // 3. Skill alternatives
        List<Map<String, Object>> skillAlternatives = new ArrayList<>();
        for (String skill : requiredSkills.stream().limit(3).collect(Collectors.toList())) {
            List<String> alternatives = skillGraphService.findAlternatives(skill, 0.75);
            if (!alternatives.isEmpty()) {
                Map<String, Object> alt = new HashMap<>();
                alt.put("skill", skill);
                alt.put("alternatives", alternatives);
                alt.put("impact", "Adding alternatives could increase candidate pool by 30-50%");
                skillAlternatives.add(alt);
            }
        }

        // 4. Market insights
        recommendations.put("optimizations", optimizations);
        recommendations.put("skillAlternatives", skillAlternatives);
        recommendations.put("estimatedCandidateIncrease", calculateCandidateIncrease(optimizations.size()));
        recommendations.put("competitionLevel", assessCompetition(requiredSkills, minExperience));

        return recommendations;
    }

    // Helper methods
    private List<JobRecommendationDTO> generateMatchingJobs(
            List<String> skills, Integer experience, String reason) {

        // Mock data - would query actual job database
        return Arrays.asList(
                JobRecommendationDTO.builder()
                        .jobId(101L)
                        .jobTitle("Senior Java Developer")
                        .companyName("Tech Corp")
                        .location("San Francisco")
                        .matchScore(92.0)
                        .reason(reason)
                        .skillMatch(95.0)
                        .experienceMatch(90.0)
                        .score(92.0)
                        .build()
        );
    }

    private List<JobRecommendationDTO> generateStretchJobs(
            List<String> skills, Integer experience, String reason) {

        return Arrays.asList(
                JobRecommendationDTO.builder()
                        .jobId(102L)
                        .jobTitle("Tech Lead")
                        .companyName("Startup Inc")
                        .location("Remote")
                        .matchScore(75.0)
                        .reason(reason)
                        .skillMatch(70.0)
                        .experienceMatch(80.0)
                        .score(75.0)
                        .missingSkills(Arrays.asList("Kubernetes", "Terraform"))
                        .build()
        );
    }

    private List<JobRecommendationDTO> generateTrendingJobs(
            List<String> skills, String reason) {

        return Arrays.asList(
                JobRecommendationDTO.builder()
                        .jobId(103L)
                        .jobTitle("Cloud Engineer")
                        .companyName("Cloud Solutions")
                        .location("Austin, TX")
                        .matchScore(80.0)
                        .reason(reason)
                        .skillMatch(85.0)
                        .experienceMatch(75.0)
                        .score(80.0)
                        .build()
        );
    }

    private List<SkillRecommendationDTO> getComplementarySkills(List<String> currentSkills) {
        return Arrays.asList(
                SkillRecommendationDTO.builder()
                        .skillName("Docker")
                        .reason("COMPLEMENTARY")
                        .description("Commonly used with your current tech stack")
                        .difficulty("EASY")
                        .learningTime("2-3 weeks")
                        .salaryImpact("+$5,000")
                        .demandTrend("HIGH")
                        .priority(90)
                        .relatedSkills(Arrays.asList("Kubernetes", "CI/CD"))
                        .build()
        );
    }

    private List<SkillRecommendationDTO> getTrendingSkills(List<String> currentSkills) {
        return Arrays.asList(
                SkillRecommendationDTO.builder()
                        .skillName("Rust")
                        .reason("TRENDING")
                        .description("Fast-growing language with 50% YoY growth")
                        .difficulty("MEDIUM")
                        .learningTime("2-3 months")
                        .salaryImpact("+$15,000")
                        .demandTrend("VERY_HIGH")
                        .priority(85)
                        .build()
        );
    }

    private List<SkillRecommendationDTO> getHighValueSkills(List<String> currentSkills) {
        return Arrays.asList(
                SkillRecommendationDTO.builder()
                        .skillName("Kubernetes")
                        .reason("HIGH_SALARY_IMPACT")
                        .description("High demand skill with significant salary premium")
                        .difficulty("MEDIUM")
                        .learningTime("1-2 months")
                        .salaryImpact("+$20,000")
                        .demandTrend("HIGH")
                        .priority(95)
                        .build()
        );
    }

    private List<SkillRecommendationDTO> getAdvancementSkills(
            List<String> currentSkills, String careerGoal) {

        return Arrays.asList(
                SkillRecommendationDTO.builder()
                        .skillName("System Design")
                        .reason("CAREER_ADVANCEMENT")
                        .description("Essential for senior/lead roles")
                        .difficulty("HARD")
                        .learningTime("6+ months")
                        .salaryImpact("+$30,000")
                        .demandTrend("HIGH")
                        .priority(88)
                        .build()
        );
    }

    private String calculateCandidateIncrease(int optimizationCount) {
        if (optimizationCount >= 3) return "50-70%";
        if (optimizationCount >= 2) return "30-50%";
        if (optimizationCount >= 1) return "15-30%";
        return "0-15%";
    }

    private String assessCompetition(List<String> skills, Integer experience) {
        // Simple heuristic - would use actual market data
        if (skills.size() > 8 && experience > 7) return "VERY_HIGH";
        if (skills.size() > 5 && experience > 5) return "HIGH";
        if (skills.size() > 3) return "MEDIUM";
        return "LOW";
    }
}
