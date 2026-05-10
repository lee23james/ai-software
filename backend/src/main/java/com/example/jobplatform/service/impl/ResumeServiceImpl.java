package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.jobplatform.dto.CreateResumeRequestDTO;
import com.example.jobplatform.dto.ResumeSkillInputDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.entity.JobMatchResult;
import com.example.jobplatform.entity.ResumeParseResult;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.ResumeSkill;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.JobMatchResultMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.ResumeParseResultMapper;
import com.example.jobplatform.mapper.ResumeSkillMapper;
import com.example.jobplatform.mapper.UserProfileMapper;
import com.example.jobplatform.service.ResumeService;
import com.example.jobplatform.service.PdfResumeDocumentParser;
import com.example.jobplatform.util.JobSalaryMonthlyYuan;
import com.example.jobplatform.vo.JobMatchVO;
import com.example.jobplatform.vo.ResumeCreateVO;
import com.example.jobplatform.vo.ResumeHistoryDetailVO;
import com.example.jobplatform.vo.ResumeHistoryVO;
import com.example.jobplatform.vo.ResumeParseResultVO;
import com.example.jobplatform.vo.ResumeSkillVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String MATCH_ALGORITHM_VERSION = "v1";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final Pattern UNIVERSITY_PATTERN = Pattern.compile("([\\p{IsHan}A-Za-z]{2,30}大学)");
    private static final Pattern GRADUATION_PATTERN = Pattern.compile("毕业");
    /** 岗位库 salary 为 0 时，从 JD 文本回退解析月薪（元），便于与意向区间比较 */
    private static final Pattern JD_SALARY_MONTHLY_WAN = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*[-－~～至到—]\\s*(\\d+(?:\\.\\d+)?)\\s*万\\s*/\\s*月"
    );
    private static final Pattern JD_SALARY_ANNUAL_WAN = Pattern.compile(
        "年\\s*薪\\s*(\\d+(?:\\.\\d+)?)\\s*[-－~～至到—]\\s*(\\d+(?:\\.\\d+)?)\\s*万"
    );
    private static final Pattern JD_SALARY_LABELED_YUAN = Pattern.compile(
        "(?:月薪|薪资|工资|基薪|待遇|综合收入)[:：\\s　]*(?:人民币|RMB|rmb)?\\s*(\\d{3,7})\\s*[-－~～至到—]\\s*(\\d{3,7})"
    );
    private static final Pattern JD_SALARY_K_RANGE = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*[-－~～至到—]\\s*(\\d+(?:\\.\\d+)?)\\s*[kK千]"
    );
    private static final Pattern JD_SALARY_WAN_RANGE = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*[-－~～至到—]\\s*(\\d+(?:\\.\\d+)?)\\s*万"
    );
    /** 来自东南大学 / 就读于清华大学 */
    private static final Pattern SCHOOL_CONTEXT_PATTERN = Pattern.compile(
        "(?:来自|就读于|毕业于|升学至|保送|考入)\\s*([\\p{IsHan}A-Za-z]{2,30}大学)"
    );
    private static final Pattern SCHOOL_LABEL_PATTERN = Pattern.compile(
        "学校[：:\\s　]+([^\\n，。；]{2,40})"
    );
    /** 专业：软件工程 / 主修 xxx / 所学专业：xxx */
    private static final Pattern MAJOR_LABEL_PATTERN = Pattern.compile(
        "(?:所学专业|主修|专业)[：:\\s　]+([^\\n，。；]{2,40})"
    );
    /** 本科专业是计算机科学与技术 / 专业为软件工程 */
    private static final Pattern MAJOR_SHI_WEI_PATTERN = Pattern.compile(
        "专业(?:是|为)[：:\\s　]*([^\\n，。；]{2,40})"
    );
    /** 东南大学 计算机科学与技术 本科 */
    private static final Pattern MAJOR_BETWEEN_UNI_AND_DEGREE = Pattern.compile(
        "([\\p{IsHan}A-Za-z]{2,30}大学)\\s+([\\p{IsHan}A-Za-z0-9·]{2,30}?)\\s*(本科|硕士|博士)"
    );

    private final ResumeMapper resumeMapper;
    private final ResumeSkillMapper resumeSkillMapper;
    private final ResumeParseResultMapper resumeParseResultMapper;
    private final JobInfoMapper jobInfoMapper;
    private final JobMatchResultMapper jobMatchResultMapper;
    private final AccountUserMapper accountUserMapper;
    private final UserProfileMapper userProfileMapper;
    private final PdfResumeDocumentParser pdfResumeDocumentParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeServiceImpl(ResumeMapper resumeMapper,
                             ResumeSkillMapper resumeSkillMapper,
                             ResumeParseResultMapper resumeParseResultMapper,
                             JobInfoMapper jobInfoMapper,
                             JobMatchResultMapper jobMatchResultMapper,
                             AccountUserMapper accountUserMapper,
                             UserProfileMapper userProfileMapper,
                             PdfResumeDocumentParser pdfResumeDocumentParser) {
        this.resumeMapper = resumeMapper;
        this.resumeSkillMapper = resumeSkillMapper;
        this.resumeParseResultMapper = resumeParseResultMapper;
        this.jobInfoMapper = jobInfoMapper;
        this.jobMatchResultMapper = jobMatchResultMapper;
        this.accountUserMapper = accountUserMapper;
        this.userProfileMapper = userProfileMapper;
        this.pdfResumeDocumentParser = pdfResumeDocumentParser;
    }

    @Override
    public ResumeCreateVO createResume(CreateResumeRequestDTO request) {
        ensureUserExists(request.getUserId());
        Resume resume = new Resume();
        resume.setUserId(request.getUserId());
        resume.setResumeName(request.getResumeName().trim());
        resume.setSourceType("manual");
        resume.setFileType("manual");
        resume.setParsedText(request.getParsedText());
        resume.setTargetJobName(request.getTargetJobName());
        resume.setStatus(request.getParsedText() == null || request.getParsedText().isBlank() ? 0 : 2);
        resume.setIsDefault(0);
        resumeMapper.insert(resume);
        upsertStudentProfile(request);

        if (request.getSkills() != null) {
            for (ResumeSkillInputDTO input : request.getSkills()) {
                ResumeSkill skill = new ResumeSkill();
                skill.setResumeId(resume.getId());
                skill.setSkillName(input.getSkillName().trim());
                skill.setSkillLevel(input.getSkillLevel());
                skill.setYearsOfExperience(input.getYearsOfExperience());
                resumeSkillMapper.insert(skill);
            }
        }
        return new ResumeCreateVO(resume.getId());
    }

    @Override
    public ResumeCreateVO uploadResume(Long userId, String resumeName, String targetJobName, MultipartFile file, String skillsText) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (resumeName == null || resumeName.isBlank()) {
            throw new IllegalArgumentException("简历名称不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传简历文件");
        }
        ensureUserExists(userId);

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setResumeName(resumeName.trim());
        resume.setFileType(resolveFileType(file));
        resume.setSourceType("upload");
        resume.setTargetJobName(targetJobName == null ? null : targetJobName.trim());
        resume.setIsDefault(0);
        resume.setFileUrl(saveResumeFile(file));
        String resumeText = pdfResumeDocumentParser.extractText(file);
        resume.setParsedText(resumeText);
        resume.setStatus(resumeText == null || resumeText.isBlank() ? 3 : 2);
        resumeMapper.insert(resume);

        saveParseResult(resume.getId(), resumeText, targetJobName, skillsText);
        List<String> skillNames = parseCsvSkills(skillsText);
        for (String skillName : skillNames) {
            ResumeSkill skill = new ResumeSkill();
            skill.setResumeId(resume.getId());
            skill.setSkillName(skillName);
            resumeSkillMapper.insert(skill);
        }
        return new ResumeCreateVO(resume.getId());
    }

    @Override
    public List<JobMatchVO> triggerMatch(Long resumeId, Integer topN) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        int safeTopN = Math.max(1, Math.min(topN == null ? 20 : topN, 100));
        List<ResumeSkill> resumeSkills = resumeSkillMapper.selectList(
            new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId)
        );
        Set<String> normalizedSkills = normalizeSkills(resumeSkills.stream().map(ResumeSkill::getSkillName).toList());

        List<JobInfo> jobs = jobInfoMapper.selectList(
            new LambdaQueryWrapper<JobInfo>().eq(JobInfo::getStatus, 1)
        );
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, resume.getUserId())
        );
        CandidateProfile candidateProfile = toCandidateProfile(profile, resume);

        List<JobMatchResult> matchResults = new ArrayList<>();
        for (JobInfo job : jobs) {
            ScoreBundle score = calculateScore(candidateProfile, normalizedSkills, job);
            JobMatchResult match = new JobMatchResult();
            match.setUserId(resume.getUserId());
            match.setResumeId(resumeId);
            match.setJobId(job.getId());
            match.setTotalScore(score.totalScore);
            match.setSkillScore(score.skillScore);
            match.setExperienceScore(score.experienceScore);
            match.setEducationScore(score.educationScore);
            match.setCityScore(score.cityScore);
            match.setSalaryScore(score.salaryScore);
            match.setReasonJson(buildReasonJson(score));
            match.setAlgorithmVersion(MATCH_ALGORITHM_VERSION);
            matchResults.add(match);
        }

        matchResults.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        List<JobMatchResult> topMatches = matchResults.stream().limit(safeTopN).toList();

        jobMatchResultMapper.delete(new LambdaQueryWrapper<JobMatchResult>()
            .eq(JobMatchResult::getResumeId, resumeId)
            .eq(JobMatchResult::getAlgorithmVersion, MATCH_ALGORITHM_VERSION));
        topMatches.forEach(jobMatchResultMapper::insert);

        return toMatchVO(topMatches);
    }

    @Override
    public List<JobMatchVO> listMatches(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        List<JobMatchResult> matches = jobMatchResultMapper.selectList(
            new LambdaQueryWrapper<JobMatchResult>()
                .eq(JobMatchResult::getResumeId, resumeId)
                .orderByDesc(JobMatchResult::getTotalScore)
                .orderByDesc(JobMatchResult::getId)
        );
        return toMatchVO(matches);
    }

    @Override
    public List<ResumeHistoryVO> listResumeHistory(Long userId) {
        ensureUserExists(userId);
        List<Resume> resumes = resumeMapper.selectList(
            new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, userId)
                .orderByDesc(Resume::getCreatedAt)
                .orderByDesc(Resume::getId)
        );
        return resumes.stream().map(this::toResumeHistoryVO).toList();
    }

    @Override
    public ResumeHistoryDetailVO getResumeHistoryDetail(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        List<ResumeSkillVO> skills = resumeSkillMapper.selectList(
            new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId)
        ).stream().map(this::toResumeSkillVO).toList();
        ResumeParseResult parseResult = resumeParseResultMapper.selectOne(
            new LambdaQueryWrapper<ResumeParseResult>()
                .eq(ResumeParseResult::getResumeId, resumeId)
                .orderByDesc(ResumeParseResult::getCreatedAt)
                .last("limit 1")
        );
        List<JobMatchVO> matches = listMatches(resumeId);
        return new ResumeHistoryDetailVO(
            resume.getId(),
            resume.getResumeName(),
            resume.getFileUrl(),
            resume.getFileType(),
            resume.getTargetJobName(),
            resume.getStatus(),
            resume.getParsedText(),
            skills,
            parseResult == null ? null : toResumeParseResultVO(parseResult),
            matches,
            resume.getCreatedAt(),
            resume.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        if (userId == null || resumeId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        ensureUserExists(userId);
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new IllegalArgumentException("简历不存在");
        }
        if (!userId.equals(resume.getUserId())) {
            throw new IllegalArgumentException("无权删除该简历");
        }
        resumeMapper.deleteJobApplicationsByResumeId(resumeId);
        jobMatchResultMapper.delete(new LambdaQueryWrapper<JobMatchResult>().eq(JobMatchResult::getResumeId, resumeId));
        resumeParseResultMapper.delete(new LambdaQueryWrapper<ResumeParseResult>().eq(ResumeParseResult::getResumeId, resumeId));
        resumeSkillMapper.delete(new LambdaQueryWrapper<ResumeSkill>().eq(ResumeSkill::getResumeId, resumeId));
        resumeMapper.deleteById(resumeId);
    }

    private List<JobMatchVO> toMatchVO(List<JobMatchResult> matches) {
        if (matches.isEmpty()) {
            return List.of();
        }
        Set<Long> jobIds = matches.stream().map(JobMatchResult::getJobId).collect(HashSet::new, HashSet::add, HashSet::addAll);
        Map<Long, JobInfo> jobMap = new HashMap<>();
        for (JobInfo job : jobInfoMapper.selectBatchIds(jobIds)) {
            jobMap.put(job.getId(), job);
        }
        return matches.stream().map(item -> {
            JobInfo job = jobMap.get(item.getJobId());
            return new JobMatchVO(
                item.getJobId(),
                job == null ? null : job.getJobName(),
                job == null ? null : job.getCompanyName(),
                job == null ? null : job.getCity(),
                item.getTotalScore(),
                item.getSkillScore(),
                item.getExperienceScore(),
                item.getEducationScore(),
                item.getCityScore(),
                item.getSalaryScore(),
                item.getReasonJson()
            );
        }).toList();
    }

    private ScoreBundle calculateScore(CandidateProfile profile, Set<String> resumeSkills, JobInfo job) {
        BigDecimal skillScore = calcSkillScore(resumeSkills, splitSkills(job.getSkillTags()));
        BigDecimal experienceScore = calcExperienceScore(profile.workYears, job.getExperience());
        BigDecimal educationScore = calcEducationScore(profile.education, job.getEducation());
        BigDecimal cityScore = calcCityScore(profile.targetCity, job.getCity());
        int[] jobSalary = resolveEffectiveMonthlySalaryRange(job);
        BigDecimal salaryScore = calcSalaryScore(
            profile.expectedSalaryMin,
            profile.expectedSalaryMax,
            jobSalary == null ? null : jobSalary[0],
            jobSalary == null ? null : jobSalary[1]
        );
        BigDecimal total = skillScore.multiply(BigDecimal.valueOf(0.40))
            .add(experienceScore.multiply(BigDecimal.valueOf(0.15)))
            .add(educationScore.multiply(BigDecimal.valueOf(0.15)))
            .add(cityScore.multiply(BigDecimal.valueOf(0.10)))
            .add(salaryScore.multiply(BigDecimal.valueOf(0.20)))
            .setScale(2, RoundingMode.HALF_UP);
        return new ScoreBundle(total, skillScore, experienceScore, educationScore, cityScore, salaryScore);
    }

    private BigDecimal calcSkillScore(Set<String> resumeSkills, Set<String> jobSkills) {
        if (jobSkills.isEmpty()) {
            return BigDecimal.valueOf(60);
        }
        long hitCount = jobSkills.stream().filter(resumeSkills::contains).count();
        return BigDecimal.valueOf(hitCount * 100.0 / jobSkills.size()).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcExperienceScore(BigDecimal resumeWorkYears, String jobExperience) {
        if (resumeWorkYears == null || jobExperience == null || jobExperience.isBlank() || "不限".equals(jobExperience.trim())) {
            return BigDecimal.valueOf(60);
        }
        Matcher matcher = NUMBER_PATTERN.matcher(jobExperience);
        if (!matcher.find()) {
            return BigDecimal.valueOf(60);
        }
        BigDecimal required = new BigDecimal(matcher.group(1));
        if (resumeWorkYears.compareTo(required) >= 0) {
            return BigDecimal.valueOf(100);
        }
        if (required.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.valueOf(60);
        }
        return resumeWorkYears.multiply(BigDecimal.valueOf(100))
            .divide(required, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcEducationScore(String resumeEducation, String jobEducation) {
        if (jobEducation == null || jobEducation.isBlank() || "不限".equals(jobEducation.trim())) {
            return BigDecimal.valueOf(100);
        }
        if (resumeEducation == null || resumeEducation.isBlank()) {
            return BigDecimal.valueOf(30);
        }
        int resumeLevel = educationLevel(resumeEducation);
        int requiredLevel = educationLevel(jobEducation);
        if (resumeLevel >= requiredLevel) {
            return BigDecimal.valueOf(100);
        }
        return BigDecimal.valueOf(40);
    }

    private BigDecimal calcCityScore(String targetCity, String jobCity) {
        if (targetCity == null || targetCity.isBlank() || jobCity == null || jobCity.isBlank()) {
            return BigDecimal.valueOf(60);
        }
        return targetCity.trim().equalsIgnoreCase(jobCity.trim()) ? BigDecimal.valueOf(100) : BigDecimal.valueOf(20);
    }

    /**
     * 优先使用库里的 salary_min/max；若为 0（常见于未清洗数据），尝试从岗位描述解析月薪区间。
     */
    private int[] resolveEffectiveMonthlySalaryRange(JobInfo job) {
        Integer rawMin = job.getSalaryMin();
        Integer rawMax = job.getSalaryMax();
        int a = rawMin == null ? 0 : rawMin;
        int b = rawMax == null ? 0 : rawMax;
        if (a > b) {
            int t = a;
            a = b;
            b = t;
        }
        if (b > 0) {
            return JobSalaryMonthlyYuan.storedPairToMonthlyYuan(a, b);
        }
        String jd = job.getJobDescription();
        String title = job.getJobName();
        String blob = ((jd == null ? "" : jd) + "\n" + (title == null ? "" : title)).trim();
        return parseMonthlySalaryRangeFromText(blob);
    }

    private int[] parseMonthlySalaryRangeFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        int[] r = trySalaryTwoDoubles(JD_SALARY_MONTHLY_WAN, text, 10000.0, 10000.0);
        if (r != null) {
            return r;
        }
        r = trySalaryAnnualWanToMonthly(text);
        if (r != null) {
            return r;
        }
        r = trySalaryLabeledYuan(text);
        if (r != null) {
            return r;
        }
        r = trySalaryTwoDoubles(JD_SALARY_K_RANGE, text, 1000.0, 1000.0);
        if (r != null) {
            return r;
        }
        r = trySalaryWanRangeMonthly(text);
        if (r != null) {
            return r;
        }
        return null;
    }

    private int[] trySalaryAnnualWanToMonthly(String text) {
        Matcher m = JD_SALARY_ANNUAL_WAN.matcher(text);
        if (!m.find()) {
            return null;
        }
        double d1 = Double.parseDouble(m.group(1));
        double d2 = Double.parseDouble(m.group(2));
        int lo = (int) Math.round(Math.min(d1, d2) * 10000.0 / 12.0);
        int hi = (int) Math.round(Math.max(d1, d2) * 10000.0 / 12.0);
        return normalizeParsedSalaryRange(lo, hi);
    }

    private int[] trySalaryLabeledYuan(String text) {
        Matcher m = JD_SALARY_LABELED_YUAN.matcher(text);
        if (!m.find()) {
            return null;
        }
        int lo = Integer.parseInt(m.group(1));
        int hi = Integer.parseInt(m.group(2));
        return normalizeParsedSalaryRange(lo, hi);
    }

    private int[] trySalaryWanRangeMonthly(String text) {
        Matcher m = JD_SALARY_WAN_RANGE.matcher(text);
        while (m.find()) {
            int start = m.start();
            if (start > 0) {
                String prefix = text.substring(Math.max(0, start - 4), start);
                if (prefix.contains("年")) {
                    continue;
                }
            }
            double d1 = Double.parseDouble(m.group(1));
            double d2 = Double.parseDouble(m.group(2));
            int lo = (int) Math.round(Math.min(d1, d2) * 10000.0);
            int hi = (int) Math.round(Math.max(d1, d2) * 10000.0);
            int[] norm = normalizeParsedSalaryRange(lo, hi);
            if (norm != null) {
                return norm;
            }
        }
        return null;
    }

    private int[] trySalaryTwoDoubles(Pattern pattern, String text, double mul1, double mul2) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return null;
        }
        double d1 = Double.parseDouble(m.group(1));
        double d2 = Double.parseDouble(m.group(2));
        int lo = (int) Math.round(Math.min(d1, d2) * mul1);
        int hi = (int) Math.round(Math.max(d1, d2) * mul2);
        return normalizeParsedSalaryRange(lo, hi);
    }

    /** 过滤明显非月薪的误匹配，并限制在合理区间 */
    private int[] normalizeParsedSalaryRange(int lo, int hi) {
        if (hi <= 0) {
            return null;
        }
        if (lo > hi) {
            int t = lo;
            lo = hi;
            hi = t;
        }
        if (hi < 1000 || lo > 500_000) {
            return null;
        }
        return new int[] { lo, hi };
    }

    private BigDecimal calcSalaryScore(Integer expectMin, Integer expectMax, Integer jobMin, Integer jobMax) {
        if (expectMin == null || expectMax == null) {
            return BigDecimal.valueOf(60);
        }
        int eLo = Math.min(expectMin, expectMax);
        int eHi = Math.max(expectMin, expectMax);
        if (jobMin == null || jobMax == null) {
            return BigDecimal.valueOf(60);
        }
        int jLo = Math.min(jobMin, jobMax);
        int jHi = Math.max(jobMin, jobMax);
        if (jHi <= 0) {
            return BigDecimal.valueOf(60);
        }
        int overlap = Math.max(0, Math.min(eHi, jHi) - Math.max(eLo, jLo));
        int expectRange = Math.max(1, eHi - eLo);
        return BigDecimal.valueOf(overlap * 100.0 / expectRange).setScale(2, RoundingMode.HALF_UP);
    }

    private int educationLevel(String education) {
        String text = education.trim().toLowerCase(Locale.ROOT);
        if (text.contains("博士")) {
            return 5;
        }
        if (text.contains("硕士") || text.contains("研究生")) {
            return 4;
        }
        if (text.contains("本科")) {
            return 3;
        }
        if (text.contains("大专")) {
            return 2;
        }
        if (text.contains("高中") || text.contains("中专")) {
            return 1;
        }
        return 0;
    }

    private Set<String> normalizeSkills(List<String> skills) {
        Set<String> result = new HashSet<>();
        for (String skill : skills) {
            if (skill == null) {
                continue;
            }
            String normalized = skill.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Set<String> splitSkills(String skillTags) {
        if (skillTags == null || skillTags.isBlank()) {
            return Set.of();
        }
        return normalizeSkills(Arrays.stream(skillTags.split(",")).toList());
    }

    private String buildReasonJson(ScoreBundle score) {
        Map<String, Object> reason = Map.of(
            "totalScore", score.totalScore,
            "skillScore", score.skillScore,
            "experienceScore", score.experienceScore,
            "educationScore", score.educationScore,
            "cityScore", score.cityScore,
            "salaryScore", score.salaryScore
        );
        try {
            return objectMapper.writeValueAsString(reason);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String saveResumeFile(MultipartFile file) {
        try {
            Path uploadDir = Path.of("uploads", "resumes");
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename() == null ? "resume.bin" : file.getOriginalFilename();
            String safeName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadDir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new IllegalArgumentException("简历文件保存失败");
        }
    }

    private List<String> parseCsvSkills(String skillsText) {
        if (skillsText == null || skillsText.isBlank()) {
            return List.of();
        }
        return Arrays.stream(skillsText.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .distinct()
            .toList();
    }

    private void ensureUserExists(Long userId) {
        AccountUser user = accountUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException(
                "用户不存在。若刚重置过数据库，请退出登录后重新注册；或确认当前页面显示的「用户ID」与已注册账号一致。"
            );
        }
    }

    private void upsertStudentProfile(CreateResumeRequestDTO request) {
        UserProfile profile = userProfileMapper.selectOne(
            new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, request.getUserId())
        );
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(request.getUserId());
            profile.setTargetCity(request.getTargetCity());
            profile.setTargetPosition(request.getTargetJobName());
            profile.setHighestEducation(request.getEducation());
            userProfileMapper.insert(profile);
        } else {
            profile.setTargetCity(request.getTargetCity());
            profile.setTargetPosition(request.getTargetJobName());
            profile.setHighestEducation(request.getEducation());
            userProfileMapper.updateById(profile);
        }
    }

    private ResumeHistoryVO toResumeHistoryVO(Resume resume) {
        String preview = resume.getParsedText();
        if (preview != null && preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        return new ResumeHistoryVO(
            resume.getId(),
            resume.getResumeName(),
            resume.getFileUrl(),
            resume.getFileType(),
            resume.getTargetJobName(),
            resume.getStatus(),
            preview,
            resume.getCreatedAt(),
            resume.getUpdatedAt()
        );
    }

    private ResumeSkillVO toResumeSkillVO(ResumeSkill skill) {
        return new ResumeSkillVO(
            skill.getId(),
            skill.getResumeId(),
            skill.getSkillName(),
            skill.getSkillLevel(),
            skill.getYearsOfExperience()
        );
    }

    private ResumeParseResultVO toResumeParseResultVO(ResumeParseResult result) {
        return new ResumeParseResultVO(
            result.getId(),
            result.getParsedName(),
            result.getParsedEducation(),
            result.getParsedSchool(),
            result.getParsedMajor(),
            result.getParsedSkillsJson(),
            result.getParsedProjectsJson(),
            result.getSuggestions(),
            result.getRawResultJson(),
            result.getModelName(),
            result.getCreatedAt()
        );
    }

    private void saveParseResult(Long resumeId, String resumeText, String targetJobName, String skillsText) {
        String education = extractEducation(resumeText);
        String school = extractSchool(resumeText);
        String major = extractMajor(resumeText);

        ResumeParseResult result = new ResumeParseResult();
        result.setResumeId(resumeId);
        result.setParsedName(null);
        result.setParsedEducation(education);
        result.setParsedSchool(school);
        result.setParsedMajor(major);
        result.setParsedSkillsJson(toJsonSafely(parseCsvSkills(skillsText)));
        result.setParsedProjectsJson("[]");
        result.setSuggestions(buildParseSuggestion(resumeText, skillsText, school, major, education, targetJobName));
        Map<String, Object> rawResult = new HashMap<>();
        rawResult.put("resumeTextLength", resumeText == null ? 0 : resumeText.length());
        rawResult.put("targetJobName", targetJobName);
        rawResult.put("parsedSchool", school);
        rawResult.put("parsedMajor", major);
        rawResult.put("parsedEducation", education);
        result.setRawResultJson(toJsonSafely(rawResult));
        result.setModelName("pdf-text-parser-v2");
        resumeParseResultMapper.insert(result);
    }

    private String extractSchool(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return null;
        }
        Matcher context = SCHOOL_CONTEXT_PATTERN.matcher(resumeText);
        if (context.find()) {
            return stripBracketNote(context.group(1));
        }
        Matcher label = SCHOOL_LABEL_PATTERN.matcher(resumeText);
        if (label.find()) {
            String raw = label.group(1).trim();
            Matcher inner = UNIVERSITY_PATTERN.matcher(raw);
            if (inner.find()) {
                return stripBracketNote(inner.group(1));
            }
            return stripBracketNote(raw);
        }
        Matcher uni = UNIVERSITY_PATTERN.matcher(resumeText);
        if (uni.find()) {
            return stripBracketNote(uni.group(1));
        }
        return null;
    }

    private String stripBracketNote(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("[（(][^）)]*[）)]", "").trim();
    }

    private String extractMajor(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return null;
        }
        Matcher label = MAJOR_LABEL_PATTERN.matcher(resumeText);
        if (label.find()) {
            return normalizeMajorToken(label.group(1));
        }
        Matcher shiWei = MAJOR_SHI_WEI_PATTERN.matcher(resumeText);
        if (shiWei.find()) {
            return normalizeMajorToken(shiWei.group(1));
        }
        Matcher between = MAJOR_BETWEEN_UNI_AND_DEGREE.matcher(resumeText);
        if (between.find()) {
            return normalizeMajorToken(between.group(2));
        }
        return null;
    }

    private String normalizeMajorToken(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim().replaceAll("^[：:\\s　]+", "").replaceAll("[，。；;、]+$", "").trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.matches("(本科|硕士|博士|学士|研究生|大专|专科|不限|\\d+.*)")) {
            return null;
        }
        return t;
    }

    private String extractEducation(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return null;
        }
        String explicit = extractEducationFromKeywords(resumeText);
        if (explicit != null) {
            return explicit;
        }
        Matcher matcher = GRADUATION_PATTERN.matcher(resumeText);
        int graduationCount = 0;
        while (matcher.find()) {
            graduationCount++;
        }
        if (graduationCount <= 0) {
            return null;
        }
        if (graduationCount == 1) {
            return "本科";
        }
        if (graduationCount == 2) {
            return "硕士";
        }
        return "博士";
    }

    /** 识别「本科」「硕士」等显式表述（无需出现「毕业」）。 */
    private String extractEducationFromKeywords(String text) {
        if (text.contains("博士研究生")) {
            return "博士";
        }
        if (text.contains("硕士研究生")) {
            return "硕士";
        }
        if (text.contains("博士生")) {
            return "博士";
        }
        if (text.contains("硕士生")) {
            return "硕士";
        }
        if (text.contains("本科")) {
            return "本科";
        }
        if (text.contains("硕士") || text.contains("研究生")) {
            return "硕士";
        }
        if (text.contains("博士")) {
            return "博士";
        }
        if (text.contains("大专") || text.contains("专科")) {
            return "大专";
        }
        return null;
    }

    private String buildParseSuggestion(String resumeText,
                                        String skillsText,
                                        String school,
                                        String major,
                                        String education,
                                        String targetJobName) {
        if (resumeText == null || resumeText.isBlank()) {
            return "当前 PDF 没有提取到可见文本，建议检查是否为扫描件或受保护文件。";
        }
        List<String> extracted = new ArrayList<>();
        if (school != null && !school.isBlank()) {
            extracted.add("学校「" + school + "」");
        }
        if (major != null && !major.isBlank()) {
            extracted.add("专业「" + major + "」");
        }
        if (education != null && !education.isBlank()) {
            extracted.add("学历「" + education + "」");
        }
        String head = "";
        if (!extracted.isEmpty()) {
            head = String.join("，", extracted) + "。";
        }
        String skillHint = "";
        if (skillsText == null || skillsText.isBlank()) {
            skillHint = "建议补充技能关键词，方便后续岗位匹配。";
        } else {
            skillHint = "简历已保存，可在历史页查看原文、技能和匹配结果。";
        }
        String jobHint = "";
        if (targetJobName != null && !targetJobName.isBlank()) {
            jobHint = "本简历适配岗位（表单填写）：" + targetJobName + "。";
        }
        if (head.isEmpty()) {
            return skillHint + (jobHint.isEmpty() ? "" : " " + jobHint);
        }
        return head + " " + skillHint + (jobHint.isEmpty() ? "" : " " + jobHint);
    }

    private String toJsonSafely(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String resolveFileType(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (original.endsWith(".pdf")) {
            return "pdf";
        }
        if (original.endsWith(".txt")) {
            return "txt";
        }
        return "unknown";
    }

    private CandidateProfile toCandidateProfile(UserProfile profile, Resume resume) {
        CandidateProfile candidate = new CandidateProfile();
        candidate.targetCity = profile == null ? null : profile.getTargetCity();
        candidate.education = profile == null ? null : profile.getHighestEducation();
        candidate.workYears = resume.getWorkYears();
        candidate.expectedSalaryMin = profile == null ? null : profile.getExpectedSalaryMin();
        candidate.expectedSalaryMax = profile == null ? null : profile.getExpectedSalaryMax();
        return candidate;
    }

    private static class CandidateProfile {
        private String targetCity;
        private String education;
        private BigDecimal workYears;
        private Integer expectedSalaryMin;
        private Integer expectedSalaryMax;
    }

    private record ScoreBundle(
        BigDecimal totalScore,
        BigDecimal skillScore,
        BigDecimal experienceScore,
        BigDecimal educationScore,
        BigDecimal cityScore,
        BigDecimal salaryScore
    ) {
    }
}
