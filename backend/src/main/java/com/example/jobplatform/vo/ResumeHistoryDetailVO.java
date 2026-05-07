package com.example.jobplatform.vo;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeHistoryDetailVO(
    Long resumeId,
    String resumeName,
    String fileUrl,
    String fileType,
    String targetJobName,
    Integer parseStatus,
    String resumeText,
    List<ResumeSkillVO> skills,
    ResumeParseResultVO parseResult,
    List<JobMatchVO> matches,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
