package com.example.jobplatform.service;

import com.example.jobplatform.vo.ChartItemVO;

import java.util.List;

public interface AnalysisService {

    List<ChartItemVO> cityJobCount();

    List<ChartItemVO> educationRequirementCount();

    List<ChartItemVO> salaryRangeCount();

    List<ChartItemVO> topSkillCount(int limit);

    List<ChartItemVO> experienceRequirementCount();

    List<ChartItemVO> companyJobCount(int limit);
}

