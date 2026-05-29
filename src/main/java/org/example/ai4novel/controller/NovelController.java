package org.example.ai4novel.controller;

import org.example.ai4novel.entity.Novel;
import org.example.ai4novel.service.NovelService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/novels")
public class NovelController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @PostMapping
    public Map<String, Object> createNovel(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.getOrDefault("description", "");

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("小说名称不能为空");
        }

        Novel novel = novelService.createNovel(name.trim(), description);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "创建成功");
        result.put("data", novel);
        return result;
    }

    @GetMapping
    public Map<String, Object> listNovels() {
        List<Novel> novels = novelService.listNovels();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "查询成功");
        result.put("data", novels);
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getNovel(@PathVariable String id) {
        Novel novel = novelService.getNovel(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "查询成功");
        result.put("data", novel);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNovel(@PathVariable String id) {
        novelService.deleteNovel(id);
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
