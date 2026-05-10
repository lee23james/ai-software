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
import com.example.jobplatform.service.UserInterestService;
import com.example.jobplatform.vo.InterestJobVO;
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
class InterestTailoredResumeAdviceServiceImplTest {

    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private ResumeSkillMapper resumeSkillMapper;
    @Mock
    private JobInfoMapper jobInfoMapper;
    @Mock
    private UserInterestService userInterestService;
    @Mock
    private DeepseekChatClient deepseekChatClient;
    @Mock
    private DeepseekProperties deepseekProperties;

    @InjectMocks
    private InterestTailoredResumeAdviceServiceImpl service;

    @Test
    void throwsWhenNoInterests() {
        when(deepseekProperties.getApiKey()).thenReturn("sk-x");
        Resume r = new Resume();
        r.setId(1L);
        r.setUserId(99L);
        when(resumeMapper.selectById(1L)).thenReturn(r);
        when(userInterestService.listInterestJobs(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.generateAndPersist(1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("兴趣岗位");
    }

    @Test
    void throwsWhenApiKeyMissing() {
        when(deepseekProperties.getApiKey()).thenReturn("");

        assertThatThrownBy(() -> service.generateAndPersist(1L))
            .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void generatesWhenInterestsPresent() {
        when(deepseekProperties.getApiKey()).thenReturn("sk-test");
        when(deepseekProperties.getModel()).thenReturn("deepseek-chat");
        when(deepseekProperties.getMaxJobDescriptionChars()).thenReturn(500);
        when(deepseekProperties.getMaxResumeTextChars()).thenReturn(12000);

        Resume r = new Resume();
        r.setId(3L);
        r.setUserId(7L);
        r.setResumeName("CV");
        r.setTargetJobName("开发");
        r.setParsedText("Java");
        when(resumeMapper.selectById(3L)).thenReturn(r);

        when(userInterestService.listInterestJobs(7L)).thenReturn(
            List.of(new InterestJobVO("Java 后端", 1, "student_profile"))
        );

        when(resumeSkillMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        JobInfo job = new JobInfo();
        job.setId(50L);
        job.setJobName("Java 后端开发");
        job.setCompanyName("Co");
        job.setCity("上海");
        job.setSalaryMin(1);
        job.setSalaryMax(2);
        job.setSkillTags("Java");
        job.setJobDescription("Spring");
        job.setStatus(1);
        when(jobInfoMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(job));

        when(deepseekChatClient.chatCompletion(eq("deepseek-chat"), eq("sk-test"), any(String.class), any(String.class)))
            .thenReturn("修改建议正文");

        JobSelectionAdviceVO vo = service.generateAndPersist(3L);

        assertThat(vo.advice()).isEqualTo("修改建议正文");
        verify(resumeMapper).update(eq(null), any(UpdateWrapper.class));
    }
}
