package com.coder.mall.order.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.mapper.CustomerOrderMapper;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.service.OrderScheduleService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderScheduleServiceImpl implements OrderScheduleService {

    private static final String ORDER_TIMEOUT_KEY = "order:timeout";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    @Override
    public void addOrderToTimeoutQueue(String orderNo, long timeout, TimeUnit unit) {
        // 计算过期时间戳
        double score = System.currentTimeMillis() + unit.toMillis(timeout);
        redisTemplate.opsForZSet().add(ORDER_TIMEOUT_KEY, orderNo, score);
        log.info("订单{}已加入延迟取消队列，将在{}后自动取消", orderNo, 
                LocalDateTime.now().plusSeconds(unit.toSeconds(timeout))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Scheduled(fixedRate = 5000)
    public void checkTimeoutOrders() {
        try {
            // 获取当前时间戳
            double now = System.currentTimeMillis();
            // 获取所有到期的订单
            Set<String> timeoutOrders = redisTemplate.opsForZSet()
                    .rangeByScore(ORDER_TIMEOUT_KEY, 0, now);

            if (timeoutOrders == null || timeoutOrders.isEmpty()) {
                return;
            }

            for (String orderNo : timeoutOrders) {
                try {
                    CustomerOrder order = customerOrderMapper.selectByOrderNo(orderNo);
                    if (order != null && OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
                        int rows = customerOrderMapper.updateStatus(orderNo, OrderStatus.CANCELLED.name());
                        if (rows > 0) {
                            // 从集合中移除已处理的订单
                            redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
                            log.info("订单{}已自动取消", orderNo);
                        }
                    } else {
                        // 订单状态不符合取消条件，直接从队列移除
                        redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
                        log.info("订单{}不需要取消，状态：{}", orderNo, order != null ? order.getStatus() : "不存在");
                    }
                } catch (Exception e) {
                    log.error("处理订单{}时发生错误: {}", orderNo, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查超时订单时发生错误: {}", e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getTimeoutOrders() {
        double now = System.currentTimeMillis();
        return redisTemplate.opsForZSet().rangeByScore(ORDER_TIMEOUT_KEY, 0, now);
    }

    @Override
    public void removeFromTimeoutQueue(String orderNo) {
        redisTemplate.opsForZSet().remove(ORDER_TIMEOUT_KEY, orderNo);
        log.info("订单{}已从延迟取消队列中移除", orderNo);
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