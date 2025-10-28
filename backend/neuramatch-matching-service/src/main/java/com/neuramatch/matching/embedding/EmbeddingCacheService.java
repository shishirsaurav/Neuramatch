package com.neuramatch.matching.embedding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis-based cache for embeddings to reduce API costs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "embedding:";
    private static final Duration DEFAULT_TTL = Duration.ofDays(30);

    /**
     * Get cached embedding for text
     */
    public Optional<List<Double>> getCachedEmbedding(String text, String model) {
        String cacheKey = generateCacheKey(text, model);

        try {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);

            if (cachedJson != null) {
                log.debug("Cache HIT for text hash: {}", cacheKey);
                List<Double> embedding = objectMapper.readValue(cachedJson, new TypeReference<List<Double>>() {});
                return Optional.of(embedding);
            }

            log.debug("Cache MISS for text hash: {}", cacheKey);
            return Optional.empty();

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached embedding: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cache embedding for text
     */
    public void cacheEmbedding(String text, String model, List<Double> embedding) {
        String cacheKey = generateCacheKey(text, model);

        try {
            String embeddingJson = objectMapper.writeValueAsString(embedding);
            redisTemplate.opsForValue().set(cacheKey, embeddingJson, DEFAULT_TTL);

            log.debug("Cached embedding for text hash: {}", cacheKey);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize embedding for caching: {}", e.getMessage());
        }
    }

    /**
     * Get cached embeddings for multiple texts
     */
    public List<Optional<List<Double>>> getCachedEmbeddings(List<String> texts, String model) {
        List<Optional<List<Double>>> results = new ArrayList<>();

        for (String text : texts) {
            results.add(getCachedEmbedding(text, model));
        }

        long cacheHits = results.stream().filter(Optional::isPresent).count();
        log.debug("Batch cache: {} hits out of {} texts", cacheHits, texts.size());

        return results;
    }

    /**
     * Cache embeddings for multiple texts
     */
    public void cacheEmbeddings(List<String> texts, String model, List<List<Double>> embeddings) {
        if (texts.size() != embeddings.size()) {
            log.error("Texts and embeddings size mismatch: {} vs {}", texts.size(), embeddings.size());
            return;
        }

        for (int i = 0; i < texts.size(); i++) {
            cacheEmbedding(texts.get(i), model, embeddings.get(i));
        }

        log.debug("Cached {} embeddings", embeddings.size());
    }

    /**
     * Invalidate cached embedding
     */
    public void invalidateCache(String text, String model) {
        String cacheKey = generateCacheKey(text, model);
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated cache for text hash: {}", cacheKey);
    }

    /**
     * Clear all cached embeddings
     */
    public void clearAllCache() {
        redisTemplate.keys(CACHE_PREFIX + "*").forEach(redisTemplate::delete);
        log.info("Cleared all embedding cache");
    }

    /**
     * Generate cache key from text and model
     * Uses SHA-256 hash to create consistent, compact keys
     */
    private String generateCacheKey(String text, String model) {
        String combined = model + ":" + text.trim().toLowerCase();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return CACHE_PREFIX + hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 not available
            return CACHE_PREFIX + combined.hashCode();
        }
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        java.util.Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        long totalKeys = (keys != null) ? keys.size() : 0L;

        return Map.of(
            "totalCachedEmbeddings", totalKeys,
            "cachePrefix", CACHE_PREFIX,
            "cacheTTL", DEFAULT_TTL.toDays() + " days"
        );
    }
}
