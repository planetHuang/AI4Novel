package org.example.ai4novel.controller;

import org.example.ai4novel.entity.TreeNode;
import org.example.ai4novel.service.FileService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/novels/{id}")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/tree")
    public Map<String, Object> getTree(@PathVariable String id) {
        TreeNode tree = fileService.getTree(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "查询成功");
        result.put("data", tree);
        return result;
    }

    @GetMapping("/file")
    public Map<String, Object> readFile(@PathVariable String id, @RequestParam String path) {
        Map<String, String> fileData = fileService.readFile(id, path);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "读取成功");
        result.put("data", fileData);
        return result;
    }

    @PutMapping("/file")
    public Map<String, Object> writeFile(@PathVariable String id, @RequestBody Map<String, String> body) {
        String path = body.get("path");
        String content = body.get("content");
        if (path == null || content == null) {
            throw new RuntimeException("path 和 content 不能为空");
        }
        fileService.writeFile(id, path, content);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "保存成功");
        return result;
    }

    @PostMapping("/resource")
    public Map<String, Object> createResource(@PathVariable String id, @RequestBody Map<String, String> body) {
        String parentPath = body.getOrDefault("parentPath", "");
        String name = body.get("name");
        String type = body.get("type");

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("名称不能为空");
        }
        if (!"file".equals(type) && !"directory".equals(type)) {
            throw new RuntimeException("类型必须是 file 或 directory");
        }

        fileService.createResource(id, parentPath, name.trim(), type);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "创建成功");
        return result;
    }

    @DeleteMapping("/resource")
    public Map<String, Object> deleteResource(@PathVariable String id, @RequestBody Map<String, String> body) {
        String path = body.get("path");
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("path 不能为空");
        }
        fileService.deleteResource(id, path);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "删除成功");
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
