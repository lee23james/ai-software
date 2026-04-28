package com.example.jobplatform.vo;

public record DashboardStatsVO(
    Integer jobCount,
    Integer userCount,
    Integer resumeCount,
    Integer analysisMetricCount
) {
}

