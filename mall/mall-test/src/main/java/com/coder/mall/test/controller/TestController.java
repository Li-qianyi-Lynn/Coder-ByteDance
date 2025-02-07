package com.coder.mall.test.controller;

import com.coder.framework.common.response.Response;
import com.coder.framework.common.util.JsonUtils;
import com.coder.framework.biz.operationlog.aspect.ApiOperationLog;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TestController {

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperationLog("测试接口")
    @PostMapping("/test")
    public Response<?> test() {
        LocalDateTime now = LocalDateTime.now();
        String jsonString = JsonUtils.toJsonString(now);
        redisTemplate.opsForValue().set("now", jsonString);

        return Response.success(now);
    }
}
