package com.coder.mall.order.service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface OrderScheduleService {
    /**
     * 将订单加入延迟取消队列
     */
    void addOrderToTimeoutQueue(String orderNo, long timeout, TimeUnit unit);

    /**
     * 获取所有超时订单号
     */
    Set<String> getTimeoutOrders();

    /**
     * 从队列中移除订单
     */
    void removeFromTimeoutQueue(String orderNo);
} 