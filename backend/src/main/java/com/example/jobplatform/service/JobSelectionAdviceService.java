package com.example.jobplatform.service;

import com.example.jobplatform.vo.JobSelectionAdviceVO;

public interface JobSelectionAdviceService {

    JobSelectionAdviceVO generateAndPersist(Long resumeId);
}
