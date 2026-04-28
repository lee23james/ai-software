package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.vo.DashboardStatsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsVO> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }
}

