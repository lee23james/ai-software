package com.example.jobplatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class InterestJobItemDTO {

    @NotBlank(message = "岗位名称不能为空")
    private String jobName;

    @Min(value = 1, message = "优先级最小为1")
    @Max(value = 5, message = "优先级最大为5")
    private Integer priority = 3;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
