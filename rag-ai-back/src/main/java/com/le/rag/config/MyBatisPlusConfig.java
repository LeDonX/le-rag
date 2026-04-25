package com.le.rag.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.le.rag.mapper")
public class MyBatisPlusConfig {
}