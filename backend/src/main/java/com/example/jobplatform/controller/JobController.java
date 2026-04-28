package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobSummaryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/list")
    public ApiResponse<List<JobSummaryVO>> list(@ModelAttribute JobQueryDTO query) {
        return ApiResponse.ok(jobService.listJobs(query));
    }
}

