package com.neuramatch.matching.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TemporalDecayService {

    private static final int HALF_LIFE_MONTHS = 24; // 2 years
    private static final int RECENT_BOOST_MONTHS = 6;
    private static final double RECENT_BOOST_MULTIPLIER = 1.2;

    // Technology lifecycle stages
    public enum TechnologyLifecycle {
        EMERGING(1.2),      // New, hot technologies
        MAINSTREAM(1.0),    // Widely adopted
        MATURE(0.9),        // Stable but aging
        LEGACY(0.7);        // Outdated

        private final double multiplier;

        TechnologyLifecycle(double multiplier) {
            this.multiplier = multiplier;
        }

        public double getMultiplier() {
            return multiplier;
        }
    }

    /**
     * Apply temporal decay to a skill score based on last used date
     */
    public double applyTemporalDecay(
            double baseScore,
            LocalDate lastUsedDate,
            String technology) {

        if (lastUsedDate == null) {
            // No date information, assume moderate decay
            return baseScore * 0.8;
        }

        // Calculate months since last used
        long monthsSinceUsed = ChronoUnit.MONTHS.between(lastUsedDate, LocalDate.now());

        // Apply half-life decay formula: score = baseScore * (0.5) ^ (time / halfLife)
        double decayFactor = Math.pow(0.5, (double) monthsSinceUsed / HALF_LIFE_MONTHS);

        // Apply recent boost for skills used in last 6 months
        if (monthsSinceUsed <= RECENT_BOOST_MONTHS) {
            decayFactor *= RECENT_BOOST_MULTIPLIER;
            decayFactor = Math.min(1.0, decayFactor); // Cap at 1.0
        }

        // Apply technology lifecycle adjustment
        TechnologyLifecycle lifecycle = getTechnologyLifecycle(technology);
        decayFactor *= lifecycle.getMultiplier();

        // Final score with decay applied
        double finalScore = baseScore * decayFactor;

        log.debug("Temporal decay for {}: base={}, months={}, decay={}, lifecycle={}, final={}",
                technology, baseScore, monthsSinceUsed, decayFactor,
                lifecycle, finalScore);

        return finalScore;
    }

    /**
     * Apply temporal decay to multiple skills
     */
    public Map<String, Double> applyTemporalDecayToSkills(
            Map<String, SkillWithDate> skills) {

        Map<String, Double> decayedScores = new HashMap<>();

        for (Map.Entry<String, SkillWithDate> entry : skills.entrySet()) {
            String skillName = entry.getKey();
            SkillWithDate skill = entry.getValue();

            double decayedScore = applyTemporalDecay(
                    skill.getProficiencyScore(),
                    skill.getLastUsedDate(),
                    skillName
            );

            decayedScores.put(skillName, decayedScore);
        }

        return decayedScores;
    }

    /**
     * Calculate weighted average with temporal decay
     */
    public double calculateWeightedAverageWithDecay(
            Map<String, SkillWithDate> skills) {

        if (skills.isEmpty()) {
            return 0.0;
        }

        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (SkillWithDate skill : skills.values()) {
            double decayedScore = applyTemporalDecay(
                    skill.getProficiencyScore(),
                    skill.getLastUsedDate(),
                    skill.getSkillName()
            );

            double weight = skill.getImportance();
            weightedSum += decayedScore * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }

    /**
     * Get technology lifecycle stage
     * In production, this would query from knowledge graph or external API
     */
    private TechnologyLifecycle getTechnologyLifecycle(String technology) {
        // Simplified mapping - would be from database/knowledge graph
        Map<String, TechnologyLifecycle> lifecycleMap = new HashMap<>();

        // Emerging technologies
        lifecycleMap.put("rust", TechnologyLifecycle.EMERGING);
        lifecycleMap.put("go", TechnologyLifecycle.EMERGING);
        lifecycleMap.put("svelte", TechnologyLifecycle.EMERGING);
        lifecycleMap.put("deno", TechnologyLifecycle.EMERGING);

        // Mainstream technologies
        lifecycleMap.put("java", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("python", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("javascript", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("typescript", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("react", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("spring boot", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("docker", TechnologyLifecycle.MAINSTREAM);
        lifecycleMap.put("kubernetes", TechnologyLifecycle.MAINSTREAM);

        // Mature technologies
        lifecycleMap.put("php", TechnologyLifecycle.MATURE);
        lifecycleMap.put("ruby", TechnologyLifecycle.MATURE);
        lifecycleMap.put("jquery", TechnologyLifecycle.MATURE);
        lifecycleMap.put("angular", TechnologyLifecycle.MATURE);

        // Legacy technologies
        lifecycleMap.put("cobol", TechnologyLifecycle.LEGACY);
        lifecycleMap.put("visual basic", TechnologyLifecycle.LEGACY);
        lifecycleMap.put("flash", TechnologyLifecycle.LEGACY);
        lifecycleMap.put("silverlight", TechnologyLifecycle.LEGACY);

        return lifecycleMap.getOrDefault(
                technology.toLowerCase(),
                TechnologyLifecycle.MAINSTREAM
        );
    }

    /**
     * Check if skill is considered "current" (used recently)
     */
    public boolean isSkillCurrent(LocalDate lastUsedDate) {
        if (lastUsedDate == null) {
            return false;
        }

        long monthsSince = ChronoUnit.MONTHS.between(lastUsedDate, LocalDate.now());
        return monthsSince <= 12; // Current if used within last year
    }

    /**
     * Get recency category for a skill
     */
    public String getRecencyCategory(LocalDate lastUsedDate) {
        if (lastUsedDate == null) {
            return "UNKNOWN";
        }

        long monthsSince = ChronoUnit.MONTHS.between(lastUsedDate, LocalDate.now());

        if (monthsSince <= 6) {
            return "VERY_RECENT";
        } else if (monthsSince <= 12) {
            return "RECENT";
        } else if (monthsSince <= 24) {
            return "MODERATE";
        } else if (monthsSince <= 48) {
            return "DATED";
        } else {
            return "OUTDATED";
        }
    }

    // Helper class to represent a skill with temporal information
    public static class SkillWithDate {
        private String skillName;
        private double proficiencyScore; // 0.0 - 1.0
        private LocalDate lastUsedDate;
        private int importance; // 1-10

        public SkillWithDate(String skillName, double proficiencyScore,
                           LocalDate lastUsedDate, int importance) {
            this.skillName = skillName;
            this.proficiencyScore = proficiencyScore;
            this.lastUsedDate = lastUsedDate;
            this.importance = importance;
        }

        // Getters
        public String getSkillName() { return skillName; }
        public double getProficiencyScore() { return proficiencyScore; }
        public LocalDate getLastUsedDate() { return lastUsedDate; }
        public int getImportance() { return importance; }
    }
}
