package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.service.JobService;
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
        String salary = item.getSalaryMin() + "-" + item.getSalaryMax();
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

