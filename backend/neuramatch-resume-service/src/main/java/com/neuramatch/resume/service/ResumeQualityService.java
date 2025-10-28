package com.neuramatch.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ResumeQualityService {

    /**
     * Calculate overall resume quality score (0-100)
     */
    public int calculateQualityScore(String content, Map<String, Object> structuredInfo) {
        int score = 0;
        int maxScore = 100;

        // Completeness (40 points)
        score += calculateCompletenessScore(content, structuredInfo);

        // Length and clarity (30 points)
        score += calculateLengthScore(content);

        // Format and structure (20 points)
        score += calculateStructureScore(content);

        // Recency (10 points)
        score += calculateRecencyScore(content);

        log.info("Calculated resume quality score: {}/100", score);
        return Math.min(score, maxScore);
    }

    /**
     * Generate suggestions for improving resume quality
     */
    public List<String> generateSuggestions(String content, Map<String, Object> structuredInfo, int qualityScore) {
        List<String> suggestions = new ArrayList<>();

        // Check completeness
        if (!(Boolean) structuredInfo.getOrDefault("hasEmail", false)) {
            suggestions.add("Add a valid email address");
        }
        if (!(Boolean) structuredInfo.getOrDefault("hasPhone", false)) {
            suggestions.add("Add a phone number");
        }
        if (!(Boolean) structuredInfo.getOrDefault("hasLinkedIn", false)) {
            suggestions.add("Include your LinkedIn profile URL");
        }

        // Check length
        int wordCount = (Integer) structuredInfo.getOrDefault("wordCount", 0);
        if (wordCount < 200) {
            suggestions.add("Resume is too short. Add more details about your experience and skills");
        } else if (wordCount > 1000) {
            suggestions.add("Resume is too long. Consider condensing to 1-2 pages");
        }

        // Check sections
        if (!content.toLowerCase().contains("experience") && !content.toLowerCase().contains("work history")) {
            suggestions.add("Add a work experience section");
        }
        if (!content.toLowerCase().contains("education")) {
            suggestions.add("Add an education section");
        }
        if (!content.toLowerCase().contains("skill")) {
            suggestions.add("Add a skills section");
        }

        // Overall score guidance
        if (qualityScore < 50) {
            suggestions.add("Resume needs significant improvement. Consider using a professional template");
        } else if (qualityScore < 70) {
            suggestions.add("Resume is acceptable but could be enhanced with more details");
        } else if (qualityScore >= 85) {
            suggestions.add("Excellent resume! Well-structured and comprehensive");
        }

        return suggestions;
    }

    private int calculateCompletenessScore(String content, Map<String, Object> structuredInfo) {
        int score = 0;

        // Contact information (15 points)
        if ((Boolean) structuredInfo.getOrDefault("hasEmail", false)) score += 5;
        if ((Boolean) structuredInfo.getOrDefault("hasPhone", false)) score += 5;
        if ((Boolean) structuredInfo.getOrDefault("hasLinkedIn", false)) score += 5;

        // Required sections (25 points)
        String lowerContent = content.toLowerCase();
        if (lowerContent.contains("experience") || lowerContent.contains("work")) score += 10;
        if (lowerContent.contains("education")) score += 8;
        if (lowerContent.contains("skill")) score += 7;

        return score;
    }

    private int calculateLengthScore(String content) {
        int wordCount = content.split("\\s+").length;

        // Optimal word count: 300-800 words
        if (wordCount >= 300 && wordCount <= 800) {
            return 30;
        } else if (wordCount >= 200 && wordCount <= 1000) {
            return 20;
        } else if (wordCount >= 100 && wordCount <= 1500) {
            return 10;
        } else {
            return 5;
        }
    }

    private int calculateStructureScore(String content) {
        int score = 0;

        // Check for common section headers
        String lowerContent = content.toLowerCase();
        if (lowerContent.matches(".*\\n\\s*(professional )?summary.*")) score += 5;
        if (lowerContent.matches(".*\\n\\s*(work )?experience.*")) score += 5;
        if (lowerContent.matches(".*\\n\\s*education.*")) score += 5;
        if (lowerContent.matches(".*\\n\\s*skills.*")) score += 5;

        return score;
    }

    private int calculateRecencyScore(String content) {
        // Check for recent years (2023, 2024, 2025)
        String currentYear = String.valueOf(java.time.Year.now().getValue());
        String lastYear = String.valueOf(java.time.Year.now().getValue() - 1);
        String twoYearsAgo = String.valueOf(java.time.Year.now().getValue() - 2);

        if (content.contains(currentYear)) return 10;
        if (content.contains(lastYear)) return 8;
        if (content.contains(twoYearsAgo)) return 6;

        return 3;
    }

    /**
     * Analyze specific aspects of resume quality
     */
    public Map<String, Object> analyzeQualityMetrics(String content, Map<String, Object> structuredInfo) {
        return Map.of(
            "completeness", calculateCompletenessScore(content, structuredInfo),
            "length", calculateLengthScore(content),
            "structure", calculateStructureScore(content),
            "recency", calculateRecencyScore(content),
            "wordCount", structuredInfo.getOrDefault("wordCount", 0),
            "hasContactInfo", (Boolean) structuredInfo.getOrDefault("hasEmail", false) &&
                             (Boolean) structuredInfo.getOrDefault("hasPhone", false)
        );
    }
}
