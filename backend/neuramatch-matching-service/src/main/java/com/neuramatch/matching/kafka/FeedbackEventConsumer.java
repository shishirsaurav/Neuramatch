package com.neuramatch.matching.kafka;

import com.neuramatch.matching.entity.MatchFeedback;
import com.neuramatch.matching.repository.MatchFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackEventConsumer {

    private final MatchFeedbackRepository feedbackRepository;
    private final FeedbackAggregationService aggregationService;

    @KafkaListener(topics = "feedback-events", groupId = "feedback-processor")
    public void processFeedbackEvent(Map<String, Object> event) {
        log.info("Processing feedback event: {}", event);

        try {
            Long feedbackId = ((Number) event.get("feedbackId")).longValue();
            Long jobId = ((Number) event.get("jobId")).longValue();
            Long resumeId = ((Number) event.get("resumeId")).longValue();
            String action = (String) event.get("action");
            Double weight = ((Number) event.get("weight")).doubleValue();

            // Aggregate feedback for analytics
            aggregationService.aggregateFeedback(jobId, resumeId, action, weight);

            // Check if retraining threshold reached
            long totalFeedback = feedbackRepository.count();
            log.debug("Total feedback count: {}", totalFeedback);

            if (totalFeedback >= 1000 && totalFeedback % 1000 == 0) {
                log.info("RETRAINING THRESHOLD REACHED: {} samples collected", totalFeedback);
                triggerModelRetraining();
            }

            // Update job-specific statistics
            updateJobStatistics(jobId);

        } catch (Exception e) {
            log.error("Error processing feedback event: {}", event, e);
            // Don't rethrow - we don't want to block Kafka consumer
        }
    }

    private void triggerModelRetraining() {
        log.info("Triggering model retraining...");
        // TODO: Integrate with ML pipeline
        // This would typically:
        // 1. Export feedback data
        // 2. Trigger training job (e.g., via REST API to ML service)
        // 3. Update model weights/embeddings
        // 4. Deploy new model version

        // For now, just log the event
        log.info("Model retraining scheduled (placeholder - integrate with ML pipeline)");
    }

    private void updateJobStatistics(Long jobId) {
        // Update cached statistics for job
        Map<String, Object> stats = calculateJobStats(jobId);
        log.debug("Updated statistics for job {}: {}", jobId, stats);
        // TODO: Store in Redis cache for fast retrieval
    }

    private Map<String, Object> calculateJobStats(Long jobId) {
        // Calculate various metrics
        Map<String, Object> stats = new HashMap<>();

        long totalViews = feedbackRepository.countByActionSince(
                MatchFeedback.FeedbackAction.VIEWED,
                java.time.LocalDateTime.now().minusDays(30)
        );

        long totalHired = feedbackRepository.countByActionSince(
                MatchFeedback.FeedbackAction.HIRED,
                java.time.LocalDateTime.now().minusDays(30)
        );

        double conversionRate = totalViews > 0 ?
            (double) totalHired / totalViews * 100 : 0.0;

        stats.put("totalViews", totalViews);
        stats.put("totalHired", totalHired);
        stats.put("conversionRate", conversionRate);

        return stats;
    }
}
