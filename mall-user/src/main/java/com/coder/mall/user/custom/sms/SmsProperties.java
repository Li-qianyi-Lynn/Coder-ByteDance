package com.coder.mall.user.custom.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
}
