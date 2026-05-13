package com.example.jobplatform.service;

public interface OperationLogService {

    /**
     * 记录操作日志
     */
    void log(Long userId, String moduleName, String operationType, String operationDesc,
             String requestPath, String requestMethod, String requestParams,
             String ipAddress, String resultStatus, String errorMessage);

    /**
     * 简化的日志记录（常用）
     */
    void log(Long userId, String moduleName, String operationType, String operationDesc);

    /**
     * 记录登录日志
     */
    void logLogin(Long userId, String username, boolean success, String ipAddress, String errorMessage);

    /**
     * 记录岗位操作日志
     */
    void logJob(Long userId, String operationType, String jobName, Long jobId, boolean success);

    /**
     * 记录简历操作日志
     */
    void logResume(Long userId, String operationType, String resumeName, Long resumeId, boolean success);
}
