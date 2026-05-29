package org.example.ai4novel.service;

import org.example.ai4novel.entity.Novel;
import org.example.ai4novel.mapper.NovelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NovelService {

    private static final Logger log = LoggerFactory.getLogger(NovelService.class);

    private static final String[] SUB_FOLDERS = {
            "人物设定",
            "剧情走向设定",
            "世界观设定",
            "伏笔",
            "章节"
    };

    @Value("${novel.storage.base-path:./novels}")
    private String basePath;

    private final NovelMapper novelMapper;

    public NovelService(NovelMapper novelMapper) {
        this.novelMapper = novelMapper;
    }

    @PostConstruct
    public void init() {
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        log.info("小说存储根目录: {}", baseDir.getAbsolutePath());
    }

    public Novel createNovel(String name, String description) {
        String id = UUID.randomUUID().toString().replace("-", "");
        String safeName = sanitizeFolderName(name);
        File novelDir = new File(basePath, safeName);

        if (novelDir.exists()) {
            throw new RuntimeException("小说文件夹已存在: " + name);
        }

        novelDir.mkdirs();
        for (String subFolder : SUB_FOLDERS) {
            File subDir = new File(novelDir, subFolder);
            subDir.mkdirs();
        }

        Novel novel = new Novel(id, name, description, novelDir.getAbsolutePath(), LocalDateTime.now());
        novelMapper.insert(novel);
        log.info("小说创建成功: {} -> {}", name, novelDir.getAbsolutePath());
        return novel;
    }

    public List<Novel> listNovels() {
        return novelMapper.findAll();
    }

    public Novel getNovel(String id) {
        Novel novel = novelMapper.findById(id);
        if (novel == null) {
            throw new RuntimeException("小说不存在: " + id);
        }
        return novel;
    }

    public void deleteNovel(String id) {
        Novel novel = novelMapper.findById(id);
        if (novel == null) {
            throw new RuntimeException("小说不存在: " + id);
        }
        deleteFolder(new File(novel.getFolderPath()));
        novelMapper.deleteById(id);
        log.info("小说删除成功: {}", novel.getName());
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }

    private String sanitizeFolderName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
