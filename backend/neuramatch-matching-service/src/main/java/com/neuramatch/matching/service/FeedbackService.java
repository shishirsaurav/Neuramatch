package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.FeedbackDTO;
import com.neuramatch.matching.dto.FeedbackRequest;
import com.neuramatch.matching.entity.MatchFeedback;
import com.neuramatch.matching.repository.MatchFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final MatchFeedbackRepository feedbackRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Transactional
    public FeedbackDTO recordFeedback(FeedbackRequest request) {
        log.info("Recording feedback: action={}, jobId={}, resumeId={}",
                request.getAction(), request.getJobId(), request.getResumeId());

        // Save feedback
        MatchFeedback feedback = MatchFeedback.builder()
                .jobId(request.getJobId())
                .resumeId(request.getResumeId())
                .recruiterId(request.getRecruiterId())
                .action(MatchFeedback.FeedbackAction.valueOf(request.getAction()))
                .notes(request.getNotes())
                .originalScore(request.getOriginalScore())
                .build();

        feedback = feedbackRepository.save(feedback);

        // Publish to Kafka for processing
        publishFeedbackEvent(feedback);

        return convertToDTO(feedback);
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTO> getFeedbackForJob(Long jobId) {
        return feedbackRepository.findByJobId(jobId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackDTO> getFeedbackForResume(Long resumeId) {
        return feedbackRepository.findByResumeId(resumeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFeedbackStatistics(Long jobId) {
        List<MatchFeedback> feedbacks = feedbackRepository.findByJobId(jobId);

        Map<String, Long> actionCounts = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getAction().name(),
                        Collectors.counting()
                ));

        long totalViews = actionCounts.getOrDefault("VIEWED", 0L);
        long shortlisted = actionCounts.getOrDefault("SHORTLISTED", 0L);
        long interviewed = actionCounts.getOrDefault("INTERVIEWED", 0L);
        long hired = actionCounts.getOrDefault("HIRED", 0L);
        long rejected = actionCounts.getOrDefault("REJECTED", 0L);

        double conversionRate = totalViews > 0 ?
                (double) hired / totalViews * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFeedback", feedbacks.size());
        stats.put("actionCounts", actionCounts);
        stats.put("totalViews", totalViews);
        stats.put("shortlisted", shortlisted);
        stats.put("interviewed", interviewed);
        stats.put("hired", hired);
        stats.put("rejected", rejected);
        stats.put("conversionRate", conversionRate);

        return stats;
    }

    private void publishFeedbackEvent(MatchFeedback feedback) {
        Map<String, Object> event = new HashMap<>();
        event.put("feedbackId", feedback.getId());
        event.put("jobId", feedback.getJobId());
        event.put("resumeId", feedback.getResumeId());
        event.put("recruiterId", feedback.getRecruiterId());
        event.put("action", feedback.getAction().name());
        event.put("weight", feedback.getAction().getWeight());
        event.put("originalScore", feedback.getOriginalScore());
        event.put("timestamp", feedback.getCreatedAt().toString());

        kafkaTemplate.send("feedback-events", event);
        log.debug("Published feedback event to Kafka: {}", event);
    }

    private FeedbackDTO convertToDTO(MatchFeedback feedback) {
        return FeedbackDTO.builder()
                .id(feedback.getId())
                .jobId(feedback.getJobId())
                .resumeId(feedback.getResumeId())
                .recruiterId(feedback.getRecruiterId())
                .action(feedback.getAction().name())
                .actionWeight(feedback.getAction().getWeight())
                .notes(feedback.getNotes())
                .originalScore(feedback.getOriginalScore())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
