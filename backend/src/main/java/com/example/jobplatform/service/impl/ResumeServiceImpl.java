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
import com.example.jobplatform.vo.JobMatchVO;
import com.example.jobplatform.vo.ResumeCreateVO;
import com.example.jobplatform.vo.ResumeHistoryDetailVO;
import com.example.jobplatform.vo.ResumeHistoryVO;
import com.example.jobplatform.vo.ResumeParseResultVO;
import com.example.jobplatform.vo.ResumeSkillVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
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
            resume.getJobSelectionAdvice(),
            resume.getJobSelectionAdviceModel(),
            resume.getInterestResumeAdvice(),
            resume.getInterestResumeAdviceModel(),
            resume.getCreatedAt(),
            resume.getUpdatedAt()
        );
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
        BigDecimal salaryScore = calcSalaryScore(
            profile.expectedSalaryMin,
            profile.expectedSalaryMax,
            job.getSalaryMin(),
            job.getSalaryMax()
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

    private BigDecimal calcSalaryScore(Integer expectMin, Integer expectMax, Integer jobMin, Integer jobMax) {
        if (expectMin == null || expectMax == null || jobMin == null || jobMax == null) {
            return BigDecimal.valueOf(60);
        }
        int overlap = Math.max(0, Math.min(expectMax, jobMax) - Math.max(expectMin, jobMin));
        int expectRange = Math.max(1, expectMax - expectMin);
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
            throw new IllegalArgumentException("用户不存在");
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
        ResumeParseResult result = new ResumeParseResult();
        result.setResumeId(resumeId);
        result.setParsedName(null);
        result.setParsedEducation(extractEducation(resumeText));
        result.setParsedSchool(extractSchool(resumeText));
        result.setParsedMajor(targetJobName);
        result.setParsedSkillsJson(toJsonSafely(parseCsvSkills(skillsText)));
        result.setParsedProjectsJson("[]");
        result.setSuggestions(buildParseSuggestion(resumeText, skillsText));
        Map<String, Object> rawResult = new HashMap<>();
        rawResult.put("resumeTextLength", resumeText == null ? 0 : resumeText.length());
        rawResult.put("targetJobName", targetJobName);
        rawResult.put("parsedSchool", result.getParsedSchool());
        rawResult.put("parsedEducation", result.getParsedEducation());
        result.setRawResultJson(toJsonSafely(rawResult));
        result.setModelName("pdf-text-parser-v2");
        resumeParseResultMapper.insert(result);
    }

    private String extractSchool(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return null;
        }
        Matcher matcher = UNIVERSITY_PATTERN.matcher(resumeText);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private String extractEducation(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return null;
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

    private String buildParseSuggestion(String resumeText, String skillsText) {
        if (resumeText == null || resumeText.isBlank()) {
            return "当前 PDF 没有提取到可见文本，建议检查是否为扫描件或受保护文件。";
        }
        if (skillsText == null || skillsText.isBlank()) {
            return "建议补充技能关键词，方便后续岗位匹配。";
        }
        return "简历已保存，可在历史页查看原文、技能和匹配结果。";
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
        candidate.expectedSalaryMin = null;
        candidate.expectedSalaryMax = null;
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
