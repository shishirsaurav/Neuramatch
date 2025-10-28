package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    private Long jobId;
    private Long resumeId;
    private Long recruiterId;
    private String action; // VIEWED, SHORTLISTED, REJECTED, INTERVIEWED, HIRED, etc.
    private String notes;
    private Double originalScore;
}
