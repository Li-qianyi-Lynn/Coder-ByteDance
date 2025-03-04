package com.coder.mall.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MallCheckoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallCheckoutApplication.class, args);
    }
}