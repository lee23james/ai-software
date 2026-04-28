package com.example.jobplatform.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.jobplatform.mapper")
public class MyBatisConfig {
}

