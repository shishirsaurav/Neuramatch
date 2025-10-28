package com.neuramatch.job.controller;

import com.neuramatch.job.dto.JobCreateRequest;
import com.neuramatch.job.dto.JobDTO;
import com.neuramatch.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobDTO> createJob(@Valid @RequestBody JobCreateRequest request) {
        log.info("POST /api/v1/jobs - Creating job: {}", request.getJobTitle());
        JobDTO job = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJobById(@PathVariable Long id) {
        log.info("GET /api/v1/jobs/{} - Fetching job", id);
        JobDTO job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping
    public ResponseEntity<Page<JobDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("GET /api/v1/jobs - Fetching jobs (page={}, size={})", page, size);
        Page<JobDTO> jobs = jobService.getAllJobs(page, size, sortBy, sortDir);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<JobDTO>> getActiveJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/jobs/active - Fetching active jobs");
        Page<JobDTO> jobs = jobService.getActiveJobs(page, size);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<JobDTO>> searchJobs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/jobs/search?query={}", query);
        Page<JobDTO> jobs = jobService.searchJobs(query, page, size);
        return ResponseEntity.ok(jobs);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobDTO> updateJobStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("PATCH /api/v1/jobs/{}/status?status={}", id, status);
        JobDTO job = jobService.updateJobStatus(id, status);
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobCreateRequest request) {
        log.info("PUT /api/v1/jobs/{} - Updating job", id);
        JobDTO job = jobService.updateJob(id, request);
        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        log.info("DELETE /api/v1/jobs/{}", id);
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
