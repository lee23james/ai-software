package com.example.jobplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("resume_parse_result")
public class ResumeParseResult {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resumeId;
    private String parsedName;
    private String parsedEducation;
    private String parsedSchool;
    private String parsedMajor;
    private String parsedSkillsJson;
    private String parsedProjectsJson;
    private String suggestions;
    private String rawResultJson;
    private String modelName;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getParsedName() {
        return parsedName;
    }

    public void setParsedName(String parsedName) {
        this.parsedName = parsedName;
    }

    public String getParsedEducation() {
        return parsedEducation;
    }

    public void setParsedEducation(String parsedEducation) {
        this.parsedEducation = parsedEducation;
    }

    public String getParsedSchool() {
        return parsedSchool;
    }

    public void setParsedSchool(String parsedSchool) {
        this.parsedSchool = parsedSchool;
    }

    public String getParsedMajor() {
        return parsedMajor;
    }

    public void setParsedMajor(String parsedMajor) {
        this.parsedMajor = parsedMajor;
    }

    public String getParsedSkillsJson() {
        return parsedSkillsJson;
    }

    public void setParsedSkillsJson(String parsedSkillsJson) {
        this.parsedSkillsJson = parsedSkillsJson;
    }

    public String getParsedProjectsJson() {
        return parsedProjectsJson;
    }

    public void setParsedProjectsJson(String parsedProjectsJson) {
        this.parsedProjectsJson = parsedProjectsJson;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getRawResultJson() {
        return rawResultJson;
    }

    public void setRawResultJson(String rawResultJson) {
        this.rawResultJson = rawResultJson;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
