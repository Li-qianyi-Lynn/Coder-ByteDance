package com.coder.mall.order.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDTO {
    private String productId;
    private String dealerId;
    private String productName;
    private BigDecimal price;
    // 其他必要的商品信息
} 