package com.example.jobplatform.service.impl;

import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobSummaryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    @Override
    public List<JobSummaryVO> listJobs(JobQueryDTO query) {
        return List.of(
            new JobSummaryVO(1L, "Java 后端开发实习生", "示例科技", "上海", "10K-15K", "本科", "Java,Spring Boot,MySQL"),
            new JobSummaryVO(2L, "前端开发实习生", "数智未来", "杭州", "8K-12K", "本科", "Vue,Element Plus,ECharts"),
            new JobSummaryVO(3L, "数据分析助理", "云启数据", "深圳", "9K-13K", "本科", "Python,SQL,Excel")
        );
    }
}

