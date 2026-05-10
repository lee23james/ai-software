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

    /** 期望月薪下限（元），与 expectedSalaryMax 同时传 */
    private Integer expectedSalaryMin;
    /** 期望月薪上限（元） */
    private Integer expectedSalaryMax;

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

    public Integer getExpectedSalaryMin() {
        return expectedSalaryMin;
    }

    public void setExpectedSalaryMin(Integer expectedSalaryMin) {
        this.expectedSalaryMin = expectedSalaryMin;
    }

    public Integer getExpectedSalaryMax() {
        return expectedSalaryMax;
    }

    public void setExpectedSalaryMax(Integer expectedSalaryMax) {
        this.expectedSalaryMax = expectedSalaryMax;
    }
}
