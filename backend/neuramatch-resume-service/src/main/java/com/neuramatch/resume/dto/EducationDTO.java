package com.neuramatch.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationDTO {

    private Long id;
    private String institutionName;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Double gpa;
    private String maxGpa;
    private String achievements;
    private String educationLevel;
}
