package com.le.rag.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.le.rag.common.BaseResponse;
import com.le.rag.common.ErrorCode;
import com.le.rag.common.ResultUtils;
import com.le.rag.entity.AliOssFile;
import com.le.rag.pojo.dto.QueryFileDTO;
import com.le.rag.service.AliOssFileService;
import com.le.rag.mapper.AliOssFileMapper;
import com.le.rag.utils.AliOssUtil;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author LeDon
* @description 针对表【ali_oss_file】的数据库操作Service实现
* @createDate 2025-02-08 20:51:33
*/
@Service
public class AliOssFileServiceImpl extends ServiceImpl<AliOssFileMapper, AliOssFile>
    implements AliOssFileService{

    @Autowired
    private AliOssFileMapper aliOssFileMapper;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private AliOssUtil aliOssUtil;


    /**
     * 查询文件
     * @param request
     * @return
     */
    @Override
    public BaseResponse queryPage(QueryFileDTO request) {
        Page<AliOssFile> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<AliOssFile> fileList = aliOssFileMapper.findByFileNameContaining(page, request.getFileName());
        return ResultUtils.success(fileList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse deleteFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择文件");
        }
        List<AliOssFile> aliOssFiles = aliOssFileMapper.selectByIds(ids);
        int count = aliOssFileMapper.deleteBatchIds(ids);
        if (count == 0) {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "删除失败");
        }
        for (AliOssFile aliOssFile : aliOssFiles) {
            List<String> vectorIds = JSON.parseArray(aliOssFile.getVectorId(), String.class);
            vectorStore.delete(vectorIds);
            aliOssUtil.deleteOss(aliOssFile.getUrl());
        }
        return ResultUtils.success("成功删除" + count + "个文件");
    }

    @Override
    public BaseResponse downloadFiles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择文件");
        }
        List<AliOssFile> aliOssFiles = aliOssFileMapper.selectByIds(ids);
        for (AliOssFile aliOssFile : aliOssFiles) {
            aliOssUtil.download(aliOssFile.getUrl());
        }
        return ResultUtils.success("下载成功");
    }

}




