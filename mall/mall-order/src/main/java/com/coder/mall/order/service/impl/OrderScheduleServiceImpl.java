package com.coder.mall.order.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.coder.framework.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.mapper.CustomerOrderMapper;
import com.coder.mall.order.service.OrderScheduleService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderScheduleServiceImpl implements OrderScheduleService {

    private static final String ORDER_TIMEOUT_KEY = "order:timeout";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1秒

    @Override
    public void addOrderToTimeoutQueue(String orderNo, long timeout, TimeUnit timeUnit) {
        try {
            double score = System.currentTimeMillis() + timeUnit.toMillis(timeout);
            redisTemplate.opsForZSet().add(ORDER_TIMEOUT_KEY, orderNo, score);
            log.info("订单{}已添加到超时队列，将在{}{}后超时", orderNo, timeout, timeUnit);
        } catch (Exception e) {
            log.error("添加订单到超时队列失败: {}", e.getMessage(), e);
            throw new BizException(OrderErrorEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public Set<String> getTimeoutOrders() {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                double maxScore = System.currentTimeMillis();
                Set<Object> timeoutOrders = redisTemplate.opsForZSet()
                    .rangeByScore(ORDER_TIMEOUT_KEY, 0, maxScore);
                
                if (timeoutOrders == null) {
                    return new HashSet<>();
                }
                return timeoutOrders.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
                
            } catch (Exception e) {
                attempts++;
                log.warn("获取超时订单失败，第{}次重试: {}", attempts, e.getMessage());
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("获取超时订单最终失败", e);
                    return new HashSet<>(); // 返回空集合而不是抛出异常
                }
                
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return new HashSet<>();
                }
            }
        }
        return new HashSet<>();
    }

    @Override
    public void removeFromTimeoutQueue(String orderNo) {
        try {
            redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
            log.info("订单{}已从超时队列中移除", orderNo);
        } catch (Exception e) {
            log.error("从超时队列移除订单失败: {}", e.getMessage(), e);
            // 这里选择记录日志但不抛出异常，因为订单已经处理完成
        }
    }

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkTimeoutOrders() {
        try {
            Set<String> timeoutOrders = getTimeoutOrders();
            if (!timeoutOrders.isEmpty()) {
                log.info("发现{}个超时订单", timeoutOrders.size());
            }
        } catch (Exception e) {
            log.error("检查超时订单时发生错误: {}", e.getMessage(), e);
            // 记录错误但不中断定时任务
        }
    }
    
    /**
     * 验证订单号格式是否正确
     * 订单号格式为：yyyyMMddHHmmssxxxxxx (20位)
     */
    private boolean isValidOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return false;
        }
        
        // 验证订单号长度和格式
        if (orderNo.length() != 20) {  // 修改为20位
            log.warn("无效的订单号格式: {} (长度应为20位)", orderNo);
            return false;
        }
        
        // 验证前缀是否是日期格式 (前14位: yyyyMMddHHmmss)
        String dateStr = orderNo.substring(0, 14);
        try {
            LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            return true;
        } catch (Exception e) {
            log.warn("无效的订单号日期格式: {} (前14位应为yyyyMMddHHmmss)", orderNo);
            return false;
        }
    }
} 