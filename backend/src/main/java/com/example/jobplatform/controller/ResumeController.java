package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.dto.CreateResumeRequestDTO;
import com.example.jobplatform.service.ResumeService;
import com.example.jobplatform.vo.JobMatchVO;
import com.example.jobplatform.vo.ResumeCreateVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ApiResponse<ResumeCreateVO> createResume(@Valid @RequestBody CreateResumeRequestDTO request) {
        return ApiResponse.ok(resumeService.createResume(request));
    }

    @PostMapping("/upload")
    public ApiResponse<ResumeCreateVO> uploadResume(@RequestParam Long userId,
                                                    @RequestParam String resumeName,
                                                    @RequestParam(required = false) String targetJobName,
                                                    @RequestParam(required = false) String skillsText,
                                                    @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(resumeService.uploadResume(userId, resumeName, targetJobName, file, skillsText));
    }

    @PostMapping("/{resumeId}/match")
    public ApiResponse<List<JobMatchVO>> triggerMatch(@PathVariable Long resumeId,
                                                      @RequestParam(defaultValue = "20") Integer topN) {
        return ApiResponse.ok(resumeService.triggerMatch(resumeId, topN));
    }

    @GetMapping("/{resumeId}/matches")
    public ApiResponse<List<JobMatchVO>> listMatches(@PathVariable Long resumeId) {
        return ApiResponse.ok(resumeService.listMatches(resumeId));
    }
}
