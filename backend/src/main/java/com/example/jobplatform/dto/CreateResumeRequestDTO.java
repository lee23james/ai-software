package com.example.jobplatform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class CreateResumeRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "简历名称不能为空")
    private String resumeName;

    private String parsedText;
    private String targetJobName;
    private Integer targetSalaryMin;
    private Integer targetSalaryMax;
    private String targetCity;
    private String education;
    private BigDecimal workYears;

    @Valid
    private List<ResumeSkillInputDTO> skills;

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

    public List<ResumeSkillInputDTO> getSkills() {
        return skills;
    }

    public void setSkills(List<ResumeSkillInputDTO> skills) {
        this.skills = skills;
    }
}
