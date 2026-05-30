package org.example.ai4novel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ai4novel.entity.AiConfig;
import org.example.ai4novel.service.AiConfigService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final AiConfigService aiConfigService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/api/novels/{id}/chat")
    public Map<String, Object> chat(@PathVariable String id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");

        // 获取AI配置：优先使用前端指定的配置，否则使用默认配置
        String aiConfigId = (String) body.get("aiConfigId");
        AiConfig aiConfig;
        if (aiConfigId != null && !aiConfigId.isEmpty()) {
            aiConfig = aiConfigService.getConfigById(aiConfigId);
            if (aiConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", -1);
                result.put("message", "指定的AI配置不存在");
                return result;
            }
        } else {
            aiConfig = aiConfigService.getDefaultConfig();
        }

        if (aiConfig == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", -1);
            result.put("message", "未配置AI，请先在设置页面添加AI配置");
            return result;
        }

        String lastUserMsg = "";
        if (messages != null) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                Map<String, String> msg = messages.get(i);
                if ("user".equals(msg.get("role"))) {
                    lastUserMsg = msg.get("content");
                    break;
                }
            }
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", messages);

            // DeepSeek thinking 模式
            if (aiConfig.getThinkingEnabled() != null && aiConfig.getThinkingEnabled()) {
                Map<String, Object> thinking = new HashMap<>();
                thinking.put("type", "enabled");
                requestBody.put("thinking", thinking);

                if (aiConfig.getReasoningEffort() != null && !aiConfig.getReasoningEffort().isEmpty()) {
                    requestBody.put("reasoning_effort", aiConfig.getReasoningEffort());
                }
            }

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 调用AI服务
            ResponseEntity<String> response = restTemplate.exchange(
                    aiConfig.getApiUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 解析响应
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode messageNode = root.path("choices").get(0).path("message");

                String reply = messageNode.path("content").asText();
                String reasoning = messageNode.path("reasoning_content").asText(null);

                Map<String, Object> data = new HashMap<>();
                data.put("reply", reply);
                if (reasoning != null && !reasoning.isEmpty()) {
                    data.put("reasoning", reasoning);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("code", 0);
                result.put("message", "成功");
                result.put("data", data);
                return result;
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("code", -1);
                result.put("message", "AI服务请求失败");
                return result;
            }
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<>();
            data.put("reply", "AI 对话功能发生错误: " + e.getMessage() + "\n您说: " + lastUserMsg);

            Map<String, Object> result = new HashMap<>();
            result.put("code", -1);
            result.put("message", e.getMessage());
            result.put("data", data);
            return result;
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", -1);
        result.put("message", e.getMessage());
        return result;
    }
}
