package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.jobplatform.config.DeepseekProperties;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.ResumeSkill;
import com.example.jobplatform.exception.ServiceUnavailableException;
import com.example.jobplatform.llm.DeepseekChatClient;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.ResumeSkillMapper;
import com.example.jobplatform.vo.JobSelectionAdviceVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSelectionAdviceServiceImplTest {

    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private ResumeSkillMapper resumeSkillMapper;
    @Mock
    private JobInfoMapper jobInfoMapper;
    @Mock
    private DeepseekChatClient deepseekChatClient;
    @Mock
    private DeepseekProperties deepseekProperties;

    @InjectMocks
    private JobSelectionAdviceServiceImpl jobSelectionAdviceService;

    @Test
    void throwsWhenApiKeyMissing() {
        when(deepseekProperties.getApiKey()).thenReturn("  ");

        assertThatThrownBy(() -> jobSelectionAdviceService.generateAndPersist(1L))
            .isInstanceOf(ServiceUnavailableException.class)
            .hasMessageContaining("DEEPSEEK_API_KEY");
    }

    @Test
    void persistsAdviceFromModel() {
        when(deepseekProperties.getApiKey()).thenReturn("sk-test");
        when(deepseekProperties.getModel()).thenReturn("deepseek-chat");
        when(deepseekProperties.getMaxJobDescriptionChars()).thenReturn(500);
        when(deepseekProperties.getMaxResumeTextChars()).thenReturn(12000);

        Resume resume = new Resume();
        resume.setId(5L);
        resume.setResumeName("我的简历");
        resume.setTargetJobName("Java");
        resume.setParsedText("熟悉 Spring");
        when(resumeMapper.selectById(5L)).thenReturn(resume);

        ResumeSkill skill = new ResumeSkill();
        skill.setSkillName("Java");
        when(resumeSkillMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(List.of(skill));

        JobInfo job = new JobInfo();
        job.setId(100L);
        job.setJobName("后端开发");
        job.setCompanyName("测试公司");
        job.setCity("北京");
        job.setSalaryMin(15000);
        job.setSalaryMax(25000);
        job.setEducation("本科");
        job.setExperience("1-3年");
        job.setSkillTags("Java,Spring");
        job.setJobDescription("负责后端开发");
        job.setStatus(1);
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(job));

        when(deepseekChatClient.chatCompletion(eq("deepseek-chat"), eq("sk-test"), any(String.class), any(String.class)))
            .thenReturn("建议投递岗位 id=100");

        JobSelectionAdviceVO vo = jobSelectionAdviceService.generateAndPersist(5L);

        assertThat(vo.advice()).isEqualTo("建议投递岗位 id=100");
        assertThat(vo.model()).isEqualTo("deepseek-chat");

        verify(resumeMapper).update(eq(null), any(UpdateWrapper.class));
    }
}
