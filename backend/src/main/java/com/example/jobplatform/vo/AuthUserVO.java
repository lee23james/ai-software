package com.example.jobplatform.vo;

import java.time.LocalDateTime;

public record AuthUserVO(
    Long userId,
    String username,
    String phone,
    String email,
    LocalDateTime lastLoginAt
) {
}
