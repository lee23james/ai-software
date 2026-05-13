package com.example.jobplatform.controller;

import com.example.jobplatform.common.ApiResponse;
import com.example.jobplatform.common.PageResult;
import com.example.jobplatform.dto.AdminJobQueryDTO;
import com.example.jobplatform.dto.AdminUserQueryDTO;
import com.example.jobplatform.dto.OperationLogQueryDTO;
import com.example.jobplatform.entity.AccountUser;
import com.example.jobplatform.entity.JobInfo;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.service.OperationLogService;
import com.example.jobplatform.vo.DashboardStatsVO;
import com.example.jobplatform.vo.JobDetailVO;
import com.example.jobplatform.vo.OperationLogVO;
import com.example.jobplatform.vo.UserVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final JobService jobService;
    private final OperationLogService operationLogService;

    public AdminController(AdminService adminService, JobService jobService, OperationLogService operationLogService) {
        this.adminService = adminService;
        this.jobService = jobService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsVO> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    @GetMapping("/job-list")
    public ApiResponse<PageResult<JobDetailVO>> jobList(AdminJobQueryDTO query) {
        return ApiResponse.ok(jobService.pageJobsForAdmin(query));
    }

    @GetMapping("/job/{id}")
    public ApiResponse<JobDetailVO> getJob(@PathVariable Long id) {
        JobDetailVO detail = jobService.getJobDetail(id);
        if (detail == null) {
            return ApiResponse.fail(404, "岗位不存在");
        }
        return ApiResponse.ok(detail);
    }

    @PostMapping("/job")
    public ApiResponse<Long> addJob(@RequestBody JobInfo job) {
        Long id = jobService.addJob(job);
        operationLogService.logJob(null, "新增", job.getJobName(), id, true);
        return ApiResponse.ok(id);
    }

    @PutMapping("/job/{id}")
    public ApiResponse<Void> updateJob(@PathVariable Long id, @RequestBody JobInfo job) {
        jobService.updateJob(id, job);
        operationLogService.logJob(null, "修改", job.getJobName(), id, true);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/job/{id}")
    public ApiResponse<Void> deleteJob(@PathVariable Long id) {
        JobDetailVO detail = jobService.getJobDetail(id);
        String jobName = detail != null ? detail.jobName() : String.valueOf(id);
        jobService.deleteJob(id);
        operationLogService.logJob(null, "删除", jobName, id, true);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/job/{id}/status")
    public ApiResponse<Void> toggleJobStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.fail(400, "状态值必须为 0 或 1");
        }
        JobDetailVO detail = jobService.getJobDetail(id);
        String jobName = detail != null ? detail.jobName() : String.valueOf(id);
        jobService.toggleJobStatus(id, status);
        String operationType = status == 1 ? "上线" : "下线";
        operationLogService.logJob(null, operationType, jobName, id, true);
        return ApiResponse.ok(null);
    }

    @PostMapping("/job/import")
    public ApiResponse<Integer> importJobs(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            return ApiResponse.fail(400, "仅支持 CSV 文件");
        }
        int count = jobService.importFromCsv(file);
        operationLogService.log(null, "岗位管理", "批量导入", "批量导入 " + count + " 条岗位数据");
        return ApiResponse.ok(count);
    }

    @GetMapping("/user-list")
    public ApiResponse<PageResult<UserVO>> userList(AdminUserQueryDTO query) {
        return ApiResponse.ok(adminService.pageUsers(query));
    }

    @PatchMapping("/user/{id}/status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return ApiResponse.fail(400, "状态值必须为 0 或 1");
        }
        adminService.updateUserStatus(id, status);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/log-list")
    public ApiResponse<PageResult<OperationLogVO>> logList(OperationLogQueryDTO query) {
        return ApiResponse.ok(adminService.pageLogs(query));
    }

    @PostMapping("/clean-data")
    public ApiResponse<Void> cleanData(@RequestBody Map<String, String> body) {
        String type = body.get("type");
        if (type == null || type.isEmpty()) {
            return ApiResponse.fail(400, "清理类型不能为空");
        }
        adminService.cleanData(type);
        return ApiResponse.ok(null);
    }
}
