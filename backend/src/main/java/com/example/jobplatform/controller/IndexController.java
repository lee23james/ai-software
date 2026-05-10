package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IndexController {

    @GetMapping("/")
    public ApiResponse<Map<String, String>> index() {
        return ApiResponse.ok(Map.of(
            "message", "job-platform-backend is running",
            "health", "/api/health"
        ));
    }
}
