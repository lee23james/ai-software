package com.example.jobplatform.vo;

public record DashboardStatsVO(
    Integer totalJobs,
    Integer totalUsers,
    Integer totalResumes,
    Integer todayNew
) {
}

