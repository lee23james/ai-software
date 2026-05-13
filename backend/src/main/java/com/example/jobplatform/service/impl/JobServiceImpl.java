package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.common.PageResult;
import com.example.jobplatform.dto.AdminJobQueryDTO;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobDetailVO;
import com.example.jobplatform.vo.JobSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public PageResult<JobDetailVO> pageJobsForAdmin(AdminJobQueryDTO query) {
        LambdaQueryWrapper<JobInfo> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(query.getKeyword() != null && !query.getKeyword().isBlank(), JobInfo::getJobName, query.getKeyword());
        wrapper.like(query.getCompanyName() != null && !query.getCompanyName().isBlank(), JobInfo::getCompanyName, query.getCompanyName());
        wrapper.eq(query.getCity() != null && !query.getCity().isBlank(), JobInfo::getCity, query.getCity());
        wrapper.eq(query.getEducation() != null && !query.getEducation().isBlank(), JobInfo::getEducation, query.getEducation());
        wrapper.eq(query.getStatus() != null, JobInfo::getStatus, query.getStatus());
        wrapper.orderByDesc(JobInfo::getPublishTime).orderByDesc(JobInfo::getId);

        List<JobInfo> allRecords = jobInfoMapper.selectList(wrapper);
        long total = allRecords.size();
        long pageNum = query.getPageNum();
        long pageSize = query.getPageSize();

        int fromIndex = (int) ((pageNum - 1) * pageSize);
        int toIndex = (int) Math.min(fromIndex + pageSize, total);

        List<JobDetailVO> records;
        if (fromIndex >= total) {
            records = new ArrayList<>();
        } else {
            records = allRecords.subList(fromIndex, toIndex).stream()
                .map(this::toDetailVO)
                .toList();
        }

        return new PageResult<>(total, pageNum, pageSize, records);
    }

    @Override
    public JobDetailVO getJobDetail(Long id) {
        JobInfo job = jobInfoMapper.selectById(id);
        if (job == null) {
            return null;
        }
        return toDetailVO(job);
    }

    @Override
    public Long addJob(JobInfo job) {
        if (job.getPublishTime() == null) {
            job.setPublishTime(LocalDateTime.now());
        }
        if (job.getStatus() == null) {
            job.setStatus(1);
        }
        jobInfoMapper.insert(job);
        return job.getId();
    }

    @Override
    public void updateJob(Long id, JobInfo job) {
        JobInfo existing = jobInfoMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("岗位不存在: " + id);
        }
        job.setId(id);
        jobInfoMapper.updateById(job);
    }

    @Override
    public void deleteJob(Long id) {
        jobInfoMapper.deleteById(id);
    }

    @Override
    public void toggleJobStatus(Long id, Integer status) {
        JobInfo job = new JobInfo();
        job.setId(id);
        job.setStatus(status);
        jobInfoMapper.updateById(job);
    }

    @Override
    public int importFromCsv(MultipartFile file) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 5) {
                    continue;
                }

                JobInfo job = new JobInfo();
                job.setJobName(cols[0].trim());
                job.setCompanyName(cols[1].trim());
                job.setCity(cols[2].trim());
                try {
                    job.setSalaryMin(Integer.parseInt(cols[3].trim()));
                    job.setSalaryMax(Integer.parseInt(cols[4].trim()));
                } catch (NumberFormatException e) {
                    job.setSalaryMin(0);
                    job.setSalaryMax(0);
                }
                job.setEducation(cols.length > 5 ? cols[5].trim() : "不限");
                job.setExperience(cols.length > 6 ? cols[6].trim() : "不限");
                job.setSkillTags(cols.length > 7 ? cols[7].trim() : "");
                job.setJobDescription(cols.length > 8 ? cols[8].trim() : "");
                job.setStatus(1);
                job.setPublishTime(LocalDateTime.now());

                jobInfoMapper.insert(job);
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV 导入失败: " + e.getMessage(), e);
        }
        return count;
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

    private JobDetailVO toDetailVO(JobInfo item) {
        return new JobDetailVO(
            item.getId(),
            item.getJobName(),
            item.getCompanyName(),
            item.getCity(),
            item.getSalaryMin(),
            item.getSalaryMax(),
            item.getEducation(),
            item.getExperience(),
            item.getSkillTags(),
            item.getJobDescription(),
            item.getPublishTime(),
            item.getStatus()
        );
    }
}
