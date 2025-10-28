package com.neuramatch.resume.controller;

import com.neuramatch.resume.dto.ResumeDTO;
import com.neuramatch.resume.dto.ResumeUploadResponse;
import com.neuramatch.resume.entity.Resume;
import com.neuramatch.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * Upload a resume file
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeUploadResponse> uploadResume(
            @RequestPart("file") MultipartFile file) {

        log.info("Received resume upload request: {}", file.getOriginalFilename());

        ResumeUploadResponse response = resumeService.uploadResume(file);

        if (response.getHasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get resume by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumeDTO> getResumeById(@PathVariable Long id) {
        log.info("Fetching resume: {}", id);
        ResumeDTO resume = resumeService.getResumeById(id);
        return ResponseEntity.ok(resume);
    }

    /**
     * Get all resumes with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ResumeDTO>> getAllResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResumeDTO> resumes = resumeService.getAllResumes(pageable);

        return ResponseEntity.ok(resumes);
    }

    /**
     * Get active resumes
     */
    @GetMapping("/active")
    public ResponseEntity<Page<ResumeDTO>> getActiveResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<ResumeDTO> resumes = resumeService.getActiveResumes(pageable);

        return ResponseEntity.ok(resumes);
    }

    /**
     * Search resumes by name or email
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ResumeDTO>> searchResumes(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Searching resumes with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        Page<ResumeDTO> resumes = resumeService.searchResumes(query, pageable);

        return ResponseEntity.ok(resumes);
    }

    /**
     * Update resume status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ResumeDTO> updateResumeStatus(
            @PathVariable Long id,
            @RequestParam Resume.ResumeStatus status) {

        log.info("Updating resume {} status to {}", id, status);
        ResumeDTO resume = resumeService.updateResumeStatus(id, status);

        return ResponseEntity.ok(resume);
    }

    /**
     * Delete resume
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        log.info("Deleting resume: {}", id);
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }
}
