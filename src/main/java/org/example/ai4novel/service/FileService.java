package org.example.ai4novel.service;

import org.example.ai4novel.entity.Novel;
import org.example.ai4novel.entity.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final NovelService novelService;

    public FileService(NovelService novelService) {
        this.novelService = novelService;
    }

    public TreeNode getTree(String novelId) {
        Novel novel = novelService.getNovel(novelId);
        return buildTree(novel.getFolderPath(), "");
    }

    public Map<String, String> readFile(String novelId, String relativePath) {
        Novel novel = novelService.getNovel(novelId);
        File file = resolvePath(novel.getFolderPath(), relativePath);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("文件不存在: " + relativePath);
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            Map<String, String> result = new HashMap<>();
            result.put("path", relativePath);
            result.put("content", content);
            result.put("fileName", file.getName());
            return result;
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    public void writeFile(String novelId, String relativePath, String content) {
        Novel novel = novelService.getNovel(novelId);
        File file = resolvePath(novel.getFolderPath(), relativePath);

        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            log.info("文件保存成功: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败: " + e.getMessage());
        }
    }

    public void createResource(String novelId, String parentPath, String name, String type) {
        Novel novel = novelService.getNovel(novelId);
        File parentDir = resolvePath(novel.getFolderPath(), parentPath);

        if (!parentDir.isDirectory()) {
            throw new RuntimeException("父路径不是目录: " + parentPath);
        }

        File resource = new File(parentDir, name);
        if (resource.exists()) {
            throw new RuntimeException("资源已存在: " + name);
        }

        if ("directory".equals(type)) {
            resource.mkdirs();
        } else {
            try {
                resource.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("创建文件失败: " + e.getMessage());
            }
        }
        log.info("创建资源成功: {}", resource.getAbsolutePath());
    }

    public void deleteResource(String novelId, String relativePath) {
        Novel novel = novelService.getNovel(novelId);
        File resource = resolvePath(novel.getFolderPath(), relativePath);

        if (!resource.exists()) {
            throw new RuntimeException("资源不存在: " + relativePath);
        }

        deleteRecursive(resource);
        log.info("删除资源成功: {}", resource.getAbsolutePath());
    }

    private TreeNode buildTree(String rootPath, String relativePath) {
        File dir = new File(rootPath, relativePath);
        String name = relativePath.isEmpty() ? new File(rootPath).getName() : dir.getName();
        String path = relativePath.isEmpty() ? "" : relativePath;

        TreeNode node = new TreeNode(name, "directory", path);

        File[] files = dir.listFiles();
        if (files == null) {
            return node;
        }

        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        fileList.sort((a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        for (File file : fileList) {
            String childRelPath = relativePath.isEmpty() ? file.getName() : relativePath + "/" + file.getName();
            if (file.isDirectory()) {
                node.getChildren().add(buildTree(rootPath, childRelPath));
            } else {
                node.getChildren().add(new TreeNode(file.getName(), "file", childRelPath));
            }
        }

        return node;
    }

    private File resolvePath(String rootPath, String relativePath) {
        try {
            File root = new File(rootPath).getCanonicalFile();
            File target = new File(root, relativePath).getCanonicalFile();
            if (!target.toPath().startsWith(root.toPath())) {
                throw new RuntimeException("不允许访问小说目录外的文件");
            }
            return target;
        } catch (IOException e) {
            throw new RuntimeException("路径解析失败: " + e.getMessage());
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
