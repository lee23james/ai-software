package com.example.jobplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.jobplatform.entity.Resume;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface ResumeMapper extends BaseMapper<Resume> {

    @Delete("DELETE FROM job_application WHERE resume_id = #{resumeId}")
    void deleteJobApplicationsByResumeId(@Param("resumeId") Long resumeId);
}
