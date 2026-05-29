package org.example.ai4novel.service;

import org.example.ai4novel.entity.AiConfig;
import org.example.ai4novel.mapper.AiConfigMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AiConfigService {

    private final AiConfigMapper aiConfigMapper;

    public AiConfigService(AiConfigMapper aiConfigMapper) {
        this.aiConfigMapper = aiConfigMapper;
    }

    public List<AiConfig> getAllConfigs() {
        return aiConfigMapper.findAll();
    }

    public AiConfig getConfigById(String id) {
        return aiConfigMapper.findById(id);
    }

    public AiConfig getDefaultConfig() {
        return aiConfigMapper.findDefault();
    }

    public AiConfig createConfig(String name, String apiUrl, String apiKey,
                                   String model, Boolean thinkingEnabled, String reasoningEffort) {
        String id = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        // 检查是否是第一条记录，如果是则设为默认
        List<AiConfig> existing = aiConfigMapper.findAll();
        Boolean isDefault = existing.isEmpty();

        AiConfig config = new AiConfig(id, name, apiUrl, apiKey, model,
                thinkingEnabled, reasoningEffort, isDefault, now, now);
        aiConfigMapper.insert(config);
        return config;
    }

    public AiConfig updateConfig(String id, String name, String apiUrl, String apiKey,
                                   String model, Boolean thinkingEnabled, String reasoningEffort) {
        AiConfig config = aiConfigMapper.findById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        config.setName(name);
        config.setApiUrl(apiUrl);
        config.setApiKey(apiKey);
        config.setModel(model);
        config.setThinkingEnabled(thinkingEnabled);
        config.setReasoningEffort(reasoningEffort);
        config.setUpdateTime(LocalDateTime.now());

        aiConfigMapper.update(config);
        return config;
    }

    public void setDefaultConfig(String id) {
        AiConfig config = aiConfigMapper.findById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        // 先清除所有默认标记
        aiConfigMapper.clearAllDefault();
        // 设置新的默认配置
        aiConfigMapper.setDefault(id, LocalDateTime.now());
    }

    public void deleteConfig(String id) {
        AiConfig config = aiConfigMapper.findById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }

        if (config.getIsDefault()) {
            throw new RuntimeException("不能删除默认配置，请先设置其他配置为默认");
        }

        aiConfigMapper.deleteById(id);
    }
}