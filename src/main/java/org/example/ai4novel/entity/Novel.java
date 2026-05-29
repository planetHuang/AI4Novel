package org.example.ai4novel.entity;

import java.time.LocalDateTime;

public class Novel {

    private String id;
    private String name;
    private String description;
    private String folderPath;
    private LocalDateTime createTime;

    public Novel() {}

    public Novel(String id, String name, String description, String folderPath, LocalDateTime createTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.folderPath = folderPath;
        this.createTime = createTime;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
