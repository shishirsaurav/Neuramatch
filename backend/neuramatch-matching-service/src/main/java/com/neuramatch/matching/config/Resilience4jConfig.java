package com.neuramatch.matching.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j rate limiting and retry logic
 */
@Configuration
@Slf4j
public class Resilience4jConfig {

    @Value("${openai.ratelimit.requests-per-minute:3000}")
    private int requestsPerMinute;

    @Value("${openai.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Bean
    public RateLimiter openAiRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .limitForPeriod(requestsPerMinute)
            .timeoutDuration(Duration.ofSeconds(30))
            .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = registry.rateLimiter("openai");

        log.info("Configured OpenAI rate limiter: {} requests per minute", requestsPerMinute);
        return rateLimiter;
    }

    @Bean
    public Retry openAiRetry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(maxRetryAttempts)
            .waitDuration(Duration.ofSeconds(2))
            .retryExceptions(
                java.net.SocketTimeoutException.class,
                java.io.IOException.class
            )
            .ignoreExceptions(IllegalArgumentException.class)
            .build();

        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("openai");

        retry.getEventPublisher()
            .onRetry(event -> log.warn("OpenAI API retry attempt {}: {}",
                event.getNumberOfRetryAttempts(),
                event.getLastThrowable().getMessage()))
            .onError(event -> log.error("OpenAI API failed after {} attempts",
                event.getNumberOfRetryAttempts()));

        log.info("Configured OpenAI retry: {} max attempts", maxRetryAttempts);
        return retry;
    }
}
