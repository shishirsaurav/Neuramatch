package com.neuramatch.matching.embedding;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.neuramatch.matching.config.GeminiConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating embeddings using Google Gemini API
 */
@Service
@Slf4j
public class GeminiEmbeddingService {

    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta";

    private final GeminiConfig geminiConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final RateLimiter rateLimiter;
    private final Retry retry;
    private final EmbeddingCacheService cacheService;

    public GeminiEmbeddingService(
            GeminiConfig geminiConfig,
            OkHttpClient geminiHttpClient,
            EmbeddingCacheService cacheService) {

        this.geminiConfig = geminiConfig;
        this.httpClient = geminiHttpClient;
        this.cacheService = cacheService;
        this.gson = new Gson();

        // Rate limiter configuration
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
            .limitForPeriod(geminiConfig.getRatelimit().getRequestsPerMinute())
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(10))
            .build();

        this.rateLimiter = RateLimiter.of("gemini-api", rateLimiterConfig);

        // Retry configuration
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(geminiConfig.getRetry().getMaxAttempts())
            .waitDuration(Duration.ofSeconds(2))
            .retryExceptions(IOException.class)
            .build();

        this.retry = Retry.of("gemini-api", retryConfig);
    }

    /**
     * Generate embedding for a single text
     */
    public List<Double> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Empty text provided for embedding generation");
            return new ArrayList<>();
        }

        // Check cache first
        String model = geminiConfig.getEmbedding().getModel();
        java.util.Optional<List<Double>> cachedOpt = cacheService.getCachedEmbedding(text, model);
        if (cachedOpt.isPresent()) {
            log.debug("Cache hit for text: {}", text.substring(0, Math.min(50, text.length())));
            return cachedOpt.get();
        }

        // Generate new embedding
        log.debug("Generating embedding for text: {}", text.substring(0, Math.min(50, text.length())));

        try {
            List<Double> embedding = rateLimiter.executeSupplier(() ->
                Retry.decorateSupplier(retry, () -> {
                    try {
                        return callGeminiEmbeddingApi(text);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to call Gemini API", e);
                    }
                }).get()
            );

            // Cache the result
            cacheService.cacheEmbedding(text, model, embedding);

            return embedding;
        } catch (Exception e) {
            log.error("Failed to generate embedding: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    /**
     * Generate embeddings for multiple texts in batch
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("Generating embeddings for {} texts", texts.size());

        // Process in batches to avoid API limits
        int batchSize = geminiConfig.getEmbedding().getBatchSize();
        List<List<Double>> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);

            log.debug("Processing batch {}/{}", (i / batchSize) + 1,
                (texts.size() + batchSize - 1) / batchSize);

            List<List<Double>> batchEmbeddings = batch.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());

            allEmbeddings.addAll(batchEmbeddings);
        }

        log.info("Generated {} embeddings", allEmbeddings.size());
        return allEmbeddings;
    }

    /**
     * Call Gemini API to generate embedding
     */
    private List<Double> callGeminiEmbeddingApi(String text) throws IOException {
        String url = String.format("%s/%s:embedContent?key=%s",
            GEMINI_API_BASE,
            geminiConfig.getEmbedding().getModel(),
            geminiConfig.getApi().getKey());

        // Build request body
        JsonObject requestBody = new JsonObject();
        JsonObject content = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", text);

        content.add("parts", gson.toJsonTree(new JsonObject[]{parts}));
        requestBody.add("content", content);

        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Gemini API error: {} - {}", response.code(), errorBody);
                throw new IOException("Gemini API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // Extract embedding from response
            if (jsonResponse.has("embedding") &&
                jsonResponse.getAsJsonObject("embedding").has("values")) {

                List<Double> embedding = new ArrayList<>();
                jsonResponse.getAsJsonObject("embedding")
                    .getAsJsonArray("values")
                    .forEach(element -> embedding.add(element.getAsDouble()));

                log.debug("Generated embedding with {} dimensions", embedding.size());
                return embedding;
            } else {
                log.error("Invalid response format from Gemini API: {}", responseBody);
                throw new IOException("Invalid response format");
            }
        }
    }

    /**
     * Get embedding dimensions
     */
    public int getEmbeddingDimensions() {
        return geminiConfig.getEmbedding().getDimensions();
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double cosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1 == null || embedding2 == null ||
            embedding1.size() != embedding2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
