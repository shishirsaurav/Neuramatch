package com.neuramatch.resume.service;

import com.neuramatch.resume.client.NlpServiceClient;
import com.neuramatch.resume.entity.Education;
import com.neuramatch.resume.entity.Experience;
import com.neuramatch.resume.entity.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NlpExtractionServiceTest {

    @Mock
    private NlpServiceClient nlpServiceClient;

    @InjectMocks
    private NlpExtractionService nlpExtractionService;

    private Map<String, Object> mockNlpResponse;

    @BeforeEach
    void setUp() {
        mockNlpResponse = new HashMap<>();

        // Mock contact info
        Map<String, Object> contact = new HashMap<>();
        contact.put("fullName", "John Doe");
        contact.put("email", "john.doe@example.com");
        contact.put("phone", "123-456-7890");
        contact.put("linkedinUrl", "https://linkedin.com/in/johndoe");
        contact.put("githubUrl", "https://github.com/johndoe");
        mockNlpResponse.put("contact", contact);

        // Mock skills
        Map<String, Object> skill1 = new HashMap<>();
        skill1.put("skillName", "Java");
        skill1.put("category", "PROGRAMMING_LANGUAGE");
        skill1.put("proficiencyLevel", "EXPERT");
        skill1.put("confidenceScore", 0.9);

        Map<String, Object> skill2 = new HashMap<>();
        skill2.put("skillName", "Spring Boot");
        skill2.put("category", "FRAMEWORK");
        skill2.put("proficiencyLevel", "ADVANCED");
        skill2.put("confidenceScore", 0.85);

        mockNlpResponse.put("skills", List.of(skill1, skill2));

        // Mock experiences
        Map<String, Object> exp1 = new HashMap<>();
        exp1.put("jobTitle", "Senior Software Engineer");
        exp1.put("companyName", "Tech Corp");
        exp1.put("startDate", "2020-01-01");
        exp1.put("endDate", "2023-12-31");
        exp1.put("isCurrentRole", false);
        exp1.put("description", "Led development of microservices");
        exp1.put("teamSize", 5);
        exp1.put("leadershipRole", "Lead");
        exp1.put("impactMetrics", "40%");

        mockNlpResponse.put("experiences", List.of(exp1));

        // Mock education
        Map<String, Object> edu1 = new HashMap<>();
        edu1.put("degree", "Bachelor of Science");
        edu1.put("fieldOfStudy", "Computer Science");
        edu1.put("institutionName", "State University");
        edu1.put("startDate", "2012-09-01");
        edu1.put("endDate", "2016-05-01");
        edu1.put("gpa", 3.8);
        edu1.put("educationLevel", "BACHELORS");

        mockNlpResponse.put("education", List.of(edu1));
    }

    @Test
    void testExtractResumeData_Success() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(mockNlpResponse);

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getFullName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getPhone()).isEqualTo("123-456-7890");
        assertThat(result.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/johndoe");
        assertThat(result.getGithubUrl()).isEqualTo("https://github.com/johndoe");
    }

    @Test
    void testExtractSkills() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(mockNlpResponse);

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getSkills()).hasSize(2);

        Skill javaSkill = result.getSkills().get(0);
        assertThat(javaSkill.getSkillName()).isEqualTo("Java");
        assertThat(javaSkill.getCategory()).isEqualTo(Skill.SkillCategory.PROGRAMMING_LANGUAGE);
        assertThat(javaSkill.getProficiencyLevel()).isEqualTo(Skill.ProficiencyLevel.EXPERT);
        assertThat(javaSkill.getConfidenceScore()).isEqualTo(0.9);

        Skill springSkill = result.getSkills().get(1);
        assertThat(springSkill.getSkillName()).isEqualTo("Spring Boot");
        assertThat(springSkill.getCategory()).isEqualTo(Skill.SkillCategory.FRAMEWORK);
        assertThat(springSkill.getProficiencyLevel()).isEqualTo(Skill.ProficiencyLevel.ADVANCED);
    }

    @Test
    void testExtractExperiences() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(mockNlpResponse);

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getExperiences()).hasSize(1);

        Experience exp = result.getExperiences().get(0);
        assertThat(exp.getJobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(exp.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(exp.getIsCurrentRole()).isFalse();
        assertThat(exp.getTeamSize()).isEqualTo(5);
        assertThat(exp.getLeadershipRole()).isEqualTo("Lead");
        assertThat(exp.getImpactMetrics()).isEqualTo("40%");
    }

    @Test
    void testExtractEducation() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(mockNlpResponse);

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getEducations()).hasSize(1);

        Education edu = result.getEducations().get(0);
        assertThat(edu.getDegree()).isEqualTo("Bachelor of Science");
        assertThat(edu.getFieldOfStudy()).isEqualTo("Computer Science");
        assertThat(edu.getInstitutionName()).isEqualTo("State University");
        assertThat(edu.getGpa()).isEqualTo(3.8);
        assertThat(edu.getEducationLevel()).isEqualTo(Education.EducationLevel.BACHELORS);
    }

    @Test
    void testExtractResumeData_EmptyResponse() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(new HashMap<>());

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getFullName()).isNull();
        assertThat(result.getSkills()).isEmpty();
        assertThat(result.getExperiences()).isEmpty();
        assertThat(result.getEducations()).isEmpty();
    }

    @Test
    void testExtractResumeData_NullResponse() {
        // Given
        when(nlpServiceClient.extractEntities(anyString())).thenReturn(null);

        // When
        NlpExtractionService.ExtractedResumeData result = nlpExtractionService.extractResumeData("sample resume text");

        // Then
        assertThat(result.getSkills()).isEmpty();
        assertThat(result.getExperiences()).isEmpty();
        assertThat(result.getEducations()).isEmpty();
    }
}
