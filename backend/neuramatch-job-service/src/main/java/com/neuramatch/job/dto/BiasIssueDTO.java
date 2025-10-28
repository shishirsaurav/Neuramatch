package com.neuramatch.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiasIssueDTO {
    private String type; // AGE_BIAS, GENDER_CODED, DISABILITY_BIAS, RACIAL_BIAS
    private String term; // The problematic term found
    private String context; // Surrounding context
    private String severity; // LOW, MEDIUM, HIGH
    private String suggestion; // Alternative wording
    private String explanation; // Why this is problematic
}
