package com.example.jobplatform.service;

import com.example.jobplatform.dto.CreateResumeRequestDTO;
import com.example.jobplatform.vo.JobMatchVO;
import com.example.jobplatform.vo.ResumeHistoryDetailVO;
import com.example.jobplatform.vo.ResumeHistoryVO;
import com.example.jobplatform.vo.ResumeCreateVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeCreateVO createResume(CreateResumeRequestDTO request);

    ResumeCreateVO uploadResume(Long userId, String resumeName, String targetJobName, MultipartFile file, String skillsText);

    List<JobMatchVO> triggerMatch(Long resumeId, Integer topN);

    List<JobMatchVO> listMatches(Long resumeId);

    List<ResumeHistoryVO> listResumeHistory(Long userId);

    ResumeHistoryDetailVO getResumeHistoryDetail(Long resumeId);
}
