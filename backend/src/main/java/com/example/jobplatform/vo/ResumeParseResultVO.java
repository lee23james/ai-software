package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public record ResumeParseResultVO(
    Long id,
    String parsedName,
    String parsedEducation,
    String parsedSchool,
    String parsedMajor,
    String parsedSkillsJson,
    String parsedProjectsJson,
    String suggestions,
    String rawResultJson,
    String modelName,
    LocalDateTime createdAt
) {
}
