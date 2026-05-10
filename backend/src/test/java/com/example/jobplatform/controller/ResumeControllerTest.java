package com.example.jobplatform.controller;

import com.example.jobplatform.service.InterestTailoredResumeAdviceService;
import com.example.jobplatform.service.JobSelectionAdviceService;
import com.example.jobplatform.service.ResumeService;
import com.example.jobplatform.vo.JobSelectionAdviceVO;
import com.example.jobplatform.vo.ResumeHistoryDetailVO;
import com.example.jobplatform.vo.ResumeHistoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobSelectionAdviceService jobSelectionAdviceService;

    @MockBean
    private InterestTailoredResumeAdviceService interestTailoredResumeAdviceService;

    @MockBean
    private ResumeService resumeService;

    @Test
    void returnsResumeHistoryList() throws Exception {
        given(resumeService.listResumeHistory(1L)).willReturn(List.of(
            new ResumeHistoryVO(10L, "resume.pdf", "/files/resume.pdf", "pdf", "数据分析师", 0, "Java Python SQL", null, null)
        ));

        mockMvc.perform(get("/api/resume/history").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].resumeId").value(10))
            .andExpect(jsonPath("$.data[0].resumeName").value("resume.pdf"));
    }

    @Test
    void returnsResumeHistoryDetail() throws Exception {
        given(resumeService.getResumeHistoryDetail(10L)).willReturn(
            new ResumeHistoryDetailVO(10L, "resume.pdf", "/files/resume.pdf", "pdf", "数据分析师", 0, "Java Python SQL",
                List.of(), null, List.of(), null, null, null, null, null, null)
        );

        mockMvc.perform(get("/api/resume/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.resumeId").value(10))
            .andExpect(jsonPath("$.data.resumeName").value("resume.pdf"));
    }

    @Test
    void generatesJobSelectionAdvice() throws Exception {
        given(jobSelectionAdviceService.generateAndPersist(10L))
            .willReturn(new JobSelectionAdviceVO("优先投递 A 岗位", "deepseek-chat"));

        mockMvc.perform(post("/api/resume/10/job-selection-advice").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.advice").value("优先投递 A 岗位"))
            .andExpect(jsonPath("$.data.model").value("deepseek-chat"));
    }

    @Test
    void generatesInterestResumeAdvice() throws Exception {
        given(interestTailoredResumeAdviceService.generateAndPersist(10L))
            .willReturn(new JobSelectionAdviceVO("建议强化项目经历中的量化指标", "deepseek-chat"));

        mockMvc.perform(post("/api/resume/10/interest-resume-advice").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.advice").value("建议强化项目经历中的量化指标"))
            .andExpect(jsonPath("$.data.model").value("deepseek-chat"));
    }
}
