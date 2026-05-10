package com.example.jobplatform.service;

import com.example.jobplatform.dto.SaveInterestJobsRequestDTO;
import com.example.jobplatform.vo.InterestJobsPayloadVO;

public interface UserInterestService {

    void saveInterestJobs(SaveInterestJobsRequestDTO request);

    InterestJobsPayloadVO listInterestJobs(Long userId);
}
