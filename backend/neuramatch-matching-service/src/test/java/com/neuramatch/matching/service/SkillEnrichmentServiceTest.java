package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.SkillEnrichmentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SkillEnrichmentService
 */
@ExtendWith(MockitoExtension.class)
class SkillEnrichmentServiceTest {

    @Mock
    private SkillGraphService skillGraphService;

    @InjectMocks
    private SkillEnrichmentService skillEnrichmentService;

    private SkillEnrichmentDTO javaEnriched;
    private SkillEnrichmentDTO pythonEnriched;

    @BeforeEach
    void setUp() {
        javaEnriched = SkillEnrichmentDTO.builder()
            .skillName("java")
            .canonicalName("java")
            .displayName("Java")
            .category("PROGRAMMING_LANGUAGE")
            .popularity(0.92)
            .trendScore(0.85)
            .avgSalaryImpact(15000)
            .difficultyLevel("INTERMEDIATE")
            .build();

        pythonEnriched = SkillEnrichmentDTO.builder()
            .skillName("python")
            .canonicalName("python")
            .displayName("Python")
            .category("PROGRAMMING_LANGUAGE")
            .popularity(0.95)
            .trendScore(0.95)
            .avgSalaryImpact(18000)
            .difficultyLevel("BEGINNER")
            .build();
    }

    @Test
    void enrichSkills_ShouldEnrichAllValidSkills() {
        // Given
        List<String> skillNames = List.of("java", "python", "unknown");

        when(skillGraphService.enrichSkill("java")).thenReturn(Optional.of(javaEnriched));
        when(skillGraphService.enrichSkill("python")).thenReturn(Optional.of(pythonEnriched));
        when(skillGraphService.enrichSkill("unknown")).thenReturn(Optional.empty());

        // When
        List<SkillEnrichmentDTO> result = skillEnrichmentService.enrichSkills(skillNames);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("displayName").containsExactlyInAnyOrder("Java", "Python");
    }

    @Test
    void normalizeSkills_ShouldResolveSynonymsAndDeduplicate() {
        // Given
        List<String> skillNames = List.of("java", "Java", "js", "javascript");

        when(skillGraphService.resolveSkillSynonym("java")).thenReturn("java");
        when(skillGraphService.resolveSkillSynonym("Java")).thenReturn("java");
        when(skillGraphService.resolveSkillSynonym("js")).thenReturn("javascript");
        when(skillGraphService.resolveSkillSynonym("javascript")).thenReturn("javascript");

        // When
        Set<String> result = skillEnrichmentService.normalizeSkills(skillNames);

        // Then
        assertThat(result).containsExactlyInAnyOrder("java", "javascript");
        assertThat(result).hasSize(2); // Deduplicated
    }

    @Test
    void expandSkillSet_ShouldAddImplicitSkills() {
        // Given
        List<String> skillNames = List.of("spring boot");

        SkillEnrichmentDTO springBootEnriched = SkillEnrichmentDTO.builder()
            .skillName("spring boot")
            .canonicalName("spring boot")
            .displayName("Spring Boot")
            .partOfEcosystem(List.of("Spring"))
            .build();

        when(skillGraphService.resolveSkillSynonym("spring boot")).thenReturn("spring boot");
        when(skillGraphService.enrichSkill("spring boot")).thenReturn(Optional.of(springBootEnriched));

        // When
        Set<String> result = skillEnrichmentService.expandSkillSet(skillNames);

        // Then
        assertThat(result).contains("spring boot", "spring");
    }

    @Test
    void analyzeSkillSet_ShouldProvideComprehensiveAnalysis() {
        // Given
        List<String> skillNames = List.of("java", "python");

        when(skillGraphService.resolveSkillSynonym("java")).thenReturn("java");
        when(skillGraphService.resolveSkillSynonym("python")).thenReturn("python");
        when(skillGraphService.enrichSkill("java")).thenReturn(Optional.of(javaEnriched));
        when(skillGraphService.enrichSkill("python")).thenReturn(Optional.of(pythonEnriched));

        // When
        Map<String, Object> analysis = skillEnrichmentService.analyzeSkillSet(skillNames);

        // Then
        assertThat(analysis).containsKeys(
            "totalSkills",
            "enrichedSkills",
            "categoryDistribution",
            "averagePopularity",
            "averageTrendScore",
            "totalSalaryImpact",
            "levelDistribution"
        );

        assertThat(analysis.get("totalSkills")).isEqualTo(2);
        assertThat((Double) analysis.get("averagePopularity")).isGreaterThan(0.9);
        assertThat((Integer) analysis.get("totalSalaryImpact")).isEqualTo(33000);
    }

    @Test
    void hasPrerequisites_WhenAllPrerequisitesMet_ShouldReturnTrue() {
        // Given
        List<String> candidateSkills = List.of("java", "sql");
        String targetSkill = "spring boot";

        SkillEnrichmentDTO.RelatedSkillDTO javaPrereq = SkillEnrichmentDTO.RelatedSkillDTO.builder()
            .skillName("java")
            .build();

        SkillEnrichmentDTO springBootEnriched = SkillEnrichmentDTO.builder()
            .skillName("spring boot")
            .prerequisites(List.of(javaPrereq))
            .build();

        when(skillGraphService.resolveSkillSynonym(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().toLowerCase());
        when(skillGraphService.enrichSkill("spring boot")).thenReturn(Optional.of(springBootEnriched));

        // When
        boolean result = skillEnrichmentService.hasPrerequisites(candidateSkills, targetSkill);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasPrerequisites_WhenPrerequisitesMissing_ShouldReturnFalse() {
        // Given
        List<String> candidateSkills = List.of("python", "django");
        String targetSkill = "spring boot";

        SkillEnrichmentDTO.RelatedSkillDTO javaPrereq = SkillEnrichmentDTO.RelatedSkillDTO.builder()
            .skillName("java")
            .build();

        SkillEnrichmentDTO springBootEnriched = SkillEnrichmentDTO.builder()
            .skillName("spring boot")
            .prerequisites(List.of(javaPrereq))
            .build();

        when(skillGraphService.resolveSkillSynonym(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().toLowerCase());
        when(skillGraphService.enrichSkill("spring boot")).thenReturn(Optional.of(springBootEnriched));

        // When
        boolean result = skillEnrichmentService.hasPrerequisites(candidateSkills, targetSkill);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void calculateSkillCoverage_WithExactMatches_ShouldReturn100Percent() {
        // Given
        List<String> candidateSkills = List.of("java", "python", "sql");
        List<String> requiredSkills = List.of("java", "python", "sql");

        when(skillGraphService.resolveSkillSynonym(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().toLowerCase());

        // When
        double coverage = skillEnrichmentService.calculateSkillCoverage(candidateSkills, requiredSkills, false);

        // Then
        assertThat(coverage).isEqualTo(1.0);
    }

    @Test
    void calculateSkillCoverage_WithPartialMatches_ShouldReturnPartialCoverage() {
        // Given
        List<String> candidateSkills = List.of("java", "python");
        List<String> requiredSkills = List.of("java", "python", "sql", "docker");

        when(skillGraphService.resolveSkillSynonym(anyString())).thenAnswer(inv -> inv.getArgument(0).toString().toLowerCase());

        // When
        double coverage = skillEnrichmentService.calculateSkillCoverage(candidateSkills, requiredSkills, false);

        // Then
        assertThat(coverage).isEqualTo(0.5); // 2 out of 4
    }

    @Test
    void calculateSkillCoverage_WithAlternatives_ShouldIncludeAlternativeMatches() {
        // Given
        List<String> candidateSkills = List.of("mysql");
        List<String> requiredSkills = List.of("postgresql");

        when(skillGraphService.resolveSkillSynonym("mysql")).thenReturn("mysql");
        when(skillGraphService.resolveSkillSynonym("postgresql")).thenReturn("postgresql");
        when(skillGraphService.findAlternatives("postgresql", 0.75)).thenReturn(List.of("mysql"));

        // When
        double coverage = skillEnrichmentService.calculateSkillCoverage(candidateSkills, requiredSkills, true);

        // Then
        assertThat(coverage).isEqualTo(1.0); // MySQL is alternative to PostgreSQL
    }

    @Test
    void calculateSkillCoverage_WithEmptyRequired_ShouldReturn100Percent() {
        // Given
        List<String> candidateSkills = List.of("java", "python");
        List<String> requiredSkills = Collections.emptyList();

        // When
        double coverage = skillEnrichmentService.calculateSkillCoverage(candidateSkills, requiredSkills, false);

        // Then
        assertThat(coverage).isEqualTo(1.0);
    }
}
