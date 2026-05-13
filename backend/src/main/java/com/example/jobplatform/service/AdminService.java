package com.example.jobplatform.service;

import com.example.jobplatform.common.PageResult;
import com.example.jobplatform.dto.AdminUserQueryDTO;
import com.example.jobplatform.dto.OperationLogQueryDTO;
import com.example.jobplatform.vo.DashboardStatsVO;
import com.example.jobplatform.vo.OperationLogVO;
import com.example.jobplatform.vo.UserVO;

public interface AdminService {

    DashboardStatsVO dashboard();

    PageResult<UserVO> pageUsers(AdminUserQueryDTO query);

    void updateUserStatus(Long id, Integer status);

    void deleteUser(Long id);

    PageResult<OperationLogVO> pageLogs(OperationLogQueryDTO query);

    void cleanData(String type);
}

