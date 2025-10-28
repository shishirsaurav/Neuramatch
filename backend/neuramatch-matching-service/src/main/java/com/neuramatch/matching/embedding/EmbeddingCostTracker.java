package com.neuramatch.matching.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks API usage and costs for embedding generation
 */
@Component
@Slf4j
public class EmbeddingCostTracker {

    // Pricing per 1K tokens (as of 2024)
    private static final Map<String, Double> MODEL_PRICING = Map.of(
        "text-embedding-3-large", 0.00013,  // $0.13 per 1M tokens
        "text-embedding-3-small", 0.00002,   // $0.02 per 1M tokens
        "text-embedding-ada-002", 0.0001     // $0.10 per 1M tokens
    );

    private final AtomicInteger totalApiCalls = new AtomicInteger(0);
    private final AtomicInteger totalTextsEmbedded = new AtomicInteger(0);
    private final AtomicLong totalTokensUsed = new AtomicLong(0);
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    private final Map<String, AtomicInteger> callsByModel = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> tokensByModel = new ConcurrentHashMap<>();

    /**
     * Record an API call
     */
    public void recordApiCall(int numTexts, String model, long latencyMs) {
        totalApiCalls.incrementAndGet();
        totalTextsEmbedded.addAndGet(numTexts);
        totalLatencyMs.addAndGet(latencyMs);

        // Estimate tokens (rough approximation: 1 token ≈ 4 characters)
        // For more accuracy, would need to use tiktoken library
        long estimatedTokens = numTexts * 100; // Assume avg 100 tokens per text

        totalTokensUsed.addAndGet(estimatedTokens);

        callsByModel.computeIfAbsent(model, k -> new AtomicInteger(0)).incrementAndGet();
        tokensByModel.computeIfAbsent(model, k -> new AtomicLong(0)).addAndGet(estimatedTokens);

        if (totalApiCalls.get() % 100 == 0) {
            logStats();
        }
    }

    /**
     * Get total estimated cost
     */
    public double getTotalCost() {
        double totalCost = 0.0;

        for (Map.Entry<String, AtomicLong> entry : tokensByModel.entrySet()) {
            String model = entry.getKey();
            long tokens = entry.getValue().get();

            double pricePerToken = MODEL_PRICING.getOrDefault(model, 0.0001) / 1000.0;
            totalCost += tokens * pricePerToken;
        }

        return totalCost;
    }

    /**
     * Get average latency per API call
     */
    public double getAverageLatencyMs() {
        int calls = totalApiCalls.get();
        return calls == 0 ? 0.0 : (double) totalLatencyMs.get() / calls;
    }

    /**
     * Get cache hit rate
     */
    public double getCacheHitRate() {
        // Will be implemented when cache is added
        return 0.0;
    }

    /**
     * Log statistics
     */
    public void logStats() {
        log.info("=== Embedding API Statistics ===");
        log.info("Total API calls: {}", totalApiCalls.get());
        log.info("Total texts embedded: {}", totalTextsEmbedded.get());
        log.info("Total tokens used (estimated): {}", totalTokensUsed.get());
        log.info("Total cost (estimated): ${}", String.format("%.4f", getTotalCost()));
        log.info("Average latency: {}ms", String.format("%.2f", getAverageLatencyMs()));
        log.info("Calls by model: {}", callsByModel);
        log.info("================================");
    }

    /**
     * Get statistics as map
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "totalApiCalls", totalApiCalls.get(),
            "totalTextsEmbedded", totalTextsEmbedded.get(),
            "totalTokensUsed", totalTokensUsed.get(),
            "estimatedCost", getTotalCost(),
            "averageLatencyMs", getAverageLatencyMs(),
            "callsByModel", callsByModel,
            "cacheHitRate", getCacheHitRate()
        );
    }

    /**
     * Reset statistics
     */
    public void reset() {
        totalApiCalls.set(0);
        totalTextsEmbedded.set(0);
        totalTokensUsed.set(0);
        totalLatencyMs.set(0);
        callsByModel.clear();
        tokensByModel.clear();

        log.info("Embedding cost tracker statistics reset");
    }
}
