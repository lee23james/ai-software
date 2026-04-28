package com.example.jobplatform.service.impl;

import com.example.jobplatform.service.AnalysisService;
import com.example.jobplatform.vo.ChartItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Override
    public List<ChartItemVO> cityJobCount() {
        return List.of(
            new ChartItemVO("上海", 320),
            new ChartItemVO("杭州", 260),
            new ChartItemVO("深圳", 240),
            new ChartItemVO("北京", 210),
            new ChartItemVO("广州", 180)
        );
    }
}

