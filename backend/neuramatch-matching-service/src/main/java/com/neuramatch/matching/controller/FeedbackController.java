package com.neuramatch.matching.controller;

import com.neuramatch.matching.dto.FeedbackDTO;
import com.neuramatch.matching.dto.FeedbackRequest;
import com.neuramatch.matching.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackDTO> recordFeedback(@RequestBody FeedbackRequest request) {
        log.info("POST /api/v1/feedback - Recording feedback for job={}, resume={}",
                request.getJobId(), request.getResumeId());
        FeedbackDTO feedback = feedbackService.recordFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(feedback);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackForJob(@PathVariable Long jobId) {
        log.info("GET /api/v1/feedback/job/{}", jobId);
        List<FeedbackDTO> feedback = feedbackService.getFeedbackForJob(jobId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/resume/{resumeId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbackForResume(@PathVariable Long resumeId) {
        log.info("GET /api/v1/feedback/resume/{}", resumeId);
        List<FeedbackDTO> feedback = feedbackService.getFeedbackForResume(resumeId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/job/{jobId}/statistics")
    public ResponseEntity<Map<String, Object>> getFeedbackStatistics(@PathVariable Long jobId) {
        log.info("GET /api/v1/feedback/job/{}/statistics", jobId);
        Map<String, Object> stats = feedbackService.getFeedbackStatistics(jobId);
        return ResponseEntity.ok(stats);
    }
}
