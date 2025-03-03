package com.coder.mall.order.mq;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.coder.mall.order.model.dto.OrderCancelDTO;
import com.coder.mall.order.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderEventListener {

    @Autowired
    private OrderService orderService;
    
    @EventListener
    public void handleOrderCancelEvent(OrderCancelEvent event) {
        Map<String, Object> orderInfo = event.getOrderInfo();
        log.info("接收到订单取消事件: {}", orderInfo);
        
        OrderCancelDTO cancelDTO = new OrderCancelDTO();
        cancelDTO.setOrderNo(orderInfo.get("orderNo").toString());
        cancelDTO.setUserId((Long) orderInfo.get("userId"));
        
        orderService.cancelOrder(cancelDTO);
        log.info("订单{}已自动取消", orderInfo.get("orderNo"));
    }
}