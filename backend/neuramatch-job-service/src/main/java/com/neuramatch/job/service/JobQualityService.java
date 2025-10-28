package com.neuramatch.job.service;

import com.neuramatch.job.dto.JobQualityAnalysisDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JobQualityService {

    private static final int MIN_WORD_COUNT = 100;
    private static final int OPTIMAL_MIN_WORD_COUNT = 200;
    private static final int OPTIMAL_MAX_WORD_COUNT = 600;
    private static final int MAX_REALISTIC_EXPERIENCE = 15;

    public JobQualityAnalysisDTO analyzeJobQuality(
            String jobTitle,
            String jobDescription,
            String responsibilities,
            String qualifications,
            Integer minYearsExperience,
            Integer maxYearsExperience,
            Integer minSalary,
            Integer maxSalary,
            String applicationUrl
    ) {
        log.debug("Analyzing job quality for: {}", jobTitle);

        Map<String, Integer> breakdown = new HashMap<>();
        List<String> suggestions = new ArrayList<>();

        // Completeness Score (30 points)
        int completenessScore = calculateCompletenessScore(
                jobDescription, responsibilities, qualifications,
                minSalary, maxSalary, applicationUrl, breakdown, suggestions
        );

        // Specificity Score (25 points)
        int specificityScore = calculateSpecificityScore(
                jobDescription, responsibilities, qualifications, breakdown, suggestions
        );

        // Realism Score (25 points)
        int realismScore = calculateRealismScore(
                minYearsExperience, maxYearsExperience, minSalary, maxSalary,
                breakdown, suggestions
        );

        // Clarity Score (20 points)
        int clarityScore = calculateClarityScore(
                jobDescription, breakdown, suggestions
        );

        // Calculate total quality score
        int totalScore = completenessScore + specificityScore + realismScore + clarityScore;

        // Determine quality level
        String qualityLevel = getQualityLevel(totalScore);

        // Add overall feedback
        if (totalScore >= 90) {
            suggestions.add(0, "Excellent job posting! Clear, complete, and professional.");
        } else if (totalScore >= 70) {
            suggestions.add(0, "Good job posting. Consider the suggestions below for improvement.");
        } else if (totalScore >= 50) {
            suggestions.add(0, "Job posting needs improvement. Please review the suggestions.");
        } else {
            suggestions.add(0, "Job posting requires significant improvement before activation.");
        }

        return JobQualityAnalysisDTO.builder()
                .qualityScore(totalScore)
                .qualityLevel(qualityLevel)
                .suggestions(suggestions)
                .breakdown(breakdown)
                .completenessScore(completenessScore)
                .specificityScore(specificityScore)
                .realismScore(realismScore)
                .clarityScore(clarityScore)
                .wordCount(countWords(jobDescription + " " + responsibilities + " " + qualifications))
                .hasSalaryRange(minSalary != null && maxSalary != null)
                .hasResponsibilities(responsibilities != null && !responsibilities.trim().isEmpty())
                .hasQualifications(qualifications != null && !qualifications.trim().isEmpty())
                .hasApplicationProcess(applicationUrl != null && !applicationUrl.trim().isEmpty())
                .experienceRangeSize(calculateExperienceRange(minYearsExperience, maxYearsExperience))
                .hasRealisticRequirements(isRealisticRequirements(minYearsExperience, maxYearsExperience))
                .build();
    }

    private int calculateCompletenessScore(
            String jobDescription, String responsibilities, String qualifications,
            Integer minSalary, Integer maxSalary, String applicationUrl,
            Map<String, Integer> breakdown, List<String> suggestions
    ) {
        int score = 0;

        // Job description (10 points)
        if (jobDescription != null && jobDescription.trim().length() >= 50) {
            score += 10;
        } else {
            suggestions.add("Job description is too short. Provide at least 50 characters of detailed information.");
        }

        // Responsibilities (8 points)
        if (responsibilities != null && responsibilities.trim().length() >= 50) {
            score += 8;
        } else {
            suggestions.add("Add detailed responsibilities section (at least 50 characters).");
        }

        // Qualifications (7 points)
        if (qualifications != null && qualifications.trim().length() >= 30) {
            score += 7;
        } else {
            suggestions.add("Add clear qualifications section outlining required skills and experience.");
        }

        // Salary range (3 points)
        if (minSalary != null && maxSalary != null && maxSalary > minSalary) {
            score += 3;
        } else {
            suggestions.add("Include salary range for transparency and to attract quality candidates.");
        }

        // Application process (2 points)
        if (applicationUrl != null && !applicationUrl.trim().isEmpty()) {
            score += 2;
        } else {
            suggestions.add("Provide application URL or instructions.");
        }

        breakdown.put("completeness", score);
        return score;
    }

    private int calculateSpecificityScore(
            String jobDescription, String responsibilities, String qualifications,
            Map<String, Integer> breakdown, List<String> suggestions
    ) {
        int score = 0;
        String allText = (jobDescription + " " + responsibilities + " " + qualifications).toLowerCase();

        // Check for vague terms (negative scoring)
        List<String> vagueTerms = Arrays.asList(
                "rockstar", "ninja", "guru", "wizard", "unicorn",
                "passionate", "motivated", "dynamic", "innovative"
        );
        long vagueCount = vagueTerms.stream()
                .filter(allText::contains)
                .count();

        if (vagueCount == 0) {
            score += 10; // No vague terms
        } else if (vagueCount <= 2) {
            score += 5; // Few vague terms
            suggestions.add("Reduce vague buzzwords (rockstar, ninja, etc.). Be more specific about requirements.");
        } else {
            suggestions.add("Too many vague buzzwords. Replace with specific, measurable requirements.");
        }

        // Check for specific technical terms (positive scoring)
        boolean hasTechnicalDetails = allText.matches(".*\\b(java|python|javascript|sql|aws|kubernetes|docker|react|angular|spring|django)\\b.*");
        if (hasTechnicalDetails) {
            score += 8; // Has specific technical requirements
        } else {
            score += 3; // Generic posting
            suggestions.add("Add specific technical skills and tools required for the role.");
        }

        // Check for quantifiable requirements
        Pattern numberPattern = Pattern.compile("\\d+\\s*(years?|months?|projects?|\\+)");
        Matcher matcher = numberPattern.matcher(allText);
        int quantifiableCount = 0;
        while (matcher.find()) {
            quantifiableCount++;
        }

        if (quantifiableCount >= 3) {
            score += 7; // Good specificity
        } else if (quantifiableCount >= 1) {
            score += 4;
            suggestions.add("Add more quantifiable requirements (e.g., '3+ years experience', '5-10 person team').");
        } else {
            suggestions.add("Include specific, measurable requirements instead of vague descriptions.");
        }

        breakdown.put("specificity", score);
        return score;
    }

    private int calculateRealismScore(
            Integer minYearsExperience, Integer maxYearsExperience,
            Integer minSalary, Integer maxSalary,
            Map<String, Integer> breakdown, List<String> suggestions
    ) {
        int score = 0;

        // Experience requirements realism (15 points)
        if (minYearsExperience != null) {
            if (minYearsExperience <= MAX_REALISTIC_EXPERIENCE) {
                score += 10;
            } else {
                score += 3;
                suggestions.add(String.format(
                        "Minimum experience requirement (%d years) may be too high. Consider %d years or less.",
                        minYearsExperience, MAX_REALISTIC_EXPERIENCE
                ));
            }

            // Check for reasonable range
            if (maxYearsExperience != null) {
                int range = maxYearsExperience - minYearsExperience;
                if (range >= 3 && range <= 8) {
                    score += 5; // Reasonable range
                } else if (range < 3) {
                    score += 2;
                    suggestions.add("Experience range is too narrow. Consider widening to 3-5 years.");
                } else {
                    score += 2;
                    suggestions.add("Experience range is too wide. Consider narrowing for better targeting.");
                }
            } else {
                score += 3; // Has min but no max
            }
        } else {
            score += 5; // No specific requirement (flexible)
        }

        // Salary range realism (10 points)
        if (minSalary != null && maxSalary != null) {
            double salaryRatio = (double) maxSalary / minSalary;
            if (salaryRatio >= 1.2 && salaryRatio <= 1.5) {
                score += 10; // Realistic range (20-50% spread)
            } else if (salaryRatio < 1.2) {
                score += 5;
                suggestions.add("Salary range is too narrow. Consider a 20-30% spread for flexibility.");
            } else {
                score += 5;
                suggestions.add("Salary range is very wide. Consider narrowing for better candidate targeting.");
            }
        } else {
            score += 5; // No salary specified (neutral)
        }

        breakdown.put("realism", score);
        return score;
    }

    private int calculateClarityScore(
            String jobDescription, Map<String, Integer> breakdown, List<String> suggestions
    ) {
        int score = 0;
        int wordCount = countWords(jobDescription);

        // Length appropriateness (10 points)
        if (wordCount >= OPTIMAL_MIN_WORD_COUNT && wordCount <= OPTIMAL_MAX_WORD_COUNT) {
            score += 10; // Optimal length
        } else if (wordCount >= MIN_WORD_COUNT && wordCount < OPTIMAL_MIN_WORD_COUNT) {
            score += 6;
            suggestions.add("Job description is a bit short. Add more details about the role and company.");
        } else if (wordCount > OPTIMAL_MAX_WORD_COUNT) {
            score += 6;
            suggestions.add("Job description is quite long. Consider being more concise.");
        } else {
            score += 2;
            suggestions.add("Job description is too short. Provide at least 100 words of information.");
        }

        // Structure indicators (10 points)
        String lowerDesc = jobDescription.toLowerCase();
        int structureScore = 0;

        if (lowerDesc.contains("responsibilities") || lowerDesc.contains("what you'll do")) {
            structureScore += 3;
        }
        if (lowerDesc.contains("requirements") || lowerDesc.contains("qualifications")) {
            structureScore += 3;
        }
        if (lowerDesc.contains("benefits") || lowerDesc.contains("we offer")) {
            structureScore += 2;
        }
        if (lowerDesc.contains("about") || lowerDesc.contains("company")) {
            structureScore += 2;
        }

        score += structureScore;
        if (structureScore < 6) {
            suggestions.add("Add clear section headers: Responsibilities, Requirements, Benefits, About Us.");
        }

        breakdown.put("clarity", score);
        return score;
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private Integer calculateExperienceRange(Integer min, Integer max) {
        if (min != null && max != null) {
            return max - min;
        }
        return null;
    }

    private Boolean isRealisticRequirements(Integer min, Integer max) {
        if (min == null) return true;
        if (min > MAX_REALISTIC_EXPERIENCE) return false;
        if (max != null) {
            int range = max - min;
            return range >= 2 && range <= 10;
        }
        return true;
    }

    private String getQualityLevel(int score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 50) return "FAIR";
        return "POOR";
    }
}
