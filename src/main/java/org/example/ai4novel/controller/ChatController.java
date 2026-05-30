package org.example.ai4novel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ai4novel.entity.AiConfig;
import org.example.ai4novel.entity.TreeNode;
import org.example.ai4novel.entity.ChatConversation;
import org.example.ai4novel.entity.ChatMessage;
import org.example.ai4novel.service.AiConfigService;
import org.example.ai4novel.service.ChatService;
import org.example.ai4novel.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private static final Pattern READ_PATTERN = Pattern.compile(
            "阅读\\s*[“”\"《]?([^”\"》\\s,，。.!；;]+(?:\\.[a-zA-Z]+)?)[”\"》]?"
    );

    private final AiConfigService aiConfigService;
    private final FileService fileService;
    private final ChatService chatService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChatController(AiConfigService aiConfigService, FileService fileService, ChatService chatService) {
        this.aiConfigService = aiConfigService;
        this.fileService = fileService;
        this.chatService = chatService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ========== 对话管理 ==========

    @GetMapping("/api/novels/{novelId}/conversations")
    public Map<String, Object> listConversations(@PathVariable String novelId) {
        List<ChatConversation> list = chatService.getConversations(novelId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", list);
        return result;
    }

    @PostMapping("/api/novels/{novelId}/conversations")
    public Map<String, Object> createConversation(@PathVariable String novelId, @RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "新对话");
        ChatConversation conv = chatService.createConversation(novelId, title);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", conv);
        return result;
    }

    @DeleteMapping("/api/novels/{novelId}/conversations/{convId}")
    public Map<String, Object> deleteConversation(@PathVariable String novelId, @PathVariable String convId) {
        chatService.deleteConversation(convId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }

    @PutMapping("/api/novels/{novelId}/conversations/{convId}")
    public Map<String, Object> renameConversation(@PathVariable String novelId, @PathVariable String convId,
                                                   @RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title == null || title.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", -1);
            result.put("message", "标题不能为空");
            return result;
        }
        chatService.updateConversationTitle(convId, title.trim());
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "重命名成功");
        return result;
    }

    @GetMapping("/api/novels/{novelId}/conversations/{convId}")
    public Map<String, Object> getConversation(@PathVariable String novelId, @PathVariable String convId) {
        Map<String, Object> data = chatService.getConversationWithMessages(convId);
        Map<String, Object> result = new HashMap<>();
        if (data == null) {
            result.put("code", -1);
            result.put("message", "对话不存在");
        } else {
            result.put("code", 0);
            result.put("data", data);
        }
        return result;
    }

    // ========== 聊天 ==========

    @PostMapping("/api/novels/{id}/chat")
    public Map<String, Object> chat(@PathVariable String id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");

        // 获取或创建对话
        String conversationId = (String) body.get("conversationId");
        if (conversationId == null || conversationId.isEmpty()) {
            // 自动创建新对话，标题用第一条用户消息截取
            String title = "新对话";
            if (messages != null) {
                for (Map<String, String> msg : messages) {
                    if ("user".equals(msg.get("role"))) {
                        String content = msg.get("content");
                        if (content != null && !content.isEmpty()) {
                            title = content.length() > 30 ? content.substring(0, 30) + "..." : content;
                        }
                        break;
                    }
                }
            }
            ChatConversation conv = chatService.createConversation(id, title);
            conversationId = conv.getId();
        }

        // 保存新产生的用户消息（最后一条user消息）
        if (messages != null) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                Map<String, String> msg = messages.get(i);
                if ("user".equals(msg.get("role"))) {
                    chatService.saveMessage(conversationId, "user", msg.get("content"));
                    chatService.touchConversation(conversationId);
                    break;
                }
            }
        }

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

        // 前端显式选中的文件 + "阅读"指令解析的文件，合并拼入上下文
        @SuppressWarnings("unchecked")
        List<String> explicitPaths = (List<String>) body.get("filePaths");
        augmentContext(id, messages, explicitPaths);

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

                // 保存AI回复
                chatService.saveMessage(conversationId, "assistant", reply);
                chatService.touchConversation(conversationId);

                Map<String, Object> data = new HashMap<>();
                data.put("reply", reply);
                data.put("conversationId", conversationId);
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

    /**
     * 解析最后一条用户消息中的"阅读 文件路径"指令，读取对应文件内容拼入上下文
     */
    private void augmentContext(String novelId, List<Map<String, String>> messages, List<String> explicitPaths) {
        if (messages == null || messages.isEmpty()) return;

        // 找到最后一条 user 消息
        int lastUserIdx = -1;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).get("role"))) {
                lastUserIdx = i;
                break;
            }
        }
        if (lastUserIdx < 0) return;

        String userMsg = messages.get(lastUserIdx).get("content");
        if (userMsg == null) userMsg = "";

        // 收集文件路径：前端显式选择 + "阅读"文本指令
        List<String> filePaths = new ArrayList<>();
        if (explicitPaths != null) {
            filePaths.addAll(explicitPaths);
        }
        if (userMsg.contains("阅读")) {
            Matcher matcher = READ_PATTERN.matcher(userMsg);
            while (matcher.find()) {
                filePaths.add(matcher.group(1).trim());
            }
        }
        if (filePaths.isEmpty()) return;

        StringBuilder context = new StringBuilder();
        context.append("[项目文件内容]\n\n");

        // 获取文件树用于模糊匹配
        TreeNode tree = null;
        try {
            tree = fileService.getTree(novelId);
        } catch (Exception e) {
            log.warn("获取文件树失败，仅支持精确路径匹配: {}", e.getMessage());
        }

        for (String path : filePaths) {
            String resolved = resolvePath(tree, path);
            if (resolved == null) {
                context.append("【文件未找到：").append(path).append("】\n\n");
                continue;
            }
            try {
                Map<String, String> fileData = fileService.readFile(novelId, resolved);
                context.append("【文件：").append(resolved).append("】\n");
                context.append(fileData.get("content")).append("\n\n");
            } catch (Exception e) {
                context.append("【读取失败：").append(resolved).append(" — ").append(e.getMessage()).append("】\n\n");
            }
        }

        // 去掉原消息中的"阅读 xxx"片段，保留剩余提示词
        String remaining = READ_PATTERN.matcher(userMsg).replaceAll("").trim();
        // 去掉开头的标点/连词
        remaining = remaining.replaceFirst("^[，,。.、\\s]+", "");

        context.append("---\n用户提示：");
        if (!remaining.isEmpty()) {
            context.append(remaining);
        }

        messages.get(lastUserIdx).put("content", context.toString());
    }

    private String resolvePath(TreeNode tree, String path) {
        if (tree == null) return path;

        // 按完整路径精确查找
        if (findByPath(tree, path)) return path;

        // 按文件名模糊查找
        String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
        return findByFileName(tree, fileName);
    }

    private boolean findByPath(TreeNode node, String path) {
        if (node == null) return false;
        if (path.equals(node.getPath())) return true;
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                if (findByPath(child, path)) return true;
            }
        }
        return false;
    }

    private String findByFileName(TreeNode node, String fileName) {
        if (node == null) return null;
        if (fileName.equals(node.getName()) && "file".equals(node.getType())) {
            return node.getPath();
        }
        if (node.getChildren() != null) {
            for (TreeNode child : node.getChildren()) {
                String found = findByFileName(child, fileName);
                if (found != null) return found;
            }
        }
        return null;
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", -1);
        result.put("message", e.getMessage());
        return result;
    }
}
