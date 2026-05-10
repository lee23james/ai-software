package com.example.jobplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("resume_info")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String resumeName;
    @TableField(exist = false)
    private String sourceType;
    private String fileUrl;
    private String fileType;
    @TableField("resume_text")
    private String parsedText;
    @TableField("target_position")
    private String targetJobName;
    @TableField(exist = false)
    private Integer targetSalaryMin;
    @TableField(exist = false)
    private Integer targetSalaryMax;
    @TableField(exist = false)
    private String targetCity;
    @TableField(exist = false)
    private String education;
    @TableField(exist = false)
    private BigDecimal workYears;
    private Integer isDefault;
    @TableField("parse_status")
    private Integer status;
    @TableField("job_selection_advice")
    private String jobSelectionAdvice;
    @TableField("job_selection_advice_model")
    private String jobSelectionAdviceModel;
    @TableField("interest_resume_advice")
    private String interestResumeAdvice;
    @TableField("interest_resume_advice_model")
    private String interestResumeAdviceModel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getResumeName() {
        return resumeName;
    }

    public void setResumeName(String resumeName) {
        this.resumeName = resumeName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getParsedText() {
        return parsedText;
    }

    public void setParsedText(String parsedText) {
        this.parsedText = parsedText;
    }

    public String getTargetJobName() {
        return targetJobName;
    }

    public void setTargetJobName(String targetJobName) {
        this.targetJobName = targetJobName;
    }

    public Integer getTargetSalaryMin() {
        return targetSalaryMin;
    }

    public void setTargetSalaryMin(Integer targetSalaryMin) {
        this.targetSalaryMin = targetSalaryMin;
    }

    public Integer getTargetSalaryMax() {
        return targetSalaryMax;
    }

    public void setTargetSalaryMax(Integer targetSalaryMax) {
        this.targetSalaryMax = targetSalaryMax;
    }

    public String getTargetCity() {
        return targetCity;
    }

    public void setTargetCity(String targetCity) {
        this.targetCity = targetCity;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public BigDecimal getWorkYears() {
        return workYears;
    }

    public void setWorkYears(BigDecimal workYears) {
        this.workYears = workYears;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getJobSelectionAdvice() {
        return jobSelectionAdvice;
    }

    public void setJobSelectionAdvice(String jobSelectionAdvice) {
        this.jobSelectionAdvice = jobSelectionAdvice;
    }

    public String getJobSelectionAdviceModel() {
        return jobSelectionAdviceModel;
    }

    public void setJobSelectionAdviceModel(String jobSelectionAdviceModel) {
        this.jobSelectionAdviceModel = jobSelectionAdviceModel;
    }

    public String getInterestResumeAdvice() {
        return interestResumeAdvice;
    }

    public void setInterestResumeAdvice(String interestResumeAdvice) {
        this.interestResumeAdvice = interestResumeAdvice;
    }

    public String getInterestResumeAdviceModel() {
        return interestResumeAdviceModel;
    }

    public void setInterestResumeAdviceModel(String interestResumeAdviceModel) {
        this.interestResumeAdviceModel = interestResumeAdviceModel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
