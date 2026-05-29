package org.example.ai4novel.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/novels/{id}")
public class ChatController {

    @PostMapping("/chat")
    public Map<String, Object> chat(@PathVariable String id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");

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

        if (lastUserMsg.length() > 50) {
            lastUserMsg = lastUserMsg.substring(0, 50) + "...";
        }

        String reply = "AI 对话功能正在开发中。您说: " + lastUserMsg;

        Map<String, Object> data = new HashMap<>();
        data.put("reply", reply);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "成功");
        result.put("data", data);
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
