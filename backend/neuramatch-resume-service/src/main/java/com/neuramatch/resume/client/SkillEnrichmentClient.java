package com.neuramatch.resume.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Client for calling the skill enrichment service in matching-service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SkillEnrichmentClient {

    private final RestTemplate restTemplate;

    @Value("${matching.service.url:http://localhost:8083}")
    private String matchingServiceUrl;

    /**
     * Enrich skills with graph data
     */
    public List<Map<String, Object>> enrichSkills(List<String> skillNames) {
        try {
            String url = matchingServiceUrl + "/api/skills/enrich";

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(skillNames),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to enrich skills: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Normalize skills (resolve synonyms)
     */
    public Set<String> normalizeSkills(List<String> skillNames) {
        try {
            String url = matchingServiceUrl + "/api/skills/normalize";

            ResponseEntity<Set<String>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(skillNames),
                new ParameterizedTypeReference<Set<String>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to normalize skills: {}", e.getMessage());
            return new HashSet<>(skillNames);
        }
    }

    /**
     * Get skill recommendations
     */
    public List<Map<String, Object>> getRecommendations(List<String> existingSkills, int limit) {
        try {
            String url = matchingServiceUrl + "/api/skills/recommendations?limit=" + limit;

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(existingSkills),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get skill recommendations: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Analyze skillset
     */
    public Map<String, Object> analyzeSkillSet(List<String> skillNames) {
        try {
            String url = matchingServiceUrl + "/api/skills/analyze";

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(skillNames),
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to analyze skillset: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
