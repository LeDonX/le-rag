package com.le.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Data
public class AliOssProperties {

    /**
     * 存储类型，当前先固定 local
     */
    private String type;

    /**
     * 上传目录
     */
    private String baseDir;

    /**
     * 下载导出目录
     */
    private String downloadDir;
}