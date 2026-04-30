package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class ResumeSkillInputDTO {

    @NotBlank(message = "技能名不能为空")
    private String skillName;
    private Integer skillLevel;
    private BigDecimal yearsOfExperience;

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public BigDecimal getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(BigDecimal yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
