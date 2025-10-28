package com.neuramatch.matching.controller;

import com.neuramatch.matching.dto.MatchExplanationDTO;
import com.neuramatch.matching.search.ResumeJobMatchingService;
import com.neuramatch.matching.service.ExplainabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/explainability")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExplainabilityController {

    private final ExplainabilityService explainabilityService;
    private final ResumeJobMatchingService matchingService;

    @GetMapping("/match/{jobId}/{resumeId}")
    public ResponseEntity<MatchExplanationDTO> explainMatch(
            @PathVariable Long jobId,
            @PathVariable Long resumeId) {

        log.info("GET /api/v1/explainability/match/{}/{} - Explaining match", jobId, resumeId);

        // Get match result
        ResumeJobMatchingService.MatchingCriteria criteria =
                ResumeJobMatchingService.MatchingCriteria.builder()
                        .limit(1)
                        .build();

        List<ResumeJobMatchingService.JobMatch> matches =
                matchingService.findMatchingJobsForResume(resumeId, criteria);

        if (matches.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ResumeJobMatchingService.JobMatch match = matches.get(0);

        // Generate explanation
        MatchExplanationDTO explanation = explainabilityService.explainMatch(
                match,
                List.of(), // Would get from resume
                List.of(), // Would get from job
                match.getYearsOfExperience(),
                match.getMinYearsRequired()
        );

        return ResponseEntity.ok(explanation);
    }
}
