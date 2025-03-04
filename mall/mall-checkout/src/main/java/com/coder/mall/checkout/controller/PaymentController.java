package com.coder.mall.checkout.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.coder.framework.common.response.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/createurl")
    public Response<Map<String, Object>> createPaymentUrl(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody Map<String, Object> request) {

        log.info("收到创建支付链接请求 - userId: {}, request: {}", userId, request);

        String orderNo = (String) request.get("orderNo");
        BigDecimal totalAmount = new BigDecimal(request.get("totalAmount").toString());

        // 构建模拟的支付URL
        String paymentUrl = String.format("//localhost:3000/pay?orderNo=%s", orderNo);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("totalAmount", totalAmount);
        data.put("paymentUrl", paymentUrl);

        log.info("生成支付链接成功 - orderNo: {}, paymentUrl: {}", orderNo, paymentUrl);

        return Response.success(data);
    }
}