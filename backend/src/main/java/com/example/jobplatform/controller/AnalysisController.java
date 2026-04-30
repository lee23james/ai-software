package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.service.AnalysisService;
import com.example.jobplatform.vo.ChartItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/city-job-count")
    public ApiResponse<List<ChartItemVO>> cityJobCount() {
        return ApiResponse.ok(analysisService.cityJobCount());
    }

    @GetMapping("/education-count")
    public ApiResponse<List<ChartItemVO>> educationCount() {
        return ApiResponse.ok(analysisService.educationRequirementCount());
    }

    @GetMapping("/salary-range-count")
    public ApiResponse<List<ChartItemVO>> salaryRangeCount() {
        return ApiResponse.ok(analysisService.salaryRangeCount());
    }

    @GetMapping("/top-skills")
    public ApiResponse<List<ChartItemVO>> topSkills(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(analysisService.topSkillCount(limit));
    }

    @GetMapping("/experience-count")
    public ApiResponse<List<ChartItemVO>> experienceCount() {
        return ApiResponse.ok(analysisService.experienceRequirementCount());
    }

    @GetMapping("/company-job-count")
    public ApiResponse<List<ChartItemVO>> companyJobCount(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(analysisService.companyJobCount(limit));
    }
}

