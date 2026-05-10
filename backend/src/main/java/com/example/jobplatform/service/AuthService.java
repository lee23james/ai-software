package com.example.jobplatform.service;

import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.vo.AuthUserVO;

public interface AuthService {

    AuthUserVO register(RegisterRequestDTO request);

    AuthUserVO login(LoginRequestDTO request);

    /**
     * 用于前端校验本地缓存的 userId 是否仍对应有效账号（例如数据库重建后 ID 失效）。
     */
    AuthUserVO requireActiveUser(Long userId);
}
