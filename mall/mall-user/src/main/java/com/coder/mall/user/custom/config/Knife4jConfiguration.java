package com.coder.mall.user.custom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("User服务接口").version("1.0").description("User服务接口").termsOfService("http://doc.xiaominfo.com").license(new License().name("Apache 2.0").url("http://doc.xiaominfo.com")));
    }

    @Bean
    public GroupedOpenApi userAPI() {
        return GroupedOpenApi.builder().group("用户模块").pathsToMatch("/user/**").build();
    }
}