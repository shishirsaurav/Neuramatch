package com.neuramatch.resume.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeQualityServiceTest {

    private ResumeQualityService qualityService;

    @BeforeEach
    void setUp() {
        qualityService = new ResumeQualityService();
    }

    @Test
    void testCalculateQualityScore_HighQualityResume() {
        // Given
        String content = """
                John Doe
                john.doe@example.com
                +1234567890
                LinkedIn: linkedin.com/in/johndoe

                Professional Summary
                Experienced software engineer with 5 years of experience

                Work Experience
                Senior Developer at Tech Corp (2020-2025)
                - Led team of 5 developers
                - Improved system performance by 40%

                Education
                Bachelor of Science in Computer Science

                Skills
                Java, Python, Spring Boot, AWS
                """;

        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", true,
                "hasPhone", true,
                "hasLinkedIn", true,
                "hasGitHub", false,
                "wordCount", 70
        );

        // When
        int score = qualityService.calculateQualityScore(content, structuredInfo);

        // Then
        assertThat(score).isGreaterThan(70);
    }

    @Test
    void testCalculateQualityScore_LowQualityResume() {
        // Given
        String content = "I am a developer";

        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", false,
                "hasPhone", false,
                "hasLinkedIn", false,
                "hasGitHub", false,
                "wordCount", 4
        );

        // When
        int score = qualityService.calculateQualityScore(content, structuredInfo);

        // Then
        assertThat(score).isLessThan(30);
    }

    @Test
    void testGenerateSuggestions_MissingContactInfo() {
        // Given
        String content = "Experience: Developer at Company";
        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", false,
                "hasPhone", false,
                "hasLinkedIn", false,
                "wordCount", 5
        );

        // When
        List<String> suggestions = qualityService.generateSuggestions(content, structuredInfo, 30);

        // Then
        assertThat(suggestions).contains("Add a valid email address");
        assertThat(suggestions).contains("Add a phone number");
        assertThat(suggestions).contains("Include your LinkedIn profile URL");
    }

    @Test
    void testGenerateSuggestions_TooShort() {
        // Given
        String content = "Short resume";
        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", true,
                "hasPhone", true,
                "hasLinkedIn", true,
                "wordCount", 2
        );

        // When
        List<String> suggestions = qualityService.generateSuggestions(content, structuredInfo, 40);

        // Then
        assertThat(suggestions).anyMatch(s -> s.contains("too short"));
    }

    @Test
    void testGenerateSuggestions_MissingSections() {
        // Given
        String content = "John Doe, john@example.com, +1234567890";
        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", true,
                "hasPhone", true,
                "hasLinkedIn", false,
                "wordCount", 10
        );

        // When
        List<String> suggestions = qualityService.generateSuggestions(content, structuredInfo, 50);

        // Then
        assertThat(suggestions).anyMatch(s -> s.contains("work experience"));
        assertThat(suggestions).anyMatch(s -> s.contains("education"));
        assertThat(suggestions).anyMatch(s -> s.contains("skills"));
    }

    @Test
    void testGenerateSuggestions_ExcellentResume() {
        // Given
        String content = """
                John Doe
                john@example.com | +1234567890
                linkedin.com/in/johndoe

                Experience: Senior Developer (2020-2025)
                Education: BS Computer Science
                Skills: Java, Python, AWS
                """;

        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", true,
                "hasPhone", true,
                "hasLinkedIn", true,
                "wordCount", 30
        );

        // When
        List<String> suggestions = qualityService.generateSuggestions(content, structuredInfo, 90);

        // Then
        assertThat(suggestions).anyMatch(s -> s.contains("Excellent resume"));
    }

    @Test
    void testAnalyzeQualityMetrics() {
        // Given
        String content = """
                John Doe - Software Engineer
                Email: john@example.com | Phone: +1234567890

                Experience
                Senior Developer at Tech Corp (2023-2025)

                Education
                Bachelor of Computer Science

                Skills
                Java, Spring Boot, AWS
                """;

        Map<String, Object> structuredInfo = Map.of(
                "hasEmail", true,
                "hasPhone", true,
                "hasLinkedIn", false,
                "wordCount", 40
        );

        // When
        Map<String, Object> metrics = qualityService.analyzeQualityMetrics(content, structuredInfo);

        // Then
        assertThat(metrics).containsKeys("completeness", "length", "structure", "recency", "wordCount", "hasContactInfo");
        assertThat((Boolean) metrics.get("hasContactInfo")).isTrue();
        assertThat((Integer) metrics.get("wordCount")).isEqualTo(40);
    }
}
