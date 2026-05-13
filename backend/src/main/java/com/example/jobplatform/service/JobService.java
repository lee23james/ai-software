package com.example.jobplatform.service;

import com.example.jobplatform.common.PageResult;
import com.example.jobplatform.dto.AdminJobQueryDTO;
import com.example.jobplatform.dto.JobQueryDTO;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.vo.JobDetailVO;
import com.example.jobplatform.vo.JobSummaryVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JobService {

    List<JobSummaryVO> listJobs(JobQueryDTO query);

    PageResult<JobDetailVO> pageJobsForAdmin(AdminJobQueryDTO query);

    JobDetailVO getJobDetail(Long id);

    Long addJob(JobInfo job);

    void updateJob(Long id, JobInfo job);

    void deleteJob(Long id);

    void toggleJobStatus(Long id, Integer status);

    int importFromCsv(MultipartFile file);
}
