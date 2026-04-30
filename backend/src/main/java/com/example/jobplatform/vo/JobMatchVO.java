package com.example.jobplatform.vo;

import java.math.BigDecimal;

public record JobMatchVO(
    Long jobId,
    String jobName,
    String companyName,
    String city,
    BigDecimal totalScore,
    BigDecimal skillScore,
    BigDecimal experienceScore,
    BigDecimal educationScore,
    BigDecimal cityScore,
    BigDecimal salaryScore,
    String reasonJson
) {
}
