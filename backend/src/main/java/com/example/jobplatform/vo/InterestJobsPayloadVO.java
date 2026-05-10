package com.example.jobplatform.vo;

import java.util.List;

public record InterestJobsPayloadVO(
    List<InterestJobVO> jobs,
    Integer expectedSalaryMin,
    Integer expectedSalaryMax
) {
}
