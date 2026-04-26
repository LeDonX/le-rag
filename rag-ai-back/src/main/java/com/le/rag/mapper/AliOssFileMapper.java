package com.le.rag.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.le.rag.entity.AliOssFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LeDon
* @description 针对表【ali_oss_file】的数据库操作Mapper
* @Entity com.le.rag.entity.AliOssFile
*/

@Mapper
public interface AliOssFileMapper extends BaseMapper<AliOssFile> {

    IPage<AliOssFile> findByFileNameContaining(Page<AliOssFile> page, String fileName);
}




