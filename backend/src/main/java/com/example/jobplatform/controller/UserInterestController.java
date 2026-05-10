package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.dto.SaveInterestJobsRequestDTO;
import com.example.jobplatform.service.UserInterestService;
import com.example.jobplatform.vo.InterestJobsPayloadVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/interest-jobs")
public class UserInterestController {

    private final UserInterestService userInterestService;

    public UserInterestController(UserInterestService userInterestService) {
        this.userInterestService = userInterestService;
    }

    @PostMapping
    public ApiResponse<Void> saveInterestJobs(@Valid @RequestBody SaveInterestJobsRequestDTO request) {
        userInterestService.saveInterestJobs(request);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<InterestJobsPayloadVO> listInterestJobs(@RequestParam Long userId) {
        return ApiResponse.ok(userInterestService.listInterestJobs(userId));
    }
}
