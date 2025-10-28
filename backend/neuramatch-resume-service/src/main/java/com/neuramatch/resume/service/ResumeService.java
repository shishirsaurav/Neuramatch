package com.neuramatch.resume.service;

import com.neuramatch.resume.dto.*;
import com.neuramatch.resume.entity.Resume;
import com.neuramatch.resume.exception.ResumeNotFoundException;
import com.neuramatch.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;
    private final ResumeParsingService parsingService;
    private final ResumeQualityService qualityService;
    private final NlpExtractionService nlpExtractionService;

    @Transactional
    public ResumeUploadResponse uploadResume(MultipartFile file) {
        try {
            // Store file
            String storedFilename = fileStorageService.storeFile(file);
            String fileType = fileStorageService.getContentType(storedFilename);

            // Parse resume
            String content = parsingService.extractTextContent(storedFilename);
            Map<String, Object> structuredInfo = parsingService.extractStructuredInfo(content);

            // Calculate quality score
            int qualityScore = qualityService.calculateQualityScore(content, structuredInfo);
            List<String> suggestions = qualityService.generateSuggestions(content, structuredInfo, qualityScore);

            // Extract structured data using NLP
            NlpExtractionService.ExtractedResumeData extractedData = nlpExtractionService.extractResumeData(content);

            // Create resume entity
            Resume resume = Resume.builder()
                    .fullName(extractedData.getFullName())
                    .email(extractedData.getEmail())
                    .phone(extractedData.getPhone())
                    .linkedinUrl(extractedData.getLinkedinUrl())
                    .githubUrl(extractedData.getGithubUrl())
                    .originalFileName(file.getOriginalFilename())
                    .fileStoragePath(storedFilename)
                    .fileType(fileType)
                    .qualityScore(qualityScore)
                    .qualitySuggestions(String.join("; ", suggestions))
                    .status(Resume.ResumeStatus.DRAFT)
                    .build();

            // Add skills, experiences, and educations
            extractedData.getSkills().forEach(resume::addSkill);
            extractedData.getExperiences().forEach(resume::addExperience);
            extractedData.getEducations().forEach(resume::addEducation);

            // Save to database
            Resume savedResume = resumeRepository.save(resume);

            log.info("Resume uploaded successfully: {}", savedResume.getId());

            return ResumeUploadResponse.builder()
                    .resumeId(savedResume.getId())
                    .message("Resume uploaded successfully")
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(fileType)
                    .qualityScore(qualityScore)
                    .status("DRAFT")
                    .uploadedAt(LocalDateTime.now())
                    .hasErrors(false)
                    .build();

        } catch (Exception e) {
            log.error("Error uploading resume", e);
            return ResumeUploadResponse.builder()
                    .message("Failed to upload resume")
                    .hasErrors(true)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public ResumeDTO getResumeById(Long id) {
        Resume resume = resumeRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));

        return convertToDTO(resume);
    }

    @Transactional(readOnly = true)
    public Page<ResumeDTO> getAllResumes(Pageable pageable) {
        return resumeRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ResumeDTO> getActiveResumes(Pageable pageable) {
        return resumeRepository.findActiveResumes(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ResumeDTO> searchResumes(String searchTerm, Pageable pageable) {
        return resumeRepository.searchByEmailOrName(searchTerm, pageable).map(this::convertToDTO);
    }

    @Transactional
    public void deleteResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));

        // Delete file from storage
        if (resume.getFileStoragePath() != null) {
            fileStorageService.deleteFile(resume.getFileStoragePath());
        }

        // Delete from database
        resumeRepository.delete(resume);
        log.info("Resume deleted: {}", id);
    }

    @Transactional
    public ResumeDTO updateResumeStatus(Long id, Resume.ResumeStatus status) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));

        resume.setStatus(status);
        Resume updatedResume = resumeRepository.save(resume);

        log.info("Resume status updated: {} -> {}", id, status);
        return convertToDTO(updatedResume);
    }

    private ResumeDTO convertToDTO(Resume resume) {
        return ResumeDTO.builder()
                .id(resume.getId())
                .fullName(resume.getFullName())
                .email(resume.getEmail())
                .phone(resume.getPhone())
                .summary(resume.getSummary())
                .location(resume.getLocation())
                .linkedinUrl(resume.getLinkedinUrl())
                .githubUrl(resume.getGithubUrl())
                .portfolioUrl(resume.getPortfolioUrl())
                .careerObjective(resume.getCareerObjective())
                .qualityScore(resume.getQualityScore())
                .status(resume.getStatus().name())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .skills(resume.getSkills().stream().map(this::convertSkillToDTO).collect(Collectors.toList()))
                .experiences(resume.getExperiences().stream().map(this::convertExperienceToDTO).collect(Collectors.toList()))
                .educations(resume.getEducations().stream().map(this::convertEducationToDTO).collect(Collectors.toList()))
                .build();
    }

    private SkillDTO convertSkillToDTO(com.neuramatch.resume.entity.Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory() != null ? skill.getCategory().name() : null)
                .proficiencyLevel(skill.getProficiencyLevel() != null ? skill.getProficiencyLevel().name() : null)
                .lastUsedDate(skill.getLastUsedDate())
                .yearsOfExperience(skill.getYearsOfExperience())
                .confidenceScore(skill.getConfidenceScore())
                .isVerified(skill.getIsVerified())
                .build();
    }

    private ExperienceDTO convertExperienceToDTO(com.neuramatch.resume.entity.Experience experience) {
        return ExperienceDTO.builder()
                .id(experience.getId())
                .jobTitle(experience.getJobTitle())
                .companyName(experience.getCompanyName())
                .location(experience.getLocation())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .isCurrentRole(experience.getIsCurrentRole())
                .description(experience.getDescription())
                .achievements(experience.getAchievements())
                .teamSize(experience.getTeamSize())
                .leadershipRole(experience.getLeadershipRole())
                .impactMetrics(experience.getImpactMetrics())
                .durationInMonths(experience.getDurationInMonths())
                .build();
    }

    private EducationDTO convertEducationToDTO(com.neuramatch.resume.entity.Education education) {
        return EducationDTO.builder()
                .id(education.getId())
                .institutionName(education.getInstitutionName())
                .degree(education.getDegree())
                .fieldOfStudy(education.getFieldOfStudy())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .location(education.getLocation())
                .gpa(education.getGpa())
                .maxGpa(education.getMaxGpa())
                .achievements(education.getAchievements())
                .educationLevel(education.getEducationLevel() != null ? education.getEducationLevel().name() : null)
                .build();
    }
}
