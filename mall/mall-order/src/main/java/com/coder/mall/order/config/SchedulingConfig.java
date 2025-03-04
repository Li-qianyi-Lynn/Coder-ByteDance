package com.coder.mall.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@Slf4j
public class SchedulingConfig {
    
    public SchedulingConfig() {
        log.info("定时任务配置初始化完成");
    }
} 