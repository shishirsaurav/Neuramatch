package com.neuramatch.matching.controller;

import com.neuramatch.matching.dto.SkillEnrichmentDTO;
import com.neuramatch.matching.dto.SkillRecommendationDTO;
import com.neuramatch.matching.service.SkillEnrichmentService;
import com.neuramatch.matching.service.SkillGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for skill knowledge graph operations
 */
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Slf4j
public class SkillGraphController {

    private final SkillGraphService skillGraphService;
    private final SkillEnrichmentService skillEnrichmentService;

    /**
     * Enrich a single skill
     */
    @GetMapping("/{skillName}/enrich")
    public ResponseEntity<SkillEnrichmentDTO> enrichSkill(@PathVariable String skillName) {
        log.debug("GET /api/skills/{}/enrich", skillName);

        return skillGraphService.enrichSkill(skillName)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Enrich multiple skills
     */
    @PostMapping("/enrich")
    public ResponseEntity<List<SkillEnrichmentDTO>> enrichSkills(@RequestBody List<String> skillNames) {
        log.debug("POST /api/skills/enrich - {} skills", skillNames.size());

        List<SkillEnrichmentDTO> enriched = skillEnrichmentService.enrichSkills(skillNames);
        return ResponseEntity.ok(enriched);
    }

    /**
     * Normalize skills (resolve synonyms)
     */
    @PostMapping("/normalize")
    public ResponseEntity<Set<String>> normalizeSkills(@RequestBody List<String> skillNames) {
        log.debug("POST /api/skills/normalize - {} skills", skillNames.size());

        Set<String> normalized = skillEnrichmentService.normalizeSkills(skillNames);
        return ResponseEntity.ok(normalized);
    }

    /**
     * Expand skillset with implicit skills
     */
    @PostMapping("/expand")
    public ResponseEntity<Set<String>> expandSkillSet(@RequestBody List<String> skillNames) {
        log.debug("POST /api/skills/expand - {} skills", skillNames.size());

        Set<String> expanded = skillEnrichmentService.expandSkillSet(skillNames);
        return ResponseEntity.ok(expanded);
    }

    /**
     * Get skill recommendations
     */
    @PostMapping("/recommendations")
    public ResponseEntity<List<SkillRecommendationDTO>> getRecommendations(
            @RequestBody Set<String> existingSkills,
            @RequestParam(defaultValue = "10") int limit) {

        log.debug("POST /api/skills/recommendations - {} existing skills, limit={}", existingSkills.size(), limit);

        List<SkillRecommendationDTO> recommendations = skillGraphService.getRecommendations(existingSkills, limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Find missing prerequisites for a skill
     */
    @GetMapping("/{skillName}/prerequisites/missing")
    public ResponseEntity<List<SkillRecommendationDTO>> findMissingPrerequisites(
            @PathVariable String skillName,
            @RequestParam Set<String> existingSkills) {

        log.debug("GET /api/skills/{}/prerequisites/missing - {} existing skills", skillName, existingSkills.size());

        List<SkillRecommendationDTO> missing = skillGraphService.findMissingPrerequisites(skillName, existingSkills);
        return ResponseEntity.ok(missing);
    }

    /**
     * Find alternative skills
     */
    @GetMapping("/{skillName}/alternatives")
    public ResponseEntity<List<String>> findAlternatives(
            @PathVariable String skillName,
            @RequestParam(defaultValue = "0.75") double minTransferability) {

        log.debug("GET /api/skills/{}/alternatives - minTransferability={}", skillName, minTransferability);

        List<String> alternatives = skillGraphService.findAlternatives(skillName, minTransferability);
        return ResponseEntity.ok(alternatives);
    }

    /**
     * Analyze skillset
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeSkillSet(@RequestBody List<String> skillNames) {
        log.debug("POST /api/skills/analyze - {} skills", skillNames.size());

        Map<String, Object> analysis = skillEnrichmentService.analyzeSkillSet(skillNames);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Calculate skill coverage for job requirements
     */
    @PostMapping("/coverage")
    public ResponseEntity<Map<String, Object>> calculateCoverage(
            @RequestBody CoverageRequest request) {

        log.debug("POST /api/skills/coverage - {} candidate skills vs {} required skills",
            request.getCandidateSkills().size(), request.getRequiredSkills().size());

        double coverage = skillEnrichmentService.calculateSkillCoverage(
            request.getCandidateSkills(),
            request.getRequiredSkills(),
            request.isUseAlternatives()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("coverage", coverage);
        result.put("coveragePercentage", Math.round(coverage * 100));

        return ResponseEntity.ok(result);
    }

    /**
     * Search skills
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillEnrichmentDTO>> searchSkills(@RequestParam String query) {
        log.debug("GET /api/skills/search?query={}", query);

        List<SkillEnrichmentDTO> results = skillGraphService.searchSkills(query);
        return ResponseEntity.ok(results);
    }

    /**
     * Calculate similarity between two skillsets
     */
    @PostMapping("/similarity")
    public ResponseEntity<Map<String, Object>> calculateSimilarity(@RequestBody SimilarityRequest request) {
        log.debug("POST /api/skills/similarity - {} skills vs {} skills",
            request.getSkillSet1().size(), request.getSkillSet2().size());

        double similarity = skillGraphService.calculateSkillSetSimilarity(
            request.getSkillSet1(),
            request.getSkillSet2()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("similarity", similarity);
        result.put("similarityPercentage", Math.round(similarity * 100));

        return ResponseEntity.ok(result);
    }

    // ========== Request DTOs ==========

    @lombok.Data
    public static class CoverageRequest {
        private List<String> candidateSkills;
        private List<String> requiredSkills;
        private boolean useAlternatives = true;
    }

    @lombok.Data
    public static class SimilarityRequest {
        private Set<String> skillSet1;
        private Set<String> skillSet2;
    }
}
