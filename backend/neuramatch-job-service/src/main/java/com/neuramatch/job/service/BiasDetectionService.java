package com.neuramatch.job.service;

import com.neuramatch.job.dto.BiasIssueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BiasDetectionService {

    // Age-related bias keywords
    private static final Map<String, String> AGE_BIAS_TERMS = Map.ofEntries(
            Map.entry("young", "Replace with 'enthusiastic' or 'energetic approach'"),
            Map.entry("energetic", "This can imply age bias. Consider 'motivated' or 'proactive'"),
            Map.entry("recent graduate", "This implies age preference. Use 'entry-level' instead"),
            Map.entry("digital native", "Age-coded term. Use 'tech-savvy' or 'digitally proficient'"),
            Map.entry("mature", "Age-coded. Use 'experienced professional' instead"),
            Map.entry("seasoned", "Can imply age. Use 'experienced' without age connotation")
    );

    // Gender-coded language (masculine-coded)
    private static final Map<String, String> MASCULINE_CODED_TERMS = Map.ofEntries(
            Map.entry("rockstar", "Gender-coded. Use 'excellent' or 'outstanding'"),
            Map.entry("ninja", "Gender-coded. Use 'expert' or 'specialist'"),
            Map.entry("guru", "Gender-coded. Use 'expert' or 'authority'"),
            Map.entry("wizard", "Gender-coded. Use 'expert' or 'skilled professional'"),
            Map.entry("dominate", "Masculine-coded. Use 'excel at' or 'proficient in'"),
            Map.entry("aggressive", "Masculine-coded. Use 'assertive' or 'proactive'"),
            Map.entry("competitive", "Can be masculine-coded. Consider 'goal-oriented'"),
            Map.entry("strong", "Can be masculine-coded. Consider 'effective' or 'capable'"),
            Map.entry("independent", "Masculine-coded. Consider 'self-motivated'"),
            Map.entry("confident", "Masculine-coded. Consider 'capable' or 'skilled'")
    );

    // Gender-coded language (feminine-coded)
    private static final Map<String, String> FEMININE_CODED_TERMS = Map.ofEntries(
            Map.entry("support", "Feminine-coded. Consider 'assist' or 'enable'"),
            Map.entry("nurture", "Feminine-coded. Use 'develop' or 'mentor'"),
            Map.entry("collaborative", "Feminine-coded. Consider 'team-oriented'"),
            Map.entry("interpersonal", "Feminine-coded. Use 'communication skills'"),
            Map.entry("understanding", "Feminine-coded. Consider 'empathetic' or 'perceptive'")
    );

    // Disability bias
    private static final Map<String, String> DISABILITY_BIAS_TERMS = Map.ofEntries(
            Map.entry("walk", "May exclude wheelchair users. Use 'move' or 'navigate'"),
            Map.entry("see", "May exclude visually impaired. Use 'review' or 'examine'"),
            Map.entry("hear", "May exclude hearing impaired. Use 'understand' or 'comprehend'"),
            Map.entry("stand", "May exclude those with mobility issues. Rephrase or remove"),
            Map.entry("physically fit", "Potentially discriminatory. Specify actual requirements"),
            Map.entry("able-bodied", "Discriminatory term. Remove or specify actual needs")
    );

    // Cultural/racial bias
    private static final Map<String, String> CULTURAL_BIAS_TERMS = Map.ofEntries(
            Map.entry("native english speaker", "Potentially discriminatory. Use 'fluent in English'"),
            Map.entry("native speaker", "Potentially discriminatory. Use 'fluent' or 'proficient'"),
            Map.entry("cultural fit", "Can mask discrimination. Use 'values alignment' or 'team compatibility'"),
            Map.entry("traditional", "Can be coded language. Be specific about actual values"),
            Map.entry("american", "Unless legally required, avoid nationality requirements")
    );

    // Over-qualification terms
    private static final Map<String, String> OVERQUALIFICATION_TERMS = Map.ofEntries(
            Map.entry("overqualified", "This is discriminatory. Remove this term"),
            Map.entry("too experienced", "This is discriminatory. Remove this term")
    );

    public Map<String, Object> detectBias(String jobTitle, String jobDescription,
                                          String responsibilities, String qualifications) {
        log.debug("Analyzing bias in job posting: {}", jobTitle);

        String fullText = String.join(" ",
                jobTitle != null ? jobTitle : "",
                jobDescription != null ? jobDescription : "",
                responsibilities != null ? responsibilities : "",
                qualifications != null ? qualifications : ""
        ).toLowerCase();

        List<BiasIssueDTO> issues = new ArrayList<>();

        // Detect different types of bias
        issues.addAll(detectAgeBias(fullText));
        issues.addAll(detectGenderCodedLanguage(fullText));
        issues.addAll(detectDisabilityBias(fullText));
        issues.addAll(detectCulturalBias(fullText));
        issues.addAll(detectOverqualificationBias(fullText));

        // Calculate overall bias score (0-100, where 100 is no bias)
        int biasScore = calculateBiasScore(issues);

        // Generate summary
        String summary = generateBiasSummary(issues, biasScore);

        Map<String, Object> result = new HashMap<>();
        result.put("biasScore", biasScore);
        result.put("issues", issues);
        result.put("issueCount", issues.size());
        result.put("summary", summary);
        result.put("recommendation", getRecommendation(biasScore));
        result.put("hasCriticalIssues", hasCriticalIssues(issues));

        return result;
    }

    private List<BiasIssueDTO> detectAgeBias(String text) {
        List<BiasIssueDTO> issues = new ArrayList<>();

        for (Map.Entry<String, String> entry : AGE_BIAS_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("AGE_BIAS")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity(getSeverity(entry.getKey()))
                        .suggestion(entry.getValue())
                        .explanation("This term may indicate age preference, which is discriminatory.")
                        .build());
            }
        }

        // Check for specific age ranges
        if (Pattern.compile("\\d+\\s*-\\s*\\d+\\s*years old").matcher(text).find()) {
            issues.add(BiasIssueDTO.builder()
                    .type("AGE_BIAS")
                    .term("age range specification")
                    .context("Specifies age range")
                    .severity("HIGH")
                    .suggestion("Remove age range. Specify experience requirements instead.")
                    .explanation("Specifying age ranges is illegal discrimination.")
                    .build());
        }

        return issues;
    }

    private List<BiasIssueDTO> detectGenderCodedLanguage(String text) {
        List<BiasIssueDTO> issues = new ArrayList<>();

        // Check masculine-coded terms
        for (Map.Entry<String, String> entry : MASCULINE_CODED_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("GENDER_CODED_MASCULINE")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity("MEDIUM")
                        .suggestion(entry.getValue())
                        .explanation("Masculine-coded language may discourage women from applying.")
                        .build());
            }
        }

        // Check feminine-coded terms
        for (Map.Entry<String, String> entry : FEMININE_CODED_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("GENDER_CODED_FEMININE")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity("MEDIUM")
                        .suggestion(entry.getValue())
                        .explanation("Feminine-coded language may discourage men from applying.")
                        .build());
            }
        }

        // Check for gendered pronouns
        if (text.matches(".*\\b(he|him|his)\\b.*") && !text.matches(".*\\b(she|her|they|them)\\b.*")) {
            issues.add(BiasIssueDTO.builder()
                    .type("GENDER_PRONOUN")
                    .term("male pronouns only")
                    .context("Uses 'he/him/his' exclusively")
                    .severity("HIGH")
                    .suggestion("Use gender-neutral pronouns (they/them) or 'the candidate'")
                    .explanation("Exclusive use of male pronouns suggests gender preference.")
                    .build());
        }

        return issues;
    }

    private List<BiasIssueDTO> detectDisabilityBias(String text) {
        List<BiasIssueDTO> issues = new ArrayList<>();

        for (Map.Entry<String, String> entry : DISABILITY_BIAS_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("DISABILITY_BIAS")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity(entry.getKey().equals("able-bodied") ? "HIGH" : "MEDIUM")
                        .suggestion(entry.getValue())
                        .explanation("This term may exclude people with disabilities.")
                        .build());
            }
        }

        return issues;
    }

    private List<BiasIssueDTO> detectCulturalBias(String text) {
        List<BiasIssueDTO> issues = new ArrayList<>();

        for (Map.Entry<String, String> entry : CULTURAL_BIAS_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("CULTURAL_BIAS")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity(entry.getKey().contains("native") ? "HIGH" : "MEDIUM")
                        .suggestion(entry.getValue())
                        .explanation("This term may discriminate based on nationality or ethnicity.")
                        .build());
            }
        }

        return issues;
    }

    private List<BiasIssueDTO> detectOverqualificationBias(String text) {
        List<BiasIssueDTO> issues = new ArrayList<>();

        for (Map.Entry<String, String> entry : OVERQUALIFICATION_TERMS.entrySet()) {
            if (text.contains(entry.getKey())) {
                issues.add(BiasIssueDTO.builder()
                        .type("OVERQUALIFICATION_BIAS")
                        .term(entry.getKey())
                        .context(extractContext(text, entry.getKey()))
                        .severity("HIGH")
                        .suggestion(entry.getValue())
                        .explanation("'Overqualified' is often used to discriminate based on age.")
                        .build());
            }
        }

        return issues;
    }

    private int calculateBiasScore(List<BiasIssueDTO> issues) {
        if (issues.isEmpty()) {
            return 100; // Perfect score - no bias detected
        }

        int deductions = 0;
        for (BiasIssueDTO issue : issues) {
            switch (issue.getSeverity()) {
                case "HIGH":
                    deductions += 20;
                    break;
                case "MEDIUM":
                    deductions += 10;
                    break;
                case "LOW":
                    deductions += 5;
                    break;
            }
        }

        return Math.max(0, 100 - deductions);
    }

    private String extractContext(String text, String term) {
        int index = text.indexOf(term);
        if (index == -1) return "";

        int start = Math.max(0, index - 30);
        int end = Math.min(text.length(), index + term.length() + 30);

        return "..." + text.substring(start, end) + "...";
    }

    private String getSeverity(String term) {
        // Terms that are clearly discriminatory
        if (term.contains("native") || term.equals("able-bodied") ||
            term.contains("old") || term.equals("overqualified")) {
            return "HIGH";
        }
        // Coded language
        if (term.equals("rockstar") || term.equals("ninja") ||
            term.equals("young") || term.equals("energetic")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String generateBiasSummary(List<BiasIssueDTO> issues, int biasScore) {
        if (biasScore >= 90) {
            return "Excellent! Job posting shows minimal to no bias.";
        } else if (biasScore >= 70) {
            return String.format("Job posting has %d potential bias issue(s). Review suggestions for improvement.", issues.size());
        } else if (biasScore >= 50) {
            return String.format("Job posting has %d bias issues that should be addressed before publishing.", issues.size());
        } else {
            return String.format("Job posting has %d significant bias issues. Rewrite is strongly recommended.", issues.size());
        }
    }

    private String getRecommendation(int biasScore) {
        if (biasScore >= 90) {
            return "APPROVED - Minimal bias detected";
        } else if (biasScore >= 70) {
            return "REVIEW RECOMMENDED - Address suggestions before publishing";
        } else if (biasScore >= 50) {
            return "REVISIONS REQUIRED - Significant issues must be resolved";
        } else {
            return "MAJOR REWRITE REQUIRED - Too many critical bias issues";
        }
    }

    private boolean hasCriticalIssues(List<BiasIssueDTO> issues) {
        return issues.stream()
                .anyMatch(issue -> "HIGH".equals(issue.getSeverity()));
    }
}
