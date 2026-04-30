package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.vo.AuthUserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthUserVO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserVO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponse.ok(authService.login(request));
    }
}
