package com.example.jobplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("job_match_record")
public class JobMatchResult {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long resumeId;
    private Long jobId;
    @TableField("match_score")
    private BigDecimal totalScore;
    private BigDecimal skillScore;
    private BigDecimal experienceScore;
    private BigDecimal educationScore;
    private BigDecimal cityScore;
    private BigDecimal salaryScore;
    @TableField("raw_result_json")
    private String reasonJson;
    @TableField("model_name")
    private String algorithmVersion;
    @TableField(exist = false)
    private LocalDateTime matchedAt;
    private LocalDateTime createdAt;

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

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public BigDecimal getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(BigDecimal skillScore) {
        this.skillScore = skillScore;
    }

    public BigDecimal getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(BigDecimal experienceScore) {
        this.experienceScore = experienceScore;
    }

    public BigDecimal getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(BigDecimal educationScore) {
        this.educationScore = educationScore;
    }

    public BigDecimal getCityScore() {
        return cityScore;
    }

    public void setCityScore(BigDecimal cityScore) {
        this.cityScore = cityScore;
    }

    public BigDecimal getSalaryScore() {
        return salaryScore;
    }

    public void setSalaryScore(BigDecimal salaryScore) {
        this.salaryScore = salaryScore;
    }

    public String getReasonJson() {
        return reasonJson;
    }

    public void setReasonJson(String reasonJson) {
        this.reasonJson = reasonJson;
    }

    public String getAlgorithmVersion() {
        return algorithmVersion;
    }

    public void setAlgorithmVersion(String algorithmVersion) {
        this.algorithmVersion = algorithmVersion;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }

    public void setMatchedAt(LocalDateTime matchedAt) {
        this.matchedAt = matchedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
