package com.coder.mall.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.Retryable;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.coder.mall.comment.biz.domain.mapper")
@Retryable
@EnableFeignClients(basePackages = "com.coder.mall")
public class MallCommentBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallCommentBizApplication.class, args);
    }
}
