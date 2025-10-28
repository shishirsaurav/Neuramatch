package com.neuramatch.matching.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackAggregationService {

    // In-memory aggregation (would use Redis in production)
    private final Map<String, FeedbackAggregate> aggregates = new ConcurrentHashMap<>();

    public void aggregateFeedback(Long jobId, Long resumeId, String action, Double weight) {
        String key = jobId + ":" + resumeId;

        FeedbackAggregate aggregate = aggregates.computeIfAbsent(key,
            k -> new FeedbackAggregate());

        aggregate.addFeedback(action, weight);

        log.debug("Aggregated feedback for {}: {}", key, aggregate);
    }

    public FeedbackAggregate getAggregate(Long jobId, Long resumeId) {
        String key = jobId + ":" + resumeId;
        return aggregates.getOrDefault(key, new FeedbackAggregate());
    }

    public static class FeedbackAggregate {
        private int totalFeedback = 0;
        private double totalWeight = 0.0;
        private Map<String, Integer> actionCounts = new ConcurrentHashMap<>();

        public void addFeedback(String action, Double weight) {
            totalFeedback++;
            totalWeight += weight;
            actionCounts.merge(action, 1, Integer::sum);
        }

        public double getAverageWeight() {
            return totalFeedback > 0 ? totalWeight / totalFeedback : 0.0;
        }

        public int getTotalFeedback() {
            return totalFeedback;
        }

        public Map<String, Integer> getActionCounts() {
            return actionCounts;
        }

        @Override
        public String toString() {
            return String.format("FeedbackAggregate{total=%d, avgWeight=%.2f, actions=%s}",
                    totalFeedback, getAverageWeight(), actionCounts);
        }
    }
}
