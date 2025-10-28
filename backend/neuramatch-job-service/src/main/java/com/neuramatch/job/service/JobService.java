package com.neuramatch.job.service;

import com.neuramatch.job.dto.*;
import com.neuramatch.job.entity.Company;
import com.neuramatch.job.entity.Job;
import com.neuramatch.job.entity.JobSkill;
import com.neuramatch.job.repository.CompanyRepository;
import com.neuramatch.job.repository.JobRepository;
import com.neuramatch.job.repository.JobSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobQualityService jobQualityService;
    private final BiasDetectionService biasDetectionService;

    @Transactional
    public JobDTO createJob(JobCreateRequest request) {
        log.info("Creating new job: {}", request.getJobTitle());

        // Validate company exists
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + request.getCompanyId()));

        // Analyze quality
        JobQualityAnalysisDTO qualityAnalysis = jobQualityService.analyzeJobQuality(
                request.getJobTitle(),
                request.getJobDescription(),
                request.getResponsibilities(),
                request.getQualifications(),
                request.getMinYearsExperience(),
                request.getMaxYearsExperience(),
                request.getMinSalary() != null ? request.getMinSalary().intValue() : null,
                request.getMaxSalary() != null ? request.getMaxSalary().intValue() : null,
                request.getApplicationUrl()
        );

        // Detect bias
        Map<String, Object> biasAnalysis = biasDetectionService.detectBias(
                request.getJobTitle(),
                request.getJobDescription(),
                request.getResponsibilities(),
                request.getQualifications()
        );

        // Create job entity
        Job job = Job.builder()
                .jobTitle(request.getJobTitle())
                .jobDescription(request.getJobDescription())
                .responsibilities(request.getResponsibilities())
                .qualifications(request.getQualifications())
                .company(company)
                .location(request.getLocation())
                .isRemote(request.getIsRemote() != null ? request.getIsRemote() : false)
                .workMode(request.getWorkMode() != null ?
                        Job.WorkMode.valueOf(request.getWorkMode()) : Job.WorkMode.ONSITE)
                .minYearsExperience(request.getMinYearsExperience())
                .maxYearsExperience(request.getMaxYearsExperience())
                .experienceLevel(request.getExperienceLevel() != null ?
                        Job.ExperienceLevel.valueOf(request.getExperienceLevel()) : null)
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .salaryCurrency(request.getSalaryCurrency())
                .salaryPeriod(request.getSalaryPeriod() != null ?
                        Job.SalaryPeriod.valueOf(request.getSalaryPeriod()) : Job.SalaryPeriod.ANNUAL)
                .jobType(request.getJobType() != null ?
                        Job.JobType.valueOf(request.getJobType()) : Job.JobType.FULL_TIME)
                .industry(request.getIndustry())
                .applicationUrl(request.getApplicationUrl())
                .applicationDeadline(request.getApplicationDeadline())
                .qualityScore(qualityAnalysis.getQualityScore())
                .biasScore((Integer) biasAnalysis.get("biasScore"))
                .status(Job.JobStatus.DRAFT)
                .expiresAt(request.getExpiresAt() != null ? request.getExpiresAt() :
                        LocalDate.now().plusMonths(3))
                .build();

        // Save job first
        job = jobRepository.save(job);

        // Add skills
        if (request.getRequiredSkills() != null) {
            for (JobSkillDTO skillDTO : request.getRequiredSkills()) {
                JobSkill skill = createJobSkill(skillDTO, job, true);
                job.addJobSkill(skill);
            }
        }

        if (request.getPreferredSkills() != null) {
            for (JobSkillDTO skillDTO : request.getPreferredSkills()) {
                JobSkill skill = createJobSkill(skillDTO, job, false);
                job.addJobSkill(skill);
            }
        }

        // Save with skills
        job = jobRepository.save(job);

        log.info("Job created successfully with ID: {}", job.getId());
        return convertToDTO(job, qualityAnalysis, biasAnalysis);
    }

    @Transactional(readOnly = true)
    public JobDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return convertToDTO(job, null, null);
    }

    @Transactional(readOnly = true)
    public Page<JobDTO> getAllJobs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("DESC") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return jobRepository.findAll(pageable)
                .map(job -> convertToDTO(job, null, null));
    }

    @Transactional(readOnly = true)
    public Page<JobDTO> getActiveJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return jobRepository.findActiveJobs(pageable)
                .map(job -> convertToDTO(job, null, null));
    }

    @Transactional(readOnly = true)
    public Page<JobDTO> searchJobs(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobRepository.searchJobs(query, pageable)
                .map(job -> convertToDTO(job, null, null));
    }

    @Transactional
    public JobDTO updateJobStatus(Long id, String status) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        Job.JobStatus newStatus = Job.JobStatus.valueOf(status);

        // Validate status transition
        if (newStatus == Job.JobStatus.ACTIVE && job.getQualityScore() < 70) {
            throw new RuntimeException("Job quality score must be at least 70 to activate. Current score: " + job.getQualityScore());
        }

        job.setStatus(newStatus);
        job = jobRepository.save(job);

        log.info("Job {} status updated to {}", id, status);
        return convertToDTO(job, null, null);
    }

    @Transactional
    public JobDTO updateJob(Long id, JobCreateRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        // Update fields
        job.setJobTitle(request.getJobTitle());
        job.setJobDescription(request.getJobDescription());
        job.setResponsibilities(request.getResponsibilities());
        job.setQualifications(request.getQualifications());
        job.setLocation(request.getLocation());
        job.setIsRemote(request.getIsRemote());
        job.setMinYearsExperience(request.getMinYearsExperience());
        job.setMaxYearsExperience(request.getMaxYearsExperience());
        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());
        job.setApplicationUrl(request.getApplicationUrl());
        job.setApplicationDeadline(request.getApplicationDeadline());

        // Re-analyze quality
        JobQualityAnalysisDTO qualityAnalysis = jobQualityService.analyzeJobQuality(
                request.getJobTitle(),
                request.getJobDescription(),
                request.getResponsibilities(),
                request.getQualifications(),
                request.getMinYearsExperience(),
                request.getMaxYearsExperience(),
                request.getMinSalary() != null ? request.getMinSalary().intValue() : null,
                request.getMaxSalary() != null ? request.getMaxSalary().intValue() : null,
                request.getApplicationUrl()
        );

        Map<String, Object> biasAnalysis = biasDetectionService.detectBias(
                request.getJobTitle(),
                request.getJobDescription(),
                request.getResponsibilities(),
                request.getQualifications()
        );

        job.setQualityScore(qualityAnalysis.getQualityScore());
        job.setBiasScore((Integer) biasAnalysis.get("biasScore"));

        job = jobRepository.save(job);

        log.info("Job {} updated successfully", id);
        return convertToDTO(job, qualityAnalysis, biasAnalysis);
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        jobRepository.delete(job);
        log.info("Job {} deleted successfully", id);
    }

    private JobSkill createJobSkill(JobSkillDTO dto, Job job, boolean isRequired) {
        return JobSkill.builder()
                .job(job)
                .skillName(dto.getSkillName())
                .category(dto.getCategory() != null ?
                        JobSkill.SkillCategory.valueOf(dto.getCategory()) : null)
                .requiredLevel(dto.getRequiredLevel() != null ?
                        JobSkill.ProficiencyLevel.valueOf(dto.getRequiredLevel()) : null)
                .minYearsExperience(dto.getMinYearsExperience())
                .isRequired(isRequired)
                .importance(dto.getImportance() != null ? dto.getImportance() : 5)
                .build();
    }

    private JobDTO convertToDTO(Job job, JobQualityAnalysisDTO qualityAnalysis,
                                Map<String, Object> biasAnalysis) {
        List<JobSkillDTO> requiredSkills = job.getJobSkills().stream()
                .filter(JobSkill::getIsRequired)
                .map(this::convertSkillToDTO)
                .collect(Collectors.toList());

        List<JobSkillDTO> preferredSkills = job.getJobSkills().stream()
                .filter(skill -> !skill.getIsRequired())
                .map(this::convertSkillToDTO)
                .collect(Collectors.toList());

        JobDTO dto = JobDTO.builder()
                .id(job.getId())
                .jobTitle(job.getJobTitle())
                .jobDescription(job.getJobDescription())
                .responsibilities(job.getResponsibilities())
                .qualifications(job.getQualifications())
                .companyId(job.getCompany().getId())
                .companyName(job.getCompany().getCompanyName())
                .location(job.getLocation())
                .isRemote(job.getIsRemote())
                .workMode(job.getWorkMode() != null ? job.getWorkMode().name() : null)
                .minYearsExperience(job.getMinYearsExperience())
                .maxYearsExperience(job.getMaxYearsExperience())
                .experienceLevel(job.getExperienceLevel() != null ? job.getExperienceLevel().name() : null)
                .minSalary(job.getMinSalary())
                .maxSalary(job.getMaxSalary())
                .salaryCurrency(job.getSalaryCurrency())
                .salaryPeriod(job.getSalaryPeriod() != null ? job.getSalaryPeriod().name() : null)
                .jobType(job.getJobType() != null ? job.getJobType().name() : null)
                .industry(job.getIndustry())
                .applicationUrl(job.getApplicationUrl())
                .applicationDeadline(job.getApplicationDeadline())
                .qualityScore(job.getQualityScore())
                .biasScore(job.getBiasScore())
                .requiredSkills(requiredSkills)
                .preferredSkills(preferredSkills)
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .expiresAt(job.getExpiresAt())
                .build();

        // Add quality suggestions if analysis was performed
        if (qualityAnalysis != null) {
            dto.setQualitySuggestions(qualityAnalysis.getSuggestions());
        }

        // Add bias issues if analysis was performed
        if (biasAnalysis != null) {
            @SuppressWarnings("unchecked")
            List<BiasIssueDTO> issues = (List<BiasIssueDTO>) biasAnalysis.get("issues");
            dto.setBiasIssues(issues);
        }

        return dto;
    }

    private JobSkillDTO convertSkillToDTO(JobSkill skill) {
        return JobSkillDTO.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory() != null ? skill.getCategory().name() : null)
                .requiredLevel(skill.getRequiredLevel() != null ? skill.getRequiredLevel().name() : null)
                .minYearsExperience(skill.getMinYearsExperience())
                .isRequired(skill.getIsRequired())
                .importance(skill.getImportance())
                .build();
    }
}
