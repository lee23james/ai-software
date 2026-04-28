package com.example.jobplatform.vo;

public record JobSummaryVO(
    Long id,
    String jobName,
    String companyName,
    String city,
    String salary,
    String education,
    String skillTags
) {
}

