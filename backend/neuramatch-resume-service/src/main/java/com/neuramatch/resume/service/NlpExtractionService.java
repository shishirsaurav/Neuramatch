package com.neuramatch.resume.service;

import com.neuramatch.resume.client.NlpServiceClient;
import com.neuramatch.resume.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NlpExtractionService {

    private final NlpServiceClient nlpServiceClient;

    /**
     * Extract all structured data from resume text using NLP
     */
    public ExtractedResumeData extractResumeData(String resumeText) {
        log.info("Extracting resume data using NLP service");

        Map<String, Object> nlpResult = nlpServiceClient.extractEntities(resumeText);

        if (nlpResult == null || nlpResult.isEmpty()) {
            log.warn("NLP service returned empty result");
            return new ExtractedResumeData();
        }

        ExtractedResumeData data = new ExtractedResumeData();

        // Extract contact information
        Map<String, Object> contact = (Map<String, Object>) nlpResult.get("contact");
        if (contact != null) {
            data.setFullName((String) contact.get("fullName"));
            data.setEmail((String) contact.get("email"));
            data.setPhone((String) contact.get("phone"));
            data.setLinkedinUrl((String) contact.get("linkedinUrl"));
            data.setGithubUrl((String) contact.get("githubUrl"));
        }

        // Extract skills
        List<Map<String, Object>> skillsData = (List<Map<String, Object>>) nlpResult.get("skills");
        if (skillsData != null) {
            data.setSkills(convertSkills(skillsData));
        }

        // Extract experiences
        List<Map<String, Object>> experiencesData = (List<Map<String, Object>>) nlpResult.get("experiences");
        if (experiencesData != null) {
            data.setExperiences(convertExperiences(experiencesData));
        }

        // Extract education
        List<Map<String, Object>> educationData = (List<Map<String, Object>>) nlpResult.get("education");
        if (educationData != null) {
            data.setEducations(convertEducation(educationData));
        }

        log.info("Extracted: {} skills, {} experiences, {} educations",
                data.getSkills().size(),
                data.getExperiences().size(),
                data.getEducations().size());

        return data;
    }

    private List<Skill> convertSkills(List<Map<String, Object>> skillsData) {
        List<Skill> skills = new ArrayList<>();

        for (Map<String, Object> skillMap : skillsData) {
            try {
                Skill skill = Skill.builder()
                        .skillName((String) skillMap.get("skillName"))
                        .category(parseSkillCategory((String) skillMap.get("category")))
                        .proficiencyLevel(parseProficiencyLevel((String) skillMap.get("proficiencyLevel")))
                        .confidenceScore(parseDouble(skillMap.get("confidenceScore")))
                        .isVerified(false)
                        .build();

                skills.add(skill);
            } catch (Exception e) {
                log.error("Error converting skill: {}", skillMap, e);
            }
        }

        return skills;
    }

    private List<Experience> convertExperiences(List<Map<String, Object>> experiencesData) {
        List<Experience> experiences = new ArrayList<>();

        for (Map<String, Object> expMap : experiencesData) {
            try {
                Experience experience = Experience.builder()
                        .jobTitle((String) expMap.get("jobTitle"))
                        .companyName((String) expMap.get("companyName"))
                        .startDate(parseDate((String) expMap.get("startDate")))
                        .endDate(parseDate((String) expMap.get("endDate")))
                        .isCurrentRole((Boolean) expMap.getOrDefault("isCurrentRole", false))
                        .description((String) expMap.get("description"))
                        .teamSize(parseInteger(expMap.get("teamSize")))
                        .leadershipRole((String) expMap.get("leadershipRole"))
                        .impactMetrics((String) expMap.get("impactMetrics"))
                        .build();

                experiences.add(experience);
            } catch (Exception e) {
                log.error("Error converting experience: {}", expMap, e);
            }
        }

        return experiences;
    }

    private List<Education> convertEducation(List<Map<String, Object>> educationData) {
        List<Education> educations = new ArrayList<>();

        for (Map<String, Object> eduMap : educationData) {
            try {
                Education education = Education.builder()
                        .degree((String) eduMap.get("degree"))
                        .fieldOfStudy((String) eduMap.get("fieldOfStudy"))
                        .institutionName((String) eduMap.get("institutionName"))
                        .startDate(parseDate((String) eduMap.get("startDate")))
                        .endDate(parseDate((String) eduMap.get("endDate")))
                        .gpa(parseDouble(eduMap.get("gpa")))
                        .educationLevel(parseEducationLevel((String) eduMap.get("educationLevel")))
                        .build();

                educations.add(education);
            } catch (Exception e) {
                log.error("Error converting education: {}", eduMap, e);
            }
        }

        return educations;
    }

    private Skill.SkillCategory parseSkillCategory(String category) {
        if (category == null) return Skill.SkillCategory.OTHER;

        try {
            return Skill.SkillCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Skill.SkillCategory.OTHER;
        }
    }

    private Skill.ProficiencyLevel parseProficiencyLevel(String level) {
        if (level == null) return Skill.ProficiencyLevel.INTERMEDIATE;

        try {
            return Skill.ProficiencyLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Skill.ProficiencyLevel.INTERMEDIATE;
        }
    }

    private Education.EducationLevel parseEducationLevel(String level) {
        if (level == null) return Education.EducationLevel.OTHER;

        try {
            return Education.EducationLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Education.EducationLevel.OTHER;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Double) {
                return (Double) value;
            }
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Data class to hold extracted resume information
     */
    public static class ExtractedResumeData {
        private String fullName;
        private String email;
        private String phone;
        private String linkedinUrl;
        private String githubUrl;
        private List<Skill> skills = new ArrayList<>();
        private List<Experience> experiences = new ArrayList<>();
        private List<Education> educations = new ArrayList<>();

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getLinkedinUrl() { return linkedinUrl; }
        public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

        public String getGithubUrl() { return githubUrl; }
        public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

        public List<Skill> getSkills() { return skills; }
        public void setSkills(List<Skill> skills) { this.skills = skills; }

        public List<Experience> getExperiences() { return experiences; }
        public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }

        public List<Education> getEducations() { return educations; }
        public void setEducations(List<Education> educations) { this.educations = educations; }
    }
}
