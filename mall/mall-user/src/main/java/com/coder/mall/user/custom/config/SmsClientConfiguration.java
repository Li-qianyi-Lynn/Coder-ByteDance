package com.coder.mall.user.custom.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.coder.mall.user.custom.sms.SmsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
public class SmsClientConfiguration {

    @Autowired
    private SmsProperties properties;

    @Bean
    public Client createClient() {//1.使用短信服务，需要先创建一个Client对象
        //创建Client对象需要config
        Config config = new Config();
        config.setAccessKeyId(properties.getAccessKeyId());
        config.setAccessKeySecret(properties.getAccessKeySecret());
        config.setEndpoint(properties.getEndpoint());//在set这个属性时，因为提到了endpoint，所以该配置类生效
        try {
            return new Client(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
