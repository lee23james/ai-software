package com.example.jobplatform.service.impl;

import com.example.jobplatform.entity.OperationLog;
import com.example.jobplatform.mapper.OperationLogMapper;
import com.example.jobplatform.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void log(Long userId, String moduleName, String operationType, String operationDesc,
                    String requestPath, String requestMethod, String requestParams,
                    String ipAddress, String resultStatus, String errorMessage) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setModuleName(moduleName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setRequestPath(requestPath);
        log.setRequestMethod(requestMethod);
        log.setRequestParams(truncate(requestParams, 2000));
        log.setIpAddress(ipAddress);
        log.setResultStatus(resultStatus);
        log.setErrorMessage(truncate(errorMessage, 1000));
        log.setCreatedAt(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Override
    public void log(Long userId, String moduleName, String operationType, String operationDesc) {
        log(userId, moduleName, operationType, operationDesc, null, null, null, null, "success", null);
    }

    @Override
    public void logLogin(Long userId, String username, boolean success, String ipAddress, String errorMessage) {
        String resultStatus = success ? "success" : "fail";
        String desc = success ? "用户 " + username + " 登录成功" : "用户 " + username + " 登录失败";
        log(userId, "用户登录", "登录", desc, "/api/auth/login", "POST", null, ipAddress, resultStatus, errorMessage);
    }

    @Override
    public void logJob(Long userId, String operationType, String jobName, Long jobId, boolean success) {
        String resultStatus = success ? "success" : "fail";
        String moduleName = "岗位管理";
        String desc = operationType + "岗位: " + jobName;
        if (jobId != null) {
            desc += " (ID: " + jobId + ")";
        }
        log(userId, moduleName, operationType, desc, "/api/admin/job", operationType.equals("删除") ? "DELETE" : "POST", null, null, resultStatus, null);
    }

    @Override
    public void logResume(Long userId, String operationType, String resumeName, Long resumeId, boolean success) {
        String resultStatus = success ? "success" : "fail";
        String moduleName = "简历管理";
        String desc = operationType + "简历: " + resumeName;
        if (resumeId != null) {
            desc += " (ID: " + resumeId + ")";
        }
        log(userId, moduleName, operationType, desc, "/api/resume/upload", "POST", null, null, resultStatus, null);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
