package com.example.jobplatform.service;

import com.example.jobplatform.vo.JobSelectionAdviceVO;

/**
 * 基于用户在「兴趣岗位」接口中保存的意向岗位名称，生成针对性简历修改建议（DeepSeek）。
 */
public interface InterestTailoredResumeAdviceService {

    JobSelectionAdviceVO generateAndPersist(Long resumeId);
}
