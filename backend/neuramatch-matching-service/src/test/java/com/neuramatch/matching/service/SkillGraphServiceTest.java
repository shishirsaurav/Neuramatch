package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.SkillEnrichmentDTO;
import com.neuramatch.matching.dto.SkillRecommendationDTO;
import com.neuramatch.matching.entity.SkillNode;
import com.neuramatch.matching.repository.SkillGraphRepository;
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
 * Unit tests for SkillGraphService
 */
@ExtendWith(MockitoExtension.class)
class SkillGraphServiceTest {

    @Mock
    private SkillGraphRepository skillGraphRepository;

    @InjectMocks
    private SkillGraphService skillGraphService;

    private SkillNode javaSkill;
    private SkillNode springBootSkill;
    private SkillNode pythonSkill;

    @BeforeEach
    void setUp() {
        javaSkill = SkillNode.builder()
            .name("java")
            .displayName("Java")
            .category(SkillNode.SkillCategory.PROGRAMMING_LANGUAGE)
            .popularity(0.92)
            .trendScore(0.85)
            .avgSalaryImpact(15000)
            .difficultyLevel(SkillNode.DifficultyLevel.INTERMEDIATE)
            .description("Object-oriented programming language")
            .build();

        springBootSkill = SkillNode.builder()
            .name("spring boot")
            .displayName("Spring Boot")
            .category(SkillNode.SkillCategory.FRAMEWORK)
            .popularity(0.90)
            .trendScore(0.90)
            .avgSalaryImpact(14000)
            .difficultyLevel(SkillNode.DifficultyLevel.INTERMEDIATE)
            .build();

        pythonSkill = SkillNode.builder()
            .name("python")
            .displayName("Python")
            .category(SkillNode.SkillCategory.PROGRAMMING_LANGUAGE)
            .popularity(0.95)
            .trendScore(0.95)
            .avgSalaryImpact(18000)
            .difficultyLevel(SkillNode.DifficultyLevel.BEGINNER)
            .build();
    }

    @Test
    void enrichSkill_WhenSkillExists_ShouldReturnEnrichedData() {
        // Given
        when(skillGraphRepository.findCanonicalSkill("java")).thenReturn(Optional.empty());
        when(skillGraphRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(javaSkill));
        when(skillGraphRepository.findDirectPrerequisites("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findComplementarySkills("java", 10)).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findAlternativeSkills("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findSynonyms("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findParentSkills("java")).thenReturn(Collections.emptyList());

        // When
        Optional<SkillEnrichmentDTO> result = skillGraphService.enrichSkill("java");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSkillName()).isEqualTo("java");
        assertThat(result.get().getDisplayName()).isEqualTo("Java");
        assertThat(result.get().getPopularity()).isEqualTo(0.92);
        assertThat(result.get().getTrendScore()).isEqualTo(0.85);

        verify(skillGraphRepository).findByNameIgnoreCase("java");
    }

    @Test
    void enrichSkill_WhenSkillNotFound_ShouldReturnEmpty() {
        // Given
        when(skillGraphRepository.findCanonicalSkill("unknown")).thenReturn(Optional.empty());
        when(skillGraphRepository.findByNameIgnoreCase("unknown")).thenReturn(Optional.empty());

        // When
        Optional<SkillEnrichmentDTO> result = skillGraphService.enrichSkill("unknown");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void resolveSkillSynonym_WhenSynonymExists_ShouldReturnCanonical() {
        // Given
        SkillNode k8sSkill = SkillNode.builder().name("k8s").displayName("K8s").build();
        SkillNode kubernetesSkill = SkillNode.builder().name("kubernetes").displayName("Kubernetes").build();

        when(skillGraphRepository.findCanonicalSkill("k8s")).thenReturn(Optional.of(kubernetesSkill));

        // When
        String result = skillGraphService.resolveSkillSynonym("k8s");

        // Then
        assertThat(result).isEqualTo("kubernetes");
    }

    @Test
    void resolveSkillSynonym_WhenNoSynonym_ShouldReturnOriginal() {
        // Given
        when(skillGraphRepository.findCanonicalSkill("java")).thenReturn(Optional.empty());
        when(skillGraphRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(javaSkill));

        // When
        String result = skillGraphService.resolveSkillSynonym("java");

        // Then
        assertThat(result).isEqualTo("java");
    }

    @Test
    void getRecommendations_ShouldReturnMultipleRecommendationTypes() {
        // Given
        Set<String> existingSkills = Set.of("java", "spring boot");

        when(skillGraphRepository.findSkillGaps(anySet(), anyInt()))
            .thenReturn(List.of(pythonSkill));
        when(skillGraphRepository.findTrendingSkills(0.85))
            .thenReturn(List.of(pythonSkill));
        when(skillGraphRepository.findPopularSkills(0.80))
            .thenReturn(List.of(pythonSkill));

        // When
        List<SkillRecommendationDTO> recommendations = skillGraphService.getRecommendations(existingSkills, 10);

        // Then
        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).anyMatch(r -> r.getRecommendationType() == SkillRecommendationDTO.RecommendationType.COMPLEMENTARY);
        assertThat(recommendations).anyMatch(r -> r.getRecommendationType() == SkillRecommendationDTO.RecommendationType.TRENDING);
    }

    @Test
    void findMissingPrerequisites_ShouldReturnMissingSkills() {
        // Given
        Set<String> existingSkills = Set.of("python", "django");

        when(skillGraphRepository.findMissingPrerequisites(eq("spring boot"), anySet()))
            .thenReturn(List.of(javaSkill));

        // When
        List<SkillRecommendationDTO> missing = skillGraphService.findMissingPrerequisites("spring boot", existingSkills);

        // Then
        assertThat(missing).hasSize(1);
        assertThat(missing.get(0).getSkillName()).isEqualTo("java");
        assertThat(missing.get(0).getRecommendationType())
            .isEqualTo(SkillRecommendationDTO.RecommendationType.PREREQUISITE);
    }

    @Test
    void findAlternatives_ShouldReturnAlternativeSkills() {
        // Given
        SkillNode mysqlSkill = SkillNode.builder().name("mysql").displayName("MySQL").build();

        when(skillGraphRepository.findEasilyTransferableAlternatives("postgresql", 0.75))
            .thenReturn(List.of(mysqlSkill));

        // When
        List<String> alternatives = skillGraphService.findAlternatives("postgresql", 0.75);

        // Then
        assertThat(alternatives).containsExactly("mysql");
    }

    @Test
    void calculateSkillSetSimilarity_WithExactMatches_ShouldReturnHighScore() {
        // Given
        Set<String> skillSet1 = Set.of("java", "python", "sql");
        Set<String> skillSet2 = Set.of("java", "python", "sql");

        SkillGraphRepository.SkillSimilarityScore mockScore = new SkillGraphRepository.SkillSimilarityScore() {
            @Override
            public Integer getRelationshipCount() { return 3; }
            @Override
            public Integer getSet1Size() { return 3; }
            @Override
            public Integer getSet2Size() { return 3; }
        };

        when(skillGraphRepository.calculateSkillSetSimilarity(anySet(), anySet())).thenReturn(mockScore);

        // When
        double similarity = skillGraphService.calculateSkillSetSimilarity(skillSet1, skillSet2);

        // Then
        assertThat(similarity).isGreaterThan(0.5);
    }

    @Test
    void calculateSkillSetSimilarity_WithEmptySet_ShouldReturnZero() {
        // Given
        Set<String> skillSet1 = Collections.emptySet();
        Set<String> skillSet2 = Set.of("java", "python");

        // When
        double similarity = skillGraphService.calculateSkillSetSimilarity(skillSet1, skillSet2);

        // Then
        assertThat(similarity).isEqualTo(0.0);
    }

    @Test
    void searchSkills_ShouldReturnMatchingSkills() {
        // Given
        when(skillGraphRepository.searchSkills("java")).thenReturn(List.of(javaSkill));
        when(skillGraphRepository.findCanonicalSkill("java")).thenReturn(Optional.empty());
        when(skillGraphRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(javaSkill));
        when(skillGraphRepository.findDirectPrerequisites("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findComplementarySkills("java", 10)).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findAlternativeSkills("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findSynonyms("java")).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findParentSkills("java")).thenReturn(Collections.emptyList());

        // When
        List<SkillEnrichmentDTO> results = skillGraphService.searchSkills("java");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDisplayName()).isEqualTo("Java");
    }

    @Test
    void getPopularSkillsByCategory_ShouldReturnSortedSkills() {
        // Given
        when(skillGraphRepository.findByCategory(SkillNode.SkillCategory.PROGRAMMING_LANGUAGE))
            .thenReturn(List.of(javaSkill, pythonSkill));
        when(skillGraphRepository.findCanonicalSkill(anyString())).thenReturn(Optional.empty());
        when(skillGraphRepository.findByNameIgnoreCase(anyString()))
            .thenAnswer(inv -> {
                String name = inv.getArgument(0);
                if ("java".equals(name)) return Optional.of(javaSkill);
                if ("python".equals(name)) return Optional.of(pythonSkill);
                return Optional.empty();
            });
        when(skillGraphRepository.findDirectPrerequisites(anyString())).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findComplementarySkills(anyString(), anyInt())).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findAlternativeSkills(anyString())).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findSynonyms(anyString())).thenReturn(Collections.emptyList());
        when(skillGraphRepository.findParentSkills(anyString())).thenReturn(Collections.emptyList());

        // When
        List<SkillEnrichmentDTO> results = skillGraphService.getPopularSkillsByCategory(
            SkillNode.SkillCategory.PROGRAMMING_LANGUAGE, 5
        );

        // Then
        assertThat(results).isNotEmpty();
        // Python has higher popularity (0.95) than Java (0.92)
        assertThat(results.get(0).getDisplayName()).isEqualTo("Python");
    }
}
