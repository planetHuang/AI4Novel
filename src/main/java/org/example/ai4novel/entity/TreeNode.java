package org.example.ai4novel.entity;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    private String name;
    private String type;
    private String path;
    private List<TreeNode> children;

    public TreeNode() {}

    public TreeNode(String name, String type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
        if ("directory".equals(type)) {
            this.children = new ArrayList<>();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }
}
