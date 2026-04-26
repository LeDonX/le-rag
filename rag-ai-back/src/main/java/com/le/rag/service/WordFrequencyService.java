package com.le.rag.service;

import com.le.rag.common.PageResult;
import com.le.rag.entity.WordFrequency;
import com.baomidou.mybatisplus.extension.service.IService;
import com.le.rag.pojo.dto.WordFrequencyPageQueryDTO;

/**
* @author LeDon
* @description 针对表【word_frequency】的数据库操作Service
*/
public interface WordFrequencyService extends IService<WordFrequency> {

    PageResult pageQuery(WordFrequencyPageQueryDTO queryDTO);
}
