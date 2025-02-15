package com.coder.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CartApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(CartApplication.class, args);
    }
}