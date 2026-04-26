package com.le.rag.controller;

/**
 * @Title: KnowledgeController
 * @Author LeDon
 * @Package com.le.rag.controller
 * @description: 知识库
 */

import com.alibaba.fastjson2.JSON;
import com.le.rag.common.ApplicationConstant;
import com.le.rag.common.BaseResponse;
import com.le.rag.common.ErrorCode;
import com.le.rag.common.ResultUtils;
import com.le.rag.entity.AliOssFile;
import com.le.rag.pojo.dto.QueryFileDTO;
import com.le.rag.service.AliOssFileService;
import com.le.rag.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "KnowledgeController", description = "知识库管理接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/knowledge")
public class KnowledgeController {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private TokenTextSplitter tokenTextSplitter;

    @Autowired
    private AliOssFileService aliOssFileService;
    
    /**
     * 上传附件接口
     *
     *  1. 提供不同的分片策略
     *  2. 分片后的预览
     *
     *  text-to-sql:
     *  上传资料--->（用户选择是否聚合）----> tosql---->创建表、插入数据
     * @param
     * @return
     * @throws IOException
     */

    @Operation(summary = "upload", description = "上传附件接口")
    @PostMapping(value = "file/upload", headers = "content-type=multipart/form-data")
    public BaseResponse upload(@RequestParam("file") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || !originalFilename.contains(".")) {
                    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件名非法");
                }
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String objectName = UUID.randomUUID() + extension;
                // 1. 保存本地文件
                String storagePath = aliOssUtil.upload(file.getBytes(), objectName);
                // 2. 从本地文件读取，交给 Tika
                Resource resource = aliOssUtil.loadAsResource(storagePath);
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                List<Document> documents = reader.read();
                // 3. 补齐 metadata
                documents.forEach(document -> {
                    document.getMetadata().put("source", originalFilename);
                    document.getMetadata().put("storagePath", storagePath);
                    document.getMetadata().put("fileId", objectName);
                });
                // 4. 切片
                List<Document> splitDocuments = tokenTextSplitter.apply(documents);
                // 5. 再次确保切片 metadata 存在
                splitDocuments.forEach(document -> {
                    document.getMetadata().put("source", originalFilename);
                    document.getMetadata().put("storagePath", storagePath);
                    document.getMetadata().put("fileId", objectName);
                });
                // 6. 入向量库
                vectorStore.add(splitDocuments);
                long currMillis = System.currentTimeMillis();
                // 7. 保存文件记录到数据库
                aliOssFileService.save(AliOssFile.builder()
                        .fileName(originalFilename)
                        .vectorId(JSON.toJSONString(
                                splitDocuments.stream().map(Document::getId).collect(Collectors.toList())
                        ))
                        .url(storagePath)
                        .createTime(new Date(currMillis))
                        .updateTime(new Date(currMillis))
                        .build());
            } catch (IOException e) {
                log.error("上传文件失败", e);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "上传文件失败");
            } catch (Exception e) {
                log.error("上传文件失败", e);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "向量化失败");
            }
        }
        return ResultUtils.success("文件上传成功");
    }

    @Operation(summary = "contents",description = "文件查询")
    @GetMapping("/contents")
    public BaseResponse queryFiles(QueryFileDTO request){
        if(request.getPage() == null || request.getPageSize() == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR,"page 或 pageSize为空");
        }
        return aliOssFileService.queryPage(request);
    }

    @Operation(summary = "delete",description = "文件删除")
    @DeleteMapping("/delete")
    public BaseResponse deleteFiles(@RequestParam List<Long> ids){
        return aliOssFileService.deleteFiles(ids);
    }


    @Operation(summary = "download",description = "文件下载")
    @GetMapping("/download")
    public BaseResponse downloadFiles(@RequestParam List<Long> ids){
        return aliOssFileService.downloadFiles(ids);
    }


}