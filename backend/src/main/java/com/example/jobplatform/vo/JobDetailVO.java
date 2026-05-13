package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public record JobDetailVO(
    Long id,
    String jobName,
    String companyName,
    String city,
    Integer salaryMin,
    Integer salaryMax,
    String education,
    String experience,
    String skillTags,
    String jobDescription,
    LocalDateTime publishTime,
    Integer status
) {
}
