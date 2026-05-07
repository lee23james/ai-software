package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public record ResumeHistoryVO(
    Long resumeId,
    String resumeName,
    String fileUrl,
    String fileType,
    String targetJobName,
    Integer parseStatus,
    String resumeTextPreview,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
