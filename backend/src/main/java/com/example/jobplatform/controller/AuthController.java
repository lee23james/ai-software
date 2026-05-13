package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.dto.LoginRequestDTO;
import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.service.OperationLogService;
import com.example.jobplatform.vo.AuthUserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OperationLogService operationLogService;

    public AuthController(AuthService authService, OperationLogService operationLogService) {
        this.authService = authService;
        this.operationLogService = operationLogService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthUserVO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthUserVO user = authService.register(request);
        operationLogService.log(user.userId(), "用户注册", "注册", "新用户注册: " + user.username());
        return ApiResponse.ok(user);
    }

    @PostMapping("/login")
    public ApiResponse<AuthUserVO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        try {
            AuthUserVO user = authService.login(request);
            operationLogService.logLogin(user.userId(), user.username(), true, ipAddress, null);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            operationLogService.logLogin(null, request.getIdentifier(), false, ipAddress, e.getMessage());
            throw e;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
