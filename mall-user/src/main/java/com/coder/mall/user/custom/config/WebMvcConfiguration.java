package com.coder.mall.user.custom.config;

import com.coder.mall.user.custom.converter.StringToUserTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

//    @Autowired
//    private StringToUserTypeConverter stringToUserTypeConverter;//注入的是前端字符串转java枚举类的转换器工厂
//
//    @Override
//    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverterFactory(this.stringToUserTypeConverter);
//    }

}
