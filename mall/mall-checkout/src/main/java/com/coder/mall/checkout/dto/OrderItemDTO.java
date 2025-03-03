package com.coder.mall.checkout.dto;

import lombok.Data;

import java.math.BigDecimal;

// OrderItemDTO.java（请求用）
@Data
public class OrderItemDTO {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    // 移除actualPrice和totalPrice字段
}

