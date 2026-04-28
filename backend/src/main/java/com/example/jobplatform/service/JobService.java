package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.vo.JobSummaryVO;

import java.util.List;

public interface JobService {

    List<JobSummaryVO> listJobs(JobQueryDTO query);
}

