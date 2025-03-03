package com.coder.mall.order.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import com.coder.framework.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.constant.RedisKeyConstant;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderNoGenerator {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${spring.application.name:mall-order}")
    private String applicationName;
    
    public String generateOrderNo() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.BASIC_ISO_DATE);
            String key = RedisKeyConstant.ORDER_SEQUENCE + ":" + dateStr;
            
            return redisTemplate.execute(new SessionCallback<String>() {
                @Override
                public String execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    
                    operations.opsForValue().increment(key);
                    operations.expire(key, 48, TimeUnit.HOURS);
                    
                    List<Object> results = operations.exec();
                    if (results != null && !results.isEmpty()) {
                        Long sequence = (Long) results.get(0);
                        return String.format("%s%06d", 
                            now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), 
                            sequence);
                    }
                    
                    log.error("Generate order no failed: Redis transaction returned null");
                    throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
                }
            });
        } catch (Exception e) {
            log.error("Generate order no failed", e);
            throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
        }
    }
    
}