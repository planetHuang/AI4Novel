package org.example.ai4novel.controller;

import org.example.ai4novel.entity.AiConfig;
import org.example.ai4novel.service.AiConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-configs")
public class AiConfigController {

    private final AiConfigService aiConfigService;

    public AiConfigController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @GetMapping
    public Map<String, Object> getAllConfigs() {
        List<AiConfig> configs = aiConfigService.getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "成功");
        result.put("data", configs);
        return result;
    }

    @PostMapping
    public Map<String, Object> createConfig(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String apiUrl = body.get("apiUrl");
        String apiKey = body.get("apiKey");
        String model = body.getOrDefault("model", "");
        Boolean thinkingEnabled = Boolean.parseBoolean(body.getOrDefault("thinkingEnabled", "false"));
        String reasoningEffort = body.getOrDefault("reasoningEffort", "");

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("配置名称不能为空");
        }
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new RuntimeException("API URL不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API KEY不能为空");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new RuntimeException("模型名称不能为空");
        }

        AiConfig config = aiConfigService.createConfig(name.trim(), apiUrl.trim(), apiKey.trim(),
                model.trim(), thinkingEnabled, reasoningEffort.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "创建成功");
        result.put("data", config);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getConfigById(@PathVariable String id) {
        AiConfig config = aiConfigService.getConfigById(id);
        if (config == null) {
            throw new RuntimeException("配置不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "成功");
        result.put("data", config);
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateConfig(@PathVariable String id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String apiUrl = body.get("apiUrl");
        String apiKey = body.get("apiKey");
        String model = body.getOrDefault("model", "");
        Boolean thinkingEnabled = Boolean.parseBoolean(body.getOrDefault("thinkingEnabled", "false"));
        String reasoningEffort = body.getOrDefault("reasoningEffort", "");

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("配置名称不能为空");
        }
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new RuntimeException("API URL不能为空");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API KEY不能为空");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new RuntimeException("模型名称不能为空");
        }

        AiConfig config = aiConfigService.updateConfig(id, name.trim(), apiUrl.trim(), apiKey.trim(),
                model.trim(), thinkingEnabled, reasoningEffort.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "更新成功");
        result.put("data", config);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteConfig(@PathVariable String id) {
        aiConfigService.deleteConfig(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }

    @PutMapping("/{id}/set-default")
    public Map<String, Object> setDefaultConfig(@PathVariable String id) {
        aiConfigService.setDefaultConfig(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "设置成功");
        return result;
    }

    @GetMapping("/default")
    public Map<String, Object> getDefaultConfig() {
        AiConfig config = aiConfigService.getDefaultConfig();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "成功");
        result.put("data", config);
        return result;
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", -1);
        result.put("message", e.getMessage());
        return result;
    }
}