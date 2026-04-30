package com.example.jobplatform.service;

import com.example.jobplatform.dto.SaveInterestJobsRequestDTO;
import com.example.jobplatform.vo.InterestJobVO;

import java.util.List;

public interface UserInterestService {

    void saveInterestJobs(SaveInterestJobsRequestDTO request);

    List<InterestJobVO> listInterestJobs(Long userId);
}
