package com.coder.mall.order.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.coder.framework.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.mapper.CustomerOrderMapper;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.mq.OrderCancelEvent;
import com.coder.mall.order.service.OrderScheduleService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderScheduleServiceImpl implements OrderScheduleService {

    private static final String ORDER_TIMEOUT_KEY = "order:timeout";
    private static final String TIMEOUT_LOCK_KEY = "order:timeout:lock";
    private static final long LOCK_EXPIRY = 60000; // 60秒锁过期时间
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
                
                // 使用pipeline或multi来保证原子性
                Set<Object> timeoutOrders = redisTemplate.opsForZSet()
                    .rangeByScore(ORDER_TIMEOUT_KEY, 0, maxScore);
                
                if (timeoutOrders == null || timeoutOrders.isEmpty()) {
                    return new HashSet<>();
                }
                
                // 记录当前获取的超时订单（调试用）
                log.debug("从Redis获取到超时订单: {}", timeoutOrders);
                
                return timeoutOrders.stream()
                    .map(obj -> {
                        String str = obj.toString();
                        // 处理可能包含引号的情况
                        if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 2) {
                            str = str.substring(1, str.length() - 1);
                        }
                        return str;
                    })
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
        int attempts = 0;
        boolean removed = false;
        
        while (attempts < MAX_RETRY_ATTEMPTS && !removed) {
            try {
                // 尝试多种格式查找和删除
                String[] possibleFormats = {
                    orderNo,                  // 原始格式
                    "\"" + orderNo + "\"",    // 带双引号
                    "[" + orderNo + "]",      // 带方括号
                    "\"[" + orderNo + "]\"",  // 带引号的方括号
                    "[\"" + orderNo + "\"]",  // 方括号内带引号
                };
                
                for (String format : possibleFormats) {
                    // 检查此格式是否存在
                    Double score = redisTemplate.opsForZSet().score(ORDER_TIMEOUT_KEY, format);
                    
                    if (score != null) {
                        log.info("订单{}在超时队列中找到，格式为: {}", orderNo, format);
                        Long result = redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, format);
                        
                        if (result != null && result > 0) {
                            log.info("订单{}已从超时队列中移除", orderNo);
                            removed = true;
                            break;
                        }
                    }
                }
                
                // 如果尝试了所有格式但仍未删除成功
                if (!removed) {
                    // 尝试逐个比较队列中的元素
                    Set<Object> allMembers = redisTemplate.opsForZSet().range(ORDER_TIMEOUT_KEY, 0, -1);
                    if (allMembers != null) {
                        for (Object member : allMembers) {
                            if (member.toString().contains(orderNo)) {
                                Long result = redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, member);
                                if (result != null && result > 0) {
                                    log.info("通过内容匹配方式移除订单{}", orderNo);
                                    removed = true;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!removed) {
                        attempts++;
                        log.warn("订单{}未能通过任何格式从队列中找到或删除", orderNo);
                        if (attempts < MAX_RETRY_ATTEMPTS) {
                            Thread.sleep(RETRY_DELAY_MS);
                        }
                    }
                }
            } catch (Exception e) {
                attempts++;
                log.error("从超时队列移除订单{}失败: {}", orderNo, e.getMessage());
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void cancelTimeoutOrder(String orderNo, Long userId) {
        log.info("订单{}超时未支付，发布自动取消事件", orderNo);
        
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("orderNo", orderNo);  
        orderInfo.put("userId", userId);    
        
        // 发布订单取消事件而不是直接调用服务
        eventPublisher.publishEvent(new OrderCancelEvent(orderInfo));
    }

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkTimeoutOrders() {
        // 尝试获取分布式锁
        Boolean locked = false;
        try {
            // 尝试兼容不同版本的Redis客户端
            locked = redisTemplate.opsForValue().setIfAbsent(TIMEOUT_LOCK_KEY, "1", LOCK_EXPIRY, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 如果新API不支持，尝试旧API
            log.warn("使用带超时参数的setIfAbsent失败，尝试替代方法: {}", e.getMessage());
            try {
                locked = redisTemplate.opsForValue().setIfAbsent(TIMEOUT_LOCK_KEY, "1");
                if (locked != null && locked) {
                    redisTemplate.expire(TIMEOUT_LOCK_KEY, LOCK_EXPIRY, TimeUnit.MILLISECONDS);
                }
            } catch (Exception ex) {
                log.error("获取分布式锁失败: {}", ex.getMessage());
                return;
            }
        }
        
        if (locked == null || !locked) {
            log.info("另一个实例正在处理超时订单，本次跳过");
            return;
        }
        
        try {
            Set<String> timeoutOrders = getTimeoutOrders();
            if (!timeoutOrders.isEmpty()) {
                log.info("发现{}个超时订单", timeoutOrders.size());
                
                // 处理每个超时订单
                for (String orderNo : timeoutOrders) {
                    try {
                        if (!isValidOrderNo(orderNo)) {
                            log.warn("无效订单号格式，从队列移除: {}", orderNo);
                            removeFromTimeoutQueue(orderNo);
                            continue;
                        }
                        
                        // 查询订单状态
                        CustomerOrder order = customerOrderMapper.selectByOrderNo(orderNo);
                        
                        // 如果订单不存在，从队列中移除
                        if (order == null) {
                            log.warn("订单不存在，从队列移除: {}", orderNo);
                            removeFromTimeoutQueue(orderNo);
                            continue;
                        }
                        
                        // 如果订单状态为CREATED或PENDING_PAYMENT，则自动取消
                        if (OrderStatus.CREATED.equals(order.getStatus()) || 
                            OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
                            // 不再直接调用orderService
                            cancelTimeoutOrder(orderNo, order.getUserId());
                        }
                        
                        // 无论如何，处理完后从队列中移除
                        removeFromTimeoutQueue(orderNo);
                        
                    } catch (Exception e) {
                        log.error("处理超时订单{}时发生错误: {}", orderNo, e.getMessage(), e);
                        removeFromTimeoutQueue(orderNo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查超时订单时发生错误: {}", e.getMessage(), e);
        } finally {
            // 释放锁
            redisTemplate.delete(TIMEOUT_LOCK_KEY);
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

    /**
     * 定期清理Redis队列中的过期和已处理订单
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTimeoutQueue() {
        // 尝试获取分布式锁
        Boolean locked = redisTemplate.opsForValue().setIfAbsent("order:cleanup:lock", "1", 30*60*1000, TimeUnit.MILLISECONDS);
        if (locked == null || !locked) {
            return;
        }
        
        try {
            log.info("开始清理超时订单队列...");
            Set<Object> allOrders = redisTemplate.opsForZSet().range(ORDER_TIMEOUT_KEY, 0, -1);
            
            if (allOrders == null || allOrders.isEmpty()) {
                log.info("超时队列为空，无需清理");
                return;
            }
            
            log.info("找到{}个订单记录需要检查", allOrders.size());
            int removed = 0;
            
            for (Object orderObj : allOrders) {
                String orderNoWithQuotes = orderObj.toString();
                String orderNo = orderNoWithQuotes;
                
                // 移除可能的引号
                if (orderNo.startsWith("\"") && orderNo.endsWith("\"")) {
                    orderNo = orderNo.substring(1, orderNo.length() - 1);
                }
                
                try {
                    // 检查订单状态
                    CustomerOrder order = customerOrderMapper.selectByOrderNo(orderNo);
                    
                    // 如果订单不存在或已处理，从队列移除
                    if (order == null || 
                       !OrderStatus.CREATED.equals(order.getStatus()) && 
                       !OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
                        redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderObj);
                        removed++;
                    }
                } catch (Exception e) {
                    log.error("检查订单{}状态时出错: {}", orderNo, e.getMessage());
                }
            }
            
            log.info("队列清理完成，移除了{}个过期或已处理的订单", removed);
        } catch (Exception e) {
            log.error("清理超时队列时发生错误: {}", e.getMessage(), e);
        } finally {
            redisTemplate.delete("order:cleanup:lock");
        }
    }
} 