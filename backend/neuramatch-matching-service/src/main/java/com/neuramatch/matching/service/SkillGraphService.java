package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.SkillEnrichmentDTO;
import com.neuramatch.matching.dto.SkillRecommendationDTO;
import com.neuramatch.matching.entity.*;
import com.neuramatch.matching.repository.SkillGraphRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for interacting with the skill knowledge graph
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SkillGraphService {

    private final SkillGraphRepository skillGraphRepository;

    /**
     * Enrich a skill with graph data
     */
    public Optional<SkillEnrichmentDTO> enrichSkill(String skillName) {
        log.debug("Enriching skill: {}", skillName);

        // First, try to resolve synonym
        String canonicalName = resolveSkillSynonym(skillName);

        Optional<SkillNode> skillOpt = skillGraphRepository.findByNameIgnoreCase(canonicalName);

        return skillOpt.map(skill -> {
            List<SkillNode> prerequisites = skillGraphRepository.findDirectPrerequisites(skill.getName());
            List<SkillNode> complements = skillGraphRepository.findComplementarySkills(skill.getName(), 10);
            List<SkillNode> alternatives = skillGraphRepository.findAlternativeSkills(skill.getName());
            List<SkillNode> synonymNodes = skillGraphRepository.findSynonyms(skill.getName());
            List<SkillNode> parents = skillGraphRepository.findParentSkills(skill.getName());

            return SkillEnrichmentDTO.builder()
                .skillName(skill.getName())
                .canonicalName(canonicalName)
                .displayName(skill.getDisplayName())
                .category(skill.getCategory().name())
                .popularity(skill.getPopularity())
                .trendScore(skill.getTrendScore())
                .avgSalaryImpact(skill.getAvgSalaryImpact())
                .difficultyLevel(skill.getDifficultyLevel().name())
                .prerequisites(convertToRelatedSkills(prerequisites, "REQUIRES"))
                .complementarySkills(convertToRelatedSkills(complements, "COMPLEMENTS"))
                .alternatives(convertToRelatedSkills(alternatives, "ALTERNATIVE_TO"))
                .synonyms(synonymNodes.stream().map(SkillNode::getDisplayName).collect(Collectors.toList()))
                .partOfEcosystem(parents.stream().map(SkillNode::getDisplayName).collect(Collectors.toList()))
                .build();
        });
    }

    /**
     * Resolve skill name to canonical form (handles synonyms)
     */
    public String resolveSkillSynonym(String skillName) {
        Optional<SkillNode> canonical = skillGraphRepository.findCanonicalSkill(skillName.toLowerCase());

        if (canonical.isPresent()) {
            log.debug("Resolved synonym '{}' to canonical '{}'", skillName, canonical.get().getName());
            return canonical.get().getName();
        }

        // Check if skill exists as-is
        Optional<SkillNode> directMatch = skillGraphRepository.findByNameIgnoreCase(skillName);
        if (directMatch.isPresent()) {
            return directMatch.get().getName();
        }

        // Return original if no match found
        log.debug("No canonical form found for '{}', using as-is", skillName);
        return skillName.toLowerCase();
    }

    /**
     * Get skill recommendations based on existing skills
     */
    public List<SkillRecommendationDTO> getRecommendations(Set<String> existingSkills, int limit) {
        log.debug("Getting recommendations for {} skills", existingSkills.size());

        // Resolve all synonyms first
        Set<String> resolvedSkills = existingSkills.stream()
            .map(this::resolveSkillSynonym)
            .collect(Collectors.toSet());

        List<SkillRecommendationDTO> recommendations = new ArrayList<>();

        // 1. Find complementary skills (skill gaps)
        List<SkillNode> complementary = skillGraphRepository.findSkillGaps(resolvedSkills, limit);
        recommendations.addAll(convertToRecommendations(
            complementary,
            SkillRecommendationDTO.RecommendationType.COMPLEMENTARY,
            "Works well with your existing skills"
        ));

        // 2. Find trending skills not in skillset
        List<SkillNode> trending = skillGraphRepository.findTrendingSkills(0.85);
        trending.stream()
            .filter(skill -> !resolvedSkills.contains(skill.getName()))
            .limit(5)
            .forEach(skill -> recommendations.add(convertToRecommendation(
                skill,
                SkillRecommendationDTO.RecommendationType.TRENDING,
                "High growth trend in the market"
            )));

        // 3. Find high salary impact skills
        recommendations.addAll(
            skillGraphRepository.findPopularSkills(0.80).stream()
                .filter(skill -> !resolvedSkills.contains(skill.getName()))
                .filter(skill -> skill.getAvgSalaryImpact() > 15000)
                .limit(5)
                .map(skill -> convertToRecommendation(
                    skill,
                    SkillRecommendationDTO.RecommendationType.HIGH_SALARY_IMPACT,
                    String.format("Average salary impact: $%,d", skill.getAvgSalaryImpact())
                ))
                .collect(Collectors.toList())
        );

        // Sort by recommendation score and limit
        return recommendations.stream()
            .sorted(Comparator.comparing(SkillRecommendationDTO::getRecommendationScore).reversed())
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Find missing prerequisites for a target skill
     */
    public List<SkillRecommendationDTO> findMissingPrerequisites(String targetSkill, Set<String> existingSkills) {
        log.debug("Finding missing prerequisites for '{}' given {} existing skills", targetSkill, existingSkills.size());

        String resolvedTarget = resolveSkillSynonym(targetSkill);
        Set<String> resolvedExisting = existingSkills.stream()
            .map(this::resolveSkillSynonym)
            .collect(Collectors.toSet());

        List<SkillNode> missing = skillGraphRepository.findMissingPrerequisites(resolvedTarget, resolvedExisting);

        return convertToRecommendations(
            missing,
            SkillRecommendationDTO.RecommendationType.PREREQUISITE,
            String.format("Required to learn %s", targetSkill)
        );
    }

    /**
     * Find alternative skills (for job matching flexibility)
     */
    public List<String> findAlternatives(String skillName, double minTransferability) {
        String resolved = resolveSkillSynonym(skillName);

        return skillGraphRepository.findEasilyTransferableAlternatives(resolved, minTransferability)
            .stream()
            .map(SkillNode::getName)
            .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between two skillsets
     */
    public double calculateSkillSetSimilarity(Set<String> skillSet1, Set<String> skillSet2) {
        if (skillSet1.isEmpty() || skillSet2.isEmpty()) {
            return 0.0;
        }

        // Resolve synonyms
        Set<String> resolved1 = skillSet1.stream().map(this::resolveSkillSynonym).collect(Collectors.toSet());
        Set<String> resolved2 = skillSet2.stream().map(this::resolveSkillSynonym).collect(Collectors.toSet());

        // Exact matches
        Set<String> intersection = new HashSet<>(resolved1);
        intersection.retainAll(resolved2);
        double exactMatchScore = (double) intersection.size() / Math.max(resolved1.size(), resolved2.size());

        // Relationship-based similarity
        SkillGraphRepository.SkillSimilarityScore score =
            skillGraphRepository.calculateSkillSetSimilarity(resolved1, resolved2);

        double relationshipScore = 0.0;
        if (score != null && score.getRelationshipCount() > 0) {
            int totalPossible = score.getSet1Size() * score.getSet2Size();
            relationshipScore = (double) score.getRelationshipCount() / totalPossible;
        }

        // Weighted combination: 70% exact matches, 30% relationships
        return (exactMatchScore * 0.7) + (relationshipScore * 0.3);
    }

    /**
     * Search skills by text
     */
    public List<SkillEnrichmentDTO> searchSkills(String searchTerm) {
        return skillGraphRepository.searchSkills(searchTerm).stream()
            .map(skill -> enrichSkill(skill.getName()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * Get popular skills in a category
     */
    public List<SkillEnrichmentDTO> getPopularSkillsByCategory(SkillNode.SkillCategory category, int limit) {
        return skillGraphRepository.findByCategory(category).stream()
            .sorted(Comparator.comparing(SkillNode::getPopularity).reversed())
            .limit(limit)
            .map(skill -> enrichSkill(skill.getName()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    // ========== Helper Methods ==========

    private List<SkillEnrichmentDTO.RelatedSkillDTO> convertToRelatedSkills(
            List<SkillNode> skills,
            String relationshipType) {
        return skills.stream()
            .map(skill -> SkillEnrichmentDTO.RelatedSkillDTO.builder()
                .skillName(skill.getName())
                .displayName(skill.getDisplayName())
                .relationshipStrength(0.8) // Default, should extract from relationship
                .relationshipType(relationshipType)
                .build())
            .collect(Collectors.toList());
    }

    private List<SkillRecommendationDTO> convertToRecommendations(
            List<SkillNode> skills,
            SkillRecommendationDTO.RecommendationType type,
            String reason) {
        return skills.stream()
            .map(skill -> convertToRecommendation(skill, type, reason))
            .collect(Collectors.toList());
    }

    private SkillRecommendationDTO convertToRecommendation(
            SkillNode skill,
            SkillRecommendationDTO.RecommendationType type,
            String reason) {

        double score = calculateRecommendationScore(skill, type);

        return SkillRecommendationDTO.builder()
            .skillName(skill.getName())
            .displayName(skill.getDisplayName())
            .category(skill.getCategory().name())
            .popularity(skill.getPopularity())
            .trendScore(skill.getTrendScore())
            .avgSalaryImpact(skill.getAvgSalaryImpact())
            .difficultyLevel(skill.getDifficultyLevel().name())
            .description(skill.getDescription())
            .recommendationType(type)
            .recommendationScore(score)
            .reason(reason)
            .build();
    }

    private double calculateRecommendationScore(
            SkillNode skill,
            SkillRecommendationDTO.RecommendationType type) {

        double baseScore = 0.0;

        switch (type) {
            case PREREQUISITE:
                baseScore = 0.95; // Very high priority
                break;
            case COMPLEMENTARY:
                baseScore = 0.80;
                break;
            case TRENDING:
                baseScore = skill.getTrendScore() != null ? skill.getTrendScore() : 0.70;
                break;
            case HIGH_SALARY_IMPACT:
                baseScore = 0.75;
                break;
            case SKILL_GAP:
                baseScore = 0.70;
                break;
            case CAREER_GROWTH:
                baseScore = 0.65;
                break;
        }

        // Boost by popularity and trend
        double popularityBoost = (skill.getPopularity() != null ? skill.getPopularity() : 0.5) * 0.1;
        double trendBoost = (skill.getTrendScore() != null ? skill.getTrendScore() : 0.5) * 0.1;

        return Math.min(baseScore + popularityBoost + trendBoost, 1.0);
    }
}
