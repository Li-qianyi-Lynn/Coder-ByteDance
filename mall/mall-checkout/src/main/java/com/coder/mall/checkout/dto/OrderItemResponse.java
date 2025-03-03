package com.coder.mall.checkout.dto;

import lombok.Data;

import java.math.BigDecimal;

// OrderItemResponse.java（响应用）
@Data
public class OrderItemResponse {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal actualPrice; // 仅响应时包含
    private BigDecimal totalPrice;
}
