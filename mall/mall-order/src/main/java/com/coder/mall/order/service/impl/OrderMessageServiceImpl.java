package com.coder.mall.order.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coder.framework.common.response.Response;
import com.coder.mall.order.model.dto.PaymentUrlResponse;
import com.coder.mall.order.model.message.OrderMessage;
import com.coder.mall.order.service.OrderMessageService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderMessageServiceImpl implements OrderMessageService {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    public void sendOrderPlacedMessage(OrderMessage message) {
        try {
            rabbitTemplate.convertAndSend("order.exchange", "order.placed", message);
            log.info("订单创建消息发送成功: {}", message.getOrderNo());
        } catch (Exception e) {
            log.error("订单创建消息发送失败: {}", message.getOrderNo(), e);
            // 可以考虑重试或其他错误处理
        }
    }
    
    @Override
    public String sendOrderPlacedMessageAndWaitForPaymentUrl(OrderMessage message) {
        try {
            log.info("开始获取支付链接, orderNo: {}", message.getOrderNo());
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("orderNo", message.getOrderNo());
            requestBody.put("totalAmount", message.getExtraData().get("totalAmount"));
            requestBody.put("currency", message.getExtraData().get("currency"));
            
            // 发送HTTP请求到结算服务
            ResponseEntity<Response<PaymentUrlResponse>> response = restTemplate.exchange(
                "http://mall-checkout/api/payments/createurl",
                HttpMethod.POST,
                new HttpEntity<>(requestBody, createHeaders(message.getUserId())),
                new ParameterizedTypeReference<Response<PaymentUrlResponse>>() {}
            );
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("获取支付链接失败, orderNo: {}, statusCode: {}", 
                    message.getOrderNo(), response.getStatusCode());
                return null;
            }
            
            PaymentUrlResponse paymentUrlResponse = response.getBody().getData();
            if (paymentUrlResponse == null || paymentUrlResponse.getPaymentUrl() == null) {
                log.error("支付链接为空, orderNo: {}", message.getOrderNo());
                return null;
            }
            
            log.info("成功获取支付链接, orderNo: {}, paymentUrl: {}", 
                message.getOrderNo(), paymentUrlResponse.getPaymentUrl());
                
            return paymentUrlResponse.getPaymentUrl();
            
        } catch (Exception e) {
            log.error("调用结算服务失败, orderNo: {}, error: {}", 
                message.getOrderNo(), e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void sendOrderStatusChangeMessage(OrderMessage message) {
        try {
            String routingKey = "order.status." + message.getStatus().toString().toLowerCase();
            rabbitTemplate.convertAndSend("order.exchange", routingKey, message);
            log.info("订单状态变更消息发送成功: {}, 状态: {}", message.getOrderNo(), message.getStatus());
        } catch (Exception e) {
            log.error("订单状态变更消息发送失败: {}", message.getOrderNo(), e);
        }
    }
    
    // 创建请求头
    private HttpHeaders createHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-ID", String.valueOf(userId));
        return headers;
    }
} 