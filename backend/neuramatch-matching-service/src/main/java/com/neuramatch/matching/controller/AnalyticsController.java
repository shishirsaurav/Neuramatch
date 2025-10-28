package com.neuramatch.matching.controller;

import com.neuramatch.matching.dto.SalaryBenchmarkDTO;
import com.neuramatch.matching.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/skills/trending")
    public ResponseEntity<Map<String, Object>> getTrendingSkills(
            @RequestParam(defaultValue = "6") int months) {
        log.info("GET /api/v1/analytics/skills/trending?months={}", months);
        Map<String, Object> trends = analyticsService.getTrendingSkills(months);
        return ResponseEntity.ok(trends);
    }

    @PostMapping("/salary/benchmark")
    public ResponseEntity<SalaryBenchmarkDTO> getSalaryBenchmark(
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/v1/analytics/salary/benchmark");

        @SuppressWarnings("unchecked")
        List<String> skills = (List<String>) request.get("skills");
        Integer experience = (Integer) request.get("experience");
        String location = (String) request.get("location");
        String industry = (String) request.get("industry");

        SalaryBenchmarkDTO benchmark = analyticsService.getSalaryBenchmark(
                skills, experience, location, industry);
        return ResponseEntity.ok(benchmark);
    }

    @GetMapping("/skills/{skill}/supply-demand")
    public ResponseEntity<Map<String, Object>> getSupplyDemand(@PathVariable String skill) {
        log.info("GET /api/v1/analytics/skills/{}/supply-demand", skill);
        Map<String, Object> analysis = analyticsService.getSupplyDemandAnalysis(skill);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/hiring-funnel/{jobId}")
    public ResponseEntity<Map<String, Object>> getHiringFunnel(@PathVariable Long jobId) {
        log.info("GET /api/v1/analytics/hiring-funnel/{}", jobId);
        Map<String, Object> funnel = analyticsService.getHiringFunnelAnalytics(jobId);
        return ResponseEntity.ok(funnel);
    }
}
