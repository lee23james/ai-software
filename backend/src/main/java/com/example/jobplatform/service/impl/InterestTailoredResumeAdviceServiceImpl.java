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
import com.example.jobplatform.service.InterestTailoredResumeAdviceService;
import com.example.jobplatform.service.UserInterestService;
import com.example.jobplatform.vo.InterestJobVO;
import com.example.jobplatform.vo.JobSelectionAdviceVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterestTailoredResumeAdviceServiceImpl implements InterestTailoredResumeAdviceService {

    private static final String SYSTEM_PROMPT = """
        你是资深简历与校招顾问。用户已保存【兴趣岗位名称】列表（含优先级）。
        你还会看到该用户当前简历的解析文本（已脱敏处理）、已保存技能，以及与意向相关的在招岗位公开摘录（描述可能被截断，不含内部数据说明）。
        请针对这些兴趣岗位方向，输出尽量可执行的简历修改建议，包括但不限于：
        个人摘要/自我评价如何改写；教育、项目、实习经历的 bullet 如何用 STAR 对齐岗位关键词；技能排序与证书补充；
        如何避免空泛表述；若简历与意向岗位差距大，如何诚实补强叙事。
        若某意向岗位没有匹配到具体岗位描述，请依据该岗位名称的常见要求合理推断，并明确说明你在推断。
        不要提及数据来源、导入方式或内部系统实现细节；不要复述或猜测证件号、手机号等敏感信息。
        使用中文，分段分条，不要使用 Markdown 代码块。""";

    private static final int MAX_RELATED_JOBS = 24;

    private final ResumeMapper resumeMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final JobInfoMapper jobInfoMapper;
    private final UserInterestService userInterestService;
    private final DeepseekChatClient deepseekChatClient;
    private final DeepseekProperties deepseekProperties;

    public InterestTailoredResumeAdviceServiceImpl(ResumeMapper resumeMapper,
                                                   ResumeSkillMapper resumeSkillMapper,
                                                   JobInfoMapper jobInfoMapper,
                                                   UserInterestService userInterestService,
                                                   DeepseekChatClient deepseekChatClient,
                                                   DeepseekProperties deepseekProperties) {
        this.resumeMapper = resumeMapper;
        this.resumeSkillMapper = resumeSkillMapper;
        this.jobInfoMapper = jobInfoMapper;
        this.userInterestService = userInterestService;
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

        List<InterestJobVO> interests = userInterestService.listInterestJobs(resume.getUserId());
        if (interests.isEmpty()) {
            throw new IllegalArgumentException("请先在「简历辅助」页保存兴趣岗位后再生成建议");
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

        List<JobInfo> activeJobs = jobInfoMapper.selectList(
            new LambdaQueryWrapper<JobInfo>().eq(JobInfo::getStatus, 1)
        );
        List<JobInfo> related = pickRelatedJobs(interests, activeJobs);

        String userPrompt = buildUserPrompt(resume, skillLine, resumeBody, interests, related);

        String model = deepseekProperties.getModel();
        String advice = deepseekChatClient.chatCompletion(model, apiKey, SYSTEM_PROMPT, userPrompt);

        UpdateWrapper<Resume> uw = new UpdateWrapper<>();
        uw.eq("id", resumeId)
            .set("interest_resume_advice", advice)
            .set("interest_resume_advice_model", model)
            .set("updated_at", LocalDateTime.now());
        resumeMapper.update(null, uw);

        return new JobSelectionAdviceVO(advice, model);
    }

    private List<JobInfo> pickRelatedJobs(List<InterestJobVO> interests, List<JobInfo> activeJobs) {
        Map<Long, JobInfo> byId = new LinkedHashMap<>();
        outer:
        for (InterestJobVO vo : interests) {
            if (vo.jobName() == null || vo.jobName().isBlank()) {
                continue;
            }
            String needle = vo.jobName().trim().toLowerCase(Locale.ROOT);
            for (JobInfo j : activeJobs) {
                if (byId.size() >= MAX_RELATED_JOBS) {
                    break outer;
                }
                if (matchesInterest(needle, j)) {
                    byId.putIfAbsent(j.getId(), j);
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    private static boolean matchesInterest(String interestLower, JobInfo j) {
        String jn = emptyToLower(j.getJobName());
        String st = emptyToLower(j.getSkillTags());
        String jd = emptyToLower(j.getJobDescription());
        return jn.contains(interestLower)
            || (!jn.isEmpty() && interestLower.contains(jn))
            || st.contains(interestLower)
            || jd.contains(interestLower);
    }

    private static String emptyToLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private String buildUserPrompt(Resume resume,
                                   String skillLine,
                                   String resumeBody,
                                   List<InterestJobVO> interests,
                                   List<JobInfo> relatedJobs) {
        StringBuilder sb = new StringBuilder(16_384);
        sb.append("【简历】\n");
        sb.append("resumeId=").append(resume.getId()).append('\n');
        sb.append("名称: ").append(nullToDash(PrivacyTextSanitizer.sanitizePersonalContent(resume.getResumeName()))).append('\n');
        sb.append("简历上填写的目标岗位: ").append(nullToDash(PrivacyTextSanitizer.sanitizePersonalContent(resume.getTargetJobName()))).append('\n');
        sb.append("已保存技能: ").append(skillLine.isBlank() ? "无" : skillLine).append('\n');
        sb.append("解析文本:\n").append(resumeBody.isBlank() ? "(空)" : resumeBody).append('\n');

        sb.append("\n【用户保存的兴趣岗位】\n");
        for (InterestJobVO vo : interests) {
            sb.append("- 优先级 P").append(vo.priority() == null ? "?" : vo.priority())
                .append(" : ").append(nullToDash(vo.jobName())).append('\n');
        }

        sb.append("\n【与意向相关的在招岗位摘录】共 ").append(relatedJobs.size()).append(" 条（无则仅按意向名称分析）\n");
        int maxDesc = deepseekProperties.getMaxJobDescriptionChars();
        for (JobInfo j : relatedJobs) {
            sb.append("- id=").append(j.getId())
                .append(" | ").append(nullToDash(j.getJobName()))
                .append(" | ").append(nullToDash(j.getCompanyName()))
                .append(" | ").append(nullToDash(j.getCity()))
                .append(" | 薪资 ").append(j.getSalaryMin()).append("-").append(j.getSalaryMax())
                .append(" | 技能标签 ").append(nullToDash(j.getSkillTags()))
                .append('\n');
            String desc = PrivacyTextSanitizer.sanitizeJobPostingText(truncate(j.getJobDescription(), maxDesc));
            sb.append("  描述: ").append(desc.isBlank() ? "(无)" : desc).append('\n');
        }

        sb.append("\n请输出针对上述兴趣岗位的详细简历修改建议。");
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
