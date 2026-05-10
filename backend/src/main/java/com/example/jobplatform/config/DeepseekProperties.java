package com.example.jobplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "deepseek")
public class DeepseekProperties {

    /**
     * OpenAI-compatible API base URL, without trailing slash.
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * API key; prefer env DEEPSEEK_API_KEY.
     */
    private String apiKey = "";

    private String model = "deepseek-chat";

    private Duration connectTimeout = Duration.ofSeconds(10);

    private Duration readTimeout = Duration.ofSeconds(120);

    /**
     * Max characters of job_description per job in the LLM context (truncated).
     */
    private int maxJobDescriptionChars = 500;

    /**
     * Max characters of resume text in context (truncated if longer).
     */
    private int maxResumeTextChars = 12000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxJobDescriptionChars() {
        return maxJobDescriptionChars;
    }

    public void setMaxJobDescriptionChars(int maxJobDescriptionChars) {
        this.maxJobDescriptionChars = maxJobDescriptionChars;
    }

    public int getMaxResumeTextChars() {
        return maxResumeTextChars;
    }

    public void setMaxResumeTextChars(int maxResumeTextChars) {
        this.maxResumeTextChars = maxResumeTextChars;
    }
}
