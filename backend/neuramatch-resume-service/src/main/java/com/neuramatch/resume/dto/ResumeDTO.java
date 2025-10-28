package com.neuramatch.resume.dto;

import com.neuramatch.resume.entity.Resume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String summary;
    private String location;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String careerObjective;
    private Integer qualityScore;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<SkillDTO> skills;
    private List<ExperienceDTO> experiences;
    private List<EducationDTO> educations;
}
