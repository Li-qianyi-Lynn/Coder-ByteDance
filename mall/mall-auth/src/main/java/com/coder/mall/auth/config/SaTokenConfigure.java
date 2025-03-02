package com.coder.mall.auth.config;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new  SaInterceptor(handler -> {
                    // 登录校验
                    SaRouter.match("/api/**")  // 拦截路径
                            .notMatch( // 排除路径
                                    "/auth/token",
                                    "/doc.html",
                                    "/webjars/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources",
                                    "/favicon.ico",
                                    "/error",
                                    "/static/**"
                            )
                            .check(StpUtil::checkLogin); // 校验逻辑
                }))
                .order(1) // 建议设置优先级
                .addPathPatterns("/**"); // 全局拦截
    }
}