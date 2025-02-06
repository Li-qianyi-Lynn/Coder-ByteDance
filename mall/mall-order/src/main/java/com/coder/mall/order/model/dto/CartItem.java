package com.coder.mall.order.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {
    private String productId;
    private Integer quantity;
    private Boolean isValid;
    private BigDecimal unitPrice;
}
