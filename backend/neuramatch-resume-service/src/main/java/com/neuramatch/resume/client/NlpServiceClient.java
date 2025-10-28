package com.neuramatch.resume.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NlpServiceClient {

    private final RestTemplate restTemplate;
    private final String nlpServiceUrl;

    public NlpServiceClient(
            RestTemplate restTemplate,
            @Value("${nlp.service.url:http://localhost:5000}") String nlpServiceUrl) {
        this.restTemplate = restTemplate;
        this.nlpServiceUrl = nlpServiceUrl;
    }

    /**
     * Extract all entities from resume text
     */
    public Map<String, Object> extractEntities(String text) {
        try {
            String url = nlpServiceUrl + "/extract";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = new HashMap<>();
            request.put("text", text);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully extracted entities from NLP service");
                return response.getBody();
            } else {
                log.error("NLP service returned status: {}", response.getStatusCode());
                return new HashMap<>();
            }

        } catch (Exception e) {
            log.error("Error calling NLP service", e);
            return new HashMap<>();
        }
    }

    /**
     * Check if NLP service is healthy
     */
    public boolean isHealthy() {
        try {
            String url = nlpServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("NLP service health check failed", e);
            return false;
        }
    }
}
