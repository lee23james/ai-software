package com.example.jobplatform.service.impl;

import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.vo.DashboardStatsVO;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Override
    public DashboardStatsVO dashboard() {
        return new DashboardStatsVO(1280, 326, 148, 5);
    }
}

