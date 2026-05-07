package com.example.jobplatform.vo;

import java.math.BigDecimal;

public record ResumeSkillVO(
    Long id,
    Long resumeId,
    String skillName,
    Integer skillLevel,
    BigDecimal yearsOfExperience
) {
}
