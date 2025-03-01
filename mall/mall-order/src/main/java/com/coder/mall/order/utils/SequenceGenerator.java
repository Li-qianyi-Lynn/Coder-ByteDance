package com.coder.mall.order.utils;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SequenceGenerator {
    // @Autowired
    // private MongoTemplate mongoTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SEQUENCE_KEY_PREFIX = "mall:order:sequence:";

    public String getNextSequence(String key) {
        String sequenceKey = SEQUENCE_KEY_PREFIX + key;
        Long sequence = redisTemplate.opsForValue().increment(sequenceKey);
        return String.format("%06d", sequence);  // 生成6位数字的序列号
    }
} 