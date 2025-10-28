package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.SalaryBenchmarkDTO;
import com.neuramatch.matching.dto.SkillTrendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    /**
     * Get trending skills analysis
     */
    public Map<String, Object> getTrendingSkills(int months) {
        log.info("Analyzing trending skills for last {} months", months);

        // Mock data - would query from actual database
        List<SkillTrendDTO> trends = generateMockTrends();

        // Sort by growth rate
        List<SkillTrendDTO> topTrending = trends.stream()
                .sorted(Comparator.comparing(SkillTrendDTO::getGrowthRate).reversed())
                .limit(20)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("period", months + " months");
        result.put("topTrending", topTrending);
        result.put("fastestGrowing", topTrending.stream().limit(5).collect(Collectors.toList()));
        result.put("emergingSkills", getEmergingSkills(trends));
        result.put("decliningSkills", getDecliningSkills(trends));
        result.put("generatedAt", LocalDateTime.now());

        return result;
    }

    /**
     * Get salary benchmarks for given criteria
     */
    public SalaryBenchmarkDTO getSalaryBenchmark(
            List<String> skills,
            Integer experience,
            String location,
            String industry) {

        log.info("Calculating salary benchmark: skills={}, experience={}, location={}, industry={}",
                skills.size(), experience, location, industry);

        // Mock calculation - would query actual job database
        List<Integer> salaries = generateMockSalaries(skills, experience);

        // Calculate statistics
        Collections.sort(salaries);
        int min = salaries.get(0);
        int max = salaries.get(salaries.size() - 1);
        int median = salaries.get(salaries.size() / 2);
        double average = salaries.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int percentile25 = salaries.get(salaries.size() / 4);
        int percentile75 = salaries.get(3 * salaries.size() / 4);

        String confidence = salaries.size() >= 50 ? "HIGH" :
                           salaries.size() >= 20 ? "MEDIUM" : "LOW";

        return SalaryBenchmarkDTO.builder()
                .skills(skills)
                .experienceYears(experience)
                .location(location)
                .industry(industry)
                .min(min)
                .max(max)
                .median(median)
                .average((int) average)
                .percentile25(percentile25)
                .percentile75(percentile75)
                .sampleSize(salaries.size())
                .confidence(confidence)
                .currency("USD")
                .period("YEARLY")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get supply/demand ratio for skills
     */
    public Map<String, Object> getSupplyDemandAnalysis(String skill) {
        log.info("Analyzing supply/demand for skill: {}", skill);

        // Mock data - would query actual database
        int jobPostings = 150;  // Jobs requiring this skill
        int candidates = 1200;  // Candidates with this skill
        double ratio = (double) candidates / jobPostings;

        String marketStatus;
        if (ratio > 5) {
            marketStatus = "OVERSUPPLIED";
        } else if (ratio > 2) {
            marketStatus = "BALANCED";
        } else {
            marketStatus = "HIGH_DEMAND";
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("skill", skill);
        analysis.put("jobPostings", jobPostings);
        analysis.put("candidates", candidates);
        analysis.put("supplyDemandRatio", ratio);
        analysis.put("marketStatus", marketStatus);
        analysis.put("averageSalary", 115000);
        analysis.put("salaryTrend", "+5.2%");
        analysis.put("topHiringCompanies", Arrays.asList("Google", "Amazon", "Microsoft"));

        return analysis;
    }

    /**
     * Get hiring funnel analytics
     */
    public Map<String, Object> getHiringFunnelAnalytics(Long jobId) {
        log.info("Analyzing hiring funnel for job: {}", jobId);

        // Mock data - would query from feedback and match tables
        Map<String, Object> funnel = new LinkedHashMap<>();
        funnel.put("viewed", 250);
        funnel.put("shortlisted", 45);
        funnel.put("interviewed", 12);
        funnel.put("offered", 3);
        funnel.put("hired", 1);

        // Calculate conversion rates
        Map<String, Double> conversionRates = new HashMap<>();
        conversionRates.put("view_to_shortlist", 18.0);
        conversionRates.put("shortlist_to_interview", 26.7);
        conversionRates.put("interview_to_offer", 25.0);
        conversionRates.put("offer_to_hire", 33.3);
        conversionRates.put("overall", 0.4);

        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("funnel", funnel);
        result.put("conversionRates", conversionRates);
        result.put("averageTimeToHire", "42 days");
        result.put("dropOffStage", "shortlist_to_interview");

        return result;
    }

    // Helper methods for mock data
    private List<SkillTrendDTO> generateMockTrends() {
        return Arrays.asList(
                SkillTrendDTO.builder()
                        .skillName("Rust")
                        .currentDemand(450)
                        .previousDemand(300)
                        .growthRate(50.0)
                        .averageSalary(135000)
                        .category("PROGRAMMING_LANGUAGE")
                        .build(),
                SkillTrendDTO.builder()
                        .skillName("Kubernetes")
                        .currentDemand(2100)
                        .previousDemand(1500)
                        .growthRate(40.0)
                        .averageSalary(125000)
                        .category("DEVOPS_TOOL")
                        .build(),
                SkillTrendDTO.builder()
                        .skillName("TypeScript")
                        .currentDemand(1800)
                        .previousDemand(1400)
                        .growthRate(28.6)
                        .averageSalary(115000)
                        .category("PROGRAMMING_LANGUAGE")
                        .build(),
                SkillTrendDTO.builder()
                        .skillName("React")
                        .currentDemand(3200)
                        .previousDemand(2800)
                        .growthRate(14.3)
                        .averageSalary(110000)
                        .category("FRAMEWORK")
                        .build(),
                SkillTrendDTO.builder()
                        .skillName("Python")
                        .currentDemand(4500)
                        .previousDemand(4200)
                        .growthRate(7.1)
                        .averageSalary(120000)
                        .category("PROGRAMMING_LANGUAGE")
                        .build()
        );
    }

    private List<SkillTrendDTO> getEmergingSkills(List<SkillTrendDTO> trends) {
        return trends.stream()
                .filter(t -> t.getGrowthRate() > 30.0)
                .collect(Collectors.toList());
    }

    private List<SkillTrendDTO> getDecliningSkills(List<SkillTrendDTO> trends) {
        return trends.stream()
                .filter(t -> t.getGrowthRate() < 0)
                .collect(Collectors.toList());
    }

    private List<Integer> generateMockSalaries(List<String> skills, Integer experience) {
        // Base salary calculation based on experience
        int baseSalary = 50000 + (experience * 10000);

        // Generate sample salaries with variation
        List<Integer> salaries = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < 30; i++) {
            int variation = random.nextInt(20000) - 10000;
            salaries.add(baseSalary + variation);
        }

        return salaries;
    }
}
