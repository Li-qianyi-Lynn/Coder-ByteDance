package com.coder.mall.order.mq;

import java.util.Map;

public class OrderCancelEvent {
    private final Map<String, Object> orderInfo;
    
    public OrderCancelEvent(Map<String, Object> orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public Map<String, Object> getOrderInfo() {
        return orderInfo;
    }
}