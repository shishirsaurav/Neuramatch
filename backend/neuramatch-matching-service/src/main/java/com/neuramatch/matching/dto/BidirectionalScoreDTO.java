package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidirectionalScoreDTO {
    private Long jobId;
    private Long resumeId;

    // Individual direction scores
    private Double jobToResumeScore; // Job requirements → Candidate fit (0-1)
    private Double resumeToJobScore; // Candidate goals → Job fit (0-1)

    // Combined scores
    private Double harmonicMean; // Penalizes imbalanced matches
    private Double weightedScore; // 60% job→resume + 40% resume→job
    private Double temporalAdjustedScore; // With skill recency decay
    private Double finalScore; // Final recommended score

    // Analysis
    private String matchQuality; // EXCELLENT, GOOD, FAIR, POOR
    private Boolean isBalanced; // True if both directions are within 20%
}
