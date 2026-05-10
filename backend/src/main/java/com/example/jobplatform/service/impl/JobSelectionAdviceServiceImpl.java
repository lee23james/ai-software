package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.jobplatform.config.DeepseekProperties;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.ResumeSkill;
import com.example.jobplatform.exception.ServiceUnavailableException;
import com.example.jobplatform.llm.DeepseekChatClient;
import com.example.jobplatform.llm.PrivacyTextSanitizer;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.ResumeSkillMapper;
import com.example.jobplatform.service.JobSelectionAdviceService;
import com.example.jobplatform.vo.JobSelectionAdviceVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobSelectionAdviceServiceImpl implements JobSelectionAdviceService {

    private static final String SYSTEM_PROMPT = """
        你是面向大学生的求职顾问。用户会提供一份简历摘要（含解析文本与技能列表）以及平台内在招岗位的公开信息清单（仅含职位要求与组织公开字段，不含个人隐私）。
        请基于这些信息，用自然中文给出岗位选择与投递策略建议。
        要求：
        1. 只根据用户提供的岗位列表讨论，不要编造列表中不存在的公司或岗位。
        2. 可引用岗位 id 与岗位名称以便对照。
        3. 说明哪些岗位更适合优先投递、哪些可作为备选，并简要说明理由（技能匹配、城市、薪资期望、经验学历等）。
        4. 若岗位描述在上下文中被截断，请在分析中说明信息有限。
        5. 不要提及数据来源、导入方式或内部系统实现细节。
        6. 使用分段与条目，便于阅读，不要使用 Markdown 代码块。""";

    private final ResumeMapper resumeMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final JobInfoMapper jobInfoMapper;
    private final DeepseekChatClient deepseekChatClient;
    private final DeepseekProperties deepseekProperties;

    public JobSelectionAdviceServiceImpl(ResumeMapper resumeMapper,
                                         ResumeSkillMapper resumeSkillMapper,
                                         JobInfoMapper jobInfoMapper,
                                         DeepseekChatClient deepseekChatClient,
                                         DeepseekProperties deepseekProperties) {
        this.resumeMapper = resumeMapper;
        this.resumeSkillMapper = resumeSkillMapper;
        this.jobInfoMapper = jobInfoMapper;
        this.deepseekChatClient = deepseekChatClient;
        this.deepseekProperties = deepseekProperties;
    }

    @Override
    public JobSelectionAdviceVO generateAndPersist(Long resumeId) {
        String apiKey = deepseekProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ServiceUnavailableException("未配置 DeepSeek API Key，请设置环境变量 DEEPSEEK_API_KEY");
        }

        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }

        List<ResumeSkill> skills = resumeSkillMapper.selectList(
            new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId)
        );
        String skillLine = skills.stream()
            .map(ResumeSkill::getSkillName)
            .filter(n -> n != null && !n.isBlank())
            .collect(Collectors.joining(", "));

        String resumeBody = PrivacyTextSanitizer.sanitizePersonalContent(
            truncate(resume.getParsedText(), deepseekProperties.getMaxResumeTextChars())
        );

        List<JobInfo> jobs = jobInfoMapper.selectList(
            new LambdaQueryWrapper<JobInfo>().eq(JobInfo::getStatus, 1).orderByDesc(JobInfo::getId)
        );

        String userPrompt = buildUserPrompt(resume, skillLine, resumeBody, jobs);

        String model = deepseekProperties.getModel();
        String advice = deepseekChatClient.chatCompletion(model, apiKey, SYSTEM_PROMPT, userPrompt);

        UpdateWrapper<Resume> uw = new UpdateWrapper<>();
        uw.eq("id", resumeId)
            .set("job_selection_advice", advice)
            .set("job_selection_advice_model", model)
            .set("updated_at", LocalDateTime.now());
        resumeMapper.update(null, uw);

        return new JobSelectionAdviceVO(advice, model);
    }

    private String buildUserPrompt(Resume resume, String skillLine, String resumeBody, List<JobInfo> jobs) {
        StringBuilder sb = new StringBuilder(16_384);
        sb.append("【简历】\n");
        sb.append("resumeId=").append(resume.getId()).append('\n');
        sb.append("名称: ").append(nullToDash(PrivacyTextSanitizer.sanitizePersonalContent(resume.getResumeName()))).append('\n');
        sb.append("目标岗位: ").append(nullToDash(PrivacyTextSanitizer.sanitizePersonalContent(resume.getTargetJobName()))).append('\n');
        sb.append("技能(已保存): ").append(skillLine.isBlank() ? "无" : skillLine).append('\n');
        sb.append("解析文本:\n").append(resumeBody.isBlank() ? "(空)" : resumeBody).append('\n');
        sb.append("\n【在招岗位清单】共 ").append(jobs.size()).append(" 条\n");
        int maxDesc = deepseekProperties.getMaxJobDescriptionChars();
        for (JobInfo j : jobs) {
            sb.append("- id=").append(j.getId())
                .append(" | ").append(nullToDash(j.getJobName()))
                .append(" | ").append(nullToDash(j.getCompanyName()))
                .append(" | ").append(nullToDash(j.getCity()))
                .append(" | 薪资 ").append(j.getSalaryMin()).append("-").append(j.getSalaryMax())
                .append(" | 学历 ").append(nullToDash(j.getEducation()))
                .append(" | 经验 ").append(nullToDash(j.getExperience()))
                .append(" | 技能标签 ").append(nullToDash(j.getSkillTags()))
                .append('\n');
            String desc = PrivacyTextSanitizer.sanitizeJobPostingText(truncate(j.getJobDescription(), maxDesc));
            sb.append("  描述: ").append(desc.isBlank() ? "(无)" : desc).append('\n');
        }
        sb.append("\n请输出岗位选择建议。");
        return sb.toString();
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...(已截断)";
    }
}
