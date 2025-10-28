package com.neuramatch.matching.config;

import lombok.Data;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Google Gemini API
 */
@Configuration
@ConfigurationProperties(prefix = "gemini")
@Data
public class GeminiConfig {

    private ApiConfig api;
    private EmbeddingConfig embedding;
    private TextConfig text;
    private RateLimitConfig ratelimit;
    private RetryConfig retry;

    @Data
    public static class ApiConfig {
        private String key;
        private int timeout;
    }

    @Data
    public static class EmbeddingConfig {
        private String model;
        private int dimensions;
        private int batchSize;
    }

    @Data
    public static class TextConfig {
        private String model;
    }

    @Data
    public static class RateLimitConfig {
        private int requestsPerMinute;
    }

    @Data
    public static class RetryConfig {
        private int maxAttempts;
    }

    @Bean
    public OkHttpClient geminiHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(api != null ? api.getTimeout() : 60))
            .readTimeout(Duration.ofSeconds(api != null ? api.getTimeout() : 60))
            .writeTimeout(Duration.ofSeconds(api != null ? api.getTimeout() : 60))
            .build();
    }
}
