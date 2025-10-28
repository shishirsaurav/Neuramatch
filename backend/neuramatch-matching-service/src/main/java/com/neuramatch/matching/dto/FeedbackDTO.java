package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private Long id;
    private Long jobId;
    private Long resumeId;
    private Long recruiterId;
    private String action;
    private Double actionWeight;
    private String notes;
    private Double originalScore;
    private LocalDateTime createdAt;
}
