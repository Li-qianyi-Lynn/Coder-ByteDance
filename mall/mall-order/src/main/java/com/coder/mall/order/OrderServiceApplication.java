package com.coder.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableRabbit
@EnableFeignClients
@EnableMongoRepositories(basePackages = "com.coder.mall.order.repository")
@MapperScan("com.coder.mall.order.mapper")
@Slf4j
@Component
public class OrderServiceApplication {
    
    @Autowired
    private Environment env;
    
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
    
    @PostConstruct
    public void logRedisConfig() {
        log.info("Redis Configuration:");
        log.info("Host: {}", env.getProperty("spring.redis.host"));
        log.info("Port: {}", env.getProperty("spring.redis.port"));
        log.info("Database: {}", env.getProperty("spring.redis.database"));
    }
}