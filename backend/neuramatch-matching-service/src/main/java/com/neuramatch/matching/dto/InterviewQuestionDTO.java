package com.neuramatch.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionDTO {
    private String skill;
    private String type; // TECHNICAL_CONCEPTUAL, TECHNICAL_PRACTICAL, BEHAVIORAL, SCENARIO, SKILL_GAP_ASSESSMENT
    private String difficulty; // EASY, MEDIUM, HARD
    private String question;
    private String suggestedAnswer;
    private List<String> evaluationCriteria;
    private Integer timeLimit; // in minutes
    private List<String> followUpQuestions;
}
