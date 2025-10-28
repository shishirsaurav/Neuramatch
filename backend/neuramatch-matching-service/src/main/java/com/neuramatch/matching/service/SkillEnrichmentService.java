package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.SkillEnrichmentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for enriching resume skills with knowledge graph data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillEnrichmentService {

    private final SkillGraphService skillGraphService;

    /**
     * Enrich a list of skills with graph data
     * Resolves synonyms and adds relationship information
     */
    public List<SkillEnrichmentDTO> enrichSkills(List<String> skillNames) {
        log.debug("Enriching {} skills", skillNames.size());

        return skillNames.stream()
            .map(skillGraphService::enrichSkill)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * Normalize and deduplicate skills using synonyms
     */
    public Set<String> normalizeSkills(List<String> skillNames) {
        log.debug("Normalizing {} skills", skillNames.size());

        return skillNames.stream()
            .map(skillGraphService::resolveSkillSynonym)
            .collect(Collectors.toSet());
    }

    /**
     * Expand skillset with implicit skills (prerequisites, part-of relationships)
     */
    public Set<String> expandSkillSet(List<String> skillNames) {
        log.debug("Expanding skillset of {} skills", skillNames.size());

        Set<String> expandedSkills = new HashSet<>();

        for (String skill : skillNames) {
            String normalized = skillGraphService.resolveSkillSynonym(skill);
            expandedSkills.add(normalized);

            // Add implicit skills from enrichment
            Optional<SkillEnrichmentDTO> enriched = skillGraphService.enrichSkill(normalized);
            enriched.ifPresent(e -> {
                // If someone knows Spring Boot, they implicitly know Spring
                if (e.getPartOfEcosystem() != null) {
                    e.getPartOfEcosystem().forEach(parent ->
                        expandedSkills.add(parent.toLowerCase())
                    );
                }
            });
        }

        log.debug("Expanded {} skills to {} skills", skillNames.size(), expandedSkills.size());
        return expandedSkills;
    }

    /**
     * Find skill gaps and provide recommendations
     */
    public Map<String, Object> analyzeSkillSet(List<String> skillNames) {
        log.debug("Analyzing skillset of {} skills", skillNames.size());

        Set<String> normalizedSkills = normalizeSkills(skillNames);
        List<SkillEnrichmentDTO> enrichedSkills = enrichSkills(new ArrayList<>(normalizedSkills));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalSkills", normalizedSkills.size());
        analysis.put("enrichedSkills", enrichedSkills);

        // Categorize skills
        Map<String, Long> categoryDistribution = enrichedSkills.stream()
            .collect(Collectors.groupingBy(
                SkillEnrichmentDTO::getCategory,
                Collectors.counting()
            ));
        analysis.put("categoryDistribution", categoryDistribution);

        // Calculate average metrics
        double avgPopularity = enrichedSkills.stream()
            .mapToDouble(s -> s.getPopularity() != null ? s.getPopularity() : 0.0)
            .average()
            .orElse(0.0);

        double avgTrendScore = enrichedSkills.stream()
            .mapToDouble(s -> s.getTrendScore() != null ? s.getTrendScore() : 0.0)
            .average()
            .orElse(0.0);

        int totalSalaryImpact = enrichedSkills.stream()
            .mapToInt(s -> s.getAvgSalaryImpact() != null ? s.getAvgSalaryImpact() : 0)
            .sum();

        analysis.put("averagePopularity", avgPopularity);
        analysis.put("averageTrendScore", avgTrendScore);
        analysis.put("totalSalaryImpact", totalSalaryImpact);

        // Identify skill level distribution
        Map<String, Long> levelDistribution = enrichedSkills.stream()
            .collect(Collectors.groupingBy(
                SkillEnrichmentDTO::getDifficultyLevel,
                Collectors.counting()
            ));
        analysis.put("levelDistribution", levelDistribution);

        return analysis;
    }

    /**
     * Validate if a candidate has prerequisites for a target skill
     */
    public boolean hasPrerequisites(List<String> candidateSkills, String targetSkill) {
        Set<String> normalizedSkills = normalizeSkills(candidateSkills);
        String normalizedTarget = skillGraphService.resolveSkillSynonym(targetSkill);

        Optional<SkillEnrichmentDTO> enriched = skillGraphService.enrichSkill(normalizedTarget);

        if (enriched.isEmpty() || enriched.get().getPrerequisites() == null) {
            return true; // No prerequisites required
        }

        // Check if candidate has all required prerequisites
        List<String> requiredPrereqs = enriched.get().getPrerequisites().stream()
            .map(SkillEnrichmentDTO.RelatedSkillDTO::getSkillName)
            .collect(Collectors.toList());

        Set<String> normalizedPrereqs = normalizeSkills(requiredPrereqs);

        return normalizedSkills.containsAll(normalizedPrereqs);
    }

    /**
     * Calculate skill coverage percentage for job requirements
     */
    public double calculateSkillCoverage(
            List<String> candidateSkills,
            List<String> requiredSkills,
            boolean useAlternatives) {

        if (requiredSkills.isEmpty()) {
            return 1.0;
        }

        Set<String> normalizedCandidate = normalizeSkills(candidateSkills);
        Set<String> normalizedRequired = normalizeSkills(requiredSkills);

        int matchCount = 0;

        for (String required : normalizedRequired) {
            if (normalizedCandidate.contains(required)) {
                matchCount++;
            } else if (useAlternatives) {
                // Check if candidate has an alternative skill
                List<String> alternatives = skillGraphService.findAlternatives(required, 0.75);
                boolean hasAlternative = alternatives.stream()
                    .anyMatch(normalizedCandidate::contains);

                if (hasAlternative) {
                    matchCount++;
                }
            }
        }

        return (double) matchCount / normalizedRequired.size();
    }
}
