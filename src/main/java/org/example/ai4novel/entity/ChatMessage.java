package org.example.ai4novel.entity;

import java.time.LocalDateTime;

public class ChatMessage {

    private Long id;
    private String conversationId;
    private String role;
    private String content;
    private LocalDateTime createTime;

    public ChatMessage() {}

    public ChatMessage(String conversationId, String role, String content, LocalDateTime createTime) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createTime = createTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
