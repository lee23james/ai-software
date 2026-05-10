package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.util.JobSalaryMonthlyYuan;
import com.example.jobplatform.vo.JobSummaryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobInfoMapper jobInfoMapper;

    public JobServiceImpl(JobInfoMapper jobInfoMapper) {
        this.jobInfoMapper = jobInfoMapper;
    }

    @Override
    public List<JobSummaryVO> listJobs(JobQueryDTO query) {
        LambdaQueryWrapper<JobInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobInfo::getStatus, 1);
        wrapper.like(query.getKeyword() != null && !query.getKeyword().isBlank(), JobInfo::getJobName, query.getKeyword());
        wrapper.eq(query.getCity() != null && !query.getCity().isBlank(), JobInfo::getCity, query.getCity());
        wrapper.eq(query.getEducation() != null && !query.getEducation().isBlank(), JobInfo::getEducation, query.getEducation());
        wrapper.ge(query.getSalaryMin() != null, JobInfo::getSalaryMin, query.getSalaryMin());
        wrapper.le(query.getSalaryMax() != null, JobInfo::getSalaryMax, query.getSalaryMax());
        wrapper.orderByDesc(JobInfo::getPublishTime).orderByDesc(JobInfo::getId);

        return jobInfoMapper.selectList(wrapper).stream()
            .map(this::toSummaryVO)
            .toList();
    }

    private JobSummaryVO toSummaryVO(JobInfo item) {
        int mn = item.getSalaryMin() == null ? 0 : item.getSalaryMin();
        int mx = item.getSalaryMax() == null ? 0 : item.getSalaryMax();
        int[] yuan = JobSalaryMonthlyYuan.storedPairToMonthlyYuan(mn, mx);
        String salary = yuan == null ? "面议" : yuan[0] + "-" + yuan[1];
        return new JobSummaryVO(
            item.getId(),
            item.getJobName(),
            item.getCompanyName(),
            item.getCity(),
            salary,
            item.getEducation(),
            item.getSkillTags()
        );
    }
}

