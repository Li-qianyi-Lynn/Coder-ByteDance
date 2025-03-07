package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveData(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}

