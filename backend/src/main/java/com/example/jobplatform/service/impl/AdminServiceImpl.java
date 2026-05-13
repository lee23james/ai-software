package com.example.jobplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.jobplatform.common.PageResult;
import com.example.jobplatform.dto.AdminUserQueryDTO;
import com.example.jobplatform.dto.OperationLogQueryDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.entity.OperationLog;
import com.example.jobplatform.mapper.AccountUserMapper;
import com.example.jobplatform.mapper.JobInfoMapper;
import com.example.jobplatform.mapper.OperationLogMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.vo.DashboardStatsVO;
import com.example.jobplatform.vo.OperationLogVO;
import com.example.jobplatform.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final JobInfoMapper jobInfoMapper;
    private final AccountUserMapper accountUserMapper;
    private final ResumeMapper resumeMapper;
    private final OperationLogMapper operationLogMapper;

    public AdminServiceImpl(JobInfoMapper jobInfoMapper, AccountUserMapper accountUserMapper,
                            ResumeMapper resumeMapper, OperationLogMapper operationLogMapper) {
        this.jobInfoMapper = jobInfoMapper;
        this.accountUserMapper = accountUserMapper;
        this.resumeMapper = resumeMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public DashboardStatsVO dashboard() {
        long totalJobs = jobInfoMapper.selectCount(null);
        long totalUsers = accountUserMapper.selectCount(null);
        long totalResumes = resumeMapper.selectCount(null);

        QueryWrapper<JobInfo> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("publish_time", LocalDateTime.now().toLocalDate().atStartOfDay());
        long todayNew = jobInfoMapper.selectCount(todayWrapper);

        return new DashboardStatsVO(
                (int) totalJobs,
                (int) totalUsers,
                (int) totalResumes,
                (int) todayNew
        );
    }

    @Override
    public PageResult<UserVO> pageUsers(AdminUserQueryDTO query) {
        QueryWrapper<AccountUser> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like("username", query.getKeyword())
                    .or()
                    .like("real_name", query.getKeyword())
                    .or()
                    .like("email", query.getKeyword()));
        }
        if (StringUtils.hasText(query.getRole())) {
            wrapper.eq("role", query.getRole());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByDesc("created_at");

        List<AccountUser> allUsers = accountUserMapper.selectList(wrapper);
        int total = allUsers.size();
        int start = (query.getPageNum() - 1) * query.getPageSize();
        int end = Math.min(start + query.getPageSize(), total);
        List<AccountUser> pageData = start < total ? allUsers.subList(start, end) : new ArrayList<>();

        List<UserVO> records = new ArrayList<>();
        for (AccountUser user : pageData) {
            UserVO vo = new UserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            vo.setLastLoginAt(user.getLastLoginAt());
            vo.setCreatedAt(user.getCreatedAt());
            records.add(vo);
        }

        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        AccountUser user = accountUserMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        accountUserMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        accountUserMapper.deleteById(id);
    }

    @Override
    public PageResult<OperationLogVO> pageLogs(OperationLogQueryDTO query) {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getModuleName())) {
            wrapper.like("module_name", query.getModuleName());
        }
        if (StringUtils.hasText(query.getOperationType())) {
            wrapper.eq("operation_type", query.getOperationType());
        }
        if (StringUtils.hasText(query.getResultStatus())) {
            wrapper.eq("result_status", query.getResultStatus());
        }
        wrapper.orderByDesc("created_at");

        List<OperationLog> allLogs = operationLogMapper.selectList(wrapper);
        int total = allLogs.size();
        int start = (query.getPageNum() - 1) * query.getPageSize();
        int end = Math.min(start + query.getPageSize(), total);
        List<OperationLog> pageData = start < total ? allLogs.subList(start, end) : new ArrayList<>();

        List<OperationLogVO> records = new ArrayList<>();
        for (OperationLog log : pageData) {
            OperationLogVO vo = new OperationLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            vo.setModuleName(log.getModuleName());
            vo.setOperationType(log.getOperationType());
            vo.setOperationDesc(log.getOperationDesc());
            vo.setRequestPath(log.getRequestPath());
            vo.setRequestMethod(log.getRequestMethod());
            vo.setIpAddress(log.getIpAddress());
            vo.setResultStatus(log.getResultStatus());
            vo.setErrorMessage(log.getErrorMessage());
            vo.setCreatedAt(log.getCreatedAt());
            records.add(vo);
        }

        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), records);
    }

    @Override
    public void cleanData(String type) {
        switch (type) {
            case "jobs":
                jobInfoMapper.delete(null);
                break;
            case "users":
                accountUserMapper.delete(null);
                break;
            case "resumes":
                resumeMapper.delete(null);
                break;
            case "logs":
                operationLogMapper.delete(null);
                break;
            case "all":
                jobInfoMapper.delete(null);
                accountUserMapper.delete(null);
                resumeMapper.delete(null);
                operationLogMapper.delete(null);
                break;
            default:
                throw new RuntimeException("不支持的清理类型: " + type);
        }
    }
}
