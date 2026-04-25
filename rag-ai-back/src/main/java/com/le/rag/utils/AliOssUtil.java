package com.le.rag.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {
    /**
     * 本地上传目录
     */
    private String baseDir;
    /**
     * 本地下载导出目录
     */
    private String downloadDir;

    /**
     * 上传文件到本地目录
     *
     * @param bytes      文件字节
     * @param objectName 文件名（建议 UUID + 后缀）
     * @return 相对存储路径，例如：xxx.pdf
     */
    public String upload(byte[] bytes, String objectName) {
        try {
            Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
            Files.createDirectories(basePath);
            String safeName = objectName.replace("\\", "/").replace("..", "");
            Path target = basePath.resolve(safeName).normalize();
            if (!target.startsWith(basePath)) {
                throw new IllegalArgumentException("非法文件路径: " + objectName);
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, bytes);
            String relativePath = basePath.relativize(target).toString().replace("\\", "/");
            log.info("文件保存到本地: {}", target);
            return relativePath;
        } catch (IOException e) {
            log.error("本地文件保存失败", e);
            throw new RuntimeException("本地文件保存失败", e);
        }
    }

    /**
     * 删除本地文件
     *
     * @param storedPath 数据库存储的相对路径
     * @return 是否删除成功
     */
    public boolean deleteOss(String storedPath) {
        try {
            Path target = resolveAbsolutePath(storedPath);
            boolean deleted = Files.deleteIfExists(target);
            log.info("删除本地文件: {}, result={}", target, deleted);
            return deleted;
        } catch (IOException e) {
            log.error("删除本地文件失败: {}", storedPath, e);
            throw new RuntimeException("删除本地文件失败", e);
        }
    }

    /**
     * 下载文件：复制到 downloadDir
     *
     * @param storedPath 数据库存储的相对路径
     */
    public void download(String storedPath) {
        try {
            Path source = resolveAbsolutePath(storedPath);
            Path downloadPath = Paths.get(downloadDir).toAbsolutePath().normalize();
            Files.createDirectories(downloadPath);
            Path target = downloadPath.resolve(Objects.requireNonNull(source.getFileName())).normalize();
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件已导出到本地下载目录: {}", target);
        } catch (IOException e) {
            log.error("下载本地文件失败: {}", storedPath, e);
            throw new RuntimeException("下载本地文件失败", e);
        }
    }

    /**
     * 把相对路径转为绝对路径
     */
    public Path resolveAbsolutePath(String storedPath) {
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path resolved = basePath.resolve(storedPath).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("非法文件路径: " + storedPath);
        }
        return resolved;
    }

    /**
     * 供 TikaDocumentReader 使用
     */
    public Resource loadAsResource(String storedPath) {
        return new FileSystemResource(resolveAbsolutePath(storedPath));
    }
}