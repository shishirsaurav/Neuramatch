package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryBenchmarkDTO {
    private List<String> skills;
    private Integer experienceYears;
    private String location;
    private String industry;

    // Salary statistics
    private Integer min;
    private Integer max;
    private Integer median;
    private Integer average;
    private Integer percentile25;
    private Integer percentile75;

    // Metadata
    private Integer sampleSize;
    private String confidence; // HIGH, MEDIUM, LOW
    private String currency;
    private String period; // YEARLY, MONTHLY, HOURLY
    private LocalDateTime generatedAt;
}
