package com.le.rag.mapper;

import com.le.rag.entity.SensitiveWord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LeDon
* @description 针对表【sensitive_word】的数据库操作Mapper
* @createDate 2025-03-03 21:29:10
* @Entity com.le.rag.entity.SensitiveWord
*/

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {

}




