package com.coder.mall.order.service;

import com.coder.mall.order.model.message.OrderMessage;

public interface OrderMessageService {
    
    /**
     * 发送订单创建消息
     * @param message 订单消息
     */
    void sendOrderPlacedMessage(OrderMessage message);
    
    /**
     * 发送订单消息并等待支付链接返回
     * @param message 订单消息
     * @return 支付链接
     */
    String sendOrderPlacedMessageAndWaitForPaymentUrl(OrderMessage message);
    
    /**
     * 发送订单状态变更消息
     * @param message 订单消息
     */
    void sendOrderStatusChangeMessage(OrderMessage message);
} 