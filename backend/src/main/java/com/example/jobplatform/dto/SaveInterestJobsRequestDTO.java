package com.example.jobplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SaveInterestJobsRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotEmpty(message = "兴趣岗位不能为空")
    @Valid
    private List<InterestJobItemDTO> jobs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<InterestJobItemDTO> getJobs() {
        return jobs;
    }

    public void setJobs(List<InterestJobItemDTO> jobs) {
        this.jobs = jobs;
    }
}
