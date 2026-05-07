package com.example.jobplatform.service;

import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.vo.AuthUserVO;

public interface AuthService {

    AuthUserVO register(RegisterRequestDTO request);

    AuthUserVO login(LoginRequestDTO request);
}
