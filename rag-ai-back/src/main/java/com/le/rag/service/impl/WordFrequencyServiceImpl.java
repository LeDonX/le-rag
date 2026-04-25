package com.le.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.le.rag.common.PageResult;
import com.le.rag.entity.WordFrequency;
import com.le.rag.pojo.dto.WordFrequencyPageQueryDTO;
import com.le.rag.service.WordFrequencyService;
import com.le.rag.mapper.WordFrequencyMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author LeDon
* @description 针对表【word_frequency】的数据库操作Service实现
* @createDate 2025-03-06 15:56:07
*/
@Service
public class WordFrequencyServiceImpl extends ServiceImpl<WordFrequencyMapper, WordFrequency>
    implements WordFrequencyService{

    @Override
    public PageResult pageQuery(WordFrequencyPageQueryDTO queryDTO) {
        Page<WordFrequency> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<WordFrequency> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryDTO.getWord()),
                        WordFrequency::getWord, queryDTO.getWord())
                .eq(StringUtils.isNotEmpty(queryDTO.getBusinessType()),
                        WordFrequency::getBusinessType, queryDTO.getBusinessType())
                .gt(queryDTO.getCountNumMin() != null,
                        WordFrequency::getCountNum, queryDTO.getCountNumMin())
                .orderByDesc(WordFrequency::getCountNum);

        this.page(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }
}




