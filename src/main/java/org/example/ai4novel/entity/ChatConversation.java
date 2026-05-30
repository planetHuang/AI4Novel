package org.example.ai4novel.entity;

import java.time.LocalDateTime;

public class ChatConversation {

    private String id;
    private String novelId;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public ChatConversation() {}

    public ChatConversation(String id, String novelId, String title, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.novelId = novelId;
        this.title = title;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNovelId() { return novelId; }
    public void setNovelId(String novelId) { this.novelId = novelId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
