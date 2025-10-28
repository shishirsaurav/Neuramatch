package com.neuramatch.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeUploadResponse {

    private Long resumeId;
    private String message;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Integer qualityScore;
    private String status;
    private LocalDateTime uploadedAt;
    private Boolean hasErrors;
    private String errorMessage;
}
