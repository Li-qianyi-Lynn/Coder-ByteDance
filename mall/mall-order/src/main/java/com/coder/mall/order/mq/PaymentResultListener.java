package com.coder.mall.order.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.model.entity.PaymentResult;
import com.coder.mall.order.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentResultListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "payment.result.queue")
    public void handlePaymentResult(PaymentResult result) {
        log.info("收到支付结果消息: {}", result);
        try {
            // 根据支付结果更新订单状态
            OrderStatus newStatus = result.isSuccess() ? 
                OrderStatus.PAID : OrderStatus.FAILED;
            
            orderService.updateOrderPaymentStatus(result.getOrderNo(), newStatus);
            
            log.info("订单支付状态更新成功: orderNo={}, status={}", 
                result.getOrderNo(), newStatus);
        } catch (Exception e) {
            log.error("处理支付结果消息失败: {}", e.getMessage(), e);
        }
    }
} 