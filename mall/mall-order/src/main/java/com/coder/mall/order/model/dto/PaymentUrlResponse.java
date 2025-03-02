package com.coder.mall.order.model.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUrlResponse {
    private String orderNo;
    private BigDecimal totalAmount;
    private String paymentUrl;
} 