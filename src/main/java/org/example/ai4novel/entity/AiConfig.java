package org.example.ai4novel.entity;

import java.time.LocalDateTime;

public class AiConfig {

    private String id;
    private String name;
    private String apiUrl;
    private String apiKey;
    private String model;
    private Boolean thinkingEnabled;
    private String reasoningEffort;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public AiConfig() {}

    public AiConfig(String id, String name, String apiUrl, String apiKey, String model,
                    Boolean thinkingEnabled, String reasoningEffort,
                    Boolean isDefault, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.thinkingEnabled = thinkingEnabled;
        this.reasoningEffort = reasoningEffort;
        this.isDefault = isDefault;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
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

    public Boolean getThinkingEnabled() {
        return thinkingEnabled;
    }

    public void setThinkingEnabled(Boolean thinkingEnabled) {
        this.thinkingEnabled = thinkingEnabled;
    }

    public String getReasoningEffort() {
        return reasoningEffort;
    }

    public void setReasoningEffort(String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}