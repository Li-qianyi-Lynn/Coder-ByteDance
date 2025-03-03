package com.coder.mall.order.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coder.mall.order.service.OrderScheduleService;
import com.coder.mall.order.service.impl.OrderScheduleServiceImpl;

@RestController
@RequestMapping("/api/admin/order")
public class OrderAdminController {

    private static final String ORDER_TIMEOUT_KEY = "order:timeout";
    
        @Autowired
        private OrderScheduleService orderScheduleService;
    
        @Autowired
        private RedisTemplate<String, Object> redisTemplate;
    
        @DeleteMapping("/run-cleanup")
        public Map<String, Object> runQueueCleanup() {
            Map<String, Object> result = new HashMap<>();
            try {
                // 手动触发队列清理方法
                ((OrderScheduleServiceImpl)orderScheduleService).cleanupTimeoutQueue();
                result.put("success", true);
                result.put("message", "清理任务已执行");
            } catch (Exception e) {
                result.put("success", false);
                result.put("error", e.getMessage());
            }
            return result;
        }

    @DeleteMapping("/clear-timeout-queue")
    public Map<String, Object> clearTimeoutQueue() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 删除整个超时队列
            Boolean deleted = redisTemplate.delete(ORDER_TIMEOUT_KEY);
            
            result.put("success", deleted != null && deleted);
            result.put("message", deleted != null && deleted ? 
                      "超时队列已成功清空" : "队列不存在或清空失败");
                      
            // 验证队列已清空
            Set<Object> remainingItems = redisTemplate.opsForZSet().range(ORDER_TIMEOUT_KEY, 0, -1);
            result.put("queueEmpty", remainingItems == null || remainingItems.isEmpty());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

}


    
    