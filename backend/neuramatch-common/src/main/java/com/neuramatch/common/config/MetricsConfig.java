package com.neuramatch.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter matchRequestCounter(MeterRegistry registry) {
        return Counter.builder("neuramatch.matches.requests.total")
                .description("Total number of match requests")
                .tag("service", "matching")
                .register(registry);
    }

    @Bean
    public Counter resumeUploadCounter(MeterRegistry registry) {
        return Counter.builder("neuramatch.resumes.uploads.total")
                .description("Total number of resume uploads")
                .tag("service", "resume")
                .register(registry);
    }

    @Bean
    public Counter jobPostingCounter(MeterRegistry registry) {
        return Counter.builder("neuramatch.jobs.postings.total")
                .description("Total number of job postings created")
                .tag("service", "job")
                .register(registry);
    }

    @Bean
    public Timer embeddingGenerationTimer(MeterRegistry registry) {
        return Timer.builder("neuramatch.embeddings.generation.duration")
                .description("Time to generate embeddings")
                .tag("model", "text-embedding-3-large")
                .tag("service", "matching")
                .register(registry);
    }

    @Bean
    public Timer vectorSearchTimer(MeterRegistry registry) {
        return Timer.builder("neuramatch.vector.search.duration")
                .description("Time to perform vector similarity search")
                .tag("engine", "pgvector")
                .tag("service", "matching")
                .register(registry);
    }

    @Bean
    public Timer matchScoringTimer(MeterRegistry registry) {
        return Timer.builder("neuramatch.matching.scoring.duration")
                .description("Time to calculate match scores")
                .tag("service", "matching")
                .register(registry);
    }

    @Bean
    public Counter feedbackEventCounter(MeterRegistry registry) {
        return Counter.builder("neuramatch.feedback.events.total")
                .description("Total number of feedback events")
                .tag("service", "matching")
                .register(registry);
    }

    @Bean
    public Counter biasDetectionCounter(MeterRegistry registry) {
        return Counter.builder("neuramatch.bias.detections.total")
                .description("Total number of bias issues detected")
                .tag("service", "job")
                .register(registry);
    }
}
